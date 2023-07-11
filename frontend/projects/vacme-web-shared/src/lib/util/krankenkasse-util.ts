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

import {AbstractControl, UntypedFormGroup, ValidatorFn} from '@angular/forms';
import {AuslandArtTS, KrankenkasseTS} from 'vacme-web-generated';
import {Option} from '../components/form-controls/input-select/option';
import {KRANKENKASSE_KARTENNUMMER_PRAEFIX, KRANKENKASSENNUMMERN_OHNE_PREFIX} from '../constants';
import {LogFactory} from '../logging';
import TenantUtil from './TenantUtil';

export interface KrankenkasseEnumInterface {
    name: string;
    bagNummer: string;
}

const LOG = LogFactory.createLog('KrankenkasseUtil');

export class KrankenkasseUtil {

    public static krankenkasseOptions(): Option[] {
        const options = Object.values(KrankenkasseTS).map(t => {
            return {label: t, value: t};
        });
        return options;
    };

    public static auslandArtOptions(): Option[] {
        const options = Object.values(AuslandArtTS).map(t => {
            return {label: t, value: t};
        });


        if (!TenantUtil.hasFluechtlingUeberKrankenkasse() ) {
            return options.filter(option => option.value !== 'FLUECHTLING');
        }

        return options;
    }

    public static createAuslandArtValidator(component: { formGroup: UntypedFormGroup }, krankenkasseKey: string): ValidatorFn {
        return (control: AbstractControl): null | {required: string} | {auslandArt: string} => {
            if (!(component.formGroup?.get(krankenkasseKey)?.value === KrankenkasseTS.AUSLAND)) {
                return null;
            }
            if (!control.value) {
                return {required: 'true'};
            }
            if (![AuslandArtTS.AUSLANDSCHWEIZER, AuslandArtTS.GRENZGAENGER, AuslandArtTS.FLUECHTLING].includes(control.value)
                && component.formGroup?.get(krankenkasseKey)?.value === KrankenkasseTS.AUSLAND) {
                return {auslandArt: 'true'};
            }
            return null;
        };
    }

    public static isKrankenkasseAuslandOrKeine(component: { formGroup: UntypedFormGroup }, krankenkasseKey: string): boolean {
        return component.formGroup.get(krankenkasseKey)?.value === KrankenkasseTS.AUSLAND;
    }

    public static onKrankenkasseChange(component: {
                                           formGroup: UntypedFormGroup;
                                           krankenkassen: KrankenkasseEnumInterface[];
                                           krankenkassenSelected: boolean;
                                           bezugOptions: Option[];
                                       },
                                       krankenkasseKey: string,
                                       krankenkassenNrKey: string,
                                       auslandArtKey: string): void {

        const kasse = component.krankenkassen.find((k: any) => k.name === component.formGroup.get(krankenkasseKey)?.value);
        if (!kasse) {
            LOG.warn('No Krankenkasse in list with key ' + component.formGroup.get(krankenkasseKey)?.value, component.krankenkassen);
            return;
        }
        const bagNummer = kasse.bagNummer;
        if (bagNummer !== KRANKENKASSENNUMMERN_OHNE_PREFIX) {
            component.formGroup.get(krankenkassenNrKey)?.setValue(KRANKENKASSE_KARTENNUMMER_PRAEFIX + bagNummer);
            component.formGroup.get(krankenkassenNrKey)?.enable();
        } else {
            component.formGroup.get(krankenkassenNrKey)?.setValue(bagNummer);
            component.formGroup.get(krankenkassenNrKey)?.disable();
        }
        component.krankenkassenSelected = true;

        // AuslandArt zuruecksetzen, wenn Krankenkasse nicht undefined und nicht Ausland ist
        if (!!component.formGroup.get(krankenkasseKey)?.value && !this.isKrankenkasseAuslandOrKeine(component, krankenkasseKey)) {
            component.formGroup.get(auslandArtKey)?.setValue(null);
        }

        if (this.isKrankenkasseAuslandOrKeine(component, krankenkasseKey)) {
            component.bezugOptions = this.auslandArtOptions();
        }

        // wichtig, sonst bleibt auslandArt fuer immer required, wenn es einmal war:
        component.formGroup.get(auslandArtKey)?.updateValueAndValidity();
    }

    public static isKrankenkasseSelected(component: { formGroup: UntypedFormGroup; krankenkassenSelected: boolean },
                                         kkNrKey: string): boolean {
        if (component.formGroup.get(kkNrKey)?.touched ||
            component.formGroup.get(kkNrKey)?.value &&
            component.formGroup.get(kkNrKey)?.value.length === 20) {
            return false;
        }
        return component.krankenkassenSelected;
    }

    public static krankenkasseDisableIfOhnePrefix(component: {
                                                      formGroup: UntypedFormGroup;
                                                      krankenkassen: any[];
                                                      krankenkassenSelected: boolean;
                                                  },
                                                  krankenkasseKey: string,
                                                  krankenkassenNrKey: string): void {
        const kasse = component.krankenkassen.filter((k: any) => k.name === component.formGroup.get(krankenkasseKey)?.value);
        const bagNummer = kasse[0].bagNummer;
        if (bagNummer === KRANKENKASSENNUMMERN_OHNE_PREFIX) {
            component.formGroup.get(krankenkassenNrKey)?.setValue(bagNummer);
            component.formGroup.get(krankenkassenNrKey)?.disable();
        }
    }

}
