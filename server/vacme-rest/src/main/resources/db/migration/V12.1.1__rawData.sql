CREATE TABLE RawData (
	id                VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	typ               VARCHAR(50)  NOT NULL,
	data              LONGBLOB     NOT NULL
);
