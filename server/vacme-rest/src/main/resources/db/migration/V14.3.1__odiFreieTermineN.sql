ALTER TABLE OrtDerImpfung CHANGE noFreieTermine noFreieTermine1 BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE OrtDerImpfung_AUD CHANGE noFreieTermine noFreieTermine1 BOOLEAN NULL;

ALTER TABLE OrtDerImpfung ADD noFreieTermineN BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE OrtDerImpfung_AUD ADD noFreieTermineN BOOLEAN NULL;


/* UNDO

ALTER TABLE OrtDerImpfung CHANGE noFreieTermine1 noFreieTermine BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE OrtDerImpfung_AUD CHANGE noFreieTermine1 noFreieTermine BOOLEAN NULL;

ALTER TABLE OrtDerImpfung DROP noFreieTermineN;
ALTER TABLE OrtDerImpfung_AUD DROP noFreieTermineN;

DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V14.3.1__odiFreieTermineN.sql';

 */
