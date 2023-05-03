INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
							  anzahlDosenBenoetigt, code, name, hersteller, covidCertProdCode, hexFarbe,
							  zulassungsStatus,
							  informationsLink, impfstofftyp, zulassungsStatusBooster)
VALUES ('765dd8e2-5294-4d85-87bb-6fce77362348', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, '7680690470012', 'Comirnaty Bivalent Original/Omicron BA.1®', 'Pfizer/BioNTech', 'EU/1/20/1528', '#888888',
		'EMPFOHLEN', null, 'MRNA', 'EMPFOHLEN');

# Impfempfehlung wenn bereits geimpft
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlVerabreicht, notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES (
		   'a8122d40-8e95-4c97-8a31-d8c6a159998e', '2022-10-10 00:00:00.000000', '2022-10-10 00:00:00.000000', 'flyway', 'flyway', 0,
		   1, 1, '765dd8e2-5294-4d85-87bb-6fce77362348');
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlVerabreicht, notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES (
		   'bc990129-e02a-4f70-9272-5271ea9dda15', '2022-10-10 00:00:00.000000', '2022-10-10 00:00:00.000000', 'flyway', 'flyway', 0,
		   2, 0, '765dd8e2-5294-4d85-87bb-6fce77362348');

-- Fuege eine Verknuepfung zu Pfizer Comirnaty Bivalent hinzu fuer alle Odis die bisher Comirnaty Monovalent hatten
INSERT IGNORE INTO OrtDerImpfung_Impfstoff (impfstoff_id, ortDerImpfung_id)
SELECT '765dd8e2-5294-4d85-87bb-6fce77362348', odi.id
FROM OrtDerImpfung odi
	 INNER JOIN OrtDerImpfung_Impfstoff ODII ON odi.id = ODII.ortDerImpfung_id
	 INNER JOIN Impfstoff I ON ODII.impfstoff_id = I.id
WHERE I.id = '141fca55-ab78-4c0e-a2fd-edf2fe4e9b30';

/*
-- UNDO:

DELETE
FROM OrtDerImpfung_Impfstoff
WHERE impfstoff_id = '765dd8e2-5294-4d85-87bb-6fce77362348' AND ortDerImpfung_id IN (SELECT odi.id
	   FROM OrtDerImpfung odi
			INNER JOIN OrtDerImpfung_Impfstoff ODII ON odi.id = ODII.ortDerImpfung_id
			INNER JOIN Impfstoff I ON ODII.impfstoff_id = I.id
	   WHERE I.id = '141fca55-ab78-4c0e-a2fd-edf2fe4e9b30');

DELETE FROM Impfstoff where id = '765dd8e2-5294-4d85-87bb-6fce77362348';

DELETE FROM ImpfempfehlungChGrundimmunisierung where impfstoff_id ='765dd8e2-5294-4d85-87bb-6fce77362348'
DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V18.8.3__impfstoffPfitzerBivalent.sql';
*/

