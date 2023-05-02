INSERT IGNORE INTO Krankheit (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, identifier, kantonaleBerechtigung) VALUES
       ('61bd68be-9808-4247-9f3c-cd569dec7bc2', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 1, 'FSME', 'LEISTUNGSERBRINGER');

# Impfstoff FSME-Immun CC
## Impfstoff
INSERT IGNORE INTO Impfstoff (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlDosenBenoetigt, code, name, hersteller, covidCertProdCode, hexFarbe, zulassungsStatus,
	informationsLink, impfstofftyp, zulassungsStatusBooster, wHoch2Code, eingestellt)
VALUES('b013cc91-3865-4150-93cf-2d1799d15061', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
	   3, 'FSME-Immun CC®', 'FSME-Immun CC®', 'TODO', 'TODO' ,'#00BFFF', 'EMPFOHLEN',
       null, 'ANDERE', 'EMPFOHLEN', null, false);
## Krankheit
INSERT IGNORE INTO Impfstoff_Krankheit (impfstoff_id, krankheit_id)
SELECT 'b013cc91-3865-4150-93cf-2d1799d15061', (SELECT id FROM Krankheit WHERE identifier = 'FSME');
## Impfempfehlung
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlVerabreicht, notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES (
	'c2c1c8eb-74b0-47eb-a538-e91c9aef35c0', '2022-09-22 00:00:00.000000', '2022-09-22 00:00:00.000000', 'flyway', 'flyway', 0,
	1, 2, 'b013cc91-3865-4150-93cf-2d1799d15061');
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlVerabreicht, notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES (
	'd6d6caf9-066b-43b0-b2dd-c22d4afa7a83', '2022-09-22 00:00:00.000000', '2022-09-22 00:00:00.000000', 'flyway', 'flyway', 0,
	2, 1, 'b013cc91-3865-4150-93cf-2d1799d15061');
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlVerabreicht, notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES (
	'f6bcb647-2838-41cf-9830-7ed0b7d35718', '2022-09-22 00:00:00.000000', '2022-09-22 00:00:00.000000', 'flyway', 'flyway', 0,
	3, 0, 'b013cc91-3865-4150-93cf-2d1799d15061');

# Impfstoff Encepur
## Impfstoff
INSERT IGNORE INTO Impfstoff (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlDosenBenoetigt, code, name, hersteller, covidCertProdCode, hexFarbe, zulassungsStatus,
	informationsLink, impfstofftyp, zulassungsStatusBooster, wHoch2Code, eingestellt)
VALUES('1b4b44f9-6726-4d6e-8f22-7266b1848bbf', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
	   3, 'Encepur®', 'Encepur®', 'TODO', 'TODO' ,'#1E90FF', 'EMPFOHLEN',
       null, 'ANDERE', 'EMPFOHLEN', null, false);
## Krankheit
INSERT IGNORE INTO Impfstoff_Krankheit (impfstoff_id, krankheit_id)
SELECT '1b4b44f9-6726-4d6e-8f22-7266b1848bbf', (SELECT id FROM Krankheit WHERE identifier = 'FSME');
## Impfempfehlung
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlVerabreicht, notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES (
	'f4883a02-3244-4e92-906b-9e07c8c9104f', '2022-09-22 00:00:00.000000', '2022-09-22 00:00:00.000000', 'flyway', 'flyway', 0,
	1, 2, '1b4b44f9-6726-4d6e-8f22-7266b1848bbf');
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlVerabreicht, notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES (
	'de97ef74-b171-40ec-9459-4ed9861fa81d', '2022-09-22 00:00:00.000000', '2022-09-22 00:00:00.000000', 'flyway', 'flyway', 0,
	2, 1, '1b4b44f9-6726-4d6e-8f22-7266b1848bbf');
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	anzahlVerabreicht, notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES (
	'704b0578-326f-4cdb-9c1d-7d5442165ba3', '2022-09-22 00:00:00.000000', '2022-09-22 00:00:00.000000', 'flyway', 'flyway', 0,
	3, 0, '1b4b44f9-6726-4d6e-8f22-7266b1848bbf');



/*
-- UNDO

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V20.0.0__krankheitFSME.sql';
*/