DROP TABLE IF EXISTS ImpfungArchive;

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('ffe53ace-77e4-40d4-8600-c57eca1e5b89',  UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'ARCHIVIERUNG_JOB_BATCH_SIZE', '10');

/*
-- UNDO

CREATE TABLE ImpfungArchive (
	id                VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	data              LONGBLOB     NOT NULL,
	fileName          VARCHAR(255) NOT NULL,
	fileSize          BIGINT       NOT NULL,
	mimeType          VARCHAR(255) NOT NULL,
	registrierung_id  VARCHAR(36)  NOT NULL,

	CONSTRAINT UC_ImpfungArchive_registrierung_id
		UNIQUE (registrierung_id),
	CONSTRAINT FK_ImpfungArchive_registrierung_id
		FOREIGN KEY (registrierung_id) REFERENCES Registrierung (id)
);

DELETE FROM ApplicationProperty WHERE name = 'ARCHIVIERUNG_JOB_BATCH_SIZE';

DELETE from flyway_schema_history where script = 'db/migration/V19.10.0__removeImpfungArchive.sql';
*/
