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

import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from '@angular/router';
import * as moment from 'moment';
import {Observable, of} from 'rxjs';
import {
    ImpffolgeTS,
    ImpfslotJaxTS,
    KrankheitIdentifierTS,
    NextFreierTerminJaxTS,
    TerminbuchungService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';

const LOG = LogFactory.createLog('resolveFreieImpfslotsWeb');

export const resolveFreieImpfslotsWeb: ResolveFn<Observable<Array<ImpfslotJaxTS> | undefined>> =
    (route: ActivatedRouteSnapshot, _: RouterStateSnapshot) => {
        const terminbuchungService = inject(TerminbuchungService);

        const id = route.params.ortDerImpfungId;
        const impffolge = route.params.impffolge as ImpffolgeTS;
        const date = route.params.date;
        const krankheit = route.params.krankheit as KrankheitIdentifierTS;

        if (id && impffolge && date && krankheit) {
            if (id) {
                const nextDateParam: NextFreierTerminJaxTS =
                    {nextDate: (DateUtil.localDateTimeToMoment(date) as moment.Moment).toDate()};
                return terminbuchungService.terminbuchungResourceGetFreieImpftermine(
                    impffolge,
                    krankheit,
                    id, nextDateParam);
            }
            return of(undefined);
        }
        LOG.debug('missing param, can not load free slots ', id, impffolge, date, krankheit);
        return of(undefined);
    };
