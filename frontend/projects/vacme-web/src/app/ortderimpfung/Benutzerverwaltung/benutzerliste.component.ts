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

import {Component, Input, OnInit, QueryList, ViewChildren} from '@angular/core';
import {UntypedFormBuilder} from '@angular/forms';
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {FachRolleTS, OdibenutzerService, OdiUserJaxTS, OrtDerImpfungJaxTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../../vacme-web-shared';
import OdiUserPaging from '../../../../../vacme-web-shared/src/lib/model/OdiUserPaging';
import {PersonenListePaginationCacheService} from '../../service/personen-liste-pagination-cache.service';
import {SortableHeaderDirective, SortEvent} from './sortable.directive';

const LOG = LogFactory.createLog('BenutzerlisteComponent');

@Component({
    selector: 'app-benutzerliste',
    templateUrl: './benutzerliste.component.html',
    styleUrls: ['./benutzerliste.component.scss'],
})
export class BenutzerlisteComponent extends BaseDestroyableComponent implements OnInit {

    @Input() public ortDerImfpung?: OrtDerImpfungJaxTS;

    table: Array<OdiUserJaxTS> = [];
    ortDerImpfungName = '';
    ortDerImpfungIdentifier = '';
    ortDerImpfungId = '';
    public disableLoadMore = false;

    constructor(
        private fb: UntypedFormBuilder,
        private router: Router,
        private odibenutzerService: OdibenutzerService,
        private translate: TranslateService,
        private personenListeService: PersonenListePaginationCacheService,
    ) {
        super();
    }

    @ViewChildren(SortableHeaderDirective) headers!: QueryList<SortableHeaderDirective>;

    ngOnInit(): void {
        if (this.ortDerImfpung) {
            if (this.ortDerImfpung.name) {
                this.ortDerImpfungName = this.ortDerImfpung?.name;
            }
            if (this.ortDerImfpung.identifier) {
                this.ortDerImpfungIdentifier = this.ortDerImfpung?.identifier;
            }
            if (this.ortDerImfpung.id) {
                this.ortDerImpfungId = this.ortDerImfpung?.id;
            }
        }
        this.loadNextBatch();
        this.addListenerForAddedUsers();
    }

    public confirmAndLeaveGroup(odiUserJaxTS: OdiUserJaxTS): void {
        Swal.fire({
            icon: 'warning',
            text: this.translate.instant('FACH-APP.ODI.BENUTZER.LOESCHEN.CONFIRM.TITLE',
                {user: odiUserJaxTS.firstName + ' ' + odiUserJaxTS.lastName}),
            showCancelButton: true,
            cancelButtonText: this.translate.instant('FACH-APP.ODI.BENUTZER.LOESCHEN.CONFIRM.CANCEL'),
            confirmButtonText: this.translate.instant('FACH-APP.ODI.BENUTZER.LOESCHEN.CONFIRM.OK'),
        }).then(result => {
            if (result.isConfirmed) {
                this.leaveGroupe(odiUserJaxTS);
            }
        });
    }

    public leaveGroupe(odiUserJaxTS: OdiUserJaxTS): void {

        if (odiUserJaxTS.id != null) {
            this.odibenutzerService.odiBenutzerResourceLeavegroup(this.ortDerImpfungIdentifier, odiUserJaxTS.id)
                .pipe()
                .subscribe((result: OdiUserJaxTS) => {
                        const index = this.table.indexOf(odiUserJaxTS, 0);
                        if (index > -1) {
                            this.table.splice(index, 1);
                        }
                    },
                    error => {
                        LOG.error(error);
                    });
        }
    }

    public onSort({column, direction}: SortEvent): void {
        // resetting other headers
        this.headers.forEach(header => {
            if (header.appSortable !== column) {
                header.direction = '';
            }
        });

        this.table = this.sort(this.table, column, direction);
    }

    private compare(v1: OdiUserJaxTS, v2: OdiUserJaxTS): number {
        return v1 < v2 ? -1 : v1 > v2 ? 1 : 0;
    }

    private sort(odiUserJaxTS: OdiUserJaxTS[], column: string, direction: string): OdiUserJaxTS[] {
        if (direction === '') {
            return odiUserJaxTS;
        } else {
            return [...odiUserJaxTS].sort((a, b) => {
                // @ts-ignore
                const res = this.compare(a[column], b[column]);
                return direction === 'asc' ? res : -res;
            });
        }
    }

    public canBeModified(odiUserJaxTS: OdiUserJaxTS): boolean {
        if (odiUserJaxTS) {
            return (odiUserJaxTS.fachRolle !== FachRolleTS.FACHVERANTWORTUNG_BAB
                && odiUserJaxTS.fachRolle !== FachRolleTS.ORGANISATIONSVERANTWORTUNG
                && odiUserJaxTS.fachRolle !== FachRolleTS.APPLIKATIONS_SUPPORT);
        }
        return false;
    }

    public getRollenbezeichnung(fachRolle: FachRolleTS | undefined): string {
        if (fachRolle) {
            return this.translate.instant('FACH-APP.ODI.BENUTZER.ROLLE.' + fachRolle);
        }
        return 'n/a';
    }

    public encode(aUriPart: string): string {
        return encodeURIComponent(aUriPart);
    }

    public loadNextBatch(): void {
        this.personenListeService.loadNextBatch$(this.ortDerImpfungIdentifier)
            .pipe(this.takeUntilDestroyed())
            .subscribe(
                loadedOdiPage => this.handleChangedOdiPagingTable(loadedOdiPage), // must be arrowfunction
                error => {
                    LOG.error(error);
                });
    }

    private handleChangedOdiPagingTable(odiUserPage: OdiUserPaging): void {
        if (odiUserPage.odiIdentifier === this.ortDerImpfungIdentifier) {
            this.table = odiUserPage.list;
            this.disableLoadMore = odiUserPage.foundAll;
        }
    }

    public toggleUserEnabled(user: OdiUserJaxTS): void {
        const initial = user.enabled;
        user.enabled = !initial;

        if (user.id) {
            this.odibenutzerService.odiBenutzerResourceToggleEnabled(this.ortDerImpfungIdentifier, user.id)
                .subscribe(() => {
                }, error => {
                    // roll back changes
                    user.enabled = initial;
                    LOG.error(error);
                });
        }
    }

    private addListenerForAddedUsers(): void {
        this.personenListeService.personListForOdi$
            .pipe(this.takeUntilDestroyed())
            .subscribe(
                loadedOdiPage => this.handleChangedOdiPagingTable(loadedOdiPage), // must be arrowfunction
                error => LOG.error(error));
    }
}
