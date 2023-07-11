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

import {DOCUMENT} from '@angular/common';
import {Component, Inject, OnInit} from '@angular/core';
import {ActivatedRoute, Data, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject, Subject} from 'rxjs';
import {finalize} from 'rxjs/operators';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    DashboardJaxTS,
    DossierService,
    DownloadService,
    ImpfdokumentationJaxTS,
    ImpfdokumentationService,
    ImpfdossiersOverviewJaxTS,
    ImpffolgeTS,
    ImpfstoffJaxTS,
    ImpfterminJaxTS,
    KrankheitIdentifierTS,
    OrtDerImpfungDisplayNameJaxTS,
    OrtDerImpfungJaxTS,
    RegistrierungStatusTS,
    TerminbuchungService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../../../vacme-web-shared';
import TSPersonFolgetermin from '../../../../../../vacme-web-shared/src/lib/model/TSPersonFolgetermin';
import {ErrorMessageService} from '../../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {TerminUtilService} from '../../../../../../vacme-web-shared/src/lib/service/termin-util.service';
import {TerminfindungService} from '../../../../../../vacme-web-shared/src/lib/service/terminfindung.service';
import {VacmeSettingsService} from '../../../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import {BlobUtil} from '../../../../../../vacme-web-shared/src/lib/util/BlobUtil';
import {BoosterUtil} from '../../../../../../vacme-web-shared/src/lib/util/booster-util';
import {fromDashboard} from '../../../../../../vacme-web-shared/src/lib/util/person-folgetermin-util';
import {ImpfdokumentationCacheService} from '../../../service/impfdokumentation.cache.service';
import {NavigationService} from '../../../service/navigation.service';
import {RegistrierungValidationService} from '../../../service/registrierung-validation.service';
import {TerminmanagementKontrolleImpfdokService} from '../../../service/terminmanagement-kontrolle-impfdok.service';
import {
    ImpfdokumentationFormBaseData,
    ImpfdokumentationFormSubmission,
} from '../impfdokumentation-form/impfdokumentation-form.component';

const LOG = LogFactory.createLog('ImpfdokumentationGrundimunisierungComponent');

@Component({
    selector: 'app-impfdokumentation-grundimunisierung-page',
    templateUrl: './impfdokumentation-grundimunisierung-page.component.html',
    styleUrls: ['./impfdokumentation-grundimunisierung-page.component.scss'],
})
export class ImpfdokumentationGrundimunisierungPageComponent extends BaseDestroyableComponent implements OnInit {

    ortDerImpfungId: string | null = null;
    impffolge!: ImpffolgeTS;
    public impffolgeNr = 1;

    public saveInProgress = false;
    public isZertifikatEnabled = false;
    public formBaseData?: ImpfdokumentationFormBaseData;
    public odiList: OrtDerImpfungJaxTS[] = [];
    public dashboardJax?: DashboardJaxTS;
    public accessOk?: boolean; // zuerst undefined, dann true oder false
    public saved$ = new BehaviorSubject(false);
    private formDisabled$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    private validateTrigger$: Subject<void> = new Subject<void>();
    private impfstoffe?: ImpfstoffJaxTS[];
    public boosterUtil = BoosterUtil;
    private selbstzahlende?: boolean;
    public krankheitIdentifier = KrankheitIdentifierTS.COVID; // fuer Grundimmunisierung aktuell immer COVID

    // Termine umbuchen
    public modif = false;
    public erstTerminAdHoc = false;

    // ********** INIT ********************

    constructor(
        private activeRoute: ActivatedRoute,
        private router: Router,
        private impfdokumentationService: ImpfdokumentationService,
        private errorService: ErrorMessageService,
        private translationService: TranslateService,
        private impfdokumentationCacheService: ImpfdokumentationCacheService,
        private registrierungValidationService: RegistrierungValidationService,
        private terminbuchungService: TerminbuchungService,
        private dossierService: DossierService,
        private terminfindungService: TerminfindungService,
        private terminUtilService: TerminUtilService,
        private terminmanager: TerminmanagementKontrolleImpfdokService,
        private downloadService: DownloadService,
        public vacmeSettingsService: VacmeSettingsService,
        public navigationService: NavigationService,
        @Inject(DOCUMENT) private document: Document,
    ) {
        super();
    }

