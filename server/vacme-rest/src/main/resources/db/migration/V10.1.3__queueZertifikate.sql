CREATE TABLE ZertifikatQueue (
	id                       VARCHAR(36)   NOT NULL PRIMARY KEY,
	timestampErstellt        DATETIME(6)   NOT NULL,
	timestampMutiert         DATETIME(6)   NOT NULL,
	userErstellt             VARCHAR(255)  NOT NULL,
	userMutiert              VARCHAR(255)  NOT NULL,
	version                  BIGINT        NOT NULL,
	typ                  	 VARCHAR(50)   NOT NULL,
	errorCount               INT           NULL,
	lastError                VARCHAR(2000) NULL,
	status                   VARCHAR(50)   NOT NULL,
	zertifikatToRevoke_id    VARCHAR(36)   NOT NULL,
	CONSTRAINT FK_Queue_zertifikat
		FOREIGN KEY (zertifikatToRevoke_id) REFERENCES Zertifikat(id)
);

INSERT INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('47460b99-cc86-420e-840d-7fb7e0fe81ae', '2021-06-10 00:00:0', '2021-06-10 00:00:0', 'flyway', 'flyway', 0,
		'VACME_COVID_CERT_BATCHJOB_REVOC_ONLINE_DISABLED', 'true');

INSERT INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('fe739753-5de3-4e69-ae02-e68acea87c07', '2021-06-10 00:00:0', '2021-06-10 00:00:0', 'flyway', 'flyway', 0,
		'VACME_COVID_CERT_BATCHJOB_REVOC_POST_DISABLED', 'true');

INSERT INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('5ff149e3-0c6c-4d6d-83d7-a17ce220d5b8', '2021-06-09 00:00:0', '2021-06-09 00:00:0', 'flyway', 'flyway', 0,
		'VACME_COVIDAPI_REVOCATION_ONLINE_PS_BATCHJOB_LOCK', 'false');

INSERT INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('189329a4-9883-4b38-ba1a-9e1cc62cf9b5', '2021-06-09 00:00:0', '2021-06-09 00:00:0', 'flyway', 'flyway', 0,
		'VACME_COVIDAPI_REVOCATION_POST_PS_BATCHJOB_LOCK', 'false');