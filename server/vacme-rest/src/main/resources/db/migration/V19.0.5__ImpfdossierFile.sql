CREATE TABLE IF NOT EXISTS ImpfdossierFile (
	id                VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	data              LONGBLOB     NOT NULL,
	fileExtension     VARCHAR(255) NOT NULL,
	fileName          VARCHAR(255) NOT NULL,
	fileSize          BIGINT       NOT NULL,
	mimeType          VARCHAR(255) NOT NULL,
	fileTyp           VARCHAR(50)  NOT NULL,
	impfdossier_id  VARCHAR(36)  NOT NULL,
	regenerated BIT NOT NULL DEFAULT FALSE,
	CONSTRAINT FK_ImpfdossierFile_impfdossier
		FOREIGN KEY (impfdossier_id) REFERENCES Impfdossier(id)
);

/*
-- UNDO:

DROP TABLE ImpfdossierFile;

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.0.5__ImpfdossierFile.sql';
*/
