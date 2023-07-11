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

package ch.dvbern.oss.vacme.jax.registration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsart;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsort;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsseite;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ImpfdokumentationJax {

	@NonNull
	@NotNull
	@Schema(required = true)
	private KrankheitIdentifier krankheitIdentifier;

	private boolean nachtraeglicheErfassung;

	@Nullable
	private LocalDate datumFallsNachtraeglich;

	@NonNull
	@NotNull
	@Schema(required = true)
	private String registrierungsnummer;

	@NonNull
	@Schema(required = true)
	private UUID verantwortlicherBenutzerId;

	@NonNull
	@Schema(required = true)
	private UUID durchfuehrenderBenutzerId;

	@NonNull
	@NotNull
	@Schema(required = true)
	private ImpfstoffJax impfstoff;

	@NonNull
	@Schema(required = true)
	private String lot;

	@Nullable // must be Boolean not boolean because it can be Null
	private Boolean keineBesonderenUmstaende = false;

	@Schema(required = true)
	private boolean fieber = false;

	@Schema(required = true)
	private boolean einwilligung = false;

	@Schema(required = true)
	private boolean neueKrankheit = false;

	@NonNull
	@Schema(required = true)
	private Verarbreichungsart verarbreichungsart;

	@NonNull
	@Schema(required = true)
	private Verarbreichungsort verarbreichungsort;

	@NonNull
	@Schema(required = true)
	private Verarbreichungsseite verarbreichungsseite;

	@NonNull
	@Schema(required = true)
	private BigDecimal menge;

	private String bemerkung;

	@Schema(required = true)
	private boolean extern = false;

	@Schema(required = true)
	private boolean grundimmunisierung;

	@Nullable // must be Boolean not boolean because it can be Null
	private Boolean schwanger;

	@Schema(required = true)
	private boolean selbstzahlende;

	@Nullable // must be Boolean not boolean because it can be Null
	private Boolean immunsupprimiert;

	@Nullable // must be Boolean not boolean because it can be Null
	private Boolean risikoreichesSexualleben;

	@Nullable // must be Boolean not boolean because it can be Null
	private Boolean impfungAusBeruflichenGruenden;

	@Nullable // must be Boolean not boolean because it can be Null
	private Boolean kontaktMitPersonAusRisikogruppe;

	@Schema(required = true)
	private boolean schnellschema;

	@Nullable // must be Boolean not boolean because it can be Null
	private Boolean zeckenstich;

	@NonNull
	public Impfung toEntity(
		@NonNull Benutzer benutzerVerantwortlich,
		@NonNull Benutzer benutzerDurchfuehrend,
		@NonNull Impfstoff impfstoff
	) {
		Impfung impfung = new Impfung();
		impfung.setBenutzerVerantwortlicher(benutzerVerantwortlich);
		impfung.setBenutzerDurchfuehrend(benutzerDurchfuehrend);
		impfung.setImpfstoff(impfstoff);
		impfung.setLot(lot);
		impfung.setFieber(fieber);
		impfung.setKeineBesonderenUmstaende(keineBesonderenUmstaende);
		impfung.setSchwanger(schwanger);
		impfung.setEinwilligung(einwilligung);
		impfung.setBemerkung(bemerkung);
		impfung.setNeueKrankheit(neueKrankheit);
		impfung.setVerarbreichungsart(verarbreichungsart);
		impfung.setVerarbreichungsort(verarbreichungsort);
		impfung.setVerarbreichungsseite(verarbreichungsseite);
		impfung.setMenge(menge);
		impfung.setExtern(extern);
		impfung.setGrundimmunisierung(grundimmunisierung);
		impfung.setSelbstzahlende(selbstzahlende);
		impfung.setRisikoreichesSexualleben(risikoreichesSexualleben);
		impfung.setImpfungAusBeruflichenGruenden(impfungAusBeruflichenGruenden);
		impfung.setKontaktMitPersonAusRisikogruppe(kontaktMitPersonAusRisikogruppe);
		impfung.setSchnellschemaGesetztFuerImpfung(schnellschema);
		impfung.setZeckenstich(zeckenstich);

		return impfung;
	}
}
