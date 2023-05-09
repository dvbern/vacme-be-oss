import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {KorrekturDashboardJaxTS, OnboardingService, RegistrierungsEingangTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {AuthServiceRsService} from '../../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import TenantUtil from '../../../../../../vacme-web-shared/src/lib/util/TenantUtil';
import {getAllowdRoles, TSDatenKorrekturTyp} from '../TSDatenKorrekturTyp';

const LOG = LogFactory.createLog('DatenKorrekturOnboardingComponent');

@Component({
    selector: 'app-daten-korrektur-onboarding',
    templateUrl: './daten-korrektur-onboarding.component.html',
})
export class DatenKorrekturOnboardingComponent {
    @Input()
    korrekturDashboard!: KorrekturDashboardJaxTS | undefined;

    @Output()
    public finished = new EventEmitter<boolean>();

    constructor(
        private authService: AuthServiceRsService,
        private translationService: TranslateService,
        private onboardingService: OnboardingService,
    ) {
    }

    public hasRequiredRole(): boolean {
        return this.authService.isOneOfRoles(getAllowdRoles(TSDatenKorrekturTyp.ONBOARDING));
    }

    public reset(): void {
        this.korrekturDashboard = undefined;
        this.finished.emit(false);
    }

    resendOnboardingLetter(): void {
        const regNr = this.korrekturDashboard?.registrierungsnummer;
        if (regNr) {
            Swal.fire({
                icon: 'question',
                text: this.translationService.instant('ONBOARDING.TRIGGER.CONFIRM.TEXT'),
                confirmButtonText: this.translationService.instant('ONBOARDING.TRIGGER.CONFIRM.CONFIRM'),
                showCancelButton: true,
                cancelButtonText: this.translationService.instant('ONBOARDING.TRIGGER.CONFIRM.CANCEL'),
            }).then(r => {
                if (r.isConfirmed) {
                    this.onboardingService.onboardingResourceTriggerOnboardingLetter(regNr)
                        .subscribe(() => {
                            Swal.fire({
                                icon: 'success',
                                text: this.translationService.instant('ONBOARDING.TRIGGER.BESTAETIGUNG'),
                                timer: 2000,
                                showCloseButton: false,
                                showConfirmButton: false,
                            });
                        }, error => LOG.error(error));
                }
            });
        }
    }

    public isGueltigeSchweizerAdresse(): boolean {
        return !!this.korrekturDashboard?.gueltigeSchweizerAdresse;
    }

    public canSendOnboardingLetter(): boolean {
        return this.korrekturDashboard?.eingang !== RegistrierungsEingangTS.ONLINE_REGISTRATION
             && TenantUtil.hasFachanwendungOnboarding();
    }
}
