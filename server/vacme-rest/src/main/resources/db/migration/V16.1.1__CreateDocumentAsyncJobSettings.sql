INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('d21b7d49-c4ec-44cc-b946-a3a0cb51eb84', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_ASYNC_DOCUMENT_CREATION_DISABLED', 'false');

