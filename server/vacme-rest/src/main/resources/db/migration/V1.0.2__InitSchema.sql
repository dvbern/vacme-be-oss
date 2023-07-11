CREATE TABLE ApplicationProperty (
	id                VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	name              VARCHAR(50)  NOT NULL,
	value             VARCHAR(255) NOT NULL,
	CONSTRAINT UC_ApplicationProperty_name
		UNIQUE (name)
);

CREATE TABLE Benutzer (
	id                    VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt     DATETIME(6)  NOT NULL,
	timestampMutiert      DATETIME(6)  NOT NULL,
	userErstellt          VARCHAR(255) NOT NULL,
	userMutiert           VARCHAR(255) NOT NULL,
	version               BIGINT       NOT NULL,
	benutzername          VARCHAR(255) NULL,
	deaktiviert           BIT          NOT NULL,
	email                 VARCHAR(255) NULL,
	geloescht             BIT          NOT NULL,
	geloeschtAm           DATETIME(6)  NULL,
	glnNummer             VARCHAR(255) NULL,
	mobiltelefon          VARCHAR(30)  NULL,
	mobiltelefonValidiert BIT          NOT NULL,
	name                  VARCHAR(255) NOT NULL,
	vorname               VARCHAR(255) NOT NULL,
	CONSTRAINT UC_Benutzer_benutzername
		UNIQUE (benutzername),
	CONSTRAINT UC_Benutzer_mail
		UNIQUE (email)
);

CREATE INDEX IX_Benutzer_email
	ON Benutzer(email, id);

CREATE INDEX IX_Benutzer_version_geloeschtam
	ON Benutzer(id, version, geloeschtAm);

CREATE TABLE BenutzerBerechtigung (
	id                VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	rolle             VARCHAR(50)  NOT NULL,
	benutzer_id       VARCHAR(36)  NOT NULL,
	CONSTRAINT benutzerberechtigung_ux1
		UNIQUE (benutzer_id, rolle),
	CONSTRAINT benutzerberechtigung_benutzer_fk1
		FOREIGN KEY (benutzer_id) REFERENCES Benutzer(id)
);

CREATE INDEX benutzerberechtigung_benutzer_fk1_ix
	ON BenutzerBerechtigung(benutzer_id);

CREATE TABLE Impfstoff (
	id                          VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt           DATETIME(6)  NOT NULL,
	timestampMutiert            DATETIME(6)  NOT NULL,
	userErstellt                VARCHAR(255) NOT NULL,
	userMutiert                 VARCHAR(255) NOT NULL,
	version                     BIGINT       NOT NULL,
	anzahlDosenBenoetigt        INT          NOT NULL,
	anzahlTageZwischenImpfungen INT          NOT NULL,
	code                        VARCHAR(255) NOT NULL,
	name                        VARCHAR(255) NOT NULL,
	CONSTRAINT UC_Impfstoff_code
		UNIQUE (code),
	CONSTRAINT UC_Impfstoff_name
		UNIQUE (name)
);

CREATE TABLE ImpfungkontrolleTermin (
	id                 VARCHAR(36)   NOT NULL
		PRIMARY KEY,
	timestampErstellt  DATETIME(6)   NOT NULL,
	timestampMutiert   DATETIME(6)   NOT NULL,
	userErstellt       VARCHAR(255)  NOT NULL,
	userMutiert        VARCHAR(255)  NOT NULL,
	version            BIGINT        NOT NULL,
	bemerkung          VARCHAR(2000) NULL,
	identitaetGeprueft BIT           NOT NULL
);

CREATE TABLE InitialRegistrierung (
	id          VARCHAR(255) NOT NULL
		PRIMARY KEY,
	initialJson VARCHAR(255) NULL,
	insertTime  DATETIME(6)  NULL
);

