DELETE from flyway_schema_history where script = 'db/migration/V20.0.0__krankheitFSME.sql';

# 19.4.0
DELETE FROM Krankheit where identifier = 'FSME';
DELETE FROM Krankheit_AUD where identifier = 'FSME';
