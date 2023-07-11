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
import {ChangeDetectorRef, Component, Inject, OnInit} from '@angular/core';
import {UntypedFormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {
    DashboardJaxTS,
    ImpffolgeTS,
    KrankheitIdentifierTS,
    OrtDerImpfungDisplayNameJaxTS,
    OrtDerImpfungJaxTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent, NextFreierTerminSearch} from '../../../../../vacme-web-shared';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {ErrorMessageService} from '../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {TerminfindungService} from '../../../../../vacme-web-shared/src/lib/service/terminfindung.service';
import {filterOdisByKrankheit} from '../../../../../vacme-web-shared/src/lib/util/krankheit-utils';
import {TerminmanagementKontrolleImpfdokService} from '../../service/terminmanagement-kontrolle-impfdok.service';

const LOG = LogFactory.createLog('ImpfdokumentationComponent');

@Component({
    selector: 'app-geimpft-termine-bearbeiten-page',
    templateUrl: './geimpft-termine-bearbeiten-page.component.html',
    styleUrls: ['./geimpft-termine-bearbeiten-page.component.scss'],
})
export class GeimpftTermineBearbeitenPageComponent extends BaseDestroyableComponent implements OnInit {

    public dashboardJax?: DashboardJaxTS;
    public selectedOdiId: string | undefined;
    public ortDerImpfungList: OrtDerImpfungDisplayNameJaxTS[] = [];

    constructor(
        private router: Router,
        private fb: UntypedFormBuilder,
        private activeRoute: ActivatedRoute,
        private errorService: ErrorMessageService,
        private authService: AuthServiceRsService,
        private terminmanager: TerminmanagementKontrolleImpfdokService,
        private terminfindungService: TerminfindungService,
        private cdRef: ChangeDetectorRef,
        @Inject(DOCUMENT) private document: Document,
    ) {
        super();
    }

    ngOnInit(): void {
        this.initFromActiveRoute();
    }

    private initFromActiveRoute(): void {
        this.activeRoute.data
            .pipe(this.takeUntilDestroyed())
            .subscribe(next => {
                this.onChangedParams(
                    next.data.odiList$,
                    next.data?.dashboard$,
                );
            }, error => {
                LOG.error(error);
            });
    }

    private onChangedParams(
        ortDerImpfungList?: Array<OrtDerImpfungJaxTS>,
        dashboard?: DashboardJaxTS,
    ): void {

        // Dashboard laden
        if (dashboard) {
            this.dashboardJax = dashboard;
            this.ortDerImpfungList = filterOdisByKrankheit(ortDerImpfungList, this.getKrankheit());
            // Terminmanager und Terminfindungsservice anhand des Dashboards initialisieren
            this.reinitializeFromDashboard(this.dashboardJax, false);
        }
        this.cdRef.detectChanges();
    }

    public getKrankheit(): KrankheitIdentifierTS {
        if (this.dashboardJax?.krankheitIdentifier === undefined || this.dashboardJax.krankheitIdentifier === null) {
            this.errorService.addMesageAsError('KRANKHEIT NICHT GESETZT');
            throw new Error('Krankheit nicht gesetzt ' + this.dashboardJax?.krankheitIdentifier);
        }
        return this.dashboardJax.krankheitIdentifier;
    }

    exitTerminumbuchung(registrierungsNummer: string): void {
        if (registrierungsNummer && this.dashboardJax) {
            this.reinitializeFromDashboard(this.dashboardJax, true);
        }
        this.router.navigate(['person', this.dashboardJax?.registrierungsnummer, 'geimpft']);
    }

    public cancelAppointment($event: string): void {
        throw new Error('Darf nicht aufgerufen werden aus geimpft seite');
    }

    public updateAppointment(registrierungsNummer: string): void {
        this.terminmanager.umbuchungRequestPut$(ImpffolgeTS.BOOSTER_IMPFUNG, registrierungsNummer, this.getKrankheit())
            .subscribe(resultDashboard => {
                this.exitTerminumbuchung(registrierungsNummer);
            }, (error => {
                LOG.error(error);
            }));
    }

    public gotoNextFreienTermin($event: NextFreierTerminSearch): void {
        this.terminmanager.gotoNextFreienTermin('geimpftterminbearbeitung', $event, this.getKrankheit());
    }

    private reinitializeFromDashboard(dashboardJax: DashboardJaxTS, forceReload: boolean): void {
        this.dashboardJax = dashboardJax;
        this.terminmanager.reinitializeFromDashboard(dashboardJax, forceReload, ImpffolgeTS.BOOSTER_IMPFUNG, this);
        this.selectedOdiId = this.terminfindungService.ortDerImpfung?.id;
    }

    public isUserBerechtigtForOdiOfTermin(): boolean {
        return true;
    }
}

