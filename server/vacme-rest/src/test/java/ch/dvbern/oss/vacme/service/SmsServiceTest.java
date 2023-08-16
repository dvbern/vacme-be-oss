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
import java.util.Objects;
import java.util.UUID;

import ch.dvbern.oss.vacme.dto.SmsSettingsDTO;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.registration.Sprache;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.util.Gsm0338;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.mockito.Mockito.never;

class SmsServiceTest {

	public static final String EMPFAENGER_MOBILE_NUMMER = "077 123 45 67";
	private SmsService smsService;
	private final SmsSettingsDTO smsSettingsDTO = TestdataCreationUtil.dummySmsSettingsDTO();

	@BeforeEach
	void setUp() {
		System.setProperty("vacme.mandant", "BE");
		smsService = Mockito.spy(SmsService.class);
		VacmeSettingsService vacmeSettingsService = Mockito.mock(VacmeSettingsService.class);
		Mockito.when(vacmeSettingsService.getSmsSettingsDTO()).thenReturn(smsSettingsDTO);
		smsService.vacmeSettingsService = vacmeSettingsService;
	}

	@ParameterizedTest
	@CsvSource({
		// originalString,  transformedString,  isGmsOnly
		"ćĆğščžőđĐ’–çÀâÂë, cCgsczodD'-cAaAe, false", // bekannte Replacements
		"ÁĂÀÂáâąãǎČĆćçčĐĎđËÊÈēěễęěėëêğİïıîÍíķĽŁľłňńÓÕÒọőōôóõõŘřŠŞšśșşÚûúűūűýŽŻŻžźżż–—’‘`´ˈ“”„‟, "
			+ "AAAAaaaaaCCcccDDdEEEeeeeeeeegIiiiiikLLllnnOOOoooooooRrSSssssUuuuuuyZZZzzzz--'''''\"\"\"\","
			+ "false", // alle bekannten Replacements (ausser die Leerschlaege, die gehen in der csv source nicht)
		"Đinđić, Dindic, false",
		"èéâÈÉÀćğőđĆ, èéaEÉAcgodC, false",
		"Werwölfe, Werwölfe, true",
		"Peyvənd əladır, Peyv?nd ?ladir, false",
		"Вакцинација је одлична, ??????????? ?? ???????, false",
		"ការចាក់វ៉ាក់សាំងគឺអស្ចារ្យណាស់។, ???????????????????????????????, false",
		// GMS basic (das Komma mussten wir hier rausnehmen, das geht in der csv source nicht!)
		"@ΔSP0¡P¿p£_!1AQaq$Φ\"2BRbr¥Γ#3CScsèΛ¤4DTdtéΩ%5EUeuùΠ&6FVfvìΨ'7GWgwòΣ(8HXhxÇΘ)9IYiyLFΞ*:JZjzØESC+;KÄkäøÆ<LÖlöCRæ-=MÑmñÅß.>NÜnüåÉ/?O§oà, "
			+ "@ΔSP0¡P¿p£_!1AQaq$Φ\"2BRbr¥Γ#3CScsèΛ¤4DTdtéΩ%5EUeuùΠ&6FVfvìΨ'7GWgwòΣ(8HXhxÇΘ)9IYiyLFΞ*:JZjzØESC+;KÄkäøÆ<LÖlöCRæ-=MÑmñÅß.>NÜnüåÉ/?O§oà, "
			+ "true",
		"\f|^{}\\[~]€, \f|^{}\\[~]€, true" // GSM extension
	})
	void removeUnsupportedCharacters(String original, String expected, boolean isGmsOnly) {
		Assertions.assertEquals(isGmsOnly, Gsm0338.isValidGsm0338(original));
		Assertions.assertEquals(expected, Gsm0338.transformToGsm0338String(original));
	}

	@Test
	void testSpecialUnsupportedCharacters() {
		Assertions.assertEquals("     ", Gsm0338.transformToGsm0338String("\u200B\u202A\u202C\u200F\u00A0"));
	}

