/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.oss.vacme.repo;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.ImpfungkontrolleTermin;
import ch.dvbern.oss.vacme.entities.onboarding.Onboarding;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Personenkontrolle;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.smartdb.Db;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequestScoped
@Transactional
@Slf4j
public class AudHelperRepo {

	private final Db db;

	@Inject
	public AudHelperRepo(Db db) {
		this.db = db;
	}

	public void deleteBenutzerDataInAuditTables(@NonNull Benutzer benutzer) {
		deleteBenutzerBerechtigungAUD(benutzer);
		deleteBenutzerAUD(benutzer);
	}

	public void deleteFragebogenDataInAuditTables(@NonNull Fragebogen fragebogen) {
		deleteRegistrierungAUD(fragebogen.getRegistrierung());
		deleteFragebogenAUD(fragebogen);
	}

	private void deleteImpfungkontrolleTerminAUD(@NonNull ImpfungkontrolleTermin kontrolleTermin) {
		deleteFromAuditTable("ImpfungkontrolleTermin_AUD", "id", kontrolleTermin.getId().toString());
	}

	private void deletePersonenkontrolleAUD(@NonNull Personenkontrolle personenkontrolle) {
		deleteFromAuditTable("Personenkontrolle_AUD", "id", personenkontrolle.getId().toString());
	}

	private void deleteRegistrierungAUD(@NonNull Registrierung registrierung) {
		deleteFromAuditTable("Registrierung_AUD", "id", registrierung.getId().toString());
	}

	private void deleteFragebogenAUD(@NonNull Fragebogen fragebogen) {
		deleteFromAuditTable("Fragebogen_AUD", "id", fragebogen.getId().toString());
	}

	private void deleteBenutzerBerechtigungAUD(@NonNull Benutzer benutzer) {
		deleteFromAuditTable("BenutzerBerechtigung_AUD", "benutzer_id", benutzer.getId().toString());
	}

	private void deleteBenutzerAUD(@NonNull Benutzer benutzer) {
		deleteFromAuditTable("Benutzer_AUD", "id", benutzer.getId().toString());
	}

	public void deleteImpfdossierDataInAuditTables(@NonNull Impfdossier impfdossier) {
		final ImpfungkontrolleTermin kontrolle1 = impfdossier.getImpfungkontrolleTermin1();
		if (kontrolle1 != null) {
			deleteImpfungkontrolleTerminAUD(kontrolle1);
		}
		final ImpfungkontrolleTermin kontrolle2 = impfdossier.getImpfungkontrolleTermin2();
		if (kontrolle2 != null) {
			deleteImpfungkontrolleTerminAUD(kontrolle2);
		}
		if (impfdossier.getPersonenkontrolle() != null) {
			deletePersonenkontrolleAUD(impfdossier.getPersonenkontrolle());
		}

		deleteImpfdossierAUD(impfdossier);
	}

	private void deleteImpfdossierAUD(@NonNull Impfdossier impfdossier) {
		deleteFromAuditTable("Impfdossier_AUD", "id", impfdossier.getId().toString());
	}

	public void deleteOnboardingDataInAuditTables(@NonNull Onboarding onboarding) {
		deleteFromAuditTable("Onboarding_AUD", "id", onboarding.getId().toString());
	}


	/**
	 * Fuehrt ein native SQL Skript aus welches in den AUD Tabellen die zu einem attribut matchenden Daten loescht.
	 * ACHTUNG: Es muss sichergestellt sein dass keiner der 3 Parameter Daten enthaelt die der User beinflussen kann
	 * (sonst gefahr von SQL Injection)
	 * *
	 */
	private void deleteFromAuditTable(@NonNull String tablename, @NonNull String attributname, @NonNull String attributvalue) {
		String query = "delete from " + tablename + " where " + attributname + " = '" + attributvalue + '\'';
		final Query nativeQuery = db.getEntityManager().createNativeQuery(query);
		final int i = nativeQuery.executeUpdate();
		LOG.info("... {} elements deleted in table {}", i, tablename);
	}

	public void deleteExternesZertifikatInAuditTables(@NonNull ExternesZertifikat externesZertifikat) {
		deleteFromAuditTable("ExternesZertifikat_AUD", "id", externesZertifikat.getId().toString());
	}
}