    ngOnInit(): void {
        this.dossierService.dossierResourceIsZertifikatEnabled().subscribe(
            response => this.isZertifikatEnabled = response,
            error => LOG.error(error));
        this.initFromActiveRoute();
    }

    private initFromActiveRoute(): void {
        this.activeRoute.data
            .pipe(this.takeUntilDestroyed())
            .subscribe(next => {
                this.onChangedParams(next);

            }, error => {
                LOG.error(error);
            });
    }

    private onChangedParams(next: Data): void {
        // Parameter laden
        this.modif = next.modif;
        this.erstTerminAdHoc = next.erstTerminAdHoc;
        this.selbstzahlende = next.selbstzahlende;

        if (next.data) {
            this.setDossier(next.data.dossier$);
            this.setImpfstoffe(next.data.impfstoffe$);
            this.odiList = next.data.odi$;
            this.setOdi();

            if (this.dashboardJax) {
                this.reinitializeFromDashboard(this.dashboardJax, false);
                this.impffolgeNr = TerminUtilService.determineImpffolgeNr(this.dashboardJax);
            }
        } else {
            this.errorService.addMesageAsError('IMPFDOK.ERROR.LOAD-DATA');
            this.accessOk = false;
        }
        this.formBaseData = {
            krankheitIdentifier: this.krankheitIdentifier,
            modif: this.modif,
            accessOk: this.accessOk,
            validate: this.validate,
            disable$: this.formDisabled$.asObservable(),
            saved$: this.saved$.asObservable(),
            validateTrigger$: this.validateTrigger$.asObservable(),
            dashboardJax: this.dashboardJax,
            impffolgeNr: this.impffolgeNr,
            impffolge: this.impffolge,
            impfstoffe: this.impfstoffe,
            odiList: this.odiList,
            canSelectGrundimmunisierung: false,
            defaultGrundimmunisierung: true,
            selbstzahlende: this.selbstzahlende,
        };
    }

    // Termine initialisieren und Validierung anstossen
    private reinitializeFromDashboard(dashboardJax: DashboardJaxTS, forceReload: boolean): void {

        this.terminmanager.reinitializeFromDashboard(dashboardJax, forceReload, this.impffolge, this);

        // nun da wir die Termine neu gesetzt haben koennen wir nochmal validieren
        if (this.canShowValidierungen()) {
            this.validateTrigger$.next();
        }
    }

    private canShowValidierungen(): boolean {
        return !this.modif && !this.formDisabled$.value;
    }

    validate = (dossier: DashboardJaxTS | undefined, impffolge: ImpffolgeTS,
                odiId: string | null, impfstoff: ImpfstoffJaxTS | undefined, odiList: OrtDerImpfungJaxTS[],
    ): void => {
        this.registrierungValidationService.validateTermineAndStatusAndOdiAndImpfstoffAndUserodi(
            this.krankheitIdentifier,
            dossier,
            impffolge,
            odiId,
            impfstoff,
            odiList,
        );
    };

    // ********** DIVERSE GETTER/SETTER ********************

    private setOdi(): void {
        this.ortDerImpfungId = this.terminmanager.identifyOdiIdToSelect(this.odiList, this.krankheitIdentifier);
    }

    selectOdi(odiId: string | null): void {
        this.ortDerImpfungId = odiId;
    }

    title(): string {
        if (this.impffolgeNr) {
            return this.translationService.instant('IMPFDOK.TITLE',
                {i: this.impffolgeNr});
        }
        return ''; // sonst wird Impfung 2 angezeigt, wenn Impfung 1 geoeffnet wird und im falschen Status ist ->
                   // impffolge undefined
    }

    private setImpfstoffe(data: ImpfstoffJaxTS[]): void {
        this.impfstoffe = data;
    }

