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

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import {KrankheitIdentifierTS} from 'vacme-web-generated';
import {LogFactory} from '../../../logging';

const LOG = LogFactory.createLog('ImpfdossierAuswahlListItemComponent');

@Component({
    selector: 'lib-impfdossier-auswahl-list-item',
    templateUrl: './impfdossier-auswahl-list-item.component.html',
    styleUrls: ['./impfdossier-auswahl-list-item.component.scss'],
})
export class ImpfdossierAuswahlListItemComponent {

    @Input() registrierungsnummer!: string;
    @Input() krankheitIdentifier!: KrankheitIdentifierTS;

    @Output()
    public dossierSelected = new EventEmitter<{
        registrierungsnummer: string;
        krankheitIdentifier: KrankheitIdentifierTS;
    }>();

    constructor(
        private translationService: TranslateService,
    ) {
    }

    public getDossierTitle(): string {
        return this.translationService.instant('KRANKHEITEN.' + this.krankheitIdentifier);
    }

    public getDossierDescription1(): string | undefined {
        return this.getDossierDescription(1);
    }

    public getDossierDescription2(): string | undefined {
        return this.getDossierDescription(2);
    }

    private getDossierDescription(nr: number): string | undefined {
        const key = 'KRANKHEITEN.' + this.krankheitIdentifier + '.DESCRIPTION' + nr;
        return this.hasTranslation(key) ? this.translationService.instant(key) : undefined;
    }

    public addDossier(): void {
        this.dossierSelected.emit({
            registrierungsnummer: this.registrierungsnummer,
            krankheitIdentifier: this.krankheitIdentifier,
        });
    }

    public hasTranslation(key: string): boolean {
        const translation = this.translationService.instant(key);
        return translation !== key && translation !== '';
    }
}
