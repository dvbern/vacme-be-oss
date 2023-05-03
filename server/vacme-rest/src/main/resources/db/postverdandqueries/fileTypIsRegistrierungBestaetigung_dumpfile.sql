select data
into DUMPFILE '/tmp/Registrierungsbestaetigung_${d}.pdf'
from Registrierung inner join RegistrierungFile RF
ON Registrierung.id = RF.registrierung_id
where RF.fileTyp like 'REGISTRIERUNG_BESTAETIGUNG' AND registrierungsnummer = '${d}';