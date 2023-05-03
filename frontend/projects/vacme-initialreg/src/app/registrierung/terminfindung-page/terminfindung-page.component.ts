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
import {DossierService, ImpffolgeTS, ImpfslotJaxTS, KrankheitIdentifierTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {
    BaseDestroyableComponent,
} from '../../../../../vacme-web-shared';
import {ErrorMessageService} from '../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {TerminfindungService} from '../../../../../vacme-web-shared/src/lib/service/terminfindung.service';
import {VacmeSettingsService} from '../../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import {
    isAtLeastGebucht,
    isAtLeastOnceGeimpft,
} from '../../../../../vacme-web-shared/src/lib/util/registrierung-status-utils';
import {NavigationService} from '../../service/navigation.service';

const LOG = LogFactory.createLog('TerminfindungPageComponent');

@Component({
    selector: 'app-terminfindung-page',
    templateUrl: './terminfindung-page.component.html',
    styleUrls: ['./terminfindung-page.component.scss'],
})
export class TerminfindungPageComponent extends BaseDestroyableComponent implements OnInit {

    public freieSlots!: Array<ImpfslotJaxTS>;
    public impffolge!: ImpffolgeTS;
    public startDatum!: Date;
    public odi!: string;
    public modif = false;
    private krankheit: KrankheitIdentifierTS | undefined;

    constructor(
        private activeRoute: ActivatedRoute,
        public terminfindungService: TerminfindungService,
        private router: Router,
        public translateService: TranslateService,
        private dossierService: DossierService,
        private errorMessageService: ErrorMessageService,
        private vacmeSettingsService: VacmeSettingsService,
        private navigationService: NavigationService,
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
                this.terminfindungService.dashboard.registrierungsnummer = next.registrierungsnummer;
                this.terminfindungService.ortDerImpfung = {id: this.odi}; // id reicht da auf overview reload triggerd
                                                                          // wird

                this.freieSlots?.sort((a: ImpfslotJaxTS, b: ImpfslotJaxTS) => {
                    // @ts-ignore
                    return a.zeitfenster?.von?.getTime() - b.zeitfenster?.von?.getTime();
                });

                this.checkStartDatumMatchesReturnedDates();
                // When click on F5, we should be able to go back
                if (!!next.termin1 && !this.terminfindungService.selectedSlot1) {
                    this.terminfindungService.selectedSlot1 = next.termin1;
                }
                if (!!next.termin2 && !this.terminfindungService.selectedSlot2) {
                    this.terminfindungService.selectedSlot2 = next.termin2;
                }
                if (!!next.terminN && !this.terminfindungService.selectedSlotN) {
                    this.terminfindungService.selectedSlotN = next.terminN;
                }
                if (!!next.status && !this.terminfindungService.dashboard.status) {
                    this.terminfindungService.dashboard.status = next.status;
                }

                this.krankheit = next.krankheit;

            }, error => {
                LOG.error(error);
            });
    }

    private checkStartDatumMatchesReturnedDates(): void {
        this.freieSlots?.forEach((v: ImpfslotJaxTS) => {
            if (v.zeitfenster !== undefined) {
                const returnedDate: Date = v.zeitfenster?.von;
                const daysDiff = DateUtil.getDaysDiff(this.startDatum, returnedDate);
                if (daysDiff !== 0) {
                    // should never happen
                    const message = `expected \'freie Impfslots\' for Date: \'${DateUtil.dateAsLocalDateString(this.startDatum)}\'
                    but received \'freie Impfslots\' for Date: \'${DateUtil.dateAsLocalDateString(returnedDate)}\'`;
                    LOG.error(message);
                    this.errorMessageService.addMesageAsError(message);
                }
            }
        });
    }

    public getKrankheit(): KrankheitIdentifierTS {
        if (this.krankheit === undefined || this.krankheit === null) {
            this.errorMessageService.addMesageAsError('KRANKHEIT NICHT GESETZT');
            throw new Error('Krankheit nicht gesetzt ' + this.krankheit);
        }
        return this.krankheit;
    }

    public getMinDateRestriction(): Date | undefined {
        return this.terminfindungService.getMinDateRestriction(this.impffolge);
    }

    public getMaxDateRestriction(): Date | undefined {
        return this.terminfindungService.getMaxDateRestriction(this.impffolge);
    }

    public onChangedDatum($event: Date): void {
        this.router.navigate([
                'registrierung',
                this.terminfindungService.dashboard.registrierungsnummer,
                'terminfindung',
                'krankheit',
                this.getKrankheit(),
                this.odi,
                this.impffolge,
                DateUtil.momentToLocalDateTime(moment($event))],
            {
                queryParams: this.activeRoute.snapshot.queryParams
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
            this.handleReservationSuccess(impfslot);
            return;
        }

        this.dossierService.dossierResourceRegReservieren(
            this.impffolge, impfslot.id, this.getKrankheit(), regNr).subscribe(
            () => {
            this.handleReservationSuccess(impfslot);
            },
            (error: any) => {
                // im Fehlerfall einfach zuruecknavigieren ohne Termine zu waehlen
                LOG.error(error);
                LOG.error('Termin konnte nicht reserviert werden');
                this.navigateBack();
            },
        );
    }

    private handleReservationSuccess(impfslot: ImpfslotJaxTS): void {
        // im Erfolgsfalls setzen wir den gewahtlen und navigieren zurueck
        switch (this.impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                this.terminfindungService.selectedSlot1 = impfslot;
                if (isAtLeastGebucht(this.terminfindungService.dashboard?.status) &&
                    !isAtLeastOnceGeimpft(this.terminfindungService.dashboard?.status)) {
                    if (this.terminfindungService.selectedSlot2) {
                        const dateDff = DateUtil.getDaysDiff(
                            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                            this.terminfindungService.selectedSlot2!.zeitfenster!.von,
                            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                            this.terminfindungService.selectedSlot1!.zeitfenster!.von);
                        // wenn der neu gewahlte Termin nicht mehr mit dem 2. Zusammenpasst muss der 2. weg
                        if (dateDff < this.getMinDiff()) {
                            this.terminfindungService.selectedSlot2 = undefined;
                        }
                    }
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
        this.navigationService.navigateToDossierDetailWithNavigationExtras(
            this.terminfindungService.dashboard.registrierungsnummer,
            this.getKrankheit(),
            {
                queryParams: {modif: this.modif}, fragment: 'termine-anchor',
            });
    }

    getMinDiff(): number {
        return this.vacmeSettingsService.distanceImpfungenMinimal();
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
}
