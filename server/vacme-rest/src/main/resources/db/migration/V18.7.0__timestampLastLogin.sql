ALTER TABLE Registrierung ADD IF NOT EXISTS timestampPhonenumberUpdate DATETIME(6) NULL;
ALTER TABLE Registrierung_AUD ADD IF NOT EXISTS timestampPhonenumberUpdate DATETIME(6) NULL;

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('21318547-eed8-4ccf-b868-c7f749b4a95b', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'MINUTES_BETWEEN_PHONENUMBER_UPDATE', '1440');


/*
-- UNDO

ALTER TABLE Registrierung DROP COLUMN timestampPhonenumberUpdate;
ALTER TABLE Registrierung_AUD DROP COLUMN timestampPhonenumberUpdate;

DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V18.7.0__timestampLastLogin.sql';
*/