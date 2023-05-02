(SELECT 'Freie Termine Impfung 1', 'Freie Termine Impfung N')
UNION
(SELECT (SELECT count(*)
		 FROM Impftermin T use index (IX_Impftermin_Impffolge_Gebucht)
			  INNER JOIN Impfslot S ON T.impfslot_id = S.id
			  INNER JOIN OrtDerImpfung ODI ON S.ortDerImpfung_id = ODI.id
		 WHERE S.von >= '${TOMORROW} 00:00' AND T.impffolge = 'ERSTE_IMPFUNG' AND gebucht IS FALSE AND
			   S.krankheitIdentifier = 'COVID') AS 'Freie Termine Impfung 1',
		(SELECT count(*)
		 FROM Impftermin T use index (IX_Impftermin_Impffolge_Gebucht)
			  INNER JOIN Impfslot S ON T.impfslot_id = S.id
			  INNER JOIN OrtDerImpfung ODI ON S.ortDerImpfung_id = ODI.id
		 WHERE S.von >= '${TOMORROW} 00:00' AND T.impffolge = 'BOOSTER_IMPFUNG' AND gebucht IS FALSE AND
			   S.krankheitIdentifier = 'COVID') AS 'Freie Termine Impfung N'
 into outfile '/tmp/${PREFIX_SLOTS}_${DATE}.csv' fields terminated by ',' optionally enclosed by '\"' escaped by '');