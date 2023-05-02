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

import {DashboardJaxTS, RegistrierungStatusTS} from 'vacme-web-generated';

export class FreigabeEntzugUtil {

    public static lostOdi(dashboardVorher: DashboardJaxTS, dashboardNachher: DashboardJaxTS): boolean {
        const stati = [RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER];
        return this.lostStatus(dashboardVorher, dashboardNachher, stati);
    }

    public static lostNichtVerwalteterOdi(dashboardVorher: DashboardJaxTS, dashboardNachher: DashboardJaxTS): boolean {
        const hadNichtVerwalteterOdiVorher = !!dashboardVorher.nichtVerwalteterOdiSelected;
        const hasNichtVerwalteterOdiNachher = !!dashboardNachher.nichtVerwalteterOdiSelected;
        return hadNichtVerwalteterOdiVorher && !hasNichtVerwalteterOdiNachher;
    }

    public static lostTermin(dashboardVorher: DashboardJaxTS, dashboardNachher: DashboardJaxTS): boolean {
        const stati = [RegistrierungStatusTS.GEBUCHT_BOOSTER];
        return this.lostStatus(dashboardVorher, dashboardNachher, stati);
    }

    public static lostFreigabe(dashboardVorher: DashboardJaxTS, dashboardNachher: DashboardJaxTS): boolean {
        const stati = [
            RegistrierungStatusTS.FREIGEGEBEN_BOOSTER,
            RegistrierungStatusTS.GEBUCHT_BOOSTER,
            RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER,
        ];
        return this.lostStatus(dashboardVorher, dashboardNachher, stati);
    }

    private static lostStatus(
        dashboardVorher: DashboardJaxTS,
        dashboardNachher: DashboardJaxTS,
        stati: Array<RegistrierungStatusTS>,
    ): boolean {
        const hadFreigabeVorher = !!dashboardVorher.status && stati.includes(dashboardVorher.status);
        const hasFreigabeNachher = !!dashboardNachher.status && stati.includes(dashboardNachher.status);
        const isFreigegeben =  dashboardNachher.status === RegistrierungStatusTS.FREIGEGEBEN;
        return (hadFreigabeVorher && !hasFreigabeNachher) && !isFreigegeben;
    }
}
