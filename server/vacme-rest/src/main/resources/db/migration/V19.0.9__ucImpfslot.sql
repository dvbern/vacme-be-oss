DROP INDEX IF EXISTS UK_Impfslot_odi_bis ON Impfslot;

ALTER TABLE Impfslot
ADD CONSTRAINT UC_Impfslot_odi_bis_krankheitIdentifier UNIQUE (ortDerImpfung_id, bis, krankheitIdentifier);


CREATE INDEX IX_Impfslot_odi_bis_krankheitIdentifier ON Impfslot(ortDerImpfung_id, bis, krankheitIdentifier);
DROP INDEX IF EXISTS IX_Impftermin_odi_id_bis ON Impfslot;

