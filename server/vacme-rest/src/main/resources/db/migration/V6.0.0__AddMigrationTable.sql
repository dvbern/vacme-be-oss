CREATE TABLE Migration (
       id                                VARCHAR(36)   NOT NULL PRIMARY KEY,
       timestampErstellt                 DATETIME(6)   NOT NULL,
       timestampMutiert                  DATETIME(6)   NOT NULL,
       userErstellt                      VARCHAR(255)  NOT NULL,
       userMutiert                       VARCHAR(255)  NOT NULL,
       version                           BIGINT        NOT NULL,
       impfung_id                        VARCHAR(36)   NOT NULL,
       impfortGLN                        VARCHAR(255)  NULL,
       verantwortlicherPersonGLN         VARCHAR(255)  NULL,
       durchfuehrendPersonGLN            VARCHAR(255)  NULL,
       CONSTRAINT UC_Migration_Impfung UNIQUE (impfung_id),
       CONSTRAINT FK_Migration_Impfung FOREIGN KEY (impfung_id) REFERENCES Impfung(id)
);

CREATE TABLE Migration_AUD (
       id                                VARCHAR(36)   NOT NULL,
       REV                               INT           NOT NULL,
       REVTYPE                           TINYINT       NULL,
       timestampErstellt                 DATETIME(6)   NULL,
       timestampMutiert                  DATETIME(6)   NULL,
       userErstellt                      VARCHAR(255)  NULL,
       userMutiert                       VARCHAR(255)  NULL,
       impfung_id                        VARCHAR(36)   NULL,
       impfortGLN                        VARCHAR(255)  NULL,
       verantwortlicherPersonGLN         VARCHAR(255)  NULL,
       durchfuehrendPersonGLN            VARCHAR(255)  NULL,
       PRIMARY KEY (id, REV),
       CONSTRAINT FKmaytu2xd2ebgy0wwp9l5ouis6
           FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

ALTER TABLE Registrierung ADD externalId VARCHAR(255) NULL;
ALTER TABLE Registrierung ADD anonymisiert BIT NOT NULL;

ALTER TABLE Registrierung ADD CONSTRAINT UC_Registrierung_externalId UNIQUE (externalId);
CREATE INDEX IX_Registrierung_externalId ON Registrierung(externalId);

ALTER TABLE Registrierung_AUD ADD externalId VARCHAR(255) NULL;
ALTER TABLE Registrierung_AUD ADD anonymisiert BIT NULL;