	@Test
	void testTerminbestaetigungSMS() {
		LocalDate latestImpfungDate = LocalDate.of(2021, 6, 30);
		LocalDate boosterTerminDate = LocalDate.of(2022, 11, 9);
		OrtDerImpfung odi = TestdataCreationUtil.createOrtDerImpfung();

		ImpfinformationDto infos = TestdataCreationUtil.createImpfinformationen(
			KrankheitIdentifier.AFFENPOCKEN,
			latestImpfungDate,
			null);
		infos.getRegistrierung().setName("Tester");
		infos.getRegistrierung().setVorname("Tim");
		infos.getRegistrierung().setRegistrierungsnummer("AABBCC");
		infos.getRegistrierung().setSprache(Sprache.DE);

		// Booster Termin sms_terminbestaetigung_booster_text
		infos.getImpfdossier().setDossierStatus(ImpfdossierStatus.GEBUCHT_BOOSTER);

		Impftermin impftermin = TestdataCreationUtil.createImpftermin(odi, boosterTerminDate);

		String expectedMessageBooster =
			"Ihr Termin für die nächste Affenpocken Impfung: 09.11.2022 00:00\n"
				+ odiAdressShort(odi) + '\n'
				+ "Ihr Code: AABBCC\n"
				+ "https://be.vacme.ch";

		final Benutzer empfaenger = getVacmeBenutzer();
		smsService.sendTerminbestaetigungSMS(infos.getImpfdossier(), impftermin, empfaenger);
		Mockito.verify(smsService).sendSMSToRegistrierung(EMPFAENGER_MOBILE_NUMMER, expectedMessageBooster, infos.getRegistrierung(), smsSettingsDTO);

		// Basis Termine sms_terminbestaetigung_text
		infos.getImpfdossier().setDossierStatus(ImpfdossierStatus.GEBUCHT);
		Objects.requireNonNull(infos.getImpfdossier().getBuchung().getImpftermin1());
		String expectedMessageBasisTermin =
			"1. Termin 01.01.2021 00:00\n"
				+  "2. Termin 01.01.2021 00:00\n"
				+ odiAdressShort(infos.getImpfdossier().getBuchung().getImpftermin1().getImpfslot().getOrtDerImpfung()) + '\n'
				+ "Ihr Code: AABBCC\n"
				+ "https://be.vacme.ch";

		smsService.sendTerminbestaetigungSMS(infos.getImpfdossier(), impftermin, empfaenger);
		Mockito.verify(smsService).sendSMSToRegistrierung(EMPFAENGER_MOBILE_NUMMER, expectedMessageBasisTermin, infos.getRegistrierung(), smsSettingsDTO);

		infos = TestdataCreationUtil.createImpfinformationen(
			KrankheitIdentifier.AFFENPOCKEN,
			null,
			null);
		infos.getRegistrierung().setRegistrierungsnummer("AABBCC");
		infos.getRegistrierung().setSprache(Sprache.DE);

		// Ohne terminverwaltung sms_terminbestaetigung_text_ohne_termin
		infos.getImpfdossier().getBuchung().setGewuenschterOdi(odi);
		infos.getImpfdossier().setKrankheitIdentifier(KrankheitIdentifier.AFFENPOCKEN); // Weakness of the test data cration util
		String expectedMessageOhneTerminbuchung =
			"Bitte vereinbaren Sie persönlich einen Impftermin bei:\n"
				+ odiAdressShort(odi) + " für Ihre Affenpocken Impfung\n"
				+ "Ihr Code: AABBCC\n"
				+ "https://be.vacme.ch";

		smsService.sendTerminbestaetigungSMS(infos.getImpfdossier(), impftermin, empfaenger);
		Mockito.verify(smsService).sendSMSToRegistrierung(EMPFAENGER_MOBILE_NUMMER, expectedMessageOhneTerminbuchung, infos.getRegistrierung(), smsSettingsDTO);

		// Ohne terminverwaltung mobiles odi sms_terminbestaetigung_text_mobiler_odi
		Objects.requireNonNull(infos.getImpfdossier().getBuchung().getGewuenschterOdi());
		infos.getImpfdossier().getBuchung().getGewuenschterOdi().setMobilerOrtDerImpfung(true);
		String expectedMessageOhneTerminbuchungMobilesOdi =
			"Sie haben sich für die Affenpocken Impfung durch folgendes mobiles Impfteam entschieden:\n"
				+ odiAdressShort(odi) +'\n'
				+ "Ihr Code: AABBCC\n"
				+ "https://be.vacme.ch";

		smsService.sendTerminbestaetigungSMS(infos.getImpfdossier(), impftermin, empfaenger);
		Mockito.verify(smsService).sendSMSToRegistrierung(EMPFAENGER_MOBILE_NUMMER, expectedMessageOhneTerminbuchungMobilesOdi, infos.getRegistrierung(), smsSettingsDTO);

		// Nicht verwalteter odi sms_terminbestaetigung_text_nicht_verwalteter_odi
		infos.getImpfdossier().getBuchung().setGewuenschterOdi(null);
		infos.getImpfdossier().getBuchung().setNichtVerwalteterOdiSelected(true);

		String expectedMessageNichtVerwalteterOdi =
			"Bitte vereinbaren Sie persönlich einen Impftermin für Ihre Affenpocken Impfung\n"
				+ "Ihr Code: AABBCC\n"
				+ "https://be.vacme.ch";

		smsService.sendTerminbestaetigungSMS(infos.getImpfdossier(), impftermin, empfaenger);
		Mockito.verify(smsService).sendSMSToRegistrierung(EMPFAENGER_MOBILE_NUMMER, expectedMessageNichtVerwalteterOdi, infos.getRegistrierung(), smsSettingsDTO);
	}

