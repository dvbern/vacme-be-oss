/*
 *
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import ch.dvbern.oss.vacme.service.wellapi.CheckIfWellDisabledInterceptor;
import ch.dvbern.oss.vacme.service.wellapi.WellApiInitalDataSenderService;
import ch.dvbern.oss.vacme.service.wellapi.WellApiService;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

public class ArchUnitWellApiDisableableTest {

	private final JavaClasses importedClasses = new ClassFileImporter().importClasses(WellApiService.class);

	@Test
	public void archUnitWellServicePublicMethodsMustBeAnnotatedWithInterceptor(){
		ArchRule rule = ArchRuleDefinition
			.methods()
			.that()
			.areDeclaredInClassesThat()
			.areAssignableTo(JavaClass.Predicates.belongToAnyOf(WellApiService.class, WellApiInitalDataSenderService.class))
			.and().arePublic()
			.should().beAnnotatedWith(CheckIfWellDisabledInterceptor.class)
			.because("Sonst werden sie nicht disabled wenn das relevante flag gesetzt ist.");

		rule.check(importedClasses);

	}
}
