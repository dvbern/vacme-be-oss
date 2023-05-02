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

import {Injectable} from '@angular/core';
import {ImpfdokumentationJaxTS, KrankheitIdentifierTS} from 'vacme-web-generated';
import EnumUtil from '../../../../vacme-web-shared/src/lib/util/EnumUtil';
import {ImpfdokumentationCache, LotAndMenge} from '../model/impfdokumentation.cache';

@Injectable({
    providedIn: 'root'
})
export class ImpfdokumentationCacheService {

    private impfdokumentationPerKrankheit: Map<KrankheitIdentifierTS, ImpfdokumentationCache> =
        new Map<KrankheitIdentifierTS, ImpfdokumentationCache>();

    constructor() {
    }

    public cacheImpfdokumentation(data: ImpfdokumentationJaxTS, odiId: string, krankheit: KrankheitIdentifierTS): void {
        const cacheLotAndMenge: {[impfstoff: string]: LotAndMenge} = this.createLotAndMenge(data, krankheit);

        const impfdoku = {
            ortDerImpfungId: odiId,
            impfstoff: data.impfstoff?.id,
            lotAndMengeByImpfstoff: cacheLotAndMenge,
            verantwortlicherBenutzerId: data.verantwortlicherBenutzerId,
            durchfuehrenderBenutzerId: data.durchfuehrenderBenutzerId,
            verarbreichungsart: data.verarbreichungsart,
            verarbreichungsort: data.verarbreichungsort,
            verarbreichungsseite: data.verarbreichungsseite
        } as ImpfdokumentationCache;

        this.impfdokumentationPerKrankheit.set(krankheit, impfdoku);

        localStorage.setItem('impfdokumentation', JSON.stringify(Array.from(this.impfdokumentationPerKrankheit)));
    }

    private createLotAndMenge(data: ImpfdokumentationJaxTS, krankheit: KrankheitIdentifierTS): { [impfstoff: string]: LotAndMenge } {
        const lotAndMenge = {lot: data.lot, menge: data.menge};
        const impfdokumentation = this.getImpfdokumentation(krankheit);
        const cached = impfdokumentation?.lotAndMengeByImpfstoff;
        if (cached && data.impfstoff?.id) {
            cached[data.impfstoff?.id] = lotAndMenge;
            return cached;
        }
        if (data.impfstoff?.id) {
            return {[data.impfstoff?.id]: lotAndMenge};
        }
        return {};
    }

    public getLotAndMengeForImpfstoff(krankheit: KrankheitIdentifierTS, impfstoff?: string | null): LotAndMenge | undefined {
        const impfdokumentationCache = this.getImpfdokumentation(krankheit);
        if (impfstoff && impfdokumentationCache?.lotAndMengeByImpfstoff) {
            return impfdokumentationCache.lotAndMengeByImpfstoff[impfstoff];
        }
        return undefined;
    }

    public getImpfdokumentation(krankheit: KrankheitIdentifierTS): ImpfdokumentationCache | undefined {
        if (this.impfdokumentationPerKrankheit.size === 0) {
            let item = localStorage.getItem('impfdokumentation');
            if (item) {
                item = EnumUtil.ensureBackwardsCompatibleCache(item);
                try {
                    this.impfdokumentationPerKrankheit =
                        new Map(JSON.parse(item)) as Map<KrankheitIdentifierTS, ImpfdokumentationCache>;
                } catch (e) {
                    // Parsing failed, probably old cache object for Covid only. Try to salvage.
                    this.impfdokumentationPerKrankheit.set(KrankheitIdentifierTS.COVID, JSON.parse(item));
                }
            }
        }

        return this.impfdokumentationPerKrankheit?.has(krankheit) ?
            this.impfdokumentationPerKrankheit.get(krankheit) :
            undefined;
    }

    public cacheSelectedOdi(newOdi: string | undefined, krankheit: KrankheitIdentifierTS): void {
        let localStoredImpfdoku = this.getImpfdokumentation(krankheit);

        if (!localStoredImpfdoku) {
            localStoredImpfdoku = {} as ImpfdokumentationCache;
        }

        if (localStoredImpfdoku.ortDerImpfungId !== newOdi && newOdi) {
            localStoredImpfdoku.ortDerImpfungId = newOdi;
            localStoredImpfdoku.durchfuehrenderBenutzerId = undefined;
            localStoredImpfdoku.verantwortlicherBenutzerId = undefined;

            this.impfdokumentationPerKrankheit.set(krankheit, localStoredImpfdoku);
            localStorage.setItem('impfdokumentation', JSON.stringify(Array.from(this.impfdokumentationPerKrankheit)));
        }

    }

    public removeOdiFromCache(krankheit: KrankheitIdentifierTS): void {
        let localStoredImpfdoku = this.getImpfdokumentation(krankheit);

        if (!localStoredImpfdoku) {
            localStoredImpfdoku = {} as ImpfdokumentationCache;
        }

        localStoredImpfdoku.ortDerImpfungId = undefined;
        localStoredImpfdoku.durchfuehrenderBenutzerId = undefined;
        localStoredImpfdoku.verantwortlicherBenutzerId = undefined;

        this.impfdokumentationPerKrankheit.set(krankheit, localStoredImpfdoku);

        localStorage.setItem('impfdokumentation', JSON.stringify(Array.from(this.impfdokumentationPerKrankheit)));
    }
}
