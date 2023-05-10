# Insert KinderImpfstoff
INSERT IGNORE
INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
					   anzahlDosenBenoetigt, code, name, hersteller, hexFarbe, zulassungsStatus, covidCertProdCode)
VALUES ('4ebb48c1-cc96-4a8e-9832-77092bb968db', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, 'Comirnaty® Kinder', 'Comirnaty Kinder®', 'Pfizer/BioNTech', '#ffbde7', 'NICHT_ZUGELASSEN', 'Comirnaty Kinder');

# Unterdessen bekannte covidCertProdCodes der neuen extern zugelassenen Impfstoffe
update Impfstoff set covidCertProdCode = 'CoronaVac' where id = 'ef4d4e99-583a-4e5d-9759-823002c6a260';
update Impfstoff set covidCertProdCode = 'BBIBP-CorV' where id = 'eb08558f-8d2b-4f64-adee-cdf0c642a387';
update Impfstoff set covidCertProdCode = 'Covaxin' where id = '54513128-c6ed-4fd6-b61d-c23e6a73b20c';