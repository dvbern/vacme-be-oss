-- Insert Janssen (Johnson&Johnson) Impfstoff
INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
					   anzahlDosenBenoetigt, code, name, hersteller, myCOVIDvacCode, hexFarbe, zulassungsStatus)
VALUES ('12c5d49e-ce77-464a-a951-3c840e5a1d1b', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		1, '05413868120110', 'COVID-19 Vaccine Janssen', 'Janssen-Cilag International', 'xxx', '#d5abe0', 'NICHT_ZUGELASSEN');


/*
UNDO:

DELETE FROM Impfstoff WHERE id = 12c5d49e-ce77-464a-a951-3c840e5a1d1b;
*/