/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

# Externes Zertifikat in Nachhinein eingefuegt aber Termin 1 und Termin 2 vorhanden da nicht freigegeben
# Termine 1 freigeben
update Impftermin T set gebucht = false where id in (
	select R.impftermin1_id
	from Registrierung R
			 LEFT JOIN Impftermin t1 ON R.impftermin1_id = t1.id
			 LEFT JOIN Impfung i1 ON t1.id = i1.termin_id
	where R.vollstaendigerImpfschutzTyp in ('VOLLSTAENDIG_EXTERNESZERTIFIKAT', 'VOLLSTAENDIG_EXTERNESZERTIFIKAT_GENESEN')
	      and R.impftermin1_id is not null and i1.id is null
);
# Termine 2 freigeben
update Impftermin T set gebucht = false where id in (
	select R.impftermin2_id
	from Registrierung R
			 LEFT JOIN Impftermin t2 ON R.impftermin2_id = t2.id
			 LEFT JOIN Impfung i2 ON t2.id = i2.termin_id
	where R.vollstaendigerImpfschutzTyp in ('VOLLSTAENDIG_EXTERNESZERTIFIKAT', 'VOLLSTAENDIG_EXTERNESZERTIFIKAT_GENESEN')
	      and R.impftermin2_id is not null and i2.id is null
);
# Registrierung abhaengen
update Registrierung set impftermin1_id = null, impftermin2_id = null where id in (
	select R.id
	from Registrierung R
			 LEFT JOIN Impftermin t1 ON R.impftermin1_id = t1.id
			 LEFT JOIN Impfung i1 ON t1.id = i1.termin_id
			 LEFT JOIN Impftermin t2 ON R.impftermin2_id = t2.id
			 LEFT JOIN Impfung i2 ON t2.id = i2.termin_id
	where R.vollstaendigerImpfschutzTyp in ('VOLLSTAENDIG_EXTERNESZERTIFIKAT', 'VOLLSTAENDIG_EXTERNESZERTIFIKAT_GENESEN')
	      and (R.impftermin1_id is not null or R.impftermin2_id is not null) and i1.id is null and i2.id is null
);


# Noch nicht grundimmunisiert, aber Booster Termin vorhanden
# Termin freigeben
update Impftermin  set gebucht = false where id in (
	select E.impftermin_id
	from Registrierung R
		 inner join Impfdossier D ON R.id = D.registrierung_id
		 inner join Impfdossiereintrag E ON D.id = E.impfdossier_id
		 left join Impftermin T ON E.impftermin_id = T.id
		 left join Impfung I ON T.id = I.termin_id
	where R.registrierungStatus in ('REGISTRIERT', 'FREIGEGEBEN', 'ODI_GEWAEHLT', 'GEBUCHT', 'IMPFUNG_1_KONTROLLIERT', 'IMPFUNG_1_DURCHGEFUEHRT', 'IMPFUNG_2_KONTROLLIERT',
									'IMPFUNG_2_DURCHGEFUEHRT', 'ABGESCHLOSSEN') and E.impftermin_id is not null and I.id is null
);
# Impfdossiereintrag abhaengen
update Impfdossiereintrag set impftermin_id = null where id in (
	select E.id
	from Registrierung R
		 inner join Impfdossier D ON R.id = D.registrierung_id
		 inner join Impfdossiereintrag E ON D.id = E.impfdossier_id
		 left join Impftermin T ON E.impftermin_id = T.id
		 left join Impfung I ON T.id = I.termin_id
	where R.registrierungStatus in ('REGISTRIERT', 'FREIGEGEBEN', 'ODI_GEWAEHLT', 'GEBUCHT', 'IMPFUNG_1_KONTROLLIERT', 'IMPFUNG_1_DURCHGEFUEHRT', 'IMPFUNG_2_KONTROLLIERT',
									'IMPFUNG_2_DURCHGEFUEHRT', 'ABGESCHLOSSEN') and E.impftermin_id is not null and I.id is null
);