CREATE INDEX IX_Impftermin_gebucht ON Impftermin(gebucht);

create index IX_Impftermin_gebucht_folge on Impftermin (impfslot_id, gebucht, impffolge);
create index IX_Impftermin_odi_id_bis on Impfslot (ortDerImpfung_id, bis);
