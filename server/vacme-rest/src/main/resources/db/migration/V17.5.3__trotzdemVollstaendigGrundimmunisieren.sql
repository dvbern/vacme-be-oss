ALTER TABLE ExternesZertifikat ADD COLUMN IF NOT EXISTS trotzdemVollstaendigGrundimmunisieren BOOLEAN;
ALTER TABLE ExternesZertifikat_AUD ADD COLUMN IF NOT EXISTS trotzdemVollstaendigGrundimmunisieren BOOLEAN;


/*
-- UNDO:
ALTER TABLE ExternesZertifikat DROP COLUMN trotzdemVollstaendigGrundimmunisieren;
ALTER TABLE ExternesZertifikat_AUD DROP COLUMN trotzdemVollstaendigGrundimmunisieren;
DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V17.5.3__trotzdemVollstaendigGrundimmunisieren.sql';
*/
