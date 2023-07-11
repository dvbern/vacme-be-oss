/* eslint-disable @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match */
/*
 * Copyright (c) 2018 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschuetzt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulaessig. Dies gilt
 * insbesondere fuer Vervielfaeltigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht uebergeben ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 *
 */

import {Injectable} from '@angular/core';
import {ReplaySubject, Subject} from 'rxjs';
import {AppEvent} from '../model';



@Injectable({
    providedIn: 'root',
})
export class ApplicationEventService {

    private _appEventStream$: Subject<AppEvent>;

    constructor() {
        this._appEventStream$ = new ReplaySubject<AppEvent>(1);

    }

    public broadcastEvent(appEvent: AppEvent): void {
        if (appEvent) {
            this._appEventStream$.next(appEvent);
        }

    }

    public get appEventStream$(): Subject<AppEvent> {
        return this._appEventStream$;
    }
}
