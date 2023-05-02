INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('42371ab4-7883-4767-8b16-7d5afdf5f873', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_BOOSTER_FREIGABE_SMS_SLEEP_TIME_MS', '50');


#UNDO
# DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.4__freigabeBoosterBatchjobSettings.sql';