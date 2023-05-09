/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {formatDate} from '@angular/common';
import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {LogFactory} from 'vacme-web-shared';
import TSPersonFolgetermin from '../../../../../vacme-web-shared/src/lib/model/TSPersonFolgetermin';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import {isAnyStatusOfBooster} from '../../../../../vacme-web-shared/src/lib/util/registrierung-status-utils';

const LOG = LogFactory.createLog('PersonFolgeterminComponent');

@Component({
    selector: 'app-person-folgetermin',
    templateUrl: './person-folgetermin.component.html',
    styleUrls: ['./person-folgetermin.component.scss'],
})
export class PersonFolgeterminComponent {

    @Input() public data!: TSPersonFolgetermin;
    @Output() public termineUmbuchenCalled = new EventEmitter<VoidFunction>();

    constructor(
        private translateService: TranslateService,
    ) {
    }

    public showNextTerminBuchen(): boolean {
        if (!isAnyStatusOfBooster(this.data.status)) {
            return false;
        }
        const datumFreigegebenEKIF = this.data.freigegebenNaechsteImpfungAb;
        return !!datumFreigegebenEKIF;
    }

    public getTextNextTerminBuchen(): string {
        const datumFreigegebenEKIF = this.data.freigegebenNaechsteImpfungAb;
        if (!datumFreigegebenEKIF) {
            return '';
        }
        if (DateUtil.isAfterToday(datumFreigegebenEKIF)) {
            return this.translateService.instant('FACH-APP.KONTROLLE.NEXT_FREIGABE_TEXT',
                {datumFreigabe: this.getDateString(datumFreigegebenEKIF)});
        } else {
            return this.translateService.instant('FACH-APP.KONTROLLE.ALREADY_FREIGEGEBEN_TEXT');
        }
    }

    private getDateString(date: Date | undefined | null): string {
        if (date == null) {
            return '';
        }
        return formatDate(
            date,
            DateUtil.dateFormatMedium(this.translateService.currentLang),
            this.translateService.currentLang);
    }

    public termineAnpassen(): void {
        this.termineUmbuchenCalled.emit();
    }
}
