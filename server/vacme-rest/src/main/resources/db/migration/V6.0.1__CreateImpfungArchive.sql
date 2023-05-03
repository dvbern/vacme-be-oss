CREATE TABLE ImpfungArchive (
	id                VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	data              LONGBLOB     NOT NULL,
	fileName          VARCHAR(255) NOT NULL,
	fileSize          BIGINT       NOT NULL,
	mimeType          VARCHAR(255) NOT NULL,
	registrierung_id  VARCHAR(36)  NOT NULL,

	CONSTRAINT UC_ImpfungArchive_registrierung_id
		UNIQUE (registrierung_id),
	CONSTRAINT FK_ImpfungArchive_registrierung_id
		FOREIGN KEY (registrierung_id) REFERENCES Registrierung (id)
);