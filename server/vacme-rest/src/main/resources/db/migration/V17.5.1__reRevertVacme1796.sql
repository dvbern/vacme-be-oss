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


# COVID-19 Vaccine Janssen (Johnson&Johnson)
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('2387a322-33b6-472e-8216-76d9a7176656', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 0,
		'12c5d49e-ce77-464a-a951-3c840e5a1d1b');



# Comirnaty® (Pfizer)
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('dbdc2dc0-e19f-4380-8a34-1a55ef0f6e60', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 1,
		'141fca55-ab78-4c0e-a2fd-edf2fe4e9b30');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('9f8ad329-38d3-4cd8-87cc-81aeab7afd0f', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 0,
		'141fca55-ab78-4c0e-a2fd-edf2fe4e9b30');

# Comirnaty® 10 Mikrogramm/Dosis (Pfizer Kinderimpfung)
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('1fb3372e-e5ff-4687-9888-66a887897374', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 1,
		'4ebb48c1-cc96-4a8e-9832-77092bb968db');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('33836f2c-f4ff-4444-b3cf-82c9d8599423', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 0,
		'4ebb48c1-cc96-4a8e-9832-77092bb968db');



# Spikevax® (Moderna)
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('c5e52cf7-9d38-47ac-bfba-a218b7426d97', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 1,
		'c5abc3d7-f80d-44fd-be6e-0aba4cf03643');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('def2e1e5-b848-4f6f-af3f-47fa5bfc57e0', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 0,
		'c5abc3d7-f80d-44fd-be6e-0aba4cf03643');


# COVAXIN®
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('344b695e-1be2-4537-bfdb-9428c78f9d2f', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 1,
		'54513128-c6ed-4fd6-b61d-c23e6a73b20c');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('bcfa05d4-5205-41a3-92e9-bf524d71a011', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 0,
		'54513128-c6ed-4fd6-b61d-c23e6a73b20c');


# Vaxzevria® (AstraZeneca)
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('76dbb6d1-d13a-44ff-bded-edc989b95456', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 1,
		'7ff61fb9-0993-11ec-b1f1-0242ac140003');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('1edc51a5-b26d-44aa-9fd1-d0eb41a08f61', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 0,
		'7ff61fb9-0993-11ec-b1f1-0242ac140003');


# SARS-CoV-2 Vaccine (Vero Cell)
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('77e0036d-39a4-4359-befd-b419c96ed81b', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 1,
		'eb08558f-8d2b-4f64-adee-cdf0c642a387');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('80aa93dc-207a-4f9c-98cc-f6d533350f74', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 3, 0,
		'eb08558f-8d2b-4f64-adee-cdf0c642a387');


# CoronaVac
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('bae83249-3395-47f9-9ebe-a2a5ecfdbbf6', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 1,
		'ef4d4e99-583a-4e5d-9759-823002c6a260');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('37fca3f0-8f00-484d-85db-cb97a2f7bf7d', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 3, 0,
		'ef4d4e99-583a-4e5d-9759-823002c6a260');


# COVISHIELD™
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('654c44ce-f8c1-4edd-b6d8-0c9c2b71cf83', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 1,
		'c065820c-ee51-411c-aef4-daf17fd5799a');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('e7d64a8f-02f0-43bb-885a-4ef077757d01', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 0,
		'c065820c-ee51-411c-aef4-daf17fd5799a');


# COVOVAX™
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('611da7bd-2be2-4191-b735-9d1bc466ff1c', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 1,
		'7227720b-8764-465a-a430-dfc51c388709');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('8ae5c492-2c0d-4d93-a1c1-c7756ce60f4a', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 0,
		'7227720b-8764-465a-a430-dfc51c388709');


# NUVAXOVID™
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('7ef290bf-51b0-461a-8487-ac194f0392cf', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 1,
		'58f34c3a-b07b-48c8-a6a4-ae4e1305ba8d');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('8ea0f0df-7d8e-476f-9d50-f1ea63356b59', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 0,
		'58f34c3a-b07b-48c8-a6a4-ae4e1305ba8d');



# Sputnik Light
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('b708cb25-ba36-43c1-a262-67958fd914e8', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 1,
		'9d8debb1-dbd6-4251-82ff-4c5da6ae19d5');


# Sputnik V
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('b516040a-fd34-45a3-b3a6-b5857b17f4c6', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 1,
		'fb8d982c-61b3-4efb-adaf-9fad9160a501');
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('091541ea-09b3-471b-87f9-de6794876872', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 0,
		'fb8d982c-61b3-4efb-adaf-9fad9160a501');


