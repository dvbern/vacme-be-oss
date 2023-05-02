ALTER TABLE Impfstoff
	ADD hersteller VARCHAR(255) NOT NULL;
ALTER TABLE Impfstoff
	ADD anzahlTageZwischenImpfungenOffsetBefore INT NOT NULL;
ALTER TABLE Impfstoff
	ADD anzahlTageZwischenImpfungenOffsetAfter INT NOT NULL;

ALTER TABLE Impfstoff_AUD
	ADD hersteller VARCHAR(255);
ALTER TABLE Impfstoff_AUD
	ADD anzahlTageZwischenImpfungenOffsetBefore INT;
ALTER TABLE Impfstoff_AUD
	ADD anzahlTageZwischenImpfungenOffsetAfter INT;


INSERT INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
					   anzahlDosenBenoetigt, anzahlTageZwischenImpfungen, code, name, hersteller,
					   anzahlTageZwischenImpfungenOffsetBefore, anzahlTageZwischenImpfungenOffsetAfter)
VALUES (
        '141fca55-ab78-4c0e-a2fd-edf2fe4e9b30', '2020-12-11 18:10:45.000000', '2020-12-11 18:10:45.000000', 'flyway', 'flyway', 0,
        2, 28, '7680682250011', 'Comirnaty', 'Pfizer/BioNTech', 1, 5);