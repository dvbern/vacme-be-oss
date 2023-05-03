ALTER TABLE Impfdossier ADD COLUMN externGeimpftConfirmationNeeded BIT NULL;
ALTER TABLE Impfdossier_AUD ADD COLUMN externGeimpftConfirmationNeeded BIT NULL;

UPDATE Impfdossier SET externGeimpftConfirmationNeeded = FALSE WHERE dossierStatus != 'NEU' and dossierStatus != 'FREIGEGEBEN';
UPDATE Impfdossier SET externGeimpftConfirmationNeeded = TRUE WHERE dossierStatus = 'NEU' or dossierStatus = 'FREIGEGEBEN';

# Wenn es noch keine Affenpockenimpfung gibt dann koennen wir das Flag auf true setzen
UPDATE Impfdossier DOS
SET externGeimpftConfirmationNeeded = TRUE
WHERE krankheitIdentifier = 'AFFENPOCKEN' AND
	NOT EXISTS(SELECT 1
			   FROM Impfdossier D
					INNER JOIN Impfdossiereintrag DE ON D.id = DE.impfdossier_id
					INNER JOIN Impftermin T ON DE.impftermin_id = T.id
					INNER JOIN Impfung I ON T.id = I.termin_id
			   WHERE D.id = DOS.id);

# change to not nullable
ALTER TABLE Impfdossier MODIFY externGeimpftConfirmationNeeded BIT NOT NULL;

/*
-- UNDO

ALTER TABLE Impfdossier DROP COLUMN IF EXISTS externGeimpftConfirmationNeeded;
ALTER TABLE Impfdossier_AUD DROP COLUMN IF EXISTS externGeimpftConfirmationNeeded;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.2.8__externGeimpftConfirmationFlag.sql';
*/