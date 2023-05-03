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

import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {
    DashboardJaxTS,
    DossierService,
    PersonalienSucheService,
    RegistrierungSearchResponseWithRegnummerJaxTS,
} from 'vacme-web-generated';
import {BaseDestroyableComponent} from '../../../../../vacme-web-shared';
import TenantUtil from '../../../../../vacme-web-shared/src/lib/util/TenantUtil';
import {NavigationService} from '../../service/navigation.service';

@Component({
    selector: 'app-odi-personalien-uvci-suche-page',
    templateUrl: './odi-personalien-uvci-suche-page.component.html',
    styleUrls: ['./odi-personalien-uvci-suche-page.component.scss'],
})
export class OdiPersonalienUVCISuchePageComponent extends BaseDestroyableComponent implements OnInit {

    constructor(
        private dossierService: DossierService,
        private navigationService: NavigationService,
        private personalienSucheService: PersonalienSucheService,
    ) {
        super();
    }

    ngOnInit(): void {
    }

    getSelectionFunction(): (daten: any) => void {
        return (daten: any): void => this.suchen(daten.regNummer);
    }

    getSearchFunctionUVCI(): (
        geburtsdatum: Date,
        name: string,
        vorname: string,
        uvci: string,
    ) => Observable<RegistrierungSearchResponseWithRegnummerJaxTS[]> {
        // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
        return (geburtsdatum: Date, name: string, vorname: string, uvci: string) =>
            this.personalienSucheService.personalienSucheResourceSuchenUvci(
                geburtsdatum,
                name,
                uvci,
                vorname);
    }

    public suchen(code: string): void {
        this.dossierService.dossierResourceGetDashboardRegistrierung(code).subscribe(
            (res: DashboardJaxTS) => {
                this.navigationService.navigate(res);
            },
            () => {
                this.navigationService.notFoundResult();
            },
        );
    }

    public showUVCISearch(): boolean {
        return TenantUtil.ZURICH;
    }
}
