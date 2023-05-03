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
import {DATE_PATTERN, DB_DEFAULT_MAX_LENGTH} from '../constants';
import {LogFactory} from '../logging';
import {datumInPastValidator} from '../util/customvalidator/datum-in-past-validator';
import {parsableDateValidator} from '../util/customvalidator/parsable-date-validator';
import DateUtil from '../util/DateUtil';
import FormUtil from '../util/FormUtil';

const LOG = LogFactory.createLog('PersonalienSucheComponent');

@Component({
  selector: 'lib-personalien-suche',
  templateUrl: './personalien-suche.component.html',
  styleUrls: ['./personalien-suche.component.scss']
})
export class PersonalienSucheComponent implements OnInit {

    public personalienGroup!: FormGroup;
    public matchFormArray?: FormArray;
    public searched = false;

    @Input()
    public searchFunction!: (geburtsdatum: Date, name: string, vorname: string) => Observable<any[]>;

    @Input()
    public selectionFunction!: (daten: any) => void;

    @Input()
    public selectionButtonKey!: string;

    @Input()
    public showResultAdressen = true;

    @Input()
    public showResultRegNr = false;

    @Input()
    public pageTitle = 'CALLCENTER.PERSONALIEN-SUCHE.TITLE';

    constructor(
        private fb: FormBuilder,
        private datePipe: DatePipe
    ) { }

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
        });
    }

    public personalienSuchen(): void {
        FormUtil.doIfValid(this.personalienGroup, () => {
            const name = this.personalienGroup.get('name')?.value;
            const vorname = this.personalienGroup.get('vorname')?.value;
            const geburtsdatum = DateUtil.parseDateAsMidday(this.personalienGroup.get('geburtsdatum')?.value);
            this.searchFunction(geburtsdatum, name, vorname)
                .subscribe(response => {
                    this.searched = true;
                    const matches = response;
                    // Pro Resultat eine Box
                    if (matches) {
                        this.matchFormArray = this.fb.array(matches.map(personalien => {
                            return this.fb.group({
                                name: this.fb.control(personalien.name),
                                vorname: this.fb.control(personalien.vorname),
                                geburtsdatum: this.fb.control(
                                    this.datePipe.transform(personalien.geburtsdatum?.setHours(12), 'dd.MM.yyyy')),
                                strasse: this.fb.control(personalien.adresse?.adresse1),
                                plz: this.fb.control(personalien.adresse?.plz),
                                ort: this.fb.control(personalien.adresse?.ort),
                                regNummer: this.fb.control(personalien.regNummer),
                                daten: this.fb.control(personalien)
                            });
                        }));
                        this.matchFormArray.disable();

                        // Wenn genau 1 Resultat: direkt oeffnen
                        if (matches?.length === 1) {
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
