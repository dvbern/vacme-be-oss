/*
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

import {FachappKeycloakLoginPo} from '../support/po/fachapp/fachapp-keycloak-login.po';
import {FachappKontrollePo} from '../support/po/fachapp/fachapp-kontrolle.po';
import {FachappStartseitePo} from '../support/po/fachapp/fachapp-startseite.po';
import {Settings} from '../support/shared/settings';

describe('AdHoc Kontrolle', () => {

    it('AdHoc Kontrolle COVID', () => {
        FachappKeycloakLoginPo.loginToFachapp(Settings.userFachverantwortung());
        FachappStartseitePo.neuePersonErfassen('COVID');
        cy.getPageTitle().should('have.text', 'Erfassung zu impfende Person Covid-19');
        FachappKontrollePo.fillKontrolleAdHoc();
        FachappKontrollePo.clickKontrolliert();
    });

});
