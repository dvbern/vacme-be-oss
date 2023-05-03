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

SELECT registrierung.id AS 'Registrierungs_ID',
	   odi.identifier AS 'Ort_der_Impfung_ID',
	   odi.name AS 'Ort_der_Impfung_Name',
	   odi.glnNummer AS 'Ort_der_Impfung_GLN',
	   odi.typ AS 'Ort_der_Impfung_Typ',
	   DATE_FORMAT(slot.von, '%d.%m.%Y %T') AS 'Termin_Impfung',
	   DATE_FORMAT(impfung.timestampImpfung, '%d.%m.%Y %T') AS 'Impfung_am',
	   impfstoff.name AS 'Impfstoff_Name',
	   impfstoff.id AS 'Impfstoff_ID',
	   CASE WHEN impfung.extern = 1 THEN 'TRUE' WHEN impfung.extern = 0 THEN 'FALSE' ELSE null END AS 'Impfung_extern',
	   CASE WHEN impfung.grundimmunisierung = 1 THEN 'TRUE' WHEN impfung.grundimmunisierung = 0 THEN 'FALSE' ELSE NULL END AS 'Grundimmunisierung',
	   eintrag.impffolgeNr AS 'Impffolgenummer',
	   CASE WHEN impfung.selbstzahlende = 1 THEN 'TRUE' WHEN impfung.selbstzahlende = 0 THEN 'FALSE' ELSE NULL END AS 'Impfung_selbstzahlende',
	   CASE WHEN Fragebogen.immunsupprimiert = 1 THEN 'TRUE' WHEN Fragebogen.immunsupprimiert = 0 THEN 'FALSE' ELSE NULL END AS 'Immunsupprimiert',
	   dossier.krankheitIdentifier AS 'Krankheit'

FROM Impftermin termin
	 LEFT JOIN Impfung impfung ON termin.id = impfung.termin_id
	 LEFT JOIN Impfstoff impfstoff ON impfung.impfstoff_id = impfstoff.id
	 INNER JOIN Impfslot slot ON termin.impfslot_id = slot.id
	 INNER JOIN OrtDerImpfung odi ON slot.ortDerImpfung_id = odi.id
	 INNER JOIN Impfdossiereintrag eintrag ON termin.id = eintrag.impftermin_id
	 INNER JOIN Impfdossier dossier ON eintrag.impfdossier_id = dossier.id
	 INNER JOIN Registrierung registrierung ON dossier.registrierung_id = registrierung.id
	 inner join Fragebogen on Fragebogen.registrierung_id = registrierung.id
WHERE impfung.kantonaleBerechtigung = 'KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG';