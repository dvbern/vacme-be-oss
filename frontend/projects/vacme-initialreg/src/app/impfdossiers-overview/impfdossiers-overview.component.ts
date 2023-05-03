import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {ImpfdossiersOverviewJaxTS, ImpfdossierSummaryJaxTS, KrankheitIdentifierTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {
    BaseDestroyableComponent,
} from '../../../../vacme-web-shared';
import {TSRole} from '../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {VacmeSettingsService} from '../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import {NavigationService} from '../service/navigation.service';

const LOG = LogFactory.createLog('ImpfdossiersOverviewComponent');

@Component({
    selector: 'app-impfdossiers-overview',
    templateUrl: './impfdossiers-overview.component.html',
    styleUrls: ['./impfdossiers-overview.component.scss'],
})
export class ImpfdossiersOverviewComponent extends BaseDestroyableComponent implements OnInit {

    public impfdossiersOverviewJax!: ImpfdossiersOverviewJaxTS;

    constructor(
        private activeRoute: ActivatedRoute,
        private authService: AuthServiceRsService,
        private vacmeSettingsService: VacmeSettingsService,
        private navigationService: NavigationService,
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
                if (next) {
                    this.impfdossiersOverviewJax = next.dossiersOverview;
                }
            }, error => {
                LOG.error(error);
            });
    }

    public weitereImpfdossierAnlegen(): void {
        this.navigationService.navigateToDossierAddition(this.impfdossiersOverviewJax.registrierungsnummer);
    }

    public canAddMoreImpfdossier(): boolean {
        return Object.values(KrankheitIdentifierTS).some(krankheit => {
            return this.canAddImpfdossier(krankheit);
        });
    }

    private canAddImpfdossier(krankheit: KrankheitIdentifierTS): boolean {
        // Fuer Callcenter: Nur hinzufuegbar, falls die Krankheit Callcenter unterstuetzt
        if (this.authService.isOneOfRoles([TSRole.CC_AGENT])) {
            if (!this.vacmeSettingsService.supportsCallcenter(krankheit)) {
                return false;
            }
        }
        // Ansonsten falls noch nicht vorhanden
        return !this.impfdossiersOverviewJax.impfdossierSummaryList?.map(summary => summary.krankheit.identifier)
            .includes(krankheit);
    }

    public onDossierSelected(dossierSummary: ImpfdossierSummaryJaxTS): void {
        this.navigationService.navigateToDossierDetailCheckingPreConditions(
            dossierSummary.registrierungsnummer,
            dossierSummary.krankheit.identifier,
            dossierSummary.leistungerbringerAgbConfirmationNeeded,
            dossierSummary.externGeimpftConfirmationNeeded,
        );
    }
}
