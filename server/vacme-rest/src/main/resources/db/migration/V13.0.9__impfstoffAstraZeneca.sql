-- Insert AstraZeneca
INSERT INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
					   anzahlDosenBenoetigt, code, name, hersteller, myCOVIDvacCode, hexFarbe, zulassungsStatus)
VALUES ('7ff61fb9-0993-11ec-b1f1-0242ac140003', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		2, 'ATC J07BX03', 'Vaxzevria®', 'AstraZeneca AB', 'xxx', '#f9d34b', 'EXTERN_ZUGELASSEN');


/*
UNDO:

DELETE FROM Impfstoff WHERE id = '7ff61fb9-0993-11ec-b1f1-0242ac140003';
*/