    private setDossier(data: DashboardJaxTS): void {
        this.dashboardJax = data;
        if (!this.dashboardJax) {
            this.accessOk = false;
        } else {
            switch (this.dashboardJax.status) {
                case RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT:
                    this.impffolge = ImpffolgeTS.ERSTE_IMPFUNG;
                    this.accessOk = true;
                    break;
                case RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT:
                    this.impffolge = ImpffolgeTS.ZWEITE_IMPFUNG;
                    this.accessOk = true;
                    break;
                case RegistrierungStatusTS.IMPFUNG_1_DURCHGEFUEHRT:
                    // Direkt nach der ersten Impfung darf noch ein Zweit-Termin gebucht werden!
                    this.impffolge = ImpffolgeTS.ERSTE_IMPFUNG;
                    this.accessOk = true;
                    this.saved$.next(true);
                    this.disableForm();
                    break;
                case RegistrierungStatusTS.IMMUNISIERT:
                case RegistrierungStatusTS.FREIGEGEBEN_BOOSTER:
                case RegistrierungStatusTS.GEBUCHT_BOOSTER: {
                    // Neu wird die Seite auch nach dem Speichern der letzten GI angezeigt,
                    // damit direkt ein Folgetermin (Booster) gebucht werden kann
                    this.impffolge = ImpffolgeTS.BOOSTER_IMPFUNG;
                    // Die Impfdokumentation darf aber nicht mehr gespeicher werden
                    this.accessOk = false;
                    this.disableForm();
                    break;
                }
                default:
                    const errorMsg = this.translationService.instant('IMPFDOK.ERROR.WRONG-STATUS', {
                        dossierStatus: this.dashboardJax.status,
                        dossierKrankheit: this.dashboardJax.krankheitIdentifier,
                        urlKrankheit: this.krankheitIdentifier,
                    });
                    LOG.warn(errorMsg);
                    this.accessOk = false;
                    break;

            }

            // Wir nehmen den ODI was im Termin steht. Mit der Annahme, dass die Kontrolle das vorher validiert hat.
        }
    }

    private disableForm(): void {
        this.formDisabled$.next(true);
    }

    private needsOnlyOneDosisAndIsErsteImpfung(impfdokumentation: ImpfdokumentationJaxTS): boolean {
        return impfdokumentation.impfstoff?.anzahlDosenBenoetigt === 1 && this.impffolge === ImpffolgeTS.ERSTE_IMPFUNG;
    }

    private needsOnlyOneDosis(impfdokumentation: ImpfdokumentationJaxTS): boolean {
        return impfdokumentation.impfstoff?.anzahlDosenBenoetigt === 1;
    }

    // ********** SAVE ********************

    public canSave(): boolean {
        return !this.saved$;
    }

    save(formSubmission: ImpfdokumentationFormSubmission): void {
        const impfdokumentation = formSubmission.impfdokumentation;
        const impfstoff = impfdokumentation.impfstoff;
        if (this.registrierungValidationService.isAndererImpfstoff(this.dashboardJax, this.impffolge, impfstoff)) {
            this.registrierungValidationService.showConfirmPopupAndererImpfstoff().then(value => {
                if (value.isConfirmed) {
                    this.checkOdiAndProceedSave(formSubmission);
                }
            });
        } else {
            this.checkOdiAndProceedSave(formSubmission);
        }
    }

    private checkOdiAndProceedSave(formSubmission: ImpfdokumentationFormSubmission): void {
        const impfdokumentation = formSubmission.impfdokumentation;
        if (!!this.ortDerImpfungId) {
            this.saveImpfdokumentation(impfdokumentation);
        }
    }

    isNotSameODI(odiId: string | undefined): boolean {
        if (this.impffolge === ImpffolgeTS.BOOSTER_IMPFUNG) {
            return false;
        }
        return this.terminUtilService.isNotSameODI(this.impffolge, odiId, this.dashboardJax);
    }

    private saveImpfdokumentation(impfdokumentation: ImpfdokumentationJaxTS): void {
        // Wir merken uns, ob es ein Nachtrag war, damit wir nach dem Speichern richtig weiterfahren koennen
        const wasImpfungNachtragen = impfdokumentation.nachtraeglicheErfassung;
        // Bei Impfstoff mit nur 1 Dosis: Hinweisen, dass der zweite Termin nicht benoetigt wird
        this.confirmDosisCountAndSave(impfdokumentation, wasImpfungNachtragen, () => {
            this.doSave(impfdokumentation, wasImpfungNachtragen);
        });

    }

