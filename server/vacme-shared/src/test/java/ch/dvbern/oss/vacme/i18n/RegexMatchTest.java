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

package ch.dvbern.oss.vacme.i18n;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;

public class RegexMatchTest {

	private final String REGEX_PATTERN_MIGRATION_ZERTIFIKAT_1 = "AEF.*|T-SPITAL.*|SPITAL_3.*";
	private final String REGEX_PATTERN_MIGRATION_ZERTIFIKAT_2 = "AEF.*|T-SPITAL.*|SPITAL_3.*|^T-HEIM-(?!177)(?!179)(?!229)(?!269)(?!315)(?!316).*";

	final String PATTERN_REGNUMMER = "^.*([A-Z0-9]{6}),.*$";
	final String PATTERN_REGNUMMER_IMPFFOLGE = "^.*([A-Z0-9]{6}).*impffolge=(ERSTE_IMPFUNG|ZWEITE_IMPFUNG).*$";

	@Test
	public void matchRegnummerInLoglineTest() {

		String input = "2021-06-24 20:44:46,539 WARN  [ch.dvb.oss.vac.ser.KorrekturService] (executor-thread-4042) "
			+ "(efd58b20-4078-4c27-8d1b-af2622eacf11) VACME-ZERTIFIKAT-REVOKE: Zertifikat muss revoked werden fuer "
			+ "Registrierung KZ4VZY, da das Datum der Impfung veraendert wurde";
		Pattern pattern = Pattern.compile(PATTERN_REGNUMMER);

		Matcher matcher = pattern.matcher(input);
		boolean matches = matcher.matches();
		Assert.assertTrue(matches);
//		System.out.println(matcher.group(1));
	}

	@Test
	public void matchFromFile() throws IOException {

		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(Objects.requireNonNull(classLoader.getResource("ZH_RegsToRevokeTest.txt")).getFile());

		List<String> strings = Files.readAllLines(Path.of(file.getPath()));

		Pattern pattern = Pattern.compile(PATTERN_REGNUMMER);

		strings.stream().filter(StringUtils::isNotBlank).forEach(input -> {
			Matcher matcher = pattern.matcher(input);
			boolean matches = matcher.matches();
			if (matches) {
				String regCode = matcher.group(1);
				Assertions.assertEquals(6, regCode.length());
//				System.out.println(regCode);
			} else {
				System.out.println(" No match in line '" + input + "'");
				Assertions.fail("Should have one match per line");
			}

		});

	}

	@Test
	public void matchRegNummerAndImpffolgeInLogLine() {
		String input = "2021-06-28 07:42:47,414 INFO  [ch.dvb.oss.vac.ser.KorrekturService] (executor-thread-2009) (3dc93f3c-f7ef-4672-9542-71e3eddc58db) VACME-KORREKTUR: Fuer die Registrierung QDNXXA wurde eine Korrektur des Impfstoffs vorgenommen ImpfungKorrekturJax(impffolge=ERSTE_IMPFUNG, impfstoff=c5abc3d7-f80d-44fd-be6e-0aba4cf03643, lot=3002186, menge=0.5) durch irgendjemand@vacme.ch";

		Pattern pattern = Pattern.compile(PATTERN_REGNUMMER_IMPFFOLGE);

		Matcher matcher = pattern.matcher(input);
		boolean matches = matcher.matches();
		Assert.assertTrue(matches);
		final String regNummer = matcher.group(1);
		final String impffolge = matcher.group(2);
		System.out.println(regNummer + ", " + impffolge);
	}

	@Test
	public void matchRegNummerAndImpffolgeFromFile() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File fileRevocations = new File(Objects.requireNonNull(classLoader.getResource("ZH_RegsToCreateRevokedTest.txt")).getFile());
		List<String> linesRevocations = Files.readAllLines(Path.of(fileRevocations.getPath()));
		Pattern patternRevocations = Pattern.compile(PATTERN_REGNUMMER);
		Set<String> regsThatWhereRevocated = new HashSet<>();
		linesRevocations.stream().filter(StringUtils::isNotBlank).forEach(input -> {
			Matcher matcher = patternRevocations.matcher(input);
			boolean matches = matcher.matches();
			if (matches) {
				final String regNummer = matcher.group(1);
				Assertions.assertEquals(6, regNummer.length());
				regsThatWhereRevocated.add(regNummer);
//				System.out.println("Revoked: " + regNummer);
			} else {
				System.out.println(" No match in line '" + input + "'");
				Assertions.fail("Should have one match per line");
			}
		});


