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
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import moment from 'moment';
import {ImpffolgeTS, ImpfslotJaxTS, KrankheitIdentifierTS, TerminbuchungService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../../vacme-web-shared';
import {ErrorMessageService} from '../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {TerminUtilService} from '../../../../../vacme-web-shared/src/lib/service/termin-util.service';
import {TerminfindungService} from '../../../../../vacme-web-shared/src/lib/service/terminfindung.service';
import {VacmeSettingsService} from '../../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';

const LOG = LogFactory.createLog('LandingpageComponent');

@Component({
    selector: 'app-terminfindung-web-page',
    templateUrl: './terminfindung-web-page.component.html',
    styleUrls: ['./terminfindung-web-page.component.scss'],
})
export class TerminfindungWebPageComponent extends BaseDestroyableComponent implements OnInit {

    public freieSlots!: Array<ImpfslotJaxTS>;
    public impffolge!: ImpffolgeTS;
    public startDatum!: Date;
    public odi!: string;
    public modif = false;
    public erstTerminAdHoc = false;
    public referrerPart: string | undefined = undefined;
    public krankheit: KrankheitIdentifierTS | undefined;

    constructor(
        private activeRoute: ActivatedRoute,
        public terminfindungService: TerminfindungService,
        private router: Router,
        public translateService: TranslateService,
        private terminbuchungService: TerminbuchungService,
        private terminUtilService: TerminUtilService,
        private errorService: ErrorMessageService,
        private vacmeSettingsService: VacmeSettingsService,
    ) {
        super();
    }

    ngOnInit(): void {
        this.activeRoute.data
            .pipe(this.takeUntilDestroyed())
            .subscribe(next => {
                this.freieSlots = next.freieSlots;
                this.impffolge = next.impffolge;
                this.startDatum = next.datum;
                this.odi = next.odi;
                this.modif = next.modif;
                this.erstTerminAdHoc = next.erstTerminAdHoc;
                this.referrerPart = next.referrerPart;
                this.terminfindungService.dashboard.registrierungsnummer = next.registrierungsnummer;
                this.terminfindungService.ortDerImpfung = {id: this.odi}; // id reicht da auf overview reload triggerd
                this.krankheit = next.krankheit;

                this.freieSlots?.sort((a: ImpfslotJaxTS, b: ImpfslotJaxTS) => {
                    // @ts-ignore
                    return a.zeitfenster?.von?.getTime() - b.zeitfenster?.von?.getTime();

                });
                // When click on F5, we should be able to go back
                if (!!next.termin1 && !this.terminfindungService.selectedSlot1) {
                    this.terminfindungService.selectedSlot1 = next.termin1;
                }
                if (!!next.termin2 && !this.terminfindungService.selectedSlot2) {
                    this.terminfindungService.selectedSlot2 = next.termin2;
                }
                if (!!next.status && !this.terminfindungService.dashboard.status) {
                    this.terminfindungService.dashboard.status = next.status;
                }

            }, error => {
                LOG.error(error);
            });
    }

    public getMinDateRestriction(): Date | undefined {
        return this.terminfindungService.getMinDateRestriction(this.impffolge);
    }

    public getMaxDateRestriction(): Date | undefined {
        // VACME-437 Impfkontrolle/Impfdoku: Termin Heute.
        // Kommentar von Xaver: Beim Impfort sollte dies immer moeglich sein auch bei ad Hoc buchungen
        return undefined;
    }

    public onChangedDatum($event: Date): void {
        this.router.navigate([
                'person',
                this.terminfindungService.dashboard.registrierungsnummer,
                'terminfindung',
                'krankheit',
                this.getKrankheit(),
                this.odi,
                this.impffolge,
                DateUtil.momentToLocalDateTime(moment($event)),
            ],
            {
                queryParams: this.activeRoute.snapshot.queryParams,
            });
    }

    public onSelectedImpfslot($event: ImpfslotJaxTS): void {

        const regNr = this.terminfindungService.dashboard.registrierungsnummer;
        if (regNr) {
            this.sendReservation($event, regNr);
        }

    }

    private sendReservation(impfslot: ImpfslotJaxTS, regNr: string): void {

        if (!impfslot || !impfslot.id) {
            LOG.warn('impfslot event is null');
            return;
        }

        if (!this.vacmeSettingsService.reservationsEnabled()) {
            this.handleSlotSuccessfullySelected(impfslot);
            return;
        }

        this.terminbuchungService.terminbuchungResourceReservieren(
            this.impffolge, impfslot.id, regNr).subscribe(
            () => {
                this.handleSlotSuccessfullySelected(impfslot);
            },
            (error: any) => {
                // im Fehlerfall einfach zuruecknavigieren ohne Termine zu waehlen
                LOG.error(error);
                LOG.error('Termin konnte nicht reserviert werden');
                this.navigateBack();
            },
        );
    }

    private handleSlotSuccessfullySelected(impfslot: ImpfslotJaxTS): void {
        switch (this.impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                this.terminfindungService.selectedSlot1 = impfslot;
                if (!this.terminUtilService.isCorrectAbstandZweiterTermin(
                    this.terminfindungService.selectedSlot1,
                    this.terminfindungService.selectedSlot2,
                    this.terminfindungService.dashboard?.status,
                )
                ) {
                    this.terminfindungService.selectedSlot2 = undefined;
                }
                break;
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                this.terminfindungService.selectedSlot2 = impfslot;
                break;
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                this.terminfindungService.selectedSlotN = impfslot;
                break;
        }
        this.navigateBack();
    }

    public navigateBack(): void {
        const referrerArray = this.referrerPart ? this.referrerPart.split('/') : [];
        const commands = [
            'person',
            this.terminfindungService.dashboard.registrierungsnummer,
        ].concat(referrerArray)
            .concat(this.getKrankheit());
        this.router.navigate(commands,
            {
                queryParams: {modif: true, erstTerminAdHoc: this.erstTerminAdHoc},
            });
    }

    public getSelectedImpfslot(): ImpfslotJaxTS | undefined {
        switch (this.impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                return this.terminfindungService.selectedSlot1;
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                return this.terminfindungService.selectedSlot2;
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                return this.terminfindungService.selectedSlotN;
        }
    }

    public getKrankheit(): KrankheitIdentifierTS {
        if (this.krankheit === undefined || this.krankheit === null) {
            this.errorService.addMesageAsError('KRANKHEIT NICHT GESETZT');
            throw new Error('Krankheit nicht gesetzt ' + this.krankheit);
        }
        return this.krankheit;
    }
}
