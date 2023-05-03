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
import {Observable, of} from 'rxjs';
import {
    DossierService,
    ImpffolgeTS,
    ImpfslotJaxTS,
    KrankheitIdentifierTS,
    NextFreierTerminJaxTS,
} from 'vacme-web-generated';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import {LogFactory} from 'vacme-web-shared';
import * as moment from 'moment';

const LOG = LogFactory.createLog('FreieImpfslotsResolverService');
@Injectable({
    providedIn: 'root',
})
export class FreieImpfslotsResolverService implements Resolve<Array<ImpfslotJaxTS> | undefined> {

    constructor(protected dossierService: DossierService) {
    }

    public resolve(route: ActivatedRouteSnapshot): Observable<Array<ImpfslotJaxTS> | undefined> {

        const id = route.params.ortDerImpfungId;
        const impffolge = route.params.impffolge as ImpffolgeTS;
        const date = route.params.date;
        const krankheit = route.params.krankheit as KrankheitIdentifierTS;

        if (id && impffolge && date && krankheit) {
            return this.loadSlots$(id, impffolge, date, krankheit);
        }
        LOG.debug('missing param, can not load free slots ', id, impffolge, date, krankheit);
        return of(undefined);
    }

    private loadSlots$(
        id: string,
        impffolge: ImpffolgeTS,
        date: string,
        krankheit: KrankheitIdentifierTS,
    ): Observable<Array<ImpfslotJaxTS> | undefined> {
        if (id) {

            const nextDateParam: NextFreierTerminJaxTS =
                {nextDate: (DateUtil.localDateTimeToMoment(date) as moment.Moment).toDate()};

            return this.dossierService.dossierResourceRegGetFreieImpftermine(
                impffolge,
                krankheit,
                id, nextDateParam);
        }

        return of(undefined);
    }
}
