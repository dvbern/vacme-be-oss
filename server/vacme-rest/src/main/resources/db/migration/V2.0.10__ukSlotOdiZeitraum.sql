ALTER TABLE Impfslot
	ADD CONSTRAINT UK_Impfslot_odi_bis UNIQUE (ortDerImpfung_id, bis);