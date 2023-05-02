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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.base.ImpfInfo;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.migration.Migration;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.jax.korrektur.EmailTelephoneKorrekturJax;
import ch.dvbern.oss.vacme.jax.korrektur.ImpfungDatumKorrekturJax;
import ch.dvbern.oss.vacme.jax.korrektur.ImpfungKorrekturJax;
import ch.dvbern.oss.vacme.jax.korrektur.ImpfungOdiKorrekturJax;
import ch.dvbern.oss.vacme.jax.korrektur.ImpfungSelbstzahlendeKorrekturJax;
import ch.dvbern.oss.vacme.jax.korrektur.ImpfungVerabreichungKorrekturJax;
import ch.dvbern.oss.vacme.jax.korrektur.PersonendatenKorrekturJax;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLUploadBaseJax;
import ch.dvbern.oss.vacme.repo.BenutzerRepo;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.repo.MigrationRepo;
import ch.dvbern.oss.vacme.service.booster.BoosterService;
import ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioUtil;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.service.vmdl.VMDLServiceAbstract;
import ch.dvbern.oss.vacme.service.vmdl.VMDLServiceFactory;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ImpfinformationenUtil;
import ch.dvbern.oss.vacme.util.ValidationUtil;
import ch.dvbern.oss.vacme.wrapper.VacmeDecoratorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class KorrekturService {

	private final ImpfungRepo impfungRepo;
	private final ImpfterminRepo impfterminRepo;
	private final UserPrincipal userPrincipal;
	private final TerminbuchungService terminbuchungService;
	private final OrtDerImpfungService ortDerImpfungService;
	private final PdfArchivierungService pdfArchivierungService;
	private final ImpfstoffService impfstoffService;
	private final ImpfdokumentationService impfdokumentationService;
	private final VMDLServiceFactory vmdlServiceFactory;
	private final MigrationRepo migrationRepo;
	private final ZertifikatService zertifikatService;
	private final KeyCloakRegService keyCloakService;
	private final BenutzerRepo benutzerRepo;
	private final ApplicationPropertyService propertyService;
	private final BoosterService boosterService;
	private final FragebogenService fragebogenService;
	private final ImpfdossierService impfdossierService;
	private final ImpfinformationenService impfinformationenService;


	public void impfungKorrigieren(
		@NonNull ImpfungKorrekturJax korrekturJax,
		@NonNull ImpfinformationDto impfinformation,
		@NonNull Impfung impfung
	) {
		Registrierung registrierung = impfinformation.getRegistrierung();

		final Impfstoff impfstoffKorrigiert = impfstoffService.findById(Impfstoff.toId(korrekturJax.getImpfstoff()));
		final Impfstoff impfstoffFalschErfasst = impfung.getImpfstoff();

		// Der Impfstoff muss zugelassen sein, oder wenn es ein Nachtrag ist reicht auch extern_zugelassen
		ValidationUtil.validateImpfstoffZulassung(impfstoffKorrigiert, impfung.isExtern());

		final boolean needsNewZertifikat = ImpfungKorrekturJax.needsNewZertifikat(impfinformation.getImpfdossier(), korrekturJax.getImpffolge(),
			impfstoffFalschErfasst, impfstoffKorrigiert, impfinformation.getExternesZertifikat());
		final boolean needToRevoke = ImpfungKorrekturJax.needToRevoke(impfinformation.getImpfdossier(), korrekturJax.getImpffolge(), impfstoffFalschErfasst, impfstoffKorrigiert);

		impfung.setImpfstoff(impfstoffKorrigiert);
		impfung.setLot(korrekturJax.getLot());
		impfung.setMenge(korrekturJax.getMenge());

		// Andere Anzahl Dosen noetig als bisher:
		if (impfstoffKorrigiert.getAnzahlDosenBenoetigt() != impfstoffFalschErfasst.getAnzahlDosenBenoetigt()) {

			// Validierung: wenn wir Impfung 2 korrigieren, darf die Erstimpfung nicht nur 1 Dosis benoetigen.
			ValidationUtil.validateAnzahlImpfungen(korrekturJax.getImpffolge(), impfinformation);

			LOG.warn("VACME-KORREKTUR: Es wurde ein Impfstoff mit {} Dosen korrigiert auf einen Impfstoff mit {} Dosen. Registrierung {}",
				impfstoffFalschErfasst.getAnzahlDosenBenoetigt(),
				impfstoffKorrigiert.getAnzahlDosenBenoetigt(),
				registrierung.getRegistrierungsnummer());

			// Erstimpfung
			if (korrekturJax.getImpffolge() == Impffolge.ERSTE_IMPFUNG) {

				// vorher 2 Dosen noetig, nachher 1: Darf noch keine Zweitimpfung haben
				if (ImpfinformationenUtil.willBeGrundimmunisiertAfterErstimpfungImpfstoff(impfstoffKorrigiert, impfinformation.getExternesZertifikat())) {
					// Sicherstellen, dass keine zweite Impfung vorhanden ist
					if (impfinformation.getImpfung2() != null) {
						throw AppValidationMessage.IMPFUNG_ZUVIELE_DOSEN_KORREKTUR.create(impfstoffKorrigiert.getAnzahlDosenBenoetigt(), impfstoffFalschErfasst.getAnzahlDosenBenoetigt());
					}

					// Die Impfung ist mit dieser Impfung abgeschlossen, der zweite Termin wird nicht mehr benoetigt
					impfterminRepo.termin2Freigeben(impfinformation.getImpfdossier());
				}
				// vorher 1 Dosis noetig, nachher 2: Darf noch keinen Booster haben
				if (ImpfinformationenUtil.willBeGrundimmunisiertAfterErstimpfungImpfstoff(impfstoffFalschErfasst, impfinformation.getExternesZertifikat())) {
					// Der alte Impfstoff hat nur eine Dosis benoetigt. Falls unterdessen bereits eine Booster-Impfung besteht, waere diese
					// nach der Aenderung eigentlich eine Grundimpfung -> nicht zulassen
					if (CollectionUtils.isNotEmpty(impfinformation.getBoosterImpfungen())) {
						throw AppValidationMessage.IMPFUNG_ZUVIELE_DOSEN_KORREKTUR.create(impfstoffFalschErfasst.getAnzahlDosenBenoetigt(), impfstoffKorrigiert.getAnzahlDosenBenoetigt());
					}
				}
			}

			// Impfung 1 oder 2:
			if (korrekturJax.getImpffolge() != Impffolge.BOOSTER_IMPFUNG) {

				// wenn eine Reg abgeschlossen ist weil man corona hatte muss der Status nicht neu berechnet  werden
				// weil in diesem  Fall unabhaengig vom Impfstoff immer nur 1 Impfung noetig ist. Ansonsten pruefen in
				// welchem Status  die Registrierung neu sein muss
				if (!impfinformation.getImpfdossier().abgeschlossenMitCorona()) {
					final Fragebogen fragebogen =
						fragebogenService.findFragebogenByRegistrierungsnummer(registrierung.getRegistrierungsnummer());
					impfdokumentationService.setNextStatus(impfinformation, korrekturJax.getImpffolge(), impfung, false, fragebogen.getImmunsupprimiert());
				}
			}
		}

		resendToSchnittstellenAndRegenerateArchive(registrierung, impfung);

		// Auch das Zertifikat muss neu erstellt werden, falls der Impfstoff geaendert hat
		if (needsNewZertifikat) {
			registrierung.setGenerateZertifikatTrueIfAllowed(impfinformation, impfung);
		}
		// Eventuell bereits vorhandene Zertifikate muessen storniert werden
		if (needToRevoke) {
			LOG.info("VACME-ZERTIFIKAT-REVOCATION: Zertifikat der Impfung {} wird revoked fuer Registrierung {}, da der Impfstoff korrigiert wurde",
				impfung.getId(),
				registrierung.getRegistrierungsnummer());
			zertifikatService.queueForRevocation(impfung);
		}

		impfungRepo.update(impfung);
		LOG.info("VACME-KORREKTUR: Fuer die Registrierung {} wurde eine Korrektur des Impfstoffs vorgenommen {} durch {}",
			registrierung.getRegistrierungsnummer(), korrekturJax, this.userPrincipal.getBenutzerOrThrowException().getBenutzername());
		// sicherstellen dass Infos fuer recalculation neu geladen werden
		boosterService.recalculateImpfschutzAndStatusmovesForSingleRegWithReload(registrierung, impfinformation.getKrankheitIdentifier());
	}

	public void impfungOdiKorrigieren(
		@NotNull ImpfungOdiKorrekturJax korrekturJax,
		@NonNull ImpfinformationDto impfinformationDto,
		@NonNull Impfung impfung
	) {
		Impftermin impftermin = impfung.getTermin();
		Registrierung registrierung = impfinformationDto.getRegistrierung();
		OrtDerImpfung odiKorrigiert = ortDerImpfungService.getById(OrtDerImpfung.toId(korrekturJax.getOdi()));
		OrtDerImpfung odiFalschErfasst = impftermin.getImpfslot().getOrtDerImpfung();

		if (odiKorrigiert.getId().equals(odiFalschErfasst.getId())) {
			// Es hat nichts geaendert
			return;
		}

		// Wenn dies die zweite Impfung ist, darf der ODI unterschiedlich sein zur ersten Impfung
		// Falls es aber die erste ist, muss ein allfaelliger zweiter (offener!) Termin beim selben ODI sein
		Impfdossier impfdossier = impfinformationDto.getImpfdossier();
		if (impftermin.getImpffolge() == Impffolge.ERSTE_IMPFUNG
				&& !ImpfdossierStatus.getMindestensGrundimmunisiertOrAbgeschlossen().contains(impfdossier.getDossierStatus())
				&& impfdossier.getBuchung().getImpftermin2() != null) {
			impfterminRepo.termin2Freigeben(impfdossier);
			LOG.warn("VACME-KORREKTUR: Fuer die Registrierung {} wurde der zweite Termin geloescht", registrierung.getRegistrierungsnummer());
		}

		final Impftermin onDemandImpftermin = terminbuchungService.createOnDemandImpftermin(
			impfinformationDto.getKrankheitIdentifier(),
			impftermin.getImpffolge(),
			odiKorrigiert,
			impfung.getTimestampImpfung());
		// Wir rufen hier die Methode direkt auf, um die Validierung zu umgehen! (im Repo wird validiert dass keine Impfung am Termin haengt)
		impftermin.setGebuchtFromImpfterminRepo(false);
		switch (impftermin.getImpffolge()) {
		case ERSTE_IMPFUNG:
			impfdossier.getBuchung().setImpftermin1FromImpfterminRepo(null);
			impfterminRepo.termin1Speichern(impfinformationDto.getImpfdossier(), onDemandImpftermin);
			break;
		case ZWEITE_IMPFUNG:
			impfdossier.getBuchung().setImpftermin2FromImpfterminRepo(null);
			impfterminRepo.termin2Speichern(impfinformationDto.getImpfdossier(), onDemandImpftermin);
			break;
		case BOOSTER_IMPFUNG:
			if (impfinformationDto.getImpfdossier() == null || korrekturJax.getImpffolgeNr() == null) {
				throw new AppFailureException("Unable to correct booster without impfdossier or impffolgeNr");
			}
			Impfdossiereintrag eintrag = impfinformationDto.getImpfdossier().getEintragForImpffolgeNr(korrekturJax.getImpffolgeNr());
			eintrag.setImpfterminFromImpfterminRepo(null);
			impfterminRepo.boosterTerminSpeichern(eintrag, onDemandImpftermin);
			break;
		}

		// Die Impfung muss an den neuen Termin gehaengt werden
		impfung.setTermin(onDemandImpftermin);

		if (impfdossier.getBuchung().isNichtVerwalteterOdiSelected() && odiKorrigiert.isOeffentlich()) {
			// Die Person hatte vorher Nicht-verwalteter-Hausarzt/Apotheke gewaehlt, jetzt aber ein oeffentliches ODI
			// aufgesucht. Wir setzen das Flag auf FALSE, da nach aktueller Vorgabe beide Termine beim selben ODI
			// gebucht werden sollen
			impfdossier.getBuchung().setNichtVerwalteterOdiSelected(false);
		}

		// Schnittstellen neu befuellen
		resendToSchnittstellenAndRegenerateArchive(registrierung, impfung);

		impfungRepo.update(impfung);
		LOG.info("VACME-KORREKTUR: Fuer die Registrierung {} wurde eine Korrektur des ODIs vorgenommen {} durch {}",
			registrierung.getRegistrierungsnummer(),
			korrekturJax,
			this.userPrincipal.getBenutzerOrThrowException().getBenutzername());
	}

	public void impfungSelbstzahlendeKorrigieren(
		@NonNull ImpfungSelbstzahlendeKorrekturJax korrekturJax,
		@NonNull Impfung impfung,
		@NonNull ImpfinformationDto impfinformation
	) {
		impfung.setSelbstzahlende(korrekturJax.isSelbstzahlende());
		Registrierung registrierung = impfinformation.getRegistrierung();

		// Die korrigierten Daten werden weder zu VMDL noch zu MyCovidVac geschickt, es muss also nichts zurueckgesetzt werden
		// Die Archivierung muss jedoch neu angestossen werden, vorsichtshalber machen wir alles aufs Mal
		resendToSchnittstellenAndRegenerateArchive(registrierung, impfung);

		impfungRepo.update(impfung);

		LOG.info("VACME-KORREKTUR: Fuer die Registrierung {} wurde eine Korrektur des Zahlungsmodus vorgenommen {} durch {}",
			registrierung.getRegistrierungsnummer(),
			korrekturJax,
			this.userPrincipal.getBenutzerOrThrowException().getBenutzername());
	}

	public void impfungVerabreichungKorrigieren(
		@NonNull ImpfungVerabreichungKorrekturJax korrekturJax,
		@NonNull Impfung impfung,
		@NonNull ImpfinformationDto impfinformation
	) {
		Registrierung registrierung = impfinformation.getRegistrierung();
		impfung.setVerarbreichungsart(korrekturJax.getVerabreichungsart());
		impfung.setVerarbreichungsort(korrekturJax.getVerabreichungsort());
		impfung.setVerarbreichungsseite(korrekturJax.getVerabreichungsseite());

		// Die korrigierten Daten werden weder zu VMDL noch zu MyCovidVac geschickt, es muss also nichts zurueckgesetzt werden
		// Sie erscheinen auch nicht auf der Impfdoku, darum muss diese nicht neu generiert werden
		// Die Archivierung muss jedoch neu angestossen werden, vorsichtshalber machen wir alles aufs Mal
		resendToSchnittstellenAndRegenerateArchive(registrierung, impfung);
		impfungRepo.update(impfung);

		LOG.info("VACME-KORREKTUR: Fuer die Registrierung {} wurde eine Korrektur der Verabreichung vorgenommen {} durch {}",
			registrierung.getRegistrierungsnummer(),
			korrekturJax,
			this.userPrincipal.getBenutzerOrThrowException().getBenutzername());
	}

	@NonNull
	public Impfung getImpfungByTermin(@NonNull Impftermin impftermin) {
		return impfungRepo.getByImpftermin(impftermin)
			.orElseThrow(() -> {
				LOG.error("VACME-KORREKTUR: Fehler; Es wurde keine Impfung fuer den Termin gefunden");
				return AppFailureException.entityNotFound(Impfung.class, impftermin);
			});
	}

	public void impfungLoeschen(
		@NonNull final ImpfinformationDto impfinformationen,
		@NonNull Impffolge impffolge,
		@Nullable Integer impffolgeNr
	) {
		Registrierung registrierung = impfinformationen.getRegistrierung();
		Impfdossier dossier = impfinformationen.getImpfdossier();
		if (impffolge == Impffolge.ERSTE_IMPFUNG) {
			impfung1Loeschen(impfinformationen);
			// Egal, ob es die erste oder die zweite Impfung war: Die Info ueber eine eventuellen verzichtete
			// zweite Impfung muss geloescht werden
			// Der Impfschutz ist auf jeden Fall nicht mehr komplett und es darf kein Zertifikat geben
			dossier.setStatusToNichtAbgeschlossenStatus(
				impfinformationen,
				dossier.getDossierStatus(),
				impfinformationen.getImpfung1());

		} else if (impffolge == Impffolge.ZWEITE_IMPFUNG) {
			impfung2Loeschen(impfinformationen);
			// Egal, ob es die erste oder die zweite Impfung war: Die Info ueber eine eventuellen verzichtete
			// zweite Impfung muss geloescht werden
			// Der Impfschutz ist auf jeden Fall nicht mehr komplett und es darf kein Zertifikat geben
			dossier.setStatusToNichtAbgeschlossenStatus(
				impfinformationen,
				dossier.getDossierStatus(),
				impfinformationen.getImpfung1());
		} else if (impffolge == Impffolge.BOOSTER_IMPFUNG) {
			Objects.requireNonNull(impffolgeNr);
			int currentMaxImpfungNr = ImpfinformationenService.getNumberOfImpfung(impfinformationen);
			if (currentMaxImpfungNr != impffolgeNr) {
				throw AppValidationMessage.IMPFUNG_LOESCHEN_N.create( impffolgeNr, currentMaxImpfungNr, registrierung.getRegistrierungsnummer());
			}
			Impfdossiereintrag impfdossiereintrag = dossier.getEintragForImpffolgeNr(impffolgeNr);
			impfungNLoeschen(impfinformationen, impfdossiereintrag);
		}
		// sicherstellen dass Infos fuer recalculation neu geladen werden
		boosterService.recalculateImpfschutzAndStatusmovesForSingleRegWithReload(registrierung, impfinformationen.getKrankheitIdentifier());
	}

	private void impfung1Loeschen(@NonNull ImpfinformationDto infos) {
		if (infos.getImpfdossier().getBuchung().getImpftermin1() != null) {
			if (infos.getImpfung2() != null || CollectionUtils.isNotEmpty(infos.getBoosterImpfungen())) {
				throw AppValidationMessage.IMPFUNG_LOESCHEN_ERSTE.create(infos.getRegistrierung().getRegistrierungsnummer());
			}
			final Impftermin termin = infos.getImpfdossier().getBuchung().getImpftermin1();
			Impfung impfung = this.getImpfungByTermin(termin);

			// Eventuell bereits vorhandene Zertifikate muessen storniert werden
			unlinkZertifikatFromImpfungAndRevocateZertifikat(infos.getRegistrierung(), impfung);
			// Erst ab 14.0.0 koennen wir sicher sein, dass die Impfung verknuepft ist mit dem Zertifikat
			// Aber die Impfung 1 kann nur als letzte geloescht werden, d.h. wir haben nach dem Loeschen sicher kein Zertifikt mehr
			// und koennen daher alles revozieren
			zertifikatService.queueForRevocation(infos.getRegistrierung());

			impfungLoeschen(infos, impfung);

			// Diesen und zukuenftige Termine freigeben
			impfterminRepo.termin1Freigeben(infos.getImpfdossier());
			impfterminRepo.termin2Freigeben(infos.getImpfdossier());
			final Optional<Impfdossiereintrag> pendingDossiereintrag = ImpfinformationenService.getPendingDossiereintrag(infos);
			if (pendingDossiereintrag.isPresent()) {
				impfterminRepo.boosterTerminFreigeben(pendingDossiereintrag.get());
			}
			// neuer Status kann erst nach freigabe des Termins korrekt berechnet werden
			Impfdossier dossier = infos.getImpfdossier();
			dossier.setDossierStatus(impfdossierService.ermittleLetztenDossierStatusVorKontrolle1(dossier));
			// Falls der letzte Status ODI_GEWAEHLT war, dann lassen wir den gewuenschten ODI drin.
			// Ansonsten loeschen
			if (dossier.getDossierStatus() != ImpfdossierStatus.ODI_GEWAEHLT) {
				dossier.getBuchung().setGewuenschterOdi(null);
			}

		} else{
			throw AppValidationMessage.ILLEGAL_STATE.create("Kein Termin1 gefunden");
		}
	}

	private void impfung2Loeschen(@NonNull ImpfinformationDto infos) {
		if (infos.getImpfdossier().getBuchung().getImpftermin2() != null) {
			// Die zweite Impfung darf nicht geloescht werden, wenn noch Booster Impfungen existieren
			if (CollectionUtils.isNotEmpty(infos.getBoosterImpfungen())) {
				throw AppValidationMessage.IMPFUNG_LOESCHEN_ZWEITE.create(infos.getRegistrierung().getRegistrierungsnummer());
			}
			final Impftermin termin = infos.getImpfdossier().getBuchung().getImpftermin2();
			Impfung impfung = this.getImpfungByTermin(termin);

			// Eventuell bereits vorhandene Zertifikate muessen storniert werden
			unlinkZertifikatFromImpfungAndRevocateZertifikat(infos.getRegistrierung(), impfung);
			// Erst ab 14.0.0 koennen wir sicher sein, dass die Impfung verknuepft ist mit dem Zertifikat
			// Aber die Impfung 2 kann nur als letzte geloescht werden, d.h. wir haben nach dem Loeschen sicher kein Zertifikt mehr
			// und koennen daher alles revozieren
			zertifikatService.queueForRevocation(infos.getRegistrierung());
			impfungLoeschen(infos, impfung); //dies loescht auch die Impfdoku

			// Diesen und zukuenftige Termine freigeben
			impfterminRepo.termin2Freigeben(infos.getImpfdossier());
			final Optional<Impfdossiereintrag> pendingDossiereintrag = ImpfinformationenService.getPendingDossiereintrag(infos);
			pendingDossiereintrag.ifPresent(impfterminRepo::boosterTerminFreigeben);

			infos.getImpfdossier().setDossierStatus(ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT);

		} else {
			throw AppValidationMessage.ILLEGAL_STATE.create("Kein Termin2 gefunden");
		}
	}

	private void impfungNLoeschen(@NonNull ImpfinformationDto impfinformationDto, @NonNull Impfdossiereintrag impfdossiereintrag) {
		Registrierung registrierung = impfinformationDto.getRegistrierung();
		final Impftermin impftermin = impfdossiereintrag.getImpftermin();
		if (impftermin != null) {
			Impfung impfung = this.getImpfungByTermin(impftermin);

			// Eventuell bereits vorhandene Zertifikate muessen storniert werden
			// Bei Booster Impfungen koennen wir NICHT zwingend davon ausgehen, dass jetzt kein gueltiges Zertifikat mehr bestehen darf.
			unlinkZertifikatFromImpfungAndRevocateZertifikat(registrierung, impfung);

			impfungBoosterLoeschen(impfung, impfinformationDto); //dies loescht auch die Impfdoku

			// Diesen und zukuenftige Termine freigeben
			impfterminRepo.boosterTerminFreigeben(impfdossiereintrag);

			// Selbstzahlende flag auf registrierung resetten
			impfinformationDto.getImpfdossier().getBuchung().setSelbstzahler(false);

			// neuer Status kann erst nach freigabe des Termins korrekt berechnet werden
			ImpfdossierStatus impfdossierStatus =
				impfdossierService.ermittleLetztenDossierStatusVorKontrolleBooster(impfinformationDto.getImpfdossier(), impfdossiereintrag);
			impfinformationDto.getImpfdossier().setDossierStatus(impfdossierStatus);

			// Falls der letzte Status ODI_GEWAEHLT_BOOSTER war, dann lassen wir den gewuenschten ODI drin.
			// Ansonsten loeschen
			if (impfdossierStatus != ImpfdossierStatus.ODI_GEWAEHLT_BOOSTER) {
				impfinformationDto.getImpfdossier().getBuchung().setGewuenschterOdi(null);
			}

			// Die gerade zu loeschende Impfung ist noch in der Liste, wir nehmen die nach dem loeschen manuell raus!
			Objects.requireNonNull(impfinformationDto.getBoosterImpfungen());
			impfinformationDto.getBoosterImpfungen().remove(impfung);
		} else {
			throw AppValidationMessage.ILLEGAL_STATE.create("Kein Termin fuer Boosterimpfung gefunden");
		}
	}

	private void unlinkZertifikatFromImpfungAndRevocateZertifikat(@NonNull Registrierung registrierung, @NonNull Impfung impfung) {
		final List<Zertifikat> zertifikatList = zertifikatService.findZertifikatForImpfung(impfung);
		zertifikatList.forEach(zertifikat -> {
				LOG.info("VACME-ZERTIFIKAT-REVOCATION: Zertifikat {} wird revoked fuer Registrierung {}, da eine "
						+ "Impfung geloescht wurde",
					zertifikat.getUvci(),
					registrierung.getRegistrierungsnummer());
				Zertifikat unlinkedZert = zertifikatService.unlinkFromImpfung(zertifikat);
				// Im Fall der Loeschung einer Impfung koennen wir das Zertifikat direkt revozieren.
				// Dies, damit das Zertifikat nicht noch zum Download angeboten wird, bis die Revozierung ueber den Batchjob durch ist
				// Gerade bei Postversand wird ja 3 Tage gewartet, damit das neue Zertifikat sicher schon da ist, bevor das alte revoziert
				// wird, aber beim Loeschen der Impfung gibt es gar kein neues Zertifikat und somit kein Grund zu warten.
				zertifikatService.queueForRevocationPrioritaerUndOhneWaittime(unlinkedZert);
			}
		);
	}

	private void impfungLoeschen(@NonNull ImpfinformationDto infos, @NonNull Impfung impfung) {
		pdfArchivierungService.deleteImpfungArchive(infos.getRegistrierung());

		// delete Migration Metadata if it exists
		migrationRepo.getByImpfung(impfung).map(Migration::getId).map(Migration::toId).ifPresent(id -> migrationRepo.delete(id));
		// delete the Impfung
		impfungRepo.delete(Impfung.toId(impfung.getId()));

		VMDLServiceAbstract<? extends VMDLUploadBaseJax> vmdlService = getVmdlService(impfung);
		if (vmdlService.wasSentToVMDL(impfung)) {
			// Send delete request to BAG VMDL if the Impfung was sent (Impfung.timestampVMDL != null) or was at any moment in the past sent to VMDL (Impfung_AUD.timestampVMDL != null)
			// This guarantees that in case the Impfung was already sent once to VMDL and then corrected in VacMe (which causes the timestampVMDL to become null again)
			// that this Impfung is deleted from VMDL even if the current timestampVMDL is null
			vmdlService.deleteImpfung(impfung);
		}

		LOG.info("VACME-KORREKTUR: Fuer die Registrierung {} wurde die Impfung {} geloescht durch {}",
			infos.getRegistrierung().getRegistrierungsnummer(),
			impfung.getTermin().getImpffolge().name(),
			this.userPrincipal.getBenutzerOrThrowException().getBenutzername());
	}

	@NonNull
	private VMDLServiceAbstract<? extends VMDLUploadBaseJax> getVmdlService(@NonNull Impfung impfung) {
		VMDLServiceAbstract<? extends VMDLUploadBaseJax> vmdlService = vmdlServiceFactory.createVMDLService(impfung.getTermin().getImpfslot().getKrankheitIdentifier());
		return vmdlService;
	}

	private void impfungBoosterLoeschen(
		@NonNull Impfung impfung,
		@NonNull ImpfinformationDto impfinformationen) {

		Registrierung registrierung = impfinformationen.getRegistrierung();

		resetTimestampZuletztAbgeschlossen(impfinformationen, impfung);

		pdfArchivierungService.deleteImpfungArchive(registrierung);

		// delete the Impfung
		impfungRepo.delete(Impfung.toId(impfung.getId()));

		if (impfinformationen.getKrankheitIdentifier().isSupportsVMDL()) {
			VMDLServiceAbstract<? extends VMDLUploadBaseJax> vmdlService = getVmdlService(impfung);
			if (vmdlService.wasSentToVMDL(impfung)) {
				// Send delete request to BAG VMDL if the Impfung was sent (Impfung.timestampVMDL != null) or was at any moment in the past sent to VMDL (Impfung_AUD.timestampVMDL != null)
				// This guarantees that in case the Impfung was already sent once to VMDL and then corrected in VacMe (which causes the timestampVMDL to become null again)
				// that this Impfung is deleted from VMDL even if the current timestampVMDL is null
				vmdlService.deleteImpfung(impfung);
			}
		}

		Objects.requireNonNull(impfinformationen.getImpfdossier());
		LOG.info("VACME-KORREKTUR: Fuer die Registrierung {} wurde die {} Impfung vom {} geloescht durch {}",
			registrierung.getRegistrierungsnummer(),
			impfinformationen.getImpfdossier().getKrankheitIdentifier() == KrankheitIdentifier.AFFENPOCKEN ? KrankheitIdentifier.AFFENPOCKEN.name() : KrankheitIdentifier.COVID.name() + " Booster-",
			impfung.getTimestampImpfung(),
			this.userPrincipal.getBenutzerOrThrowException().getBenutzername());
	}

	private void resetTimestampZuletztAbgeschlossen(
		@NonNull ImpfinformationDto infos,
		@NonNull Impfung impfungToDelete
	) {
		// Reihenfolge umkehren (neueste Impfung zuerst)
		final List<Impfung> impfungenOrderedByImpffolgeNr =
			ImpfinformationenUtil.getImpfungenOrderedByImpffolgeNr(infos);
		Collections.reverse(impfungenOrderedByImpffolgeNr);

		final LocalDateTime timestampOfLatestImpfung = impfungenOrderedByImpffolgeNr
			.stream()
			.filter(impfung -> !impfung.getId().equals(impfungToDelete.getId()))
			.map(Impfung::getTimestampImpfung)
			.findFirst()
			.orElse(null);

		final LocalDateTime zweiteImpfungVerzichtetZeit =
			infos.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().getZweiteImpfungVerzichtetZeit();

		final LocalDateTime newTimestampZuletztAbgeschlossen =
			DateUtil.getLaterDateTimeOrNull(timestampOfLatestImpfung, zweiteImpfungVerzichtetZeit);

		infos.getImpfdossier().setTimestampZuletztAbgeschlossen(newTimestampZuletztAbgeschlossen);
	}

	public void impfungDatumKorrigieren(
		@NonNull ImpfungDatumKorrekturJax korrekturJax,
		@NonNull ImpfinformationDto impfinformationDto,
		@NonNull Impfung impfung
	) {
		Impftermin impftermin = impfung.getTermin();
		Registrierung registrierung = impfinformationDto.getRegistrierung();
		ImpfinformationenService.validateImpffolgeExists(korrekturJax.getImpffolge(), korrekturJax.getImpffolgeNr(), impfinformationDto);
		OrtDerImpfung ortDerImpfung = impftermin.getImpfslot().getOrtDerImpfung();

		final boolean needsNewZertifikat = korrekturJax.needsNewZertifikat(impfinformationDto, impfung.getTimestampImpfung());
		final boolean needToRevoke = korrekturJax.needToRevoke(impfinformationDto, impfung.getTimestampImpfung());

		Impftermin onDemandImpftermin = this.terminbuchungService.createOnDemandImpftermin(
			impfinformationDto.getKrankheitIdentifier(),
			impftermin.getImpffolge(),
			ortDerImpfung,
			korrekturJax.getTerminTime());

		// Wir rufen hier die Methode direkt auf, um die Validierung zu umgehen dass kein Termin freigegeben wird der eine Impfung  hat!
		impftermin.setGebuchtFromImpfterminRepo(false);

		final Impfdossier impfdossier = impfinformationDto.getImpfdossier();
		switch (impftermin.getImpffolge()) {
		case ERSTE_IMPFUNG:
			impfdossier.getBuchung().setImpftermin1FromImpfterminRepo(null);
			impfterminRepo.termin1Speichern(impfdossier, onDemandImpftermin);
			break;
		case ZWEITE_IMPFUNG:
			impfdossier.getBuchung().setImpftermin2FromImpfterminRepo(null);
			impfterminRepo.termin2Speichern(impfdossier, onDemandImpftermin);
			break;
		case BOOSTER_IMPFUNG:
			if (korrekturJax.getImpffolgeNr() == null) {
				throw new AppFailureException("ImpffolgeNr should not be null");
			}
			int impffolgeNr = korrekturJax.getImpffolgeNr();
			if (impfdossier == null) {
				throw AppValidationMessage.IMPFFOLGE_NUMBER_NOT_EXISTING.create(impffolgeNr);
			}
			Impfdossiereintrag eintrag = impfdossier.getOrderedEintraege().stream()
				.filter(impfdossiereintrag -> impfdossiereintrag.getImpffolgeNr() == impffolgeNr)
				.findFirst()
				.orElseThrow(() -> AppValidationMessage.IMPFFOLGE_NUMBER_NOT_EXISTING.create(impffolgeNr));
			eintrag.setImpfterminFromImpfterminRepo(null);
			impfterminRepo.boosterTerminSpeichern(eintrag, onDemandImpftermin);
			break;
		}

		// Die Impfung muss an den neuen Termin gehaengt werden
		impfung.setTermin(onDemandImpftermin);
		impfung.setTimestampImpfung(onDemandImpftermin.getImpfslot().getZeitfenster().getVon());

		// Schnittstellen neu befuellen
		resendToSchnittstellenAndRegenerateArchive(registrierung, impfung);

		// Auch das Zertifikat muss neu erstellt werden
		if (needsNewZertifikat) {
			registrierung.setGenerateZertifikatTrueIfAllowed(impfinformationDto, impfung);
			if (needToRevoke) {
				LOG.info("VACME-ZERTIFIKAT-REVOCATION: Zertifikat fuer Impfung {} wird revoked fuer Registrierung {}, "
						+ "da das Datum der Impfung veraendert wurde",
					impfung.getId(),
					registrierung.getRegistrierungsnummer());
				// Eventuell bereits vorhandene Zertifikate dieser Impfung muessen storniert werden
				zertifikatService.queueForRevocation(impfung);
			}
		}

		impfungRepo.update(impfung);
		LOG.info("VACME-KORREKTUR: Fuer die Registrierung {} wurde eine Korrektur des Datums vorgenommen {} durch {}",
			registrierung.getRegistrierungsnummer(),
			korrekturJax,
			this.userPrincipal.getBenutzerOrThrowException().getBenutzername());

		ValidationUtil.validateNotFuture(onDemandImpftermin);
		ValidationUtil.validateImpfungMinDate(onDemandImpftermin);

		List<ImpfInfo> orderedImpfInfos = BoosterPrioUtil.getImpfinfosOrderedByImpffolgeNr(impfinformationDto);
		ValidationUtil.validateDatumAbfolgeOfImpfungenStillValid(orderedImpfInfos, impfung);
		ValidationUtil.validateImpffolgenummernContinuous(impfinformationDto);
		// sicherstellen dass Infos fuer recalculation neu geladen werden
		boosterService.recalculateImpfschutzAndStatusmovesForSingleRegWithReload(impfinformationDto.getRegistrierung(), impfinformationDto.getKrankheitIdentifier());
	}

	public void personendatenKorrigieren(@NonNull Fragebogen fragebogen, @NonNull PersonendatenKorrekturJax korrekturJax) {
		final Registrierung registrierung = fragebogen.getRegistrierung();
		// we need to check this before we change the values in the database so that we can check if the values have changed
		boolean didInfoRelevantToZertifikatChange = korrekturJax.didInfoRelevantToZertifikatChange(registrierung);

		// Es gibt keine Kontrollen, die Angaben koennen in jedem Status angepasst werden
		final Consumer<Fragebogen> updateEntityConsumer = korrekturJax.getUpdateEntityConsumer();
		updateEntityConsumer.accept(fragebogen);

		// Die KT_NACKDOKUMENTATION role kann handy und mail des Benutzerobjects anpassen deshalb ermoeglichen wir das auch fuer die Registrierungsdaten
		if (registrierung.getRegistrierungsEingang() == RegistrierungsEingang.ONLINE_REGISTRATION
			&& userPrincipal.isCallerInRole(BenutzerRolle.KT_NACHDOKUMENTATION)
			&& propertyService.isEmailKorrekturEnabled()) {
			registrierung.setMail(korrekturJax.getMail());
			registrierung.setTelefon(korrekturJax.getTelefon());
		}

		List<ImpfinformationDto> infosPerImpfdossier =
			impfinformationenService.getImpfinformationenForAllDossiers(registrierung.getRegistrierungsnummer());

		for (ImpfinformationDto infos : infosPerImpfdossier) {

			// Unter Umstaenden aendert sich mein vollstaendigerImpfschutzTyp aufgrund einer Anpassung des immunsupprimiert-Flags:
			VacmeDecoratorFactory.getDecorator(infos.getKrankheitIdentifier())
				.recalculateVollstaendigerImpfschutzTypBasedOnImmunsupprimiert(infos, fragebogen.getImmunsupprimiert());

			// Schnittstellen neu befuellen
			resendImpfungenToVMDLAndRegenArchiveIfNecessary(infos);

			if (infos.getKrankheitIdentifier().isSupportsZertifikat()) {
				// Im Zertifikat sind Name, Vorname, Geburtsdatum
				boolean needsNewZertifikat = korrekturJax.needsNewZertifikat(infos.getImpfdossier(), didInfoRelevantToZertifikatChange);
				boolean needToRevoke = korrekturJax.needToRevoke(registrierung);

				// Ggf. das Zertifikat neu erstellen
				if (needsNewZertifikat) {
					// Die Personalien betreffen alle Zertifikate. Wir sollten daher fuer alle
					// Impfungen die vorhanden sind pruefen ob ein Zertifikat ausgestellt werden muss
					final List<Impfung> allImpfungen =
						ImpfinformationenUtil.getImpfungenOrderedByImpffolgeNr(infos);
					for (Impfung impfung : allImpfungen) {
						registrierung.setGenerateZertifikatTrueIfAllowed(infos, impfung);
					}// Eventuell bereits vorhandene Zertifikate (alle) muessen storniert werden
					if (needToRevoke) {
						// Die Personendaten betreffen immer alle Zertifikate. Wir muessen hier also alle revozieren
						LOG.info("VACME-ZERTIFIKAT-REVOCATION: Alle Zertifikate werden revoked fuer Registrierung {}, da Personendaten veraendert wurden",
							registrierung.getRegistrierungsnummer());
						zertifikatService.queueForRevocation(registrierung);
					}
				}
			}
			// Gegebenenfalls den Status anpassen
			impfdossierService.setStatusAccordingToPrioritaetFreischaltung(infos.getImpfdossier());

			boosterService.recalculateImpfschutzAndStatusmovesForSingleReg(infos);
		}

		LOG.info("VACME-KORREKTUR: Fuer die Registrierung {} wurde eine Korrektur der Personendaten vorgenommen {} durch {}",
			registrierung.getRegistrierungsnummer(),
			korrekturJax,
			this.userPrincipal.getBenutzerOrThrowException().getBenutzername());

	}

	public void updateEmailTelephone(@NonNull UUID benutzerId, @NonNull EmailTelephoneKorrekturJax korrekturJax) {
		Benutzer benutzer = benutzerRepo.getById(Benutzer.toId(benutzerId))
			.orElseThrow(AppValidationMessage.USER_NOT_FOUND::create);
		keyCloakService.updateUserLoginDataForRegistrierung(benutzer, korrekturJax.getMail(), korrekturJax.getTelefon());
		if (StringUtils.isNotBlank(korrekturJax.getMail())) {
			benutzer.setEmail(korrekturJax.getMail());
		}
		if (StringUtils.isNotBlank(korrekturJax.getTelefon())){
			benutzer.setMobiltelefon(korrekturJax.getTelefon());
		}
		benutzerRepo.merge(benutzer);
	}

	private void resendImpfungenToVMDLAndRegenArchiveIfNecessary(@Nullable ImpfinformationDto infos) {
		if (infos == null) {
			return;
		}
		if (infos.getImpfung1() != null) {
			resendImpfungVMDL(infos.getImpfung1());
		}
		if (infos.getImpfung2() != null) {
			resendImpfungVMDL(infos.getImpfung2());
		}
		if (CollectionUtils.isNotEmpty(infos.getBoosterImpfungen())) {
			for (Impfung impfung : infos.getBoosterImpfungen()) {
				resendImpfungVMDL(impfung);
			}
		}
		regenerateArchiveIfNecessary(infos.getRegistrierung());
	}

	private void resendToSchnittstellenAndRegenerateArchive(@NonNull Registrierung registrierung, @NonNull Impfung impfung) {
		resendImpfungVMDL(impfung);
		regenerateArchiveIfNecessary(registrierung);
	}

	private void regenerateArchiveIfNecessary(@NonNull Registrierung registrierung) {
		if (registrierung.getTimestampArchiviert() != null) {
			pdfArchivierungService.archiveManually(registrierung);
		}
	}

	private void resendImpfungVMDL(@NotNull Impfung impfung) {
		impfung.setTimestampVMDL(null); // Muss neu in Schnittstelle geschickt werden!
		impfungRepo.update(impfung);
	}
}
