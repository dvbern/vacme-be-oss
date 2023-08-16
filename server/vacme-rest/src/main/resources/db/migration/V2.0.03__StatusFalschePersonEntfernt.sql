# Status zuruecksetzen von FALSCHE_PERSON_IMPFUNG_1
# Wenn Termine vorhanden, war es vorher GEBUCHT
update Registrierung set Registrierung.registrierungStatus = 'GEBUCHT'
where registrierungStatus = 'FALSCHE_PERSON_IMPFUNG_1' and Registrierung.impftermin1_id is not null;
# Wenn ODI, war es vorher ODI_GEWAEHLT
update Registrierung set Registrierung.registrierungStatus = 'ODI_GEWAEHLT'
where registrierungStatus = 'FALSCHE_PERSON_IMPFUNG_1' and Registrierung.gewuenschterOdi_id is not null;
# Alle die jetzt noch FALSCHE_PERSON_IMPFUNG_1 sind, koennen wir auf REGISTRIERT setzen
# Beim naechsten Einloggen werden sie gegebenenfalls wieder auf FREIGEGEBEN gesetzt
update Registrierung set Registrierung.registrierungStatus = 'REGISTRIERT'
where registrierungStatus = 'FALSCHE_PERSON_IMPFUNG_1';

# Status zuruecksetzen von FALSCHE_PERSON_IMPFUNG_2: Es war immer IMPFUNG_1_DURCHGEFUEHRT
update Registrierung set Registrierung.registrierungStatus = 'IMPFUNG_1_DURCHGEFUEHRT'
where registrierungStatus = 'FALSCHE_PERSON_IMPFUNG_2';