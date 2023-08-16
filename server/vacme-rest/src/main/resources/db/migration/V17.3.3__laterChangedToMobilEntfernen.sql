# Alle die immobil=true haben, aber laterChangedToMobil ebenfalls true, sind nicht mehr immobil
update Registrierung set immobil = false where immobil is true and laterChangedToMobil is true;

ALTER TABLE Registrierung DROP COLUMN  IF EXISTS  laterChangedToMobil;
ALTER TABLE Registrierung_AUD DROP COLUMN  IF EXISTS  laterChangedToMobil;