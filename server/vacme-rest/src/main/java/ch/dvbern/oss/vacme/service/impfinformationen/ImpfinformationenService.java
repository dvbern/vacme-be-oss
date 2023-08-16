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

package ch.dvbern.oss.vacme.service.impfinformationen;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.ImpfungkontrolleTermin;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.repo.ImpfdossierRepo;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.util.DeservesZertifikatValidator;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ImpfinformationenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This service does all the magic (aka business logic) to navigate between the impfungen of a registration!
 */
@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ImpfinformationenService {

	private final ImpfungRepo impfungRepo;
	private final ImpfdossierRepo impfdossierRepo;

	@NonNull
	public ImpfinformationDto getImpfinformationen(
		@NonNull String registrierungsnummer,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		ImpfinformationDto infos = getImpfinformationenOptional(registrierungsnummer, krankheitIdentifier)
			.orElseThrow(() -> AppValidationMessage.UNKNOWN_REGISTRIERUNGSNUMMER_KRANKHEIT.create(
				registrierungsnummer,
				krankheitIdentifier));
		return infos;
	}

	@NonNull
	public Optional<ImpfinformationDto> getImpfinformationenOptional(
		@NonNull String registrierungsnummer,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		return impfungRepo.getImpfinformationenOptional(registrierungsnummer, krankheitIdentifier);
	}

	@NonNull
	public List<ImpfinformationDto> getImpfinformationenForAllDossiers(@NonNull String registrierungsnummer) {
		return Arrays.stream(KrankheitIdentifier.values())
			.map(krankheitIdentifier -> impfungRepo.getImpfinformationenOptional(
				registrierungsnummer,
				krankheitIdentifier))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}

	@Nullable
	public static Impfung readImpfungForImpffolge(@NonNull ImpfinformationDto infos, @NonNull Impffolge impffolge) {
		switch (impffolge) {
		case ERSTE_IMPFUNG:
			return infos.getImpfung1();
		case ZWEITE_IMPFUNG:
			return infos.getImpfung2();
		case BOOSTER_IMPFUNG:
			throw new AppFailureException("Not Yet Implemented for Booster");
		}
		return null;
	}

	@Nullable
	public static Impfung readImpfungForImpffolgeNr(
		@NonNull ImpfinformationDto infos,
		@NonNull Impffolge impffolge,
		@NonNull Integer impffolgeNr) {
		if (impffolge == Impffolge.ERSTE_IMPFUNG) {
			return infos.getImpfung1();
		}
		if (impffolge == Impffolge.ZWEITE_IMPFUNG) {
			return infos.getImpfung2();
		}

		Impfdossiereintrag eintrag = getDossiereintragForNr(infos, impffolgeNr);
		if (eintrag != null) {
			return getImpfungForEintrag(infos, eintrag);
		}
		return null;
	}

	@Nullable
	public static Impfung getImpfungForEintrag(@NonNull ImpfinformationDto infos,
		@NonNull Impfdossiereintrag eintrag) {
		if (infos.getBoosterImpfungen() != null) {
			for (Impfung boosterImpfung : infos.getBoosterImpfungen()) {
				if (eintrag.getImpftermin() != null && eintrag.getImpftermin().equals(boosterImpfung.getTermin())) {
					return boosterImpfung;
				}
			}
		}
		return null;
	}

	@Nullable
	public static Impfdossiereintrag getDossiereintragForNr(
		@NonNull ImpfinformationDto infos,
		@NonNull Integer impffolgeNr
	) {
		for (Impfdossiereintrag eintrag : infos.getImpfdossier().getImpfdossierEintraege()) {
			if (impffolgeNr.equals(eintrag.getImpffolgeNr())) {
				return eintrag;
			}
		}
		return null;
	}

	@NonNull
	public static Integer getImpffolgeNr(@NonNull ImpfinformationDto infos, @NonNull Impfung impfung) {
		if (impfung.equals(infos.getImpfung1())) {
			return getImpffolgeNrOfImpfung1Or2(infos.getExternesZertifikat(), Impffolge.ERSTE_IMPFUNG);
		}
		if (impfung.equals(infos.getImpfung2())) {
			return getImpffolgeNrOfImpfung1Or2(infos.getExternesZertifikat(), Impffolge.ZWEITE_IMPFUNG);
		}
		Impfdossiereintrag eintrag = ImpfinformationenUtil.getDossiereintragForImpfung(infos, impfung);
		if (eintrag != null) {
			return eintrag.getImpffolgeNr();
		}
		return 0;
	}

	@NonNull
	public static Impffolge getImpffolge(@NonNull ImpfinformationDto infos, @NonNull Impfung impfung) {
		if (impfung.equals(infos.getImpfung1())) {
			return Impffolge.ERSTE_IMPFUNG;
		}
		if (impfung.equals(infos.getImpfung2())) {
			return Impffolge.ZWEITE_IMPFUNG;
		}
		Impfdossiereintrag eintrag = ImpfinformationenUtil.getDossiereintragForImpfung(infos, impfung);
		if (eintrag != null) {
			return Impffolge.BOOSTER_IMPFUNG;
		}
		throw AppValidationMessage.ILLEGAL_STATE.create("Impfung nicht gefunden");
	}

	public static int getNumberOfImpfung(
		@NonNull ImpfinformationDto infos
	) {
		int result = 0;

		if (infos.getKrankheitIdentifier().isSupportsImpffolgenEinsUndZwei()) {
			if (infos.getImpfung1() != null) {
				result++;
			}
			if (infos.getImpfung2() != null) {
				result++;
			}
		}

		if (infos.getBoosterImpfungen() != null) {
			result += infos.getBoosterImpfungen().size();
		}

		if (infos.getKrankheitIdentifier().isSupportsExternesZertifikat()) {
			if (infos.getExternesZertifikat()
				!= null) { // Die Impfungen des Externen Zertifikats zaehlen auch, wenn es nicht vollstaendig ist
				// (Realitaet abbilden)
				result += infos.getExternesZertifikat().getAnzahlImpfungen();
			}
		}
		return result;
	}

	// Die neuste Kontrolle, die noch keine Impfung besitzt; also eine Zahl hoeher als die Nummer der neusten Impfung

	public static int getCurrentKontrolleNr(@NonNull ImpfinformationDto infos) {
		return getNumberOfImpfung(infos) + 1;
	}

	@Nullable
	public static ImpfungkontrolleTermin getCurrentKontrolleTerminOrNull(@NonNull ImpfinformationDto infos) {
		Impfdossier dossier = infos.getImpfdossier();

		switch (dossier.getDossierStatus()) {
		// Kontrolle 1
		case NEU:
		case FREIGEGEBEN:
		case GEBUCHT:
		case ODI_GEWAEHLT:
		case IMPFUNG_1_KONTROLLIERT:
			return dossier.getImpfungkontrolleTermin1();

		// Grund-Kontrolle 2
		case IMPFUNG_1_DURCHGEFUEHRT:
		case IMPFUNG_2_KONTROLLIERT:
			return dossier.getImpfungkontrolleTermin2();

		// Boosterkontrolle N
		default:
			var eintragOpt = getImpfdossierEintragForKontrolle(infos);
			return eintragOpt.map(Impfdossiereintrag::getImpfungkontrolleTermin).orElse(null);
		}
	}

	@NonNull
	public static Optional<Impfdossiereintrag> getImpfdossierEintragForKontrolle(@NonNull ImpfinformationDto infos) {
		final int impffolgeNr = getCurrentKontrolleNr(infos);
		return infos.getImpfdossier().findEintragForImpffolgeNr(impffolgeNr);
	}

	@NonNull
	public static Optional<Impfdossiereintrag> getImpfdossierEintragWithID(
		@NonNull ImpfinformationDto infos,
		@NonNull UUID eintragId
	) {
		return infos.getImpfdossier().getImpfdossierEintraege().stream()
			.filter(impfdossiereintrag ->
				impfdossiereintrag.getId().equals(eintragId))
			.findAny();
	}

	@Nullable
	public static Impftermin getImpftermin(
		@NonNull ImpfinformationDto infos,
		@NonNull Impffolge impffolge,
		@Nullable Integer impffolgeNr) {
		// Der Termin kann leer sein (wenn die Person einfach am ODI erscheint ohne Termin)
		switch (impffolge) {
		case ERSTE_IMPFUNG:
			return infos.getImpfdossier().getBuchung().getImpftermin1();
		case ZWEITE_IMPFUNG:
			return infos.getImpfdossier().getBuchung().getImpftermin2();
		case BOOSTER_IMPFUNG:
			if (impffolgeNr == null) {
				return null;
			}
			return infos.getImpfdossier()
				.findEintragForImpffolgeNr(impffolgeNr)
				.map(Impfdossiereintrag::getImpftermin)
				.orElse(null);
		default:
			throw new IllegalStateException("Unexpected value: " + impffolge);
		}
	}

	public static boolean hasFreigegebenenImpfschutz(@NonNull Impfdossier impfdossier) {
		Impfschutz impfschutz = impfdossier.getImpfschutz();
		if (impfschutz == null || impfschutz.getFreigegebenNaechsteImpfungAb() == null) {
			return false;
		}
		return impfschutz.getFreigegebenNaechsteImpfungAb().isBefore(LocalDateTime.now());
	}

	public static int getImpffolgeNrOfImpfung1Or2(
		@Nullable ExternesZertifikat externesZertifikat,
		@NonNull Impffolge impffolge) {
		int result = 0;

		if (externesZertifikat
			!= null) { // Die Impfungen des Externen Zertifikats zaehlen auch, wenn es nicht vollstaendig ist
			// (Realitaet abbilden)
			result += externesZertifikat.getAnzahlImpfungen();
		}

		switch (impffolge) {
		case ERSTE_IMPFUNG:
			result += 1;
			break;
		case ZWEITE_IMPFUNG:
			result += 2;
			break;
		default:
			throw AppValidationMessage.IMPFTERMIN_FALSCHE_IMPFFOLGE.create(impffolge);
		}

		return result;
	}

	@NonNull
	public static List<Impfung> sortImpfungenByTimestampImpfung(@NonNull List<Impfung> impfungen) {
		return impfungen.stream()
			.filter(Objects::nonNull)
			.sorted(Comparator.comparing(Impfung::getTimestampImpfung))
			.collect(Collectors.toList());
	}

	@Nullable
	public static Impfung getNewestVacmeImpfung(@NonNull ImpfinformationDto infos) {
		return getNewestVacmeImpfung(infos.getImpfung1(), infos.getImpfung2(), infos.getBoosterImpfungen());
	}

	public static ID<Impfung> getNewestVacmeImpfungId(@NonNull ImpfinformationDto infos) {
		final Impfung newestImpfung = ImpfinformationenService.getNewestVacmeImpfung(infos);
		Objects.requireNonNull(newestImpfung);
		final ID<Impfung> idOfNewestImpfung = Impfung.toId(newestImpfung.getId());
		return idOfNewestImpfung;
	}

	@Nullable
	public static Impfung getNewestVacmeImpfung(
		@Nullable Impfung impfung1,
		@Nullable Impfung impfung2,
		@Nullable List<Impfung> boosterimpfungen) {
		if (CollectionUtils.isNotEmpty(boosterimpfungen)) {
			return getNewestBoosterImpfung(boosterimpfungen);
		}
		if (impfung2 != null) {
			return impfung2;
		}
		return impfung1;
	}

	@NonNull
	public static Impfung getNewestBoosterImpfung(@NonNull List<Impfung> boosterimpfungen) {
		// make sure list is sorted by date
		List<Impfung> sortedImpfungen = sortImpfungenByTimestampImpfung(boosterimpfungen);
		Impfung impfungN = boosterimpfungen.get(sortedImpfungen.size() - 1); // neuste Impfung aus Liste
		return impfungN;
	}

	public static void validateImpffolgeExists(
		@NonNull Impffolge impffolge,
		@Nullable Integer impffolgeNr,
		@NonNull ImpfinformationDto infos
	) {
		switch (impffolge) {
		case ERSTE_IMPFUNG:
			if (infos.getImpfung1() == null) {
				throw AppValidationMessage.IMPFFOLGE_NOT_EXISTING.create(impffolge);
			}
			return;
		case ZWEITE_IMPFUNG:
			if (infos.getImpfung2() == null) {
				throw AppValidationMessage.IMPFFOLGE_NOT_EXISTING.create(impffolge);
			}
			return;
		case BOOSTER_IMPFUNG:
			Validate.notNull(impffolgeNr);
			Impfung impfung = readImpfungForImpffolgeNr(infos, impffolge, impffolgeNr);
			if (impfung == null) {
				throw AppValidationMessage.IMPFFOLGE_NOT_EXISTING.create(impffolge);
			}
			return;
		}
		throw new AppFailureException("Impffolge could not be inferred");
	}

	@Nullable
	public static ImpfungkontrolleTermin getNewestImpfkontrolleTermin(@NonNull ImpfinformationDto infos) {
		ImpfungkontrolleTermin kontrolleTermin1 = infos.getImpfdossier().getImpfungkontrolleTermin1();
		ImpfungkontrolleTermin kontrolleTermin2 = infos.getImpfdossier().getImpfungkontrolleTermin2();
		Optional<Impfdossiereintrag> newestImpfdossierEintrag =
			infos.getImpfdossier().getImpfdossierEintraege().stream().max(Comparator.naturalOrder());
		if (newestImpfdossierEintrag.isPresent()
			&& newestImpfdossierEintrag.get().getImpfungkontrolleTermin() != null) {
			return newestImpfdossierEintrag.get().getImpfungkontrolleTermin();
		}
		if (kontrolleTermin2 != null) {
			return kontrolleTermin2;
		}
		return kontrolleTermin1;
	}

	@NonNull
	public Impfdossiereintrag getExistingLatestImpfdossierEintrag(
		@NonNull ImpfinformationDto impfinformationen,
		@NonNull Integer impffolgeNr
	) {
		if (!impffolgeNr.equals(getNumberOfImpfung(impfinformationen) + 1)) {
			throw new AppFailureException("Die ubergebene ImpffolgeNr "
				+ impffolgeNr
				+ " entspicht nicht der detektierten Anzahl Impfungen");
		}
		Impfdossier impfdossier = impfdossierRepo.getOrCreateImpfdossier(
			impfinformationen.getRegistrierung(),
			impfinformationen.getKrankheitIdentifier());

		return impfdossier.findEintragForImpffolgeNr(impffolgeNr).orElseThrow();
	}

	@NonNull
	public Impfdossiereintrag getOrCreateLatestImpfdossierEintrag(
		@NonNull ImpfinformationDto impfinformationen,
		@NonNull Integer impffolgeNr
	) {
		if (!impffolgeNr.equals(getNumberOfImpfung(impfinformationen) + 1)) {
			throw new AppFailureException("Die ubergebene ImpffolgeNr "
				+ impffolgeNr
				+ " entspicht nicht der detektierten Anzahl Impfungen");
		}
		Impfdossier impfdossier = impfdossierRepo.getOrCreateImpfdossier(
			impfinformationen.getRegistrierung(),
			impfinformationen.getKrankheitIdentifier());

		return impfdossier.findEintragForImpffolgeNr(impffolgeNr)
			.orElseGet(() -> {
				Impfdossiereintrag newEintrag = impfdossierRepo.addEintrag(impffolgeNr, impfdossier);
				return newEintrag;
			});
	}

	public boolean hasVacmeImpfungen(@NonNull @NotNull ImpfinformationDto infos) {
		return infos.getImpfung1() != null || infos.getImpfung2() != null ||
			(infos.getBoosterImpfungen() != null && !infos.getBoosterImpfungen().isEmpty());
	}

	public boolean hasAnyVacmeImpfungen(@NonNull Registrierung registrierung) {
		return !getAllVacmeImpfungen(registrierung).isEmpty();
	}

	@NonNull
	public List<Impfung> getAllVacmeImpfungen(@NonNull Registrierung registrierung) {
		return impfungRepo.getAllImpfungen(registrierung.getRegistrierungsnummer());
	}

	public static boolean deservesZertifikatForAnyImpfung(@NonNull @NotNull ImpfinformationDto infos) {
		return DeservesZertifikatValidator.deservesZertifikatForAnyImpfung(infos);
	}

	@NonNull
	public Impfung getImpfungById(@NonNull ID<Impfung> impfungId) {
		return impfungRepo.getById(impfungId)
			.orElseThrow(() -> AppFailureException.entityNotFound(Impfung.class, impfungId));
	}

	@NonNull
	public static Optional<Impfdossiereintrag> getPendingDossiereintrag(@Nullable ImpfinformationDto infos) {
		if (infos == null) {
			return Optional.empty();
		}
		final int currentKontrolleNr = ImpfinformationenService.getCurrentKontrolleNr(infos);
		return Optional.ofNullable(getDossiereintragForNr(infos, currentKontrolleNr));
	}

	@NonNull
	public static Optional<Impftermin> getPendingBoosterTermin(@Nullable ImpfinformationDto infos) {
		final Optional<Impfdossiereintrag> pendingDossiereintrag = getPendingDossiereintrag(infos);
		return pendingDossiereintrag.map(Impfdossiereintrag::getImpftermin);
	}

	@NonNull
	public static Optional<Impfung> getImpfungForKontrolle(
		@NonNull ImpfinformationDto impfinformationen,
		@NonNull Impffolge impffolge,
		@Nullable UUID dossiereintragID
	) {
		switch (impffolge) {
		case ERSTE_IMPFUNG:
			return Optional.ofNullable(impfinformationen.getImpfung1());

		case ZWEITE_IMPFUNG:
			return Optional.ofNullable(impfinformationen.getImpfung2());

		case BOOSTER_IMPFUNG:
			if (dossiereintragID != null) {
				Optional<Impfdossiereintrag> existingDossiereintragOptional =
					ImpfinformationenService.getImpfdossierEintragWithID(impfinformationen, dossiereintragID);
				if (existingDossiereintragOptional.isPresent()) {
					return Optional.ofNullable(ImpfinformationenService.getImpfungForEintrag(
						impfinformationen,
						existingDossiereintragOptional.get()));
				}
			}
			return Optional.empty();

		default:
			throw new IllegalStateException("Unexpected value: " + impffolge);
		}
	}

	public static boolean willBeGrundimmunisiertAfterErstimpfung(
		@NonNull @NotNull Impfung erstImpfung,
		@NonNull @NotNull ImpfinformationDto infos) {
		return ImpfinformationenUtil.willBeGrundimmunisiertAfterErstimpfungImpfstoff(
			erstImpfung.getImpfstoff(),
			infos.getExternesZertifikat());
	}
}