CREATE TABLE OrtDerImpfung (
	id                   VARCHAR(36)   NOT NULL
		PRIMARY KEY,
	timestampErstellt    DATETIME(6)   NOT NULL,
	timestampMutiert     DATETIME(6)   NOT NULL,
	userErstellt         VARCHAR(255)  NOT NULL,
	userMutiert          VARCHAR(255)  NOT NULL,
	version              BIGINT        NOT NULL,
	adresse1             VARCHAR(255)  NOT NULL,
	adresse2             VARCHAR(255)  NULL,
	ort                  VARCHAR(255)  NOT NULL,
	plz                  VARCHAR(255)  NOT NULL,
	glnNummer            VARCHAR(255)  NULL,
	identifier           VARCHAR(255)  NULL,
	kommentar            VARCHAR(2000) NULL,
	mobilerOrtDerImpfung BIT           NOT NULL,
	name                 VARCHAR(255)  NOT NULL,
	oeffentlich          BIT           NOT NULL,
	terminverwaltung     BIT           NOT NULL,
	typ                  VARCHAR(50)   NOT NULL,
	zsrNummer            VARCHAR(255)  NULL,
	CONSTRAINT UC_OrtDerImpfung_identifier
		UNIQUE (identifier),
	CONSTRAINT UC_OrtDerImpfung_name
		UNIQUE (name)
);

CREATE TABLE Benutzer_OrtDerImpfung (
	benutzer_id      VARCHAR(36) NOT NULL,
	ortDerImpfung_id VARCHAR(36) NOT NULL,
	PRIMARY KEY (benutzer_id, ortDerImpfung_id),
	CONSTRAINT benutzer_fk
		FOREIGN KEY (benutzer_id) REFERENCES Benutzer(id),
	CONSTRAINT ortderimpfung_fk
		FOREIGN KEY (ortDerImpfung_id) REFERENCES OrtDerImpfung(id)
);

CREATE INDEX benutzer_fk_ix
	ON Benutzer_OrtDerImpfung(benutzer_id);

CREATE INDEX ortderimpfung_fk_ix
	ON Benutzer_OrtDerImpfung(ortDerImpfung_id);

CREATE TABLE Impfslot (
	id                      VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt       DATETIME(6)  NOT NULL,
	timestampMutiert        DATETIME(6)  NOT NULL,
	userErstellt            VARCHAR(255) NOT NULL,
	userMutiert             VARCHAR(255) NOT NULL,
	version                 BIGINT       NOT NULL,
	kapazitaetErsteImpfung  INT          NOT NULL,
	kapazitaetZweiteImpfung INT          NOT NULL,
	bis                     DATETIME(6)  NULL,
	von                     DATETIME(6)  NULL,
	ortDerImpfung_id        VARCHAR(36)  NOT NULL,
	CONSTRAINT FK_impfslot_impfzentrum_id
		FOREIGN KEY (ortDerImpfung_id) REFERENCES OrtDerImpfung(id)
);

CREATE TABLE Impftermin (
	id                VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	impffolge         VARCHAR(255) NOT NULL,
	impfslot_id       VARCHAR(36)  NOT NULL,
	CONSTRAINT FK_impftermin_impfslot_id
		FOREIGN KEY (impfslot_id) REFERENCES Impfslot(id)
);

CREATE TABLE Impfung (
	id                          VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt           DATETIME(6)  NOT NULL,
	timestampMutiert            DATETIME(6)  NOT NULL,
	userErstellt                VARCHAR(255) NOT NULL,
	userMutiert                 VARCHAR(255) NOT NULL,
	version                     BIGINT       NOT NULL,
	fieber                      BIT          NOT NULL,
	lot                         VARCHAR(255) NOT NULL,
	menge                       INT          NOT NULL,
	neueKrankheit               BIT          NOT NULL,
	timestampImpfung            DATETIME(6)  NOT NULL,
	verarbreichungsart          VARCHAR(50)  NOT NULL,
	verarbreichungsort          VARCHAR(50)  NOT NULL,
	verarbreichungsseite        VARCHAR(50)  NOT NULL,
	benutzerDurchfuehrend_id    VARCHAR(36)  NOT NULL,
	benutzerVerantwortlicher_id VARCHAR(36)  NOT NULL,
	impfstoff_id                VARCHAR(36)  NOT NULL,
	termin_id                   VARCHAR(36)  NOT NULL,
	CONSTRAINT UC_Impfung_termin
		UNIQUE (termin_id),
	CONSTRAINT FK_impfung_durchfuehrender_id
		FOREIGN KEY (benutzerDurchfuehrend_id) REFERENCES Benutzer(id),
	CONSTRAINT FK_impfung_impfstoff_id
		FOREIGN KEY (impfstoff_id) REFERENCES Impfstoff(id),
	CONSTRAINT FK_impfung_termin_id
		FOREIGN KEY (termin_id) REFERENCES Impftermin(id),
	CONSTRAINT FK_impfung_verantwortlicher_id
		FOREIGN KEY (benutzerVerantwortlicher_id) REFERENCES Benutzer(id)
);

