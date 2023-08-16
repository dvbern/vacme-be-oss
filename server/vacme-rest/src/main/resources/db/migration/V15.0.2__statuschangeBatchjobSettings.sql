INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('1c521ee1-abd5-4ccc-aea3-dc6e780c46f0', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_BOOSTER_STATUSMOVER_JOB_DISABLED', 'true');

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('45bf192a-1c67-11ec-9621-0242ac130002',  UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_BOOSTER_STATUSMOVER_JOB_BATCH_SIZE', '1');

# UNDO
# DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.2__statuschangeBatchjobSettings.sql';