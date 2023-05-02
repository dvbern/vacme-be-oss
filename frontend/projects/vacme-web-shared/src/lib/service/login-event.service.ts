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

/* eslint-disable @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match */
import {Injectable} from '@angular/core';
import {ReplaySubject, Subject} from 'rxjs';
import {TSAppEventTyp} from '../model';



@Injectable({
    providedIn: 'root',
})
export class LoginEventService {

    private _appEventStream$: Subject<TSAppEventTyp>;

    constructor() {
        // improve: da wir hier eine buffer Size von 1 haben (damit der footer das event bekommt wenn er fertig gezeichnet ist)
        // sollten wir diesen Bus eigentlich nur fuer LoginLogout Events verwenden. Das ist im Moment der Fall. Sollte das aendern muessen
        // wir den Bus wohl exklusiv so nennen
        this._appEventStream$ = new ReplaySubject<TSAppEventTyp>(1);

    }

    public broadcastEvent(appEvent: TSAppEventTyp): void {
        if (appEvent) {
            this._appEventStream$.next(appEvent);
        }

    }

    public get appEventStream$(): Subject<TSAppEventTyp> {
        return this._appEventStream$;
    }
}
