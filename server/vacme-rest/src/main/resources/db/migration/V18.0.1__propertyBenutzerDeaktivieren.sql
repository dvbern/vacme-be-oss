INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('918aa7d8-4448-4bd7-a545-d652a10ebc4e', '2022-07-06 00:00:0', '2022-07-06 00:00:0', 'flyway', 'flyway', 0,
		'VACME_DEACTIVATE_UNUSED_USERACCOUNTS_JOB_DISABLED', 'false');

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('3843c799-7ce0-436a-8b06-4450a2ec0dbb', '2022-07-06 00:00:0', '2022-07-06 00:00:0', 'flyway', 'flyway', 0,
		'VACME_DEACTIVATE_UNUSED_USERACCOUNTS_AFTER_MINUTES', '43200');

/*
-- UNDO:
DELETE FROM ApplicationProperty WHERE name = 'VACME_DEACTIVATE_UNUSED_USERACCOUNTS_JOB_DISABLED';
DELETE FROM ApplicationProperty WHERE name = 'VACME_DEACTIVATE_UNUSED_USERACCOUNTS_AFTER_MINUTES';
*/