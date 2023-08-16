/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
import {Settings} from '../../shared/settings';

export namespace FachappKeycloakLoginPo {

    export function loginToFachapp(username: string) {
        const sentArgs = {user: username, pwd: Settings.passwords(username)};
        cy.origin('https://impfen-vacme-dev.dvbern.ch/', {args: sentArgs}, ({user, pwd}) => {
            cy.visit('http://localhost:4222/');
            cy.get('#username').type(user);
            cy.get('#password').type(pwd);
            cy.get('#kc-login').click();
        });
        // Account-Menu im Header muss jetzt existieren
        cy.getByTestid('headerAccount').should('exist');
        // Und den Usernamen enthalten als "title" des Links
        cy.get('[data-testid="headerAccount"]').invoke('attr', 'title').should('eq', username);
    }

    export function logout() {
        cy.getByTestid('headerLogout').first().click();
    }
}
