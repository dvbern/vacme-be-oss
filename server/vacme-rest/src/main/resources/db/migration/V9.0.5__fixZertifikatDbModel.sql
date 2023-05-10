ALTER TABLE ZertifikatFile
	DROP COLUMN IF EXISTS fileExtension;

alter table Zertifikat modify payload varchar(4096) not null;
alter table Zertifikat modify signature varchar(2000) not null;


# FKs gingen faelschlicherweise auf die Impftermintable
ALTER TABLE Zertifikat 	DROP FOREIGN KEY FK_Zertifikat_zertifikatPdf;
ALTER TABLE Zertifikat 	DROP FOREIGN KEY FK_Zertifikat_zertifikatQrCode;

ALTER  TABLE  Zertifikat ADD FOREIGN KEY  FK_Zertifikat_zertifikatPdf
		 (zertifikatPdf_id) REFERENCES ZertifikatFile(id);

ALTER  TABLE  Zertifikat ADD FOREIGN KEY  FK_Zertifikat_zertifikatQrCode
		 (zertifikatPdf_id) REFERENCES ZertifikatFile(id);

# FK in Zertifikattabelle der zur Registrierung gehen muesste ging auf die Zertifikatstabelle
ALTER TABLE Zertifikat 	DROP FOREIGN KEY FK_Zertifikat_registrierung;

ALTER  TABLE  Zertifikat ADD FOREIGN KEY  FK_Zertifikat_registrierung
	(registrierung_id) REFERENCES Registrierung(id);