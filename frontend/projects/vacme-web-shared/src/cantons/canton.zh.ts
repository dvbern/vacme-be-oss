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

import {BeruflicheTaetigkeitZH} from '../lib/model';

export const canton = {
    name: 'zh',
    logo: 'assets/img/kanton-zuerich.svg',
    mobileOrtDerImpfung: false,
    mobileOrtDerImpfungAdmin: false,
    contactTracing: false,
    hasOnboarding: true,
    hasFachanwendungOnboarding: false,
    massenTermineAbsagen: true,
    beruflicheTaetigkeit: Object.values(BeruflicheTaetigkeitZH),
    additionalTranslations: true,
    showSchwangerWarnung: true,
    publicHelpURL: 'https://blog.vacme.ch/zh/',
    helpURL: 'https://blog-impfen.vacme.ch/zh/',
    odiAttributesEnabled: true,
    zweiKinderProTermin: false,
    keinKontakt: true,
    showFHIRDownload: false,
    multiKrankheitGUI: false,
    fluechtlingUeberKrankenkasse: false,
    simplifiedLebensumstaende: false,
    showQRCode: true,
    wellPartnerDomainMarker: null
};
