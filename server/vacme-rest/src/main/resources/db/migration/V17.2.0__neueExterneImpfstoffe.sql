# Insert Novavax
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
					   anzahlDosenBenoetigt, code, name, hersteller, hexFarbe, zulassungsStatus, covidCertProdCode)
VALUES ('58f34c3a-b07b-48c8-a6a4-ae4e1305ba8d', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, 'Nuvaxovid', 'Nuvaxovid', 'Novavax CZ a.s.', '#a4a1a1', 'EXTERN_ZUGELASSEN', 'EU/1/21/1618');

# Insert Covishield
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
					   anzahlDosenBenoetigt, code, name, hersteller, hexFarbe, zulassungsStatus, covidCertProdCode)
VALUES ('c065820c-ee51-411c-aef4-daf17fd5799a', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, 'Covishield', 'Covishield (ChAdOx1_nCoV-19)', 'Serum Institute of India Pvt. Ltd', '#a4a1a1', 'EXTERN_ZUGELASSEN', 'Covishield');

# Insert COVOVAX
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
					   anzahlDosenBenoetigt, code, name, hersteller, hexFarbe, zulassungsStatus, covidCertProdCode)
VALUES ('7227720b-8764-465a-a430-dfc51c388709', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, 'COVOVAX', 'COVOVAX (Novavax formulation)', 'Serum Institute of India Pvt. Ltd', '#a4a1a1', 'EXTERN_ZUGELASSEN', 'COVOVAX');

# Comirnaty Kinderimpfung zulassen
update Impfstoff set zulassungsStatus = 'ZUGELASSEN' where id = '4ebb48c1-cc96-4a8e-9832-77092bb968db';

# Die Farben der extern zugelassenen Impstoffen generell auf "grau" stellen
update Impfstoff set hexFarbe = '#a4a1a1' where zulassungsStatus = 'EXTERN_ZUGELASSEN';