	private String odiAdressShort(OrtDerImpfung odi) {
		return odi.getName() + ", " + odi.getAdresse().getAdresse1() + ", " + odi.getAdresse().getOrt();
	}

	@Test
	void testOdiRegistrierungSms() {
		ImpfinformationDto infos = TestdataCreationUtil.createImpfinformationen(
			KrankheitIdentifier.AFFENPOCKEN,
			null,
			null);
		infos.getRegistrierung().setName("Tester");
		infos.getRegistrierung().setVorname("Tim");
		infos.getRegistrierung().setRegistrierungsnummer("AABBCC");
		infos.getRegistrierung().setSprache(Sprache.DE);

		String expectedMessageAffenpocken =
			"Sie wurden mit folgendem Code für das VacMe Impfportal registriert:"
				+ "\nAABBCC";

		smsService.sendOdiRegistrierungsSMS(infos.getRegistrierung(), EMPFAENGER_MOBILE_NUMMER);

		Mockito.verify(smsService).sendSMSToRegistrierung(EMPFAENGER_MOBILE_NUMMER, expectedMessageAffenpocken, infos.getRegistrierung(), smsSettingsDTO);
	}

	@Test
	void testFreigabeBoosterSms() {
		ImpfinformationDto infos = TestdataCreationUtil.createImpfinformationen(
			KrankheitIdentifier.AFFENPOCKEN,
			null,
			null);
		infos.getRegistrierung().setName("Tester");
		infos.getRegistrierung().setVorname("Tim");
		infos.getRegistrierung().setRegistrierungsnummer("AABBCC");
		infos.getRegistrierung().setSprache(Sprache.DE);
		smsService.sendFreigabeBoosterSMS(infos, "testusername", getVacmeBenutzer());

		String expectedMessage =
			 "Guten Tag Tim Tester\n"
			+ "Zur Gewährleistung des optimalen Impfschutzes wird Ihnen eine Affenpocken Impfung empfohlen. Unter https://be.vacme.ch "
			+ "können Sie einen Termin für die Affenpocken Impfung buchen.\n"
			+ "Ihr VacMe-Code: AABBCC\n"
			+ "Ihr Benutzername: testusername";

		Mockito.verify(smsService).sendSMSToRegistrierung(EMPFAENGER_MOBILE_NUMMER, expectedMessage, infos.getRegistrierung(), smsSettingsDTO);
	}

