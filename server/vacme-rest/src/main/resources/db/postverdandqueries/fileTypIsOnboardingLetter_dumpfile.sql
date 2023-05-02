select data
into DUMPFILE '/tmp/Onboarding_${d}_${id}.pdf'
from RegistrierungFile
where id = '${id}';