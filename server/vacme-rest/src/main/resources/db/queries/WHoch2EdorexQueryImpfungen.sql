(SELECT 'Registrierungs_ID', 'Ort_der_Impfung_ID', 'Ort_der_Impfung_Name', 'Ort_der_Impfung_GLN', 'Ort_der_Impfung_Typ',
		'Termin_Impfung', 'Impfung_am', 'Impfstoff_Name', 'Impfstoff_ID', 'Impfung_extern', 'Grundimmunisierung',
		'Impffolgenummer')
UNION
(SELECT registrierung.id AS 'Registrierungs_ID', odi.identifier AS 'Ort_der_Impfung_ID',
		odi.name AS 'Ort_der_Impfung_Name', odi.glnNummer AS 'Ort_der_Impfung_GLN', odi.typ AS 'Ort_der_Impfung_Typ',
		DATE_FORMAT(slot.von, '%d.%m.%Y %T') AS 'Termin_Impfung',
		DATE_FORMAT(impfung.timestampImpfung, '%d.%m.%Y %T') AS 'Impfung_am', impfstoff.name AS 'Impfstoff_Name',
		impfstoff.id AS 'Impfstoff_ID', CASE WHEN impfung.extern = 1
												 THEN 'TRUE'
											 WHEN impfung.extern = 0
												 THEN 'FALSE'
											 ELSE null END AS 'Impfung_extern',
		CASE WHEN impfung.grundimmunisierung = 0 THEN 'FALSE' ELSE 'TRUE' END AS 'Grundimmunisierung',
		1 AS 'Impffolgenummer'
 FROM Impftermin termin
	  LEFT JOIN Impfung impfung ON termin.id = impfung.termin_id
	  LEFT JOIN Impfstoff impfstoff ON impfung.impfstoff_id = impfstoff.id
	  INNER JOIN Impfslot slot ON termin.impfslot_id = slot.id
	  INNER JOIN OrtDerImpfung odi ON slot.ortDerImpfung_id = odi.id
      INNER JOIN Impfdossier D ON termin.id = D.impftermin1_id
	  INNER JOIN Registrierung registrierung ON D.registrierung_id = registrierung.id)
UNION
(SELECT registrierung.id AS 'Registrierungs_ID', odi.identifier AS 'Ort_der_Impfung_ID',
		odi.name AS 'Ort_der_Impfung_Name', odi.glnNummer AS 'Ort_der_Impfung_GLN', odi.typ AS 'Ort_der_Impfung_Typ',
		DATE_FORMAT(slot.von, '%d.%m.%Y %T') AS 'Termin_Impfung',
		DATE_FORMAT(impfung.timestampImpfung, '%d.%m.%Y %T') AS 'Impfung_am', impfstoff.name AS 'Impfstoff_Name',
		impfstoff.id AS 'Impfstoff_ID', CASE WHEN impfung.extern = 1
												 THEN 'TRUE'
											 WHEN impfung.extern = 0
												 THEN 'FALSE'
											 ELSE null END AS 'Impfung_extern',
		CASE WHEN impfung.grundimmunisierung = 0 THEN 'FALSE' ELSE 'TRUE' END AS 'Grundimmunisierung',
		2 AS 'Impffolgenummer'
 FROM Impftermin termin
	  LEFT JOIN Impfung impfung ON termin.id = impfung.termin_id
	  LEFT JOIN Impfstoff impfstoff ON impfung.impfstoff_id = impfstoff.id
	  INNER JOIN Impfslot slot ON termin.impfslot_id = slot.id
	  INNER JOIN OrtDerImpfung odi ON slot.ortDerImpfung_id = odi.id
      INNER JOIN Impfdossier D ON termin.id = D.impftermin2_id
	  INNER JOIN Registrierung registrierung ON D.registrierung_id = registrierung.id)
UNION
(SELECT registrierung.id AS 'Registrierungs_ID', odi.identifier AS 'Ort_der_Impfung_ID',
		odi.name AS 'Ort_der_Impfung_Name', odi.glnNummer AS 'Ort_der_Impfung_GLN', odi.typ AS 'Ort_der_Impfung_Typ',
		DATE_FORMAT(slot.von, '%d.%m.%Y %T') AS 'Termin_Impfung',
		DATE_FORMAT(impfung.timestampImpfung, '%d.%m.%Y %T') AS 'Impfung_am', impfstoff.name AS 'Impfstoff_Name',
		impfstoff.id AS 'Impfstoff_ID', CASE WHEN impfung.extern = 1
												 THEN 'TRUE'
											 WHEN impfung.extern = 0
												 THEN 'FALSE'
											 ELSE null END AS 'Impfung_extern',
		CASE WHEN impfung.grundimmunisierung = 1
				 THEN 'TRUE'
			 WHEN impfung.grundimmunisierung = 0
				 THEN 'FALSE'
			 ELSE NULL END AS 'Grundimmunisierung', eintrag.impffolgeNr AS 'Impffolgenummer'
 into outfile '/tmp/${PREFIX_IMPFUNGEN}_${DATE}.csv' fields terminated by ',' optionally enclosed by '\"' LINES TERMINATED BY '\n'
 FROM Impftermin termin
	  LEFT JOIN Impfung impfung ON termin.id = impfung.termin_id
	  LEFT JOIN Impfstoff impfstoff ON impfung.impfstoff_id = impfstoff.id
	  INNER JOIN Impfslot slot ON termin.impfslot_id = slot.id
	  INNER JOIN OrtDerImpfung odi ON slot.ortDerImpfung_id = odi.id
	  INNER JOIN Impfdossiereintrag eintrag ON termin.id = eintrag.impftermin_id
	  INNER JOIN Impfdossier dossier ON eintrag.impfdossier_id = dossier.id
	  INNER JOIN Registrierung registrierung ON dossier.registrierung_id = registrierung.id
 WHERE dossier.krankheitIdentifier = 'COVID');