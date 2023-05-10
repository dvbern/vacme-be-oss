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
import {ActivatedRoute, Params, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
import {mergeMap} from 'rxjs/operators';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    DashboardJaxTS,
    DossierService,
    ImpffolgeTS,
    KrankheitIdentifierTS,
    KundengruppeFilterTS,
    NextFreierTerminJaxTS,
    OrtDerImpfungBuchungJaxTS,
    OrtDerImpfungDisplayNameExtendedJaxTS,
    TerminbuchungJaxTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent, NextFreierTerminSearch} from '../../../../../vacme-web-shared';
import {ErrorMessageService} from '../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {TerminfindungService} from '../../../../../vacme-web-shared/src/lib/service/terminfindung.service';
import {VacmeSettingsService} from '../../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import {
    isAtLeastOnceGeimpft,
    isErsteImpfungDoneAndZweitePending,
} from '../../../../../vacme-web-shared/src/lib/util/registrierung-status-utils';
import TenantUtil from '../../../../../vacme-web-shared/src/lib/util/TenantUtil';
import {OdiDistanceCacheService} from '../../service/odi-distance.cache.service';

const LOG = LogFactory.createLog('OverviewPageComponent');

@Component({
    selector: 'app-overview-page',
    templateUrl: './overview-page.component.html',
    styleUrls: ['./overview-page.component.scss'],
})
export class OverviewPageComponent extends BaseDestroyableComponent implements OnInit {

    public dashboardJax!: DashboardJaxTS;
    public ortDerImpfungList?: OrtDerImpfungDisplayNameExtendedJaxTS[];
    public modif = false;
    public selectedOdiId: string | undefined;

    constructor(
        public terminfindungService: TerminfindungService,
        public dossierService: DossierService,
        private activeRoute: ActivatedRoute,
        private router: Router,
        private translationService: TranslateService,
        private vacmeSettingsService: VacmeSettingsService,
        private errorService: ErrorMessageService,
        private odiDistanceCacheService: OdiDistanceCacheService,
    ) {
        super();
    }

    ngOnInit(): void {
        this.activeRoute.data
            .pipe(this.takeUntilDestroyed())
            .subscribe(next => {
                this.dashboardJax = next.dossier;
                this.modif = next.modif;
            }, error => {
                LOG.error(error);
            });
        if (!!this.dashboardJax.registrierungsnummer) {
            // alle orte laden
            this.dossierService.dossierResourceRegGetAllOrteDerImpfungDisplayName(
                this.getKrankheit(),
                this.getKundenGruppeForOdiFilter(),
                this.dashboardJax.registrierungsnummer,
            )
                .pipe(mergeMap(odiList => this.odiDistanceCacheService.calculateOdiDistances$(odiList,
                    this.dashboardJax.registrierungsnummer)))
                .subscribe(
                    (list: OrtDerImpfungDisplayNameExtendedJaxTS[]) => {
                        this.ortDerImpfungList = list;
                    },
                    (error: any) => {
                        LOG.error(error);
                        this.ortDerImpfungList = [];
                    },
                );
        }
        this.reinitializeFromDossier(this.dashboardJax, false);
    }

    private getKundenGruppeForOdiFilter(): KundengruppeFilterTS {
        if (TenantUtil.isWellPartnerDomain(document.location.hostname)
                && this.vacmeSettingsService.wellEnabled(this.getKrankheit())
                && this.vacmeSettingsService.odiFilterWellForGalenicaEnabled) {
            return KundengruppeFilterTS.GALENICA;
        }
        return KundengruppeFilterTS.ALL;
    }

    public getKrankheit(): KrankheitIdentifierTS {
        if (this.dashboardJax.krankheitIdentifier === undefined || this.dashboardJax.krankheitIdentifier === null) {
            this.errorService.addMesageAsError('KRANKHEIT NICHT GESETZT');
            throw new Error('Krankheit nicht gesetzt ' + this.dashboardJax.registrierungsnummer);
        }
        return this.dashboardJax.krankheitIdentifier;
    }

    public reloadDossier($event: string): void {
        this.dossierService.dossierResourceRegGetDashboardRegistrierung(this.getKrankheit(), $event)
            .subscribe(loadedDossier => {
                this.dashboardJax = loadedDossier;
                this.reinitializeFromDossier(loadedDossier, true);
            }, error => LOG.error(error));
    }

