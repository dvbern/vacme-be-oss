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
    DashboardJaxTS,
    ImpfdossierJaxTS,
    ImpfdossiersOverviewJaxTS,
    ImpffolgeTS,
    ImpfungJaxTS,
} from 'vacme-web-generated';

export class BoosterUtil {

    public static hasAnyVacmeImpfung(impfdossiersOverview: ImpfdossiersOverviewJaxTS): boolean {
        if (impfdossiersOverview.impfdossierSummaryList) {
            for (const impfdossier of impfdossiersOverview.impfdossierSummaryList) {
                if (impfdossier.letzteImpfung) {    // If any impfdossier has letzteImpfung
                    return true;
                }
            }
        }
        return false;
    }

    public static hasDashboardVacmeImpfung(dashboardJaxTS: DashboardJaxTS): boolean {
        return dashboardJaxTS.impfung1?.timestampImpfung != null
            || dashboardJaxTS.impfung2?.timestampImpfung != null
            || (!!dashboardJaxTS.impfdossier?.impfdossiereintraege?.length
                && !!dashboardJaxTS.impfdossier?.impfdossiereintraege?.filter(eintrag => !!eintrag.impfung).length);
    }

    public static getLatestVacmeImpfung(dashboardJaxTS: DashboardJaxTS): ImpfungJaxTS | undefined {
        if (dashboardJaxTS.boosterImpfungen?.length) {
            return dashboardJaxTS.boosterImpfungen[dashboardJaxTS.boosterImpfungen.length - 1];
        }
        if (dashboardJaxTS.impfung2) {
            return dashboardJaxTS.impfung2;
        }
        if (dashboardJaxTS.impfung1) {
            return dashboardJaxTS.impfung1;
        }
        return undefined;
    }

    public static getLatestVacmeImpffolge(dashboardJaxTS: DashboardJaxTS): ImpffolgeTS | undefined {
        return this.getLatestVacmeImpfung(dashboardJaxTS)?.impffolge;
    }

    public static numberOfBoosters(impfdossier: ImpfdossierJaxTS | undefined): number {
        const impfdossiereintrag = impfdossier?.impfdossiereintraege;

        if (impfdossiereintrag) {
            return impfdossiereintrag.filter(eintrag => eintrag.impfung && !eintrag.impfung.grundimmunisierung).length;
        }
        return 0;
    }
}
