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

import {ChangeDetectionStrategy, Component, EventEmitter, forwardRef, Input, Output, ViewChild} from '@angular/core';
import {NG_VALUE_ACCESSOR} from '@angular/forms';
import {NgbTypeahead, NgbTypeaheadSelectItemEvent} from '@ng-bootstrap/ng-bootstrap';
import {merge, Observable, Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, map} from 'rxjs/operators';

@Component({
    selector: 'lib-input-typeahead',
    templateUrl: './input-typeahead.component.html',
    styleUrls: ['./input-typeahead.component.scss'],
    changeDetection: ChangeDetectionStrategy.Default, // OnPush cannot detect "touched" programmatically
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            multi: true,
            useExisting: forwardRef(() => InputTypeaheadComponent),
        },
    ],
})
export class InputTypeaheadComponent {

    @Input() listOfObjects: Array<any> = [];
    // The following properties defalult values are fine when using this component as a replacement for input-select
    @Input() labelProperty = 'label';
    @Input() valueProperty = 'value';
    // Controls typeahead behaviour "simple typeahead" or "open on focus"
    // see: https://ng-bootstrap.github.io/#/components/typeahead/examples
    @Input() popUpOnFucus = true;
    @Input() disabled = false;
    @Input() required = false;
    @Input() labelKey = '';
    @Input() placeholderKey?: string;
    @Input() model: any;

    @Output() inputChanged = new EventEmitter<NgbTypeaheadSelectItemEvent>();

    @ViewChild('instance', {static: true}) instance!: NgbTypeahead;
    private _focus$ = new Subject<string>();
    private _click$ = new Subject<string>();

    private readonly DEBOUNCE_TIEM = 200;
    private readonly NUM_CHAR_TO_OPEN_LIST = 1;
    private readonly DROP_LIST_SIZE = 10;

    static toLower(value: string | undefined): string {
        return value ? value.toLowerCase() : '';
    }

    static stringify(value: any): string {
        return String(value);
    }

    onSelect(selection: NgbTypeaheadSelectItemEvent): void {
        if (selection.item) {
            if (selection.item.disabled) {
                // ngxTypeahead unterstuetzt keine disabled Options, siehe wontfix hier:
                // https://github.com/ng-bootstrap/ng-bootstrap/issues/2160
                this.model = undefined;
            } else {
                this.inputChanged.emit(selection.item);
            }
        }
    }

    getId(): string {
        return 'typeahead-' + this.labelKey;
    }

    formatter = (value: any): string => {
        return this.defaultFormatter(value);
    };

    // Search funktion fuer die Varianten "Simple typeahead" und "Open on focus"
    // in Abhaengigkeit von popUpOnFucus, Siehe:
    // https://ng-bootstrap.github.io/#/components/typeahead/examples
    searcher = (text$: Observable<string>): Observable<Array<any>> => {
        const debouncedText$ = text$.pipe(debounceTime(this.DEBOUNCE_TIEM), distinctUntilChanged());
        const clicksWithClosedPopup$ = this._click$.pipe(filter(() => this.instance && !this.instance.isPopupOpen()));
        const inputFocus$ = this._focus$;

        if (this.popUpOnFucus) {
            return merge(debouncedText$, inputFocus$, clicksWithClosedPopup$).pipe(
                map(term => term === '' ? this.listOfObjects :
                    this.listOfObjects.filter(v => InputTypeaheadComponent.toLower(v[this.labelProperty])
                        .indexOf(term.toLowerCase()) >= 0)
                        .slice(0, this.DROP_LIST_SIZE)),
            );
        } else {
            return merge(debouncedText$).pipe(
                map(term => term.length < this.NUM_CHAR_TO_OPEN_LIST ? [] :
                    this.listOfObjects.filter(v => InputTypeaheadComponent.toLower(v[this.labelProperty])
                        .indexOf(term.toLowerCase()) >= 0)
                        .slice(0, this.DROP_LIST_SIZE)),
            );
        }
    };

    private defaultFormatter(value: any): string {
        if (value && value[this.labelProperty] !== undefined) {
            return value[this.labelProperty];
        }
        return InputTypeaheadComponent.stringify(value);

    }

    public emitFocus(val: string): void{
        return this._focus$.next(val);
    }
    public emitClick(val: string): void{
        return this._click$.next(val);
    }

}
