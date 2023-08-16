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
import {KeycloakService} from 'keycloak-angular';
import {Observable, of} from 'rxjs';
import {concatMap} from 'rxjs/operators';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    DossierService,
    ImpfdossiersOverviewJaxTS,
    RegistrierungsCodeJaxTS,
    RegistrierungService,
} from 'vacme-web-generated';

import {LogFactory} from '../../logging';
import {TSRole} from '../../model';
import {AuthServiceRsService} from '../../service/auth-service-rs.service';
import {BoosterUtil} from '../../util/booster-util';

const LOG = LogFactory.createLog('AccountLinksComponent');


@Component({
    selector: 'lib-account-links',
    templateUrl: './account-links.component.html',
    styleUrls: ['./account-links.component.scss'],
})
export class AccountLinksComponent implements OnInit {

    public profileUrl = '#';
    public passwordUrl = '#';
    public profileName = 'Profile';
    public profileUsername ? = '';

    constructor(private route: ActivatedRoute,
                private authService: AuthServiceRsService,
                private keycloakService: KeycloakService,
                private translationService: TranslateService,
                private dossierService: DossierService,
                private router: Router,
                private registrationService: RegistrierungService
    ) {

    }

    public ngOnInit(): void {

        this.recalculateProfileUrl();
        this.recalculateProfileName();
        this.recalculatePasswordUrl();
    }

    public getCurrentProfileName(): string | undefined {
        const principal = this.authService.getPrincipal();
        if (principal) {
            return principal.getFullName();
        }

        return undefined;
    }

    private getCurrentProfileUsername(): string | undefined {
        const principal = this.authService.getPrincipal();
        if (principal) {
            return principal.username;
        }

        return undefined;

    }

    private recalculateProfileUrl(): void {
        this.profileUrl = this.authService.getProfileUrl();
    }

    private recalculateProfileName(): void {
        if (this.getCurrentProfileName()) {
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            this.profileName = this.getCurrentProfileName()!;
        }
        this.profileUsername = this.getCurrentProfileUsername();

    }

    private recalculatePasswordUrl(): void {
        this.passwordUrl = this.authService.getKeycloakPasswordUrl();
    }

    public canDeleteAccount(): boolean {
        return this.authService.hasRole(TSRole.IMPFWILLIGER);
    }

    // alles mit Delete: hierverschoben aus account.component
    public confirmAndDeleteAccount(): void {
        if (this.canDeleteAccount()) {
            this.deleteAction();
        }
    }


    public triggerMobileNummerChange(): void {
        // trigger required Action from Application
        // see https://github.com/keycloak/keycloak-community/blob/main/design/application-initiated-actions.md
        let loginUrl =  this.keycloakService.getKeycloakInstance().createLoginUrl();
        loginUrl = loginUrl + '&kc_action=sms_update_mobile_number';
        window.location.replace(loginUrl); // trigger login with required action
    }

    private deleteAction(): void {
        LOG.info('delete Account action was triggered');
        this.registrationService.registrierungResourceMy()
            .pipe(
                concatMap(regCode => this.loadImpfdossiersOverviewJax$(regCode))
            )
            .subscribe(
                (impfdossiersOverview) => {
                    if (impfdossiersOverview) {
                        // wenn schon Impfung da ist dann popup das nicht geloescht wird
                        if (BoosterUtil.hasAnyVacmeImpfung(impfdossiersOverview)) {
                            this.showDeleteNotPossiblePopupAndGotToStart();
                        } else {
                            // delete Benutzer and Reg
                            this.showDeleteConfirmationAndRunDeleteAction(
                                impfdossiersOverview.registrierungsnummer,
                                (regNummer: string | undefined) => {
                                    this.deleteRegistrierung(regNummer);
                                },
                            );
                        }
                    } else {
                        // delete Benutzer
                        this.showDeleteConfirmationAndRunDeleteAction(
                            undefined,
                            (_: string | undefined) => {
                                this.deleteBenutzeraccount();
                            },
                        );
                    }
                },
                (err: any) => LOG.error('Cannot get registration for current user', err));
    }

    private loadImpfdossiersOverviewJax$(regCode: RegistrierungsCodeJaxTS): Observable<ImpfdossiersOverviewJaxTS> | Observable<undefined> {
        if (regCode?.registrierungsnummer) {
            return this.dossierService.dossierResourceRegGetImpfdossiersOverview(regCode.registrierungsnummer);
        }
        return of(undefined);
    }

    private showDeleteNotPossiblePopupAndGotToStart(): void {
        void Swal.fire({
            icon: 'info',
            text: this.translationService.instant('OVERVIEW.DELETE_ACCOUNT_ALREADY_GEIMPFT'),
            showConfirmButton: true,
        });
        void this.router.navigate(['/userprofile']);
    }

    private showDeleteConfirmationAndRunDeleteAction(
        registrierungsNummer: string | undefined,
        action: (regNummer: string | undefined) => void
    ): void {
        void Swal.fire({
            icon: 'question',
            text: this.translationService.instant('OVERVIEW.DELETE_ACCOUNT_QUESTION'),
            showCancelButton: true,
            confirmButtonText: this.translationService.instant('OVERVIEW.DELETE_ACCOUNT_QUESTION_CONFIRM'),
            cancelButtonText: this.translationService.instant('OVERVIEW.DELETE_ACCOUNT_QUESTION_CANCEL')
        }).then(r => {
            if (r.isConfirmed) {
                action(registrierungsNummer);
            } else {
                void this.router.navigate(['/userprofile']);
            }
        });
    }

    private deleteRegistrierung(registrierungsNummer: string | undefined): void {
        if (registrierungsNummer) {
            this.dossierService.dossierResourceRegDeleteRegistrierung(registrierungsNummer)
                .subscribe(() => {
                    void Swal.fire({
                        icon: 'success',
                        showCancelButton: false,
                        showConfirmButton: false,
                        timer: 1500,
                    }).then(() => {
                        this.logout();
                    });
                }, error => {
                    LOG.error('Could not delete Account', error);
                });
        }
    }

    private deleteBenutzeraccount(): void {
        this.dossierService.dossierResourceRegDeleteBenutzer()
            .subscribe(() => {
                void Swal.fire({
                    icon: 'success',
                    showCancelButton: false,
                    showConfirmButton: false,
                    timer: 1500,
                }).then(() => {
                    this.logout();
                });
            }, error => {
                LOG.error('Could not delete Account', error);
            });
    }

    public logout(): void {
        this.authService.logout(this.router, '/start');
    }

}
