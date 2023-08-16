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

import {BeruflicheTaetigkeitBE} from '../lib/model';

export const canton = {
    name: 'be',
    logo: 'img/Kanton-Bern.svg',
    mobileOrtDerImpfung: true,
    mobileOrtDerImpfungAdmin: true,
    contactTracing: true,
    hasOnboarding: false,
    hasFachanwendungOnboarding: false,
    massenTermineAbsagen: false,
    beruflicheTaetigkeit: Object.values(BeruflicheTaetigkeitBE),
    additionalTranslations: false,
    showSchwangerWarnung: false,
    publicHelpURL: 'https://blog.vacme.ch/be/',
    helpURL: 'https://blog-impfen.vacme.ch/be/',
    odiAttributesEnabled: false,
    zweiKinderProTermin: true,
    keinKontakt: false,
    showFHIRDownload: false,
    multiKrankheitGUI: false,
    fluechtlingUeberKrankenkasse: false,
    simplifiedLebensumstaende: false,
    showQRCode: false,
    wellPartnerDomainMarker: 'well'
};
