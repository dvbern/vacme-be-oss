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
import {Subject} from 'rxjs';
import TSExceptionReport from '../model/TSExceptionReport';
import {TSErrorType} from '../model/TSErrorType';
import {TSErrorLevel} from '../model/TSErrorLevel';


@Injectable({
    providedIn: 'root',
})
export class ErrorMessageService {

    private _messageEventStream$: Subject<TSExceptionReport>;

    constructor() {
        this._messageEventStream$ = new Subject<TSExceptionReport>();
    }

    public addMesageAsError(msgKey: string): void {
        const error = new TSExceptionReport({
            type: TSErrorType.INTERNAL,
            severity: TSErrorLevel.SEVERE,
            msgKey,
        });
        this.addDvbError(error);
    }

    public addDvbError(dvbError: TSExceptionReport): void {
        if (dvbError && dvbError.isValid()) {
            this._messageEventStream$.next(dvbError);
        }
    }

    public get messageEventStream$(): Subject<TSExceptionReport> {
        return this._messageEventStream$;
    }

    public handleErrors(dvbErrors: Array<TSExceptionReport>): void {
        if (!dvbErrors || dvbErrors.length === 0) {
            return;
        }
        for (const err of dvbErrors) {
            this.addDvbError(err);
        }
    }
}
