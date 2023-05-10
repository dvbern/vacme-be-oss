INSERT INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('b4d4f041-dfb3-479a-a6f9-9e522d7896d2', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'SHOW_ONBOARDING_WELCOME_TEXT', 'false');

/*
-- revert
DELETE FROM ApplicationProperty
	WHERE name = 'SHOW_ONBOARDING_WELCOME_TEXT';

*/
