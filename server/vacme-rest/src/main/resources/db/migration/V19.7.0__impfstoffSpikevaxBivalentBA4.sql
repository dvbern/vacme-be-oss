#Impfstoff Spikevax Bivalent Original / Omicron BA.4-5
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
							  anzahlDosenBenoetigt, code, name, hersteller, covidCertProdCode, hexFarbe,
							  zulassungsStatus,
							  informationsLink, impfstofftyp, zulassungsStatusBooster, wHoch2Code)
VALUES ('b159b520-742c-42c1-b6db-35664b3c2ee6', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, '7680691890017', 'Spikevax® Bivalent Original / Omicron BA.4-5', 'Moderna', 'EU/1/20/1507', '#00008B',
		'EMPFOHLEN',
		'https://www.swissmedic.ch/swissmedic/de/home/news/coronavirus-covid-19/smc-laesst-bivalente-covid-19-origial-omicron-ba4-5-auffrischimpfung-moderna-zu.html',
		'MRNA', 'EMPFOHLEN', 'Spikevax®');

INSERT IGNORE INTO Impfstoff_Krankheit (impfstoff_id, krankheit_id)
SELECT 'b159b520-742c-42c1-b6db-35664b3c2ee6', (SELECT id FROM Krankheit WHERE identifier = 'COVID');

# Impfempfehlung wenn bereits geimpft
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (id, timestampErstellt, timestampMutiert, userErstellt,
													   userMutiert, version,
													   anzahlVerabreicht, notwendigFuerChGrundimmunisierung,
													   impfstoff_id)
VALUES ('3fe98735-f0e8-4368-ad4b-48c5e655a4b9', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway',
		'flyway', 0,
		1, 1, 'b159b520-742c-42c1-b6db-35664b3c2ee6');
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (id, timestampErstellt, timestampMutiert, userErstellt,
													   userMutiert, version,
													   anzahlVerabreicht, notwendigFuerChGrundimmunisierung,
													   impfstoff_id)
VALUES ('edb43a75-aed9-46d9-af77-873d829e9d0f', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway',
		'flyway', 0,
		2, 0, 'b159b520-742c-42c1-b6db-35664b3c2ee6');

/*
-- UNDO:

DELETE FROM Impfstoff_Krankheit where impfstoff_id = 'b159b520-742c-42c1-b6db-35664b3c2ee6';
DELETE FROM ImpfempfehlungChGrundimmunisierung where impfstoff_id ='b159b520-742c-42c1-b6db-35664b3c2ee6';
DELETE FROM Impfstoff where id = 'b159b520-742c-42c1-b6db-35664b3c2ee6';

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.6.0__impfstoffSpikevaxBivalentOriginal.sql';

*/