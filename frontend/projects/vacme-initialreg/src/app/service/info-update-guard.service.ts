import {Injectable, Input} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {RegistrierungService, SelfserviceEditJaxTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {VacmeSettingsService} from '../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import DateUtil from '../../../../vacme-web-shared/src/lib/util/DateUtil';
import TenantUtil from '../../../../vacme-web-shared/src/lib/util/TenantUtil';

const LOG = LogFactory.createLog('GesuchInfoUpdateGuard');

@Injectable({
    providedIn: 'root',
})
export class InfoUpdateGuard implements CanActivate {

    @Input()
    public selfserviceEditJax?: SelfserviceEditJaxTS;

    constructor(
        private registrierungService: RegistrierungService,
        private router: Router,
        private authService: AuthServiceRsService,
        private vacmeSettingsService: VacmeSettingsService,
    ) {
    }

    public canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot,
    ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

        if (TenantUtil.BERN || this.authService.hasRoleCallCenter()) {
            return true;
        }

        return this.registrierungService.registrierungResourceGetSelfserviceEditJax()
            .pipe(map(selfservice => {
                if (selfservice.registrationTimestamp !== undefined) {
                    if (selfservice.timestampInfoUpdate === null) {
                        if (DateUtil.getMinutesDiff(new Date(),
                            selfservice.registrationTimestamp) >= this.vacmeSettingsService.minutesBetweenInfoUpdates) {
                            this.logRedirect('/umfrage-aktuell-daten');
                            return this.router.parseUrl('/umfrage-aktuell-daten');
                        } else {
                            selfservice.timestampInfoUpdate = selfservice.registrationTimestamp;
                            // @ts-ignore weil die Krankenkasse ein Enum ist, aber von openapi als objekt erstellt
                            // wurde...
                            selfservice.krankenkasse = selfservice.krankenkasse.name;
                            this.registrierungService.registrierungResourceUpdateSelfserviceEditData(selfservice)
                                .subscribe(res => console.log(res), error => LOG.error(error));
                        }
                    } else {
                        if (selfservice.timestampInfoUpdate) {
                            if (DateUtil.getMinutesDiff(new Date(),
                                selfservice.timestampInfoUpdate) >= this.vacmeSettingsService.minutesBetweenInfoUpdates) {
                                this.logRedirect('/umfrage-aktuell-daten');
                                return this.router.parseUrl('/umfrage-aktuell-daten');
                            }
                        }
                    }
                }
                return true;
            }));
    }

    logRedirect(alternativeroute: string): void {
        LOG.info('Guard prevented routing to ' + this.router?.url + ', instead routing to ' + alternativeroute);
    }
}
