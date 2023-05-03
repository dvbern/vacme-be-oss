(SELECT 'Registrierungs_ID', 'Registriert_am', 'RegistrierungsEingang', 'Geschlecht', 'Immobil', 'Geburtsjahr', 'PLZ',
		'Abgleich_elektronischer_Impfausweis', 'Abgleich_Contact_tracing', 'Vollstaendiger_Impfschutz',
		'Chronische_Krankheiten', 'Lebensumstaende', 'Beruf', 'Imfgruppe', 'Verstorben', 'Immunisiert_bis',
		'Freigegeben_naechste_Impfung_ab', 'erlaubte_impfstoffe_fuer_booster')
UNION
(select Registrierung.id as Registrierungs_ID,
		date_format(Registrierung.registrationTimestamp, '%d.%m.%Y %T') as Registriert_am,
		Registrierung.registrierungsEingang as RegistrierungsEingang, Registrierung.geschlecht as Geschlecht,
		CASE WHEN Registrierung.immobil = 1 THEN 'TRUE' ELSE 'FALSE' END as Immobil,
		YEAR(Registrierung.geburtsdatum) as Geburtsjahr, Registrierung.plz as PLZ,
		CASE WHEN Registrierung.abgleichElektronischerImpfausweis = 1
				 THEN 'TRUE'
			 ELSE 'FALSE' END as Abgleich_elektronischer_Impfausweis,
		CASE WHEN Registrierung.contactTracing = 1 THEN 'TRUE' ELSE 'FALSE' END as Abgleich_Contact_tracing,
		CASE WHEN dossier.vollstaendigerImpfschutzTyp IS NOT NULL
				 THEN 'TRUE'
			 ELSE 'FALSE' END as Vollstaendiger_Impfschutz, Fragebogen.chronischeKrankheiten as Chronische_Krankheiten,
		Fragebogen.lebensumstaende as Lebensumstaende, Fragebogen.beruflicheTaetigkeit as Beruf,
		Registrierung.prioritaet as Imfgruppe, Registrierung.verstorben as Verstorben,
		date_format(impfschutz.immunisiertBis, '%d.%m.%Y') as Immunisiert_bis,
		date_format(impfschutz.freigegebenNaechsteImpfungAb, '%d.%m.%Y') as Freigegeben_naechste_Impfung_ab,
		impfschutz.erlaubteImpfstoffe as erlaubte_impfstoffe_fuer_booster
 from Registrierung
	  inner join Fragebogen on Fragebogen.registrierung_id = Registrierung.id
	  left outer join Impfdossier as dossier ON Registrierung.id = dossier.registrierung_id
	  left outer join Impfschutz as impfschutz on dossier.impfschutz_id = impfschutz.id
 WHERE dossier.registrierung_id is NULL or dossier.krankheitIdentifier = 'COVID')
UNION
(select Registrierung.id as Registrierungs_ID,
		date_format(Registrierung.registrationTimestamp, '%d.%m.%Y %T') as Registriert_am,
		Registrierung.registrierungsEingang as RegistrierungsEingang, Registrierung.geschlecht as Geschlecht,
		CASE WHEN Registrierung.immobil = 1 THEN 'TRUE' ELSE 'FALSE' END as Immobil,
		YEAR(Registrierung.geburtsdatum) as Geburtsjahr, Registrierung.plz as PLZ,
		CASE WHEN Registrierung.abgleichElektronischerImpfausweis = 1
				 THEN 'TRUE'
			 ELSE 'FALSE' END as Abgleich_elektronischer_Impfausweis,
		CASE WHEN Registrierung.contactTracing = 1 THEN 'TRUE' ELSE 'FALSE' END as Abgleich_Contact_tracing,
		CASE WHEN dossier.vollstaendigerImpfschutzTyp IS NOT NULL
				 THEN 'TRUE'
			 ELSE 'FALSE' END as Vollstaendiger_Impfschutz, Fragebogen.chronischeKrankheiten as Chronische_Krankheiten,
		Fragebogen.lebensumstaende as Lebensumstaende, Fragebogen.beruflicheTaetigkeit as Beruf,
		Registrierung.prioritaet as Imfgruppe, Registrierung.verstorben as Verstorben,
		date_format(impfschutz.immunisiertBis, '%d.%m.%Y') as Immunisiert_bis,
		date_format(impfschutz.freigegebenNaechsteImpfungAb, '%d.%m.%Y') as Freigegeben_naechste_Impfung_ab,
		impfschutz.erlaubteImpfstoffe as erlaubte_impfstoffe_fuer_booster
 into outfile '/tmp/${PREFIX_REGS}_${DATE}.csv' fields terminated by ',' optionally enclosed by '\"' LINES TERMINATED BY '\n'
 from Registrierung
	  inner join Fragebogen on Fragebogen.registrierung_id = Registrierung.id
	  left outer join Impfdossier as dossier ON Registrierung.id = dossier.registrierung_id
	  left outer join Impfschutz as impfschutz on dossier.impfschutz_id = impfschutz.id
 WHERE dossier.registrierung_id is NULL or dossier.krankheitIdentifier = 'COVID');