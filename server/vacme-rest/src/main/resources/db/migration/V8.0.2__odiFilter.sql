CREATE TABLE OdiFilter (
	id 				  VARCHAR(36)    NOT NULL PRIMARY KEY,
	minimalWert       DECIMAL(19, 2) NULL,
	maximalWert       DECIMAL(19, 2) NULL,
	stringArgument    VARCHAR(255)    NULL,
	typ   		      VARCHAR(50)    NULL,
    timestampErstellt DATETIME(6)    NOT NULL,
    timestampMutiert  DATETIME(6)    NOT NULL,
    userErstellt      VARCHAR(255)   NOT NULL,
    userMutiert       VARCHAR(255)   NOT NULL,
    version           BIGINT         NOT NULL
);

CREATE TABLE OdiFilter_AUD (
	id 				  VARCHAR(36)    NOT NULL,
	minimalWert       DECIMAL(19, 2) NULL,
	maximalWert       DECIMAL(19, 2) NULL,
	stringArgument    VARCHAR(255)   NULL,
	typ   		      VARCHAR(50)    NULL,
    timestampErstellt DATETIME(6)    NULL,
    timestampMutiert  DATETIME(6)    NULL,
    userErstellt      VARCHAR(255)   NULL,
    userMutiert       VARCHAR(255)   NULL,
	REV               INT        	 NOT NULL,
	REVTYPE           TINYINT        NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT odifilter_aud_rev
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE OdiFilter_OrtDerImpfung (
	odifilter_id      VARCHAR(36)  NOT NULL,
	ortDerImpfung_id  VARCHAR(36)  NOT NULL,
	PRIMARY KEY (odifilter_id, ortDerImpfung_id),
	CONSTRAINT odifilter_ortderimpfung_odifilter_fk
		FOREIGN KEY (odifilter_id) REFERENCES OdiFilter(id),
	CONSTRAINT odifilter_ortderimpfung_ortderimpfung_fk
		FOREIGN KEY (ortDerImpfung_id) REFERENCES OrtDerImpfung(id)
);

CREATE TABLE OdiFilter_OrtDerImpfung_AUD (
	odifilter_id      VARCHAR(36)  NOT NULL,
	ortDerImpfung_id  VARCHAR(36)  NOT NULL,
	REV               INT          NOT NULL,
	REVTYPE           TINYINT      NULL,
	PRIMARY KEY (odifilter_id, ortDerImpfung_id, REV),
	CONSTRAINT odifilter_ortderimpfung_aud_rev
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE INDEX odifilter_ortderimpfung_ortderimpfung_fk_ix
	ON OdiFilter_OrtDerImpfung(ortDerImpfung_id);

CREATE INDEX odifilter_ortderimpfung_odifilter_fk_ix
	ON OdiFilter_OrtDerImpfung(odifilter_id);