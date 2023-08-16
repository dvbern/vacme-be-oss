CREATE TABLE IF NOT EXISTS MandantProperty (
	id                VARCHAR(36)  NOT NULL,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	mandant           VARCHAR(50)  NOT NULL,
	name              VARCHAR(255) NOT NULL,
	value             VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS MandantProperty_AUD (
	id                VARCHAR(36) NOT NULL,
	REV               INTEGER     NOT NULL,
	REVTYPE           TINYINT,
	timestampErstellt DATETIME(6),
	timestampMutiert  DATETIME(6),
	userErstellt      VARCHAR(255),
	userMutiert       VARCHAR(255),
	mandant           VARCHAR(50),
	name              VARCHAR(255),
	value             VARCHAR(255),
	PRIMARY KEY (id, REV)
);

ALTER TABLE MandantProperty_AUD
ADD CONSTRAINT FK_MandantProperty_AUD_revinfo FOREIGN KEY IF NOT EXISTS (REV) REFERENCES REVINFO(REV);

ALTER TABLE MandantProperty
ADD CONSTRAINT UC_MandantProperty_name_mandantIdentifier UNIQUE (name, mandant);

# REPORTING_ANZAHL_ERSTIMPFUNGEN_DISABLED
INSERT IGNORE INTO MandantProperty (id, timestampErstellt, timestampMutiert, version, mandant, name, value,
									userErstellt, userMutiert)
VALUES (UUID(), UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0, 'BE', 'REPORTING_ANZAHL_ERSTIMPFUNGEN_DISABLED', 'true', 'flyway',
		'flyway');


# REPORTING_ANZAHL_ERSTIMPFUNGEN_EMPFAENGER
INSERT IGNORE INTO MandantProperty (id, timestampErstellt, timestampMutiert, version, mandant, name, value,
									userErstellt, userMutiert)
VALUES (UUID(), UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0, 'BE', 'REPORTING_ANZAHL_ERSTIMPFUNGEN_EMPFAENGER', 'unused',
		'flyway', 'flyway');
