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
import { ChronischeKrankheitenTS } from './chronische-krankheiten';
import { AdresseJaxTS } from './adresse-jax';
import { KrankheitIdentifierTS } from './krankheit-identifier';
import { ExternGeimpftJaxTS } from './extern-geimpft-jax';
import { ImpfungJaxTS } from './impfung-jax';
import { OrtDerImpfungDisplayNameJaxTS } from './ort-der-impfung-display-name-jax';
import { ImpfterminJaxTS } from './impftermin-jax';
import { LebensumstaendeTS } from './lebensumstaende';
import { RegistrierungStatusTS } from './registrierung-status';
import { BeruflicheTaetigkeitTS } from './berufliche-taetigkeit';
import { PrioritaetTS } from './prioritaet';
import { KrankenkasseTS } from './krankenkasse';
import { AuslandArtTS } from './ausland-art';
import { ImpfkontrolleTerminJaxTS } from './impfkontrolle-termin-jax';
import { ImpfschutzJaxTS } from './impfschutz-jax';
import { GeschlechtTS } from './geschlecht';
import { RegistrierungsEingangTS } from './registrierungs-eingang';
import { AmpelColorTS } from './ampel-color';


export interface ImpfkontrolleJaxTS {
    krankheitIdentifier: KrankheitIdentifierTS;
    impffolgeNr: number;
    registrierungsnummer?: string;
    impfdossierEintragId?: string;
    prioritaet?: PrioritaetTS;
    status?: RegistrierungStatusTS;
    geschlecht?: GeschlechtTS;
    name?: string;
    vorname?: string;
    adresse?: AdresseJaxTS;
    immobil?: boolean;
    abgleichElektronischerImpfausweis?: boolean;
    contactTracing?: boolean;
    mail?: string;
    telefon?: string;
    krankenkasse?: KrankenkasseTS;
    krankenkasseKartenNr?: string;
    auslandArt?: AuslandArtTS;
    geburtsdatum?: Date;
    verstorben?: boolean;
    bemerkung?: string;
    ampelColor?: AmpelColorTS;
    chronischeKrankheiten?: ChronischeKrankheitenTS;
    lebensumstaende?: LebensumstaendeTS;
    beruflicheTaetigkeit?: BeruflicheTaetigkeitTS;
    identifikationsnummer?: string;
    impfungkontrolleTermin?: ImpfkontrolleTerminJaxTS;
    termin1?: ImpfterminJaxTS;
    termin2?: ImpfterminJaxTS;
    terminNPending?: ImpfterminJaxTS;
    impfung1?: ImpfungJaxTS;
    impfung2?: ImpfungJaxTS;
    gewuenschterOrtDerImpfung?: OrtDerImpfungDisplayNameJaxTS;
    nichtVerwalteterOdiSelected?: boolean;
    eingang?: RegistrierungsEingangTS;
    externGeimpft?: ExternGeimpftJaxTS;
    schutzstatus?: boolean;
    keinKontakt?: boolean;
    impfschutzJax?: ImpfschutzJaxTS;
    leistungerbringerAgbConfirmationNeeded: boolean;
}

