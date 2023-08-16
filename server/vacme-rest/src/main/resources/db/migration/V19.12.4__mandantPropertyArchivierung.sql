# ARCHIVIERUNG_D3_DISABLED
INSERT IGNORE INTO MandantProperty (id, timestampErstellt, timestampMutiert, version, mandant, name, value,
									userErstellt, userMutiert)
VALUES (UUID(), UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0, 'BE', 'ARCHIVIERUNG_D3_DISABLED', 'false', 'flyway', 'flyway');