	@Test
	void testFreigabeBoosterSmsWithPreviousImpfung() {
		LocalDate latestImpfungDate = LocalDate.of(2021, 6, 30);
		LocalDate latestExterneImfpung = LocalDate.of(2021, 1, 30);

		ImpfinformationDto infos = TestdataCreationUtil.createImpfinformationen(
			KrankheitIdentifier.COVID,
			latestImpfungDate,
			latestExterneImfpung);
		infos.getRegistrierung().setName("Tester");
		infos.getRegistrierung().setVorname("Tim");
		infos.getRegistrierung().setRegistrierungsnummer("AABBCC");
		infos.getRegistrierung().setSprache(Sprache.DE);
		smsService.sendFreigabeBoosterSMS(infos, "testusername", getVacmeBenutzer());

		String expectedMessage =
			"Guten Tag Tim Tester\n"
				+ "Sie wurden zuletzt am 30.06.2021 gegen Covid-19 geimpft.\n"
				+ "Zur Gewährleistung des optimalen Impfschutzes wird Ihnen eine Impfung empfohlen. Unter https://be.vacme.ch "
				+ "können Sie einen Termin für die Covid-19 Impfung buchen.\n"
				+ "Ihr VacMe-Code: AABBCC\n"
				+ "Ihr Benutzername: testusername";

		Mockito.verify(smsService).sendSMSToRegistrierung(EMPFAENGER_MOBILE_NUMMER, expectedMessage, infos.getRegistrierung(), smsSettingsDTO);

		Objects.requireNonNull(infos.getExternesZertifikat()).setLetzteImpfungDate(LocalDate.of(2021, 7, 1));
		smsService.sendFreigabeBoosterSMS(infos, null, getVacmeBenutzer());
		String expectedMessage2 =
			"Guten Tag Tim Tester\n"
				+ "Sie wurden zuletzt am 01.07.2021 gegen Covid-19 geimpft.\n"
				+ "Zur Gewährleistung des optimalen Impfschutzes wird Ihnen eine Impfung empfohlen. Unter https://be.vacme.ch "
				+ "können Sie einen Termin für die Covid-19 Impfung buchen.\n"
				+ "Ihr VacMe-Code: AABBCC";

		Mockito.verify(smsService).sendSMSToRegistrierung(EMPFAENGER_MOBILE_NUMMER, expectedMessage2, infos.getRegistrierung(), smsSettingsDTO);
	}

	@Test
	void testFreigabeBossterSmsFR() {
		LocalDate latestImpfungDate = LocalDate.of(2021, 6, 30);
		LocalDate latestExterneImfpung = LocalDate.of(2021, 1, 30);

		ImpfinformationDto infos = TestdataCreationUtil.createImpfinformationen(
			KrankheitIdentifier.COVID,
			latestImpfungDate,
			latestExterneImfpung);
		infos.getRegistrierung().setName("Tester");
		infos.getRegistrierung().setVorname("Tim");
		infos.getRegistrierung().setRegistrierungsnummer("AABBCC");
		smsService.sendFreigabeBoosterSMS(infos, "testusername", getVacmeBenutzer());

		infos.getRegistrierung().setSprache(Sprache.FR);
		Objects.requireNonNull(infos.getExternesZertifikat()).setLetzteImpfungDate(LocalDate.of(2021, 7, 1));
		smsService.sendFreigabeBoosterSMS(infos, null, getVacmeBenutzer());
		String expectedMessageFR =
			"Bonjour Tim Tester,\n"
				+ "Le 01.07.2021 vous avez reçu votre dernière dose de vaccin contre Covid-19. Pour bénéficier "
				+ "d’une protection vaccinale complète, nous vous recommandons de vous faire administrer. "
				+ "Vous pouvez fixer un rendez-vous sur https://be.vacme.ch.\n"
				+ "Votre code d’accès à VacMe : AABBCC";

		Mockito.verify(smsService).sendSMSToRegistrierung(
			EMPFAENGER_MOBILE_NUMMER,
			expectedMessageFR,
			infos.getRegistrierung(),
			smsSettingsDTO);
	}

