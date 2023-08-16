/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

import {ActivatedRouteSnapshot} from '@angular/router';
import {KrankheitIdentifierTS, OrtDerImpfungJaxTS} from 'vacme-web-generated';

export function getEnumFromString(krankheitName: string): KrankheitIdentifierTS {
    return KrankheitIdentifierTS[krankheitName as keyof typeof KrankheitIdentifierTS];
}

export function isKrankheitName(krankheitName: string): boolean {
	return !!getEnumFromString(krankheitName);
}

export function hasOdiForKrankheit(
    odiList: Array<OrtDerImpfungJaxTS>,
    krankheitIdentifier: KrankheitIdentifierTS
): boolean {
    return Array.from(extractKrankheitenForOdis(odiList)).includes(krankheitIdentifier);
}

export function extractKrankheitenForOdis(odis: OrtDerImpfungJaxTS[] | undefined): Set<KrankheitIdentifierTS>{
    const krankheitenSet = new Set<KrankheitIdentifierTS>();
    if (odis) {
        odis.map(odi => odi.krankheiten?.forEach(value => krankheitenSet.add(value.identifier)));
    }
    return krankheitenSet;
}

export function filterOdisByKrankheit(
    odis: OrtDerImpfungJaxTS[] | undefined,
    krankheit: KrankheitIdentifierTS
): OrtDerImpfungJaxTS[] {
    const odiSet = new Set<OrtDerImpfungJaxTS>();
    if (odis) {
        odis.map(odi => odi.krankheiten?.forEach(value => {
            if (value.identifier === krankheit) {
                odiSet.add(odi);
            }
        }));
    }
    const odiArray = [];
    let index = 0;
    for (const ortDerImpfungJaxT of odiSet) {
        odiArray[index] = ortDerImpfungJaxT;
        index++;
    }
    return odiArray;
}

export function extractKrankheitFromRoute(route: ActivatedRouteSnapshot): KrankheitIdentifierTS | undefined {
    const krankheitName = route.params.krankheit as string;
    if (krankheitName !== undefined && krankheitName !== '') {
        if (isKrankheitName(krankheitName)) {
            return getEnumFromString(krankheitName);
        }
    }
    return undefined;
}
