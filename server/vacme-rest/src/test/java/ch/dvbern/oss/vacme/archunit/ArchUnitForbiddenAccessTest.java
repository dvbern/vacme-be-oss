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

package ch.dvbern.oss.vacme.archunit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.util.ImpfdossierEntityListener;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.resource.GeimpftResource;
import ch.dvbern.oss.vacme.service.ApplicationHealthCorrectionService;
import ch.dvbern.oss.vacme.service.DataMigrationService;
import ch.dvbern.oss.vacme.service.KorrekturService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfdossierBuilder;
import ch.dvbern.oss.vacme.service.wellapi.WellApiService;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.properties.HasName.Predicates;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaAccess.Predicates.target;
import static com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class ArchUnitForbiddenAccessTest {

	// Keine Testklassen (.withImportOption(new DoNotIncludeTests()) scheint nicht zu funktionieren)
	private final JavaClasses importedClasses = new ClassFileImporter()
		.withImportOption(new DoNotIncludeClassesNamedTest())
		.importPackages("ch.dvbern.oss.vacme");

	@Test
	public void impfterminGebuchtMustOnlyBeCalledFromImpfterminRepo() {
		Class<?>[] whitelisted = {
			ImpfterminRepo.class,
			KorrekturService.class,
			DataMigrationService.class,
			ImpfdossierEntityListener.class,
			ApplicationHealthCorrectionService.class,
			TestdataCreationUtil.class,
			ImpfdossierBuilder.class
		};
		final ArchRule setGebuchtFromImpfterminRepo =
			noClasses()
				.that()
				.doNotBelongToAnyOf(whitelisted)
				.should()
				.callMethod(Impftermin.class, "setGebuchtFromImpfterminRepo", boolean.class)
				.because("soll nur von ImpfterminRepo aufgerufen werden");
		setGebuchtFromImpfterminRepo.check(importedClasses);
	}

	@Test
	public void archUnitWellServiceAsyncNotCalledExternally() {
		ArchRule noAsyncCalled =
			noClasses()
				.that()
				.doNotBelongToAnyOf(WellApiService.class)
				.should()
				.callMethodWhere(target(Predicates.nameEndingWith("Async"))
					.and(target(owner(JavaClass.Predicates.assignableTo(WellApiService.class))))
				)
				.because("Die nicht async-methoden machen die nötigen mappings bereits");
		noAsyncCalled.check(importedClasses);

	}

	@Test
	public void setVollstaendigerImpfschutzFlagShouldNotBeCalled() {
		Assertions.assertFalse(importedClasses.isEmpty());
		final ArchRule setVollstImpfschutzFlagNotCalledRule =
			noClasses()
				.should()
				.callMethod(Registrierung.class, "setVollstaendigerImpfschutz", Boolean.class)
				.because("soll nicht aufgerufen werden. Stattdessen setVollstaendigerImpfschutzFlagAndTyp verwenden");
		setVollstImpfschutzFlagNotCalledRule.check(importedClasses);
	}

	@Test
	public void registrierungSetImpfterminMustOnlyBeCalledFromImpfterminRepo() {
		Class<?>[] whitelisted = {
			ImpfterminRepo.class,
			KorrekturService.class,
			DataMigrationService.class,
			TestdataCreationUtil.class,
			ApplicationHealthCorrectionService.class
		};
		final ArchRule setImpftermin1 =
			noClasses()
				.that()
				.doNotBelongToAnyOf(whitelisted)
				.should()
				.callMethod(Registrierung.class, "setImpftermin1FromImpfterminRepo", Impftermin.class)
				.because("soll nur von ImpfterminRepo aufgerufen werden");
		setImpftermin1.check(importedClasses);

		final ArchRule setImpftermin2 =
			noClasses()
				.that()
				.doNotBelongToAnyOf(whitelisted)
				.should()
				.callMethod(Registrierung.class, "setImpftermin2FromImpfterminRepo", Impftermin.class)
				.because("soll nur von ImpfterminRepo aufgerufen werden");
		setImpftermin2.check(importedClasses);
	}

	@Test
	public void registrierungFlagsShouldOnlyBeCalledFromHelperMethods() {
		List<Class<?>> whitelisted = new ArrayList<Class<?>>();
		whitelisted.add(Impfdossier.class);
		whitelisted.add(TestdataCreationUtil.class);

		// Ausgenommen ist der Status, welcher auch unabhaengig von den anderen Flags gesetzt werden kann.

		final ArchRule setAbgeschlossenZeit =
			createRuleForRegistrierungsFlags("setAbgeschlossenZeit", whitelisted, LocalDateTime.class);
		setAbgeschlossenZeit.check(importedClasses);

		final ArchRule setVollstaendigerImpfschutz =
			createRuleForRegistrierungsFlags("setVollstaendigerImpfschutz", whitelisted, boolean.class);
		setVollstaendigerImpfschutz.check(importedClasses);

		List<Class<?>> setZweiteImpfungVerzichtetGrundWhitelist = new ArrayList<Class<?>>(whitelisted);
		setZweiteImpfungVerzichtetGrundWhitelist.add(GeimpftResource.class);
		final ArchRule setZweiteImpfungVerzichtetGrund = createRuleForRegistrierungsFlags(
			"setZweiteImpfungVerzichtetGrund",
			setZweiteImpfungVerzichtetGrundWhitelist,
			String.class);
		setZweiteImpfungVerzichtetGrund.check(importedClasses);

		final ArchRule setZweiteImpfungVerzichtetZeit =
			createRuleForRegistrierungsFlags("setZweiteImpfungVerzichtetZeit", whitelisted, LocalDateTime.class);
		setZweiteImpfungVerzichtetZeit.check(importedClasses);
	}

	@NonNull
	private ArchRule createRuleForRegistrierungsFlags(
		@NonNull String methodeName,
		@NonNull List<Class<?>> whitelisted,
		@NonNull Class<?>... parameterTypes) {
		final Class[] whitelistedArray = whitelisted.toArray(new Class[0]);
		return noClasses()
			.that()
			.doNotBelongToAnyOf(whitelistedArray)
			.should()
			.callMethod(Registrierung.class, methodeName, parameterTypes)
			.because(methodeName
				+ " should only be called from helper methods (setStatusToAbgeschlossen / "
				+ "setStatusToAbgeschlossenOhneZweiteImpfung / "
				+ "setStatusToAutomatischAbgeschlossen / setStatusToNichtAbgeschlossenStatus) to ensure that all flags"
				+ " are set matching each other");
	}

}
