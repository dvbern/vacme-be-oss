# Impfstoff Moderna bivalent
INSERT IGNORE INTO Impfstoff (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlDosenBenoetigt, code, name, hersteller, covidCertProdCode, hexFarbe, zulassungsStatus,
	informationsLink, impfstofftyp, zulassungsStatusBooster, wHoch2Code, eingestellt)
VALUES('313769d0-a3e1-4c0f-92e2-264e32dd9b15', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
	   2, '7680690090012', 'Spikevax® Bivalent Original / Omicron', 'Moderna', 'EU/1/20/1507' ,'#0000DD', 'NICHT_ZUGELASSEN', null, 'MRNA', 'NICHT_ZUGELASSEN', 'Spikevax®', FALSE);

# Impfempfehlung wenn bereits geimpft
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlVerabreicht, notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES (
	'cf28b675-757d-4603-8f64-c48863a4e49e', '2022-09-22 00:00:00.000000', '2022-09-22 00:00:00.000000', 'flyway', 'flyway', 0,
	1, 1, '313769d0-a3e1-4c0f-92e2-264e32dd9b15');
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlVerabreicht, notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES (
	'620a9a76-ffe6-4b66-851a-b48b2dfa4194', '2022-09-22 00:00:00.000000', '2022-09-22 00:00:00.000000', 'flyway', 'flyway', 0,
	2, 0, '313769d0-a3e1-4c0f-92e2-264e32dd9b15');