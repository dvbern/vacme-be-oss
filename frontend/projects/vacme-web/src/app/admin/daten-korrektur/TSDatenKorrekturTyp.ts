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

import {KorrekturDashboardJaxTS, KrankheitIdentifierTS} from 'vacme-web-generated';
import {TSRole} from '../../../../../vacme-web-shared/src/lib/model';

export enum TSDatenKorrekturTyp {
    DELETE_ACCOUNT = 'DELETE_ACCOUNT',
    EMAIL_TELEPHONE = 'EMAIL_TELEPHONE',
    IMPFUNG_DATEN = 'IMPFUNG_DATEN',
    IMPFUNG_ORT = 'IMPFUNG_ORT',
    IMPFUNG_VERABREICHUNG = 'IMPFUNG_VERABREICHUNG',
    IMPFUNG_DATUM = 'IMPFUNG_DATUM',
    DELETE_IMPFUNG = 'DELETE_IMPFUNG',
    PERSONENDATEN = 'PERSONENDATEN',
    ZERTIFIKAT= 'ZERTIFIKAT',
    ZERTIFIKAT_REVOKE_AND_RECREATE = 'ZERTIFIKAT_REVOKE_AND_RECREATE',
    SELBSTZAHLENDE = 'SELBSTZAHLENDE',
    ONBOARDING = 'ONBOARDING'
}

export function getAllowdRoles(korrektur: TSDatenKorrekturTyp): Array<TSRole> {
    const roles: Array<TSRole> = [];
    switch (korrektur) {
        case TSDatenKorrekturTyp.DELETE_ACCOUNT:
            roles.push(TSRole.AS_BENUTZER_VERWALTER);
            roles.push(TSRole.KT_NACHDOKUMENTATION);
            roles.push(TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION);
            break;
        case TSDatenKorrekturTyp.EMAIL_TELEPHONE:
            roles.push(TSRole.KT_NACHDOKUMENTATION);
            break;
        case TSDatenKorrekturTyp.IMPFUNG_DATEN:
            roles.push(TSRole.OI_DOKUMENTATION);
            roles.push(TSRole.OI_IMPFVERANTWORTUNG);
            roles.push(TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION);
            break;
        case TSDatenKorrekturTyp.IMPFUNG_ORT:
            roles.push(TSRole.OI_IMPFVERANTWORTUNG);
            roles.push(TSRole.KT_NACHDOKUMENTATION);
            roles.push(TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION);
            break;
        case TSDatenKorrekturTyp.DELETE_IMPFUNG:
            roles.push(TSRole.OI_IMPFVERANTWORTUNG);
            roles.push(TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION);
            break;
        case TSDatenKorrekturTyp.IMPFUNG_VERABREICHUNG:
            roles.push(TSRole.OI_IMPFVERANTWORTUNG);
            roles.push(TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION);
            break;
        case TSDatenKorrekturTyp.IMPFUNG_DATUM:
            roles.push(TSRole.OI_IMPFVERANTWORTUNG);
            roles.push(TSRole.KT_NACHDOKUMENTATION);
            roles.push(TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION);
            break;
        case TSDatenKorrekturTyp.PERSONENDATEN:
            roles.push(TSRole.OI_IMPFVERANTWORTUNG);
            roles.push(TSRole.KT_NACHDOKUMENTATION);
            roles.push(TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION);
            break;
        case TSDatenKorrekturTyp.ZERTIFIKAT:
            roles.push(TSRole.AS_BENUTZER_VERWALTER);
            break;
        case TSDatenKorrekturTyp.ZERTIFIKAT_REVOKE_AND_RECREATE:
            roles.push(TSRole.KT_NACHDOKUMENTATION);
            roles.push(TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION);
            break;
        case TSDatenKorrekturTyp.SELBSTZAHLENDE:
            roles.push(TSRole.OI_IMPFVERANTWORTUNG);
            roles.push(TSRole.KT_NACHDOKUMENTATION);
            roles.push(TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION);
            break;
        case TSDatenKorrekturTyp.ONBOARDING:
            roles.push(TSRole.KT_NACHDOKUMENTATION);
            roles.push(TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION);
            break;
    }
    return roles;
}