    private confirmDosisCountAndSave(
        impfdokumentation: ImpfdokumentationJaxTS,
        wasImpfungNachtragen: undefined | boolean,
        saveFunction: (
            impfdokumentation: ImpfdokumentationJaxTS,
            wasImpfungNachtragen:
                (boolean | undefined),
        ) => void,
    ): void {
        if (this.saveInProgress) {
            LOG.info('saveImpfdokumentation does nothing because save is already in progress');
            return;
        }
        this.saveInProgress = true;

        // Bei Impfstoff mit nur 1 Dosis: Hinweisen, dass der zweite Termin nicht benoetigt wird
        if (this.needsOnlyOneDosisAndIsErsteImpfung(impfdokumentation)) {
            Swal.fire({
                icon: 'info',
                text: this.translationService.instant('IMPFDOK.NUR_EINE_DOSIS.CONFIRM'),
                showCancelButton: true,
                showConfirmButton: true,
            }).then(r => {
                if (r.isConfirmed) {
                    saveFunction(impfdokumentation, wasImpfungNachtragen);
                } else {
                    this.saveInProgress = false;
                }
            });
        } else {
            saveFunction(impfdokumentation, wasImpfungNachtragen);
        }
    }

    private doSave(impfdokumentation: ImpfdokumentationJaxTS, wasImpfungNachtragen: undefined | boolean): void {
        this.impfdokumentationService.impfdokumentationResourceSaveImpfdokumentation(
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion,
            this.impffolge, this.ortDerImpfungId!, impfdokumentation)
            .pipe(
                finalize(() => this.saveInProgress = false),
            )
            .subscribe(value => this.onSaved(impfdokumentation, wasImpfungNachtragen),
                error => LOG.error(error));
    }

    private onSaved(impfdokumentation: ImpfdokumentationJaxTS, wasImpfungNachtragen: undefined | boolean): void {
        this.saved$.next(true);
        this.disableForm();
        this.impfdokumentationCacheService.cacheImpfdokumentation(impfdokumentation,
            this.ortDerImpfungId as string,
            this.krankheitIdentifier);
        this.onAfterSaved(impfdokumentation, wasImpfungNachtragen);
    }

    private onAfterSaved(
        impfdokumentation: ImpfdokumentationJaxTS,
        wasImpfungNachtragen: undefined | boolean,
    ): void {
        if (wasImpfungNachtragen === true
            && this.impffolge === ImpffolgeTS.ERSTE_IMPFUNG
            && !this.needsOnlyOneDosis(impfdokumentation)) {
            // Im Fall eines Nachtrags wird die Impfdoku erst nach der zweiten Impfung gedruckt.
            // Ausser: Es wurde ein Impfstoff verwendet, von dem es nur 1 Dosis braucht
            this.router.navigate([
                'dossier',
                this.dashboardJax?.registrierungsnummer,
                'krankheit',
                this.krankheitIdentifier,
            ]);
        } else {
            this.print();
            this.dossierService.dossierResourceGetImpfdossiersOverview(impfdokumentation.registrierungsnummer).subscribe(
                (res: ImpfdossiersOverviewJaxTS) => {
                    this.navigationService.navigate(res);
                },
                error => {
                    this.navigationService.notFoundResult();
                }
            );
        }
    }

    // ********** BACK ********************

    public back(): void {
        this.router.navigate(['']);
    }

    confirmAndResetToKontrolle(): void {
        Swal.fire({
            icon: 'question',
            text: this.translationService.instant('IMPFDOK.ZURUEK_ZU_KONTROLLE.CONFIRM'),
            showCancelButton: true,
            cancelButtonText: this.translationService.instant('IMPFDOK.ZURUEK_ZU_KONTROLLE.CONFIRM_CANCEL'),
            confirmButtonText: this.translationService.instant('IMPFDOK.ZURUEK_ZU_KONTROLLE.CONFIRM_ZURUEK'),
        }).then(r => {
            if (r.isConfirmed) {
                this.resetToKontrolle();
            }
        });
    }

    resetToKontrolle(): void {
        this.impfdokumentationService.impfdokumentationResourceImpfungVerweigert(
            this.krankheitIdentifier,
            this.dashboardJax?.registrierungsnummer as string)
            .subscribe(
                () => this.back(),
                error => LOG.error(error));
    }

    // ********** PRINT ********************

    print(): void {
        const registrierungsnummer = this.dashboardJax?.registrierungsnummer;
        if (registrierungsnummer) {
            this.downloadService.downloadResourceDownloadImpfdokumentation(this.krankheitIdentifier,
                registrierungsnummer)
                .subscribe(res => BlobUtil.openInNewTab(res, document), error => {
                    LOG.error('Could not download Impfdokumentation for registration ' + registrierungsnummer, error);
                });
        }
    }

