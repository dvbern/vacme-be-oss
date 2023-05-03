INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('660cffa4-9905-479e-b4be-2af6c5e172f2', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_BOOSTER_FREIGABE_JOB_DISABLED', 'true');

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('e2a62939-9adf-4cbe-a49d-84898059b07f',  UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_BOOSTER_FREIGABE_JOB_BATCH_SIZE', '1');

#UNDO
# DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.4__freigabeBoosterBatchjobSettings.sql';