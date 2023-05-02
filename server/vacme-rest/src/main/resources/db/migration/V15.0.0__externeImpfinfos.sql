CREATE TABLE ExterneImpfinfo (
	id                VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	vonCoronaGenesen  BIT          NULL,
	registrierung_id  VARCHAR(36)  NOT NULL,
	CONSTRAINT UC_ExterneImpfinfo_registrierung
		UNIQUE (registrierung_id),
	CONSTRAINT FK_ExterneImpfinfo_registrierung
		FOREIGN KEY (registrierung_id) REFERENCES Registrierung(id)
);

CREATE INDEX IX_ExterneImpfinfo_registrierung
	ON ExterneImpfinfo(registrierung_id, id);

CREATE TABLE ExterneImpfinfo_AUD (
	id                VARCHAR(36)  NOT NULL,
	REV               INT          NOT NULL,
	REVTYPE           TINYINT      NULL,
	timestampErstellt DATETIME(6)  NULL,
	timestampMutiert  DATETIME(6)  NULL,
	userErstellt      VARCHAR(255) NULL,
	userMutiert       VARCHAR(255) NULL,
	vonCoronaGenesen  BIT          NULL,
	registrierung_id  VARCHAR(36)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FK_ExterneImpfinfo_aud_rev
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);
CREATE TABLE ExternerImpfinfoEintrag (
	id                    VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt     DATETIME(6)  NOT NULL,
	timestampMutiert      DATETIME(6)  NOT NULL,
	userErstellt          VARCHAR(255) NOT NULL,
	userMutiert           VARCHAR(255) NOT NULL,
	version               BIGINT       NOT NULL,
	datumImpfung          DATE         NOT NULL,
	impffolgeNr           INT          NOT NULL,
	kontrollePersonUUID   VARCHAR(36)  NULL,
	kontrolliertTimestamp DATETIME(6)  NULL,
	externeImpfinfo_id    VARCHAR(36)  NOT NULL,
	impfstoff_id          VARCHAR(36)  NOT NULL,
	CONSTRAINT FK_ExternerImpfinfoEintrag_externeImpfinfo
		FOREIGN KEY (externeImpfinfo_id) REFERENCES ExterneImpfinfo(id),
	CONSTRAINT FK_ExternerImpfinfoEintrag_impfstoff_id
		FOREIGN KEY (impfstoff_id) REFERENCES Impfstoff(id)
);

CREATE INDEX IX_ExternerImpfinfoEintrag_externeImpfinfo
	ON ExternerImpfinfoEintrag(externeImpfinfo_id, id);


CREATE TABLE ExternerImpfinfoEintrag_AUD (
	id                    VARCHAR(36)  NOT NULL,
	REV                   INT          NOT NULL,
	REVTYPE               TINYINT      NULL,
	timestampErstellt     DATETIME(6)  NULL,
	timestampMutiert      DATETIME(6)  NULL,
	userErstellt          VARCHAR(255) NULL,
	userMutiert           VARCHAR(255) NULL,
	datumImpfung          DATE         NULL,
	impffolgeNr           INT          NULL,
	kontrollePersonUUID   VARCHAR(36)  NULL,
	kontrolliertTimestamp DATETIME(6)  NULL,
	externeImpfinfo_id    VARCHAR(36)  NULL,
	impfstoff_id          VARCHAR(36)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FK_ExternerImpfinfoEintrag_aud_rev
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);


/*
-- UNDO:
 DROP TABLE ExterneImpfinfo_AUD;
 DROP TABLE ExternerImpfinfoEintrag;
 DROP TABLE ExterneImpfinfo;
 DROP TABLE ExternerImpfinfoEintrag_AUD;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.0__externeImpfinfos.sql';
*/


