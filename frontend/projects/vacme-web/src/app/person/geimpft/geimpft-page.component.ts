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

import {DatePipe, DOCUMENT} from '@angular/common';
import {Component, Inject, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {FileSaverService} from 'ngx-filesaver';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    DashboardJaxTS,
    DossierService,
    DownloadService,
    FileInfoJaxTS,
    GeimpftService,
    ImpfdossierSummaryJaxTS,
    ImpffolgeTS,
    KontrolleService,
    KrankheitIdentifierTS,
    ZertifikatJaxTS,
    ZertifikatService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../../vacme-web-shared';
import {TSRole} from '../../../../../vacme-web-shared/src/lib/model';
import TSPersonFolgetermin from '../../../../../vacme-web-shared/src/lib/model/TSPersonFolgetermin';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {ErrorMessageService} from '../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {VacmeSettingsService} from '../../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import {BlobUtil} from '../../../../../vacme-web-shared/src/lib/util/BlobUtil';
import {BoosterUtil} from '../../../../../vacme-web-shared/src/lib/util/booster-util';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import {ExternGeimpftUtil} from '../../../../../vacme-web-shared/src/lib/util/externgeimpft-util';

import {
    fromDashboard,
    fromImpfdossierSummary,
} from '../../../../../vacme-web-shared/src/lib/util/person-folgetermin-util';
import {isAnyStatusOfBooster} from '../../../../../vacme-web-shared/src/lib/util/registrierung-status-utils';
import {NavigationService} from '../../service/navigation.service';
import {TerminmanagementKontrolleImpfdokService} from '../../service/terminmanagement-kontrolle-impfdok.service';

const LOG = LogFactory.createLog('ImpfdokumentationComponent');

@Component({
    selector: 'app-geimpft-page',
    templateUrl: './geimpft-page.component.html',
    styleUrls: ['./geimpft-page.component.scss'],
})
export class GeimpftPageComponent extends BaseDestroyableComponent implements OnInit {

    public accessOk?: boolean; // zuerst undefined, dann true oder false
    public hasNoPendingZertifikatGeneration = false;
    public hasZertifikatAPIToken = false;
    public elektronischerAusweisGroup!: UntypedFormGroup;
    public abgleichElektronischerImpfausweis = false;
    public isZertifikatEnabled = false;

    public uploadedFiles: FileInfoJaxTS[] = [];

    public dashboardCovidJax?: DashboardJaxTS;
    public zertifikatList?: ZertifikatJaxTS[];
    public hasMoreZertifikate = false;
    public nonCovidDossierSummaries?: ImpfdossierSummaryJaxTS[] = [];
    public krankheitenForUser?: Set<KrankheitIdentifierTS>;
    public krankheitCovid = KrankheitIdentifierTS.COVID;

    constructor(
        private router: Router,
        private fb: UntypedFormBuilder,
        private activeRoute: ActivatedRoute,
        private errorService: ErrorMessageService,
        private downloadService: DownloadService,
        private translationService: TranslateService,
        private geimpftService: GeimpftService,
        private authService: AuthServiceRsService,
        private datePipe: DatePipe,
        private kontrolleService: KontrolleService,
        private filesaver: FileSaverService,
        private zertifikatService: ZertifikatService,
        private dossierService: DossierService,
        private vacmeSettingsService: VacmeSettingsService,
        private navigationService: NavigationService,
        private terminmanager: TerminmanagementKontrolleImpfdokService,
        @Inject(DOCUMENT) private document: Document,
    ) {
        super();
    }

    ngOnInit(): void {
        this.dossierService.dossierResourceIsZertifikatEnabled().subscribe(
            response => {
                this.isZertifikatEnabled = response;
            },
            error => LOG.error(error));
        this.initUI();
        this.initFromActiveRoute();

    }

    private initUI(): void {
        this.elektronischerAusweisGroup = this.fb.group({
            elektronischerImpfausweis: this.fb.control(undefined, Validators.requiredTrue),
        });
    }

    hasRoleDokumentation(): boolean {
        return this.authService.isOneOfRoles(
            [TSRole.OI_DOKUMENTATION, TSRole.KT_NACHDOKUMENTATION, TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION]);
    }

    private initFromActiveRoute(): void {
        this.activeRoute.data
            .pipe(this.takeUntilDestroyed())
            .subscribe(next => {
                if (next.data) {
                    if (next.data.dashboardCovid$) {
                        this.dashboardCovidJax = next.data.dashboardCovid$;
                        this.accessOk = true;

                        this.loadZertifikatList();
                    }
                    if (next.data.fileInfo$) {
                        this.setFileInfo(next.data.fileInfo$);
                    }
                    this.krankheitenForUser = this.authService.getKrankheitenForUser();
                    if (next.data.dossiersOverview$) {
                        const summaryList: ImpfdossierSummaryJaxTS[] = next.data.dossiersOverview$.impfdossierSummaryList;
                        for (const impfdossierSummaryJaxT of summaryList) {
                            if (impfdossierSummaryJaxT.krankheit.identifier !== KrankheitIdentifierTS.COVID) {
                                this.nonCovidDossierSummaries?.push(impfdossierSummaryJaxT);
                            }
                        }
                    }
                } else {
                    this.errorService.addMesageAsError('IMPFDOK.ERROR.LOAD-DATA');
                    this.accessOk = false;
                }

                this.loadDataForZertifikatsgenerierung();

                this.abgleichElektronischerImpfausweis = !!this.dashboardCovidJax?.abgleichElektronischerImpfausweis;
            }, error => {
                LOG.error(error);
            });
    }

    private loadZertifikatList(): void {
        if (this.dashboardCovidJax?.registrierungsnummer) {
            this.dossierService.dossierResourceGetAllZertifikate(this.dashboardCovidJax.registrierungsnummer)
                .subscribe(list => {
                    this.zertifikatList = list;
                    if (list.length >= 1) { // wenn die Liste genau 1 Zertifikat enthaelt, sollte man es bereits sehen
                        this.hasMoreZertifikate = true;
                    }
                }, error => LOG.error(error));
        }
    }

    private loadDataForZertifikatsgenerierung(): void {

        if (this.deservesZertifikat()) {
            if (this.dashboardCovidJax) {
                this.hasNoPendingZertifikatGeneration =
                    !this.dashboardCovidJax.currentZertifikatInfo?.hasPendingZertifikatGeneration;
            }

            this.zertifikatService.zertifikatResourceHasValidToken()
                .subscribe(response => {
                    this.hasZertifikatAPIToken = response;
                }, error => {
                    LOG.error(error);
                });
        }
    }

    title(): string {
        return this.translationService.instant('GEIMPFT.TITLE',
            {person: this.getPersonInfos(this.dashboardCovidJax)});
    }

    private getPersonInfos(dashboardJax: DashboardJaxTS | undefined): string {
        if (!dashboardJax) {
            return '';
        }
        const result = dashboardJax.registrierungsnummer
            + ' - ' + dashboardJax.name
            + ' ' + dashboardJax.vorname
            + ' (' + DateUtil.dateAsLocalDateString(dashboardJax.geburtsdatum) + ') ';

        return result;
    }

    public hasZweiteImpfungVerzichtet(): boolean {
        return !!this.dashboardCovidJax && !!this.dashboardCovidJax.zweiteImpfungVerzichtetZeit;
    }

    public hasVollstaendigenImpfschutz(): boolean {
        const impfschutz = this.dashboardCovidJax?.vollstaendigerImpfschutz;
        if (impfschutz) {
            return impfschutz;
        }
        return false;
    }

    public deservesZertifikat(): boolean {
        return !!this.dashboardCovidJax
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            && !!this.dashboardCovidJax.currentZertifikatInfo!.deservesZertifikat;
    }

    public isAbgeschlossen(): boolean {
        return !!this.dashboardCovidJax && !!this.dashboardCovidJax.timestampZuletztAbgeschlossen;
    }

    public impfung1NeedsMoreThanOneDosis(): boolean {
        return this.dashboardCovidJax?.impfung1?.impfstoff?.anzahlDosenBenoetigt !== 1;
    }

    public isAllowedToDownloadFiles(): boolean {
        return this.authService.isOneOfRoles([TSRole.OI_DOKUMENTATION, TSRole.KT_IMPFDOKUMENTATION]);
    }

    public isAllowedToUploadFiles(): boolean {
        return this.authService.isOneOfRoles([
            TSRole.OI_KONTROLLE,
            TSRole.KT_NACHDOKUMENTATION,
            TSRole.KT_IMPFDOKUMENTATION,
        ]);
    }

    downloadImpfdokumentation(krankheit: KrankheitIdentifierTS): void {
        this.downloadService.downloadResourceDownloadImpfdokumentation(
            krankheit,
            this.dashboardCovidJax?.registrierungsnummer as string).subscribe(BlobUtil.openInNewTab, error => {
            LOG.error('Could not download Impfdokumentation for registration '
                + this.dashboardCovidJax?.registrierungsnummer);
        });
    }

    zweiteImpfungNichtWahrgenommen(): boolean {
        return this.isAbgeschlossen() &&
            !this.dashboardCovidJax?.impfung2?.timestampImpfung &&
            this.impfung1NeedsMoreThanOneDosis();
    }

    public showKontrolle1(): boolean {
        return !this.isAbgeschlossen()
            && !this.getLastImpffolge()
            && this.canEditImpfung();
    }

    public showKontrolle2(): boolean {
        return this.getLastImpffolge() === ImpffolgeTS.ERSTE_IMPFUNG &&
            this.impfung1NeedsMoreThanOneDosis() &&
            !this.hasZweiteImpfungVerzichtet() &&
            !this.isAbgeschlossen() &&
            this.canEditImpfung();
    }

    public canEditImpfung(): boolean {
        return this.authService.isOneOfRoles(
            [TSRole.OI_DOKUMENTATION, TSRole.OI_KONTROLLE, TSRole.KT_IMPFDOKUMENTATION]);
    }

    public isKantonNachdokumentation(): boolean {
        return this.authService.isOneOfRoles(
            [TSRole.KT_NACHDOKUMENTATION, TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION]);
    }

    public canZweiteImpfungWahrnehmen(): boolean {
        return this.zweiteImpfungNichtWahrgenommen()
            && this.getLastImpffolge() === ImpffolgeTS.ERSTE_IMPFUNG
            && (!!this.dashboardCovidJax && BoosterUtil.hasDashboardVacmeImpfung(this.dashboardCovidJax))
            && (this.canEditImpfung() || this.isKantonNachdokumentation())
            && !!this.dashboardCovidJax.impfung1
            && ExternGeimpftUtil.needsZweitimpfung(this.dashboardCovidJax.impfung1.impfstoff,
                this.dashboardCovidJax.externGeimpft);
    }

    public canErkrankungEntfernen(): boolean {
        return this.canZweiteImpfungWahrnehmen()
            && this.hasZweiteImpfungVerzichtet()
            && this.hasVollstaendigenImpfschutz();
    }

    public navigateToKontrolleForCovid(): void {
        this.router.navigate([
            'person',
            this.dashboardCovidJax?.registrierungsnummer,
            'kontrolle',
            KrankheitIdentifierTS.COVID,
        ]);
    }

    public zweiteImpfungWahrnehmen(): void {
        if (this.dashboardCovidJax?.registrierungsnummer) {
            this.kontrolleService
                .impfkontrolleResourceWahrnehmenZweiteImpfung(
                    this.getKrankheit(),
                    this.dashboardCovidJax?.registrierungsnummer)
                .subscribe(() => {

                    Swal.fire({
                        icon: 'success',
                        showCancelButton: false,
                        showConfirmButton: false,
                        timer: 1500,
                    }).then(() => {
                        // navigate to default page
                        this.router.navigate([
                            'dossier',
                            this.dashboardCovidJax?.registrierungsnummer,
                            'krankheit',
                            KrankheitIdentifierTS.COVID,
                        ]);
                    });
                }, error => {
                    LOG.error(error);
                });
        }
    }

    public isUserBerechtigtForKrankheit(krankheit: KrankheitIdentifierTS): boolean {
        return this.krankheitenForUser?.has(krankheit) ?? false;
    }

    public isUserBerechtigtForMoreThanOneKrankheit(): boolean {
        if (this.krankheitenForUser?.size) {
            return this.krankheitenForUser?.size > 1;
        }
        return false;
    }

    public getDossierOfKrankheit(krankheit: KrankheitIdentifierTS): ImpfdossierSummaryJaxTS | undefined {
        return this.nonCovidDossierSummaries?.filter(value => value.krankheit.identifier === krankheit)?.[0];
    }

    public canAddCovidBooster(): boolean {
        const hasRole = this.authService.isOneOfRoles([
            TSRole.OI_KONTROLLE,
            TSRole.OI_DOKUMENTATION,
            TSRole.KT_IMPFDOKUMENTATION,
        ]);
        return this.accessOk === true && this.hasVollstaendigenImpfschutz() && hasRole;
    }

    public canAddImpfungNonCovid(krankheit: KrankheitIdentifierTS): boolean {
        if (!this.isUserBerechtigtForKrankheit(krankheit)) {
            return false;
        }
        const hasRole = this.authService.isOneOfRoles([
            TSRole.OI_KONTROLLE,
            TSRole.OI_DOKUMENTATION,
            TSRole.KT_IMPFDOKUMENTATION,
        ]);
        return this.accessOk === true && hasRole;
    }

    public boosterWahrnehmenCovid(): void {
        this.router.navigate(['person', this.dashboardCovidJax?.registrierungsnummer, 'kontrolle', 'booster', 'COVID']);
    }

    public impfungWarnehmenNonCovid(krankheit: KrankheitIdentifierTS): void {
        const registrierungsnummer = this.dashboardCovidJax?.registrierungsnummer;
        if (registrierungsnummer) {
            this.dossierService.dossierResourceCreateImpfdossier(krankheit,
                registrierungsnummer).subscribe(() => {
                this.router.navigate([
                    'person',
                    registrierungsnummer,
                    'kontrolle',
                    'booster',
                    krankheit,
                ]);
            }, error => LOG.error(error));
        } else {
            this.errorService.addMesageAsError('Registrierungsnummer muss gesetzt sein');
        }
    }

    public showAufZweiteImpfungVerzichten(): boolean {
        return this.accessOk === true
            && !this.isAbgeschlossen() // nur wenn noch nicht abgeschlossen
            && !!this.dashboardCovidJax?.impfung1; // nur wenn die erste Impfung schon existiert
    }

    public getImpffolge(): ImpffolgeTS {
        // Wir gehen davon aus, dass wir bei "geimpft" immer im Fall ERSTE_IMPFUNG sind
        // Sonst wuerden wir ja nach der Suche auf der Impfdokumentation landen und nicht hier
        return ImpffolgeTS.ERSTE_IMPFUNG;
    }

    public downloadSpecificZertifikat(zertifikat: ZertifikatJaxTS): void {
        if (this.dashboardCovidJax?.registrierungsnummer && zertifikat.id) {
            this.dossierService.dossierResourceDownloadZertifikatWithId(
                this.dashboardCovidJax.registrierungsnummer, zertifikat.id)
                .subscribe(res => {
                    BlobUtil.openInNewTab(res, this.document);
                }, error => {
                    // Die Fehlermeldung wird vom ErrorInterceptor schon ausgegeben
                    LOG.error(error);
                });
        }
    }

    private setFileInfo(fileInfos: Array<FileInfoJaxTS>): void {
        if (fileInfos && fileInfos.length >= 0) {
            // Das Array zuerst clearen, damit sich die Files nicht ansammeln
            this.uploadedFiles = [];
            this.uploadedFiles = this.uploadedFiles.concat(fileInfos);
        }
    }

    public getLastImpffolge(): ImpffolgeTS | undefined {
        if (this.dashboardCovidJax) {
            return BoosterUtil.getLatestVacmeImpffolge(this.dashboardCovidJax);
        }
        return undefined;
    }

    public hasVacmeImpfung(): boolean {
        return !!this.dashboardCovidJax && BoosterUtil.hasDashboardVacmeImpfung(this.dashboardCovidJax);
    }

    public zertifikatCreated(): void {
        this.loadZertifikatList();
    }

    public supportsImpffolgenEinsUndZwei(): boolean {
        if (this.dashboardCovidJax?.krankheitIdentifier) {
            return this.vacmeSettingsService.supportsImpffolgenEinsUndZwei(this.dashboardCovidJax?.krankheitIdentifier);
        }
        return false;
    }

    public getFilesForKrankheitToDisplay(): KrankheitIdentifierTS {
        let result: KrankheitIdentifierTS | undefined;
        if (!this.isUserBerechtigtForMoreThanOneKrankheit()) {
            // Set.iterator() returns an array ['value', 'value'].
            result = this.krankheitenForUser?.entries().next().value[0];
        }
        if (!result) {
            result = KrankheitIdentifierTS.COVID;
        }
        return result;
    }

    public getKrankheit(): KrankheitIdentifierTS {
        if (this.dashboardCovidJax?.krankheitIdentifier === undefined || this.dashboardCovidJax.krankheitIdentifier === null) {
            this.errorService.addMesageAsError('KRANKHEIT NICHT GESETZT');
            throw new Error('Krankheit nicht gesetzt ' + this.dashboardCovidJax?.krankheitIdentifier);
        }
        return this.dashboardCovidJax.krankheitIdentifier;
    }

    public termineUmbuchen(data: TSPersonFolgetermin): void {
        // Nur erlaubt, keine Termine offen
        this.terminmanager.canProceedTermineUmbuchen$(this.isTerminbuchungAllowed(data))
            .subscribe(canProceed => {
                if (canProceed) {
                    this.router.navigate([
                        'person',
                        this.dashboardCovidJax?.registrierungsnummer,
                        'geimpftterminbearbeitung',
                        data.krankheit,
                    ]);
                }
            }, error => {
                LOG.error(error);
            });
    }

    public isTerminbuchungAllowed(data: TSPersonFolgetermin): boolean {
        // Die Funktionalität hier auf der geimpft Seite soll nur zur Verfuegung stehen, wenn nicht schon
        // ein Termin offen ist und erst im Booster Status
        if (!isAnyStatusOfBooster(data.status)) {
            return false;
        }
        if (data.terminNPending) {
            return false;
        }
        return true;
    }

    public getPersonFolgeterminFromImpfdossierSummary(summary: ImpfdossierSummaryJaxTS): TSPersonFolgetermin {
        return fromImpfdossierSummary(summary);
    }

    public getPersonFolgeterminFromDashboard(dashboard: DashboardJaxTS): TSPersonFolgetermin {
        return fromDashboard(dashboard);
    }
}