    // ********** TERMINE UMBUCHEN ********************

    exitTerminumbuchung($event: string): void {
        if ($event && this.dashboardJax) {
            this.reinitializeFromDashboard(this.dashboardJax, true);
        }
        this.reloadPage(false, false, 'termine-anchor');
    }

    private hasNotSameImpfortSelectedAsInTerminfindungService(selectedOdi: OrtDerImpfungDisplayNameJaxTS | undefined): boolean {
        if (selectedOdi && this.terminfindungService
            && this.terminfindungService.selectedSlot2
            && this.terminfindungService.selectedSlot2?.ortDerImpfung) {

            return selectedOdi.id !== this.terminfindungService.selectedSlot2?.ortDerImpfung.id;
        }
        return false;
    }

    private hasTermin1NotInOdi(selectedOdi: OrtDerImpfungDisplayNameJaxTS | undefined): boolean {
        if (selectedOdi && this.terminfindungService
            && this.terminfindungService.selectedSlot1 && this.terminfindungService.selectedSlot1?.ortDerImpfung) {
            return selectedOdi.id !== this.terminfindungService.selectedSlot1?.ortDerImpfung.id;

        }
        return false;
    }

    // Termin 1 heute erstellen und zu Termin 2-Wahl gehen
    public onAdHocTermin1AndSelectTermin2(): void {
        // In der Impfdokumentation erstellen wir einen AdHoc Termin immer in dem ODI,
        // welches vom Benutzer ausgewaehlt wurde
        const gutesOdi = this.getSelectedOdi();
        if (gutesOdi) {
            this.terminfindungService.ortDerImpfung = gutesOdi;

            // Termin 2 in anderem Odi? Slot 2 resetten
            if (this.hasNotSameImpfortSelectedAsInTerminfindungService(gutesOdi)) {
                this.terminfindungService.selectedSlot2 = undefined;
            }
            // todo homa  vacme-441 fix duplicate

            this.terminmanager.setupAdhocSlot1Unsaved();

            // Falls Slot 2 gewaehlt und im korrekten Abstand
            if (this.terminfindungService.hasSelectedSlot2WithCorrectAbstand()) {
                // Wir muessen nicht navigieren weil der Abstand noch passt
                if (this.terminfindungService.dashboard.registrierungsnummer) {
                    // AdHoc-Termin speichern
                    this.updateAppointmentAdHocTermin(this.terminfindungService.dashboard.registrierungsnummer);
                }
                return;
            }
        }
        // Wir muessen navigieren: Es gibt entweder keinen Termin2 oder er passt nicht mehr vom Datum her
        this.erstTerminAdHoc = true;
        this.reloadPage(true, true, 'terminbuchung-anchor');
        this.setOrtDerImpfungInTerminfindungService();
    }

    public isUserBerechtigtForOdiOfTermin(): boolean {
        switch (this.impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                return this.isUserBerechtigtForOdiOfTermin1or2orN(this.dashboardJax?.termin1);
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                return this.isUserBerechtigtForOdiOfTermin1or2orN(this.dashboardJax?.termin2);
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                return this.isUserBerechtigtForOdiOfTermin1or2orN(this.dashboardJax?.terminNPending);
        }
    }

    private isUserBerechtigtForOdiOfTermin1or2orN(termin?: ImpfterminJaxTS): boolean {
        if (!termin) {
            return true;
        }
        // Wenn ODI in meiner Liste vorhanden -> berechtigt
        return !!this.findOdiByIdFromList(termin.impfslot?.ortDerImpfung?.id);
    }

    private getSelectedOdi(): OrtDerImpfungDisplayNameJaxTS | undefined {
        return this.odiList?.find((k: OrtDerImpfungDisplayNameJaxTS) => k.id === this.ortDerImpfungId);
    }

    private findOdiByIdFromList(odiIdToLookFor: string | undefined): OrtDerImpfungDisplayNameJaxTS | undefined {
        return this.odiList?.find(value => value.id === odiIdToLookFor);
    }

    private setOrtDerImpfungInTerminfindungService(): void {
        if (this.ortDerImpfungId !== null && this.ortDerImpfungId !== undefined) {
            this.terminfindungService.ortDerImpfung = this.findOdiByIdFromList(this.ortDerImpfungId);
        }
    }

