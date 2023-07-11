select data
into DUMPFILE '/tmp/Zertifikatsstornierung_${d}_${id}.pdf'
from ImpfdossierFile
where id = '${id}';