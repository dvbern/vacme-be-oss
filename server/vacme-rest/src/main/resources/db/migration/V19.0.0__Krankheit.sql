CREATE TABLE  IF NOT EXISTS Krankheit (
	id                          VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt           DATETIME(6)  NOT NULL,
	timestampMutiert            DATETIME(6)  NOT NULL,
	userErstellt                VARCHAR(255) NOT NULL,
	userMutiert                 VARCHAR(255) NOT NULL,
	version                     BIGINT       NOT NULL,
	identifier                  VARCHAR(50)  NOT NULL,
	CONSTRAINT UC_Krankheit_identifier
		UNIQUE (identifier)
);

CREATE TABLE  IF NOT EXISTS Krankheit_AUD (
	id                VARCHAR(36)  NOT NULL,
	REV               INT          NOT NULL,
	REVTYPE           TINYINT      NULL,
	timestampErstellt DATETIME(6)  NULL,
	timestampMutiert  DATETIME(6)  NULL,
	userErstellt      VARCHAR(255) NULL,
	userMutiert       VARCHAR(255) NULL,
	identifier        VARCHAR(50)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FK_Krankheit_aud_rev
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

INSERT IGNORE INTO Krankheit (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, identifier) VALUES
       ('27de07f3-1e9f-4950-bf8c-57c3009a5d3a', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 1, 'COVID'),
       ('4b99ace7-443c-4efc-98a5-388ba12cd1ba', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 1, 'AFFENPOCKEN');

/*
-- UNDO

DROP TABLE Krankheit;
DROP TABLE Krankheit_AUD;

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.0.0__Krankheit.sql';
*/