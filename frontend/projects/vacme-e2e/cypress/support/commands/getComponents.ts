/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import Loggable = Cypress.Loggable;
import Shadow = Cypress.Shadow;
import Timeoutable = Cypress.Timeoutable;
import Withinable = Cypress.Withinable;

declare global {
    namespace Cypress {
        interface Chainable<Subject> {

            getPersonInfoComponent<K extends keyof HTMLElementTagNameMap>(
                options?: Partial<Loggable & Timeoutable & Withinable & Shadow>,
            ): Chainable<JQuery<HTMLElementTagNameMap[K]>>;
        }
    }
}

Cypress.Commands.add('getPersonInfoComponent',
    (options?: Partial<Loggable & Timeoutable & Withinable & Shadow>) => {
        return cy.getByTestid('personInfo', options);
    });

export {};
