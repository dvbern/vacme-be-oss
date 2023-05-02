ALTER TABLE OrtDerImpfung
	ADD booster BIT NOT NULL;

ALTER TABLE OrtDerImpfung_AUD ADD booster BIT NULL;

CREATE TABLE OrtDerImpfung_Impfstoff (
	impfstoff_id     VARCHAR(36) NOT NULL,
	ortDerImpfung_id VARCHAR(36) NOT NULL,
	PRIMARY KEY (impfstoff_id, ortDerImpfung_id),
	CONSTRAINT ortderimpfung_impfstoff_impfstoff_fk
		FOREIGN KEY (impfstoff_id) REFERENCES Impfstoff(id),
	CONSTRAINT ortderimpfung_impfstoff_ortderimpfung_fk
		FOREIGN KEY (ortDerImpfung_id) REFERENCES OrtDerImpfung(id)
);

CREATE TABLE OrtDerImpfung_Impfstoff_AUD (
	impfstoff_id      VARCHAR(36)  NOT NULL,
	ortDerImpfung_id  VARCHAR(36)  NOT NULL,
	REV               INT          NOT NULL,
	REVTYPE           TINYINT      NULL,
	PRIMARY KEY (impfstoff_id, ortDerImpfung_id, REV),
	CONSTRAINT ortderimpfung_impfstoff_aud_rev
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE INDEX ortderimpfung_impfstoff_ortderimpfung_fk_ix
	ON OrtDerImpfung_Impfstoff(ortDerImpfung_id);

CREATE INDEX ortderimpfung_impfstoff_impfstoff_fk_ix
	ON OrtDerImpfung_Impfstoff(impfstoff_id);

/*
-- UNDO:
ALTER TABLE OrtDerImpfung
	DROP COLUMN booster;
ALTER TABLE OrtDerImpfung_AUD
	DROP COLUMN booster;

DROP TABLE OrtDerImpfung_Impfstoff;
DROP TABLE OrtDerImpfung_Impfstoff_AUD;

DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.1__boosterODI.sql';
*/