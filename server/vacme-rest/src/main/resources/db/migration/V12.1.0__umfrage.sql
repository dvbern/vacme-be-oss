CREATE TABLE Umfrage (
	id                VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	umfrageCode       VARCHAR(8)   NOT NULL,
	umfrageGruppe     VARCHAR(50)  NOT NULL,
	teilgenommen      BIT          NOT NULL,
	valid             BIT          NOT NULL,
	mobiltelefon      VARCHAR(30)  NOT NULL,
	registrierung_id  VARCHAR(36)  NOT NULL,
	CONSTRAINT FK_Umfrage_registrierung
		FOREIGN KEY (registrierung_id) REFERENCES Registrierung(id)
);

CREATE SEQUENCE umfrage_sequence
	START WITH 1
	INCREMENT BY 1;

