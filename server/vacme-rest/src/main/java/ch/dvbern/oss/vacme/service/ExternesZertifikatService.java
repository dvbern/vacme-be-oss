/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.base.HasImpfdossierId;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.jax.registration.ExternGeimpftJax;
import ch.dvbern.oss.vacme.repo.ExternesZertifikatRepo;
import ch.dvbern.oss.vacme.service.booster.BoosterService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ImpfinformationDtoRecreator;
import ch.dvbern.oss.vacme.util.ValidationUtil;
import ch.dvbern.oss.vacme.wrapper.VacmeDecoratorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExternesZertifikatService {

	private final ExternesZertifikatRepo externesZertifikatRepo;
	private final ImpfstoffService impfstoffService;
	private final UserPrincipal userPrincipal;
	private final ImpfinformationenService impfinformationenService;
	private final BoosterService boosterService;
	private final DossierService dossierService;
	private final ImpfdossierService impfdossierService;

	// Der Impfling kann sein externes Zertifikat editieren, aber nur in freigegeben/registriert/immunisiert und solange er keine
	// Vacme-Impfung gemacht hat.
	public void saveExternGeimpftImpfling(
		@NonNull ImpfinformationDto infos,
		@NonNull ExternGeimpftJax externGeimpftJax,
		boolean suppressBenachrichtigung
	) {

		// Status-Validierung
		ValidationUtil.validateStatusOneOf(infos.getImpfdossier(),
			ImpfdossierStatus.NEU,
			ImpfdossierStatus.FREIGEGEBEN,
			ImpfdossierStatus.IMMUNISIERT,
			ImpfdossierStatus.FREIGEGEBEN_BOOSTER);

		// Validieren: es darf noch keine Vacme-Impfungen haben.
		if (impfinformationenService.hasVacmeImpfungen(infos)) {
			throw AppValidationMessage.EXISTING_VACME_IMPFUNGEN_CANNOT_EDIT_EXTERN_GEIMPFT.create();
		}

		ExternesZertifikat nachher = this.createUpdateOrRemoveExternGeimpft(infos, externGeimpftJax, false);

		ImpfinformationDto infosWithUpdatedZert = ImpfinformationDtoRecreator
			.from(infos)
			.withExternemZertifikat(nachher)
			.build();

		ImpfdossierStatus statusVorher = infosWithUpdatedZert.getImpfdossier().getDossierStatus();
		switch (statusVorher) {
		case IMMUNISIERT:
		case FREIGEGEBEN_BOOSTER:
			if (infos.getKrankheitIdentifier().isSupportsNImpfungenWithoutGrundimmunisierung()) {
				VacmeDecoratorFactory.getDecorator(infos.getKrankheitIdentifier())
					.recalculateVollstaendigerImpfschutzTypBasedOnImmunsupprimiert(infosWithUpdatedZert, false);
			} else {
				if (nachher == null || !nachher.isGrundimmunisiert(infos.getKrankheitIdentifier())) {
					// Externes Zertifikat wurde weggenommen oder gibt keine grundimmunisierung mehr -> zurueck zum Start.
					infosWithUpdatedZert.getImpfdossier().setStatusToNichtAbgeschlossenStatus(
						infos,
						impfdossierService.ermittleLetztenDossierStatusVorKontrolle1(infosWithUpdatedZert.getImpfdossier()),
						infos.getImpfung1());
				}
			}
			break;
		case NEU:
		case FREIGEGEBEN:
			if (nachher != null && nachher.isGrundimmunisiert(infos.getKrankheitIdentifier())) {
				// vollstaendiges externes Zertifikat neu hinzugefuegt im I1/2 Modus -> zu immunisiert springen
				boolean immunsupprimiert = false; // Wir hatten hier noch gar keine Gelegenheit, das Flag zu setzen
				VacmeDecoratorFactory.getDecorator(infos.getKrankheitIdentifier())
					.setStatusToImmunisiertWithExternZertifikat(infosWithUpdatedZert, nachher, immunsupprimiert);
			}
			break;
		default:
			// wird nicht passieren, siehe Status-Validierung am Anfang dieser Methode.
			throw new IllegalStateException("Unerwarteter Status: " + statusVorher);
		}
		infosWithUpdatedZert.getImpfdossier().getBuchung().setExternGeimpftConfirmationNeeded(false); // is now done
		boosterService.recalculateImpfschutzAndStatusmovesForSingleReg(infosWithUpdatedZert, suppressBenachrichtigung);
		dossierService.freigabestatusUndTermineEntziehenFallsImpfschutzNochNichtFreigegeben(infosWithUpdatedZert);
	}

	/**
	 * 4 Faelle:
	 * - A. vorher ohne - nachher ohne
	 * - B. vorher ohne - nachher mit
	 * - C. vorher mit - nachher ohne
	 * - D. vorher mit - nachher mit
	 */
	@Nullable
	public ExternesZertifikat createUpdateOrRemoveExternGeimpft(
		@NonNull ImpfinformationDto infos,
		@NonNull ExternGeimpftJax externGeimpftJax,
		boolean kontrolle
	) {
		if (!infos.getKrankheitIdentifier().isSupportsExternesZertifikat()) {
			throw AppValidationMessage.ILLEGAL_STATE.create(
				"createUpdateOrRemoveExternGeimpft was called for Krankheit that does not support ExternesZertifikat "
					+ infos.getKrankheitIdentifier());
		}

		ExternesZertifikat existingInfo = findExternesZertifikatForDossier(infos).orElse(null);
		if (existingInfo == null) {
			if (externGeimpftJax.isExternGeimpft()) {
				// B. neu erstellen
				return createExternGeimpft(infos, externGeimpftJax, kontrolle);
			}
			// A. vorher und nachher nicht extern geimpft
			return null;
		} else {
			if (externGeimpftJax.isExternGeimpft()) {
				// D. Aenderung der externen Impfung
				updateExternGeimpft(infos, externGeimpftJax, existingInfo, kontrolle);
				return existingInfo;
			} else {
				// C. extern loeschen
				remove(existingInfo);
				return null;
			}
		}
	}

	public boolean isExternesZertGrundimmunisiertUndKontrolliert(@Nullable ExternesZertifikat externesZertifikatOrNull) {
		return externesZertifikatOrNull != null
			&& externesZertifikatOrNull.isKontrolliert()
			&& externesZertifikatOrNull.isGrundimmunisiert(externesZertifikatOrNull.getImpfdossier().getKrankheitIdentifier());
	}

	@NonNull
	public ExternesZertifikat createExternGeimpft(@NonNull ImpfinformationDto infos, @NonNull ExternGeimpftJax externGeimpftJax, boolean kontrolle) {

		// Validieren: es darf noch keine Vacme-Impfungen fuer diese Krankheit haben.
		if (impfinformationenService.hasVacmeImpfungen(infos)) {
			throw AppValidationMessage.EXISTING_VACME_IMPFUNGEN_CANNOT_ADD_EXTERN_GEIMPFT.create();
		}

		ExternesZertifikat externesZertifikat = new ExternesZertifikat();
		externesZertifikat.setImpfdossier(infos.getImpfdossier());

		updateExternesZertifikatBasic(externGeimpftJax, externesZertifikat, kontrolle);

		create(externesZertifikat);
		return externesZertifikat;
	}

	private void updateExternGeimpft(
		@NonNull ImpfinformationDto infos,
		@NonNull ExternGeimpftJax jax,
		@NonNull ExternesZertifikat externesZertifikat,
		boolean kontrolle
	) {
		if (!externesZertifikat.getImpfdossier().equals(infos.getImpfdossier())) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Dossier auf externem Zertifikat != Dossier auf infos");
		}

		validateAnzahlImpfungenNotReduced(infos, jax, externesZertifikat);
		impfdossierService.updateSchnellschemaFlag(infos.getImpfdossier(), jax.isSchnellschema());
		updateExternesZertifikatBasic(jax, externesZertifikat, kontrolle);

		update(externesZertifikat);
	}

	private void validateAnzahlImpfungenNotReduced(@NonNull ImpfinformationDto infos, @NonNull ExternGeimpftJax jax,
		@NonNull ExternesZertifikat externesZertifikat) {
		assert jax.getAnzahlImpfungen() != null && jax.getAnzahlImpfungen() > 0;
		if (jax.getAnzahlImpfungen() < externesZertifikat.getAnzahlImpfungen()) {
			if (impfinformationenService.hasVacmeImpfungen(infos)) {
				throw AppValidationMessage.EXISTING_VACME_IMPFUNGEN_CANNOT_REDUCE_EXTERN_NUMBER.create();
			}
		}
	}

	private void updateExternesZertifikatBasic(@NonNull ExternGeimpftJax jax, @NonNull ExternesZertifikat externesZertifikat, boolean kontrolle) {
		externesZertifikat.setLetzteImpfungDateUnknown(jax.isLetzteImpfungDateUnknown());
		externesZertifikat.setLetzteImpfungDate(jax.getLetzteImpfungDate());
		Objects.requireNonNull(jax.getImpfstoff());
		Impfstoff impfstoff = impfstoffService.findById(Impfstoff.toId(jax.getImpfstoff().getId()));
		externesZertifikat.setImpfstoff(impfstoff);
		assert jax.getAnzahlImpfungen() != null && jax.getAnzahlImpfungen() > 0;
		externesZertifikat.setAnzahlImpfungen(jax.getAnzahlImpfungen());
		externesZertifikat.setGenesen(Boolean.TRUE.equals(jax.getGenesen()));
		externesZertifikat.setPositivGetestetDatum(jax.getPositivGetestetDatum());

		if (kontrolle) {
			externesZertifikat.setTrotzdemVollstaendigGrundimmunisieren(jax.getTrotzdemVollstaendigGrundimmunisieren());
			if (externesZertifikat.getKontrolliertTimestamp() == null) {
				final Benutzer currentBenutzer = userPrincipal.getBenutzerOrThrowException();
				externesZertifikat.setKontrolliertTimestamp(LocalDateTime.now());
				externesZertifikat.setKontrollePersonUUID(currentBenutzer.getId().toString());
			} else {
				// Wenn es schon kontrolliert wurde, kein neuer Timestamp und User setzen!
			}
		} else {
			// Impfwilliger:
			externesZertifikat.setKontrollePersonUUID(null);
			externesZertifikat.setKontrolliertTimestamp(null);
		}
	}

	private void create(@NonNull ExternesZertifikat externesZertifikat) {
		externesZertifikatRepo.create(externesZertifikat);
	}

	private void update(@NonNull ExternesZertifikat externesZertifikat) {
		externesZertifikatRepo.update(externesZertifikat);
	}

	public void remove(@NonNull ExternesZertifikat externesZertifikat) {
		externesZertifikatRepo.remove(externesZertifikat);
	}

	@NonNull
	public Optional<ExternesZertifikat> findExternesZertifikatForDossier(@NonNull HasImpfdossierId dto) {
		return externesZertifikatRepo.findExternesZertifikatForDossier(dto);
	}
}
