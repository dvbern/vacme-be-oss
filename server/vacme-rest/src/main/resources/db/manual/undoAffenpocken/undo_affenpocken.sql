DELETE from flyway_schema_history where script = 'db/migration/V19.0.0__Krankheit.sql';
DELETE from flyway_schema_history where script = 'db/migration/V19.0.1__Impfstoff_Krankheit_migration.sql';
DELETE from flyway_schema_history where script = 'db/migration/V19.0.2__addImpfslotKrankheitAndMigrate.sql';
DELETE from flyway_schema_history where script = 'db/migration/V19.0.3__OrtDerImpfung_KrankheitAndMigrate.sql';
DELETE from flyway_schema_history where script = 'db/migration/V19.0.4__dossierVerallgemeinern.sql';
DELETE from flyway_schema_history where script = 'db/migration/V19.0.5__ImpfdossierFile.sql';
DELETE from flyway_schema_history where script = 'db/migration/V19.0.6__ImpfdossierFileWithoutFileExtension.sql';
DELETE from flyway_schema_history where script = 'db/migration/V19.0.7__impfstoffAffenpocken.sql';

# 19.0.1
DROP TABLE IF EXISTS Impfstoff_Krankheit;
DROP TABLE IF EXISTS Impfstoff_Krankheit_AUD;

# 19.0.3
DROP TABLE IF EXISTS OrtDerImpfung_Krankheit;
DROP TABLE IF EXISTS OrtDerImpfung_Krankheit_AUD;

# 19.0.0
DROP TABLE IF EXISTS Krankheit;
DROP TABLE IF EXISTS Krankheit_AUD;

# 19.0.2
# Spalte darf nicht geloescht werden, da sonst Slots nicht mehr unique!
# ALTER TABLE Impfslot DROP COLUMN IF EXISTS krankheitIdentifier;
# ALTER TABLE Impfslot_AUD DROP COLUMN IF EXISTS krankheitIdentifier;

# 19.0.4
ALTER TABLE Impfdossier DROP FOREIGN KEY IF EXISTS FK_Impfdossier_registrierung;
drop index IX_Impfdossier_registrierung_krankheit on Impfdossier;
drop index UC_Impfdossier_registrierung_krankheit on Impfdossier;

alter table Impfdossier drop COLUMN IF EXISTS dossierStatus;
alter table Impfdossier_AUD drop COLUMN IF EXISTS dossierStatus;

# Wir loeschen den krankheitsidentifier nicht, sonst bekommen wir ein Problem
# mit bereits bestehenden Regs, die beide Dossiers haben. Diese wären nach dem
# droppen des KrankheitsIdentifiers identisch und wuerden mit V19.0.4 beide nach
# COVID migriert -> verletzt unseren UC
# alter table Impfdossier drop column IF EXISTS krankheitIdentifier;
# alter table Impfdossier_AUD drop COLUMN IF EXISTS krankheitIdentifier;

# Diese (alten) Indexe koennen nicht mehr erstellt werden, wenn es unterdessen eine
# Reg mit Covid- und Affenpocken-Dossier gibt. Wir lassen die Erstellung sein, da die
# Indexe sowieso mit 19.0.4 wieder gedropt werden
# CREATE INDEX IX_Impfdossier_registrierung ON Impfdossier(registrierung_id, id);
# alter table Impfdossier add constraint UC_Impfdossier_registrierung unique (registrierung_id);

# 19.0.5
DROP TABLE ImpfdossierFile;

# 19.0.7
# Impfstoff darf nicht geloescht werden, wenn bereits Impfungen bestehen. Wird im Skript
# 19.0.7 mit ignore hinzugefuegt
# DELETE FROM Impfstoff where id = 'adea588d-edfd-4955-9794-d120cbddbdf2';

