CREATE SEQUENCE IF NOT EXISTS document_queue_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS DocumentQueueResult (
	id                VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	data              LONGBLOB     NOT NULL,
	fileName          VARCHAR(255) NOT NULL,
	fileSize          BIGINT       NOT NULL,
	mimeType          VARCHAR(255) NOT NULL
);


CREATE TABLE IF NOT EXISTS DocumentQueue (
	DTYPE                  VARCHAR(31)   NOT NULL,
	id                     BIGINT        NOT NULL PRIMARY KEY,
	timestampErstellt      DATETIME(6)   NOT NULL,
	timestampMutiert       DATETIME(6)   NOT NULL,
	userErstellt           VARCHAR(255)  NOT NULL,
	userMutiert            VARCHAR(255)  NOT NULL,
	version                BIGINT        NOT NULL,
	errorCount             INT           NOT NULL,
	jobParameters          VARCHAR(4096) NULL,
	lastError              VARCHAR(2000) NULL,
	resultTimestamp        DATETIME(6)   NULL,
	status                 VARCHAR(50)   NOT NULL,
	typ                    VARCHAR(50)   NOT NULL,
	benutzer_id            VARCHAR(36)   NOT NULL,
	documentQueueResult_id VARCHAR(36)   NULL,
	CONSTRAINT FK_DocumentQueue_benutzer
		FOREIGN KEY (benutzer_id) REFERENCES Benutzer(id),
	CONSTRAINT FK_DocumentQueue_documentQueueResult_id
		FOREIGN KEY (documentQueueResult_id) REFERENCES DocumentQueueResult(id)
);

CREATE INDEX IF NOT EXISTS IX_DocumentQueue_DTYPE
	ON DocumentQueue(DTYPE);

CREATE INDEX IF NOT EXISTS IX_DocumentQueue_DTYPE_benutzer
	ON DocumentQueue(DTYPE, benutzer_id, id);



/*
DROP INDEX IX_DocumentQueue_DTYPE ON DocumentQueue;
DROP INDEX IX_DocumentQueue_DTYPE_benutzer ON DocumentQueue;

DROP TABLE DocumentQueueResult;
DROP TABLE DocumentQueue;

DROP SEQUENCE document_queue_sequence;

DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V16.1.0__CreateDocumentQueue.sql';
*/