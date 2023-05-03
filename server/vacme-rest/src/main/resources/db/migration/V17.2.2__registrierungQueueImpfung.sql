INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('11655e21-d659-4992-90b5-079bd0c8aabb', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_IMPFUNGQUEUE_PROCESSING_JOB_DISABLED', 'false');


INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('4b9595c8-d21d-4b48-9143-5039111c1166', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_IMPFUNGQUEUE_PROCESSING_JOB_BATCH_SIZE', '200');



INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('81e9d1a1-8fe1-4316-bba1-23399eccb1ca', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_IMPFUNGQUEUE_PROCESSING_JOB_PARTITIONS', '3');

