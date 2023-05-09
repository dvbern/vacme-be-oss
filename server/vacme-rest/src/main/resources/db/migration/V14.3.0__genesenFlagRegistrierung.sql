ALTER TABLE Registrierung ADD IF NOT EXISTS genesen BIT NOT NULL DEFAULT FALSE;
ALTER TABLE Registrierung_AUD ADD IF NOT EXISTS genesen BIT NULL;

# Alte Daten migrieren
# 1. Diejenigen, welche noch nicht im Booster-Modus sind: ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG
# 		ohne AbgeschlossenGrund mit vollstaendigem Impfschutz
UPDATE Registrierung set genesen = true
where registrierungStatus = 'ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG'
    	and zweiteImpfungVerzichtetGrund is null
    	and vollstaendigerImpfschutz = true;

# 2. Diejenigen, welche bereits im Booster-Modus sind: IMMUNISIERT, mit Impfung1 aber ohne Impfung2
# 		ohne AbgeschlossenGrund mit vollstaendigem Impfschutz
UPDATE Registrierung set genesen = true
where registrierungStatus = 'IMMUNISIERT'
		and zweiteImpfungVerzichtetGrund is null
	  	and vollstaendigerImpfschutz = true
  		and id in (
			SELECT r.id
			FROM Registrierung r
				 LEFT JOIN Impftermin t1 ON r.impftermin1_id = t1.id
				 LEFT JOIN Impfslot s1 ON t1.impfslot_id = s1.id
				 LEFT JOIN Impfung i1 ON t1.id = i1.termin_id

				 LEFT JOIN Impftermin t2 ON r.impftermin2_id = t2.id
				 LEFT JOIN Impfslot s2 ON t2.impfslot_id = s2.id
				 LEFT JOIN Impfung i2 ON t2.id = i2.termin_id
			WHERE i1.id is not null and i2.id is null
		);

/*
-- UNDO:
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V14.3.0__genesenFlagRegistrierung.sql';
*/