    public termineUmbuchen(savedVal: boolean | null): void {
        this.terminmanager.canProceedTermineUmbuchen$(this.isUserBerechtigtForOdiOfTermin())
            .subscribe(canProceed => {
                if (canProceed) {
                    if (!!savedVal) {
                        // Nach dem Speichern der Grundimmunisierung laden wir das Dossier nicht neu
                        // und haben daher noch die Impffolge der Grundimmunisierung. Damit man direkt
                        // einen Booster-Termin buchen kann, muss die Impffolge auf BOOSTER gesetzt werden
                        this.impffolge = ImpffolgeTS.BOOSTER_IMPFUNG;
                    }
                    this.reloadPage(true, false, 'terminbuchung-anchor');
                    this.setOrtDerImpfungInTerminfindungService();
                }
            }, error => {
                LOG.error(error);
            });
    }

    private reloadPage(modifFlag: boolean, erstTerminAdHocFlag: boolean,
                       anchor: 'terminbuchung-anchor' | 'buttons-anchor' | 'termine-anchor',
    ): void {
        this.router.navigate(
            [],
            {
                relativeTo: this.activeRoute,
                queryParams: {modif: modifFlag, erstTerminAdHoc: erstTerminAdHocFlag},
                fragment: anchor,
            });
        // sollte die component OnPush sein, muesste hier im Anschluss noch this.cdRef.detectChanges() gemacht werden
    }

    public updateAppointment(registrierungsNummer: string): void {
        if (this.erstTerminAdHoc) {
            this.updateAppointmentAdHocTermin(registrierungsNummer);
        } else {
            this.updateAppointmentNonAdhocTermin(registrierungsNummer);
        }
    }

    public updateAppointmentAdHocTermin(registrierungsnummer: string): void {
        this.terminmanager.createAdhocTermin1RequestPut$(registrierungsnummer)
            .subscribe(resultDashboard => {
                this.erstTerminAdHoc = false;
                this.dashboardJax = resultDashboard;
                this.reinitializeFromDashboard(resultDashboard, true);
                this.exitTerminumbuchung(registrierungsnummer);
            }, (error => {
                LOG.error(error);
            }));
    }

    private updateAppointmentNonAdhocTermin(registrierungsNummer: string): void {
        this.terminmanager.umbuchungRequestPut$(this.impffolge, registrierungsNummer, this.krankheitIdentifier)
            .subscribe(resultDashboard => {
                this.dashboardJax = resultDashboard;
                this.reinitializeFromDashboard(resultDashboard, true);
                this.exitTerminumbuchung(registrierungsNummer);
            }, (error => {
                LOG.error(error);
            }));
    }

    public cancelAppointment($event: string): void {
        this.terminmanager.confirmTerminAbsagen(this.impffolge).then(r => {
            if (r.isConfirmed) {
                this.terminbuchungService
                    .terminbuchungResourceTerminAbsagen(this.krankheitIdentifier, $event)
                    .subscribe(resultDashboard => {
                        this.dashboardJax = resultDashboard;
                        this.reinitializeFromDashboard(resultDashboard, true);
                        this.exitTerminumbuchung($event);
                    }, (error => {
                        LOG.error(error);
                    }));
            }
        });
    }

    public gotoNextFreienTermin($event: any): void {
        this.terminmanager.gotoNextFreienTermin('impfdokumentation', $event, this.krankheitIdentifier);
    }

    public showTerminumbuchungButtons(): boolean {
        if (this.saved$.value && this.dashboardJax?.vollstaendigerImpfschutz) {
            return false;
        }
        const selectedOdi = this.getSelectedOdi();
        if (selectedOdi) {
            return !!selectedOdi.terminverwaltung;
        }
        return true;
    }

    public supportsImpffolgenEinsUndZwei(): boolean {
        if (this.dashboardJax?.krankheitIdentifier) {
            return this.vacmeSettingsService.supportsImpffolgenEinsUndZwei(this.dashboardJax?.krankheitIdentifier);
        }
        return false;
    }

    public getPersonFolgeterminTS(dashboard: DashboardJaxTS): TSPersonFolgetermin {
        return fromDashboard(dashboard);
    }
}
