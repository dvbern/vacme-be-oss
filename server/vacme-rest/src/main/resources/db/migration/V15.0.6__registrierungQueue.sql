CREATE SEQUENCE req_queue_sequence START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TABLE RegistrierungQueue (
	id                  BIGINT        NOT NULL  PRIMARY KEY,
	timestampErstellt   DATETIME(6)   NOT NULL,
	timestampMutiert    DATETIME(6)   NOT NULL,
	userErstellt        VARCHAR(255)  NOT NULL,
	userMutiert         VARCHAR(255)  NOT NULL,
	version             BIGINT        NOT NULL,
	errorCount          INT           NOT NULL,
	lastError           VARCHAR(2000) NULL,
	registrierungNummer VARCHAR(50)   NOT NULL,
	status              VARCHAR(50)   NOT NULL,
	typ                 VARCHAR(50)   NOT NULL
);

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('c6ba1dd8-2edc-4beb-a726-5c0002cae56a', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_BOOSTER_RULE_ENGINE_JOB_DISABLED', 'true');

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('3b852605-87fd-4047-bb68-b9aa45faaab8',  UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_BOOSTER_RULE_ENGINE_JOB_BATCH_SIZE', '1');

/*
-- UNDO:
DROP TABLE  IF EXISTS req_queue_sequence;
DROP TABLE IF EXISTS RegistrierungQueue;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.6__registrierungQueue.sql';
*/
