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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.ImpfungkontrolleTermin;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.jax.registration.ExternGeimpftJax;
import ch.dvbern.oss.vacme.jax.registration.ImpfkontrolleJax;
import ch.dvbern.oss.vacme.jax.registration.ImpfkontrolleTerminJax;
import ch.dvbern.oss.vacme.repo.FragebogenRepo;
import ch.dvbern.oss.vacme.repo.ImpfdossierRepo;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.service.booster.BoosterService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ImpfinformationDtoRecreator;
import ch.dvbern.oss.vacme.util.PhoneNumberUtil;
import ch.dvbern.oss.vacme.util.ValidationUtil;
import ch.dvbern.oss.vacme.wrapper.VacmeDecoratorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ABGESCHLOSSEN;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.FREIGEGEBEN;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.FREIGEGEBEN_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.GEBUCHT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.GEBUCHT_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMMUNISIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_2_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.KONTROLLIERT_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.NEU;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ODI_GEWAEHLT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ODI_GEWAEHLT_BOOSTER;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ImpfkontrolleService {

	private final FragebogenRepo fragebogenRepo;
	private final RegistrierungRepo registrierungRepo;
	private final StammdatenService stammdatenService;
	private final ImpfungRepo impfungRepo;
	private final SmsService smsService;
	private final ImpfdossierFileService impfdossierFileService;
	private final PdfArchivierungService pdfArchivierungService;
	private final ImpfterminRepo impfterminRepo;
	private final ImpfdossierRepo impfdossierRepo;
	private final ZertifikatService zertifikatService;
	private final ImpfinformationenService impfinformationenService;
	private final ExternesZertifikatService externesZertifikatService;
	private final RegistrierungService registrierungService;
	private final BoosterService boosterService;
	private final ImpfdossierService impfdossierService;

	@ConfigProperty(name = "vacme.validation.kontrolle.disallow.sameday", defaultValue = "true")
	protected Boolean validateSameDayKontrolle;

	@NonNull
	public Registrierung createRegistrierung(
		@NonNull Fragebogen fragebogen,
		@NonNull ImpfkontrolleJax impfkontrolleJax
	) {
		final KrankheitIdentifier krankheitIdentifier = impfkontrolleJax.getKrankheitIdentifier();
		Registrierung registrierung = fragebogen.getRegistrierung();
		if (!registrierung.isNew()) {
			throw AppValidationMessage.EXISTING_REGISTRIERUNG.create(registrierung.getRegistrierungsnummer());
		}

		// registrierung die direkt in kontrolle erfasst wird hat keinen zugeordneten user
		registrierung.setBenutzerId(null);
		// Wenn eine Registrierung ueber die Impfkontrolle erfasst wird, hat sie immer den Eingang ORT_DER_IMPFUNG
		registrierung.setRegistrierungsEingang(RegistrierungsEingang.ORT_DER_IMPFUNG);
		registrierung.setRegistrierungsnummer(stammdatenService.createUniqueRegistrierungsnummer());
		registrierung.setPrioritaet(stammdatenService.calculatePrioritaet(fragebogen));
		registrierung.setRegistrationTimestamp(LocalDateTime.now());

		// Speichern, sonst kann man das externe Zertifikat nicht erstellen
		fragebogenRepo.create(fragebogen);
		impfdossierService.createImpfdossier(registrierung, krankheitIdentifier);
		// TODO Affenpocken: Aktuell erstellen wir immer ein COVID Dossier, auch wenn wir aktuell etwas anderes impfen
		if (KrankheitIdentifier.COVID != krankheitIdentifier) {
			impfdossierService.createImpfdossier(registrierung, KrankheitIdentifier.COVID);
		}

		// AGBs aktzeptieren
		impfdossierService.acceptLeistungserbringerAgb(registrierung.getRegistrierungsnummer(), krankheitIdentifier);

		Objects.requireNonNull(impfkontrolleJax.getExternGeimpft());
		if (krankheitIdentifier.isSupportsExternesZertifikat() && impfkontrolleJax.getExternGeimpft().isExternGeimpft()) {
			// Leider brauchen wir die Impfinfos bereits beim erstellen des ExternenZertifikats
			ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
				registrierung.getRegistrierungsnummer(),
				krankheitIdentifier);

			ExternesZertifikat externesZertifikat =
				externesZertifikatService.createExternGeimpft(infos, impfkontrolleJax.getExternGeimpft(), true);

			// Impfinfos neu initialisieren, nachdem das externe Zertifikat erstellt wurde
			ImpfinformationDto infosWithExtZert = ImpfinformationDtoRecreator.from(infos)
				.withExternemZertifikat(externesZertifikat).build();

			if (externesZertifikatService.isExternesZertGrundimmunisiertUndKontrolliert(externesZertifikat)) {
				// Extern grundimmunisiert -> direkt Boosterkontrolle machen, Impfdossiereintrag erstellen
				VacmeDecoratorFactory.getDecorator(infosWithExtZert.getKrankheitIdentifier())
					.setStatusToImmunisiertWithExternZertifikat(infosWithExtZert, externesZertifikat, fragebogen.getImmunsupprimiert());
				infosWithExtZert.getImpfdossier().setDossierStatus(KONTROLLIERT_BOOSTER);
				// Kontrolltermin NEU erstellen
				impfkontrolleTerminErstellen(infosWithExtZert, Impffolge.BOOSTER_IMPFUNG, impfkontrolleJax);
			} else {
				if (infosWithExtZert.getKrankheitIdentifier().isSupportsImpffolgenEinsUndZwei()) {
					infosWithExtZert.getImpfdossier().setDossierStatus(IMPFUNG_1_KONTROLLIERT);
					// Kontrolltermin NEU erstellen
					impfkontrolleTerminErstellen(infosWithExtZert, Impffolge.ERSTE_IMPFUNG, impfkontrolleJax);
				} else{
					infosWithExtZert.getImpfdossier().setDossierStatus(KONTROLLIERT_BOOSTER);
					// Kontrolltermin NEU erstellen
					impfkontrolleTerminErstellen(infosWithExtZert, Impffolge.BOOSTER_IMPFUNG, impfkontrolleJax);
				}
			}

			// bei ad-hoc wird ExternesZertifikat nie entfernt also muessen wir nur neu rechnen wenn wir eiens haben
			boosterService.recalculateImpfschutzAndStatusmovesForSingleReg(infosWithExtZert);
		} else {
			ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
				registrierung.getRegistrierungsnummer(),
				krankheitIdentifier);
			// Registrierung normal auf erste impfung inizialisieren
			if (krankheitIdentifier.isSupportsImpffolgenEinsUndZwei()) {
				infos.getImpfdossier().setDossierStatus(IMPFUNG_1_KONTROLLIERT);
				impfkontrolleTerminErstellen(infos, Impffolge.ERSTE_IMPFUNG, impfkontrolleJax);
			} else {
				infos.getImpfdossier().setDossierStatus(KONTROLLIERT_BOOSTER);
				impfkontrolleTerminErstellen(infos, Impffolge.BOOSTER_IMPFUNG, impfkontrolleJax);
				// neue Dossier sollten einmal berechnet werden da sie schon n-impfungen machen koennen
				boosterService.recalculateImpfschutzAndStatusmovesForSingleReg(infos);
			}
		}

		// Auch bei Registrierung im ODI soll ein SMS geschicket werden, sofern es sich um eine Mobile Nummer handelt
		if (PhoneNumberUtil.isMobileNumber(registrierung.getTelefon())) {
			Objects.requireNonNull(registrierung.getTelefon());
			smsService.sendOdiRegistrierungsSMS(registrierung, registrierung.getTelefon());
		}

		return registrierung;
	}

	private void impfkontrolleTerminErstellen(
		@NonNull ImpfinformationDto infos,
		@NonNull Impffolge impffolge,
		@NonNull ImpfkontrolleJax impfkontrolleJax
	) {
		// Kontrolltermin NEU erstellen
		int currentKontrolleNr = ImpfinformationenService.getCurrentKontrolleNr(infos);
		ImpfungkontrolleTermin impfungkontrolleTermin = getOrCreateImpfkontrolleTermin(
			infos, impffolge, currentKontrolleNr, null);
		Objects.requireNonNull(impfungkontrolleTermin);

		ImpfkontrolleTerminJax impfungkontrolleTerminJax = impfkontrolleJax.getImpfungkontrolleTermin();
		Objects.requireNonNull(impfungkontrolleTerminJax);

		impfungkontrolleTerminJax.apply(impfungkontrolleTermin);
	}

	public void kontrolleOkForOnboarding(
		@NonNull Fragebogen fragebogen,
		@NonNull Impffolge impffolge,
		@Nullable LocalDateTime impfDate
	) {
		ExternGeimpftJax dummyExternGeimpftJax = new ExternGeimpftJax();
		dummyExternGeimpftJax.setExternGeimpft(false);
		ImpfkontrolleJax dummyImpfkontrolleJax = new ImpfkontrolleJax();
		// TODO Affenpocken: Onboarding aktuell fix fuer Covid
		dummyImpfkontrolleJax.setKrankheitIdentifier(KrankheitIdentifier.COVID);
		kontrolleOk(fragebogen, impffolge, impfDate, dummyImpfkontrolleJax, dummyExternGeimpftJax);
	}

	public void kontrolleOk(
		@NonNull Fragebogen fragebogen,
		@NonNull Impffolge impffolge,
		@Nullable LocalDateTime kontrolleTimeParam,
		@NonNull ImpfkontrolleJax impfkontrolleJax,
		@NonNull @NotNull ExternGeimpftJax externGeimpftJax
	) {
		final KrankheitIdentifier krankheitIdentifier = impfkontrolleJax.getKrankheitIdentifier();
		Registrierung registrierung = fragebogen.getRegistrierung();


		// Wir brauchen die Impfinfos zum erstellen des ExternenZertifikats
		ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(
				registrierung.getRegistrierungsnummer(),
				krankheitIdentifier);

		ExternesZertifikat externesZertifikatOrNull = null;
		if (krankheitIdentifier.isSupportsExternesZertifikat()) {
			externesZertifikatOrNull = externesZertifikatService.createUpdateOrRemoveExternGeimpft(
				infos, externGeimpftJax, true);
		}

		// Impfinfos neu initialisieren, nachdem das ExterneZertifikat erstellt wurde
		ImpfinformationDto infosWithExtZert = ImpfinformationDtoRecreator.from(infos)
			.withExternemZertifikat(externesZertifikatOrNull).build();
		int currentKontrolleNr = ImpfinformationenService.getCurrentKontrolleNr(infosWithExtZert);

		Impffolge impffolgeEffektiv =
			preKontrolleJumpImpffolgeForExternesZertifikat(impffolge, externesZertifikatOrNull, infosWithExtZert, fragebogen.getImmunsupprimiert());
		kontrolleOkBasic(
			fragebogen,
			impffolgeEffektiv,
			currentKontrolleNr,
			kontrolleTimeParam,
			impfkontrolleJax,
			infosWithExtZert,
			externesZertifikatOrNull);
	}

	private void kontrolleOkBasic(
		@NonNull Fragebogen fragebogen,
		@NonNull Impffolge impffolge,
		@NonNull Integer impffolgeNr,
		@Nullable LocalDateTime kontrolleTime,
		@NonNull ImpfkontrolleJax impfkontrolleJax,
		@NonNull ImpfinformationDto infos,
		@Nullable ExternesZertifikat externesZertOrNull
	) {
		final KrankheitIdentifier krankheitIdentifier = impfkontrolleJax.getKrankheitIdentifier();
		Registrierung registrierung = infos.getRegistrierung();
		Impfdossier impfdossier = infos.getImpfdossier();
		switch (impffolge) {
		case ERSTE_IMPFUNG:
			ValidationUtil.validateStatusOneOf(impfdossier, NEU, FREIGEGEBEN, ODI_GEWAEHLT, GEBUCHT);

			// Normalfall -> kontrolliert 1
			impfdossier.setDossierStatus(IMPFUNG_1_KONTROLLIERT);
			break;
		case ZWEITE_IMPFUNG:
			ValidationUtil.validateStatusOneOf(impfdossier, IMPFUNG_1_DURCHGEFUEHRT);
			validateKontrolle2HasErstImpfungVorhanden(infos);
			validateNichtAmGleichenTagSchonKontrolliert(infos);
			impfdossier.setDossierStatus(IMPFUNG_2_KONTROLLIERT);
			break;
		case BOOSTER_IMPFUNG:
			ValidationUtil.validateStatusOneOf(impfdossier,
				GEBUCHT_BOOSTER, ODI_GEWAEHLT_BOOSTER,  // bei gebuchten Boosterimpfungen
				IMMUNISIERT, FREIGEGEBEN_BOOSTER, // bei ad hoc Impfungen
				ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG, ABGESCHLOSSEN);
			Objects.requireNonNull(impffolgeNr);

			if(!krankheitIdentifier.isSupportsNImpfungenWithoutGrundimmunisierung()) {
				boolean hasGrundimmunisierung =
					isGrundimmunisiertVacMeOrExternKontrolliert(infos.getImpfdossier(), externesZertOrNull);

				if (!hasGrundimmunisierung) {
					throw AppValidationMessage.ILLEGAL_STATE.create(
						"Fuer Booster muss eine Grundimmunisierung vorhanden sein");
				}
			}
			// Normalfall: vollstaendiger Impfschutz aus Vacme oder externem Zertifikat
			validateBoosterHasGrundimmunisierung(impfdossier, externesZertOrNull);
			validateNichtAmGleichenTagSchonKontrolliert(infos);
			impfdossier.setDossierStatus(KONTROLLIERT_BOOSTER);
			break;
		default:
			throw new IllegalStateException("Unexpected value: " + impffolge);
		}

		kontrollTerminErmittelnUndUpdaten(
			fragebogen,
			impffolge,
			impffolgeNr,
			kontrolleTime,
			impfkontrolleJax,
			registrierung);
	}

	private void eventuellVorhandenenBoosterTerminFreigeben(@NonNull ImpfinformationDto infos) {
		final List<Impfdossiereintrag> eintraege = infos.getImpfdossier().getImpfdossierEintraege();
		eintraege.forEach(impfterminRepo::boosterTerminFreigeben);
	}

	private void kontrollTerminErmittelnUndUpdaten(
		@NotNull Fragebogen fragebogen,
		@NotNull Impffolge impffolge,
		@NotNull Integer impffolgeNr,
		@Nullable LocalDateTime kontrolleTime,
		@NonNull ImpfkontrolleJax impfkontrolleJax,
		@NotNull Registrierung registrierung
	) {
		final KrankheitIdentifier krankheitIdentifier = impfkontrolleJax.getKrankheitIdentifier();
		ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
			registrierung.getRegistrierungsnummer(),
			krankheitIdentifier);

		// KontrolleTermin nehmen, der im jax ist, oder ad hoc einen erstellen
		UUID originalDossiereintragIDOrNull = impfkontrolleJax.getImpfdossierEintragId();
		ValidationUtil.validateCurrentKontrolleHasNoImpfungYet(infos, impffolge, originalDossiereintragIDOrNull);
		ImpfungkontrolleTermin impfungkontrolleTermin =
			getOrCreateImpfkontrolleTermin(infos, impffolge, impffolgeNr, originalDossiereintragIDOrNull);

		// Werte uebertragen von Jax zu Fragebogen und KontrolleTermin
		Objects.requireNonNull(
			impfungkontrolleTermin,
			"ImpfungkontrolleTermin wurde nicht gefunden fuer DE" + originalDossiereintragIDOrNull);
		impfkontrolleJax.apply(fragebogen, impfungkontrolleTermin);

		// Timestamp Kontrolle
		// Achtung: dies wird auch bei saveNoProceed aufgerufen, d.h. ein gesetzter timestampKontrolle heisst nicht
		// zwingend, dass auch tatsächlich kontrolliert wurde!
		LocalDateTime kontrolleTimeOrNow = kontrolleTime == null ? LocalDateTime.now() : kontrolleTime;
		impfungkontrolleTermin.setTimestampKontrolle(kontrolleTimeOrNow);

		// Gegebenenfalls den Status anpassen
		impfdossierService.setStatusAccordingToPrioritaetFreischaltung(infos.getImpfdossier());

		// AGBs aktzeptieren
		impfdossierService.acceptLeistungserbringerAgb(infos.getRegistrierung().getRegistrierungsnummer(), krankheitIdentifier);

		// Speichern
		if (impffolge == Impffolge.BOOSTER_IMPFUNG) {
			// kann erst Impfschutz neu berechnen wenn die changes auch den fragebogen applied wurden
			// Wir muessen alle Krankheiten neu berechnen, da z.B. das Geburtsdatum geaendert haben koennte
			boosterService.recalculateImpfschutzAndStatusmovesForSingleRegWithReload(infos.getRegistrierung());
		}
		fragebogenRepo.update(fragebogen);
	}

	// Macht den Jump bei der Kontrolle wegen dem extenen Zertifikat (wenn das Zertifikat bei der Kontrolle nochmals
	// geaendert wird)
	private Impffolge preKontrolleJumpImpffolgeForExternesZertifikat(
		@NonNull Impffolge impffolge,
		@Nullable ExternesZertifikat externesZertOrNull,
		@NonNull ImpfinformationDto infos,
		@Nullable Boolean immunsupprimiert
	) {
		Impfdossier impfdossier = infos.getImpfdossier();

		switch (impffolge) {
		case ERSTE_IMPFUNG:
			ValidationUtil.validateStatusOneOf(impfdossier, NEU, FREIGEGEBEN, ODI_GEWAEHLT, GEBUCHT);
			if (externesZertifikatService.isExternesZertGrundimmunisiertUndKontrolliert(externesZertOrNull)) {
				// extern immunisiert -> direkt boostern
				Objects.requireNonNull(externesZertOrNull);
				VacmeDecoratorFactory.getDecorator(infos.getKrankheitIdentifier())
					.setStatusToImmunisiertWithExternZertifikat(infos, externesZertOrNull, immunsupprimiert);
				// Eventuell noch vorhandene (nicht wahrgenommene) Termine 1 und 2 freigeben
				// Mit externem Zertifikat kann es keinen Termin 1 und 2 geben
				impfterminRepo.termine1Und2Freigeben(impfdossier);

				return Impffolge.BOOSTER_IMPFUNG;
			}
			return Impffolge.ERSTE_IMPFUNG;
		case ZWEITE_IMPFUNG:
			ValidationUtil.validateStatusOneOf(impfdossier, IMPFUNG_1_DURCHGEFUEHRT);
			if (externesZertifikatService.isExternesZertGrundimmunisiertUndKontrolliert(externesZertOrNull)) {
				// extern immunisiert -> direkt boostern
				Objects.requireNonNull(externesZertOrNull);
				VacmeDecoratorFactory.getDecorator(infos.getKrankheitIdentifier())
					.setStatusToImmunisiertWithExternZertifikat(infos, externesZertOrNull, immunsupprimiert);
				// Eventuell noch vorhandene (nicht wahrgenommene) Termine 1 und 2 freigeben
				// Mit externem Zertifikat kann es keinen Termin 1 und 2 geben
				impfterminRepo.termine1Und2Freigeben(impfdossier);

				return Impffolge.BOOSTER_IMPFUNG;
			}
			return Impffolge.ZWEITE_IMPFUNG;
		case BOOSTER_IMPFUNG:
			ValidationUtil.validateStatusOneOf(impfdossier,
				GEBUCHT_BOOSTER, ODI_GEWAEHLT_BOOSTER,  // bei gebuchten Boosterimpfungen
				IMMUNISIERT, FREIGEGEBEN_BOOSTER, // bei ad hoc Impfungen
				ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG, ABGESCHLOSSEN);

			boolean krankheitIsNImpfungOnly = !infos.getKrankheitIdentifier().isSupportsImpffolgenEinsUndZwei();
			if (krankheitIsNImpfungOnly) {
				VacmeDecoratorFactory.getDecorator(infos.getKrankheitIdentifier())
					.recalculateVollstaendigerImpfschutzTypBasedOnImmunsupprimiert(infos, immunsupprimiert);
				return Impffolge.BOOSTER_IMPFUNG;
			}

			boolean hasGrundimmunisierung =
				isGrundimmunisiertVacMeOrExternKontrolliert(infos.getImpfdossier(), externesZertOrNull);

			if (!hasGrundimmunisierung) {
				// nicht grundimmunisiert -> zurueck zu kontrolliert 1 springen, aber es darf noch keine
				// VacMe-Impfungen haben.
				if (impfinformationenService.hasVacmeImpfungen(infos)) {
					throw AppValidationMessage.EXISTING_VACME_IMPFUNGEN_CANNOT_REMOVE_GRUNDIMMUN.create();
				}

				impfdossier.setStatusToNichtAbgeschlossenStatus(
					infos,
					impfdossierService.ermittleLetztenDossierStatusVorKontrolle1(impfdossier),
					null);
				// Einen eventuell vorhandenen Booster-Termin freigeben
				eventuellVorhandenenBoosterTerminFreigeben(infos);
					return Impffolge.ERSTE_IMPFUNG;
			}

			return Impffolge.BOOSTER_IMPFUNG;
		default:
			throw new IllegalStateException("Unexpected value: " + impffolge);
		}

	}

	@NonNull
	public ImpfungkontrolleTermin getOrCreateImpfkontrolleTermin(
		@NonNull ImpfinformationDto impfinformationen,
		@NonNull Impffolge impffolge,
		@NonNull Integer impffolgeNr,
		@Nullable UUID dossiereintragID
	) {
		Impfdossier impfdossier = impfinformationen.getImpfdossier();

		switch (impffolge) {
		case ERSTE_IMPFUNG:
			ImpfungkontrolleTermin kontrolleTermin1 = impfdossier.getOrCreatePersonenkontrolle().getKontrolleTermin1();
			if (kontrolleTermin1 == null) {
				impfdossier.getOrCreatePersonenkontrolle().setKontrolleTermin1(new ImpfungkontrolleTermin());
			}
			return impfdossier.getOrCreatePersonenkontrolle().getKontrolleTermin1();

		case ZWEITE_IMPFUNG:
			ImpfungkontrolleTermin kontrolleTermin2 = impfdossier.getOrCreatePersonenkontrolle().getKontrolleTermin2();
			if (kontrolleTermin2 == null) {
				impfdossier.getOrCreatePersonenkontrolle().setKontrolleTermin2(new ImpfungkontrolleTermin());
			}
			return impfdossier.getOrCreatePersonenkontrolle().getKontrolleTermin2();

		case BOOSTER_IMPFUNG:
			// Bestehenden Kontrolltermin per ID suchen, falls gegeben, und passe die ImpffolgeNr an
			Optional<ImpfungkontrolleTermin> existingKontrolleTerminOpt =
				getExistingBoosterKontrolleTerminAndUpdateImpffolgeNr(
					impfinformationen, impffolgeNr, dossiereintragID);

			if (existingKontrolleTerminOpt.isPresent()) {
				// KontrolleTermin existiert schon
				return existingKontrolleTerminOpt.get();
			} else {
				// Sonst: Eintrag mit der gewuenschten ImpffolgeNr suchen oder neu erstellen
				Impfdossiereintrag dossierEintragAltOrNeu =
					impfinformationenService.getOrCreateLatestImpfdossierEintrag(impfinformationen, impffolgeNr);
				// Der gefundene Eintrag hat in der Regel noch keinen Kontrolltermin haben, aber es kann trotzdem sein:
				// Wenn man Boosterkontrolle macht, das externeZertifikat entfernt, erneut die Kontrolle macht, das
				// externeZertifikat hinzufuegt
				if (dossierEintragAltOrNeu.getImpfungkontrolleTermin() != null) {
					return dossierEintragAltOrNeu.getImpfungkontrolleTermin();
				} else {
					// Normal: Neuen Kontrolltermin fuer den Eintrag erstelltn
					ImpfungkontrolleTermin termin = new ImpfungkontrolleTermin();
					dossierEintragAltOrNeu.setImpfungkontrolleTermin(termin);
					return termin;
				}
			}

		default:
			throw new IllegalStateException("Unexpected value: " + impffolge);
		}
	}

	// todo team: evtl splitten in 2 methoden?
	private Optional<ImpfungkontrolleTermin> getExistingBoosterKontrolleTerminAndUpdateImpffolgeNr(
		@NonNull ImpfinformationDto impfinformationen,
		@NonNull Integer impffolgeNr,
		@Nullable UUID eintragId) {
		if (eintragId != null) {
			Optional<Impfdossiereintrag> existingDossiereintragOptional =
				ImpfinformationenService.getImpfdossierEintragWithID(impfinformationen, eintragId);
			if (existingDossiereintragOptional.isPresent()) {
				Impfdossiereintrag eintrag = existingDossiereintragOptional.get();

				// ImpffolgeNr geaendert?
				if (!eintrag.getImpffolgeNr().equals(impffolgeNr)) {
					// ImpffolgeNr muss fuer diesen Eintrag geaendert werden - wenn moeglich! Andere Eintraege muss
					// man gegebenenfalls loeschen.
					changeImpffolgeNrAndDeleteOtherEintraege(impfinformationen, impffolgeNr, eintrag);
				}

				// Kontrolletermin
				if (eintrag.getImpfungkontrolleTermin() != null) {
					return Optional.of(eintrag.getImpfungkontrolleTermin());
				}
			}
		}
		return Optional.empty();
	}

	private void changeImpffolgeNrAndDeleteOtherEintraege(
		@NonNull ImpfinformationDto impfinformationen,
		@NonNull Integer impffolgeNr,
		@NonNull Impfdossiereintrag eintrag) {
		Impfdossier dossier = impfinformationen.getImpfdossier();
		Validate.notNull(dossier, "Impfdossier kann nicht null sein, wir haben ja soeben einen Eintrag daraus "
			+ "gelesen");

		// Die Impffolgenummer kann nur beim allerersten Booster angepasst werden wenn man ein externes Zertifikat hat
		// In diesem Fall entfernen wir alle Termine die potentiell sonst noch an anderen Eintraegen haengen und
		// entfernen die anderen
		// Eintraege.
		// Alle anderen Eintraege loeschen. Wenn Impfung dranhaengt: Fehler, wenn Termin dranhaengt: freigeben.
		Collection<Impfdossiereintrag> otherEintraege = dossier.getImpfdossierEintraege().stream()
			.filter(impfdossiereintrag -> !impfdossiereintrag.getId().equals(eintrag.getId()))
			.collect(Collectors.toList());
		for (Impfdossiereintrag otherEintrag : otherEintraege) {
			// Wenn Termin gebucht ist: freigeben (und Fehler werfen, wenn schon eine Impfung dran haengt)
			impfterminRepo.boosterTerminFreigeben(otherEintrag);
			// Eintrag loeschen
			impfdossierRepo.deleteEintrag(otherEintrag, impfinformationen.getImpfdossier());
		}

		// ImpffolgeNr updaten
		Integer originalImpffolge = eintrag.getImpffolgeNr();
		eintrag.setImpffolgeNr(impffolgeNr);
		LOG.info("ImpffolgeNr fuer Dossiereintrag geaendert von {} zu {} (Reg. {})", originalImpffolge, impffolgeNr,
			impfinformationen.getRegistrierung().getRegistrierungsnummer());
	}

	public void zweiteImpfungVerzichten(
		@NonNull ImpfinformationDto impfInfo,
		boolean vollstaendigerImpfschutz,
		@Nullable String begruendung,
		@Nullable LocalDate positivGetestetDatum
	) {
		Registrierung registrierung = impfInfo.getRegistrierung();
		Impfdossier impfdossier = impfInfo.getImpfdossier();
		ValidationUtil.validateStatusOneOf(impfdossier, IMPFUNG_2_KONTROLLIERT, IMPFUNG_1_DURCHGEFUEHRT);
		impfInfo.getImpfdossier().setStatusToAbgeschlossenOhneZweiteImpfung(
			impfInfo,
			vollstaendigerImpfschutz,
			begruendung,
			positivGetestetDatum);
		registrierungRepo.update(registrierung);
		// Den zweiten Termin loeschen
		impfterminRepo.termin2Freigeben(impfdossier);
		// Freigabe Booster neu berechnen
		boosterService.recalculateImpfschutzAndStatusmovesForSingleReg(impfInfo);
	}

	public void zweiteImpfungWahrnehmen(@NonNull ImpfinformationDto infos) {

		Registrierung registrierung = infos.getRegistrierung();

		if (infos.getImpfung2() != null) {
			throw new AppFailureException("Ein zweite Impfung ist schon für die registrierung vorhanden");
		}
		if (infos.getBoosterImpfungen() != null && !infos.getBoosterImpfungen().isEmpty()) {
			throw new AppFailureException("Es besteht bereits eine BoosterImpfung fuer diese Registrierung");
		}
		String reqMesg = "Erste Impfung muss existieren wenn man die 2.Impfung nach Verzicht doch noch wahrnehmen "
			+ "will";
		final Impftermin impfterminErsteImpffolge = infos.getImpfdossier().getBuchung().getImpftermin1();
		Objects.requireNonNull(impfterminErsteImpffolge, reqMesg);
		final Impfung impfung1 = impfungRepo.getByImpftermin(impfterminErsteImpffolge).orElse(null);
		Objects.requireNonNull(impfung1, reqMesg);

		if (registrierung.getTimestampArchiviert() != null) {
			pdfArchivierungService.deleteImpfungArchive(registrierung);
		}

		// Eventuell bereits vorhandene Zertifikate muessen storniert werden
		if (infos.getImpfdossier().abgeschlossenMitVollstaendigemImpfschutz()) {
			// Erst ab 14.0.0 koennen wir sicher sein, dass die Impfung verknuepft ist mit dem Zertifikat
			// Aber diese Funktion wird nur aufgerufen wenn nun doch noch ein 2. Termin wahrgenommen wird. Das heiss
			// wir koennen alle bisherigen Zertifkate revozieren
			LOG.info(
				"VACME-ZERTIFIKAT-REVOCATION: Zertifikat wird revoked fuer Registrierung {}, da die 2. Impfung "
					+ "noch wargenommen wird",
				registrierung.getRegistrierungsnummer());
			zertifikatService.queueForRevocation(registrierung);
		}
		infos.getImpfdossier().setStatusToNichtAbgeschlossenStatus(infos, IMPFUNG_1_DURCHGEFUEHRT, impfung1);
		impfdossierService.update(infos.getImpfdossier());
	}

	private void validateKontrolle2HasErstImpfungVorhanden(
		@NonNull ImpfinformationDto infos
	) {
		final Impftermin impfterminErsteImpffolge = infos.getImpfdossier().getBuchung().getImpftermin1();
		Objects.requireNonNull(impfterminErsteImpffolge,
			"Bei der zweiten Kontrolle muss zwingend eine Impfung1 vorhanden sein");
	}

	private void validateBoosterHasGrundimmunisierung(
		@NonNull Impfdossier impfdossier,
		@Nullable ExternesZertifikat externesZertifikat
	) {
		if (!impfdossier.getKrankheitIdentifier().isSupportsImpffolgenEinsUndZwei()) {
			// Wir sind von Anfang an im Booster-Modus, es wird daher keine Grundimmunisierung vorausgesetzt
			return;
		}
		// Grundimmunisiert in vacme COVID
		if (impfdossier.abgeschlossenMitVollstaendigemImpfschutz()) {
			return;
		}
		// Grundimmunisierung EZ
		if (!externesZertifikatService.isExternesZertGrundimmunisiertUndKontrolliert(externesZertifikat)) {
			throw AppValidationMessage.MISSING_GRUNDIMMUNISIERUNG.create();
		}
	}

	private void validateNichtAmGleichenTagSchonKontrolliert(@NonNull ImpfinformationDto infos) {
		// TODO Scheinbar validieren wir nur bei den ersten beiden Impfungen, dass sie nicht am selben Tag sind...
		if (validateSameDayKontrolle) {
			final Impftermin impfterminErsteImpffolge = infos.getImpfdossier().getBuchung().getImpftermin1();
			if (impfterminErsteImpffolge != null) {
				// impfung koennte exsitieren, muss aber nicht (wenn zwar ein Ersttermin existiert, aber bei der
				// Kontrolle ein externes Zertifikat
				// hinzugefuegt wurde)
				final Optional<Impfung> impfung1Opt = impfungRepo.getByImpftermin(impfterminErsteImpffolge);
				impfung1Opt.ifPresent(ValidationUtil::validateSecondKontrolleOnSameDay);
			}
		}
	}

	/**
	 * Updates both {@link ImpfkontrolleTerminJax} on the {@link ImpfkontrolleJax}
	 *
	 * @param fragebogen no-doc
	 * @param impfkontrolleJax no-doc
	 */
	public void saveKontrolleNoProceed(
		@NonNull Fragebogen fragebogen,
		@NonNull ImpfkontrolleJax impfkontrolleJax,
		@NonNull Impffolge impffolge
	) {
		Objects.requireNonNull(impfkontrolleJax.getExternGeimpft(), "ExternesZertifikat soll vom client immer" +
			" uebermittelt werden. Felder koennen aber null sein");
		Registrierung registrierung = fragebogen.getRegistrierung();

		// Wir brauchen die Infos bereits jetzt zum Erstellen des Externen Zertifikats
		ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
			registrierung.getRegistrierungsnummer(),
			impfkontrolleJax.getKrankheitIdentifier());

		ExternesZertifikat externesZertifikatOrNull = null;
		if (impfkontrolleJax.getKrankheitIdentifier().isSupportsExternesZertifikat()) { // manche Krankheiten haben keine Zertifikate
			externesZertifikatOrNull = externesZertifikatService.createUpdateOrRemoveExternGeimpft(
				infos,
				impfkontrolleJax.getExternGeimpft(),
				true);
		}
		// Impfinfos neu initialisieren, nachdem das externe Zertifikat erstellt wurde
		ImpfinformationDto infosWithExtZert = ImpfinformationDtoRecreator.from(infos)
			.withExternemZertifikat(externesZertifikatOrNull).build();
		int currentKontrolleNr = ImpfinformationenService.getCurrentKontrolleNr(infosWithExtZert);

		Impffolge impffolgeEffektiv =
			preKontrolleJumpImpffolgeForExternesZertifikat(impffolge, externesZertifikatOrNull, infosWithExtZert, fragebogen.getImmunsupprimiert());
		kontrollTerminErmittelnUndUpdaten(
			fragebogen,
			impffolgeEffektiv,
			currentKontrolleNr,
			null,
			impfkontrolleJax,
			registrierung);
		// hier machen wir nicht das Statusupdate auf Kontrolle 1/2/Booster

	}

	private boolean isGrundimmunisiertVacMeOrExternKontrolliert(
		@NonNull Impfdossier impfdossier,
		@Nullable ExternesZertifikat externesZertOrNull) {

		// vollstaendig dank dem soeben bearbeiteten externesZertifikat?
		if (externesZertifikatService.isExternesZertGrundimmunisiertUndKontrolliert(externesZertOrNull)) {
			return true;
		}

		// vollstaendig dank Vacme, d.h. schon vor dieser Kontrolle? Achtung hier duerfen keine untrusted values zu
		// true fuehren
		// (e.g VOLLSTAENDIG_EXTERNESZERTIFIKAT geht nicht weil das vom Impfwilligen direkt gesettz wird)
		var impfschutzTyp = impfdossier.getVollstaendigerImpfschutzTyp();
		if (impfschutzTyp != null) {
			switch (impfschutzTyp) {
			case VOLLSTAENDIG_VACME:
			case VOLLSTAENDIG_VACME_GENESEN:
				return true;
			case VOLLSTAENDIG_EXT_PLUS_VACME:
			case VOLLSTAENDIG_EXT_GENESEN_PLUS_VACME:
				Validate.notNull(
					externesZertOrNull,
					"VollstaendigerImpfschutzTyp " + impfschutzTyp + ", aber das ExterneZertifikat ist null");
				return externesZertOrNull.isKontrolliert();
			default:
				// Achtung hier darf VOLLSTAENDIG_EXTERNESZERTIFIKAT nicht drin sein weil wir nur explizit in vacme
				// gemachte Impfungen anschauen
				return false;
			}
		}
		return false;
	}
}
