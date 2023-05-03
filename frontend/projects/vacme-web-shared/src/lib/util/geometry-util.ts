import {EARTH_RADIUS} from '../constants';
import {LatLngJaxTS } from 'vacme-web-generated';
export class GeometryUtil {

    /**
     * @param from
     * @param to
     * @returns
     */
    public static computeDistanceBetween(from: LatLngJaxTS, to: LatLngJaxTS): number | undefined {
        if (from.lng && from.lat && to.lng && to.lat) {
            const radFromLat = GeometryUtil.toRadians(from.lat);
            const radFromLng = GeometryUtil.toRadians(from.lng);
            const radToLat = GeometryUtil.toRadians(to.lat);
            const radToLng = GeometryUtil.toRadians(to.lng);
            return (
                2 *
                Math.asin(
                    Math.sqrt(
                        Math.pow(Math.sin((radFromLat - radToLat) / 2), 2) +
                        Math.cos(radFromLat) *
                        Math.cos(radToLat) *
                        Math.pow(Math.sin((radFromLng - radToLng) / 2), 2)
                    )
                )
            ) * EARTH_RADIUS;
        }

        return undefined;
    }

    private static toRadians(angleDegrees: number): number {
        return (angleDegrees * Math.PI) / 180.0;
    }
}
