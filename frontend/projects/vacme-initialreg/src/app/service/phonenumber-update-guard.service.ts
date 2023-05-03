import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot, UrlTree} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {KeycloakService} from 'keycloak-angular';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {DossierService, KrankheitIdentifierTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {VacmeSettingsService} from '../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import {isMobileNumber} from '../../../../vacme-web-shared/src/lib/util/customvalidator/phone-number-validator';
import DateUtil from '../../../../vacme-web-shared/src/lib/util/DateUtil';

const LOG = LogFactory.createLog('PhonenumberUpdateGuard');

@Injectable({
    providedIn: 'root',
})
export class PhonenumberUpdateGuard implements CanActivate {

    constructor(
        private dossierService: DossierService,
        private keycloakService: KeycloakService,
        private authService: AuthServiceRsService,
        private vacmeSettingsService: VacmeSettingsService,
        private translationService: TranslateService,
    ) {
    }

    public canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot,
    ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
        const principal = this.authService.getPrincipal();
        const telefonnummer = principal?.telefon;

        if (this.authService.hasRoleCallCenter()) {
            return true;
        }

        const registrierungsnummer = route.params.registrierungsnummer;
        // Irrelevant which Krankheit we are talking about // TODO Affenpocken make tighter DTO to not have to define a Krankheit
        return this.dossierService.dossierResourceRegGetDashboardRegistrierung(KrankheitIdentifierTS.COVID, registrierungsnummer)
            .pipe(map(dashboard => {

                if (dashboard.timestampPhonenumberUpdate === undefined || dashboard.timestampPhonenumberUpdate === null ||
                    (DateUtil.getMinutesDiff(new Date(),
                        dashboard.timestampPhonenumberUpdate) >= this.vacmeSettingsService.minutesBetweenNumberUpdates)) {
                    if (telefonnummer && !isMobileNumber(telefonnummer)) {
                        this.dossierService.dossierResourceRegSetTimestampPhonenumberUpdate(registrierungsnummer)
                            .subscribe(res => LOG.debug(res), error => LOG.error(error));
                        this.showPopupQuestion();
                    }
                }

                return true;
            }));
    }

    private showPopupQuestion(): void {
        Swal.fire({
            icon: 'info',
            title: 'Invalid number',
            text: this.translationService.instant('TELEFONNUMMER_UPDATE.UPDATE_NUMMER_QUESTION'),
            allowOutsideClick: false,
            showCancelButton: true,
            showConfirmButton: true,
            confirmButtonText: this.translationService.instant(
                'TELEFONNUMMER_UPDATE.UPDATE_NUMMER_QUESTION_CONFIRM'),
            cancelButtonText: this.translationService.instant(
                'TELEFONNUMMER_UPDATE.UPDATE_NUMMER_QUESTION_CANCEL'),
        }).then(r => {
            if (r.isConfirmed) {
                let loginUrl = this.keycloakService.getKeycloakInstance().createLoginUrl();
                loginUrl += '&kc_action=sms_update_mobile_number';
                window.location.replace(loginUrl);
            }
        });
    }
}
