/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of
 *  the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.service.impfinformationen;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.ImpfungkontrolleTermin;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ImpfdossierBuilder {

	@NonNull
	private final Fragebogen fragebogen;

	@NonNull
	private final Impfdossier dossier;

	private List<Impfung> impfungen = new ArrayList<>();

	private ImpfdossierBuilder() {
		fragebogen = TestdataCreationUtil.createFragebogen();
		fragebogen.getRegistrierung().setRegistrierungsEingang(RegistrierungsEingang.ONLINE_REGISTRATION);
		fragebogen.getRegistrierung().setAbgleichElektronischerImpfausweis(true);
		fragebogen.getRegistrierung().setRegistrationTimestamp(LocalDateTime.now());
		fragebogen.getRegistrierung().setPrioritaet(Prioritaet.A);
		dossier = new Impfdossier();
		dossier.setRegistrierung(fragebogen.getRegistrierung());
		dossier.setKrankheitIdentifier(KrankheitIdentifier.COVID);
		dossier.setDossierStatus(ImpfdossierStatus.IMMUNISIERT);
		impfungen = new ArrayList<>();
	}

	public static ImpfdossierBuilder create() {
		// Reset all previous data
		return new ImpfdossierBuilder();
	}

	public ImpfdossierBuilder withRegistrierungsnummer(@NonNull String registrierungsnummer) {
		this.getRegistrierung().setRegistrierungsnummer(registrierungsnummer);
		return this;
	}

	public ImpfdossierBuilder forKrankheit(@NonNull KrankheitIdentifier krankheitIdentifier) {
		this.dossier.setKrankheitIdentifier(krankheitIdentifier);
		return this;
	}

	public ImpfdossierBuilder withImpfung(
		@NonNull LocalDate date,
		int impffolgeNr,
		@NonNull Impfstoff impfstoff,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		Impfung booster = TestdataCreationUtil.createBoosterImpfung(date, impfstoff);

		Impftermin
			boosterTermin = TestdataCreationUtil.createImpftermin(TestdataCreationUtil.createOrtDerImpfung(),
			booster.getTimestampImpfung().toLocalDate(), krankheitIdentifier
		);
		boosterTermin.setImpffolge(Impffolge.BOOSTER_IMPFUNG);
		boosterTermin.setGebuchtFromImpfterminRepo(true);// allowed for unittest
		booster.setTermin(boosterTermin);
		impfungen.add(booster);

		Impfdossiereintrag eintrag = new Impfdossiereintrag();
		eintrag.setImpfdossier(dossier);
		eintrag.setImpffolgeNr(impffolgeNr);
		ImpfungkontrolleTermin kontrolle = new ImpfungkontrolleTermin();
		kontrolle.setBemerkung("Bemerkung fuer Booster " + impffolgeNr);
		kontrolle.setTimestampKontrolle(LocalDateTime.now());
		eintrag.setImpfungkontrolleTermin(kontrolle);
		eintrag.setImpfterminFromImpfterminRepo(boosterTermin);
		dossier.getImpfdossierEintraege().add(eintrag);
		dossier.setTimestampZuletztAbgeschlossen(date.atStartOfDay());
		dossier.setVollstaendigerImpfschutzTyp(VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME);

		return this;
	}

	@NonNull
	public Fragebogen getFragebogen() {
		return fragebogen;
	}

	@NonNull
	public Registrierung getRegistrierung() {
		return fragebogen.getRegistrierung();
	}

	@NonNull
	public Impfdossier getDossier() {
		return dossier;
	}

	public List<Impfung> getImpfungen() {
		return impfungen;
	}
}
