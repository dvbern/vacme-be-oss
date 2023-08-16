delete ignore from RegistrierungQueue where typ = 'ZERTIFIKAT_COUNTER_RECALCULATION';
delete ignore from RegistrierungQueue where typ = 'ZERTIFIKAT_COUNTER_RECALCULATION_FINISHED';

delete ignore from ApplicationProperty where name = 'VACME_ZERTIFIKAT_COUNTER_RECALCULATION_DISABLED';