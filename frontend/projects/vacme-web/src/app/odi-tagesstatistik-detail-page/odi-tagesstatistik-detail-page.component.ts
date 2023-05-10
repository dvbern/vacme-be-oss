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

import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {
    ImpffolgeTS,
    ImpfstoffJaxTS,
    ImpfzentrumTagesReportDetailAusstehendEntryJaxTS,
    ImpfzentrumTagesReportDetailAusstehendJaxTS,
    ImpfzentrumTagesReportDetailEntryImpfstoffJaxTS,
    ImpfzentrumTagesReportDetailEntryJaxTS,
    ImpfzentrumTagesReportDetailJaxTS,
    StatService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../vacme-web-shared';
import LayoutUtil from '../../../../vacme-web-shared/src/lib/util/LayoutUtil';
import {GeplanteImpfungen} from '../model/geplante-impfungen';

const LOG = LogFactory.createLog('OdiTagesstatistikDetailPageComponent');

@Component({
    selector: 'app-odi-tagesstatistik-detail-page',
    templateUrl: './odi-tagesstatistik-detail-page.component.html',
    styleUrls: ['./odi-tagesstatistik-detail-page.component.scss'],
})
export class OdiTagesstatistikDetailPageComponent extends BaseDestroyableComponent implements OnInit, OnDestroy {

    slotDetails: ImpfzentrumTagesReportDetailJaxTS | undefined;
    public impfstoffList: Array<ImpfstoffJaxTS> = [];

    public selectedRow: ImpfzentrumTagesReportDetailEntryJaxTS | undefined;
    public ausstehendForRow: ImpfzentrumTagesReportDetailAusstehendJaxTS | undefined;

    loading = true;

    constructor(
        private statService: StatService,
        public translateService: TranslateService,
        private activeRoute: ActivatedRoute,
        private router: Router,
    ) {
        super();
    }

    ngOnInit(): void {
        LayoutUtil.makePageBig();

        this.activeRoute.data
            .pipe(this.takeUntilDestroyed())
            .subscribe(next => {
                this.slotDetails = next.data;
                this.impfstoffList = next.impfstoffList;
                this.loading = false;
            }, error => {
                LOG.error(error);
            });
    }

    public ngOnDestroy(): void {
        super.ngOnDestroy();
        LayoutUtil.makePageNormal();
    }

    public title(): string {
        if (this.slotDetails) {
            if (this.slotDetails.odiName && this.slotDetails.stichtag) {
                return this.slotDetails.odiName + ', ' + this.slotDetails.stichtag;
            }
        }
        return '';
    }

    public countImpfung1(slotDetail: ImpfzentrumTagesReportDetailEntryJaxTS, impfstoff: ImpfstoffJaxTS): string {
        return this.getCount(ImpffolgeTS.ERSTE_IMPFUNG, slotDetail, impfstoff);
    }

    public countImpfung2(slotDetail: ImpfzentrumTagesReportDetailEntryJaxTS, impfstoff: ImpfstoffJaxTS): string {
        return this.getCount(ImpffolgeTS.ZWEITE_IMPFUNG, slotDetail, impfstoff);
    }

    public countImpfungN(slotDetail: ImpfzentrumTagesReportDetailEntryJaxTS, impfstoff: ImpfstoffJaxTS): string {
        return this.getCount(ImpffolgeTS.BOOSTER_IMPFUNG, slotDetail, impfstoff);
    }

    public getCount(
        impffolge: ImpffolgeTS,
        slotDetail: ImpfzentrumTagesReportDetailEntryJaxTS,
        impfstoff: ImpfstoffJaxTS,
    ): string {
        const entry = this.findEntry(slotDetail, impfstoff);

        const geplantImpfstoff = this.getGeplanteOderDurchgefuehrte(impffolge, entry) || 0;
        const geplantUnbekannt = this.getUnbekannterImpfstoff(impffolge, slotDetail) || 0;

        const geplant = new GeplanteImpfungen(geplantImpfstoff, geplantImpfstoff + geplantUnbekannt);
        return geplant.print();
    }

    private getGeplanteOderDurchgefuehrte(
        impffolge: ImpffolgeTS,
        entry?: ImpfzentrumTagesReportDetailEntryImpfstoffJaxTS,
    ): number | undefined {
        if (!entry) {
            return undefined;
        }
        switch (impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                return entry.geplanteOderDurchgefuehrteImpfungen1;
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                return entry.geplanteOderDurchgefuehrteImpfungen2;
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                return entry.geplanteOderDurchgefuehrteImpfungenN;
        }
    }

    private getUnbekannterImpfstoff(
        impffolge: ImpffolgeTS,
        slotDetail: ImpfzentrumTagesReportDetailEntryJaxTS,
    ): number | undefined {
        switch (impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                return slotDetail.geplanteImpfungen1UnbekannterImpfstoff;
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                return slotDetail.geplanteImpfungen2UnbekannterImpfstoff;
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                return slotDetail.geplanteImpfungenNUnbekannterImpfstoff;
        }
    }

    private findEntry(
        slotDetail: ImpfzentrumTagesReportDetailEntryJaxTS,
        impfstoff: ImpfstoffJaxTS,
    ): ImpfzentrumTagesReportDetailEntryImpfstoffJaxTS | undefined {
        if (slotDetail && slotDetail.planungPerImpfstoff) {

            return Array.from(slotDetail.planungPerImpfstoff.values()).find(e => e.impfstoffName === impfstoff.name);
        }
        return undefined;
    }

    public onSelect(selected: ImpfzentrumTagesReportDetailEntryJaxTS): void {
        this.selectedRow = selected;
        if (selected.slotId) {
            this.statService.statsResourceGetOdiTagesReportDetailAusstehendeCodes(selected.slotId).subscribe(next => {
                this.ausstehendForRow = next;
            }, error => {
                LOG.error(error);
                this.ausstehendForRow = undefined;
            });
        }
    }

    public isSelected(row: ImpfzentrumTagesReportDetailEntryJaxTS): boolean {
        return this.selectedRow === row;
    }

    public openRegistration(item: ImpfzentrumTagesReportDetailAusstehendEntryJaxTS): void {
        // Converts the route into a string that can be used
        // with the window.open() function
        const url = this.router.serializeUrl(
            this.router.createUrlTree(['dossier', item.registrierungsnummer]),
        );
        window.open(url, '_blank');
    }
}
