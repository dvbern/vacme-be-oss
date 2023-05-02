select data
into DUMPFILE '/tmp/Boostereinladung_${d}_${id}.pdf'
from ImpfdossierFile
where id = '${id}';