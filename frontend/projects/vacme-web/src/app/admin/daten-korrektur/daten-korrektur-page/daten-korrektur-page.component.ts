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

import {formatDate} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {KorrekturDashboardJaxTS, KorrekturService, KrankheitIdentifierTS} from 'vacme-web-generated';
import {canton, LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent, Option} from '../../../../../../vacme-web-shared';
import {REGISTRIERUNGSNUMMER_LENGTH} from '../../../../../../vacme-web-shared/src/lib/constants';
import {TSRole} from '../../../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {ErrorMessageService} from '../../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {VacmeSettingsService} from '../../../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import DateUtil from '../../../../../../vacme-web-shared/src/lib/util/DateUtil';
import FormUtil from '../../../../../../vacme-web-shared/src/lib/util/FormUtil';
import {
    atLeastOneImpfung,
    getAllowdRoles,
    getAllowedKorrekturTypen,
    getAllowedKrankheiten,
    isImpfungAbhaengigeKorrektur,
    isKrankheitsUnabhaengigeKorrektur,
    TSDatenKorrekturTyp,
} from '../TSDatenKorrekturTyp';

const LOG = LogFactory.createLog('DatenKorrekturComponent');

@Component({
    selector: 'app-daten-korrektur-page',
    templateUrl: './daten-korrektur-page.component.html',
    styleUrls: ['./daten-korrektur-page.component.scss'],
})
export class DatenKorrekturPageComponent extends BaseDestroyableComponent implements OnInit {

    searchFormGroup!: FormGroup;

    korrekturDashboard!: KorrekturDashboardJaxTS | undefined;

    formGroup!: FormGroup;

    public datenKorrekturTypen: Option[] = [];
    public krankheitTypen: Option[] = this.getKrankheitOptions();

    selectedKorrekturTyp: TSDatenKorrekturTyp | undefined;
    hoveredKorrekturTyp: TSDatenKorrekturTyp | undefined;

    queryRegNummer?: string;

    constructor(
        private authService: AuthServiceRsService,
        private fb: FormBuilder,
        private korrekturService: KorrekturService,
        private router: Router,
        private route: ActivatedRoute,
        private vacmeSettingsService: VacmeSettingsService,
        private translate: TranslateService,
        private errorMessageService: ErrorMessageService,
    ) {
        super();
    }

    ngOnInit(): void {
        this.route.queryParamMap.pipe(this.takeUntilDestroyed()).subscribe(map => {
            const registrierungNummer = map.get('registrierungNummer');
            if (registrierungNummer) {
                this.queryRegNummer = registrierungNummer;
                this.router.navigate(
                    ['.'],
                    {relativeTo: this.route, queryParams: {registrierungNummer: null}});
            }
        }, error => LOG.error(error));
        this.calculateKorrekturTypenOptions();

        this.searchFormGroup = this.fb.group({
            registrierungsNummer: this.fb.control(this.queryRegNummer, [
                Validators.required,
                Validators.minLength(REGISTRIERUNGSNUMMER_LENGTH),
                Validators.maxLength(REGISTRIERUNGSNUMMER_LENGTH),
            ]),
            datenKorrekturTyp: this.fb.control(null, [Validators.required]),
            krankheit: this.fb.control(null, [Validators.required]),
        });
        if (this.authService.isOneOfRoles(getAllowdRoles(TSDatenKorrekturTyp.EMAIL_TELEPHONE))
            && this.vacmeSettingsService.emailKOrrekturEnabled) {
            this.datenKorrekturTypen.push({
                label: TSDatenKorrekturTyp.EMAIL_TELEPHONE,
                value: TSDatenKorrekturTyp.EMAIL_TELEPHONE,
            });
        }
        // Krankheit Dropdown initialisieren, falls nur fuer eine Krankheit berechtigt
        const krankheitVorauswahl = this.authService.getKrankheitForUserIfOnlyAllowedForExactlyOne();
        if (!!krankheitVorauswahl) {
            this.searchFormGroup.get('krankheit')?.setValue(krankheitVorauswahl);
        }

        this.searchFormGroup.get('datenKorrekturTyp')?.valueChanges.subscribe(value => {
            this.hoveredKorrekturTyp = value;
            this.calculateKrankheitsTypenOptionsAndSelectIfOnlyOne(value);
        }, error => LOG.error(error));
    }

