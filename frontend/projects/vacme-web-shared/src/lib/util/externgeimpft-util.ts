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
import {AbstractControl, FormBuilder, FormGroup, Validators} from '@angular/forms';
import * as moment from 'moment';
import {Observable} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {
    ExternGeimpftJaxTS,
    ImpfstoffJaxTS,
    KrankheitIdentifierTS,
    MissingForGrundimmunisiertTS,
} from 'vacme-web-generated';
import {
    ImpfempfehlungChGrundimmunisierungJaxTS,
} from '../../../../vacme-web-generated/src/lib/model/impfempfehlung-ch-grundimmunisierung-jax';
import {Option} from '../components/form-controls/input-select/option';
import {
    DATE_PATTERN,
    DB_DEFAULT_MAX_LENGTH,
    MAX_EXTERNES_ZERTIFIKAT_IMPFUNGEN,
    MIN_DATE_FOR_POSITIV_GETESTET,
    REGEX_NUMBER_INT,
} from '../constants';
import {LogFactory} from '../logging';

import {datumInPastValidator} from './customvalidator/datum-in-past-validator';
import {minDateValidator} from './customvalidator/min-date-validator';
import {parsableDateValidator} from './customvalidator/parsable-date-validator';
import {requiredIfValidator, requiredTrueIfValidator} from './customvalidator/required-if-validator';
import DateUtil from './DateUtil';
import ValidationUtil from './ValidationUtil';

const LOG = LogFactory.createLog('ExternGeimpftUtil');

export class ExternGeimpftUtil {

    public static createFormgroup(
        fb: FormBuilder,
        krankheit: KrankheitIdentifierTS,
        showKontrolliert: boolean,
        unsubscribe$: Observable<any>,
        impfstoffOptions: Option[]
    ): FormGroup {
        if (!impfstoffOptions?.length) {
            throw new Error('Cannot create ExternGeimpft formgroup with empty impfstoffOptions');
        }
        const formGroup: FormGroup = fb.group(
            {externGeimpft: fb.control(undefined, [Validators.required])});
        formGroup.addControl('letzteImpfungDateUnknown', fb.control(undefined));
        formGroup.addControl('letzteImpfungDate', fb.control(undefined,
            [
                Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                Validators.pattern(DATE_PATTERN),
                requiredIfValidator(() => ExternGeimpftUtil.showDatumLetzteImpfung(formGroup, krankheit)),
                parsableDateValidator(),
                datumInPastValidator(),
                minDateValidator(moment(ValidationUtil.getMinDateForLetzteImpfungEZ(krankheit), DateUtil.dateFormatShort()).toDate())
            ]));
        formGroup.addControl('impfstoff', fb.control(undefined,
            [Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                requiredIfValidator(() => ExternGeimpftUtil.hasBeenExternGeimpft(formGroup))]));
        formGroup.addControl('anzahlImpfungen', fb.control(undefined,
            [Validators.min(1), Validators.max(MAX_EXTERNES_ZERTIFIKAT_IMPFUNGEN),
                Validators.pattern(REGEX_NUMBER_INT),
                requiredIfValidator(() => ExternGeimpftUtil.hasBeenExternGeimpft(formGroup))]));
        formGroup.addControl('genesen', fb.control(undefined));

        formGroup.addControl('positivGetestetDatum', fb.control(undefined,
            [
                Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                Validators.pattern(DATE_PATTERN),
                requiredIfValidator(() => ExternGeimpftUtil.showPositivGetestetDatum(formGroup, krankheit)),
                parsableDateValidator(),
                datumInPastValidator(),
                minDateValidator(moment(MIN_DATE_FOR_POSITIV_GETESTET, DateUtil.dateFormatShort()).toDate())
            ]));
        formGroup.addControl('schnellschema', fb.control(undefined));

        if (showKontrolliert) {
            formGroup.addControl('kontrolliert', fb.control(undefined, [
                requiredTrueIfValidator(() => ExternGeimpftUtil.hasBeenExternGeimpft(formGroup))]));
            formGroup.addControl('trotzdemVollstaendigGrundimmunisieren', fb.control(undefined));
        }

        formGroup.get('externGeimpft')?.valueChanges
            .pipe(takeUntil(unsubscribe$))
            .subscribe(value => {
                ExternGeimpftUtil.onExterngeimpftChange(formGroup, value);
            }, err => LOG.error(err));

        formGroup.valueChanges
            .pipe(takeUntil(unsubscribe$))
            .subscribe(() => {
                this.changeHandlerPositivGetestetDatum(formGroup, krankheit);
                this.changeHandlerDatumLetzteImpfung(formGroup, krankheit);
            }, error => LOG.error(error));

        return formGroup;
    }

