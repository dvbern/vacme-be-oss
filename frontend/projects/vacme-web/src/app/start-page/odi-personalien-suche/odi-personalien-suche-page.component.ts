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
    PersonalienJaxTS,
    PersonalienSucheService,
    RegistrierungSearchResponseWithRegnummerJaxTS,
} from 'vacme-web-generated';
import {BaseDestroyableComponent} from '../../../../../vacme-web-shared';
import {NavigationService} from '../../service/navigation.service';

@Component({
    selector: 'app-odi-personalien-suche-page',
    templateUrl: './odi-personalien-suche-page.component.html',
    styleUrls: ['./odi-personalien-suche-page.component.scss'],
})
export class OdiPersonalienSuchePageComponent extends BaseDestroyableComponent implements OnInit {

    constructor(
        private dossierService: DossierService,
        private navigationService: NavigationService,
        private personalienSucheService: PersonalienSucheService,
    ) {
        super();
    }

    ngOnInit(): void {
    }

    getSearchFunction(): (geburtsdatum: Date, name: string, vorname: string) => Observable<PersonalienJaxTS[]> {
        return (
            geburtsdatum: Date,
            name: string,
            vorname: string,
        ): Observable<Array<RegistrierungSearchResponseWithRegnummerJaxTS>> =>
            this.personalienSucheService.personalienSucheResourceSuchen(geburtsdatum,
                name,
                vorname);
    }

    getSelectionFunction(): (daten: any) => void {
        return (daten: any): void => this.suchen(daten.regNummer);
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
}
