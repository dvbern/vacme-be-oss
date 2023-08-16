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
import {
    ApplicationMessageJaxTS,
    KrankheitIdentifierTS,
    MessagesService,
    OrtDerImpfungJaxTS,
    OrtderimpfungService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../vacme-web-shared';
import {TSRole} from '../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {ErrorMessageService} from '../../../../vacme-web-shared/src/lib/service/error-message.service';
import {PagerEvent, PagerState} from '../../../../vacme-web-shared/src/lib/simple-pager/pager-enum';
import {hasOdiForKrankheit} from '../../../../vacme-web-shared/src/lib/util/krankheit-utils';
import {SortByPipe} from '../../../../vacme-web-shared/src/lib/util/sort-by-pipe';
import TenantUtil from '../../../../vacme-web-shared/src/lib/util/TenantUtil';
import {ApplicationMessageCacheService} from '../service/applicationMessage.cache.service';

const LOG = LogFactory.createLog('StartPageComponent');

@Component({
    selector: 'app-start-page',
    templateUrl: './start-page.component.html',
    styleUrls: ['./start-page.component.scss'],
    providers: [SortByPipe],
})
export class StartPageComponent extends BaseDestroyableComponent implements OnInit {

    ortderimpfungenListe: Array<OrtDerImpfungJaxTS> = [];
    ortderimpfungenAnzeige: Array<OrtDerImpfungJaxTS> = [];
    odiFilterState: PagerState = PagerState.DISPLAY_NEXT;
    displayLessOdi = false;
    typeaheadModel: any;
    pagePointer = 0;
    appMessages: (ApplicationMessageJaxTS)[] = [];
    openedAppMessage = '';
    readonly PAGE_SIZE = 5;
    showAdHocCovidButton = false;
    krankheitenForUser: Set<KrankheitIdentifierTS> = new Set();
    krankheitenNonCovidForUser: Set<KrankheitIdentifierTS> = new Set();

    constructor(
        private router: Router,
        private currRoute: ActivatedRoute,
        private errorService: ErrorMessageService,
        private ortderimpfungService: OrtderimpfungService,
        private authService: AuthServiceRsService,
        private messagesService: MessagesService,
        private messageCacheService: ApplicationMessageCacheService,
        private sortPipe: SortByPipe,
    ) {
        super();
    }

    ngOnInit(): void {
        // query params mit error handeln
        this.currRoute.queryParams
            .pipe(this.takeUntilDestroyed())
            .subscribe(params => {
                if (params && params.err) {
                    const errorKey = params.err;
                    this.errorService.addMesageAsError(errorKey);
                    void this.router.navigate([], {relativeTo: this.currRoute});
                }

            }, error => (LOG.error(error)));
        if (this.isManagementAllGetBerechtigt()) {
            this.krankheitenForUser = this.authService.getKrankheitenForUser();
            for (const krankheit of this.krankheitenForUser) {
                if (krankheit !== KrankheitIdentifierTS.COVID) {
                    this.krankheitenNonCovidForUser.add(krankheit);
                }
            }
            this.authService.loadOdisForCurrentUserAndStoreInPrincipal$(false).subscribe(
                (list: Array<OrtDerImpfungJaxTS>) => {
                    this.ortderimpfungenListe = this.sortPipe.transform(list, 'asc', 'name');
                    this.ortderimpfungenAnzeige = this.ortderimpfungenListe.slice(0, this.PAGE_SIZE);
                    this.showAdHocCovidButton = hasOdiForKrankheit(
                        this.ortderimpfungenListe, KrankheitIdentifierTS.COVID);
                },
                (error: any) => {
                    LOG.error(error);
                },
            );
        }

        // Es werden maximal zwei Meldungen gleichzeitig angezeigt
        this.messagesService.applicationMessageResourceGetApplicationMessageLatest(2).pipe().subscribe(
            (messages: ApplicationMessageJaxTS[]) => {
                if (messages) {
                    this.handleMessages(messages);
                }
            },
            (error) => {
                LOG.error(error);
            });
    }

    public onSelect(event: any): void {
        if (event) {
            this.ortderimpfungenAnzeige = [];
            this.ortderimpfungenAnzeige.push(event);
            this.typeaheadModel = event;
            this.odiFilterState = PagerState.DISPLAY_CLEAR_FILTER;
        }
    }

    public onPageButton(event: PagerEvent): void {
        switch (event) {
            case PagerEvent.CLEAR_FILTER:
                this.pagePointer = 0;
                this.ortderimpfungenAnzeige = this.ortderimpfungenListe.slice(0, this.PAGE_SIZE);
                this.odiFilterState = PagerState.DISPLAY_NEXT;
                this.typeaheadModel = undefined; // clear typeahead model
                break;
            case PagerEvent.NEXT:
                this.pageNext();
                break;
            case PagerEvent.PREVIOUS:
                this.pagePrev();
                break;
        }
    }

    private pageNext(): void {
        const from = this.pagePointer + this.PAGE_SIZE;
        let to = this.pagePointer + 2 * this.PAGE_SIZE;
        this.odiFilterState = PagerState.DISPLAY_NEXT_AND_PREVIOUS;
        if (to >= this.ortderimpfungenListe.length) {
            to = this.ortderimpfungenListe.length;
            this.odiFilterState = PagerState.DISPLAY_PREVIOUS;
        }
        this.ortderimpfungenAnzeige = this.ortderimpfungenListe.slice(from, to);
        this.pagePointer = from;
    }

    private pagePrev(): void {
        let to = this.pagePointer;
        let from = this.pagePointer - this.PAGE_SIZE;
        this.odiFilterState = PagerState.DISPLAY_NEXT_AND_PREVIOUS;
        if (from <= 0) {
            from = 0;
            to = this.PAGE_SIZE;
            this.odiFilterState = PagerState.DISPLAY_NEXT;
        }
        this.ortderimpfungenAnzeige = this.ortderimpfungenListe.slice(from, to);
        this.pagePointer = from;
    }

    public neuesOrtDerImpfung(): void {
        void this.router.navigate(['/ortderimpfung/stammdaten']);
    }

    public ortderImpfungBearbeiten(id: string | undefined): void {
        void this.router.navigate(['/ortderimpfung/stammdaten', id]);
    }

    public isManagementAllGetBerechtigt(): boolean {
        return this.authService.isOneOfRoles([
            TSRole.OI_ORT_VERWALTER,
            TSRole.AS_REGISTRATION_OI,
            TSRole.OI_KONTROLLE,
            TSRole.OI_DOKUMENTATION,
            TSRole.OI_BENUTZER_VERWALTER,
            TSRole.OI_BENUTZER_REPORTER,
            TSRole.OI_IMPFVERANTWORTUNG,
            TSRole.OI_LOGISTIK_REPORTER,
            TSRole.OI_MEDIZINISCHER_REPORTER,
        ]);
    }

    public isOiDokumentation(): boolean {
        return this.authService.hasRole(TSRole.OI_DOKUMENTATION);
    }

    public isOiImpfverantwortung(): boolean {
        return this.authService.hasRole(TSRole.OI_IMPFVERANTWORTUNG);
    }

    public isOiKontrolle(): boolean {
        return this.authService.hasRole(TSRole.OI_KONTROLLE);
    }

    public isOiOrtVerwalter(): boolean {
        return this.authService.hasRole(TSRole.OI_ORT_VERWALTER);
    }

    public isOiLogistikReporter(): boolean {
        return this.authService.hasRole(TSRole.OI_LOGISTIK_REPORTER) ||
            this.authService.hasRole(TSRole.KT_MEDIZINISCHER_REPORTER);
    }

    public isAsRegistrationOiBenutzer(): boolean {
        return this.authService.hasRole(TSRole.AS_REGISTRATION_OI);
    }

    public isAsZertifikatKTBenutzer(): boolean {
        return this.authService.hasRole(TSRole.KT_ZERTIFIKAT_AUSSTELLER);
    }

    public isAsBenutzerVerwalterBenutzer(): boolean {
        return this.authService.hasRole(TSRole.AS_BENUTZER_VERWALTER);
    }

    public isKtNachDokumentationBenutzer(): boolean {
        return this.authService.isOneOfRoles([TSRole.KT_NACHDOKUMENTATION, TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION]);
    }

    public hasDatenkorrektur(): boolean {
        return this.isOiImpfverantwortung() ||
            this.isOiDokumentation() ||
            this.isAsBenutzerVerwalterBenutzer() ||
            this.isKtNachDokumentationBenutzer();
    }

    public hasAvailableTools(): boolean {
        return this.isAsRegistrationOiBenutzer() || this.isOiLogistikReporter()
            || this.isOiImpfverantwortung() || this.isOiDokumentation()
            || this.isAsBenutzerVerwalterBenutzer() || this.isAsZertifikatKTBenutzer()
            || this.isKtNachDokumentationBenutzer();
    }

    private handleMessages(messages: ApplicationMessageJaxTS[]): void {
        this.appMessages = messages;
        this.openedAppMessage = '';
        for (const message of messages) {
            if (message.id && !this.messageCacheService.isCached(message.id)) {
                this.openedAppMessage = message.id;
                break;
            }
        }
    }

    public closeAppMessage(appMessage: ApplicationMessageJaxTS | undefined): void {
        const messageId = appMessage?.id;
        if (messageId) {
            this.messageCacheService.cacheClosedApplicationMessages(messageId);
        }
        this.handleMessages(this.appMessages);
    }

    public isOpen(appMessage: ApplicationMessageJaxTS): boolean {
        return this.openedAppMessage === appMessage.id;
    }

    public openMessage(appMessage: ApplicationMessageJaxTS): void {
        if (appMessage.id) {
            this.openedAppMessage = appMessage.id;
        }
    }

    //enabled only for ApplicationSupport, FachBAB, FachBAB del, Organisationsverantwortung, Organisationssupervision
    public disableOdiEditButton(): boolean {
        return !this.isAsRegistrationOiBenutzer() && !this.isOiOrtVerwalter();
    }

    public showSucheUVCI(): boolean {
        return TenantUtil.ZURICH;
    }

    public showSuche(): boolean {
        return !this.authService.hasAnyKantonRole();
    }
}
