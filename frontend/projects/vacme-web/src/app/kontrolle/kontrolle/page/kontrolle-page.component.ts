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

import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, forwardRef, OnInit, ViewChild} from '@angular/core';
import {NG_VALUE_ACCESSOR, UntypedFormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {FileSaverService} from 'ngx-filesaver';
import {Observable, of} from 'rxjs';
import {first} from 'rxjs/operators';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import {
    DashboardJaxTS,
    DossierService,
    FileInfoJaxTS,
    ImpfdokumentationService,
    ImpffolgeTS,
    ImpfkontrolleJaxTS,
    KontrolleService,
    KrankheitIdentifierTS,
    OrtDerImpfungDisplayNameJaxTS,
    OrtderimpfungService,
    PrioritaetTS,
    RegistrierungsEingangTS,
    RegistrierungStatusTS,
    StammdatenService,
    TerminbuchungService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent, NextFreierTerminSearch,} from '../../../../../../vacme-web-shared';
import IPersonalienTS from '../../../../../../vacme-web-shared/src/lib/model/IPersonalienTS';
import {BlobRestService} from '../../../../../../vacme-web-shared/src/lib/service/blob-rest.service';
import {ErrorMessageService} from '../../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {TerminUtilService} from '../../../../../../vacme-web-shared/src/lib/service/termin-util.service';
import {TerminfindungService} from '../../../../../../vacme-web-shared/src/lib/service/terminfindung.service';
import {VacmeSettingsService} from '../../../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import {BoosterUtil} from '../../../../../../vacme-web-shared/src/lib/util/booster-util';
/* eslint-disable max-len */
import {RegistrierungValidationService} from '../../../service/registrierung-validation.service';
import {TerminmanagementKontrolleImpfdokService} from '../../../service/terminmanagement-kontrolle-impfdok.service';
import {KontrolleUploadComponent} from '../upload/kontrolle-upload.component';
import {KontrolleFormComponent} from './../kontrolle-form/kontrolle-form.component';

const LOG = LogFactory.createLog('KontrolleComponent');

@Component({
    selector: 'app-kontrolle-page',
    templateUrl: './kontrolle-page.component.html',
    styleUrls: ['./kontrolle-page.component.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            multi: true,
            useExisting: forwardRef(() => KontrollePageComponent),
        },
    ],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KontrollePageComponent extends BaseDestroyableComponent implements OnInit {

    krankheit: KrankheitIdentifierTS | undefined;

    public impfkontrolle?: ImpfkontrolleJaxTS;
    public dashboardJax: DashboardJaxTS | undefined;
    public newPerson?: boolean;
    public impffolge: ImpffolgeTS = ImpffolgeTS.ERSTE_IMPFUNG;
    public regNummer: string | undefined;
    public canSave = true; // Es darf nur einmal gespeichert werden

    @ViewChild(KontrolleFormComponent) kontrolleForm!: KontrolleFormComponent;

    public boosterUtil = BoosterUtil;

    @ViewChild(KontrolleUploadComponent) uploadComponent!: KontrolleUploadComponent;
    public uploadedFiles: FileInfoJaxTS[] = [];

    public personalien: IPersonalienTS = {};

    public selectedOdiId: string | undefined;
    public ortDerImpfungList: OrtDerImpfungDisplayNameJaxTS[] = [];

    /**
     * MODIF: Termine umbuchen
     * - Wieso keine separate Page dafuer? Weil bei ad hoc noch keine RegNummer?
     * - Wie funktioniert es?
     *      -- Zum Switchen machen wir reloadPage mit Param modif=true/false
     *      -- this.activeRoute.data.pipe().subscribe reagiert auf Aenderungen an den Params
     */
    public modif = false; // modif heisst, Terminumbuchung offen.
    public erstTerminAdHoc = false;

    // ********** INIT ********************

    constructor(
        private fb: UntypedFormBuilder,
        private router: Router,
        private activeRoute: ActivatedRoute,
        private errorService: ErrorMessageService,
        private stammdatenService: StammdatenService,
        private kontrolleService: KontrolleService,
        private errorMessageService: ErrorMessageService,
        private blobService: BlobRestService,
        private cdRef: ChangeDetectorRef,
        private filesaver: FileSaverService,
        private translationService: TranslateService,
        private datePipe: DatePipe,
        private registrierungValidationService: RegistrierungValidationService,
        private terminbuchungService: TerminbuchungService,
        private ortDerImpfungService: OrtderimpfungService,
        private terminfindungService: TerminfindungService,
        private terminUtilService: TerminUtilService,
        private impfdokumentationService: ImpfdokumentationService,
        private terminmanager: TerminmanagementKontrolleImpfdokService,
        private dossierService: DossierService,
        public vacmeSettingsService: VacmeSettingsService,
    ) {
        super();
    }

    ngOnInit(): void {
        this.initFromActiveRoute();
    }

    private initFromActiveRoute(): void {
        this.activeRoute.data
            .pipe(this.takeUntilDestroyed())
            .pipe().subscribe(next => {
            // Fuer Parameter siehe app-routing

            this.onChangedParams(
                next.modif,
                next.erstTerminAdHoc,
                next.krankheit,
                next.impfkontrolle,
                next.ortDerImpfungList,
                next.data?.fileInfo$,
                next.data?.dashboard$,
            );

        }, error => {
            LOG.error(error);
        });
    }

    private loadDashboard$(regNummer: string | undefined): Observable<DashboardJaxTS | undefined> {
        if (regNummer) {
            return this.dossierService.dossierResourceGetDashboardImpfdossier(this.getKrankheit(), regNummer);
        } else {
            return of(undefined);
        }
    }

    public getKrankheit(): KrankheitIdentifierTS{
        if (this.krankheit === undefined || this.krankheit === null) {
            this.errorService.addMesageAsError('KRANKHEIT NICHT GESETZT');
            throw new Error('Krankheit nicht gesetzt ' + this.krankheit);
        }
        return this.krankheit;
    }

    private onChangedParams(
        modif: boolean,
        erstTerminAdHoc: boolean,
        krankheit: KrankheitIdentifierTS,
        impfkontrolle?: ImpfkontrolleJaxTS,
        ortDerImpfungList?: any,
        fileInfo$?: any,
        dashboard?: DashboardJaxTS,
    ): void {

        this.modif = modif;
        this.erstTerminAdHoc = erstTerminAdHoc;
        this.ortDerImpfungList = ortDerImpfungList;
        this.krankheit = krankheit;

        if (!!impfkontrolle) {
            // Impfkontrolle nur setzen, wenn nicht null, denn in den changed params kann sie null sein, obwohl wir
            // schon eine haben
            this.impfkontrolle = impfkontrolle;

            // Bestehende Registrierung
            this.newPerson = false;
            this.regNummer = this.impfkontrolle.registrierungsnummer;

            // Formular mit ImpfkontrolleJax ausfuellen, falls beide schon da sind
            if (this.kontrolleForm) {
                this.onFormLoaded();
            }

            this.decideImpffolgeAndDisableForm(this.impfkontrolle.status);
            this.cdRef.detectChanges();
        } else {
            // neue Person
            this.newPerson = true;
        }

        // Uploads laden
        if (fileInfo$) {
            this.setFileInfo(fileInfo$);
        }

        // Dashboard laden
        if (dashboard) {
            this.dashboardJax = dashboard;

            // Personalien fuer PersonInfos und PersonDocuments aus Impfkontrolle und Dashboard (wieso beide
            // noetig?)
            if (this.impfkontrolle) {
                this.preparePersonalien(this.impfkontrolle, this.regNummer, this.impfkontrolle.prioritaet,
                    this.impfkontrolle.status, this.dashboardJax);
                this.cdRef.detectChanges();
            }

            // Terminmanager und Terminfindungsservice anhand des Dashboards initialisieren
            this.reinitializeFromDashboard(this.dashboardJax, false);

        }

        // Warnungen ausgeben, wenn Prio oder Termin nicht korrekt sind
        if (this.canShowValidierungen()) {
            this.registrierungValidationService.validateTermineAndStatusAndUserodi(
                this.getKrankheit(), this.dashboardJax, this.impffolge, this.ortDerImpfungList,
            );
        }

        this.cdRef.detectChanges();
    }

    private canShowValidierungen(): boolean {
        return !this.modif && this.newPerson === false && this.canSave;
    }

    public onFormLoaded(): void {
        // Bisher wurde hier nochmals this.initFromActiveRoute() gemacht, aber das machte eigentlich viel zu viel.
        if (this.impfkontrolle) {
            this.kontrolleForm.patchValue(this.impfkontrolle);
            this.kontrolleForm.disableFragebogenFields();

            //falls AGB schon akzeptiert, koennen wir sie disablen
            if (!this.impfkontrolle.leistungerbringerAgbConfirmationNeeded) {
                this.kontrolleForm.setAndDisableAgbAndEinwilligung();
            }

            // Bei Eingang ONLINE kann Telefon nicht geaendert werden
            if (this.dashboardJax?.eingang === RegistrierungsEingangTS.ONLINE_REGISTRATION) {
                this.kontrolleForm.disablePhoneNumber();
            }

            // nochmal decideImpffolgeAndDisableForm, weil das Formular gegebenenfalls komplett disabled werden muss
            this.decideImpffolgeAndDisableForm(this.impfkontrolle.status);
        }
    }

    // ********** DIVERSE GETTER ********************

    title(): string {
        if (this.newPerson === undefined) {
            return ''; // bis alles geladen wurde
        }
        const translatedKrankheit = this.translationService.instant('KRANKHEITEN.' + this.krankheit);
        if (this.newPerson) {
            return this.translationService.instant('FACH-APP.KONTROLLE.TITLE-NEW') + ' ' + translatedKrankheit;
        }
        return this.translationService.instant('FACH-APP.KONTROLLE.TITLE',
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            {i: this.impfkontrolle!.impffolgeNr + ' ' + translatedKrankheit});
    }

    private decideImpffolgeAndDisableForm(status?: RegistrierungStatusTS): void {
        if (!status) {
            LOG.error('Status der Registrierung nicht gesetzt');
            return;
        }
        switch (status) {
            case RegistrierungStatusTS.REGISTRIERT:
            case RegistrierungStatusTS.FREIGEGEBEN:
            case RegistrierungStatusTS.ODI_GEWAEHLT:
            case RegistrierungStatusTS.GEBUCHT:
                // Kontrolle 1
                this.impffolge = ImpffolgeTS.ERSTE_IMPFUNG;
                this.canSave = true;
                break;
            case RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT:
                // nach Kontrolle 1
                // nach der ersten Kontrolle soll man Termine umbuchen koennen, aber das Formular nicht mehr bearbeiten
                this.impffolge = ImpffolgeTS.ERSTE_IMPFUNG;
                this.canSave = false;
                this.kontrolleForm?.disableAllFields();
                break;
            case RegistrierungStatusTS.IMPFUNG_1_DURCHGEFUEHRT:
                // Kontrolle 2
                this.impffolge = ImpffolgeTS.ZWEITE_IMPFUNG;
                this.canSave = true;
                break;
            case RegistrierungStatusTS.FREIGEGEBEN_BOOSTER:
            case RegistrierungStatusTS.GEBUCHT_BOOSTER:
            case RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER:
            // Folgende Status sind drin, da evtl. der Batchjob noch nicht durchgelaufen ist
            // eslint-disable-next-line no-fallthrough
            case RegistrierungStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG:
            case RegistrierungStatusTS.ABGESCHLOSSEN:
            case RegistrierungStatusTS.IMMUNISIERT:
                // Kontrolle N
                this.impffolge = ImpffolgeTS.BOOSTER_IMPFUNG;
                this.canSave = true;
                break;
            case RegistrierungStatusTS.KONTROLLIERT_BOOSTER:
                // nach Kontrolle N
                this.impffolge = ImpffolgeTS.BOOSTER_IMPFUNG;
                this.canSave = false;
                this.kontrolleForm?.disableAllFields();
                break;

            default:
                LOG.warn('Status bei Kontrolle unerwartet' + status);
                this.canSave = false;
                this.kontrolleForm?.disableAllFields();
                break;
        }
    }

    private preparePersonalien(
        impfkont?: ImpfkontrolleJaxTS,
        regNummer?: string,
        prio?: PrioritaetTS,
        status?: RegistrierungStatusTS,
        dashboardJax?: DashboardJaxTS,
    ): void {
        // wegen on push immer ein neues machen wenn wir was anpassen
        this.personalien = {
            name: impfkont?.name,
            vorname: impfkont?.vorname,
            status,
            registrierungsnummer: regNummer,
            geburtsdatum: impfkont?.geburtsdatum,
            prioritaet: prio,
            eingang: impfkont?.eingang,
            hasBenutzer: dashboardJax?.hasBenutzer,
            bemerkungenRegistrierung: dashboardJax?.bemerkungenRegistrierung,
            kommentare: dashboardJax?.kommentare,
            impfdossier: dashboardJax?.impfdossier,
        };
    }

    // hinzufuegen / editieren nur moeglich, solange keine Vacme-Impfung existiert.
    public showExternGeimpft(): boolean {
        if (!this.vacmeSettingsService.supportsExternesZertifikat(this.getKrankheit())) {
            return false;
        }
        const dashboard = this.dashboardJax;
        if (!dashboard) {
            if (this.newPerson) {
                return true; // bei ad hoc ist das dashboard null -> man darf ein externGeimpft erstellen
            }
            // Model ist evlt. da aber noch nicht geladen...
            return false;
        }
        const hasVacmeImpfung = BoosterUtil.hasDashboardVacmeImpfung(dashboard);
        return !hasVacmeImpfung; // hinzufuegen nur moeglich, wenn noch keine Vacme-Impfung besteht.
    }


    // ********** SAVE ********************
    postsave(): void {
        this.kontrolleForm.disableAllFields();
        this.cdRef.detectChanges();
    }

    save(): void {
        if (!this.canSave) {
            return;
        }
        this.canSave = false;
        const data = this.kontrolleForm.presave();
        const wasNewPerson = this.newPerson;

        // Speichern
        const saveRequest$ = wasNewPerson
            ? this.kontrolleService.impfkontrolleResourceRegistrieren(data)
            : this.kontrolleService.impfkontrolleResourceKontrolleOk(this.impffolge, data);
        saveRequest$.subscribe((reloadedImpfkontrolle: ImpfkontrolleJaxTS) => {

            // Alles neu machen (Impffolge kann sich geaendert haben etc.)
            this.loadDashboard$(reloadedImpfkontrolle.registrierungsnummer).pipe(first()).subscribe(dashboard => {
                if (dashboard !== undefined) {
                    this.dashboardJax = dashboard;
                }

                // Seite neu laden
                this.onChangedParams(
                    false,
                    this.erstTerminAdHoc,
                    this.getKrankheit(),
                    reloadedImpfkontrolle,
                    this.ortDerImpfungList,
                    undefined,
                    dashboard);

                // Uploads erst jetzt schicken, wo wir eine Registrierungsnummer haben (nach onChangedParams)
                if (wasNewPerson && this.vacmeSettingsService.supportsDossierFileUpload(this.getKrankheit())) {
                    this.uploadComponent.uploadItsPreparedFiles();
                }
            }, error => LOG.error(error));

        }, err => {
            LOG.error(err);
            this.canSave = true; // Im Fehlerfall wieder enablen
        });
    }

    saveFalschePerson(): void {
        if (!this.canSave) {
            return;
        }
        this.canSave = false;
        const data = this.kontrolleForm.presave();
        this.kontrolleService.impfkontrolleResourceSaveKontrolleNoProceed(this.impffolge, data)
            .subscribe(() => {
                this.postsave();
                this.router.navigate(['startseite']);
            }, err => {
                LOG.error(err);
                this.canSave = true; // Im Fehlerfall wieder enablen
            });
    }

    // ********** BACK ********************

    back(): void {
        this.router.navigate(['startseite']);
    }


    // ********** UPLOADS ********************

    private setFileInfo(fileInfos: Array<FileInfoJaxTS>): void {
        if (fileInfos && fileInfos.length >= 0) {
            // Das Array zuerst clearen, damit sich die Files nicht ansammeln
            this.uploadedFiles = [];
            this.uploadedFiles = this.uploadedFiles.concat(fileInfos);
        }
    }

    // ********** TERMINE UMBUCHEN ********************

    exitTerminumbuchung(registrierungsNummer: string): void {
        if (registrierungsNummer && this.dashboardJax) {
            this.reinitializeFromDashboard(this.dashboardJax, true);
        }
        this.reloadPage(false, false, 'termine-anchor');
    }

    // Termin 1 heute erstellen und zu Termin 2-Wahl gehen
    public onAdHocTermin1AndSelectTermin2(): void {
        // Auf der Kontrolle haben wir das ODI nicht. Falls der eingeloggte Benutzer nur fuer
        // ein ODI berechtigt ist, koennen wir davon ausgehen, dass der AdHoc Termin in diesem
        // ODI erstellt werden soll. Ansonsten muss auf die Terminbuchung navigiert werden.
        const gutesOdi = this.getSingleOrtDerImpfungOfCurrentUser();
        if (gutesOdi) {
            this.terminfindungService.ortDerImpfung = gutesOdi;

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

    private getSingleOrtDerImpfungOfCurrentUser(): OrtDerImpfungDisplayNameJaxTS | undefined {
        if (this.ortDerImpfungList.length === 1) {
            return this.ortDerImpfungList[0];
        }
        return undefined;
    }

    public isUserBerechtigtForOdiOfTermin(): boolean {
        if (this.pendingTerminIsUndefined()) {
            return true;
        }
        const found = this.findOdiByIdFromList(this.terminfindungService.ortDerImpfung?.id);
        return !!found;
    }
    private pendingTerminIsUndefined(): boolean {
        return ((this.impffolge === ImpffolgeTS.ERSTE_IMPFUNG && !this.impfkontrolle?.termin1)
            || (this.impffolge === ImpffolgeTS.ZWEITE_IMPFUNG && !this.impfkontrolle?.termin2)
            || (this.impffolge === ImpffolgeTS.BOOSTER_IMPFUNG && !this.impfkontrolle?.terminNPending));
    }

    private findOdiByIdFromList(odiIdToLookFor: string | undefined): OrtDerImpfungDisplayNameJaxTS | undefined {
        return this.ortDerImpfungList.find(value => value.id === odiIdToLookFor);
    }

    private setOrtDerImpfungInTerminfindungService(): void {
        // wir suchen in der Kontrolle den single ort der Impfung und wenn wir keinen eindeutigen haben dann
        // versuchen wir den aus dem Termin1 zu nehmen (nur fuer T1 kann man ad-hoc Termin machen)
        const singleOrtDerImpfungOfCurrentUser = this.getSingleOrtDerImpfungOfCurrentUser();
        if (singleOrtDerImpfungOfCurrentUser === undefined) { // wir haben keinen einduetigen aber vielliecht haben wir mehrere
            const odiFromTermin = this.findOdiIfAllowed(this.getSelectedTerminOdiId());
            this.terminfindungService.ortDerImpfung = odiFromTermin;
        } else {
            this.terminfindungService.ortDerImpfung = singleOrtDerImpfungOfCurrentUser;
        }

        // wir entfernen den odi wenn er keine Terminverwaltung hat weil er dann nicht im dropdown ist
        if (this.terminfindungService.ortDerImpfung?.terminverwaltung === false) {
            this.terminfindungService.ortDerImpfung = undefined;
        }

    }

    private getSelectedTerminOdiId(): string | undefined {
        switch (this.impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                return this.terminfindungService.selectedSlot1?.ortDerImpfung?.id;
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                return this.terminfindungService.selectedSlot2?.ortDerImpfung?.id;
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                return this.terminfindungService.selectedSlotN?.ortDerImpfung?.id;
        }
    }

    public canUmbuchen(): boolean {
        // In der Kontrolle ist das ODI nicht zwingend definiert -> habe ich ueberhaupt ein ODI mit Terminverwaltung?
        if (!this.hasAtLeastOneOdiWithTerminverwaltung()) {
            return false;
        }

        // Abgeschlossen kann man keine Boostertermine buchen, also auch nicht umbuchen da nicht freigegeben
        if (RegistrierungStatusTS.ABGESCHLOSSEN === this.dashboardJax?.status) {
            return false;
        }
        return true;

    }

    public termineUmbuchen(): void {
        this.terminmanager.canProceedTermineUmbuchen$(this.isUserBerechtigtForOdiOfTermin())
            .subscribe(canProceed => {
                if (canProceed) {
                    this.reloadPage(true, false, 'terminbuchung-anchor');
                    this.setOrtDerImpfungInTerminfindungService();
                }
            }, error => {
                LOG.error(error);
            });
    }

    // Auf der gleichen Seite bleiben, aber onChangedParams triggern und scrollen
    private reloadPage(
        modifFlag: boolean,
        erstTerminAdHocFlag: boolean,
        anchor: 'terminbuchung-anchor' | 'buttons-anchor' | 'termine-anchor',
    ): void {
        this.router.navigate(
            [],
            {
                relativeTo: this.activeRoute,
                queryParams: {modif: modifFlag, erstTerminAdHoc: erstTerminAdHocFlag},
                fragment: anchor,
            }).then(() => this.cdRef.detectChanges());
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
                this.updateFromDashbaord(resultDashboard);
                this.exitTerminumbuchung(registrierungsnummer);
            }, (error => {
                LOG.error(error);
            }));
    }

    private updateAppointmentNonAdhocTermin(registrierungsNummer: string): void {
        this.terminmanager.umbuchungRequestPut$(this.impffolge, registrierungsNummer, this.getKrankheit())
            .subscribe(resultDashboard => {
                this.updateFromDashbaord(resultDashboard);
                this.exitTerminumbuchung(registrierungsNummer);
            }, (error => {
                LOG.error(error);
            }));
    }

    public cancelAppointment($event: string): void {
        this.terminmanager.confirmTerminAbsagen(this.impffolge).then(r => {
            if (r.isConfirmed) {
                this.terminbuchungService
                    .terminbuchungResourceTerminAbsagen(this.getKrankheit(), $event)
                    .subscribe(resultDashboard => {
                        this.updateFromDashbaord(resultDashboard);
                        this.exitTerminumbuchung($event);
                    }, (error => {
                        LOG.error(error);
                    }));
            }
        });
    }

    // Dashboard setzen und Termine initialisieren
    private reinitializeFromDashboard(dashboardJax: DashboardJaxTS, forceReload: boolean): void {
        this.dashboardJax = dashboardJax;

        this.terminmanager.reinitializeFromDashboard(dashboardJax, forceReload, this.impffolge, this);

        this.selectedOdiId = this.terminfindungService.ortDerImpfung?.id;
    }

    public gotoNextFreienTermin($event: NextFreierTerminSearch): void {
        this.terminmanager.gotoNextFreienTermin('kontrolle', $event, this.getKrankheit());
    }

    private updateFromDashbaord(resultDashboard: DashboardJaxTS): void {
        if (resultDashboard && this.impfkontrolle) {
            this.impfkontrolle.status = resultDashboard.status;
            this.impfkontrolle.termin1 = resultDashboard.termin1;
            this.impfkontrolle.termin2 = resultDashboard.termin2;
            this.impfkontrolle.terminNPending = resultDashboard.terminNPending;
            this.reinitializeFromDashboard(resultDashboard, true);
        }
    }

    private findOdiIfAllowed(odiId?: string): OrtDerImpfungDisplayNameJaxTS | undefined {
        if (odiId === undefined || odiId === null) {
            return undefined;
        }
        const allowedOdiId: string | undefined =
            this.ortDerImpfungList.map(odi => odi.id).find(currOdiId => currOdiId === odiId);
        if (allowedOdiId !== undefined) {
            return this.findOdiByIdFromList(allowedOdiId);
        }
        return undefined;
    }

    public hasAtLeastOneOdiWithTerminverwaltung(): boolean {
        return this.ortDerImpfungList.some(value => value.terminverwaltung);
    }

    // ********** WEITER ZUM IMPFEN ********************

    public navigateToImpfdokumentation(): void {
        if (this.impffolge === ImpffolgeTS.BOOSTER_IMPFUNG) {
            this.router.navigate(['person', this.regNummer, 'impfdokumentation', 'booster', this.krankheit]);
        } else {
            this.router.navigate(['person', this.regNummer, 'impfdokumentation', this.krankheit]);
        }
    }

    public supportsImpffolgenEinsUndZwei(): boolean {
        if (this.dashboardJax?.krankheitIdentifier) {
            return this.vacmeSettingsService.supportsImpffolgenEinsUndZwei(this.dashboardJax?.krankheitIdentifier);
        }
        return false;
    }
}
