# Add WellID to Benutzer

alter table Benutzer add column wellId varchar(255);
alter table Benutzer_AUD add column wellId varchar(255);

/*
-- UNDO:
alter table Benutzer drop column wellId;
alter table Benutzer drop column wellId;


DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.8.0__addWellidTouser.sql';

*/