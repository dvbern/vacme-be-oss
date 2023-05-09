#Impfstoff Spikevax Bivalent Original / Omicron BA.4-5 Fertigspritzen
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
							  anzahlDosenBenoetigt, code, name, hersteller, covidCertProdCode, hexFarbe,
							  zulassungsStatus,
							  informationsLink, impfstofftyp, zulassungsStatusBooster, wHoch2Code)
VALUES ('2ccc4ca0-98a0-4314-b02d-01e78e9b515f', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, '7680692110015', 'Spikevax® Bivalent Original / Omicron BA.4-5 Fertigspritzen', 'Moderna', 'EU/1/20/1507',
		'#0000DD',
		'EMPFOHLEN',
		'https://www.swissmedic.ch/swissmedic/de/home/news/coronavirus-covid-19/smc-laesst-bivalente-covid-19-origial-omicron-ba4-5-auffrischimpfung-moderna-zu.html',
		'MRNA', 'EMPFOHLEN', 'Spikevax®');

INSERT IGNORE INTO Impfstoff_Krankheit (impfstoff_id, krankheit_id)
SELECT '2ccc4ca0-98a0-4314-b02d-01e78e9b515f', (SELECT id FROM Krankheit WHERE identifier = 'COVID');

# Impfempfehlung wenn bereits geimpft
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (id, timestampErstellt, timestampMutiert, userErstellt,
													   userMutiert, version,
													   anzahlVerabreicht, notwendigFuerChGrundimmunisierung,
													   impfstoff_id)
VALUES ('9e3f5363-0cca-475c-b95a-66a5aab4f9c5', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway',
		'flyway', 0,
		1, 1, '2ccc4ca0-98a0-4314-b02d-01e78e9b515f');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (id, timestampErstellt, timestampMutiert, userErstellt,
													   userMutiert, version,
													   anzahlVerabreicht, notwendigFuerChGrundimmunisierung,
													   impfstoff_id)
VALUES ('0dac182e-def4-4e7d-94f3-cbcf60948f13', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway',
		'flyway', 0,
		2, 0, '2ccc4ca0-98a0-4314-b02d-01e78e9b515f');

-- Fuege eine Verknuepfung zu Spikevax Bivalent Original / Omicron BA.4-5 Fertigspritzen hinzu fuer alle Odis die bisher Spikevax® monovalent hatten
INSERT IGNORE INTO OrtDerImpfung_Impfstoff (impfstoff_id, ortDerImpfung_id)
SELECT '2ccc4ca0-98a0-4314-b02d-01e78e9b515f', odi.id
FROM OrtDerImpfung odi
	 INNER JOIN OrtDerImpfung_Impfstoff ODII ON odi.id = ODII.ortDerImpfung_id
	 INNER JOIN Impfstoff I ON ODII.impfstoff_id = I.id
WHERE I.id = 'c5abc3d7-f80d-44fd-be6e-0aba4cf03643';

/*
-- UNDO:
DELETE
FROM OrtDerImpfung_Impfstoff
WHERE impfstoff_id = '2ccc4ca0-98a0-4314-b02d-01e78e9b515f' AND ortDerImpfung_id IN (SELECT odi.id
	   FROM OrtDerImpfung odi
			INNER JOIN OrtDerImpfung_Impfstoff ODII ON odi.id = ODII.ortDerImpfung_id
			INNER JOIN Impfstoff I ON ODII.impfstoff_id = I.id
	   WHERE I.id = 'c5abc3d7-f80d-44fd-be6e-0aba4cf03643');
DELETE FROM Impfstoff_Krankheit where impfstoff_id = '2ccc4ca0-98a0-4314-b02d-01e78e9b515f';
DELETE FROM ImpfempfehlungChGrundimmunisierung where impfstoff_id ='2ccc4ca0-98a0-4314-b02d-01e78e9b515f';
DELETE FROM Impfstoff where id = '2ccc4ca0-98a0-4314-b02d-01e78e9b515f';

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.8.4__impfstoffModernaSpikevaxBAFertigspritze.sql';

*/