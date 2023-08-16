INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('a87bcf5a-186b-4a03-a995-72f97974731e', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_BOOSTER_FREIGABE_NOTIFICATION_DISABLED', 'false');

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('ceead97d-e77b-4001-8ec3-aae2ad38c915', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_BOOSTER_FREIGABE_NOTIFICATION_TERMIN_N_DISABLED', 'false');