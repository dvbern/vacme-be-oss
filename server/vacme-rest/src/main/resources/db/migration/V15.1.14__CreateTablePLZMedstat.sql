CREATE TABLE PLZMedstat (
	id                VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	plz 	          VARCHAR(4)   NOT NULL,
	kanton            VARCHAR(2)   NOT NULL,
	medstat           VARCHAR(4)   NOT NULL
);

CREATE INDEX IX_plz_medstat
	ON PLZMedstat(plz);


#UNDO
# DROP TABLE PLZMedstat
# DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.1.14__CreateTablePLZMedstat.sql';