CREATE TABLE ApplicationMessage (
	id                VARCHAR(36)   NOT NULL PRIMARY KEY,
	timestampErstellt DATETIME(6)   NOT NULL,
	timestampMutiert  DATETIME(6)   NOT NULL,
	userErstellt      VARCHAR(255)  NOT NULL,
	userMutiert       VARCHAR(255)  NOT NULL,
	version           BIGINT        NOT NULL,
	bis               DATETIME(6)   NOT NULL,
	von               DATETIME(6)   NOT NULL,
	status			  VARCHAR(50)   NOT NULL,
	htmlContent       VARCHAR(2000) NULL,
	title			  VARCHAR(255)  NULL
);