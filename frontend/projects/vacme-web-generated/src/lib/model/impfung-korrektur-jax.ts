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
import { KrankheitIdentifierTS } from './krankheit-identifier';
import { ImpffolgeTS } from './impffolge';


export interface ImpfungKorrekturJaxTS { 
    impffolge: ImpffolgeTS;
    impfstoff: string;
    lot: string;
    menge: number;
    impffolgeNr?: number;
    krankheitIdentifier: KrankheitIdentifierTS;
}

