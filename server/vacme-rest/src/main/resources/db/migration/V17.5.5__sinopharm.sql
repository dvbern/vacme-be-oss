/* Sinopharm und Sinovac: Impfempfehlung Bugfix: auch schon bei 1 Impfung ist nur noch 1 Grundimpfung notwendig */


# SARS-CoV-2 Vaccine (Vero Cell) [Sinopharm]
update ImpfempfehlungChGrundimmunisierung
set anzahlVerabreicht = 1
where id = '77e0036d-39a4-4359-befd-b419c96ed81b';


# CoronaVac [Sinovac]
update ImpfempfehlungChGrundimmunisierung
set anzahlVerabreicht = 1
where id = 'bae83249-3395-47f9-9ebe-a2a5ecfdbbf6';




/*
-- UNDO:

# SARS-CoV-2 Vaccine (Vero Cell) [Sinopharm]
update ImpfempfehlungChGrundimmunisierung
set anzahlVerabreicht = 2
where id = '77e0036d-39a4-4359-befd-b419c96ed81b';


# CoronaVac [Sinovac]
update ImpfempfehlungChGrundimmunisierung
set anzahlVerabreicht = 2
where id = 'bae83249-3395-47f9-9ebe-a2a5ecfdbbf6';

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V17.5.5__sinopharm.sql';
*/
