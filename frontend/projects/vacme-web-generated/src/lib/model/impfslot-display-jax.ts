/**
 * Generated VacMe API
 * Generated using custom templates to be found under vacme-web-generated/src/templates.
 *
 * The version of the OpenAPI document: 999.0.0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { DateTimeRangeJaxTS } from './date-time-range-jax';


export interface ImpfslotDisplayJaxTS { 
    id: string;
    zeitfenster?: DateTimeRangeJaxTS;
    /**
     * Max. Kapazitaet der ersten Impfung
     */
    kapazitaetErsteImpfung: number;
    /**
     * Max. Kapazitaet der zweiten Impfung
     */
    kapazitaetZweiteImpfung: number;
    /**
     * Max. Kapazitaet fuer Booster-Impfungen
     */
    kapazitaetBoosterImpfung: number;
}

