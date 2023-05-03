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

import {Component, Input, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import moment from 'moment';
import {
    ImpfstoffTagesReportJaxTS,
    ImpfzentrumTagesReportJaxTS,
    OrtDerImpfungJaxTS,
    StatService
} from 'vacme-web-generated';
import DateUtil from '../../../../vacme-web-shared/src/lib/util/DateUtil';

@Component({
    selector: 'app-odi-tagesstatistik',
    templateUrl: './odi-tagesstatistik.component.html',
    styleUrls: ['./odi-tagesstatistik.component.scss']
})
export class OdiTagesstatistikComponent implements OnInit {

    public IMPFFOLGE_1 = '1';
    public IMPFFOLGE_2 = '2';
    public IMPFFOLGE_N = 'N';

    @Input()
    odi!: OrtDerImpfungJaxTS;

    public impfstoffInfos?: Array<{ name?: string; displayName?: string }>;

    result!: ImpfzentrumTagesReportJaxTS;

    date = DateUtil.today().hours(12).toDate(); // at midday to avoid timezone issues

    loading = true;

    constructor(
        private statService: StatService,
        public translateService: TranslateService,
        private router: Router,
    ) {
    }

    ngOnInit(): void {
        this.loadNumbers();
        this.dummyUseFields();
    }

    public onChangedDate($event: Date): void {
        this.date = $event;
        this.loadNumbers();
    }

    public loadNumbers(): void {
        this.loading = true;
        this.statService.statsResourceGetOdiTagesReport(this.odi.id as string, this.date).subscribe(
            next => {
                this.result = next;
                this.loading = false;
                this.impfstoffInfos = this.result.impfstoffTagesReportJaxMap.map((e) => {
                    return {
                        name: e.impfstoffName,
                        displayName: e.impfstoffDisplayName as string
                    };
                });
            },
            error => {
                console.log(error);
                this.loading = false;
            }
        );
    }

    /**
     * Die Fields im ImpfstoffTagesReportJaxTS werden dynamisch benutzt.
     * Damit man weiss, wo, gibt es diese Info-Methode.
     * Siehe  <table *ngIf="'pendentTermin' as tablePrefix">
     */
    private dummyUseFields(): void {
        const dummyEntry = {} as ImpfstoffTagesReportJaxTS;
        let dummyNumber;
        dummyNumber = dummyEntry.pendentTermin1;
        dummyNumber = dummyEntry.pendentTermin2;
        dummyNumber = dummyEntry.pendentTerminN;
        dummyNumber = dummyEntry.durchgefuehrtImpfung1;
        dummyNumber = dummyEntry.durchgefuehrtImpfung2;
        dummyNumber = dummyEntry.durchgefuehrtImpfungN;
        dummyNumber = dummyEntry.total1;
        dummyNumber = dummyEntry.total2;
        dummyNumber = dummyEntry.totalN;
    }

    public getZeilenLabel(impfstoffDisplayname?: string): string {
        return impfstoffDisplayname || this.translateService.instant('FACH-APP.START-PAGE.TAGES-STATISTIK.IMPFSTOFF_UNBEKANNT');
    }

    private findEntry(impfstoff?: string): ImpfstoffTagesReportJaxTS | undefined {
        return this.result?.impfstoffTagesReportJaxMap.find(e => e.impfstoffName === impfstoff);
    }

    getTotal(tablePrefix: string): number {
        return this.spalteSummieren(tablePrefix, this.IMPFFOLGE_1)
            + this.spalteSummieren(tablePrefix, this.IMPFFOLGE_2)
            + this.spalteSummieren(tablePrefix, this.IMPFFOLGE_N);
    }

    getCellValue(tablePrefix: string, impffolgePostfix: string, impfstoff?: string): number {
        const impfstoffEntry = this.findEntry(impfstoff);
        if (!impfstoffEntry) {
            throw new Error('impfstoffEntry nicht gefunden');
        }
        // @ts-ignore
        return impfstoffEntry[tablePrefix + impffolgePostfix] || 0;
    }

    zeileSummieren(tablePrefix: string, impfstoff?: string): number {
        const zeileEntry = this.findEntry(impfstoff);
        if (zeileEntry == null) {
            throw new Error('impfstoff in entries nicht gefunden');
        }
        return this.getCellValue(tablePrefix, this.IMPFFOLGE_1, impfstoff)
            + this.getCellValue(tablePrefix, this.IMPFFOLGE_2, impfstoff)
            + this.getCellValue(tablePrefix, this.IMPFFOLGE_N, impfstoff);
    }

    spalteSummieren(tablePrefix: string, impffolgePostfix: string): number {
        // @ts-ignore
        return this.result?.impfstoffTagesReportJaxMap.map(impfstoffEntry => impfstoffEntry[tablePrefix + impffolgePostfix])
            .reduce((sum, current) => sum + current, 0);
    }

    public showDetailStats(): void {
        this.router.navigate([
            'odistats/ortderimpfung',
            this.odi.id,
            'date',
            DateUtil.momentToLocalDateTime(moment(this.date))]);
    }
}