    private static changeHandlerPositivGetestetDatum(formGroup: FormGroup, krankheit: KrankheitIdentifierTS): void {
        if (!ExternGeimpftUtil.showGenesen(krankheit)) {
            formGroup.get('genesen')?.setValue(undefined, {emitEvent: false});
        }
        if (!ExternGeimpftUtil.showPositivGetestetDatum(formGroup, krankheit)) {
            formGroup.get('positivGetestetDatum')?.setValue(undefined, {emitEvent: false});
        }

        // Beispielfall: Moderna 1 genesen, Testdatum leer. Wechsel nach Janssen und speichern.
        // (muesste auf impfstoff, anzahlImpfungen und genesen hoeren, deshalb hoeren wir lieber auf alle
        // zusammen)
        formGroup.get('positivGetestetDatum')?.updateValueAndValidity({emitEvent: false});
    }

    private static changeHandlerDatumLetzteImpfung(formGroup: FormGroup, krankheit: KrankheitIdentifierTS): void {
        if (!ExternGeimpftUtil.showLetzteImpfungDatumUnbekannt(krankheit)) {
            formGroup.get('letzteImpfungDateUnknown')?.setValue(undefined, {emitEvent: false});
        }
        if (!ExternGeimpftUtil.showDatumLetzteImpfung(formGroup, krankheit)) {
            formGroup.get('letzteImpfungDate')?.setValue(undefined, {emitEvent: false});
        }
        formGroup.get('letzteImpfungDate')?.updateValueAndValidity({emitEvent: false});
    }

    public static hasBeenExternGeimpft(formGroup: FormGroup): boolean {
        return formGroup?.get('externGeimpft')?.value;
    }

    public static isGenesen(formGroup: FormGroup): boolean {
        return formGroup?.get('genesen')?.value;
    }

    public static isDatumLetzteImpfungUnbekannt(formGroup: FormGroup): boolean | null | undefined {
        return formGroup?.get('letzteImpfungDateUnknown')?.value;
    }

    public static onExterngeimpftChange(formGroup: FormGroup, value?: boolean): void {
        if (!value) {
            formGroup.get('letzteImpfungDateUnknown')?.setValue(undefined);
            formGroup.get('letzteImpfungDate')?.setValue(undefined);
            formGroup.get('impfstoff')?.setValue(null); // mit undefined kommt "bitte waehlen" nicht
            formGroup.get('anzahlImpfungen')?.setValue(undefined);
            formGroup.get('genesen')?.setValue(undefined);
            formGroup.get('positivGetestetDatum')?.setValue(undefined);
            formGroup.get('kontrolliert')?.setValue(undefined);
            formGroup.updateValueAndValidity();
        }
    }

    public static formToModel(
        formGroup: FormGroup | undefined,
        impfstoffOptions: Option[],
        krankheit: KrankheitIdentifierTS
    ): ExternGeimpftJaxTS {
        if (!formGroup) {
            return {externGeimpft: false} as ExternGeimpftJaxTS;
        }
        const model: ExternGeimpftJaxTS = {
            externGeimpft: formGroup.get('externGeimpft')?.value,
            letzteImpfungDateUnknown: (this.showLetzteImpfungDatumUnbekannt(krankheit)
                ? formGroup.get('letzteImpfungDateUnknown')?.value
                : false),
            letzteImpfungDate: this.showDatumLetzteImpfung(formGroup, krankheit)
                ? DateUtil.parseDateAsMiddayOrUndefined(formGroup.get('letzteImpfungDate')?.value)
                : undefined,
            impfstoff: formGroup.get('impfstoff')?.value,
            anzahlImpfungen: formGroup.get('anzahlImpfungen')?.value,
            genesen: (this.showGenesen(krankheit)
                ? formGroup.get('genesen')?.value
                : undefined),
            positivGetestetDatum: (this.showPositivGetestetDatum(formGroup, krankheit)
                ? DateUtil.parseDateAsMiddayOrUndefined(formGroup.get('positivGetestetDatum')?.value)
                : undefined),
            trotzdemVollstaendigGrundimmunisieren: formGroup.get('trotzdemVollstaendigGrundimmunisieren')?.value,
            schnellschema: formGroup.get('schnellschema')?.value
        };
        return model;
    }

