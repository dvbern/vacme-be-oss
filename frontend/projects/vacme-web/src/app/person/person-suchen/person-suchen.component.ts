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

import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {DashboardJaxTS, DossierService} from 'vacme-web-generated';
import {REGISTRIERUNGSNUMMER_LENGTH} from '../../../../../vacme-web-shared/src/lib/constants';
import {TerminfindungResetService} from '../../../../../vacme-web-shared/src/lib/service/terminfindung-reset.service';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';
import {NavigationService} from '../../service/navigation.service';

@Component({
    selector: 'app-person-suchen',
    templateUrl: './person-suchen.component.html',
    styleUrls: ['./person-suchen.component.scss']
})
export class PersonSuchenComponent implements OnInit {

    public formGroup!: FormGroup;
    @Input() focusOnShow = false;

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private dossierService: DossierService,
        private terminfindungResetService: TerminfindungResetService,
        private navigationService: NavigationService) {
    }

    ngOnInit(): void {
        this.formGroup = this.fb.group({
            code: this.fb.control(undefined, [
                Validators.minLength(REGISTRIERUNGSNUMMER_LENGTH),
                Validators.maxLength(REGISTRIERUNGSNUMMER_LENGTH),
                Validators.required]),
        });
    }

    public submitIfValid(): void {
        const eingabe = this.formGroup.get('code')?.value;
        const code = this.readRegistrierungsnummerFromQrCodeLink(eingabe);
        // Das Ergebnis wieder zuruecksetzen, da sonst das Form nicht valid ist
        // Bisschen ein Hack, aber sonst muss man die Validierungen ausschalten, was (fuer den Normalfall)
        // auch nicht schoen ist
        this.formGroup.get('code')?.setValue(code);
        FormUtil.doIfValid(this.formGroup, () => {
            this.suchen(code);
        });
    }

    public suchen(code: string): void {
        this.terminfindungResetService.resetData();
        this.dossierService.dossierResourceGetDashboardRegistrierung(code).subscribe(
            (res: DashboardJaxTS) => {
                this.navigationService.navigate(res);
            },
            error => {
                this.navigationService.notFoundResult();
            }
        );
    }

    private readRegistrierungsnummerFromQrCodeLink(searchtext: string): string {
        // QRCode = baseUrl + "/dossier/" + registrierung.getRegistrierungsnummer();
        if (searchtext != null && searchtext.startsWith('http') && searchtext.indexOf('/dossier/') > 0) {
            return searchtext.split('/dossier/').reverse()[0];
        }
        return searchtext;
    }
}
