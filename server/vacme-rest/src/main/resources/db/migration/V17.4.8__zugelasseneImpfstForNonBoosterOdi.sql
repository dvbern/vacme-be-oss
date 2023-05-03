INSERT INTO OrtDerImpfung_Impfstoff  (impfstoff_id, ortDerImpfung_id)
SELECT I.id, ODI.id
FROM OrtDerImpfung ODI
	 CROSS JOIN Impfstoff I
WHERE zulassungsStatus = 'ZUGELASSEN' AND ODI.booster IS FALSE;