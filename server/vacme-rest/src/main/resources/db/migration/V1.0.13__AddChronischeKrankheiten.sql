ALTER TABLE Fragebogen DROP risikogruppe;
ALTER TABLE Fragebogen ADD chronischeKrankheiten varchar(50) DEFAULT 'KEINE' NOT NULL;

ALTER TABLE Fragebogen_AUD DROP risikogruppe;
ALTER TABLE Fragebogen_AUD ADD chronischeKrankheiten varchar(50) NULL;