    /**
     * reinitialisiert das gui vom uebergegebenen dossier. Lediglich die Terminverwaltung wird nicht ueberschrieben
     *
     */
    private reinitializeFromDossier(dashboardJax: DashboardJaxTS, forceReload: boolean): void {
        const dossierChange = this.terminfindungService.hasChangedDossiernummer(dashboardJax);
        const hasKrankheitChanged = this.terminfindungService.hasChangedKrankheit(dashboardJax);
        // alle daten bis auf die termine koennen aus dem dossier gelesen werden
        this.terminfindungService.setDashboardAndResetDataIfKrankheitChanged(dashboardJax);
        const needsReload = dossierChange || hasKrankheitChanged;
        // den impfort muessen wir auch wiederherstellen  wenn er im current dossier noch nicht gespeichert wurde.
        // i.e. wenn wir von der terminverwaltung zurueck kommen
        if (needsReload || forceReload ) {
            if (this.terminfindungService.isAlreadyGrundimmunisiert()) {
                if (dashboardJax.terminNPending && dashboardJax.terminNPending.impfslot?.ortDerImpfung) {
                    this.terminfindungService.ortDerImpfung = dashboardJax.terminNPending.impfslot.ortDerImpfung;
                } else {
                    this.terminfindungService.ortDerImpfung = dashboardJax.gewuenschterOrtDerImpfung;
                }
            } else {
                if (isErsteImpfungDoneAndZweitePending(dashboardJax.status)) {
                    if (dashboardJax.termin2 && dashboardJax.termin2.impfslot?.ortDerImpfung) {
                        this.terminfindungService.ortDerImpfung = dashboardJax.termin2?.impfslot?.ortDerImpfung;
                    } else {
                        this.terminfindungService.ortDerImpfung = dashboardJax.gewuenschterOrtDerImpfung;
                    }
                } else {
                    if (dashboardJax.termin1 && dashboardJax.termin1.impfslot?.ortDerImpfung) {
                        this.terminfindungService.ortDerImpfung = dashboardJax.termin1.impfslot.ortDerImpfung;
                    } else {
                        this.terminfindungService.ortDerImpfung = dashboardJax.gewuenschterOrtDerImpfung;
                    }
                }
            }
        }

        this.initSelectedOdiIfTerminBuchbar();

        if (needsReload || forceReload) {
            this.terminfindungService.selectedSlot1 = dashboardJax.termin1?.impfslot;
            this.terminfindungService.selectedSlot2 = dashboardJax.termin2?.impfslot;
            this.terminfindungService.selectedSlotN = dashboardJax.terminNPending?.impfslot;
        }

        // den gewahlten ort laden (mit noetigen Zusatzinfo die im odi auf dem dossier nicht drin sind)
        if (this.terminfindungService.ortDerImpfung && this.terminfindungService.ortDerImpfung.id) {
            this.dossierService.dossierResourceRegGetOrtDerImpfungBuchung(
                this.getKrankheit(),
                this.terminfindungService.ortDerImpfung.id
            ).subscribe(
                    (gewOrt: OrtDerImpfungBuchungJaxTS) => {
                        this.terminfindungService.ortDerImpfung = gewOrt;
                    },
                    (error: any) => {
                        LOG.error(error);
                    },
                );
        }
    }

    goBack($event: string): void {
        if ($event) {
            this.reloadDossier($event);
        }
        this.router.navigate(
            [],
            {
                relativeTo: this.activeRoute,
                queryParams: {modif: 'false'},
                fragment: 'termine-anchor',
            });
    }

    cancelAppointment($event: string): void {
        const questionKey = this.isImpffolgeBooster() ?
            'OVERVIEW.CANCEL_TERMIN_BOOSTER.QUESTION' :
            'OVERVIEW.CANCEL_TERMIN.QUESTION';
        const confirmKey = this.isImpffolgeBooster() ?
            'OVERVIEW.CANCEL_TERMIN_BOOSTER.CONFIRM' :
            'OVERVIEW.CANCEL_TERMIN.CONFIRM';
        Swal.fire({
            icon: 'question',
            text: this.translationService.instant(questionKey),
            showCancelButton: true,
            cancelButtonText: this.translationService.instant('OVERVIEW.CANCEL_TERMIN.CANCEL'),
            confirmButtonText: this.translationService.instant(confirmKey),
            customClass: {
                confirmButton: 'primary',
                cancelButton: 'secondary',
            },
        }).then(r => {
            if (r.isConfirmed) {
                this.dossierService
                    .dossierResourceRegOdiAndTermineAbsagen(this.getKrankheit(), $event)
                    .subscribe(() => {
                        this.goBack($event);
                    }, (error => {
                        LOG.error(error);
                    }));
            }
        });
    }

    public isImpffolgeBooster(): boolean {
        return !!this.dashboardJax.vollstaendigerImpfschutz;
    }

    updateAppointment($event: string): void {
        const buchung: TerminbuchungJaxTS = {
            registrierungsnummer: this.terminfindungService.dashboard.registrierungsnummer,
            slot1Id: this.terminfindungService.selectedSlot1?.id,
            slot2Id: this.terminfindungService.selectedSlot2?.id,
            slotNId: this.terminfindungService.selectedSlotN?.id,
            krankheit: this.getKrankheit()
        };
        this.dossierService.dossierResourceRegUmbuchen(buchung)
            .subscribe(() => {
                this.goBack($event);
            }, (error => {
                LOG.error(error);
            }));
    }

