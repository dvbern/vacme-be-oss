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
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {Observable} from 'rxjs';
import {ImpfstoffJaxTS, StammdatenService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';

const LOG = LogFactory.createLog('ImpfstoffZugelassenTagesstatistikResolverService');

@Injectable({
    providedIn: 'root',
})
/**
 * Holt die Liste der Impfstoffe, die zugelassen sind fuer die Tagesstatistik
 */
export class ImpfstoffZugelassenTagesstatistikResolverService implements Resolve<Array<ImpfstoffJaxTS>> {

    constructor(protected stammdatenService: StammdatenService) {
    }

    public resolve(route: ActivatedRouteSnapshot): Observable<Array<ImpfstoffJaxTS>> {
        return this.stammdatenService.stammdatenResourceGetAllZugelasseneImpfstoffeThatSupportTagesstatistik();
    }
}
