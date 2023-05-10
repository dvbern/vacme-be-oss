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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {ImpfdossierSummaryJaxTS, KrankheitIdentifierTS} from 'vacme-web-generated';

@Component({
    selector: 'lib-impfdossier-summary-list',
    templateUrl: './impfdossier-summary-list.component.html',
    styleUrls: ['./impfdossier-summary-list.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImpfdossierSummaryListComponent {

    @Input() impfdossierSummaryList?: ImpfdossierSummaryJaxTS[];

    @Output()
    public downloadEvent = new EventEmitter<{ registrierungsnummer: string; krankheitIdentifier: KrankheitIdentifierTS }>();

    @Output()
    public dossierSelected = new EventEmitter<ImpfdossierSummaryJaxTS>();

    triggerDownloadImpfdokumentation(eventObj: { registrierungsnummer: string; krankheitIdentifier: KrankheitIdentifierTS }): void {
        this.downloadEvent.emit(eventObj);
    }

    public selectDossier(dossierSummary: ImpfdossierSummaryJaxTS): void {
        this.dossierSelected.emit(dossierSummary);
    }
}
