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
import {Router} from '@angular/router';
import {Observable} from 'rxjs';
import {KorrekturService, PersonalienJaxTS} from 'vacme-web-generated';
import {BaseDestroyableComponent} from '../../../../../../vacme-web-shared';

@Component({
    selector: 'app-daten-korrektur-personalien-suche-page',
    templateUrl: './daten-korrektur-personalien-suche-page.component.html',
    styleUrls: ['./daten-korrektur-personalien-suche-page.component.scss'],
})
export class DatenKorrekturPersonalienSuchePageComponent extends BaseDestroyableComponent implements OnInit {

    constructor(
        private korrekturService: KorrekturService,
        private router: Router,
    ) {
        super();
    }

    ngOnInit(): void {

    }

    getSearchFunction(): (geburtsdatum: Date, name: string, vorname: string) => Observable<PersonalienJaxTS[]> {
        return (geburtsdatum: Date, name: string, vorname: string): Observable<Array<PersonalienJaxTS>> =>
            this.korrekturService.korrekturResourceSuchen(geburtsdatum, name, vorname);
    }

    getSelectionFunction(): (daten: any) => void {
        return (daten: any): Promise<boolean> => this.router.navigate(
            ['admin', 'datenkorrektur'],
            {queryParams: {registrierungNummer: daten.registrierungNummer}},
        );
    }
}
