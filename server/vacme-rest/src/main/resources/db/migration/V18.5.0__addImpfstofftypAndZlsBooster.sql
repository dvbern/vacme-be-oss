ALTER TABLE Impfstoff ADD COLUMN IF NOT EXISTS  impfstofftyp VARCHAR(50) DEFAULT 'ANDERE';

ALTER TABLE Impfstoff ADD COLUMN  IF NOT EXISTS  zulassungsStatusBooster varchar(50) NOT NULL DEFAULT 'NICHT_ZUGELASSEN';

UPDATE Impfstoff SET impfstofftyp = 'MRNA' WHERE id = 'c5abc3d7-f80d-44fd-be6e-0aba4cf03643'; # Moderna
UPDATE Impfstoff SET impfstofftyp = 'MRNA' WHERE id = '141fca55-ab78-4c0e-a2fd-edf2fe4e9b30'; # Pfizer
UPDATE Impfstoff SET impfstofftyp = 'MRNA' WHERE id = '4ebb48c1-cc96-4a8e-9832-77092bb968db'; # Pfizer Kinder


# Zugelassen zum Boostern sind aktuell nur Moderna, Pfizer und Janssen
UPDATE Impfstoff SET zulassungsStatusBooster = 'ZUGELASSEN' WHERE id = 'c5abc3d7-f80d-44fd-be6e-0aba4cf03643'; # Moderna
UPDATE Impfstoff SET zulassungsStatusBooster = 'ZUGELASSEN' WHERE id = '141fca55-ab78-4c0e-a2fd-edf2fe4e9b30'; # Pfizer
UPDATE Impfstoff SET zulassungsStatusBooster = 'ZUGELASSEN' WHERE id = '12c5d49e-ce77-464a-a951-3c840e5a1d1b'; # Janssen


# Neu ist Novavax zum Grundimmunisieren Zugelassen
UPDATE Impfstoff SET zulassungsStatus = 'ZUGELASSEN' WHERE id = '58f34c3a-b07b-48c8-a6a4-ae4e1305ba8d'; # Novavax

ALTER TABLE Impfstoff ALTER COLUMN impfstofftyp DROP DEFAULT;

ALTER TABLE Impfstoff ALTER COLUMN zulassungsStatusBooster DROP DEFAULT;



ALTER TABLE Impfstoff_AUD ADD COLUMN IF NOT EXISTS  impfstofftyp VARCHAR(50);
ALTER TABLE Impfstoff_AUD ADD COLUMN IF NOT EXISTS  zulassungsStatusBooster VARCHAR(50);

