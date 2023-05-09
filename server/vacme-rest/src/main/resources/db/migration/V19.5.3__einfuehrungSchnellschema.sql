ALTER TABLE Impfdossier ADD COLUMN IF NOT EXISTS schnellschema bit NOT NULL default false;
ALTER TABLE Impfdossier_AUD ADD COLUMN IF NOT EXISTS schnellschema bit null;

/*
-- UNDO
ALTER TABLE Impfdossier DROP COLUMN schnellschema;
ALTER TABLE Impfdossier_AUD DROP COLUMN schnellschema;

DELETE from flyway_schema_history where script = 'db/migration/V20.1.1__einfuehrungSchnellschema.sql';
*/
