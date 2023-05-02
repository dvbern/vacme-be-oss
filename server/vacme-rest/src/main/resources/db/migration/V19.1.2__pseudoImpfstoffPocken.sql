INSERT IGNORE INTO Impfstoff (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlDosenBenoetigt, code, name, hersteller, covidCertProdCode, hexFarbe, zulassungsStatus,
	informationsLink, impfstofftyp, zulassungsStatusBooster)
VALUES('038f7b38-a6e8-46c0-876d-c8868623d1e1', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
	   2, 'keinCode', 'UNBEKANNTE_POCKENIMPFUNG_IN_KINDHEIT', 'UNBEKANNTE_POCKENIMPFUNG_IN_KINDHEIT', 'keinCovidCertProdCode' ,'#20B2AA', 'NICHT_WHO_ZUGELASSEN', null, 'ANDERE', 'NICHT_WHO_ZUGELASSEN');

INSERT IGNORE INTO Impfstoff_Krankheit (impfstoff_id, krankheit_id)
SELECT '038f7b38-a6e8-46c0-876d-c8868623d1e1', (SELECT id FROM Krankheit WHERE identifier = 'AFFENPOCKEN');


# Impfempfehlung bei Impfung in der Kindheit
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (
    id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
    anzahlVerabreicht, notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES (
    'bccfa6e5-3586-4d9e-af9a-d69f34e0a419', '2022-09-22 00:00:00.000000', '2022-09-22 00:00:00.000000', 'flyway', 'flyway', 0,
        1, 1, '038f7b38-a6e8-46c0-876d-c8868623d1e1');


# Impfempfehlung wenn bereits mit MVA-BN geimpft
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlVerabreicht, notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES (
	'c4dedcfb-9c23-4c5b-8e7f-2f0b0e16d475', '2022-09-22 00:00:00.000000', '2022-09-22 00:00:00.000000', 'flyway', 'flyway', 0,
	1, 1, 'adea588d-edfd-4955-9794-d120cbddbdf2');
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlVerabreicht, notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES (
	'37ac3c14-4c38-4fb6-8f42-eba3b478b173', '2022-09-22 00:00:00.000000', '2022-09-22 00:00:00.000000', 'flyway', 'flyway', 0,
	2, 0, 'adea588d-edfd-4955-9794-d120cbddbdf2');