    public showAuswahlKrankheit(): boolean {
        if (isKrankheitsUnabhaengigeKorrektur(this.hoveredKorrekturTyp)) {
            return false;
        }

        // Die Krankheit muss nur ausgewählt werden, wenn überhaupt mehr als 1 Option vorhanden ist
        return this.krankheitTypen.length > 1;
    }

    public getKrankheitOptions(): Option[] {
        const options: Option[] = [];
        const krankheitenForUser = this.getEditableKrankheiten();
        for (const krankheitIdentifierT of krankheitenForUser) {
            options.push({label: krankheitIdentifierT, value: krankheitIdentifierT});
        }
        return options;
    }

    private getEditableKrankheiten(): Set<KrankheitIdentifierTS> {
        const krankheitenForUser = this.authService.getKrankheitenForUser();
        if (this.authService.hasAnyKantonRole()) {
            for (const krankheit of Object.values(KrankheitIdentifierTS)) {
                if (this.vacmeSettingsService.getHasImpfungViewableForKanton(krankheit)) {
                    krankheitenForUser.add(krankheit);
                }
            }
        }
        return krankheitenForUser;
    }

    private calculateKorrekturTypenOptions(): void {
        // Korrektur-Typ Liste fuellen aufgrund meiner Berechtigungen und krankheit
        this.datenKorrekturTypen =
            getAllowedKorrekturTypen(this.authService.getPrincipalRoles(), this.getEditableKrankheiten())
                .filter(t => t !== TSDatenKorrekturTyp.EMAIL_TELEPHONE)
                .filter(t => canton.hasFachanwendungOnboarding || t !== TSDatenKorrekturTyp.ONBOARDING)
                .map(t => {
                    return {label: t, value: t};
                });
    }

    private calculateKrankheitsTypenOptionsAndSelectIfOnlyOne(korrekturTyp: TSDatenKorrekturTyp): void {
        // Krankheitsliste fuellen aufgrund meiner Berechtigungen und dem gewaehlten KorrekturTyp
        const allowedKrankheitenForKorrekturTyp = getAllowedKrankheiten(this.authService.getPrincipalRoles(),
            korrekturTyp);
        const krankheitenForUser = Array.from(this.getEditableKrankheiten());
        const intersection = allowedKrankheitenForKorrekturTyp
            .filter(x => krankheitenForUser.includes(x));
        this.krankheitTypen = intersection
            .map(t => {
                return {label: t, value: t};
            });
        if (intersection.length === 1 || isKrankheitsUnabhaengigeKorrektur(korrekturTyp)) { // if only one select it directly
            this.searchFormGroup.get('krankheit')?.setValue(intersection[0]);
        } else {
            this.searchFormGroup.get('krankheit')?.setValue(null);
        }
    }

    public getDateString(date: Date | undefined | null): string {
        if (date == null) {
            return '';
        }
        return formatDate(date, DateUtil.dateFormatMedium(this.translate.currentLang), this.translate.currentLang);
    }

    public hasRequiredRole(): boolean {
        // Nur anzeigen, wenn ich fuer irgendetwas berechtigt bin
        return this.datenKorrekturTypen.length > 0;
    }

    public showImpfungDatenKorrektur(): boolean {
        return this.selectedKorrekturTyp === TSDatenKorrekturTyp.IMPFUNG_DATEN;
    }

    public showImpfungOrtKorrektur(): boolean {
        return this.selectedKorrekturTyp === TSDatenKorrekturTyp.IMPFUNG_ORT;
    }

    public showImpfungVerabreichungKorrektur(): boolean {
        return this.selectedKorrekturTyp === TSDatenKorrekturTyp.IMPFUNG_VERABREICHUNG;
    }

    public showImpfungDatumKorrektur(): boolean {
        return this.selectedKorrekturTyp === TSDatenKorrekturTyp.IMPFUNG_DATUM;
    }

