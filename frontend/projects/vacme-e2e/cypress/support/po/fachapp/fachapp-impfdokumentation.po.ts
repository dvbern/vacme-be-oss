/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

export namespace FachappImpfdokumentationPo {

    export function fillImpfdokumentation(): void {
        cy.getRadioByFormControlName('impfungNachtragen', 0).check();
        cy.getRadioByFormControlName('selbstzahlende', 1).check();

        cy.getAutocompleteByFormControlName('odi').first().click();
        cy.getAutocompleteByFormControlName('verantwortlich').first().click();
        cy.getAutocompleteByFormControlName('durchfuehrend').first().click();

        cy.getSelectByFormControlName('impfstoff').select(2);
        cy.getInputByFormControlName('lot').type('01234567890123456789');

        cy.getCheckboxByFormControlName('kein_fieber_keine_kontraindikation').check();
        cy.getCheckboxByFormControlName('keine_besonderen_umstaende').check();
        cy.getCheckboxByFormControlName('einwilligung').check();

        cy.getRadioByFormControlName('immunsupprimiert', 0).check();
        cy.getSelectByFormControlName('verabreichung_art').select(1);
        cy.getSelectByFormControlName('verabreichung_ort').select(1);
        cy.getRadioByFormControlName('verabreichung_ort_lr', 0).check();
        cy.getInputByFormControlName('menge').type('0.9');
    }

    export function saveVaccinationForm() {
        cy.clickSubmitButton();
    }

    export function verifyVaccinationDate() {
        const formattedDate = getVaccinationDateFromText();
        cy.get('h4').contains(`am ${formattedDate}`).should('be.visible');
    }

    export function getVaccinationDateFromText() {
        const today = new Date();
        const day = today.getDate().toString().padStart(2, '0');
        const month = (today.getMonth() + 1).toString().padStart(2, '0');
        const year = today.getFullYear().toString();
        const formattedDate = `${day}.${month}.${year}`;
        return formattedDate;
    }
}
