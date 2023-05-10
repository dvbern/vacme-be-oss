/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {DashboardJaxTS, ImpfdossierSummaryJaxTS} from 'vacme-web-generated';
import TSPersonFolgetermin from '../model/TSPersonFolgetermin';
import {fromImpfdossierStatus} from './registrierung-status-utils';

export function fromDashboard(dashboard: DashboardJaxTS): TSPersonFolgetermin {
    const value = new TSPersonFolgetermin(
        dashboard.status,
        dashboard.impfdossier?.impfschutzJax?.freigegebenNaechsteImpfungAb,
        !!dashboard.terminNPending,
        dashboard.krankheitIdentifier
    );
    return value;
}

export function fromImpfdossierSummary(dossierSummary: ImpfdossierSummaryJaxTS): TSPersonFolgetermin {
    const value = new TSPersonFolgetermin(
        fromImpfdossierStatus(dossierSummary.status),
        dossierSummary.freigabeNaechsteImpfung,
        !!dossierSummary.nextTermin,
        dossierSummary.krankheit.identifier);
    return value;
}
