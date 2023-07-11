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

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.transaction.Transactional;
import javax.ws.rs.Path;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.junit.jupiter.api.Test;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class ArchUnitResourcesTest {

	private final JavaClasses importedClasses = new ClassFileImporter().importPackages("ch.dvbern.oss.vacme");

	@Test
	public void allResourcesMustHavePathDeclarations() {
		ArchRule rule = ArchRuleDefinition
			.classes().that().haveSimpleNameEndingWith("Resource").and().arePublic()
			.and().doNotImplement(QuarkusTestResourceLifecycleManager.class) // don't check test ressources
			.should().beAnnotatedWith(Path.class)
			.because("Jede Resource soll ueber einen spezifischen Path erreichbar sein");
		rule.check(importedClasses);
	}

	@Test
	public void allResourcesMustHaveRoleDeclarations() {
		ArchRule rule = ArchRuleDefinition
			.methods().that().areDeclaredInClassesThat().areAnnotatedWith(Path.class).and().arePublic()
			.should().beAnnotatedWith(RolesAllowed.class)
			.orShould().beAnnotatedWith(PermitAll.class)
			.because("Wir haben entschieden, pro Methode die Annotationen zu machen, damit nicht aus Versehen eine heikle Methode auf einer @PermitAll "
				+ "Resource oeffentlich erreichbar ist.");
		rule.check(importedClasses);
	}
	@Test
	public void allResourcesMustSpecifyTransactionBehaviour() {
		ArchRule rule = ArchRuleDefinition
			.classes().that().haveSimpleNameEndingWith("Resource").and().arePublic()
			.and().doNotImplement(QuarkusTestResourceLifecycleManager.class) // don't check test ressources
			.should().beAnnotatedWith(Transactional.class)
			.because("We usually want a transaction");
		rule.check(importedClasses);
	}
}
