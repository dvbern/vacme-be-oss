update Registrierung R2 set R2.gewuenschterOdi_id = (
	SELECT I2.ortDerImpfung_id
	FROM Registrierung R
			 INNER JOIN Impftermin I ON R.impftermin1_id = I.id
			 INNER JOIN Impfslot I2 ON I.impfslot_id = I2.id where R.id = R2.id
) where gewuenschterOdi_id IS NULL and registrierungStatus = 'IMPFUNG_1_DURCHGEFUEHRT';