		File fileKorrekturen = new File(Objects.requireNonNull(classLoader.getResource("ZH_RegsToCreateTest.txt")).getFile());
		List<String> linesKorrekturen = Files.readAllLines(Path.of(fileKorrekturen.getPath()));
		Pattern pattern = Pattern.compile(PATTERN_REGNUMMER_IMPFFOLGE);
		linesKorrekturen.stream().filter(StringUtils::isNotBlank).forEach(input -> {
			Matcher matcher = pattern.matcher(input);
			boolean matches = matcher.matches();
			if (matches) {
				final String regNummer = matcher.group(1);
				final String impffolge = matcher.group(2);
				Assertions.assertEquals(6, regNummer.length());
				if (regsThatWhereRevocated.contains(regNummer)) {
					System.out.println(regNummer + ", " + impffolge);
				} else {
//					System.out.println("was not revoked: " + regNummer);
				}
			} else {
				System.out.println(" No match in line '" + input + "'");
				Assertions.fail("Should have one match per line");
			}
		});
	}


	@Test
	public void matchRegex1ForZertifizierungMassenimport() {
		Assert.assertTrue(externalIdMatchesRegex1("AEF"));
		Assert.assertTrue(externalIdMatchesRegex1("T-SPITAL"));
		Assert.assertTrue(externalIdMatchesRegex1("SPITAL_3"));
		Assert.assertTrue(externalIdMatchesRegex1("AEF_und_noch_viel_mehr"));
		Assert.assertTrue(externalIdMatchesRegex1("T-SPITAL_und_noch_viel_mehr"));
		Assert.assertTrue(externalIdMatchesRegex1("SPITAL_3_und_noch_viel_mehr"));

		Assert.assertFalse(externalIdMatchesRegex1("irgendetwas_AEF"));
		Assert.assertFalse(externalIdMatchesRegex1("irgendetwas_T-SPITAL"));
		Assert.assertFalse(externalIdMatchesRegex1("irgendetwas_SPITAL_3"));
		Assert.assertFalse(externalIdMatchesRegex1("AE"));
		Assert.assertFalse(externalIdMatchesRegex1("T-SPITA"));
		Assert.assertFalse(externalIdMatchesRegex1("SPITAL_"));
		Assert.assertFalse(externalIdMatchesRegex1("irgendetwas_AEF"));
		Assert.assertFalse(externalIdMatchesRegex1("irgendetwas_T-SPITAL_und_noch_viel_mehr"));
		Assert.assertFalse(externalIdMatchesRegex1("irgendetwas_SPITAL_3_und_noch_viel_mehr"));
	}

	private boolean externalIdMatchesRegex1(String externalID) {
		Pattern pattern = Pattern.compile(REGEX_PATTERN_MIGRATION_ZERTIFIKAT_1);
		Matcher matcher = pattern.matcher(externalID);
		return matcher.matches();
	}

	@Test
	public void matchRegex2ForZertifizierungMassenimport() {
		Assert.assertTrue(externalIdMatchesRegex2("AEF"));
		Assert.assertTrue(externalIdMatchesRegex2("T-SPITAL"));
		Assert.assertTrue(externalIdMatchesRegex2("SPITAL_3"));
		Assert.assertTrue(externalIdMatchesRegex2("AEF_und_noch_viel_mehr"));
		Assert.assertTrue(externalIdMatchesRegex2("T-SPITAL_und_noch_viel_mehr"));
		Assert.assertTrue(externalIdMatchesRegex2("SPITAL_3_und_noch_viel_mehr"));

		Assert.assertFalse(externalIdMatchesRegex2("irgendetwas_AEF"));
		Assert.assertFalse(externalIdMatchesRegex2("irgendetwas_T-SPITAL"));
		Assert.assertFalse(externalIdMatchesRegex2("irgendetwas_SPITAL_3"));
		Assert.assertFalse(externalIdMatchesRegex2("AE"));
		Assert.assertFalse(externalIdMatchesRegex2("T-SPITA"));
		Assert.assertFalse(externalIdMatchesRegex2("SPITAL_"));
		Assert.assertFalse(externalIdMatchesRegex2("irgendetwas_AEF"));
		Assert.assertFalse(externalIdMatchesRegex2("irgendetwas_T-SPITAL_und_noch_viel_mehr"));
		Assert.assertFalse(externalIdMatchesRegex2("irgendetwas_SPITAL_3_und_noch_viel_mehr"));

		Assert.assertTrue(externalIdMatchesRegex2("T-HEIM-999-blabla177"));
		Assert.assertTrue(externalIdMatchesRegex2("T-HEIM-999blabla177"));
		Assert.assertTrue(externalIdMatchesRegex2("T-HEIM-178-blabla177"));

		Assert.assertFalse(externalIdMatchesRegex2("T-HEIM177-blabla177"));
		Assert.assertFalse(externalIdMatchesRegex2("T-HEIM-177blabla177"));
		Assert.assertFalse(externalIdMatchesRegex2("T-HEIM999-blabla177"));
		Assert.assertFalse(externalIdMatchesRegex2("T-HEIM-177-blabla177"));
		Assert.assertFalse(externalIdMatchesRegex2("T-HEIM-179-blabla177"));
		Assert.assertFalse(externalIdMatchesRegex2("T-HEIM-229-blabla177"));
		Assert.assertFalse(externalIdMatchesRegex2("T-HEIM-269-blabla177"));
		Assert.assertFalse(externalIdMatchesRegex2("T-HEIM-315-blabla177"));
		Assert.assertFalse(externalIdMatchesRegex2("T-HEIM-316-blabla177"));
	}

	private boolean externalIdMatchesRegex2(String externalID) {
		Pattern pattern = Pattern.compile(REGEX_PATTERN_MIGRATION_ZERTIFIKAT_2);
		Matcher matcher = pattern.matcher(externalID);
		return matcher.matches();
	}
}


