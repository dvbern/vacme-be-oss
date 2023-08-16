CREATE TABLE Onboarding (
	id                              VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt               DATETIME(6)  NOT NULL,
	timestampMutiert                DATETIME(6)  NOT NULL,
	userErstellt                    VARCHAR(255) NOT NULL,
	userMutiert                     VARCHAR(255) NOT NULL,
	version                         BIGINT       NOT NULL,
	code                            VARCHAR(50)  NOT NULL,
	numOfTries                      BIGINT       NULL,
	onboardingTempToken             VARCHAR(50)  NULL,
	onboardingTempTokenCreationTime DATETIME(6)  NULL,
	used                            BIT          NULL,
	registrierung_id                VARCHAR(36)  NOT NULL,
	CONSTRAINT UC_Onboarding_code
		UNIQUE (code),
	CONSTRAINT FK_Onboarding_registrierung
		FOREIGN KEY (registrierung_id) REFERENCES Registrierung(id)
);

CREATE TABLE Onboarding_AUD (
	id                              VARCHAR(36)  NOT NULL,
	REV                             INT          NOT NULL,
	REVTYPE                         TINYINT      NULL,
	timestampErstellt               DATETIME(6)  NULL,
	timestampMutiert                DATETIME(6)  NULL,
	userErstellt                    VARCHAR(255) NULL,
	userMutiert                     VARCHAR(255) NULL,
	code                            VARCHAR(50)  NULL,
	numOfTries                      BIGINT       NULL,
	onboardingTempToken             VARCHAR(50)  NULL,
	onboardingTempTokenCreationTime DATETIME(6)  NULL,
	used                            BIT          NULL,
	registrierung_id                VARCHAR(36)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FK_Onboarding_aud_rev
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE OnboardingFile (
	id                VARCHAR(36)  NOT NULL
		PRIMARY KEY,
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



