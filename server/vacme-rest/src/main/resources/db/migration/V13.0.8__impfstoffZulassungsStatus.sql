ALTER TABLE Impfstoff
	ADD COLUMN zulassungsStatus varchar(50) NOT NULL default 'NICHT_ZUGELASSEN';
ALTER TABLE Impfstoff_AUD
	ADD COLUMN zulassungsStatus varchar(50) NOT NULL default 'NICHT_ZUGELASSEN';


UPDATE Impfstoff SET zulassungsStatus = 'ZUGELASSEN' WHERE code = '30380777700688'; # Moderna
UPDATE Impfstoff SET zulassungsStatus = 'ZUGELASSEN' WHERE code = '7680682250011'; # Comirnaty


/*
UNDO:

ALTER TABLE Impfstoff
	DROP COLUMN zulassungsStatus;
ALTER TABLE Impfstoff_AUD
	DROP COLUMN zulassungsStatus;


*/