/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

export namespace FachappKontrollePo {

    export function fillKontrolleAdHoc(): void {
        cy.getPageTitle().should('have.text', 'Erfassung zu impfende Person Covid-19');
        cy.getSelectByFormControlName('geschlecht').select(2);
        cy.getInputByFormControlName('name').type('Tester');
        cy.getInputByFormControlName('vorname').type('Fritz');
        cy.getInputByFormControlName('adresse1').type('Bahnhofstrasse 10');
        cy.getInputByFormControlName('plz').type('3012');
        cy.getInputByFormControlName('ort').type('Bern');
        cy.getInputByFormControlName('mail').type('fritz.tester@mailbucket.dvbern.ch');
        cy.getInputByFormControlName('telefon').type('031 123 12 12');
        cy.getRadioByFormControlName('selbstzahlende', 1).check();
        cy.getSelectByFormControlName('krankenkasse').select('EDA / CD');
        cy.getInputByFormControlName('geburtsdatum').type('01.01.2000');
        cy.getTextareaByFormControlName('bemerkung').type('Dies ist ein Test');
        cy.getSelectByFormControlName('chronischeKrankheiten').select(1);
        cy.getSelectByFormControlName('lebensumstaende').select(1);
        cy.getSelectByFormControlName('beruflicheTaetigkeit').select(1);

        cy.getCheckboxByFormControlName('abgleichElektronischerImpfausweis').check();
        cy.getCheckboxByFormControlName('contactTracing').check();

        cy.getAmpelByFormControlName('externGeimpft', 1).check();
    }

    export function clickKontrolliert(): void {
        cy.clickSubmitButton();
    }

    export function clickSpeichern(): void {
        cy.clickSecondaryButton();
    }

    export function clickZurImpfung(): void {
        cy.clickPrimaryButton();
    }

    export function clickZurueck(): void {
        cy.clickGoBackButton();
    }
}
