/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
import {SampleSelectors} from '../selectors/sample.selectors';
import Loggable = Cypress.Loggable;
import Shadow = Cypress.Shadow;
import Timeoutable = Cypress.Timeoutable;
import Withinable = Cypress.Withinable;

declare global {
    namespace Cypress {
        interface Chainable<Subject> {
            getPageTitle<K extends keyof HTMLElementTagNameMap>(): Chainable<JQuery<HTMLElementTagNameMap[K]>>;

            getInputByFormControlName<E extends Node = HTMLElement>(
                formcontrolname: string,
                options?: Partial<Loggable & Timeoutable & Withinable & Shadow>,
            ): Chainable<JQuery<E>>;

            getSelectByFormControlName<E extends Node = HTMLElement>(
                formcontrolname: string,
                options?: Partial<Loggable & Timeoutable & Withinable & Shadow>,
            ): Chainable<JQuery<E>>;

            getRadioByFormControlName<E extends Node = HTMLElement>(
                formcontrolname: string,
                indexStartingAt0: number,
                options?: Partial<Loggable & Timeoutable & Withinable & Shadow>,
            ): Chainable<JQuery<E>>;

            getCheckboxByFormControlName<E extends Node = HTMLElement>(
                formcontrolname: string,
                options?: Partial<Loggable & Timeoutable & Withinable & Shadow>,
            ): Chainable<JQuery<E>>;

            getTextareaByFormControlName<E extends Node = HTMLElement>(
                formcontrolname: string,
                options?: Partial<Loggable & Timeoutable & Withinable & Shadow>,
            ): Chainable<JQuery<E>>;

            getAmpelByFormControlName<E extends Node = HTMLElement>(
                formcontrolname: string,
                indexStartingAt0: number,
                options?: Partial<Loggable & Timeoutable & Withinable & Shadow>,
            ): Chainable<JQuery<E>>;

            getAutocompleteByFormControlName<E extends Node = HTMLElement>(
                formcontrolname: string,
                options?: Partial<Loggable & Timeoutable & Withinable & Shadow>,
            ): Chainable<JQuery<E>>;
        }
    }
}

Cypress.Commands.add('getPageTitle', () => {
    cy.get(SampleSelectors.PAGE_TITLE).first();
});

Cypress.Commands.add('getInputByFormControlName',
    (formcontrolname: string, options?: Partial<Loggable & Timeoutable & Withinable & Shadow>) => {
        cy.get(`lib-input-text[formcontrolname="${formcontrolname}"] > div > input`)
    });

Cypress.Commands.add('getSelectByFormControlName',
    (formcontrolname: string, options?: Partial<Loggable & Timeoutable & Withinable & Shadow>) => {
        cy.get(`lib-input-select[formcontrolname="${formcontrolname}"]`).click();
        cy.get(`lib-input-select[formcontrolname="${formcontrolname}"]`)
            .find('select');
    });

Cypress.Commands.add('getRadioByFormControlName',
    (
        formcontrolname: string,
        indexStartingAt0: number,
        options?: Partial<Loggable & Timeoutable & Withinable & Shadow>,
    ) => {
        cy.get(`lib-input-radio[formcontrolname="${formcontrolname}"] > div > div > label`)
            .eq(indexStartingAt0)
            .find('input');
    });

Cypress.Commands.add('getCheckboxByFormControlName',
    (formcontrolname: string, options?: Partial<Loggable & Timeoutable & Withinable & Shadow>) => {
        cy.get(`lib-input-checkbox[formcontrolname="${formcontrolname}"] > div > div > input`);
    });

Cypress.Commands.add('getTextareaByFormControlName',
    (formcontrolname: string, options?: Partial<Loggable & Timeoutable & Withinable & Shadow>) => {
        cy.get(`lib-input-textarea[formcontrolname="${formcontrolname}"] > div > textarea`)
    });

Cypress.Commands.add('getAmpelByFormControlName',
    (
        formcontrolname: string,
        indexStartingAt0: number,
        options?: Partial<Loggable & Timeoutable & Withinable & Shadow>,
    ) => {
        cy.get(`lib-form-control-ampel[formcontrolname="${formcontrolname}"] > div > div > label`)
            .eq(indexStartingAt0)
            .find('input');
    });

Cypress.Commands.add('getAutocompleteByFormControlName',
    (formcontrolname: string, options?: Partial<Loggable & Timeoutable & Withinable & Shadow>) => {
        cy.get(`lib-input-typeahead-form[formControlName="${formcontrolname}"]`).click();
        cy.get(`lib-input-typeahead-form[formControlName="${formcontrolname}"] > div > div > ngb-typeahead-window > button`);
    });

export {};