    public static updateFormFromModel(formGroup: FormGroup, externGeimpft: ExternGeimpftJaxTS,
                                      impfstoffOptions: Option[], datePipe: DatePipe): void {
        formGroup.get('externGeimpft')?.setValue(externGeimpft.externGeimpft);
        if (externGeimpft.impfstoff && impfstoffOptions) {
            const impfstoffOriginal = externGeimpft.impfstoff;
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            const impfstoff = impfstoffOptions.find(opt => opt.value.id === impfstoffOriginal.id)!.value;
            formGroup.get('impfstoff')?.setValue(impfstoff);
        }
        formGroup.get('letzteImpfungDateUnknown')?.setValue(externGeimpft.letzteImpfungDateUnknown);
        if (externGeimpft.letzteImpfungDate) {
            formGroup.get('letzteImpfungDate')?.setValue(
                datePipe.transform(externGeimpft.letzteImpfungDate?.setHours(12), 'dd.MM.yyyy'));
        }
        formGroup.get('anzahlImpfungen')?.setValue(externGeimpft.anzahlImpfungen);
        formGroup.get('genesen')?.setValue(externGeimpft.genesen);
        if (externGeimpft.positivGetestetDatum) {
            formGroup.get('positivGetestetDatum')?.setValue(
                datePipe.transform(externGeimpft.positivGetestetDatum?.setHours(12), 'dd.MM.yyyy'));
        }
        formGroup.get('kontrolliert')?.setValue(externGeimpft.kontrolliert);
        formGroup.get('trotzdemVollstaendigGrundimmunisieren')?.setValue(externGeimpft.trotzdemVollstaendigGrundimmunisieren);
        formGroup.get('schnellschema')?.setValue(externGeimpft.schnellschema);
    }

    public static showGenesen(krankheit: KrankheitIdentifierTS): boolean {
        if (KrankheitIdentifierTS.COVID === krankheit) {
            return true; // neu kann man in jedem Fall eine Erkrankung erfassen
        }
        if (KrankheitIdentifierTS.AFFENPOCKEN === krankheit) {
            return false;
        }
        if (KrankheitIdentifierTS.FSME === krankheit) {
            return false;
        }
        throw new Error('showGenesenForKrankheit nicht definiert fuer Krankheit ' + krankheit);
    }

    public static getMainQuestion(krankheit: KrankheitIdentifierTS): string {
        if (KrankheitIdentifierTS.AFFENPOCKEN === krankheit) {
            return 'EXTERN_GEIMPFT.EXTERN_GEIMPFT_AFFENPOCKEN';
        } else {
            // Generischer Text mit Angabe der Krankheit
            return 'EXTERN_GEIMPFT.EXTERN_GEIMPFT_MIT_KRANKHEIT';
        }
    }

    public static showPositivGetestetDatum(formGroup: FormGroup, krankheit: KrankheitIdentifierTS): boolean {
        return this.showGenesen(krankheit) && this.isGenesen(formGroup);
    }

    public static showTrotzdemVollstaendigGrundimmunisieren(formGroup: FormGroup, krankheit: KrankheitIdentifierTS): boolean {
        if (KrankheitIdentifierTS.COVID === krankheit) {
            const anzahlMissing = this.calculateAnzahlMissingImpfungen(formGroup, krankheit);
            return MissingForGrundimmunisiertTS.BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG !== anzahlMissing;
        }
        if (KrankheitIdentifierTS.AFFENPOCKEN === krankheit) {
            return false;
        }
        if (KrankheitIdentifierTS.FSME === krankheit) {
            return false;
        }
        throw new Error('showTrotzdemVollstaendigGrundimmunisieren nicht definiert fuer Krankheit ' + krankheit);
    }

    // Validierung auf dem FormGroup: wenn jemand "JA" waehlt, obwohl er noch nicht 2 Dosen erhalten hat
    public static calculateAnzahlMissingImpfungen(
        formGroup: AbstractControl,
        krankheit: KrankheitIdentifierTS,
    ): MissingForGrundimmunisiertTS | undefined {

        const anzahlImpfungenCtrl = formGroup.get('anzahlImpfungen');
        const externGeimpftCtrl = formGroup.get('externGeimpft');
        const impfstoffCtrl = formGroup.get('impfstoff');
        const genesenCtrl = formGroup.get('genesen');
        if (!impfstoffCtrl?.value) {
            return undefined;
        }
        const impfstoff = impfstoffCtrl.value as ImpfstoffJaxTS;

        // externGeimpft: JA, anzahl Impfungen schon eingegeben
        if (externGeimpftCtrl?.value && anzahlImpfungenCtrl?.value) {
            const anzahlMissingUmpfungenCalculated = this.calculateAnzahlMissingImpfungenBasic(
                krankheit,
                impfstoff,
                +anzahlImpfungenCtrl.value, // damit der Wert in eine Zahl konvertiert wird
                genesenCtrl?.value);
            return anzahlMissingUmpfungenCalculated;
        }
        return undefined;

    }

    // nach der Impfung 1: braucht es eine zweite Impfung (sollte man auf die Zweitimpfung verzichten koennen)?
    public static needsZweitimpfung(
        impfstoff1: ImpfstoffJaxTS,
        externGeimpft?: ExternGeimpftJaxTS
    ): boolean {
        // kein externes Zertifikat ODER BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG
        const brauchtVolleGrundimmunisierung = !externGeimpft
            || externGeimpft?.missingForGrundimmunisiertAfterDecision === MissingForGrundimmunisiertTS.BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG;

        return impfstoff1.anzahlDosenBenoetigt > 1
            && brauchtVolleGrundimmunisierung;
    }

