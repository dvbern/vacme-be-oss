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
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {FileSaverService} from 'ngx-filesaver';
import {merge, Observable, of, Subject, timer} from 'rxjs';
import {mergeMap, share} from 'rxjs/operators';

// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js'; // nur das JS importieren
import {
    DocumentQueueJaxTS,
    DownloadService,
    KantonaleBerechtigungTS,
    KrankheitIdentifierTS,
    OrtDerImpfungJaxTS,
    OrtderimpfungService,
    ReportsService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../vacme-web-shared';
import {
    DATE_PATTERN,
    DB_DEFAULT_MAX_LENGTH,
    REGISTRIERUNGSNUMMER_LENGTH,
} from '../../../../vacme-web-shared/src/lib/constants';
import {TSRole} from '../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {VacmeSettingsService} from '../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import {parsableDateValidator} from '../../../../vacme-web-shared/src/lib/util/customvalidator/parsable-date-validator';
import DateUtil from '../../../../vacme-web-shared/src/lib/util/DateUtil';
import FormUtil from '../../../../vacme-web-shared/src/lib/util/FormUtil';
import TenantUtil from '../../../../vacme-web-shared/src/lib/util/TenantUtil';
import {ReportDownloadService} from '../service/reportdownload.service';

const LOG = LogFactory.createLog('ReportsPageComponent');

@Component({
    selector: 'app-reports-page',
    templateUrl: './reports-page.component.html',
    styleUrls: ['./reports-page.component.scss'],
})
export class ReportsPageComponent extends BaseDestroyableComponent implements OnInit {

    public listDocumentsObs$: Observable<Array<DocumentQueueJaxTS>> = of([]);
    private triggeredReprotSubject$: Subject<string> = new Subject<string>();
    public showCodeSuche = false;

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private activeRoute: ActivatedRoute,
        private reportDownloadService: ReportDownloadService,
        private reportsService: ReportsService,
        private filesaver: FileSaverService,
        private translationService: TranslateService,
        private authService: AuthServiceRsService,
        private ortderimpfungService: OrtderimpfungService,
        public translateService: TranslateService,
        private downloadService: DownloadService,
        private vacmeSettingsService: VacmeSettingsService,
    ) {
        super();
        const minLength = 2;
        this.formGroup = this.fb.group({
            abrechnungFrom: this.fb.control(undefined,
                [
                    Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(DATE_PATTERN), Validators.required, parsableDateValidator(),
                ]),
            abrechnungTo: this.fb.control(undefined,
                [
                    Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(DATE_PATTERN), Validators.required, parsableDateValidator(),
                ]),
        });
        this.formGroupFhir = this.fb.group({
            code: this.fb.control(undefined,
                [
                    Validators.minLength(REGISTRIERUNGSNUMMER_LENGTH),
                    Validators.maxLength(REGISTRIERUNGSNUMMER_LENGTH),
                    Validators.required,
                ]),
        });
    }

    public formGroup: FormGroup;
    public formGroupFhir: FormGroup;
    loaded = false;
    reportInProgress = false;

    private showPersonalisierterImpfReport = false;

    ngOnInit(): void {
        // Personalisierter Impfreport darf nur heruntergeladen werden, wenn ich als OI_IMPFVERANTWORTUNG
        // fuer mind. 1 ODI berechtigt bin, welches dieses Flag gesetzt hat.
        this.showPersonalisierterImpfReport = false;
        if (this.authService.hasRole(TSRole.OI_IMPFVERANTWORTUNG)) {
            this.authService.loadOdisForCurrentUserAndStoreInPrincipal$(false).pipe().subscribe(
                (list: Array<OrtDerImpfungJaxTS>) => {
                    const listWithPersonalisiertemReport = list.filter(value => value.personalisierterImpfReport);
                    if (listWithPersonalisiertemReport.length > 0) {
                        this.showPersonalisierterImpfReport = true;
                    }
                },
                (error: any) => {
                    LOG.error(error);
                },
            );
        }
        this.loaded = true;
        this.initPeriodicCheckForAsyncDocuments();
    }

    public isKantonMedizinischerReporterUser(): boolean {
        return this.authService.hasRole(TSRole.KT_MEDIZINISCHER_REPORTER);
    }

    public downloadStatistikReportingKanton(): void {
        if (this.reportInProgress) {
            return;
        }
        this.reportInProgress = true;
        Swal.showLoading();
        this.reportDownloadService.reportResourceGenerateKantonCSV$() // synchron
            .subscribe(res => {
                this.filesaver.save(res, this.translationService.instant('REPORTS.REPORTING_KANTON_CSV_FILENAME'));
                Swal.hideLoading();
                this.reportInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.reportInProgress = false;
                LOG.error('Report konnte nicht heruntergeladen werden', error);
            });
    }

    public downloadStatistikReportingImpfungen(): void {
        if (this.reportInProgress) {
            return;
        }
        this.reportInProgress = true;
        Swal.showLoading();
        this.reportDownloadService.reportResourceGenerateReportingImpfungenCSVAsync$() // synchron
            .subscribe(res => {
                this.filesaver.save(res, this.translationService.instant('REPORTS.REPORTING_IMPFUNGEN_CSV_FILENAME'));
                Swal.hideLoading();
                this.reportInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.reportInProgress = false;
                LOG.error('Report konnte nicht heruntergeladen werden', error);
            });
    }

    public downloadStatistikReportingTerminslots(): void {
        if (this.reportInProgress) {
            return;
        }
        this.reportInProgress = true;
        Swal.showLoading();
        this.reportDownloadService.reportResourceGenerateReportingTerminslotsCSV$() // synchron
            .subscribe(res => {
                this.filesaver.save(res, this.translationService.instant('REPORTS.REPORTING_TERMINSLOTS_CSV_FILENAME'));
                Swal.hideLoading();
                this.reportInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.reportInProgress = false;
                LOG.error('Report konnte nicht heruntergeladen werden', error);
            });
    }

    public downloadStatistikReportingOdis(): void {
        if (this.reportInProgress) {
            return;
        }
        this.reportInProgress = true;
        Swal.showLoading();
        this.reportDownloadService.reportResourceGenerateReportingOdisCSV$() // synchron
            .subscribe(res => {
                this.filesaver.save(res, this.translationService.instant('REPORTS.REPORTING_ODIS_CSV_FILENAME'));
                Swal.hideLoading();
                this.reportInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.reportInProgress = false;
                LOG.error('Report konnte nicht heruntergeladen werden', error);
            });
    }

    public downloadAbrechnungEnabled(): boolean {
        return this.authService.hasRole(TSRole.KT_MEDIZINISCHER_REPORTER);
    }

    public downloadAbrechnung(): void {
        if (this.reportInProgress) {
            return;
        }
        FormUtil.doIfValid(this.formGroup, () => {
            this.reportInProgress = true;
            Swal.showLoading();
            // at midday to avoid timezone issues
            const dateBis: Date = DateUtil.parseDateAsMidday(this.formGroup.get('abrechnungTo')?.value);
            const dateVon: Date = DateUtil.parseDateAsMidday(this.formGroup.get('abrechnungFrom')?.value);
            // if (!this.zeitraumValid(dateVon, dateBis)) {
            //     return;
            // }
            this.reportsService.reportResourceGenerateExcelReportAbrechnungAsync(this.translationService.currentLang,
                dateBis,
                dateVon) // asynchron
                .subscribe(res => {
                    this.triggeredReprotSubject$.next(this.translationService.instant('REPORTS.ABRECHNUNG.FILE_NAME'));
                    Swal.hideLoading();
                    this.reportInProgress = false;
                }, error => {
                    Swal.hideLoading();
                    this.reportInProgress = false;
                    LOG.error('Report konnte nicht heruntergeladen werden', error);
                });
        });
    }

    public downloadAbrechnungErwachsen(): void {
        if (this.reportInProgress) {
            return;
        }
        FormUtil.doIfValid(this.formGroup, () => {
            this.reportInProgress = true;
            Swal.showLoading();
            // at midday to avoid timezone issues
            const dateBis: Date = DateUtil.parseDateAsMidday(this.formGroup.get('abrechnungTo')?.value);
            const dateVon: Date = DateUtil.parseDateAsMidday(this.formGroup.get('abrechnungFrom')?.value);

            this.reportsService.reportResourceGenerateExcelReportAbrechnungErwachsenAsync(this.translationService.currentLang,
                dateBis,
                dateVon) // asynchron
                .subscribe(() => {
                    this.triggeredReprotSubject$.next(this.translationService.instant(
                        'REPORTS.ABRECHNUNG_ERWACHSEN.FILE_NAME'));
                    Swal.hideLoading();
                    this.reportInProgress = false;
                }, error => {
                    Swal.hideLoading();
                    this.reportInProgress = false;
                    LOG.error('Report konnte nicht heruntergeladen werden', error);
                });
        });
    }

    public downloadAbrechnungKind(): void {
        if (this.reportInProgress) {
            return;
        }
        FormUtil.doIfValid(this.formGroup, () => {
            this.reportInProgress = true;
            Swal.showLoading();
            // at midday to avoid timezone issues
            const dateBis: Date = DateUtil.parseDateAsMidday(this.formGroup.get('abrechnungTo')?.value);
            const dateVon: Date = DateUtil.parseDateAsMidday(this.formGroup.get('abrechnungFrom')?.value);

            this.reportsService.reportResourceGenerateExcelReportAbrechnungKindAsync(this.translationService.currentLang,
                dateBis,
                dateVon) // asynchron
                .subscribe(() => {
                    this.triggeredReprotSubject$.next(this.translationService.instant(
                        'REPORTS.ABRECHNUNG_KIND.FILE_NAME'));
                    Swal.hideLoading();
                    this.reportInProgress = false;
                }, error => {
                    Swal.hideLoading();
                    this.reportInProgress = false;
                    LOG.error('Report konnte nicht heruntergeladen werden', error);
                });
        });
    }

    public downloadImpfungenEnabled(): boolean {
        return this.authService.hasRole(TSRole.KT_MEDIZINISCHER_REPORTER);
    }

    public isZHAndKTMedReporter(): boolean {
        return TenantUtil.ZURICH && this.authService.hasRole(TSRole.KT_MEDIZINISCHER_REPORTER);
    }

    public showKantonsreportAndKantonsarztreport(): boolean {
        if (this.isKantonMedizinischerReporterUser() && this.isCovidEnabledForKantonEdit()) {
            // Aktuell zeigen wir die beiden Reports nicht an. Grund: Zweifel wegen Datenschutzkonzept.
            // Kantonsbenutzer duerfen nur Covid-Impfungen/Dossiers sehen, in diesen Repors sind aber
            // auch Registrierungen, die nur Affenpockenimpfungen haben (nur die Registrierung, nicht
            // aber die eigentlichen Impfungen). Dies weil wir immer ein (leeres)
            // Covid-Dossier erstellen beim Registrieren
            return false;
        }
        return false;
    }

    public downloadAbrechnungZH(): void {
        if (this.reportInProgress) {
            return;
        }
        FormUtil.doIfValid(this.formGroup, () => {
            this.reportInProgress = true;
            Swal.showLoading();
            // at midday to avoid timezone issues
            const dateBis: Date = DateUtil.parseDateAsMidday(this.formGroup.get('abrechnungTo')?.value);
            const dateVon: Date = DateUtil.parseDateAsMidday(this.formGroup.get('abrechnungFrom')?.value);
            // if (!this.zeitraumValid(dateVon, dateBis)) {
            //     return;
            // }
            this.reportsService.reportResourceGenerateExcelReportAbrechnungZHAsync( // asynchron
                this.translationService.currentLang, dateBis, dateVon,
            ).subscribe(res => {
                this.triggeredReprotSubject$.next(this.translationService.instant('REPORTS.ABRECHNUNG_ZH.FILE_NAME'));
                Swal.hideLoading();
                this.reportInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.reportInProgress = false;
                LOG.error('Report konnte nicht heruntergeladen werden', error);
            });
        });
    }

    public downloadAbrechnungZHKind(): void {
        if (this.reportInProgress) {
            return;
        }
        FormUtil.doIfValid(this.formGroup, () => {
            this.reportInProgress = true;
            Swal.showLoading();
            // at midday to avoid timezone issues
            const dateBis: Date = DateUtil.parseDateAsMidday(this.formGroup.get('abrechnungTo')?.value);
            const dateVon: Date = DateUtil.parseDateAsMidday(this.formGroup.get('abrechnungFrom')?.value);

            this.reportsService.reportResourceGenerateExcelReportAbrechnungZHKindAsync( // asynchron
                this.translationService.currentLang, dateBis, dateVon,
            ).subscribe(res => {
                this.triggeredReprotSubject$.next(this.translationService.instant('REPORTS.ABRECHNUNG_ZH_KIND.FILE_NAME'));
                Swal.hideLoading();
                this.reportInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.reportInProgress = false;
                LOG.error('Report konnte nicht heruntergeladen werden', error);
            });
        });
    }

    private zeitraumValid(dateVon: Date, dateBis: Date): boolean {
        const daysDiff = DateUtil.getDaysDiff(dateBis, dateVon);
        if (daysDiff > 366) {
            Swal.fire({
                icon: 'info',
                text: this.translationService.instant('REPORTS.ABRECHNUNG_ZEITRAUM_TOO_BIG'),
                showConfirmButton: true,
            });
            this.reportInProgress = false;
            return false;
        }
        return true;
    }

    public downloadStatistikReportingKantonsarzt(): void {
        if (this.reportInProgress) {
            return;
        }
        this.reportInProgress = true;
        Swal.showLoading();
        this.reportDownloadService.reportResourceGenerateReportingKantonsarztCSV$() // synchron
            .subscribe(res => {
                this.filesaver.save(res, this.translationService.instant('REPORTS.REPORTING_KANTONSARZT_CSV_FILENAME'));
                Swal.hideLoading();
                this.reportInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.reportInProgress = false;
                LOG.error('Report konnte nicht heruntergeladen werden', error);
            });
    }

    public downloadReportingOdiImpfungenEnabled(): boolean {
        return this.showPersonalisierterImpfReport && this.authService.isOneOfRoles([TSRole.OI_LOGISTIK_REPORTER]);
    }

    public downloadReportingOdiImpfungen(): void {
        if (this.reportInProgress) {
            return;
        }
        this.reportInProgress = true;
        Swal.showLoading();
        this.reportDownloadService.reportResourceGenerateReportingOdiImpfungenCSV$(this.translationService.currentLang) // synchron
            .subscribe(res => {
                this.filesaver.save(res,
                    this.translationService.instant('REPORTS.REPORTING_ODI_IMPFUNGEN_CSV_FILENAME'));
                Swal.hideLoading();
                this.reportInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.reportInProgress = false;
                LOG.error('Report konnte nicht heruntergeladen werden', error);
            });
    }

    public downloadReportingOdiTerminbuchungen(): void {
        if (this.reportInProgress) {
            return;
        }

        this.reportInProgress = true;
        Swal.showLoading();
        this.reportsService.reportResourceGenerateReportingOdiTerminbuchungenAsync(this.translationService.currentLang, // asynchron
        ).subscribe(() => {
            this.triggeredReprotSubject$.next(this.translationService.instant(
                'REPORTS.REPORTING_ODI_TERMINBUCHUNGEN_CSV_FILENAME'));
            Swal.hideLoading();
            this.reportInProgress = false;
        }, error => {
            Swal.hideLoading();
            this.reportInProgress = false;
            LOG.error('Report konnte nicht heruntergeladen werden', error);
        });
    }

    public downloadImpungenFHIR(): void {
        if (this.reportInProgress) {
            return;
        }

        const code = this.formGroupFhir.get('code')?.value;

        FormUtil.doIfValid(this.formGroupFhir, () => {
            this.reportInProgress = true;
            Swal.showLoading();
            this.downloadService.downloadResourceDownloadFhirImpfdokumentation(code)
                .subscribe(
                    (res) => {
                        this.filesaver.save(res,
                            this.translationService.instant('REPORTS.IMPFUNGEN_FHIR.FILE_NAME', {regNum: code}));
                        Swal.hideLoading();
                        this.reportInProgress = false;
                    }, error => {
                        Swal.hideLoading();
                        this.reportInProgress = false;
                        LOG.error('FHIR File konnte nicht heruntergeladen werden', error);
                    });
        });
    }

    private initPeriodicCheckForAsyncDocuments(): void {
        if (this.userHasRoleThatHasAsyncReport()) {
            // trigger reload on new document order and on timer
            this.listDocumentsObs$ =
                merge(this.triggeredReprotSubject$, timer(0, 15000)).pipe(
                    mergeMap(() => this.reportsService.reportResourceFindJobsForBenutzer(),
                    ),
                    share(),
                );
        } else {
            LOG.info('Der aktuelle User hat keine Rolle die Zugriff auf asynchrone Jobs hat',
                this.authService.getPrincipalRoles());
        }

    }

    public userHasRoleThatHasAsyncReport(): boolean {
        return this.authService.isOneOfRoles([
            TSRole.KT_MEDIZINISCHER_REPORTER, TSRole.OI_IMPFVERANTWORTUNG, TSRole.OI_LOGISTIK_REPORTER,
        ]);
    }

    public downloadQueueItem(docResult: DocumentQueueJaxTS): void {
        if (docResult.docQueueResultId) {
            this.reportDownloadService.apiV1WebReportsDownloadDocumentqueueIdGet$(docResult.docQueueResultId)
                .subscribe(res => {
                    this.filesaver.save(res, docResult.docQueueResultFilename);
                }, error => {
                    LOG.error(error);
                });
        }

    }

    toggleCodeSuche(): void {
        this.showCodeSuche = !this.showCodeSuche;
    }

    public downloadBEEnabled(): boolean {
        return TenantUtil.BERN && this.authService.hasRole(TSRole.KT_MEDIZINISCHER_REPORTER);
    }

    showFHIRDownload(): boolean {
        //TenantUtil.showFHIRDownload() kann entfernt werden, sobald FHIR Download produktiv verwendet werden kann
        return TenantUtil.showFHIRDownload() && this.downloadBEEnabled();
    }

    public downloadReportingOdiTerminbuchungenEnabled(): boolean {
        return this.authService.isOneOfRoles([TSRole.OI_LOGISTIK_REPORTER]);
    }

    public isCovidEnabledForKantonEdit(): boolean {
        return this.vacmeSettingsService.getKantonaleBerechtigung(KrankheitIdentifierTS.COVID)
            === KantonaleBerechtigungTS.KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG;
    }
}
