/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

export namespace FachappStartseitePo {

    export function neuePersonErfassen(krankheit: string): void {
        if ('COVID' === krankheit) {
            cy.getByTestid('startpage-adhoc-COVID').first().click();
        } else {
            // Alle anderen Krankheiten beginnen im Booster-Status
            const link = '/person/new/kontrolle/booster/' + krankheit;
            cy.get(`[href="${link}"]`).click();
        }
    }

    export function searchPersonByCode(code: string) {
        cy.getInputByFormControlName('code').type(code);
        cy.clickSubmitButton();
    }
}
