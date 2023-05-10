ALTER TABLE Impfstoff ADD COLUMN hexFarbe VARCHAR(8) NULL;
ALTER TABLE Impfstoff_AUD ADD COLUMN hexFarbe VARCHAR(8) NULL;

UPDATE Impfstoff SET hexFarbe = '#FF0000' WHERE code = '30380777700688'; # Moderna RED
UPDATE Impfstoff SET hexFarbe = '#EA161F' WHERE code = '7680682250011'; # Corninarty BLUE