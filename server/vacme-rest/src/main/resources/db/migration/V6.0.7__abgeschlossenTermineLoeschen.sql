update Impftermin set gebucht = false where id in (
	select impftermin2_id from Registrierung
	where (registrierungStatus = 'ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG'
		or registrierungStatus = 'AUTOMATISCH_ABGESCHLOSSEN')
			and impftermin2_id is not null);

update Registrierung set impftermin2_id = null where (registrierungStatus = 'ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG'
	or registrierungStatus = 'AUTOMATISCH_ABGESCHLOSSEN')
		and impftermin2_id is not null;