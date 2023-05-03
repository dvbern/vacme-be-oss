create table Massenimport
(
    id                VARCHAR(36)  NOT NULL PRIMARY KEY,
    timestampErstellt DATETIME(6)  NOT NULL,
    timestampMutiert  DATETIME(6)  NOT NULL,
    userErstellt      VARCHAR(255) NOT NULL,
    userMutiert       VARCHAR(255) NOT NULL,
    version           BIGINT       NOT NULL,
    name              VARCHAR(255) NOT NULL,
    date              DATETIME(6)  NULL
);

create table MassenimportRegistrierung
(
    massenimport_id  VARCHAR(36) NOT NULL,
    registrierung_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (massenimport_id, registrierung_id),
    CONSTRAINT UC_MassenimportRegistrierung_registrierung
        UNIQUE (registrierung_id),
    CONSTRAINT FK_MassenimportRegistrierung_massenimport FOREIGN KEY (massenimport_id) REFERENCES Massenimport (id),
    CONSTRAINT FK_MassenimportRegistrierung_registrierung FOREIGN KEY (registrierung_id) REFERENCES Registrierung (id)
)