	@Test
	void testTerminbestaetigungFsmeForWell() {
		LocalDate latestImpfungDate = LocalDate.of(2021, 6, 30);
		LocalDate boosterTerminDate = LocalDate.of(2022, 11, 9);
		OrtDerImpfung odi = TestdataCreationUtil.createOrtDerImpfung();

		ImpfinformationDto infos = TestdataCreationUtil.createImpfinformationen(
			KrankheitIdentifier.FSME,
			latestImpfungDate,
			null);
		infos.getRegistrierung().setName("Tester");
		infos.getRegistrierung().setVorname("Tim");
		infos.getRegistrierung().setRegistrierungsnummer("AABBCC");
		infos.getRegistrierung().setSprache(Sprache.DE);

		// Booster Termin sms_terminbestaetigung_booster_text
		infos.getImpfdossier().setDossierStatus(ImpfdossierStatus.GEBUCHT_BOOSTER);

		Impftermin impftermin = TestdataCreationUtil.createImpftermin(odi, boosterTerminDate);

		String expectedMessageBooster =
			"Ihr Termin für die nächste FSME Impfung: 09.11.2022 00:00\n"
				+ odiAdressShort(odi) + '\n'
				+ "Ihr Code: AABBCC\n"
				+ "https://be.vacme.ch";

		final Benutzer empfaenger = getWellBenutzer();
		smsService.sendTerminbestaetigungSMS(infos.getImpfdossier(), impftermin, empfaenger);
		Mockito.verify(smsService, never()).sendSMSToRegistrierung(
			EMPFAENGER_MOBILE_NUMMER,
			expectedMessageBooster,
			infos.getRegistrierung(),
			smsSettingsDTO);

		// Basis Termine sms_terminbestaetigung_text
		infos.getImpfdossier().setDossierStatus(ImpfdossierStatus.GEBUCHT);
		Objects.requireNonNull(infos.getImpfdossier().getBuchung().getImpftermin1());
		String expectedMessageBasisTermin =
			"1. Termin 01.01.2021 00:00\n"
				+  "2. Termin 01.01.2021 00:00\n"
				+ odiAdressShort(infos.getImpfdossier().getBuchung().getImpftermin1().getImpfslot().getOrtDerImpfung()) + '\n'
				+ "Ihr Code: AABBCC\n"
				+ "https://be.vacme.ch";

		smsService.sendTerminbestaetigungSMS(infos.getImpfdossier(), impftermin, empfaenger);
		Mockito.verify(smsService, never()).sendSMSToRegistrierung(
			EMPFAENGER_MOBILE_NUMMER,
			expectedMessageBasisTermin,
			infos.getRegistrierung(),
			smsSettingsDTO);

		infos = TestdataCreationUtil.createImpfinformationen(
			KrankheitIdentifier.FSME,
			null,
			null);
		infos.getRegistrierung().setRegistrierungsnummer("AABBCC");
		infos.getRegistrierung().setSprache(Sprache.DE);

		// Ohne terminverwaltung sms_terminbestaetigung_text_ohne_termin
		infos.getImpfdossier().getBuchung().setGewuenschterOdi(odi);
		infos.getImpfdossier().setKrankheitIdentifier(KrankheitIdentifier.FSME); // Weakness of the test data cration util
		String expectedMessageOhneTerminbuchung =
			"Bitte vereinbaren Sie persönlich einen Impftermin bei:\n"
				+ odiAdressShort(odi) + " für Ihre FSME Impfung\n"
				+ "Ihr Code: AABBCC\n"
				+ "https://be.vacme.ch";

		smsService.sendTerminbestaetigungSMS(infos.getImpfdossier(), impftermin, empfaenger);
		Mockito.verify(smsService, never()).sendSMSToRegistrierung(
			EMPFAENGER_MOBILE_NUMMER,
			expectedMessageOhneTerminbuchung,
			infos.getRegistrierung(),
			smsSettingsDTO);

		// Ohne terminverwaltung mobiles odi sms_terminbestaetigung_text_mobiler_odi
		Objects.requireNonNull(infos.getImpfdossier().getBuchung().getGewuenschterOdi());
		infos.getImpfdossier().getBuchung().getGewuenschterOdi().setMobilerOrtDerImpfung(true);
		String expectedMessageOhneTerminbuchungMobilesOdi =
			"Sie haben sich für die FSME Impfung durch folgendes mobiles Impfteam entschieden:\n"
				+ odiAdressShort(odi) +'\n'
				+ "Ihr Code: AABBCC\n"
				+ "https://be.vacme.ch";

		smsService.sendTerminbestaetigungSMS(infos.getImpfdossier(), impftermin, empfaenger);
		Mockito.verify(smsService, never()).sendSMSToRegistrierung(
			EMPFAENGER_MOBILE_NUMMER,
			expectedMessageOhneTerminbuchungMobilesOdi,
			infos.getRegistrierung(),
			smsSettingsDTO);

		// Nicht verwalteter odi sms_terminbestaetigung_text_nicht_verwalteter_odi
		infos.getImpfdossier().getBuchung().setGewuenschterOdi(null);
		infos.getImpfdossier().getBuchung().setNichtVerwalteterOdiSelected(true);

		String expectedMessageNichtVerwalteterOdi =
			"Bitte vereinbaren Sie persönlich einen Impftermin für Ihre FSME Impfung\n"
				+ "Ihr Code: AABBCC\n"
				+ "https://be.vacme.ch";

		smsService.sendTerminbestaetigungSMS(infos.getImpfdossier(), impftermin, empfaenger);
		Mockito.verify(smsService, never()).sendSMSToRegistrierung(
			EMPFAENGER_MOBILE_NUMMER,
			expectedMessageNichtVerwalteterOdi,
			infos.getRegistrierung(),
			smsSettingsDTO);
	}

