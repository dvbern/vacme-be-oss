ALTER TABLE ApplicationProperty MODIFY name VARCHAR(255) NOT NULL;

ALTER TABLE  ImpfungQueue MODIFY IF EXISTS impfungId VARCHAR(50) NULL;
RENAME TABLE ImpfungQueue TO MassenverarbeitungQueue;


UPDATE ApplicationProperty
SET name = 'VACME_MASSENVERARBEITUNGQUEUE_PROCESSING_JOB_DISABLED'
WHERE name = 'VACME_IMPFUNGQUEUE_PROCESSING_JOB_DISABLED';

UPDATE ApplicationProperty
SET name = 'VACME_MASSENVERARBEITUNGQUEUE_PROCESSING_JOB_BATCH_SIZE'
WHERE name = 'VACME_IMPFUNGQUEUE_PROCESSING_JOB_BATCH_SIZE';

UPDATE ApplicationProperty
SET name = 'VACME_MASSENVERARBEITUNGQUEUE_PROCESSING_JOB_PARTITIONS'
WHERE name = 'VACME_IMPFUNGQUEUE_PROCESSING_JOB_PARTITIONS';


UPDATE ApplicationProperty_AUD
SET name = 'VACME_MASSENVERARBEITUNGQUEUE_PROCESSING_JOB_DISABLED'
WHERE name = 'VACME_IMPFUNGQUEUE_PROCESSING_JOB_DISABLED';

UPDATE ApplicationProperty_AUD
SET name = 'VACME_MASSENVERARBEITUNGQUEUE_PROCESSING_JOB_BATCH_SIZE'
WHERE name = 'VACME_IMPFUNGQUEUE_PROCESSING_JOB_BATCH_SIZE';

UPDATE ApplicationProperty_AUD
SET name = 'VACME_MASSENVERARBEITUNGQUEUE_PROCESSING_JOB_PARTITIONS'
WHERE name = 'VACME_IMPFUNGQUEUE_PROCESSING_JOB_PARTITIONS';
