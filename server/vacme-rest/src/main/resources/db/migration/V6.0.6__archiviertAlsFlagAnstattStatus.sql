ALTER TABLE Registrierung ADD timestampArchiviert DATETIME(6) NULL;
ALTER TABLE Registrierung_AUD ADD timestampArchiviert DATETIME(6) NULL;
# Zum jetzigen Zeitpunkt sind wir sicher, dass alle ARCHIVIERTen Regs vorher ABGESCHLOSSEN waren
update Registrierung set registrierungStatus = 'ABGESCHLOSSEN' where registrierungStatus = 'ARCHIVIERT';
update Registrierung_AUD set registrierungStatus = 'ABGESCHLOSSEN' where registrierungStatus = 'ARCHIVIERT';
# Alle Archivdateien sollen neu erstellt werden (neu nach 3 Monaten)
delete from ImpfungArchive where 1=1;
