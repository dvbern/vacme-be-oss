#Impfstoff Comirnaty Original / Omicron BA.4-5
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
							  anzahlDosenBenoetigt, code, name, hersteller, covidCertProdCode, hexFarbe,
							  zulassungsStatus,
							  informationsLink, impfstofftyp, zulassungsStatusBooster, wHoch2Code)
VALUES ('7c0330aa-3bb5-4bec-bdbd-d2d4e12047b4', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, '7680691270017', 'Comirnaty® Original / Omicron BA.4-5', 'Pfizer/BioNTech', 'EU/1/20/1528', '#279DC1',
		'EMPFOHLEN',
		'https://www.swissmedic.ch/swissmedic/de/home/news/coronavirus-covid-19/stand-zl-bekaempfung-covid-19.html',
		'MRNA', 'EMPFOHLEN', 'Comirnaty®');

INSERT IGNORE INTO Impfstoff_Krankheit (impfstoff_id, krankheit_id)
SELECT '7c0330aa-3bb5-4bec-bdbd-d2d4e12047b4', (SELECT id FROM Krankheit WHERE identifier = 'COVID');

# Impfempfehlung wenn bereits geimpft
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (id, timestampErstellt, timestampMutiert, userErstellt,
													   userMutiert, version,
													   anzahlVerabreicht, notwendigFuerChGrundimmunisierung,
													   impfstoff_id)
VALUES ('3e4a19b2-ce9a-4f8c-9777-2ada95b0eb98', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway',
		'flyway', 0,
		1, 1, '7c0330aa-3bb5-4bec-bdbd-d2d4e12047b4');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (id, timestampErstellt, timestampMutiert, userErstellt,
													   userMutiert, version,
													   anzahlVerabreicht, notwendigFuerChGrundimmunisierung,
													   impfstoff_id)
VALUES ('4149b512-6550-41c5-af12-440dc188475a', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway',
		'flyway', 0,
		2, 0, '7c0330aa-3bb5-4bec-bdbd-d2d4e12047b4');

-- Fuege eine Verknuepfung zu Comirnaty Original / Omicron BA.4-5 hinzu fuer alle Odis die bisher Comirnaty Bivalent Original/Omicron BA.1® hatten
INSERT IGNORE INTO OrtDerImpfung_Impfstoff (impfstoff_id, ortDerImpfung_id)
SELECT '7c0330aa-3bb5-4bec-bdbd-d2d4e12047b4', odi.id
FROM OrtDerImpfung odi
	 INNER JOIN OrtDerImpfung_Impfstoff ODII ON odi.id = ODII.ortDerImpfung_id
	 INNER JOIN Impfstoff I ON ODII.impfstoff_id = I.id
WHERE I.id = '765dd8e2-5294-4d85-87bb-6fce77362348';


/*
-- UNDO:
DELETE
FROM OrtDerImpfung_Impfstoff
WHERE impfstoff_id = '7c0330aa-3bb5-4bec-bdbd-d2d4e12047b4' AND ortDerImpfung_id IN (SELECT odi.id
	   FROM OrtDerImpfung odi
			INNER JOIN OrtDerImpfung_Impfstoff ODII ON odi.id = ODII.ortDerImpfung_id
			INNER JOIN Impfstoff I ON ODII.impfstoff_id = I.id
	   WHERE I.id = '765dd8e2-5294-4d85-87bb-6fce77362348');
DELETE FROM Impfstoff_Krankheit where impfstoff_id = '7c0330aa-3bb5-4bec-bdbd-d2d4e12047b4';
DELETE FROM ImpfempfehlungChGrundimmunisierung where impfstoff_id ='7c0330aa-3bb5-4bec-bdbd-d2d4e12047b4';
DELETE FROM Impfstoff where id = '7c0330aa-3bb5-4bec-bdbd-d2d4e12047b4';

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.8.3__impfstoffComirnatyBA4.sql';

*/