    public showImpfungLoeschen(): boolean {
        return this.selectedKorrekturTyp === TSDatenKorrekturTyp.DELETE_IMPFUNG;
    }

    public showAccountLoeschen(): boolean {
        return this.selectedKorrekturTyp === TSDatenKorrekturTyp.DELETE_ACCOUNT;
    }

    public showPersonendatenKorrektur(): boolean {
        return this.selectedKorrekturTyp === TSDatenKorrekturTyp.PERSONENDATEN;
    }

    public showZertifikatKorrektur(): boolean {
        return this.selectedKorrekturTyp === TSDatenKorrekturTyp.ZERTIFIKAT;
    }

    public showZertifikatRevokeAndRecreateKorrektur(): boolean {
        return this.selectedKorrekturTyp === TSDatenKorrekturTyp.ZERTIFIKAT_REVOKE_AND_RECREATE;
    }

    public showEmailKorrektur(): boolean {
        return this.selectedKorrekturTyp === TSDatenKorrekturTyp.EMAIL_TELEPHONE;
    }

    public showPersonalienSearch(): boolean {
        return this.authService.isOneOfRoles([TSRole.KT_NACHDOKUMENTATION, TSRole.KT_IMPFVERANTWORTUNG]);
    }

    public showImpfungSelbstzahlendKorrektur(): boolean {
        return this.selectedKorrekturTyp === TSDatenKorrekturTyp.SELBSTZAHLENDE;
    }

    public showOnboardingKorrektur(): boolean {
        return this.selectedKorrekturTyp === TSDatenKorrekturTyp.ONBOARDING;
    }

    public searchIfValid(): void {
        FormUtil.doIfValid(this.searchFormGroup, () => {
            this.search();
        });
    }

    private search(): void {
        const code = this.searchFormGroup.get('registrierungsNummer')?.value;
        this.selectedKorrekturTyp = this.searchFormGroup.get('datenKorrekturTyp')?.value;
        const krankheitIdentifier = this.searchFormGroup.get('krankheit')?.value;

        this.korrekturService.korrekturResourceGetKorrekturDashboard(krankheitIdentifier, code).subscribe(
            (res: KorrekturDashboardJaxTS) => {
                this.assertAtleastOneImpfungForImpfungKorrektur(res, this.hoveredKorrekturTyp);
                this.prepareDossier(res);
            },
            error => {
                LOG.error(`Could not find Registrierung with code ${code}`, error);
            },
        );
    }

    private assertAtleastOneImpfungForImpfungKorrektur(
        dashbaord: KorrekturDashboardJaxTS,
        korrekturTyp?: TSDatenKorrekturTyp,
    ): void {
        if (!atLeastOneImpfung(dashbaord) && isImpfungAbhaengigeKorrektur(korrekturTyp)) {
            this.errorMessageService.addMesageAsError('ERROR_UNAUTHORIZED');
            throw new Error('Keine Impfung für diese Korrektur');
        }
    }

    private prepareDossier(dashboardJaxTS: KorrekturDashboardJaxTS): void {
        this.korrekturDashboard = dashboardJaxTS;
    }

    public finished(navigate: boolean): void {
        const registrierungsnummer = this.korrekturDashboard?.registrierungsnummer as string;
        this.korrekturDashboard = undefined;
        this.searchFormGroup.reset();

        if (this.authService.hasAnyKantonRole()) {
            // Re-initialize the dropdown correctly because we reset the entire form before
            this.krankheitTypen = this.getKrankheitOptions();
            return; // If we are a Kanton user we don't have anywhere sensible to go
        }

        if (navigate) {
            this.navigateToGeimpftPage(registrierungsnummer);
        } else {
            this.navigateToStartseite();
        }
    }

    public navigateToSearch(): void {
        this.router.navigate(['admin', 'datenkorrektur', 'suche-registrierung']);
    }

    public navigateToGeimpftPage(registrierungsnummer: string): void {
        this.router.navigate(['person', registrierungsnummer, 'geimpft']);
    }

    public navigateToStartseite(): void {
        this.router.navigate(['startseite']);
    }
}
