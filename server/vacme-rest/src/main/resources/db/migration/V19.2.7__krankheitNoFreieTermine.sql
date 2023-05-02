ALTER TABLE Krankheit ADD noFreieTermine BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE Krankheit_AUD ADD noFreieTermine BOOLEAN NULL;

DELETE IGNORE FROM ApplicationProperty WHERE name = 'GLOBAL_NO_FREIE_TERMINE';

/*
-- UNDO

ALTER TABLE Krankheit DROP COLUMN IF EXISTS noFreieTermine;
ALTER TABLE Krankheit_AUD DROP COLUMN IF EXISTS noFreieTermine;
INSERT INTO ApplicationProperty (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, name, value) VALUES ('5d0f40c7-8923-4ea4-8f2a-e3b3e9663cb7', '2022-03-29 00:00:00.000000', '2022-03-29 00:00:00.000000', 'flyway', 'flyway', 0, 'GLOBAL_NO_FREIE_TERMINE', 'false');
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.2.7__krankheitNoFreieTermine.sql';
*/