# Nur die Files IMPFFREIGABE_DURCH_HAUSARZT muessen migriert werden
INSERT IGNORE INTO ImpfdossierFile (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, data,
									fileName, fileSize, mimeType, fileTyp, impfdossier_id, abgeholt)
SELECT id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, data, fileName, fileSize, mimeType,
	fileTyp,
	(SELECT id
	 FROM Impfdossier
	 WHERE registrierung_id = RegistrierungFile.registrierung_id AND krankheitIdentifier = 'COVID') AS impfdossier_id,
	abgeholt
FROM RegistrierungFile
WHERE fileTyp = 'IMPFFREIGABE_DURCH_HAUSARZT';

# Alle anderen loeschen, sie werden bei Bedarf neu erstellt
DELETE FROM RegistrierungFile where fileTyp = 'TERMIN_BESTAETIGUNG';
DELETE FROM RegistrierungFile where fileTyp = 'TERMIN_BESTAETIGUNG_FTP_FAIL';
DELETE FROM RegistrierungFile where fileTyp = 'IMPF_DOKUMENTATION';
DELETE FROM RegistrierungFile where fileTyp = 'TERMIN_ABSAGE';
DELETE FROM RegistrierungFile where fileTyp = 'TERMIN_ABSAGE_FTP_FAIL';
DELETE FROM RegistrierungFile where fileTyp = 'TERMIN_ZERTIFIKAT_STORNIERUNG';
DELETE FROM RegistrierungFile where fileTyp = 'TERMIN_ZERTIFIKAT_STORNIERUNG_FTP_FAIL';
DELETE FROM RegistrierungFile where fileTyp = 'FREIGABE_BOOSTER_INFO';
DELETE FROM RegistrierungFile where fileTyp = 'FREIGABE_BOOSTER_INFO_FTP_FAIL';
# Die ZERTIFIKAT_COUNTER_RECALCULATION werden momentan mal nicht geloescht
# DELETE FROM RegistrierungFile where fileTyp = 'ZERTIFIKAT_COUNTER_RECALCULATION';
# DELETE FROM RegistrierungFile where fileTyp = 'ZERTIFIKAT_COUNTER_RECALCULATION_FAIL';

# Am Schluss auch noch die migrierten IMPFFREIGABE_DURCH_HAUSARZT aus RegistrierungFile entfernen
DELETE FROM RegistrierungFile where fileTyp = 'IMPFFREIGABE_DURCH_HAUSARZT';


