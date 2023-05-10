INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('5b9e8821-8262-4e17-96a8-3580c9fde787', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VMDL_CRON_DISABLED', 'false');


INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('89079a0f-b235-41a4-8154-3fd3e2545f9f', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VMDL_CRON_MANUAL_TRIGGER_MULTIPLICATOR', '1');


