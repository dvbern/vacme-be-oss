CREATE SEQUENCE hibernate_sequence;


CREATE OR REPLACE TABLE ImpfungQueue(
	id                  BIGINT        NOT NULL  PRIMARY KEY,
	timestampErstellt   DATETIME(6)   NOT NULL,
	timestampMutiert    DATETIME(6)   NOT NULL,
	userErstellt        VARCHAR(255)  NOT NULL,
	userMutiert         VARCHAR(255)  NOT NULL,
	version             BIGINT        NOT NULL,
	errorCount          INT           NOT NULL,
	lastError           VARCHAR(2000) NULL,
	impfungId           VARCHAR(50)   NOT NULL,
	odiId               VARCHAR(50)   NULL,
	registrierungNummer VARCHAR(50)   NULL,
	status              VARCHAR(50)   NOT NULL,
	typ                 VARCHAR(50)   NOT NULL
);


/*
-- UNDO:
DROP TABLE  IF EXISTS hibernate_sequence;
DROP TABLE IF EXISTS ImpfungQueue;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V17.2.0__ImpfungQueue.sql';
*/
