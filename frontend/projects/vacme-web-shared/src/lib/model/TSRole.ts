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

export enum TSRole {

    // Default
    IMPFWILLIGER= 'IMPFWILLIGER,',

    // Callcenter
    // 	CC
    CC_AGENT= 'CC_AGENT,',
    CC_BENUTZER_VERWALTER= 'CC_BENUTZER_VERWALTER,',

    // Ort der Impfung
    // 	OI
    OI_KONTROLLE= 'OI_KONTROLLE,',
    OI_DOKUMENTATION= 'OI_DOKUMENTATION,',
    OI_IMPFVERANTWORTUNG= 'OI_IMPFVERANTWORTUNG',
    OI_FACHBAB_DELEGIEREN= 'OI_FACHBAB_DELEGIEREN',
    OI_BENUTZER_VERWALTER= 'OI_BENUTZER_VERWALTER,',
    OI_ORT_VERWALTER= 'OI_ORT_VERWALTER,',
    OI_BENUTZER_REPORTER= 'OI_BENUTZER_REPORTER,',
    OI_LOGISTIK_REPORTER= 'OI_LOGISTIK_REPORTER,',
    OI_MEDIZINISCHER_REPORTER= 'OI_MEDIZINISCHER_REPORTER,',

    // Kanton
    // 	KT
    KT_IMPFVERANTWORTUNG= 'KT_IMPFVERANTWORTUNG,',
    KT_BENUTZER_REPORTER= 'KT_BENUTZER_REPORTER,',
    KT_LOGISTIK_REPORTER= 'KT_LOGISTIK_REPORTER,',
    KT_MEDIZINISCHER_REPORTER= 'KT_MEDIZINISCHER_REPORTER,',
    KT_BENUTZER_VERWALTER= 'KT_BENUTZER_VERWALTER,',
    KT_ZERTIFIKAT_AUSSTELLER= 'KT_ZERTIFIKAT_AUSSTELLER,',
    KT_NACHDOKUMENTATION= 'KT_NACHDOKUMENTATION,',
    KT_MEDIZINISCHE_NACHDOKUMENTATION = 'KT_MEDIZINISCHE_NACHDOKUMENTATION',
    KT_IMPFDOKUMENTATION = 'KT_IMPFDOKUMENTATION',


    AS_REGISTRATION_OI= 'AS_REGISTRATION_OI,',
    AS_BENUTZER_VERWALTER = 'AS_BENUTZER_VERWALTER',

    UNASSIGNED_ROLE = 'UNASSIGNED_ROLE',
}

export function getTSRoleValues(): Array<TSRole> {
    return [
        TSRole.IMPFWILLIGER,
        TSRole.CC_AGENT,
        TSRole.CC_BENUTZER_VERWALTER,
        TSRole.OI_KONTROLLE,
        TSRole.OI_DOKUMENTATION,
        TSRole.OI_BENUTZER_VERWALTER,
        TSRole.OI_ORT_VERWALTER,
        TSRole.OI_BENUTZER_REPORTER,
        TSRole.OI_LOGISTIK_REPORTER,
        TSRole.OI_MEDIZINISCHER_REPORTER,
        TSRole.KT_IMPFVERANTWORTUNG,
        TSRole.KT_BENUTZER_REPORTER,
        TSRole.KT_LOGISTIK_REPORTER,
        TSRole.KT_MEDIZINISCHER_REPORTER,
        TSRole.KT_BENUTZER_VERWALTER,
        TSRole.KT_ZERTIFIKAT_AUSSTELLER,
        TSRole.KT_NACHDOKUMENTATION,
        TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION,
        TSRole.KT_IMPFDOKUMENTATION,
        TSRole.AS_REGISTRATION_OI,
        TSRole.AS_BENUTZER_VERWALTER,
        TSRole.UNASSIGNED_ROLE,

    ];
}

export function rolePrefix(): string {
    return 'TSRole_';
}
