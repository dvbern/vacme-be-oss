/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

declare global {
    namespace Cypress {
        interface Chainable<Subject> {
            clickSubmitButton<E extends Node = HTMLElement>(): Chainable<JQuery<E>>;

            clickPrimaryButton<E extends Node = HTMLElement>(): Chainable<JQuery<E>>;

            clickSecondaryButton<E extends Node = HTMLElement>(): Chainable<JQuery<E>>;

            clickGoBackButton<E extends Node = HTMLElement>(): Chainable<JQuery<E>>;

            clickGoNextButton<E extends Node = HTMLElement>(): Chainable<JQuery<E>>;
        }
    }
}

Cypress.Commands.add('clickSubmitButton',
    () => {
        // cy.get('button[type=submit]').click();
        cy.get('lib-button[buttonKind="submit"]').first().click();
    });

Cypress.Commands.add('clickPrimaryButton',
    () => {
        cy.get('lib-button[buttonKind="primary"]').click();
    });

Cypress.Commands.add('clickSecondaryButton',
    () => {
        cy.get('lib-button[buttonKind="secondary"]').click();
    });

Cypress.Commands.add('clickGoBackButton',
    () => {
        cy.get('lib-button[buttonKind="go-back"]').click();
    });

Cypress.Commands.add('clickGoNextButton',
    () => {
        cy.get('lib-button[buttonKind="go-next"]').click();
    });

export {};
