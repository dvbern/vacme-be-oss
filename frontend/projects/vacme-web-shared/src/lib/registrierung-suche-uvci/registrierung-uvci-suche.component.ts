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

import {DatePipe} from '@angular/common';
import {Component, Input, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Observable} from 'rxjs';
import {RegistrierungSearchResponseWithRegnummerJaxTS} from 'vacme-web-generated';
import {DATE_PATTERN, DB_DEFAULT_MAX_LENGTH} from '../constants';
import {LogFactory} from '../logging';
import {datumInPastValidator} from '../util/customvalidator/datum-in-past-validator';
import {parsableDateValidator} from '../util/customvalidator/parsable-date-validator';
import DateUtil from '../util/DateUtil';
import FormUtil from '../util/FormUtil';

const LOG = LogFactory.createLog('PersonalienSucheComponent');

@Component({
    selector: 'lib-registrierung-uvci-suche',
    templateUrl: './registrierung-uvci-suche.component.html',
    styleUrls: ['./registrierung-uvci-suche.component.scss']
})
export class RegistrierungUvciSucheComponent implements OnInit {

    public personalienGroup!: FormGroup;
    public matchFormArray?: FormArray;
    public searched = false;

    @Input()
    public searchFunction!: (geburtsdatum: Date, name: string, vorname: string,
                             uvci: string) => Observable<RegistrierungSearchResponseWithRegnummerJaxTS[]>;

    @Input()
    public selectionFunction!: (daten: RegistrierungSearchResponseWithRegnummerJaxTS) => void;

    constructor(
        private fb: FormBuilder,
        private datePipe: DatePipe
    ) {
    }

    ngOnInit(): void {
        this.personalienGroup = this.fb.group({
            name: this.fb.control(undefined, [
                Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required]),
            vorname: this.fb.control(undefined, [
                Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required]),
            geburtsdatum: this.fb.control(undefined, [
                Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.pattern(DATE_PATTERN), Validators.required,
                parsableDateValidator(), datumInPastValidator()
            ]),
            uvci: this.fb.control(undefined, [
                Validators.maxLength(4), Validators.required]),
        });
    }

    public personalienSuchen(): void {
        FormUtil.doIfValid(this.personalienGroup, () => {
            const name = this.personalienGroup.get('name')?.value;
            const vorname = this.personalienGroup.get('vorname')?.value;
            const geburtsdatum = DateUtil.parseDateAsMidday(this.personalienGroup.get('geburtsdatum')?.value);
            const uvci = this.personalienGroup.get('uvci')?.value;
            this.searchFunction(geburtsdatum, name, vorname, uvci)
                .subscribe(response => {
                    this.searched = true;
                    const matches = response;
                    if (matches) {
                        // Resultatliste: mehrere readonly-FormGroups machen
                        this.matchFormArray = this.fb.array(matches.map(personalien => {
                            return this.fb.group({
                                daten: this.fb.control(personalien)
                            });
                        }));
                        this.matchFormArray.disable();

                        if (matches.length === 1) {
                            // Genau 1 Resultat: direkt oeffnen
                            return this.selectionFunction(matches[0]);
                        }
                    } else {
                        this.matchFormArray = undefined;
                    }
                }, error => LOG.error(error));
        });
    }

    public getResultGroups(): FormGroup[] {
        return this.matchFormArray?.controls as FormGroup[];
    }

    public hasNoResults(): boolean {
        return !this.matchFormArray || this.matchFormArray.length === 0;
    }

    public navigateToPersonendatenEdit(group: FormGroup): void {
        const daten = group.get('daten')?.value;
        if (daten) {
            this.selectionFunction(daten);
        }
    }
}
