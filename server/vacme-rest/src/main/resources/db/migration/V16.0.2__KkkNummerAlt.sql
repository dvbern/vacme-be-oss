CREATE TABLE IF NOT EXISTS KkkNummerAlt (
	id                    VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt     DATETIME(6)  NOT NULL,
	timestampMutiert      DATETIME(6)  NOT NULL,
	userErstellt          VARCHAR(255) NOT NULL,
	userMutiert           VARCHAR(255) NOT NULL,
	version               BIGINT       NOT NULL,

	registrierung_id  VARCHAR(36)  NOT NULL,
	nummer            VARCHAR(20)  NOT NULL,
	aktivBis          DATETIME(6)  NOT NULL,
	CONSTRAINT FK_KkkNummerAlt_registrierung FOREIGN KEY (registrierung_id) REFERENCES Registrierung(id)
);

CREATE TABLE IF NOT EXISTS KkkNummerAlt_AUD (
	id                VARCHAR(36)  NOT NULL,
	REV               INT          NOT NULL,
	REVTYPE           TINYINT      NULL,
	timestampErstellt DATETIME(6)  NULL,
	timestampMutiert  DATETIME(6)  NULL,
	userErstellt      VARCHAR(255) NULL,
	userMutiert       VARCHAR(255) NULL,

	registrierung_id  VARCHAR(36)  NULL,
	nummer            VARCHAR(20)  NULL,
	aktivBis          DATETIME(6)  NULL,

	PRIMARY KEY (id, REV),
	CONSTRAINT FK_KkkNummerAlt_aud_rev
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);


CREATE INDEX IF NOT EXISTS IX_KkkNummerAlt_nummer
	ON KkkNummerAlt(nummer, id);


/*
-- UNDO

DROP TABLE IF EXISTS KkkNummerAlt;
DROP TABLE IF EXISTS KkkNummerAlt_AUD;
DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V16.0.2__KkkNummerAlt.sql';
  */