	@Test
	void testFreigabeBoosterSmsFsmeForWell() {
		ImpfinformationDto infos = TestdataCreationUtil.createImpfinformationen(
			KrankheitIdentifier.FSME,
			null,
			null);
		infos.getRegistrierung().setName("Tester");
		infos.getRegistrierung().setVorname("Tim");
		infos.getRegistrierung().setRegistrierungsnummer("AABBCC");
		infos.getRegistrierung().setSprache(Sprache.DE);
		smsService.sendFreigabeBoosterSMS(infos, "testusername", getWellBenutzer());

		String expectedMessage =
			"Guten Tag Tim Tester\n"
				+ "Zur Gewährleistung des optimalen Impfschutzes wird Ihnen eine FSME Impfung empfohlen. Unter https://be.vacme.ch "
				+ "können Sie einen Termin für die FSME Impfung buchen.\n"
				+ "Ihr VacMe-Code: AABBCC\n"
				+ "Ihr Benutzername: testusername";

		Mockito.verify(smsService, never()).sendSMSToRegistrierung(
			EMPFAENGER_MOBILE_NUMMER,
			expectedMessage,
			infos.getRegistrierung(),
			smsSettingsDTO);
	}

	@Test
	void testFreigabeBoosterSmsWithPreviousImpfungFsmeForWell() {
		LocalDate latestImpfungDate = LocalDate.of(2021, 6, 30);
		LocalDate latestExterneImfpung = LocalDate.of(2021, 1, 30);

		ImpfinformationDto infos = TestdataCreationUtil.createImpfinformationen(
			KrankheitIdentifier.FSME,
			latestImpfungDate,
			latestExterneImfpung);
		infos.getRegistrierung().setName("Tester");
		infos.getRegistrierung().setVorname("Tim");
		infos.getRegistrierung().setRegistrierungsnummer("AABBCC");
		infos.getRegistrierung().setSprache(Sprache.DE);
		smsService.sendFreigabeBoosterSMS(infos, "testusername", getVacmeBenutzer());

		String expectedMessage =
			"Guten Tag Tim Tester\n"
				+ "Sie wurden zuletzt am 30.06.2021 gegen FSME geimpft.\n"
				+ "Zur Gewährleistung des optimalen Impfschutzes wird Ihnen eine Impfung empfohlen. Unter https://be.vacme.ch "
				+ "können Sie einen Termin für die FSME Impfung buchen.\n"
				+ "Ihr VacMe-Code: AABBCC\n"
				+ "Ihr Benutzername: testusername";

		Mockito.verify(smsService, never()).sendSMSToRegistrierung(
			EMPFAENGER_MOBILE_NUMMER,
			expectedMessage,
			infos.getRegistrierung(),
			smsSettingsDTO);

		Objects.requireNonNull(infos.getExternesZertifikat()).setLetzteImpfungDate(LocalDate.of(2021, 7, 1));
		smsService.sendFreigabeBoosterSMS(infos, null, getWellBenutzer());
		String expectedMessage2 =
			"Guten Tag Tim Tester\n"
				+ "Sie wurden zuletzt am 01.07.2021 gegen FSME geimpft.\n"
				+ "Zur Gewährleistung des optimalen Impfschutzes wird Ihnen eine Impfung empfohlen. Unter https://be.vacme.ch "
				+ "können Sie einen Termin für die FSME Impfung buchen.\n"
				+ "Ihr VacMe-Code: AABBCC";

		Mockito.verify(smsService, never()).sendSMSToRegistrierung(
			EMPFAENGER_MOBILE_NUMMER,
			expectedMessage2,
			infos.getRegistrierung(),
			smsSettingsDTO);
	}

	@NonNull
	private Benutzer getVacmeBenutzer() {
		final Benutzer benutzer = TestdataCreationUtil.createBenutzer("Tester", "Tim", null);
		benutzer.setMobiltelefon(EMPFAENGER_MOBILE_NUMMER);
		return benutzer;
	}

	@NonNull
	private Benutzer getWellBenutzer() {
		final Benutzer benutzer = getVacmeBenutzer();
		benutzer.setWellId(UUID.randomUUID().toString());
		return benutzer;
	}
}
