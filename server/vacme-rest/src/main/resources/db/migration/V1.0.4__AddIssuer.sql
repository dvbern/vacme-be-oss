ALTER TABLE Benutzer
	ADD COLUMN issuer VARCHAR(255) NOT NULL;

ALTER TABLE Benutzer_AUD
	ADD COLUMN issuer VARCHAR(255) NULL;


DROP INDEX IF EXISTS UC_Benutzer_mail ON Benutzer;
DROP INDEX IF EXISTS UC_Benutzer_benutzername ON Benutzer;

CREATE UNIQUE INDEX UC_Benutzer_mail
	ON Benutzer(email, issuer);

CREATE UNIQUE INDEX UC_Benutzer_benutzername
	ON Benutzer(benutzername, issuer);