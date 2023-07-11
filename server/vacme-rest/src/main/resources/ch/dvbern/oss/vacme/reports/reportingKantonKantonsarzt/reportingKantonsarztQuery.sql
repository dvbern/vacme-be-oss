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

select
	Registrierung.id as 'Registrierungs_ID',
	date_format(Registrierung.registrationTimestamp, '%d.%m.%Y %T') as 'Registriert_am',
	Registrierung.registrierungsEingang as 'RegistrierungsEingang',
	Registrierung.geschlecht as 'Geschlecht',
	CASE WHEN Registrierung.immobil = 1 THEN 'TRUE' ELSE 'FALSE' END  as 'Immobil',
	date_format(Registrierung.geburtsdatum, '%d.%m.%Y') as 'Geburtsdatum',
	Registrierung.registrierungsnummer as 'Registrierungsnummer',
	Registrierung.name as 'Name',
	Registrierung.vorname as 'Vorname',
	Registrierung.adresse1 as 'Adresse_1',
	Registrierung.adresse2 as 'Adresse_2',
	Registrierung.plz as 'PLZ',
   	Registrierung.ort as 'Ort',
	CASE WHEN Registrierung.abgleichElektronischerImpfausweis = 1 THEN 'TRUE' ELSE 'FALSE' END  as 'Abgleich_elektronischer_Impfausweis',
	CASE WHEN Registrierung.contactTracing = 1 THEN 'TRUE' ELSE 'FALSE' END  as 'Abgleich_Contact_tracing',
	CASE WHEN dossier.vollstaendigerImpfschutzTyp IS NOT NULL THEN 'TRUE' ELSE 'FALSE' END  as 'Vollstaendiger_Impfschutz',
	Fragebogen.chronischeKrankheiten as 'Chronische_Krankheiten',
	Fragebogen.lebensumstaende as 'Lebensumstaende',
	Fragebogen.beruflicheTaetigkeit as 'Beruf',
	Registrierung.prioritaet as 'Imfgruppe',
	CASE WHEN Registrierung.verstorben = 1 THEN '1' WHEN Registrierung.verstorben = 0 THEN '0' ELSE NULL END as 'Verstorben',
	date_format(impfschutz.immunisiertBis, '%d.%m.%Y') as 'Immunisiert_bis',
	date_format(impfschutz.freigegebenNaechsteImpfungAb, '%d.%m.%Y') as 'Freigegeben_naechste_Impfung_ab',
	impfschutz.erlaubteImpfstoffe as 'erlaubte_impfstoffe_fuer_booster',
    CASE WHEN dossier.genesen = 1 THEN 'TRUE' ELSE 'FALSE' END  as 'genesen',
	date_format(dossier.positivGetestetDatum, '%d.%m.%Y') as 'Datum_positiver_Test',
	CASE WHEN dossier.selbstzahler = 1 THEN 'TRUE' WHEN dossier.selbstzahler = 0 THEN 'FALSE' ELSE NULL END AS 'Selbstzahler'

from Registrierung
		 inner join Fragebogen on Fragebogen.registrierung_id = Registrierung.id
		 left outer join Impfdossier as dossier ON Registrierung.id = dossier.registrierung_id
		 left outer join Impfschutz as impfschutz on dossier.impfschutz_id = impfschutz.id
		 left outer join Krankheit as K on dossier.krankheitIdentifier = K.identifier
where dossier.krankheitIdentifier = 'COVID' and K.kantonaleBerechtigung = 'KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG';
