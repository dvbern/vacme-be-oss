ALTER TABLE Onboarding
	ADD COLUMN onboardingPdf_id varchar(36);

ALTER TABLE Onboarding
	ADD CONSTRAINT FK_Onboarding_onboardingPdf FOREIGN KEY (onboardingPdf_id) REFERENCES RegistrierungFile(id);

ALTER TABLE Onboarding_AUD
	ADD COLUMN onboardingPdf_id varchar(36);

DROP TABLE OnboardingFile;



/*
UNDO:


ALTER TABLE Onboarding
	DROP CONSTRAINT FK_Onboarding_onboardingPdf;
ALTER TABLE Onboarding
	DROP COLUMN onboardingPdf_id;
ALTER TABLE Onboarding_AUD
	DROP COLUMN onboardingPdf_id;

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

*/