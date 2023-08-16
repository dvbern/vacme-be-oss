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

import {Component} from '@angular/core';
import {Router} from '@angular/router';
import {Observable} from 'rxjs';
import {
    KrankheitIdentifierTS,
    PersonalienSucheService,
    RegistrierungSearchResponseWithRegnummerJaxTS,
} from 'vacme-web-generated';
import {BaseDestroyableComponent} from '../../../../../vacme-web-shared';
import {NavigationService} from '../../service/navigation.service';

@Component({
    selector: 'app-registrierung-suche-page',
    templateUrl: './registrierung-uvci-suche-page.component.html',
    styleUrls: ['./registrierung-uvci-suche-page.component.scss'],
})
export class RegistrierungUvciSuchePageComponent extends BaseDestroyableComponent {

    constructor(
        private sucheService: PersonalienSucheService,
        private router: Router,
		private navigationService: NavigationService,
    ) {
        super();
    }

    getSearchFunction(): (
        geburtsdatum: Date,
        name: string,
        vorname: string,
        uvci: string,
    ) => Observable<RegistrierungSearchResponseWithRegnummerJaxTS[]> {
        return (geburtsdatum: Date, name: string, vorname: string, uvci: string):
            Observable<Array<RegistrierungSearchResponseWithRegnummerJaxTS>> =>
            this.sucheService.personalienSucheRegResourceSuchenUvci(geburtsdatum,
                name,
                uvci,
                vorname);
    }

    getSelectionFunction(): (daten: RegistrierungSearchResponseWithRegnummerJaxTS) => void {
        return (daten: RegistrierungSearchResponseWithRegnummerJaxTS): void =>
			// TODO Affenpocken: VACME-2326
			this.navigationService.navigateToDossierDetail(daten.regNummer, KrankheitIdentifierTS.COVID);
    }

}
