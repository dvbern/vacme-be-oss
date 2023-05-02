import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {RegistrierungService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {LatLngJaxTS} from '../../../../vacme-web-generated/src/lib/model/lat-lng-jax';
import {
    OrtDerImpfungDisplayNameExtendedJaxTS,
} from '../../../../vacme-web-generated/src/lib/model/ort-der-impfung-display-name-extended-jax';
import {VacmeSettingsService} from '../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import {GeometryUtil} from '../../../../vacme-web-shared/src/lib/util/geometry-util';

const LOG = LogFactory.createLog('OdiDistanceCache');

@Injectable({
    providedIn: 'root',
})
export class OdiDistanceCacheService {

    private mappings = new Map<string, { regLatLng: LatLngJaxTS; odiDistance: Map<string, number | undefined> }>();

    constructor(
        private registrierungService: RegistrierungService,
        private vacmeSettingsService: VacmeSettingsService,
    ) {
    }

    public calculateOdiDistances$(
        odiList: OrtDerImpfungDisplayNameExtendedJaxTS[],
        registrierungsnummer: string | undefined,
    ): Observable<OrtDerImpfungDisplayNameExtendedJaxTS[]> {
        if (!this.vacmeSettingsService.geocodingEnabled) {
            return of(odiList);
        }
        if (registrierungsnummer) {
            // If we know this reg's LatLng, go to distance calculation.
            if (this.mappings.has(registrierungsnummer)) {
                this.buildOdiDistanceCache(odiList, registrierungsnummer);
                return of(odiList);
            } else {
                // Else, query the LatLng and then go to distance calculation.
                return this.registrierungService.registrierungResourceGetGeocodedAddress(registrierungsnummer)
                    .pipe(map(latLong => {
                        // In absprache mit Michael Gillman ueber das verhalten von CC_AGENT usern:
                        // Es ist unwarhscheindlich, dass ein CC_AGENT eine registrierungsnummer mehrmals in kurzen
                        // abstaenden verwende.
                        this.mappings.clear();
                        this.mappings.set(registrierungsnummer,
                            {regLatLng: latLong, odiDistance: new Map<string, number>()});
                        this.buildOdiDistanceCache(odiList, registrierungsnummer);
                        return odiList;
                    }));
            }
        }
        return of(odiList);
    }

    private buildOdiDistanceCache(
        odiList: OrtDerImpfungDisplayNameExtendedJaxTS[],
        registrierungsnummer: string,
    ): void {
        const regLatLng = this.mappings.get(registrierungsnummer)?.regLatLng;
        const odiDistances = this.mappings.get(registrierungsnummer)?.odiDistance;
        if (odiDistances && regLatLng) {
            for (const odi of odiList) {
                if (odi.id) {
                    if (!odiDistances.has(odi.id) && odi.latLng?.lng && odi.latLng?.lat) {
                        const distance = GeometryUtil.computeDistanceBetween(regLatLng, odi.latLng);
                        odiDistances.set(odi.id, distance);
                    }
                    odi.distanceToReg = odiDistances.get(odi.id);
                }
            }
        }
    }

    public clear(): void {
        this.mappings.clear();
    }
}
