/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.jax;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * DTO fuer den ODI-Impfungen Report
 */
@Getter
@Setter
public class OdiImpfungenDataRow {

	private @Nullable String registrierungsnummer;
	private @Nullable String vorname;
	private @Nullable String name;
	private @Nullable LocalDate geburtsdatum;
	private @Nullable String kkKartenNr;

	private @Nullable String odiId;
	private @Nullable String odiName;
	private @Nullable String odiGln;
	private @Nullable String odiTyp;

	private @Nullable String verantwortlichePersonName;
	private @Nullable String verantwortlichePersonGln;

	private @Nullable LocalDateTime termin;
	private @Nullable LocalDateTime impfungDatum;
	private @Nullable String krankheit;
	private @Nullable String impfstoffName;
	private @Nullable String impfstoffId;
	private @Nullable Boolean impfungExtern;
	private @Nullable Boolean grundimmunisierung;
	private @Nullable Integer impffolgeNr;
	private @Nullable String lot;
	private @Nullable Boolean selbstzahlende;


	@QueryProjection
	public OdiImpfungenDataRow(
		@NonNull Impfslot slot,
		@NonNull OrtDerImpfung odi,
		@Nullable Impfung impfung,
		@NonNull Registrierung registrierung,
		@NonNull Integer impffolgeNr
	) {
		this.registrierungsnummer = registrierung.getRegistrierungsnummer();
		this.vorname = registrierung.getVorname();
		this.name = registrierung.getName();
		this.geburtsdatum = registrierung.getGeburtsdatum();
		this.kkKartenNr = registrierung.getKrankenkasseKartenNr();

		this.odiId = odi.getId().toString();
		this.odiName = odi.getName();
		this.odiGln = odi.getGlnNummer();
		this.odiTyp = odi.getTyp().name();

		this.termin = slot.getZeitfenster().getVon();
		this.krankheit = slot.getKrankheitIdentifier().name();

		if (impfung != null) {
			this.verantwortlichePersonName = impfung.getBenutzerVerantwortlicher().getDisplayName();
			this.verantwortlichePersonGln = impfung.getBenutzerVerantwortlicher().getGlnNummer();

			this.impfungDatum = impfung.getTimestampImpfung();
			this.impfstoffName = impfung.getImpfstoff().getName();
			this.impfstoffId = impfung.getImpfstoff().getId().toString();
			this.impfungExtern = impfung.isExtern();
			this.grundimmunisierung = impfung.isGrundimmunisierung();
			this.selbstzahlende = impfung.isSelbstzahlende();
			this.lot = impfung.getLot();
		}

		this.impffolgeNr = impffolgeNr;
	}
}