# Convidecia (Ad5-nCoV)
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('ce48ab41-e8d5-4ea2-9345-9a08983f5938', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 2,
		'c1542f26-6d64-4639-85c5-95a16acec687');


# Kazakhstan RIBSP
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('7ea0850f-86e2-40c5-8ef1-e3431d984e13', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 2,
		'882cff0d-ce48-4143-90a5-98f83730c3eb');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('3a8cc19b-8030-4034-b717-9a013892fa0f', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 1,
		'882cff0d-ce48-4143-90a5-98f83730c3eb');


# SARS-CoV-2 Vaccine
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('f722736c-a368-4f27-9a1f-820a507e0e31', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 2,
		'015b0cd2-e79a-41c4-92bd-62b83a64259d');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('d6d5aa1f-5207-4f65-a9f3-ba3a34dd1fc8', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 1,
		'015b0cd2-e79a-41c4-92bd-62b83a64259d');


# KoviVac
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('a2e9303e-763d-47c5-b553-178ec3dfc362', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 2,
		'c8115c11-2dab-4e61-bfcc-ea1ee2192a08');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('331e085d-31dc-4e04-b592-a326148f1eca', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 1,
		'c8115c11-2dab-4e61-bfcc-ea1ee2192a08');


# EpiVacCorona
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('0dcd11f4-2f02-4c60-8330-b260d4a3f65d', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 2,
		'd012eedc-4e85-49cd-b4c8-003954577ccf');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('09535124-dd2b-4714-8c8a-2e19bd221f6d', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 1,
		'd012eedc-4e85-49cd-b4c8-003954577ccf');


# RBD-Dimer, Zifivax
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('f883bd3e-a54f-460f-9e12-f4680777e978', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 2,
		'c91c87ed-93d1-4c7f-a93b-acb317768bff');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('cb638662-8c53-4a23-9d86-52f13327c20b', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 1,
		'c91c87ed-93d1-4c7f-a93b-acb317768bff');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('eaefda15-3d2b-4c85-8906-7ff107b1bb14', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 3, 0,
		'c91c87ed-93d1-4c7f-a93b-acb317768bff');


# Abadala (CIGB-66)
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('4e7070c3-3c37-4c56-8032-05524e1ce2c9', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 2,
		'08794c7e-6447-4556-a1fb-070d89bc86cb');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('a75e9920-507d-4034-8f8a-a78af8806c24', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 1,
		'08794c7e-6447-4556-a1fb-070d89bc86cb');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('6747557e-0474-49e1-878c-33c73876f3ea', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 3, 0,
		'08794c7e-6447-4556-a1fb-070d89bc86cb');


# Plus CIGB-66
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('aabc8f14-cea2-47f5-9e26-df3962dd40b3', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 1,
		'1ed24fff-dffc-4955-b2c8-876d3679f58d');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('6ea9ab3b-e8f3-4f9a-8327-92d54cd51462', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 3, 0,
		'1ed24fff-dffc-4955-b2c8-876d3679f58d');


# MVC-COV1901
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('ac7d894e-a304-47ef-b2ee-15e7f7e41e30', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 2,
		'030c6b57-b771-44f4-9163-fe8c03ecf35a');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('c008b36f-83f7-4b5d-b6ab-879de4dd92e2', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 1,
		'030c6b57-b771-44f4-9163-fe8c03ecf35a');


# ZyCoV-D
INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('e3d780b1-a7e7-4e33-9ca2-cbca6018f28f', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 1, 2,
		'3b15ef6d-db8d-403b-9bd0-291943443cc9');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('a3c33674-3d5f-4b19-acc1-15deb06fcdfd', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 2, 1,
		'3b15ef6d-db8d-403b-9bd0-291943443cc9');

INSERT IGNORE INTO ImpfempfehlungChGrundimmunisierung(
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, anzahlVerabreicht,
	notwendigFuerChGrundimmunisierung, impfstoff_id)
VALUES ('5cd8a958-ac18-45ba-9857-43fbd6acdcad', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0, 3, 0,
		'3b15ef6d-db8d-403b-9bd0-291943443cc9');




/*
-- UNDO:

-- Alle ImpfempfehlungChGrundimmunisierung loeschen (wie schon einmal in V17.4.4__revertVacme1796.sql)
TRUNCATE TABLE  ImpfempfehlungChGrundimmunisierung;

-- Die neuen Non-WHO Impfstoffe loeschen
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

-- Flyway Schema History
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V17.5.1__reRevertVacme1796.sql';
*/






