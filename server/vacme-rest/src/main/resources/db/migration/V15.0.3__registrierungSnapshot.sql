CREATE TABLE RegistrierungSnapshot (
	id                                VARCHAR(36)  NOT NULL	PRIMARY KEY,
	timestampErstellt                 DATETIME(6)  NOT NULL,
	timestampMutiert                  DATETIME(6)  NOT NULL,
	userErstellt                      VARCHAR(255) NOT NULL,
	userMutiert                       VARCHAR(255) NOT NULL,
	version                           BIGINT       NOT NULL,
	abgeschlossenZeit                 DATETIME(6)  NULL,
	abgleichElektronischerImpfausweis BIT          NOT NULL,
	anonymisiert                      BIT          NOT NULL,
	contactTracing                    BIT          NULL,
	externalId                        VARCHAR(255) NULL,
	geburtsdatum                      DATE         NOT NULL,
	generateZertifikat                BIT          NOT NULL,
	gewuenschterOdiId                 VARCHAR(36)  NULL,
	impftermin1Id                     VARCHAR(36)  NULL,
	impftermin2Id                     VARCHAR(36)  NULL,
	krankenkasseKartenNr              VARCHAR(255) NOT NULL,
	name                              VARCHAR(255) NOT NULL,
	nichtVerwalteterOdiSelected       BIT          NOT NULL,
	positivGetestetDatum              DATE         NULL,
	prioritaet                        VARCHAR(50)  NOT NULL,
	registrierungStatus               VARCHAR(50)  NOT NULL,
	registrierungsnummer              VARCHAR(8)   NOT NULL,
	timestampArchiviert               DATETIME(6)  NULL,
	verstorben                        BIT          NULL,
	vollstaendigerImpfschutz          BIT          NULL,
	vorname                           VARCHAR(255) NOT NULL,
	zweiteImpfungVerzichtetZeit       DATETIME(6)  NULL,
	registrierung_id                  VARCHAR(36)  NOT NULL,
	CONSTRAINT FK_RegistrierungSnapshot_registrierung FOREIGN KEY (registrierung_id) REFERENCES Registrierung(id)
);


/** UNDO
  DROP TABLE RegistrierungSnapshot;

  DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.3__registrierungSnapshot.sql';

 */