    // should be the exact same as in backend code: ExternesZertifikat.calculateAnzahlMissingImpfungen(..)
    public static calculateAnzahlMissingImpfungenBasic(
        krankheit: KrankheitIdentifierTS,
        impfstoff?: ImpfstoffJaxTS,
        anzahlImpfungen?: number,
        genesen?: boolean
    ): MissingForGrundimmunisiertTS | undefined {
        if (!impfstoff) {
            return MissingForGrundimmunisiertTS.BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG;
        }

        const impfempfehlung = this.findImpfempfehlung(impfstoff, anzahlImpfungen, genesen);
        if (!!impfempfehlung) {
            const anzahlMissing = impfempfehlung.notwendigFuerChGrundimmunisierung;

            if (anzahlMissing === impfstoff.anzahlDosenBenoetigt) {
                return MissingForGrundimmunisiertTS.BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG;
            }
            if (anzahlMissing === 0) {
                return MissingForGrundimmunisiertTS.BRAUCHT_0_IMPFUNGEN;
            }
            if (anzahlMissing === 1) {
                return MissingForGrundimmunisiertTS.BRAUCHT_1_IMPFUNG;
            }
            if (krankheit === KrankheitIdentifierTS.FSME) {
                if (anzahlMissing === 2) {
                    return MissingForGrundimmunisiertTS.BRAUCHT_2_IMPFUNGEN;
                }
            }
        }
        return MissingForGrundimmunisiertTS.BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG;
    }

    // GENAU KOPIERT VON Impfstoff.java findImpfempfehlung
    public static findImpfempfehlung(
        impfstoff?: ImpfstoffJaxTS,
        anzahlImpfungen?: number,
        genesen?: boolean
    ): ImpfempfehlungChGrundimmunisierungJaxTS | undefined {
        if (anzahlImpfungen === undefined || !impfstoff || !impfstoff.impfempfehlungen) {
            return undefined;
        }

        // genesen zaehlt als 1 Impfung, aber nur, wenn man auch mind. 1 Impfung hat.
        const countGenesen = (anzahlImpfungen > 0 && !!genesen) ? 1 : 0;
        const impfungenPlusGenesen = anzahlImpfungen + countGenesen;
        const list = impfstoff?.impfempfehlungen
            .filter((impfempfehlung: ImpfempfehlungChGrundimmunisierungJaxTS) =>
                !!impfempfehlung.anzahlVerabreicht && impfempfehlung.anzahlVerabreicht <= impfungenPlusGenesen)
            .sort((a: ImpfempfehlungChGrundimmunisierungJaxTS, b: ImpfempfehlungChGrundimmunisierungJaxTS) =>
                // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                a.notwendigFuerChGrundimmunisierung! - b.notwendigFuerChGrundimmunisierung!);
        return list.length ? list[0] : undefined;
    }

    public static showLetzteImpfungDatumUnbekannt(krankheit: KrankheitIdentifierTS): boolean {
        if (KrankheitIdentifierTS.COVID === krankheit) {
            return false;
        }
        if (KrankheitIdentifierTS.AFFENPOCKEN === krankheit) {
            return true;
        }
        if (KrankheitIdentifierTS.FSME === krankheit) {
            return false;
        }
        throw new Error('showLetzteImpfungDate nicht definiert fuer Krankheit ' + krankheit);
    }

    public static showDatumLetzteImpfung(formGroup: FormGroup, krankheit: KrankheitIdentifierTS): boolean {
        // is there an external Impfung at all?
        if (!this.hasBeenExternGeimpft(formGroup)) {
            return false;
        }
        // does the krankheit require the datum in all cases?
        const krankheitAllowsUnbekanntDatumSelection: boolean = this.showLetzteImpfungDatumUnbekannt(krankheit);
        if (!krankheitAllowsUnbekanntDatumSelection) {
            return true;
        }
        // has the customer clicked that he knows the impfdatum?
        const lastImpfdatumIsKnown = !this.isDatumLetzteImpfungUnbekannt(formGroup);
        return lastImpfdatumIsKnown;

    }

    public static showSchnellschema(krankheit: KrankheitIdentifierTS): boolean {
        if (KrankheitIdentifierTS.COVID === krankheit) {
            return false;
        }
        if (KrankheitIdentifierTS.AFFENPOCKEN === krankheit) {
            return false;
        }
        if (KrankheitIdentifierTS.FSME === krankheit) {
            return true;
        }
        throw new Error('showSchnellschema nicht definiert fuer Krankheit ' + krankheit);
    }
}
