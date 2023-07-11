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

package ch.dvbern.oss.vacme.jax.korrektur;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ImpfungDatumKorrekturJaxTest {

	@CsvSource({
		/*
		expectCert, expectRevoke, correctedFolge, vollstaendig,    status,          oldDate,          newDate,           genesenDate
		 */
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 01.01.2020 12:00," ,
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 01.01.2020 13:00," ,
		"true  , true , ERSTE_IMPFUNG , true  , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 02.01.2020 13:00," ,
		"true  , true , ERSTE_IMPFUNG , true  , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 02.01.2020 12:00 , 01.01.2020 13:00," ,

		"false , false, ERSTE_IMPFUNG , false , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 01.01.2020 12:00," ,
		"false , false, ERSTE_IMPFUNG , false , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 01.01.2020 13:00," ,
		"false , false, ERSTE_IMPFUNG , false , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 02.01.2020 13:00," ,
		"false , false, ERSTE_IMPFUNG , false , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 02.01.2020 12:00 , 01.01.2020 13:00," ,

		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN                     , 01.01.2020 12:00 , 01.01.2020 12:00," ,
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN                     , 01.01.2020 12:00 , 01.01.2020 13:00," ,
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN                     , 01.01.2020 12:00 , 02.01.2020 13:00," ,
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN                     , 02.01.2020 12:00 , 01.01.2020 13:00," ,

		"false , false, ZWEITE_IMPFUNG , true  , ABGESCHLOSSEN                    , 01.01.2020 12:00 , 01.01.2020 12:00," ,
		"false , false, ZWEITE_IMPFUNG , true  , ABGESCHLOSSEN                    , 01.01.2020 12:00 , 01.01.2020 13:00," ,
		"true  , true , ZWEITE_IMPFUNG , true  , ABGESCHLOSSEN                    , 01.01.2020 12:00 , 02.01.2020 13:00," ,
		"true  , true , ZWEITE_IMPFUNG , true  , ABGESCHLOSSEN                    , 02.01.2020 12:00 , 01.01.2020 13:00," ,
// gleicher Block mit PCR Test vor Impfung (17 - 32)
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 01.01.2020 12:00, 31.12.2019" ,
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 01.01.2020 13:00, 31.12.2019" ,
		"true  , true , ERSTE_IMPFUNG , true  , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 02.01.2020 13:00, 31.12.2019" ,
		"true  , true , ERSTE_IMPFUNG , true  , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 02.01.2020 12:00 , 01.01.2020 13:00, 31.12.2019" ,

		"false , false, ERSTE_IMPFUNG , false , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 01.01.2020 12:00, 31.12.2019" ,
		"false , false, ERSTE_IMPFUNG , false , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 01.01.2020 13:00, 31.12.2019" ,
		"false , false, ERSTE_IMPFUNG , false , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 02.01.2020 13:00, 31.12.2019" ,
		"false , false, ERSTE_IMPFUNG , false , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 02.01.2020 12:00 , 01.01.2020 13:00, 31.12.2019" ,

		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN                     , 01.01.2020 12:00 , 01.01.2020 12:00, 31.12.2019" ,
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN                     , 01.01.2020 12:00 , 01.01.2020 13:00, 31.12.2019" ,
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN                     , 01.01.2020 12:00 , 02.01.2020 13:00, 31.12.2019" ,
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN                     , 02.01.2020 12:00 , 01.01.2020 13:00, 31.12.2019" ,

		"false , false, ZWEITE_IMPFUNG , true  , ABGESCHLOSSEN                    , 01.01.2020 12:00 , 01.01.2020 12:00, 31.12.2019" ,
		"false , false, ZWEITE_IMPFUNG , true  , ABGESCHLOSSEN                    , 01.01.2020 12:00 , 01.01.2020 13:00, 31.12.2019" ,
		"true  , true , ZWEITE_IMPFUNG , true  , ABGESCHLOSSEN                    , 01.01.2020 12:00 , 02.01.2020 13:00, 31.12.2019" ,
		"true  , true , ZWEITE_IMPFUNG , true  , ABGESCHLOSSEN                    , 02.01.2020 12:00 , 01.01.2020 13:00, 31.12.2019" ,
// gleicher Test mit PCR Test nach Impfung (33 -48)
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 01.01.2020 12:00, 28.02.2020" ,
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 01.01.2020 13:00, 28.02.2020" ,
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 02.01.2020 13:00, 28.02.2020" , // kein zert da test nachher
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 02.01.2020 12:00 , 01.01.2020 13:00, 28.02.2020" , // kein zert da test nachher

		"false , false, ERSTE_IMPFUNG , false , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 01.01.2020 12:00, 28.02.2020" ,
		"false , false, ERSTE_IMPFUNG , false , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 01.01.2020 13:00, 28.02.2020" ,
		"false , false, ERSTE_IMPFUNG , false , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 02.01.2020 13:00, 28.02.2020" ,
		"false , false, ERSTE_IMPFUNG , false , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 02.01.2020 12:00 , 01.01.2020 13:00, 28.02.2020" ,

		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN                     , 01.01.2020 12:00 , 01.01.2020 12:00, 28.02.2020" ,
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN                     , 01.01.2020 12:00 , 01.01.2020 13:00, 28.02.2020" ,
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN                     , 01.01.2020 12:00 , 02.01.2020 13:00, 28.02.2020" ,
		"false , false, ERSTE_IMPFUNG , true  , ABGESCHLOSSEN                     , 02.01.2020 12:00 , 01.01.2020 13:00, 28.02.2020" ,

		"false , false, ZWEITE_IMPFUNG , true  , ABGESCHLOSSEN                    , 01.01.2020 12:00 , 01.01.2020 12:00, 28.02.2020" ,
		"false , false, ZWEITE_IMPFUNG , true  , ABGESCHLOSSEN                    , 01.01.2020 12:00 , 01.01.2020 13:00, 28.02.2020" ,
		"true  , true , ZWEITE_IMPFUNG , true  , ABGESCHLOSSEN                    , 01.01.2020 12:00 , 02.01.2020 13:00, 28.02.2020" ,
		"true  , true , ZWEITE_IMPFUNG , true  , ABGESCHLOSSEN                    , 02.01.2020 12:00 , 01.01.2020 13:00, 28.02.2020" ,
// neu: vorher PCR Test vor der Impfung, nachher PCR Test nach der Impfung -> revoke
		"false , true , ERSTE_IMPFUNG , true  , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 05.05.2020 12:00 , 01.01.2020 13:00, 28.02.2020" ,
// neu: vorher PCR Test nache der Impfung, nachher PCR Test vor der Impfung -> createZert
		"true  , true , ERSTE_IMPFUNG , true  , ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG , 01.01.2020 12:00 , 05.05.2020 13:00, 28.02.2020" ,
	})
	@ParameterizedTest
	void testDateChangeLeadsToNewCert(
		boolean expectCertCreation,
		boolean expectCertRevoke,
		Impffolge correctedFolge,
		boolean vollstaendigGeimpft,
		ImpfdossierStatus status,
		String oldDateString,
		String newDateString,
		String genesenTestDatumString) {

		LocalDateTime oldDate = parseDateTime(oldDateString);
		LocalDateTime newDate = parseDateTime(newDateString);
		LocalDate genesenTestDatum = parseDate(genesenTestDatumString);

		ImpfungDatumKorrekturJax korrekturJax = new ImpfungDatumKorrekturJax(correctedFolge, newDate, null, KrankheitIdentifier.COVID);

		Registrierung reg = new Registrierung();
		Impfdossier impfdossier = new Impfdossier();
		impfdossier.setRegistrierung(reg);
		impfdossier.setVollstaendigerImpfschutzTyp(TestdataCreationUtil
			.guessVollstaendigerImpfschutzTyp(vollstaendigGeimpft,status));
		impfdossier.setDossierStatus(status);
		impfdossier.getZweiteGrundimmunisierungVerzichtet().setPositivGetestetDatum(genesenTestDatum);

		Impfung impfung1 = new Impfung();
		if (correctedFolge == Impffolge.ERSTE_IMPFUNG) {
			impfung1.setTimestampImpfung(oldDate);
		}
		Impfung impfung2 = null;
		if (ImpfdossierStatus.ABGESCHLOSSEN == status) {
			impfung2 = new Impfung();
			if (correctedFolge == Impffolge.ZWEITE_IMPFUNG) {
				impfung2.setTimestampImpfung(oldDate);
			}
		}

		if (vollstaendigGeimpft && status == ImpfdossierStatus.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG) {
			impfdossier.getZweiteGrundimmunisierungVerzichtet().setGenesen(true);
		}

		ImpfinformationDto impfinformationDto = new ImpfinformationDto(KrankheitIdentifier.COVID, reg, impfung1, impfung2, impfdossier, null);
		boolean createNewCert = korrekturJax.needsNewZertifikat(impfinformationDto, oldDate);
		boolean needToRevoke = korrekturJax.needToRevoke(impfinformationDto, oldDate);

		Assertions.assertEquals(expectCertCreation, createNewCert, "should " + (expectCertCreation ? "" : " not ") + " create new cert");
		Assertions.assertEquals(expectCertRevoke, needToRevoke, "should " + (expectCertRevoke ? "" : " not ") + " revoke cert");
	}

	private LocalDateTime parseDateTime(String dateString) {

		DateTimeFormatter formatter = DateUtil.DEFAULT_DATE_TIME_FORMAT.apply(Locale.GERMANY);
		return LocalDateTime.parse(dateString, formatter);

	}
	@Nullable
	private LocalDate parseDate(@Nullable String dateString) {
		if (dateString == null) {
			return null;
		}

		DateTimeFormatter formatter = DateUtil.DEFAULT_DATE_FORMAT.apply(Locale.GERMANY);
		return LocalDate.parse(dateString, formatter);

	}



}
