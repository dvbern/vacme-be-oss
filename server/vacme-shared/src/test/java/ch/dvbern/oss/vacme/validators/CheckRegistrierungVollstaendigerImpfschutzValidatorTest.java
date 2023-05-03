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

package ch.dvbern.oss.vacme.validators;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CheckRegistrierungVollstaendigerImpfschutzValidatorTest {

	@ParameterizedTest
	@CsvSource({
		"IMPFUNG_1_DURCHGEFUEHRT,false,true,false,,true",
		"IMPFUNG_1_DURCHGEFUEHRT,true,true,false,,true",
		"IMPFUNG_1_DURCHGEFUEHRT,false,true,false,,true",
		"IMPFUNG_1_DURCHGEFUEHRT,false,false,false,,false",
		"IMPFUNG_1_DURCHGEFUEHRT,true,true,true,,false", // soll nie vorkommen aber wenn wir nur eine impfung haben aber genesen sind mit vollstImpfschutz waere das ok
		"ABGESCHLOSSEN,true,true,false,,false",
		"ABGESCHLOSSEN,true,false,false,,false",
		"ABGESCHLOSSEN,false,true,false,,true",
		"ABGESCHLOSSEN,false,false,false,,true",
		"ABGESCHLOSSEN,true,false,false,aua,true",
		"ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,true,true,false,aua,true",
		"ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,true,false,false,aua,true",
		"ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,false,false,false,aua,false",
		"ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,true,true,true,,false",
		"IMMUNISIERT,true,true,false,,false",
		"IMMUNISIERT,true,true,true,,false",
		"IMMUNISIERT,true,true,false,aua,true",
	})
	public void validationTest(
		String status,
		String vollstImpfschutz,
		String generateZert,
		String genesen,
		String verzichtGrund,
		String error
	) {
		ImpfdossierStatus dossierStatus = ImpfdossierStatus.valueOf(status);
		boolean vollstImpfschutzFlag = Boolean.parseBoolean(vollstImpfschutz);
		boolean genZertFlag = Boolean.parseBoolean(generateZert);
		boolean coronoa = Boolean.parseBoolean(genesen);
		boolean errorExpected = Boolean.parseBoolean(error);

		Registrierung registrierung = new Registrierung();
		Impfdossier dossier = new Impfdossier();
		dossier.setRegistrierung(registrierung);
		dossier.setDossierStatus(dossierStatus);

		dossier.setVollstaendigerImpfschutzTyp(approximateVollstaendigerImpfschutzTyp(vollstImpfschutzFlag, coronoa));
		dossier.getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetGrund(verzichtGrund);
		dossier.getZweiteGrundimmunisierungVerzichtet().setGenesen(coronoa);

		Impfung impfung = new Impfung();
		impfung.setGenerateZertifikat(genZertFlag);

		ImpfinformationDto impfinformationDto =
			new ImpfinformationDto(KrankheitIdentifier.COVID, registrierung, impfung, null, dossier, null);

		final boolean valid = CheckRegistrierungVollstaendigerImpfschutzValidator.isValid(impfinformationDto, impfung);

		Assertions.assertEquals(errorExpected, !valid);
	}

	@Nullable
	public static  VollstaendigerImpfschutzTyp approximateVollstaendigerImpfschutzTyp(boolean vollstaendigGeimpft, boolean genesen) {
		if (genesen) {
			return vollstaendigGeimpft ? VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME_GENESEN : null;
		}
		return vollstaendigGeimpft ? VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME : null;
	}
}
