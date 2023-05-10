ALTER TABLE OrtDerImpfung ADD IF NOT EXISTS personalisierterImpfReport BIT NOT NULL DEFAULT FALSE;
ALTER TABLE OrtDerImpfung_AUD ADD IF NOT EXISTS personalisierterImpfReport BIT NULL;

/**
Undo:

ALTER TABLE OrtDerImpfung DROP IF EXISTS personalisierterImpfReport;
ALTER TABLE OrtDerImpfung_AUD DROP IF EXISTS personalisierterImpfReport;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V16.0.0__personalisierterImpfReport.sql';

 **/