ALTER TABLE RegistrierungFile
	DROP fileExtension;
ALTER TABLE RegistrierungFile_AUD
	DROP fileExtension;

ALTER TABLE RegistrierungFile DROP CONSTRAINT FK_RegistrierungFile_registrierung;
ALTER TABLE RegistrierungFile DROP CONSTRAINT UC_RegistrierungFile_registrierung;

alter table RegistrierungFile
	add constraint FK_RegistrierungFile_registrierung
		foreign key (registrierung_id)
			references Registrierung (id);
