ALTER TABLE Registrierung
	ADD gewuenschterOdi_id VARCHAR(36) NULL;
ALTER TABLE Registrierung_AUD
	ADD gewuenschterOdi_id VARCHAR(36) NULL;

ALTER TABLE Registrierung
	ADD
		CONSTRAINT FK_registrierung_impfzentrum_id
			FOREIGN KEY (gewuenschterOdi_id) REFERENCES OrtDerImpfung(id);