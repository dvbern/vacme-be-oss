CREATE TABLE ZertifizierungsToken (
	id                VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	token             VARCHAR(255) NOT NULL,
	gueltigkeit		  DATETIME(6)  NOT NULL
);


CREATE TABLE ZertifikatFile (
	id                VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	data              LONGBLOB     NOT NULL,
	fileExtension     VARCHAR(255) NOT NULL,
	fileName          VARCHAR(255) NOT NULL,
	fileSize          BIGINT       NOT NULL,
	mimeType          VARCHAR(255) NOT NULL
);

CREATE TABLE Zertifikat (
	id                  VARCHAR(36)  	NOT NULL PRIMARY KEY,
	timestampErstellt   DATETIME(6)  	NOT NULL,
	timestampMutiert    DATETIME(6)  	NOT NULL,
	userErstellt        VARCHAR(255) 	NOT NULL,
	userMutiert         VARCHAR(255) 	NOT NULL,
	version             BIGINT       	NOT NULL,
	registrierung_id    VARCHAR(36)  	NOT NULL,
	payload             VARCHAR(2048) 	NOT NULL,
	signature 			VARCHAR(255)  	NOT NULL,
	uvci                VARCHAR(50)  	NOT NULL,
	revoked             BIT          	NOT NULL,
	zertifikatPdf_id 	VARCHAR(36)  	NOT NULL,
	zertifikatQrCode_id VARCHAR(36)  	NOT NULL,
	CONSTRAINT FK_Zertifikat_registrierung
		FOREIGN KEY (registrierung_id) REFERENCES Zertifikat(id),
	CONSTRAINT UC_Zertifikat_zertifikatPdf
		UNIQUE (zertifikatPdf_id),
	CONSTRAINT UC_Zertifikat_zertifikatQrCode
		UNIQUE (zertifikatQrCode_id),
	CONSTRAINT UC_Zertifikat_uvci
		UNIQUE (uvci),
	CONSTRAINT FK_Zertifikat_zertifikatPdf
		FOREIGN KEY (zertifikatPdf_id) REFERENCES Impftermin(id),
	CONSTRAINT FK_Zertifikat_zertifikatQrCode
		FOREIGN KEY (zertifikatQrCode_id) REFERENCES Impftermin(id)
);

