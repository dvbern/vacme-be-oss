CREATE TABLE AbgesagteTermine (
	id                      VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt       DATETIME(6)  NOT NULL,
	timestampMutiert        DATETIME(6)  NOT NULL,
	userErstellt            VARCHAR(255) NOT NULL,
	userMutiert             VARCHAR(255) NOT NULL,
	version                 BIGINT       NOT NULL,
	ortDerImpfung_id        VARCHAR(36)  NOT NULL,
	termin1                     DATETIME(6)  NULL,
	termin2                     DATETIME(6)  NULL,
	CONSTRAINT FK_abgesagtetermine_impfzentrum_id
		FOREIGN KEY (ortDerImpfung_id) REFERENCES OrtDerImpfung(id)
);

ALTER TABLE Registrierung
	ADD COLUMN abgesagteTermine_id VARCHAR(36) NULL;
ALTER TABLE Registrierung_AUD
	ADD COLUMN abgesagteTermine_id VARCHAR(36) NULL;

ALTER TABLE Registrierung
	ADD
		CONSTRAINT FK_registrierung_abgesagtetermine_id
			FOREIGN KEY (abgesagteTermine_id) REFERENCES AbgesagteTermine(id);