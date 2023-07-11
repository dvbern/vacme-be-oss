ALTER TABLE ExternesZertifikat ADD IF NOT EXISTS letzteImpfungDateUnknown BOOLEAN NULL;
ALTER TABLE ExternesZertifikat_AUD ADD IF NOT EXISTS letzteImpfungDateUnknown BOOLEAN NULL;

UPDATE ExternesZertifikat SET letzteImpfungDateUnknown = FALSE WHERE letzteImpfungDateUnknown IS NULL;

ALTER TABLE ExternesZertifikat MODIFY letzteImpfungDateUnknown BOOLEAN NOT NULL;
# ist neu Optional
ALTER TABLE ExternesZertifikat MODIFY letzteImpfungDate DATE NULL;