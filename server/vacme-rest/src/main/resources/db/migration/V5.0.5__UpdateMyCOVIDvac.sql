ALTER TABLE Impfstoff ADD COLUMN myCOVIDvacCode VARCHAR(255) NOT NULL;
ALTER TABLE Impfstoff_AUD ADD COLUMN myCOVIDvacCode VARCHAR(255) NULL;

UPDATE Impfstoff set myCOVIDvacCode = 'BNT162b2' where id = '141fca55-ab78-4c0e-a2fd-edf2fe4e9b30'; /* Pfizer/BioNTech */
UPDATE Impfstoff set myCOVIDvacCode = 'mRNA-1273' where id = 'c5abc3d7-f80d-44fd-be6e-0aba4cf03643'; /* moderna */
