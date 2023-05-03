INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('c2666d67-077c-414a-bc6f-e1faffb0a3af', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'MINUTES_BETWEEN_INFO_UPDATES', '525600');