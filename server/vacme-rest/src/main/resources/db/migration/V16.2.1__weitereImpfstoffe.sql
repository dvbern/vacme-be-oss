-- Insert Sinovac hellgruen
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
					   anzahlDosenBenoetigt, code, name, hersteller, hexFarbe, zulassungsStatus, covidCertProdCode)
VALUES ('ef4d4e99-583a-4e5d-9759-823002c6a260', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, 'CoronaVac', 'CoronaVac', 'Sinovac', '#c6f494', 'EXTERN_ZUGELASSEN', 'CoronaVac');

-- Insert Sinopharm/BIBP dunkelgruen
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
					   anzahlDosenBenoetigt, code, name, hersteller, hexFarbe, zulassungsStatus, covidCertProdCode)
VALUES ('eb08558f-8d2b-4f64-adee-cdf0c642a387', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, 'SARS-CoV-2 Vaccine (Vero Cell)', 'SARS-CoV-2 Vaccine (Vero Cell)', 'Sinopharm/BIBP', '#04B45F', 'EXTERN_ZUGELASSEN', 'SARS-CoV-2 Vaccine (Vero Cell)');

-- Insert COVAXIN orange
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
					   anzahlDosenBenoetigt, code, name, hersteller, hexFarbe, zulassungsStatus, covidCertProdCode)
VALUES ('54513128-c6ed-4fd6-b61d-c23e6a73b20c', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, 'COVAXIN', 'COVAXIN®', 'Bharat Biotech International Ltd', '#ffab4b', 'EXTERN_ZUGELASSEN', 'COVAXIN');

/**
UNDO:

DELETE FROM Impfstoff WHERE id = 'ef4d4e99-583a-4e5d-9759-823002c6a260';
DELETE FROM Impfstoff WHERE id = 'eb08558f-8d2b-4f64-adee-cdf0c642a387';
DELETE FROM Impfstoff WHERE id = '54513128-c6ed-4fd6-b61d-c23e6a73b20c';

DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V16.2.1__weitereImpfstoffe.sql';
**/