export function getAllowedKrankheitenPerKorrekturTyp(korrekturTyp: TSDatenKorrekturTyp): KrankheitIdentifierTS[] {
    const krankheiten: Array<KrankheitIdentifierTS> = [];
    switch (korrekturTyp) {
        case TSDatenKorrekturTyp.DELETE_IMPFUNG:
        case TSDatenKorrekturTyp.DELETE_ACCOUNT:
        case TSDatenKorrekturTyp.EMAIL_TELEPHONE:
        case TSDatenKorrekturTyp.IMPFUNG_DATEN:
        case TSDatenKorrekturTyp.IMPFUNG_ORT:
        case TSDatenKorrekturTyp.IMPFUNG_VERABREICHUNG:
        case TSDatenKorrekturTyp.IMPFUNG_DATUM:
        case TSDatenKorrekturTyp.PERSONENDATEN:
        case TSDatenKorrekturTyp.SELBSTZAHLENDE:
            krankheiten.push(...Object.values(KrankheitIdentifierTS));
            break;
        default:
            krankheiten.push(KrankheitIdentifierTS.COVID);
            break;
    }
    return krankheiten;
}

export function isKrankheitsUnabhaengigeKorrektur(korrekturTyp: TSDatenKorrekturTyp | undefined): boolean {
    if (korrekturTyp === undefined) {
        return false;
    }
    return [
        TSDatenKorrekturTyp.PERSONENDATEN,
        TSDatenKorrekturTyp.DELETE_ACCOUNT,
        TSDatenKorrekturTyp.EMAIL_TELEPHONE,
    ].includes(korrekturTyp);
}

export function isImpfungAbhaengigeKorrektur(korekturTyp: TSDatenKorrekturTyp | undefined): boolean {
    if (korekturTyp === undefined) {
        return false;
    }
    return [
        TSDatenKorrekturTyp.IMPFUNG_ORT,
        TSDatenKorrekturTyp.DELETE_IMPFUNG,
        TSDatenKorrekturTyp.IMPFUNG_DATEN,
        TSDatenKorrekturTyp.IMPFUNG_VERABREICHUNG,
        TSDatenKorrekturTyp.IMPFUNG_DATUM,
        TSDatenKorrekturTyp.SELBSTZAHLENDE
    ].includes(korekturTyp);
}

export function atLeastOneImpfung(dashbaord: KorrekturDashboardJaxTS | undefined): boolean {
    if (dashbaord === undefined) {
        return false;
    }
    return (dashbaord.impfdossiereintraegeEditableForRole?.length !== 0
        || (dashbaord.impfung1IfEditableForRole !== null && dashbaord.impfung1IfEditableForRole !== undefined)
        || (dashbaord.impfung2IfEditableForRole !== null && dashbaord.impfung2IfEditableForRole !== undefined));
}

export function getAllowedKorrekturTypen(
    roles: Array<TSRole> | undefined,
    krankheiten: Set<KrankheitIdentifierTS>,
): Array<TSDatenKorrekturTyp> {
    const typen: Array<TSDatenKorrekturTyp> = [];
    if (!roles) {
        return typen;
    }
    for (const korrekturTyp of Object.values(TSDatenKorrekturTyp)) {
        const intersectionRoles = roles.filter(x => getAllowdRoles(korrekturTyp).includes(x));
        const intersectionKrankheiten = getAllowedKrankheiten(intersectionRoles, korrekturTyp).filter(x => krankheiten.has(x));
        if (intersectionRoles.length > 0 && intersectionKrankheiten.length > 0) {
            typen.push(korrekturTyp);
        }
    }
    return typen;
}

export function getAllowedKrankheiten(roles: Array<TSRole> | undefined, korrekturTyp: TSDatenKorrekturTyp): Array<KrankheitIdentifierTS> {
    const allowedKrankheiten: Array<KrankheitIdentifierTS> = [];
    if (!roles) {
        return allowedKrankheiten;
    }
    for (const krankheit of Object.values(KrankheitIdentifierTS)) {
        const rolesAllowedForKorrekturtyp = roles.filter(x => getAllowdRoles(korrekturTyp).includes(x));
        if (rolesAllowedForKorrekturtyp.length > 0 && getAllowedKrankheitenPerKorrekturTyp(korrekturTyp).includes(krankheit)) {
            allowedKrankheiten.push(krankheit);
        }
    }
    return allowedKrankheiten;
}