    nextFreieTermin($event: NextFreierTerminSearch): void {
        if (isAtLeastOnceGeimpft(this.terminfindungService.dashboard?.status) && $event.impffolge === ImpffolgeTS.ZWEITE_IMPFUNG) {
            this.dossierService
                .dossierResourceRegGetNextFreierZweitterminUmbuchung($event.ortDerImpfungId, $event.nextDateParam)
                .subscribe(
                    nextReturned => this.handleNextFreieTermin($event, nextReturned),
                    error => {
                        LOG.error(error);
                        this.errorService.addMesageAsError('ERROR_NO_TERMINE');
                    });
        } else {
            this.dossierService
                .dossierResourceRegGetNextFreierImpftermin($event.impffolge,
                    this.getKrankheit(),
                    $event.ortDerImpfungId,
                    $event.nextDateParam)
                .subscribe(
                    nextReturned => this.handleNextFreieTermin($event, nextReturned),
                    error => {
                        LOG.error(error);
                        this.errorService.addMesageAsError('ERROR_NO_TERMINE');
                    });
        }
    }

    private handleNextFreieTermin($event: NextFreierTerminSearch, nextReturned: NextFreierTerminJaxTS): void {
        if (nextReturned) { // Termin gefunden -> navigieren
            const modifFlag = this.activeRoute.snapshot.queryParamMap.get('modif');
            const modifQueryParams: Params = {
                modif: modifFlag,
                t1: this.terminfindungService.selectedSlot1 ?
                    JSON.stringify(this.terminfindungService.selectedSlot1) : undefined,
                t2: this.terminfindungService.selectedSlot2 ?
                    JSON.stringify(this.terminfindungService.selectedSlot2) : undefined,
                tN: this.terminfindungService.selectedSlotN ?
                    JSON.stringify(this.terminfindungService.selectedSlotN) : undefined,
                status: this.terminfindungService.dashboard.status ?
                    JSON.stringify(this.terminfindungService.dashboard.status) : undefined,
            };
            this.router.navigate([
                    'registrierung',
                    $event.registrierungsnummer,
                    'terminfindung',
                    'krankheit',
                    this.getKrankheit(),
                    $event.ortDerImpfungId,
                    $event.impffolge,
                    DateUtil.momentToLocalDateTime(moment(nextReturned.nextDate)),
                ],
                {
                    queryParams: modifQueryParams,
                });
        } else {
            if ($event.otherTerminDate) { // Warscheinlich ist die Regel +-28 Tage nicht erfuellt
                const distText = this.vacmeSettingsService.getDistanceDesiredDistanceWithUnitText();
                const errMsg = this.translationService.instant('ERROR_NO_TERMINE_IN_RANGE',
                    {desiredDistance: distText});
                this.errorService.addMesageAsError(errMsg);
            } else { // Warscheinlich hat der Slot keine Termine frei
                this.errorService.addMesageAsError('ERROR_NO_TERMINE');
            }
        }
    }

    /**
     * Damit man beim umbuchen in der selectbox keinen nicht auswaehlbaren Eintrag gesetzt hat setzen wir
     * den gewahlten odi auf undefined wenn dieser nicht (mehr) gebucht werden kann
     * to improve: anderer Ansatz VACME-1865
     */
    private initSelectedOdiIfTerminBuchbar(): void {
        // wir setzen die OdiId fuer die terminbuchung nur wenn es sich um ein oeffentliches odi handelt
        this.selectedOdiId = undefined;

        if (this.terminfindungService.ortDerImpfung) {
            const selecteOrt = this.terminfindungService.ortDerImpfung;
            // to improve: Wir sollten den terminfindungService umbauen so dass er nicht nur ids hat
            // oeffentlich flag ist nicht immer gesetzt weil wir bei rueckkehr aus terminfindung nur noch die id haben
            // damit koennen wir aber leben weil wir wissen dass er nur ein erlaubtes odi dort selecten konnte
            if (selecteOrt && selecteOrt.oeffentlich !== undefined) {
                if (selecteOrt.oeffentlich
                    && selecteOrt.deaktiviert === false
                    && selecteOrt.terminverwaltung
                    && this.impfstatusValidForSelectedOdi(selecteOrt)
                ) {
                    this.selectedOdiId = selecteOrt.id;
                } else {
                    this.selectedOdiId = undefined;
                }
            } else {
                this.selectedOdiId = this.terminfindungService.ortDerImpfung.id;
            }
        }
    }

    private impfstatusValidForSelectedOdi(selecteOrt: OrtDerImpfungBuchungJaxTS): boolean {
        // wenn wir am Boostern sind muss das OdI Booster Flag haben
        if (this.terminfindungService.isAlreadyGrundimmunisiert()) {
            return selecteOrt.booster === true;
        }
        return true; // wenn wir nicht boostern ist es egal
    }
}
