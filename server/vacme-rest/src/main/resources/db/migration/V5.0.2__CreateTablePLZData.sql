CREATE TABLE PLZData (
	id                VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	plz               VARCHAR(36)  NOT NULL,
	ortsbez           VARCHAR(50)  NOT NULL,
	kanton            VARCHAR(2)   NOT NULL
);

CREATE INDEX IX_plz
	ON PLZData(plz);
