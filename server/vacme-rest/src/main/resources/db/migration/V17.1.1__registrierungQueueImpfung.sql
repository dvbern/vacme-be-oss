ALTER TABLE RegistrierungQueue ADD impfungId VARCHAR(36);

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('d1f0cd89-7e11-497d-bbfb-b6c462b9339d', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_ZERTIFIKAT_COUNTER_RECALCULATION_DISABLED', 'false');