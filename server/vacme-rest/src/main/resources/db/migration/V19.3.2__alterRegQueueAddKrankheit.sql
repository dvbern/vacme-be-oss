DELETE  FROM RegistrierungQueue WHERE status = 'SUCCESS';

ALTER TABLE RegistrierungQueue ADD IF NOT EXISTS krankheitIdentifier VARCHAR(50) NULL;
UPDATE RegistrierungQueue SET krankheitIdentifier = 'COVID' WHERE krankheitIdentifier IS NULL;

ALTER TABLE RegistrierungQueue MODIFY krankheitIdentifier  VARCHAR(50) NOT NULL;

CREATE INDEX IF NOT EXISTS IX_RegistrierungQueue_krankheit_status ON RegistrierungQueue (krankheitIdentifier, status);
CREATE INDEX IF NOT EXISTS IX_RegistrierungQueue_status_timestampErstellt ON RegistrierungQueue (status, timestampErstellt);


/**
UNDO:
ALTER TABLE RegistrierungQueue DROP COLUMN IF EXISTS krankheitIdentifier;
DROP INDEX IF EXISTS IX_RegistrierungQueue_krankheit_status ON RegistrierungQueue;
DROP INDEX IF EXISTS IX_RegistrierungQueue_status_timestampErstellt ON RegistrierungQueue;
DELETE FROM flyway_schema_history where version = '19.3.2';
 */