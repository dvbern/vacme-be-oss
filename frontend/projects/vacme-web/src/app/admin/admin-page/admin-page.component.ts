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

import {Component, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
import {
    ImpfzentrumStatJaxTS,
    OrtDerImpfungJaxTS,
    OrtderimpfungService,
    PublicService,
    StatService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../../vacme-web-shared';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import {SortByPipe} from '../../../../../vacme-web-shared/src/lib/util/sort-by-pipe';

const LOG = LogFactory.createLog('AdminPageComponent');

@Component({
    selector: 'app-admin-page',
    templateUrl: './admin-page.component.html',
    styleUrls: ['./admin-page.component.scss'],
    providers: [SortByPipe],
})
export class AdminPageComponent extends BaseDestroyableComponent implements OnInit {
    serverVersion: any;
    anzahlRegistrierungen: any;

    ortderimpfungenListe: Array<OrtDerImpfungJaxTS> = [];

    totkapazitaetTermin1 = 0;
    totkapazitaetTermin2 = 0;
    totkapazitaetTerminN = 0;
    totnumberTermin1 = 0;
    totnumberTermin2 = 0;
    totnumberTerminN = 0;
    totNumberImpfungen1 = 0;
    totNumberImpfungen2 = 0;
    totNumberImpfungenN = 0;

    public selOrtDerImfpung?: OrtDerImpfungJaxTS;
    public impfzentrumStat?: ImpfzentrumStatJaxTS;

    selectedJahr = DateUtil.currentYear();
    selectedMonat = DateUtil.currentMonth();
    monthYearCombination: Array<{ month: number; year: number }> = new Array();

    constructor(
        private publicService: PublicService,
        private ortderimpfungService: OrtderimpfungService,
        private statService: StatService,
        private sortPipe: SortByPipe,
        private authService: AuthServiceRsService,
        public translateService: TranslateService,
    ) {
        super();
    }

    ngOnInit(): void {

        this.publicService.publicResourceGetVersion().subscribe(value => {
            this.serverVersion = value;
        }, error => {
            LOG.error(error);
        });

        this.statService.statsResourceGetAnzahlRegistrierungen().subscribe(value => {
            this.anzahlRegistrierungen = value;
        }, error => {
            LOG.error(error);
        });

        this.authService.loadOdisForCurrentUserAndStoreInPrincipal$(false).subscribe(
            (list: Array<OrtDerImpfungJaxTS>) => {
                this.ortderimpfungenListe = this.sortPipe.transform(list, 'asc', 'name');
                this.updateMonthYearCombinations();
            },
            (error: any) => {
                LOG.error(error);
            },
        );
    }

    public chooseItem(event: any): void {
        if (!!event) {
            this.selOrtDerImfpung = event;
            this.load(this.selOrtDerImfpung);
        }
    }

    private load(value: OrtDerImpfungJaxTS | undefined | null): void {
        // at midday to avoid timezone issues
        const mStart = DateUtil.firstDayOfMonth(this.selectedMonat).year(this.selectedJahr).hour(12);
        const mEnd = DateUtil.lastDayOfMonth(this.selectedMonat).year(this.selectedJahr).hour(12);

        if (value && value.id) {
            this.statService.statsResourceGetImpfzentrumStatistics(value.id, mEnd.toDate(), mStart.toDate())
                .subscribe(next => {
                    this.impfzentrumStat = next;
                    this.totkapazitaetTermin1 = 0;
                    this.totkapazitaetTermin2 = 0;
                    this.totkapazitaetTerminN = 0;
                    this.totnumberTermin1 = 0;
                    this.totnumberTermin2 = 0;
                    this.totnumberTerminN = 0;
                    this.totNumberImpfungen1 = 0;
                    this.totNumberImpfungen2 = 0;
                    this.totNumberImpfungenN = 0;

                    if (this.impfzentrumStat.list) {
                        // eslint-disable-next-line @typescript-eslint/no-shadow
                        this.impfzentrumStat.list.forEach((value) => {
                            if (value.numberTermin1) {
                                this.totnumberTermin1 += value.numberTermin1;
                            }
                            if (value.numberTermin2) {
                                this.totnumberTermin2 += value.numberTermin2;
                            }
                            if (value.numberTerminN) {
                                this.totnumberTerminN += value.numberTerminN;
                            }
                            if (value.kapazitaetTermin1) {
                                this.totkapazitaetTermin1 += value.kapazitaetTermin1;
                            }
                            if (value.kapazitaetTermin2) {
                                this.totkapazitaetTermin2 += value.kapazitaetTermin2;
                            }
                            if (value.kapazitaetTerminN) {
                                this.totkapazitaetTerminN += value.kapazitaetTerminN;
                            }
                            if (value.numberImpfung1) {
                                this.totNumberImpfungen1 += value.numberImpfung1;
                            }
                            if (value.numberImpfung2) {
                                this.totNumberImpfungen2 += value.numberImpfung2;
                            }
                            if (value.numberImpfungN) {
                                this.totNumberImpfungenN += value.numberImpfungN;
                            }
                        });
                    }
                }, error => {
                    LOG.error(error);
                    this.impfzentrumStat = undefined;
                });
        }
    }

    updateMonthYearCombinations(): void {
        this.monthYearCombination.splice(0, this.monthYearCombination.length);
        const m: moment.Moment = DateUtil.ofMonthYear(this.selectedMonat, this.selectedJahr);
        const mm4: moment.Moment = DateUtil.substractMonths(m, 4);
        const mm3: moment.Moment = DateUtil.substractMonths(m, 3);
        const mm2: moment.Moment = DateUtil.substractMonths(m, 2);
        const mm1: moment.Moment = DateUtil.substractMonths(m, 1);
        const mp1: moment.Moment = DateUtil.addMonths(m, 1);
        const mp2: moment.Moment = DateUtil.addMonths(m, 2);
        const mp3: moment.Moment = DateUtil.addMonths(m, 3);
        const mp4: moment.Moment = DateUtil.addMonths(m, 4);
        this.monthYearCombination.push({month: mm4.month(), year: mm4.year()});
        this.monthYearCombination.push({month: mm3.month(), year: mm3.year()});
        this.monthYearCombination.push({month: mm2.month(), year: mm2.year()});
        this.monthYearCombination.push({month: mm1.month(), year: mm1.year()});
        this.monthYearCombination.push({month: m.month(), year: m.year()});
        this.monthYearCombination.push({month: mp1.month(), year: mp1.year()});
        this.monthYearCombination.push({month: mp2.month(), year: mp2.year()});
        this.monthYearCombination.push({month: mp3.month(), year: mp3.year()});
        this.monthYearCombination.push({month: mp4.month(), year: mp4.year()});
    }

    chooseMonth(monthYear: { month: number; year: number }): void {
        this.selectedMonat = monthYear.month;
        this.selectedJahr = monthYear.year;
        if (this.selOrtDerImfpung) {
            this.load(this.selOrtDerImfpung);
        }

    }

}

