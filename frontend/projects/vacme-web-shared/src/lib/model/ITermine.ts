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

import {
    ImpfdossierJaxTS,
    ImpfterminJaxTS,
    ImpfungJaxTS,
    OrtDerImpfungDisplayNameJaxTS,
    PrioritaetTS,
    RegistrierungStatusTS,
} from 'vacme-web-generated';

/**
 * Dies ist im Fall der Impfdokumentation ein DashboardJaxTS, im Fall der Kontrolle ein ImpfkontrolleJaxTS
 */
export default interface ITermineTS {

    status?: RegistrierungStatusTS;
    gewuenschterOrtDerImpfung?: OrtDerImpfungDisplayNameJaxTS;
    nichtVerwalteterOdiSelected?: boolean;
    prioritaet?: PrioritaetTS;
    termin1?: ImpfterminJaxTS;
    termin2?: ImpfterminJaxTS;
    terminNPending?: ImpfterminJaxTS;
    impfung1?: ImpfungJaxTS;
    impfung2?: ImpfungJaxTS;
    geburtsdatum?: Date;
    impfdossier?: ImpfdossierJaxTS;
}