CREATE TABLE Personenkontrolle (
	id                    VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt     DATETIME(6)  NOT NULL,
	timestampMutiert      DATETIME(6)  NOT NULL,
	userErstellt          VARCHAR(255) NOT NULL,
	userMutiert           VARCHAR(255) NOT NULL,
	version               BIGINT       NOT NULL,
	identifikationsnummer VARCHAR(255) NULL,
	kontrolleTermin1_id   VARCHAR(36)  NULL,
	kontrolleTermin2_id   VARCHAR(36)  NULL,
	CONSTRAINT UC_Personenkontrolle_kontrolleTermin1
		UNIQUE (kontrolleTermin1_id),
	CONSTRAINT UC_Personenkontrolle_kontrolleTermin2
		UNIQUE (kontrolleTermin2_id),
	CONSTRAINT FK_personenkontrolle_kontrolleTermin1
		FOREIGN KEY (kontrolleTermin1_id) REFERENCES ImpfungkontrolleTermin(id),
	CONSTRAINT FK_personenkontrolle_kontrolleTermin2
		FOREIGN KEY (kontrolleTermin2_id) REFERENCES ImpfungkontrolleTermin(id)
);

CREATE TABLE REVINFO (
	REV      INT AUTO_INCREMENT
		PRIMARY KEY,
	REVTSTMP BIGINT NULL
);

