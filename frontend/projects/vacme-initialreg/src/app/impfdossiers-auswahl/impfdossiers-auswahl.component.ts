import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {
    DossierService,
    ImpfdossiersOverviewJaxTS,
    ImpfdossierSummaryJaxTS,
    KrankheitIdentifierTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {
    BaseDestroyableComponent,
} from '../../../../vacme-web-shared';
import {VacmeSettingsService} from '../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import {NavigationService} from '../service/navigation.service';

const LOG = LogFactory.createLog('ImpfdossiersOverviewComponent');

@Component({
    selector: 'app-impfdossiers-auswahl',
    templateUrl: './impfdossiers-auswahl.component.html',
    styleUrls: ['./impfdossiers-auswahl.component.scss'],
})
export class ImpfdossiersAuswahlComponent extends BaseDestroyableComponent implements OnInit {

    public impfdossiersOverviewJax!: ImpfdossiersOverviewJaxTS;
    public krankheitenWithoutDossier: KrankheitIdentifierTS[] = [];

    constructor(
        private activeRoute: ActivatedRoute,
        private dossierService: DossierService,
        private router: Router,
        private navigationService: NavigationService,
        private vacmeSettingsService: VacmeSettingsService
    ) {
        super();
    }

    ngOnInit(): void {
        this.initFromActiveRoute();
        this.calculateKrankheitenWithoutDossier();
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

    private calculateKrankheitenWithoutDossier(): void {
        this.krankheitenWithoutDossier = Object.values(KrankheitIdentifierTS).filter(krankheit => {
            return !this.impfdossiersOverviewJax.impfdossierSummaryList?.map(summary => summary.krankheit.identifier)
                .includes(krankheit);
        });
    }

    public onDossierSelected($event: { registrierungsnummer: string; krankheitIdentifier: KrankheitIdentifierTS }): void {
        this.dossierService.dossierResourceRegGetOrCreateImpfdossier($event.krankheitIdentifier, $event.registrierungsnummer)
            .subscribe((updatedDossierSummary: ImpfdossierSummaryJaxTS) => {
                this.navigationService.navigateToDossierDetailCheckingPreConditions(
                    $event.registrierungsnummer,
                    $event.krankheitIdentifier,
                    updatedDossierSummary.leistungerbringerAgbConfirmationNeeded,
                    updatedDossierSummary.externGeimpftConfirmationNeeded);
            }, error => LOG.error(error));
    }

}
