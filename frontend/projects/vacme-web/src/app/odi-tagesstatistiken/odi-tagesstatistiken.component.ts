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

import {Component, Input, OnInit, ViewChildren} from '@angular/core';
import {OrtDerImpfungJaxTS} from 'vacme-web-generated';
import {MAX_ODI_STATS_ON_STARTPAGE} from '../../../../vacme-web-shared/src/lib/constants';
import DateUtil from '../../../../vacme-web-shared/src/lib/util/DateUtil';
import {OdiTagesstatistikComponent} from '../odi-tagesstatistik/odi-tagesstatistik.component';

@Component({
    selector: 'app-odi-tagesstatistiken',
    templateUrl: './odi-tagesstatistiken.component.html',
    styleUrls: ['./odi-tagesstatistiken.component.scss'],
})
export class OdiTagesstatistikenComponent implements OnInit {

    @Input()
    ortderimpfungenListe: Array<OrtDerImpfungJaxTS> = [];
    visibleOdis: Array<string | undefined> = [];
    typeaheadModel: any;

    date = DateUtil.today().hours(12).toDate(); // at midday to avoid timezone issues

    @ViewChildren(OdiTagesstatistikComponent)
    odiStats!: OdiTagesstatistikComponent[];

    MAX_ODI_STATS_ON_STARTPAGE = MAX_ODI_STATS_ON_STARTPAGE;

    constructor() {
    }

    ngOnInit(): void {
        this.loadVisibleOdiState();
    }

    getVisibleOdis(): Array<OrtDerImpfungJaxTS> {
        return this.ortderimpfungenListe.filter(value => this.visibleOdis.includes(value.id));
    }

    saveVisibleOdiState(): void {
        localStorage.setItem('visibleOdiStats', JSON.stringify(this.visibleOdis));
    }

    private loadVisibleOdiState(): void {
        const odisFromLocalStorage = localStorage.getItem('visibleOdiStats');
        if (odisFromLocalStorage) {
            this.visibleOdis = JSON.parse(odisFromLocalStorage) as Array<string>;
        }
    }

    removeOdi(impfort: OrtDerImpfungJaxTS): void {
        this.visibleOdis = this.visibleOdis.filter(value => value !== impfort.id);
        this.saveVisibleOdiState();
        this.typeaheadModel = ''; // clear typeahead
    }

    getInvisibleOdis(): Array<OrtDerImpfungJaxTS> {
        return this.ortderimpfungenListe.filter(value => !this.visibleOdis.includes(value.id));
    }

    public onChangedOdiStatsDate($event: Date): void {
        this.date = $event;
        this.odiStats.forEach(value => value.onChangedDate($event));
    }

    public chooseItem(event: any): void {
        if (!!event) {
            this.visibleOdis.push(event.id);
            this.saveVisibleOdiState();
        }
    }
}
