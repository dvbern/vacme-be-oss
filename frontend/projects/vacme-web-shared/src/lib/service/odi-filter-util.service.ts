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

import {Injectable} from '@angular/core';
import {isEqual} from 'lodash';
import {DashboardJaxTS, OrtDerImpfungTypTS} from 'vacme-web-generated';
import {
    OrtDerImpfungDisplayNameExtendedJaxTS,
} from '../../../../vacme-web-generated/src/lib/model/ort-der-impfung-display-name-extended-jax';
import {TSOdiSortTyp} from '../model';
import IOdiFilterOptions from '../model/IOdiFilterOptions';
import {isErsteImpfungDoneAndZweitePending} from '../util/registrierung-status-utils';
import {SortUtil} from '../util/SortUtil';
import {TerminfindungService} from './terminfindung.service';

@Injectable({
    providedIn: 'root',
})
export class OdiFilterUtilService {

    public static defaultFilterOptions = {
        hauspraxis: true,
        apotheke: true,
        impfzentrum: true,
        spital: true,
        sortTyp: TSOdiSortTyp.ALPHABETISCH,
    };

    constructor(
        public terminfindungService: TerminfindungService,
    ) {
    }

    public isDefaultFilter(filterOptions: IOdiFilterOptions): boolean {
        return isEqual(filterOptions, OdiFilterUtilService.defaultFilterOptions);
    }

    public filterOdis(
        filterOptions: IOdiFilterOptions | undefined,
        odis: OrtDerImpfungDisplayNameExtendedJaxTS[],
        dashboard: DashboardJaxTS | undefined,
    ): OrtDerImpfungDisplayNameExtendedJaxTS[] {
        if (filterOptions === undefined) {
            return odis;
        }
        return odis.filter(odi => {
            if (dashboard?.selbstzahler && odi.impfungGegenBezahlung === false) {
                return false;
            }
            return true;
        }).filter(odi => {
            if (filterOptions.spital && odi.typ === OrtDerImpfungTypTS.SPITAL) {
                return true;
            }
            if (filterOptions.impfzentrum && odi.typ === OrtDerImpfungTypTS.IMPFZENTRUM) {
                return true;
            }
            if (filterOptions.hauspraxis && odi.typ === OrtDerImpfungTypTS.HAUSARZT) {
                return true;
            }
            if (filterOptions.apotheke && odi.typ === OrtDerImpfungTypTS.APOTHEKE) {
                return true;
            }
            return false;
        }).sort((a: OrtDerImpfungDisplayNameExtendedJaxTS, b: OrtDerImpfungDisplayNameExtendedJaxTS) => {
            if (filterOptions.sortTyp === TSOdiSortTyp.ALPHABETISCH) {
                return SortUtil.handleCompareStandard(a.name, b.name);
            } else if (filterOptions.sortTyp === TSOdiSortTyp.TERMIN) {
                if (this.terminfindungService.isAlreadyGrundimmunisiert()) {
                    return SortUtil.handleCompareStandard(a.nextTerminNDate, b.nextTerminNDate);
                } else if (isErsteImpfungDoneAndZweitePending(dashboard?.status)) {
                    return SortUtil.handleCompareStandard(a.nextTermin2Date, b.nextTermin2Date);
                } else {
                    return SortUtil.handleCompareStandard(a.nextTermin1Date, b.nextTermin1Date);
                }
            } else if (filterOptions.sortTyp === TSOdiSortTyp.DISTANZ) {
                return SortUtil.handleCompareStandard(a.distanceToReg, b.distanceToReg);
            }

            return 0;
        });
    }

}
