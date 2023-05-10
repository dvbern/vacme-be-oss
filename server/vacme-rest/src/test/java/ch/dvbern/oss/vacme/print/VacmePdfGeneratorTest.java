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

package ch.dvbern.oss.vacme.print;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import ch.dvbern.oss.vacme.entities.base.ZertifikatInfo;
import ch.dvbern.oss.vacme.entities.embeddables.Adresse;
import ch.dvbern.oss.vacme.entities.embeddables.DateTimeRange;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsart;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsort;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsseite;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfungTyp;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import ch.dvbern.oss.vacme.print.archivierung.ArchivierungPdfGenerator;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationBuilder;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class VacmePdfGeneratorTest {

	private static final String pfad = FileUtils.getTempDirectoryPath() + "/generated/vacme/";
	private Registrierung registrierung;
	private Impfdossier dossier;
	private OrtDerImpfung odi;
	private Impftermin boosterTerminOrNull;

	private RegistrationsbestaetigungPdfGenerator registrationsbestaetigungPdfGenerator;
	private TerminbestaetigungPdfGenerator terminbestaetigungPdfGenerator;
	private TerminabsagePdfGenerator terminabsagePdfGenerator;
	private ZertifikatStornierungPdfGenerator zertifikatStornierungPdfGenerator;
	private OnboardingPdfGenerator onboardingPdfGenerator;
	private FreigabeBoosterPdfGenerator freigabeBoosterPdfGenerator;

	@BeforeEach
	void setUp() throws IOException {
		System.setProperty("vacme.mandant", "BE");
		FileUtils.forceMkdir(new File(pfad));
		for (Mandant value : Mandant.values()) {
			FileUtils.forceMkdir(new File(pfad + '/' + value));
		}
		setDummyData();
		System.setProperty("vacme.server.base.url", "http://impfen-vacme-dev.dvbern.ch");
	}

	@ParameterizedTest
	@EnumSource(value = Mandant.class, mode = Mode.MATCH_ALL)
	public void registrationsBestaetigung(Mandant mandant) throws InvoiceGeneratorException, IOException {
		createRegistrationsBestaetigung(RegistrierungsEingang.ONLINE_REGISTRATION, mandant.name(), Prioritaet.A);
		createRegistrationsBestaetigung(RegistrierungsEingang.CALLCENTER_REGISTRATION, mandant.name(), Prioritaet.A);
		createRegistrationsBestaetigung(RegistrierungsEingang.ONLINE_REGISTRATION, mandant.name(), Prioritaet.Z);
	}

	private void createRegistrationsBestaetigung(@NonNull RegistrierungsEingang eingang, @NonNull String mandant, @NonNull Prioritaet prioritaet
	) throws FileNotFoundException, InvoiceGeneratorException {
		System.setProperty("vacme.mandant", mandant);
		registrierung.setRegistrierungsEingang(eingang);
		registrierung.setPrioritaet(prioritaet);
		registrationsbestaetigungPdfGenerator = new RegistrationsbestaetigungPdfGenerator(registrierung, MandantUtil.getMandant() == Mandant.BE ? "https://be.vacme.ch" : "https://zh.vacme.ch");
		final String filename = "Registrationsbestaetigung_" + eingang.toString() + '_' + prioritaet.toString();
		registrationsbestaetigungPdfGenerator.generate(getFileOutputSteam(mandant, filename));
	}



	@ParameterizedTest
	@EnumSource(value = Mandant.class, mode = Mode.MATCH_ALL)
	public void terminBestaetigung(Mandant mandant) throws InvoiceGeneratorException, IOException {
		createTerminBestaetigung(null, false, false, false, null, mandant.name());
		createTerminBestaetigung("Der Empfang wird renoviert.", false, false, false, null, mandant.name());
		createTerminBestaetigung("Der Empfang wird renoviert.", true, true, false, null, mandant.name());
		createTerminBestaetigung("Der Empfang wird renoviert.", false, true, false, null, mandant.name());
		dossier.setDossierStatus(ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT);
		createTerminBestaetigung(null, false, false, false, null, mandant.name());
		// Reset registrierungStatus
		dossier.setDossierStatus(ImpfdossierStatus.FREIGEGEBEN);
		dossier.getBuchung().setNichtVerwalteterOdiSelected(true);
		dossier.getBuchung().setGewuenschterOdi(null);
		dossier.getBuchung().setImpftermin1FromImpfterminRepo(null);
		dossier.getBuchung().setImpftermin2FromImpfterminRepo(null);
		createTerminBestaetigung(null, false, false, true, null, mandant.name());
	}

	@ParameterizedTest
	@EnumSource(value = Mandant.class, mode = Mode.MATCH_ALL)
	public void terminBestaetigungBooster(Mandant mandant) throws InvoiceGeneratorException, IOException {
		createTerminBestaetigung(null, false, false, false, boosterTerminOrNull, mandant.name());
		createTerminBestaetigung("Der Empfang wird renoviert.", false, false, false, boosterTerminOrNull, mandant.name());
		createTerminBestaetigung("Der Empfang wird renoviert.", true, true, false, boosterTerminOrNull, mandant.name());
		createTerminBestaetigung("Der Empfang wird renoviert.", false, true, false, boosterTerminOrNull, mandant.name());
		Registrierung reg2 = registrierung;
		dossier.getBuchung().setNichtVerwalteterOdiSelected(true);
		dossier.getBuchung().setGewuenschterOdi(null);
		dossier.getBuchung().setImpftermin1FromImpfterminRepo(null);
		dossier.getBuchung().setImpftermin2FromImpfterminRepo(null);
		createTerminBestaetigung(null, false, false, true, boosterTerminOrNull, mandant.name());
		registrierung = reg2;
	}

	private void createTerminBestaetigung(
		@Nullable String odiKommentar,
		boolean mobilerOdi,
		boolean ohneTermine,
		boolean nichtVerwalteterOdi,
		@Nullable Impftermin termin,
		@NonNull String mandant
	) throws FileNotFoundException,	InvoiceGeneratorException {
		System.setProperty("vacme.mandant", mandant);
		odi.setKommentar(odiKommentar);
		odi.setMobilerOrtDerImpfung(mobilerOdi);
		odi.setTerminverwaltung(!ohneTermine);

		if (termin != null) {
			dossier.setDossierStatus(ImpfdossierStatus.IMMUNISIERT);
		}

		String bemerkung = StringUtils.isEmpty(odiKommentar) ? "ohneBemerkung" : "mitBemerkung";
		String typ = "Impfzentrum";
		if (ohneTermine) {
			typ = "OhneTermine";
		}
		if (mobilerOdi) {
			typ = "MobilerOrtDerImpfung";
		}
		if (nichtVerwalteterOdi) {
			typ = "NichtVerwalteterOdi";
		}
		if (ImpfdossierStatus.isErsteImpfungDoneAndZweitePending().contains(dossier.getDossierStatus())) {
			typ = "NurTermin2";
		}
		String status = termin != null ? "Booster" : "Grundimmunisierung";

		// Dokumente fuer COVID
		ImpfinformationDto infosCovid =
			new ImpfinformationDto(KrankheitIdentifier.COVID, registrierung, null, null, dossier, null);
		String filenameCovid = "Terminbestaetigung_" + infosCovid.getKrankheitIdentifier() + '_' + status + '_' + bemerkung + '_' + typ;
		terminbestaetigungPdfGenerator = new TerminbestaetigungPdfGenerator(infosCovid.getImpfdossier(), termin, KantonaleBerechtigung.KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG);
		terminbestaetigungPdfGenerator.generate(getFileOutputSteam(mandant, filenameCovid));

		// Dokumente fuer AFFENPOCKEN
		ImpfinformationDto infosAffenpocken =
			new ImpfinformationDto(KrankheitIdentifier.AFFENPOCKEN, registrierung, null, null, dossier, null);
		String filenameAffenpocken = "Terminbestaetigung_" + infosAffenpocken.getKrankheitIdentifier() + '_' + status + '_' + bemerkung + '_' + typ;
		terminbestaetigungPdfGenerator = new TerminbestaetigungPdfGenerator(infosAffenpocken.getImpfdossier(), termin, KantonaleBerechtigung.KANTONALE_IMPFKAMPAGNE);
		terminbestaetigungPdfGenerator.generate(getFileOutputSteam(mandant, filenameAffenpocken));

		// Dokumente fuer FSME
		ImpfinformationDto infosFSME =
			new ImpfinformationDto(KrankheitIdentifier.FSME, registrierung, null, null, dossier, null);
		String filenameFSME = "Terminbestaetigung_" + infosFSME.getKrankheitIdentifier() + '_' + status + '_' + bemerkung + '_' + typ;
		terminbestaetigungPdfGenerator = new TerminbestaetigungPdfGenerator(infosFSME.getImpfdossier(), termin, KantonaleBerechtigung.LEISTUNGSERBRINGER);
		terminbestaetigungPdfGenerator.generate(getFileOutputSteam(mandant, filenameFSME));
		dossier.setKrankheitIdentifier(KrankheitIdentifier.COVID);
	}

	@ParameterizedTest
	@EnumSource(value = Mandant.class, mode = Mode.MATCH_ALL)
	public void impfDokumentation(Mandant mandant) throws InvoiceGeneratorException, IOException {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		final ImpfinformationDto infos = builder
			.create(KrankheitIdentifier.COVID)
			.withImpfung1(LocalDate.now().minusDays(3 * 7), TestdataCreationUtil.createImpfstoffModerna())
			.withImpfung2(LocalDate.now(), TestdataCreationUtil.createImpfstoffModerna())
			.getInfos();
		final Impfung impfung1 = infos.getImpfung1();
		final Impfung impfung2 = infos.getImpfung2();
		Assertions.assertNotNull(impfung1);

		createImpfdokumentation(impfung1, null, "ersteImpfung", mandant.name(), null);

		impfung1.setExtern(true);
		createImpfdokumentation(impfung1, impfung2, "ersteImpfungExtern", mandant.name(), null);
		impfung1.setExtern(false);

		createImpfdokumentation(impfung1, impfung2, "zweiteImpfung", mandant.name(), null);

		impfung1.setImpfstoff(TestdataCreationUtil.createImpfstoffJanssen());
		infos.getImpfdossier().setStatusToAbgeschlossen(infos, impfung1);
		createImpfdokumentation(impfung1, null, "ImpfstoffNurEineImpfung", mandant.name(), null);

		impfung1.setImpfstoff(getModernaImpfstoff());
		infos.getImpfdossier().setStatusToAbgeschlossenOhneZweiteImpfung(infos, false, "Hat aua gemacht!", LocalDate.now());
		createImpfdokumentation(impfung1, null, "aufZweiteImpfungVerzichtet_unvollstaendigerImpfschutz", mandant.name(), null);

		infos.getImpfdossier().setStatusToAbgeschlossenOhneZweiteImpfung(infos, true, null, LocalDate.now());
		createImpfdokumentation(impfung1, null, "aufZweiteImpfungVerzichtet_vollstaendigerImpfschutz", mandant.name(), null);

		// Not sure if this is a possible case, test anyway
		Impfung boosterImpfung =
			generateBoosterImpfungWithTermin((LocalDateTime.now().plusDays(365)), Impffolge.BOOSTER_IMPFUNG);
		boosterImpfung.setExtern(true);
		createImpfdokumentation(impfung1, impfung2, "zweiteImpfungMitExternerBooster", mandant.name(), List.of(boosterImpfung));
		createImpfdokumentation(impfung1, impfung2, "zweiteImpfungMitBooster", mandant.name(), List.of(generateImpfungWithTermin((LocalDateTime.now().plusDays(365)), Impffolge.BOOSTER_IMPFUNG)));
		Impfung selbstzahlerImpfung =
			generateBoosterImpfungWithTermin((LocalDateTime.now().plusDays(4 * 365)), Impffolge.BOOSTER_IMPFUNG);
		selbstzahlerImpfung.setSelbstzahlende(true);
		createImpfdokumentation(impfung1, impfung2, "zweiteImpfungMitVielenBoosterImpfungen", mandant.name(), List.of(
			generateBoosterImpfungWithTermin((LocalDateTime.now().plusDays(365)), Impffolge.BOOSTER_IMPFUNG),
			generateBoosterImpfungWithTermin((LocalDateTime.now().plusDays(2*365)), Impffolge.BOOSTER_IMPFUNG),
			generateBoosterImpfungWithTermin((LocalDateTime.now().plusDays(3*365)), Impffolge.BOOSTER_IMPFUNG),
			selbstzahlerImpfung,
			generateBoosterImpfungWithTermin((LocalDateTime.now().plusDays(5*365)), Impffolge.BOOSTER_IMPFUNG),
			generateBoosterImpfungWithTermin((LocalDateTime.now().plusDays(6*365)), Impffolge.BOOSTER_IMPFUNG),
			generateBoosterImpfungWithTermin((LocalDateTime.now().plusDays(7*365)), Impffolge.BOOSTER_IMPFUNG),
			generateBoosterImpfungWithTermin((LocalDateTime.now().plusDays(8*365)), Impffolge.BOOSTER_IMPFUNG),
			generateBoosterImpfungWithTermin((LocalDateTime.now().plusDays(9*365)), Impffolge.BOOSTER_IMPFUNG)));
	}

	@ParameterizedTest
	@EnumSource(value = Mandant.class, mode = Mode.MATCH_ALL)
	public void terminabsage(Mandant mandant) throws InvoiceGeneratorException, IOException {
		System.setProperty("vacme.mandant", mandant.name());
		final String filename = "TerminabsageZweittermin";
		Objects.requireNonNull(dossier.getBuchung().getImpftermin2());
		terminabsagePdfGenerator = new TerminabsagePdfGenerator(registrierung, dossier.getBuchung().getImpftermin2(), null,
			dossier.getBuchung().getImpftermin2().getTerminZeitfensterStartDateAndTimeString(), null);
		terminabsagePdfGenerator.generate(getFileOutputSteam(mandant.name(), filename));
	}

	@ParameterizedTest
	@EnumSource(value = Mandant.class, mode = Mode.MATCH_ALL)
	public void terminabsageBeideTermine(Mandant mandant) throws InvoiceGeneratorException, IOException {
		System.setProperty("vacme.mandant", mandant.name());
		final String filename = "TerminabsageErsttermin";
		Objects.requireNonNull(dossier.getBuchung().getImpftermin1());
		Objects.requireNonNull(dossier.getBuchung().getImpftermin2());
		@NonNull String terminEffectiveStart = dossier.getBuchung().getImpftermin1().getTerminZeitfensterStartDateAndTimeString();
		@NonNull String termin2EffectiveStart = dossier.getBuchung().getImpftermin2().getTerminZeitfensterStartDateAndTimeString();
		terminabsagePdfGenerator = new TerminabsagePdfGenerator(registrierung, dossier.getBuchung().getImpftermin1(), dossier.getBuchung().getImpftermin2(),
			terminEffectiveStart, termin2EffectiveStart);
		terminabsagePdfGenerator.generate(getFileOutputSteam(mandant.name(), filename));
	}

	@ParameterizedTest
	@EnumSource(value = Mandant.class, mode = Mode.MATCH_ALL)
	public void terminabsageBoosterTermin(Mandant mandant) throws InvoiceGeneratorException, IOException {
		System.setProperty("vacme.mandant", mandant.name());
		final String filename = "TerminabsageBooster";
		Objects.requireNonNull(dossier.getBuchung().getImpftermin1());
		dossier.setRegistrierung(registrierung);
		Impfdossiereintrag eintrag = new Impfdossiereintrag();
		Assertions.assertNotNull(dossier.getBuchung().getImpftermin1());
		final Impftermin boosterTermin = TestdataCreationUtil.createImpftermin(dossier.getBuchung().getImpftermin1().getImpfslot().getOrtDerImpfung(),
			LocalDate.now());
		boosterTermin.setImpffolge(Impffolge.BOOSTER_IMPFUNG);
		boosterTermin.setOffsetInMinutes(10);
		eintrag.setImpfterminFromImpfterminRepo(boosterTermin);
		dossier.getImpfdossierEintraege().add(eintrag);
		terminabsagePdfGenerator = new TerminabsagePdfGenerator(registrierung, boosterTermin, null, boosterTermin.getTerminZeitfensterStartDateAndTimeString(), null);
		terminabsagePdfGenerator.generate(getFileOutputSteam(mandant.name(), filename));
	}

	@ParameterizedTest
	@EnumSource(value = Mandant.class, mode = Mode.MATCH_ALL)
	public void zertifikatStornierung(Mandant mandant) throws InvoiceGeneratorException, IOException {
		System.setProperty("vacme.mandant", mandant.name());
		final String filename = "ZertifikatStornierung";
		Zertifikat zertifikat = getZertifikat();
		zertifikatStornierungPdfGenerator = new ZertifikatStornierungPdfGenerator(registrierung, zertifikat);
		zertifikatStornierungPdfGenerator.generate(getFileOutputSteam(mandant.name(), filename));
	}

	@ParameterizedTest
	@EnumSource(value = Mandant.class, mode = Mode.MATCH_ALL)
	public void onboarding(Mandant mandant) throws InvoiceGeneratorException, IOException {
		System.setProperty("vacme.mandant", mandant.name());
		final String filename = "Onboarding";
		onboardingPdfGenerator = new OnboardingPdfGenerator(registrierung, "XXXXXXXX");
		onboardingPdfGenerator.generate(getFileOutputSteam(mandant.name(), filename));
	}

	@ParameterizedTest
	@EnumSource(value = Mandant.class, mode = Mode.MATCH_ALL)
	public void freigabeBooster(Mandant mandant) throws InvoiceGeneratorException, IOException {
		System.setProperty("vacme.mandant", mandant.name());
		final String filename = "FreigabeBooster";
		freigabeBoosterPdfGenerator = new FreigabeBoosterPdfGenerator(registrierung, LocalDate.now().minusMonths(11), KantonaleBerechtigung.KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG);
		freigabeBoosterPdfGenerator.generate(getFileOutputSteam(mandant.name(), filename));
	}

	private Impfung generateImpfung(@NonNull LocalDateTime date) {
		Impfstoff impfstoff = getModernaImpfstoff();
		final Impfung impfung = new Impfung();
		impfung.setTimestampImpfung(date);
		impfung.setLot("EJ6796");
		impfung.setImpfstoff(impfstoff);
		impfung.setMenge(BigDecimal.valueOf(5.5));
		impfung.setVerarbreichungsort(Verarbreichungsort.OBERARM);
		impfung.setVerarbreichungsseite(Verarbreichungsseite.LINKS);
		impfung.setVerarbreichungsart(Verarbreichungsart.INTRA_MUSKULAER);
		impfung.setGrundimmunisierung(true);
		return  impfung;
	}

	private Impfung generateImpfungWithTermin(@NonNull LocalDateTime dateTime, @NonNull Impffolge impffolge) {
		final Impfung impfung = generateImpfung(dateTime);
		LocalDate date = dateTime.toLocalDate();
		Impfslot slot = new Impfslot();
		slot.setOrtDerImpfung(odi);
		slot.setZeitfenster(DateTimeRange.of(date.atTime(14, 0), date.atTime(14, 5)));
		Impftermin termin = new Impftermin();
		termin.setImpffolge(impffolge);
		termin.setImpfslot(slot);
		impfung.setTermin(termin);
		return  impfung;
	}

	private Impfung generateBoosterImpfungWithTermin(@NonNull LocalDateTime dateTime, @NonNull Impffolge impffolge) {
		final Impfung impfung = generateImpfungWithTermin(dateTime, impffolge);
		impfung.setGrundimmunisierung(false);
		return  impfung;
	}

	private void createImpfdokumentation(@NonNull Impfung impfung1, @Nullable Impfung impfung2, @NonNull String bezeichnung, @NonNull String mandant, @Nullable List<Impfung> impfungen) throws FileNotFoundException, InvoiceGeneratorException {
		System.setProperty("vacme.mandant", mandant);
		final ImpfinformationDto infos =
			TestdataCreationUtil.createImpfinformationen(KrankheitIdentifier.COVID,
				registrierung,
				impfung1,
				impfung2,
				dossier,
				null,
				impfungen);
		ImpfdokumentationCovidPdfGenerator impfdokumentationPdfGenerator = new ImpfdokumentationCovidPdfGenerator(infos, KantonaleBerechtigung.KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG);
		final String filename = "Impfdokumentation_Covid_" + '_' + bezeichnung;
		impfdokumentationPdfGenerator.generate(getFileOutputSteam(mandant, filename));

		if (CollectionUtils.isNotEmpty(impfungen)) {
			ImpfdokumentationAffenpockenPdfGenerator affenpockenPdfGenerator = new ImpfdokumentationAffenpockenPdfGenerator(registrierung, impfungen, KantonaleBerechtigung.KANTONALE_IMPFKAMPAGNE);
			final String filenameAffenpocken = "Impfdokumentation_Affenpocken_" + '_' + bezeichnung;
			affenpockenPdfGenerator.generate(getFileOutputSteam(mandant, filenameAffenpocken));
		}

		if (CollectionUtils.isNotEmpty(impfungen)) {
			ImpfdokumentationFsmePdfGenerator fsmePdfGenerator = new ImpfdokumentationFsmePdfGenerator(registrierung, impfungen, KantonaleBerechtigung.LEISTUNGSERBRINGER);
			final String filenameAffenpocken = "Impfdokumentation_FSME_" + '_' + bezeichnung;
			fsmePdfGenerator.generate(getFileOutputSteam(mandant, filenameAffenpocken));
		}
	}

	private Impfstoff getModernaImpfstoff(){
		Impfstoff moderna = new Impfstoff();
		moderna.setAnzahlDosenBenoetigt(2);
		moderna.setHersteller("Moderna");
		moderna.setCode("mRNA-1273");
		moderna.setName("COVID-19 vaccine (Moderna)");
		moderna.setId(UUID.fromString("c5abc3d7-f80d-44fd-be6e-0aba4cf03643"));
		return moderna;
	}

	private void setDummyData() {
		Adresse adresse = new Adresse();
		adresse.setAdresse1("Nussbaumstrasse 21");
		adresse.setPlz("3000");
		adresse.setOrt("Bern");
		registrierung = new Registrierung();
		registrierung.setName("Muster");
		registrierung.setVorname("Tim");
		registrierung.setAdresse(adresse);
		registrierung.setGeburtsdatum(LocalDate.of(1980,1,21));
		registrierung.setRegistrierungsnummer("12B8RZ");
		registrierung.setPrioritaet(Prioritaet.A);

		dossier = new Impfdossier();
		dossier.setRegistrierung(registrierung);
		dossier.setKrankheitIdentifier(KrankheitIdentifier.COVID);
		dossier.setDossierStatus(ImpfdossierStatus.FREIGEGEBEN);

		odi = new OrtDerImpfung();
		odi.setName("Impfzentrum Bern");
		odi.setTyp(OrtDerImpfungTyp.IMPFZENTRUM);
		odi.setAdresse(adresse);
		odi.setTerminverwaltung(true);

		dossier.getBuchung().setGewuenschterOdi(odi);

		LocalDate date1 = LocalDate.of(2020, Month.FEBRUARY, 1);
		Impfslot slot1 = new Impfslot();
		slot1.setOrtDerImpfung(odi);
		slot1.setZeitfenster(DateTimeRange.of(date1.atTime(8, 15), date1.atTime(8, 20)));
		Impftermin termin1 = new Impftermin();
		termin1.setImpffolge(Impffolge.ERSTE_IMPFUNG);
		termin1.setImpfslot(slot1);
		dossier.getBuchung().setImpftermin1FromImpfterminRepo(termin1);

		LocalDate date2 = LocalDate.of(2020, Month.FEBRUARY, 22);
		Impfslot slot2 = new Impfslot();
		slot2.setOrtDerImpfung(odi);
		slot2.setZeitfenster(DateTimeRange.of(date2.atTime(14, 0), date2.atTime(14, 5)));
		Impftermin termin2 = new Impftermin();
		termin2.setImpffolge(Impffolge.ZWEITE_IMPFUNG);
		termin2.setImpfslot(slot2);
		dossier.getBuchung().setImpftermin2FromImpfterminRepo(termin2);

		boosterTerminOrNull = new Impftermin();
		boosterTerminOrNull.setImpffolge(Impffolge.BOOSTER_IMPFUNG);
		boosterTerminOrNull.setImpfslot(slot1);
	}

	private FileOutputStream getFileOutputSteam(String mandant, String filename) throws FileNotFoundException {
		return new FileOutputStream(pfad  + '/' +  mandant  + '/' + filename + ".pdf");
	}

	@ParameterizedTest
	@EnumSource(value = Mandant.class, mode = Mode.MATCH_ALL)
	public void archivierung(Mandant mandant) throws IOException {
		ImpfinformationDto impfinformationen = TestdataCreationUtil.createImpfinformationen(
			KrankheitIdentifier.COVID,
			LocalDate.now().minusMonths(6),
			null);
		final Fragebogen fragebogen = TestdataCreationUtil.createFragebogen();
		List<ZertifikatInfo> zertifikate = new ArrayList<>(TestdataCreationUtil.createZertifikateForImpfinformationen(impfinformationen));

		ArchivierungPdfGenerator generator = new ArchivierungPdfGenerator(fragebogen, impfinformationen, zertifikate);
		String title = "Archivierung_" + generator.getArchiveTitle();
		generator.createArchivierung(getFileOutputSteam(mandant.name(), title));
	}

	@ParameterizedTest
	@EnumSource(value = Mandant.class, mode = Mode.MATCH_ALL)
	public void archivierungVacmePlusBooster(Mandant mandant) throws IOException {
		ImpfinformationDto impfinformationen = TestdataCreationUtil.createImpfinformationen(
			KrankheitIdentifier.COVID,
			LocalDate.now().minusMonths(6),
			null);
		impfinformationen = TestdataCreationUtil.addBoosterImpfung(impfinformationen, LocalDate.now());
		final Fragebogen fragebogen = TestdataCreationUtil.createFragebogen();
		List<ZertifikatInfo> zertifikate = new ArrayList<>(TestdataCreationUtil.createZertifikateForImpfinformationen(impfinformationen));

		ArchivierungPdfGenerator generator = new ArchivierungPdfGenerator(fragebogen, impfinformationen, zertifikate);
		String title = "ArchivierungVacmeBooster_" + generator.getArchiveTitle();
		generator.createArchivierung(getFileOutputSteam(mandant.name(), title));
	}

	@ParameterizedTest
	@EnumSource(value = Mandant.class, mode = Mode.MATCH_ALL)
	public void archivierungExternPlusBooster(Mandant mandant) throws IOException {
		ImpfinformationDto impfinformationen = TestdataCreationUtil.createImpfinformationen(
			KrankheitIdentifier.COVID,
			null,
			LocalDate.now().minusMonths(6));
		impfinformationen = TestdataCreationUtil.addBoosterImpfung(impfinformationen, LocalDate.now());
		final Fragebogen fragebogen = TestdataCreationUtil.createFragebogen();
		List<ZertifikatInfo> zertifikate = new ArrayList<>(TestdataCreationUtil.createZertifikateForImpfinformationen(impfinformationen));

		ArchivierungPdfGenerator generator = new ArchivierungPdfGenerator(fragebogen, impfinformationen, zertifikate);
		String title = "ArchivierungExternBooster_" + generator.getArchiveTitle();
		generator.createArchivierung(getFileOutputSteam(mandant.name(), title));
	}

	private Zertifikat getZertifikat() {
		Zertifikat zertifikat = new Zertifikat();
		zertifikat.setUvci("urn:uvci:01:CH:5100334BB7A34E7CC659E3E9");
		return zertifikat;
	}


	@ParameterizedTest
	@EnumSource(value = Mandant.class, mode = Mode.MATCH_ALL)
	public void zertifikatCounterNeuerstellung(Mandant mandant) throws InvoiceGeneratorException, IOException {
		System.setProperty("vacme.mandant", mandant.name());
		final String filename = "ZertifikatCounterNeuerstellt";
		ZertifikatCounterNeuerstelltPdfGenerator generator = new ZertifikatCounterNeuerstelltPdfGenerator(registrierung);
		generator.generate(getFileOutputSteam(mandant.name(), filename));
	}

}
