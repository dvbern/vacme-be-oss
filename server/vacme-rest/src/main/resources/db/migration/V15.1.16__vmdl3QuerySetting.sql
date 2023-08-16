INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('77ceadb3-5301-44aa-b52d-5bbb4313b887', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VMDL_CRON_3QUERIES', 'false');



# update
# update ApplicationProperty set value = 'true' WHERE name = 'VMDL_CRON_3QUERIES';