CREATE TABLE ApplicationProperty_AUD (
	id                VARCHAR(36)  NOT NULL,
	REV               INT          NOT NULL,
	REVTYPE           TINYINT      NULL,
	timestampErstellt DATETIME(6)  NULL,
	timestampMutiert  DATETIME(6)  NULL,
	userErstellt      VARCHAR(255) NULL,
	userMutiert       VARCHAR(255) NULL,
	name              VARCHAR(50)  NULL,
	value             VARCHAR(255) NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FKjgal99v7otfo5dnchtdo4f0k6
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE BenutzerBerechtigung_AUD (
	id                VARCHAR(36)  NOT NULL,
	REV               INT          NOT NULL,
	REVTYPE           TINYINT      NULL,
	timestampErstellt DATETIME(6)  NULL,
	timestampMutiert  DATETIME(6)  NULL,
	userErstellt      VARCHAR(255) NULL,
	userMutiert       VARCHAR(255) NULL,
	rolle             VARCHAR(50)  NULL,
	benutzer_id       VARCHAR(36)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FK2dcypgie25gy0y308phfeie6n
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE Benutzer_AUD (
	id                    VARCHAR(36)  NOT NULL,
	REV                   INT          NOT NULL,
	REVTYPE               TINYINT      NULL,
	timestampErstellt     DATETIME(6)  NULL,
	timestampMutiert      DATETIME(6)  NULL,
	userErstellt          VARCHAR(255) NULL,
	userMutiert           VARCHAR(255) NULL,
	benutzername          VARCHAR(255) NULL,
	deaktiviert           BIT          NULL,
	email                 VARCHAR(255) NULL,
	geloescht             BIT          NULL,
	geloeschtAm           DATETIME(6)  NULL,
	glnNummer             VARCHAR(255) NULL,
	mobiltelefon          VARCHAR(30)  NULL,
	mobiltelefonValidiert BIT          NULL,
	name                  VARCHAR(255) NULL,
	vorname               VARCHAR(255) NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FKkwlf4e66pkv34e0h02kto7iqp
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE Benutzer_OrtDerImpfung_AUD (
	REV              INT         NOT NULL,
	benutzer_id      VARCHAR(36) NOT NULL,
	ortDerImpfung_id VARCHAR(36) NOT NULL,
	REVTYPE          TINYINT     NULL,
	PRIMARY KEY (REV, benutzer_id, ortDerImpfung_id),
	CONSTRAINT FK4jkp5qmsu503fr56067jjxsyh
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE Fragebogen_AUD (
	id                   VARCHAR(36)  NOT NULL,
	REV                  INT          NOT NULL,
	REVTYPE              TINYINT      NULL,
	timestampErstellt    DATETIME(6)  NULL,
	timestampMutiert     DATETIME(6)  NULL,
	userErstellt         VARCHAR(255) NULL,
	userMutiert          VARCHAR(255) NULL,
	ampel                VARCHAR(50)  NULL,
	beruflicheTaetigkeit VARCHAR(50)  NULL,
	lebensumstaende      VARCHAR(50)  NULL,
	risikogruppe         BIT          NULL,
	personenkontrolle_id VARCHAR(36)  NULL,
	registrierung_id     VARCHAR(36)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FK29gq7qims4vux96f761l6ytrc
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE Impfslot_AUD (
	id                      VARCHAR(36)  NOT NULL,
	REV                     INT          NOT NULL,
	REVTYPE                 TINYINT      NULL,
	timestampErstellt       DATETIME(6)  NULL,
	timestampMutiert        DATETIME(6)  NULL,
	userErstellt            VARCHAR(255) NULL,
	userMutiert             VARCHAR(255) NULL,
	kapazitaetErsteImpfung  INT          NULL,
	kapazitaetZweiteImpfung INT          NULL,
	bis                     DATETIME(6)  NULL,
	von                     DATETIME(6)  NULL,
	ortDerImpfung_id        VARCHAR(36)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FK3n6cpethkgs7dm9ftylp0345l
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE Impfstoff_AUD (
	id                          VARCHAR(36)  NOT NULL,
	REV                         INT          NOT NULL,
	REVTYPE                     TINYINT      NULL,
	timestampErstellt           DATETIME(6)  NULL,
	timestampMutiert            DATETIME(6)  NULL,
	userErstellt                VARCHAR(255) NULL,
	userMutiert                 VARCHAR(255) NULL,
	anzahlDosenBenoetigt        INT          NULL,
	anzahlTageZwischenImpfungen INT          NULL,
	code                        VARCHAR(255) NULL,
	name                        VARCHAR(255) NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FK4817446ydfinu5eikcvcgcigl
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE Impftermin_AUD (
	id                VARCHAR(36)  NOT NULL,
	REV               INT          NOT NULL,
	REVTYPE           TINYINT      NULL,
	timestampErstellt DATETIME(6)  NULL,
	timestampMutiert  DATETIME(6)  NULL,
	userErstellt      VARCHAR(255) NULL,
	userMutiert       VARCHAR(255) NULL,
	impffolge         VARCHAR(255) NULL,
	impfslot_id       VARCHAR(36)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FK52v6qemrk5djxn22oi5p1ge67
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE Impfung_AUD (
	id                          VARCHAR(36)  NOT NULL,
	REV                         INT          NOT NULL,
	REVTYPE                     TINYINT      NULL,
	timestampErstellt           DATETIME(6)  NULL,
	timestampMutiert            DATETIME(6)  NULL,
	userErstellt                VARCHAR(255) NULL,
	userMutiert                 VARCHAR(255) NULL,
	fieber                      BIT          NULL,
	lot                         VARCHAR(255) NULL,
	menge                       INT          NULL,
	neueKrankheit               BIT          NULL,
	timestampImpfung            DATETIME(6)  NULL,
	verarbreichungsart          VARCHAR(50)  NULL,
	verarbreichungsort          VARCHAR(50)  NULL,
	verarbreichungsseite        VARCHAR(50)  NULL,
	benutzerDurchfuehrend_id    VARCHAR(36)  NULL,
	benutzerVerantwortlicher_id VARCHAR(36)  NULL,
	impfstoff_id                VARCHAR(36)  NULL,
	termin_id                   VARCHAR(36)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FKtrep221s5l4kk74q7tcbls1rk
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE ImpfungkontrolleTermin_AUD (
	id                 VARCHAR(36)   NOT NULL,
	REV                INT           NOT NULL,
	REVTYPE            TINYINT       NULL,
	timestampErstellt  DATETIME(6)   NULL,
	timestampMutiert   DATETIME(6)   NULL,
	userErstellt       VARCHAR(255)  NULL,
	userMutiert        VARCHAR(255)  NULL,
	bemerkung          VARCHAR(2000) NULL,
	identitaetGeprueft BIT           NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FK7o03y0i8tpdib94krjpa5org4
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE InitialRegistrierung_AUD (
	id          VARCHAR(255) NOT NULL,
	REV         INT          NOT NULL,
	REVTYPE     TINYINT      NULL,
	initialJson VARCHAR(255) NULL,
	insertTime  DATETIME(6)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FK9ya906njy65wiitnovhsa07p1
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE OrtDerImpfung_AUD (
	id                   VARCHAR(36)   NOT NULL,
	REV                  INT           NOT NULL,
	REVTYPE              TINYINT       NULL,
	timestampErstellt    DATETIME(6)   NULL,
	timestampMutiert     DATETIME(6)   NULL,
	userErstellt         VARCHAR(255)  NULL,
	userMutiert          VARCHAR(255)  NULL,
	adresse1             VARCHAR(255)  NULL,
	adresse2             VARCHAR(255)  NULL,
	ort                  VARCHAR(255)  NULL,
	plz                  VARCHAR(255)  NULL,
	glnNummer            VARCHAR(255)  NULL,
	identifier           VARCHAR(255)  NULL,
	kommentar            VARCHAR(2000) NULL,
	mobilerOrtDerImpfung BIT           NULL,
	name                 VARCHAR(255)  NULL,
	oeffentlich          BIT           NULL,
	terminverwaltung     BIT           NULL,
	typ                  VARCHAR(50)   NULL,
	zsrNummer            VARCHAR(255)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FKfbyanmjheto1ycbioogab8y95
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE Personenkontrolle_AUD (
	id                    VARCHAR(36)  NOT NULL,
	REV                   INT          NOT NULL,
	REVTYPE               TINYINT      NULL,
	timestampErstellt     DATETIME(6)  NULL,
	timestampMutiert      DATETIME(6)  NULL,
	userErstellt          VARCHAR(255) NULL,
	userMutiert           VARCHAR(255) NULL,
	identifikationsnummer VARCHAR(255) NULL,
	kontrolleTermin1_id   VARCHAR(36)  NULL,
	kontrolleTermin2_id   VARCHAR(36)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FKqx2vd1gmp0cxd9cououvp0o95
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE Registrierung (
	id                                VARCHAR(36)   NOT NULL
		PRIMARY KEY,
	timestampErstellt                 DATETIME(6)   NOT NULL,
	timestampMutiert                  DATETIME(6)   NOT NULL,
	userErstellt                      VARCHAR(255)  NOT NULL,
	userMutiert                       VARCHAR(255)  NOT NULL,
	version                           BIGINT        NOT NULL,
	abgleichElektronischerImpfausweis BIT           NOT NULL,
	adresse1                          VARCHAR(255)  NOT NULL,
	adresse2                          VARCHAR(255)  NULL,
	ort                               VARCHAR(255)  NOT NULL,
	plz                               VARCHAR(255)  NOT NULL,
	bemerkung                         VARCHAR(2000) NULL,
	benutzerId                        VARCHAR(36)   NULL,
	geburtsdatum                      DATE          NOT NULL,
	geschlecht                        VARCHAR(50)   NOT NULL,
	immobil                           BIT           NOT NULL,
	krankenkasse                      VARCHAR(50)   NOT NULL,
	krankenkasseKartenNr              VARCHAR(255)  NOT NULL,
	mail                              VARCHAR(255)  NULL,
	mailValidiert                     BIT           NOT NULL,
	name                              VARCHAR(255)  NOT NULL,
	prioritaet                        VARCHAR(50)   NOT NULL,
	registrationTimestamp             DATETIME(6)   NOT NULL,
	registrierungStatus               VARCHAR(50)   NOT NULL,
	registrierungsEingang             VARCHAR(50)   NOT NULL,
	registrierungsnummer              VARCHAR(8)    NOT NULL,
	telefon                           VARCHAR(30)   NULL,
	vorname                           VARCHAR(255)  NOT NULL,
	impftermin1_id                    VARCHAR(36)   NULL,
	impftermin2_id                    VARCHAR(36)   NULL,
	CONSTRAINT UC_Registrierung_impftermin1
		UNIQUE (impftermin1_id),
	CONSTRAINT UC_Registrierung_impftermin2
		UNIQUE (impftermin2_id),
	CONSTRAINT UC_Registrierung_registrierungsnummer
		UNIQUE (registrierungsnummer),
	CONSTRAINT UK_lmdwogyehgug1asxrpmp9ucwd
		UNIQUE (benutzerId),
	CONSTRAINT FK_registrierung_impftermin1_id
		FOREIGN KEY (impftermin1_id) REFERENCES Impftermin(id),
	CONSTRAINT FK_registrierung_impftermin2_id
		FOREIGN KEY (impftermin2_id) REFERENCES Impftermin(id)
);

CREATE TABLE Fragebogen (
	id                   VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt    DATETIME(6)  NOT NULL,
	timestampMutiert     DATETIME(6)  NOT NULL,
	userErstellt         VARCHAR(255) NOT NULL,
	userMutiert          VARCHAR(255) NOT NULL,
	version              BIGINT       NOT NULL,
	ampel                VARCHAR(50)  NOT NULL,
	beruflicheTaetigkeit VARCHAR(50)  NOT NULL,
	lebensumstaende      VARCHAR(50)  NOT NULL,
	risikogruppe         BIT          NOT NULL,
	personenkontrolle_id VARCHAR(36)  NOT NULL,
	registrierung_id     VARCHAR(36)  NOT NULL,
	CONSTRAINT UC_Fragebogen_personenkontrolle
		UNIQUE (personenkontrolle_id),
	CONSTRAINT UC_Fragebogen_registrierung
		UNIQUE (registrierung_id),
	CONSTRAINT FK_fragebogen_personenkontrolle_id
		FOREIGN KEY (personenkontrolle_id) REFERENCES Personenkontrolle(id),
	CONSTRAINT FK_fragebogen_registrierung_id
		FOREIGN KEY (registrierung_id) REFERENCES Registrierung(id)
);

CREATE INDEX IX_registrierungsnummer
	ON Registrierung(registrierungsnummer);

CREATE TABLE RegistrierungFile (
	id                VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	data              LONGBLOB     NOT NULL,
	fileExtension     VARCHAR(255) NOT NULL,
	fileName          VARCHAR(255) NOT NULL,
	fileSize          BIGINT       NOT NULL,
	mimeType          VARCHAR(255) NOT NULL,
	fileTyp           VARCHAR(50)  NOT NULL,
	registrierung_id  VARCHAR(36)  NOT NULL,
	CONSTRAINT UC_RegistrierungFile_registrierung
		UNIQUE (registrierung_id),
	CONSTRAINT FK_RegistrierungFile_registrierung
		FOREIGN KEY (registrierung_id) REFERENCES Registrierung(id)
);

CREATE TABLE RegistrierungFile_AUD (
	id                VARCHAR(36)  NOT NULL,
	REV               INT          NOT NULL,
	REVTYPE           TINYINT      NULL,
	timestampErstellt DATETIME(6)  NULL,
	timestampMutiert  DATETIME(6)  NULL,
	userErstellt      VARCHAR(255) NULL,
	userMutiert       VARCHAR(255) NULL,
	data              LONGBLOB     NULL,
	fileExtension     VARCHAR(255) NULL,
	fileName          VARCHAR(255) NULL,
	fileSize          BIGINT       NULL,
	mimeType          VARCHAR(255) NULL,
	fileTyp           VARCHAR(50)  NULL,
	registrierung_id  VARCHAR(36)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FKg130a6hct7dnasgu2b2464wq7
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE TABLE Registrierung_AUD (
	id                                VARCHAR(36)   NOT NULL,
	REV                               INT           NOT NULL,
	REVTYPE                           TINYINT       NULL,
	timestampErstellt                 DATETIME(6)   NULL,
	timestampMutiert                  DATETIME(6)   NULL,
	userErstellt                      VARCHAR(255)  NULL,
	userMutiert                       VARCHAR(255)  NULL,
	abgleichElektronischerImpfausweis BIT           NULL,
	adresse1                          VARCHAR(255)  NULL,
	adresse2                          VARCHAR(255)  NULL,
	ort                               VARCHAR(255)  NULL,
	plz                               VARCHAR(255)  NULL,
	bemerkung                         VARCHAR(2000) NULL,
	benutzerId                        VARCHAR(36)   NULL,
	geburtsdatum                      DATE          NULL,
	geschlecht                        VARCHAR(50)   NULL,
	immobil                           BIT           NULL,
	krankenkasse                      VARCHAR(50)   NULL,
	krankenkasseKartenNr              VARCHAR(255)  NULL,
	mail                              VARCHAR(255)  NULL,
	mailValidiert                     BIT           NULL,
	name                              VARCHAR(255)  NULL,
	prioritaet                        VARCHAR(50)   NULL,
	registrationTimestamp             DATETIME(6)   NULL,
	registrierungStatus               VARCHAR(50)   NULL,
	registrierungsEingang             VARCHAR(50)   NULL,
	registrierungsnummer              VARCHAR(8)    NULL,
	telefon                           VARCHAR(30)   NULL,
	vorname                           VARCHAR(255)  NULL,
	impftermin1_id                    VARCHAR(36)   NULL,
	impftermin2_id                    VARCHAR(36)   NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FKmaytu2xd2ebgy9ssq7l5ouis6
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);



