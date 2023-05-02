# Insert Sputnik Light grau
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
					   anzahlDosenBenoetigt, code, name, hersteller, zulassungsStatus, covidCertProdCode, hexFarbe)
VALUES ('9d8debb1-dbd6-4251-82ff-4c5da6ae19d5', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		1, 'Sputnik Light', 'Sputnik Light', 'Gamaleya National Centre of epidemiology and Microbiology, Russia',
		'NICHT_WHO_ZUGELASSEN', 'Sputnik Light', '#a4a1a1');

# Insert Sputnik V grau
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
					   anzahlDosenBenoetigt, code, name, hersteller, zulassungsStatus, covidCertProdCode, hexFarbe)
VALUES ('fb8d982c-61b3-4efb-adaf-9fad9160a501', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, 'Sputnik V', 'Sputnik V', 'Gamaleya National Centre of epidemiology and Microbiology, Russia',
		'NICHT_WHO_ZUGELASSEN', 'Sputnik V', '#a4a1a1');

# Insert Convidecia (Ad5-nCoV) grau
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						anzahlDosenBenoetigt, code, name, hersteller, zulassungsStatus, covidCertProdCode, hexFarbe)
VALUES ('c1542f26-6d64-4639-85c5-95a16acec687', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		1, 'Convidecia (Ad5-nCoV)', 'Convidecia (Ad5-nCoV)', 'CanSino', 'NICHT_WHO_ZUGELASSEN', 'Convidecia (Ad5-nCoV)',
        '#a4a1a1');

# Insert Kazakhstan RIBSP grau
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						anzahlDosenBenoetigt, code, name, hersteller, zulassungsStatus, covidCertProdCode, hexFarbe)
VALUES ('882cff0d-ce48-4143-90a5-98f83730c3eb', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, 'Kazakhstan RIBSP', 'Kazakhstan RIBSP', 'QazCovid-in, Russia', 'NICHT_WHO_ZUGELASSEN', 'Kazakhstan RIBSP',
        '#a4a1a1');

# Insert SARS-CoV-2 Vaccine grau
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						anzahlDosenBenoetigt, code, name, hersteller, zulassungsStatus, covidCertProdCode, hexFarbe)
VALUES ('015b0cd2-e79a-41c4-92bd-62b83a64259d', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, 'SARS-CoV-2 Vaccine', 'SARS-CoV-2 Vaccine', 'Minhai Biotechnology Co Shenzhen Kangtai',
        'NICHT_WHO_ZUGELASSEN', 'SARS-CoV-2 Vaccine', '#a4a1a1');

# Insert KoviVac grau
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						anzahlDosenBenoetigt, code, name, hersteller, zulassungsStatus, covidCertProdCode, hexFarbe)
VALUES ('c8115c11-2dab-4e61-bfcc-ea1ee2192a08', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, 'KoviVac', 'KoviVac', 'Chumakov Center', 'NICHT_WHO_ZUGELASSEN', 'KoviVac', '#a4a1a1');

# Insert EpiVacCorona grau
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						anzahlDosenBenoetigt, code, name, hersteller, zulassungsStatus, covidCertProdCode, hexFarbe)
VALUES ('d012eedc-4e85-49cd-b4c8-003954577ccf', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, 'EpiVacCorona', 'EpiVacCorona', 'VECTOR, Russland', 'NICHT_WHO_ZUGELASSEN', 'EpiVacCorona', '#a4a1a1');

# Insert RBD-Dimer, Zifivax grau (anzahlDosenBenoetigt: 2 oder 3????)
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						anzahlDosenBenoetigt, code, name, hersteller, zulassungsStatus, covidCertProdCode, hexFarbe)
VALUES ('c91c87ed-93d1-4c7f-a93b-acb317768bff', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		3, 'RBD-Dimer, Zifivax', 'RBD-Dimer, Zifivax', 'Anhui Zhifei Longcom, China ZF2001',
		'NICHT_WHO_ZUGELASSEN', 'RBD-Dimer, Zifivax', '#a4a1a1');

# Insert Abadala (CIGB-66) grau
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						anzahlDosenBenoetigt, code, name, hersteller, zulassungsStatus, covidCertProdCode, hexFarbe)
VALUES ('08794c7e-6447-4556-a1fb-070d89bc86cb', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		3, 'Abadala (CIGB-66)', 'Abadala (CIGB-66)', 'Center for Genetic Engineering and Biotechnology (CIGB)',
		'NICHT_WHO_ZUGELASSEN', 'Abadala (CIGB-66)', '#a4a1a1');

# Insert Plus CIGB-66 grau
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						anzahlDosenBenoetigt, code, name, hersteller, zulassungsStatus, covidCertProdCode, hexFarbe)
VALUES ('1ed24fff-dffc-4955-b2c8-876d3679f58d', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		3, 'Plus CIGB-66', 'Plus CIGB-66', 'Center for Genetic Engineering and Biotechnology (CIGB)',
		'NICHT_WHO_ZUGELASSEN', 'Plus CIGB-66', '#a4a1a1');

# Insert MVC-COV1901 grau
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
                        anzahlDosenBenoetigt, code, name, hersteller, zulassungsStatus, covidCertProdCode, hexFarbe)
VALUES ('030c6b57-b771-44f4-9163-fe8c03ecf35a', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, 'MVC-COV1901', 'MVC-COV1901', 'Medigen Vaccine Biologics Corp (MVC)', 'NICHT_WHO_ZUGELASSEN', 'MVC-COV1901',
        '#a4a1a1');

# Insert ZyCoV-D grau
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						anzahlDosenBenoetigt, code, name, hersteller, zulassungsStatus, covidCertProdCode, hexFarbe)
VALUES ('3b15ef6d-db8d-403b-9bd0-291943443cc9', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		3, 'ZyCoV-D', 'ZyCoV-D', 'Zydus Cadila, Indien', 'NICHT_WHO_ZUGELASSEN', 'ZyCoV-D', '#a4a1a1');




/*
-- UNDO:
DELETE from Impfstoff where id = '9d8debb1-dbd6-4251-82ff-4c5da6ae19d5';
DELETE from Impfstoff where id = 'fb8d982c-61b3-4efb-adaf-9fad9160a501';
DELETE from Impfstoff where id = 'c1542f26-6d64-4639-85c5-95a16acec687';
DELETE from Impfstoff where id = '882cff0d-ce48-4143-90a5-98f83730c3eb';
DELETE from Impfstoff where id = '015b0cd2-e79a-41c4-92bd-62b83a64259d';
DELETE from Impfstoff where id = 'c8115c11-2dab-4e61-bfcc-ea1ee2192a08';
DELETE from Impfstoff where id = 'd012eedc-4e85-49cd-b4c8-003954577ccf';
DELETE from Impfstoff where id = 'c91c87ed-93d1-4c7f-a93b-acb317768bff';
DELETE from Impfstoff where id = '08794c7e-6447-4556-a1fb-070d89bc86cb';
DELETE from Impfstoff where id = '1ed24fff-dffc-4955-b2c8-876d3679f58d';
DELETE from Impfstoff where id = '030c6b57-b771-44f4-9163-fe8c03ecf35a';
DELETE from Impfstoff where id = '3b15ef6d-db8d-403b-9bd0-291943443cc9';

DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V17.4.0__neueNichtWhoZugelasseneImpfstoffe.sql';
*/










