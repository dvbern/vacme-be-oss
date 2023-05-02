INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('7b711a4f-85fd-4e6a-a0e3-4b08d64547bd', '2022-06-22 00:00:0', '2022-06-22 00:00:0', 'flyway', 'flyway', 0,
		'SELBSTZAHLER_FACHAPPLIKATION_ENABLED', 'false');

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('6983fa65-1858-4b4a-9a95-a21ea5a8f158', '2022-06-22 00:00:0', '2022-06-22 00:00:0', 'flyway', 'flyway', 0,
		'SELBSTZAHLER_PORTAL_ENABLED', 'false');

/*
-- UNDO:
DELETE FROM ApplicationProperty WHERE name = 'SELBSTZAHLER_FACHAPPLIKATION_ENABLED';
DELETE FROM ApplicationProperty WHERE name = 'SELBSTZAHLER_PORTAL_ENABLED';
*/