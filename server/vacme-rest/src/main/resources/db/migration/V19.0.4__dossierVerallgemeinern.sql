ALTER TABLE Impfdossier ADD IF NOT EXISTS dossierStatus VARCHAR(50) NULL;
ALTER TABLE Impfdossier ADD IF NOT EXISTS krankheitIdentifier VARCHAR(50) NULL;

UPDATE Impfdossier SET dossierStatus = 'NOCH_NICHT_MIGRIERT' WHERE dossierStatus IS NULL;
UPDATE Impfdossier SET krankheitIdentifier = 'COVID' WHERE krankheitIdentifier IS NULL;

ALTER TABLE Impfdossier MODIFY dossierStatus VARCHAR(50) NOT NULL;
ALTER TABLE Impfdossier MODIFY krankheitIdentifier VARCHAR(50) NOT NULL;


ALTER TABLE Impfdossier_AUD ADD IF NOT EXISTS dossierStatus VARCHAR(50) NULL;
ALTER TABLE Impfdossier_AUD ADD IF NOT EXISTS krankheitIdentifier VARCHAR(50) NULL;

DROP INDEX IF EXISTS IX_Impfdossier_registrierung ON Impfdossier;
# FK muss gedroppt werden damit der UC gedroppt werden kann, danach aber neu erstellen da er noetig ist
ALTER TABLE Impfdossier DROP FOREIGN KEY IF EXISTS FK_Impfdossier_registrierung;
DROP INDEX IF EXISTS UC_Impfdossier_registrierung ON Impfdossier;

CREATE INDEX IF NOT EXISTS IX_Impfdossier_registrierung_krankheit ON Impfdossier(registrierung_id, krankheitIdentifier, id);
# FK wieder erstellen, sollte intern nun neuen index verwenden
Alter Table Impfdossier Add  FOREIGN KEY FK_Impfdossier_registrierung (registrierung_id) REFERENCES Registrierung(id);

ALTER TABLE Impfdossier ADD CONSTRAINT UC_Impfdossier_registrierung_krankheit UNIQUE  (registrierung_id, krankheitIdentifier);


/**
  	UNDO:

	alter table Impfdossier drop dossierStatus;
	alter table Impfdossier drop krankheitIdentifier;

	alter table Impfdossier_AUD drop dossierStatus;
	alter table Impfdossier_AUD drop krankheitIdentifier;

	CREATE INDEX IX_Impfdossier_registrierung ON Impfdossier(registrierung_id, id);
	alter table Impfdossier add constraint UC_Impfdossier_registrierung unique (registrierung_id)

	drop index IX_Impfdossier_registrierung_krankheit on Impfdossier;
	drop index UC_Impfdossier_registrierung_krankheit on Impfdossier;

  	DELETE from flyway_schema_history where script = 'db/migration/V19.0.4__dossierVerallgemeinern.sql';
 */