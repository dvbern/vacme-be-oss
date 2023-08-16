INSERT IGNORE INTO Impfstoff (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
							  anzahlDosenBenoetigt, anzahlTageZwischenImpfungen, code, name, hersteller,
							  anzahlTageZwischenImpfungenOffsetBefore, anzahlTageZwischenImpfungenOffsetAfter)
VALUES ('c5abc3d7-f80d-44fd-be6e-0aba4cf03643', '2021-01-15 12:00:00.000000', '2021-01-15 12:00:00.000000', 'flyway',
		'flyway', 0,
		2, 28, 'mRNA-1273', 'COVID-19 vaccine (Moderna)', 'Moderna', 1, 5);