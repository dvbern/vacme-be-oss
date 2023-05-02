INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('9b62dc22-e86d-442b-b25f-0f578437d998', '2021-06-10 00:00:0', '2021-06-10 00:00:0', 'flyway', 'flyway', 0,
		'VACME_MIGRATION_ZERTIFIKAT_REGEX', 'der-niemals-matchende-prefix');