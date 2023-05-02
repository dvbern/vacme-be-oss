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

package ch.dvbern.oss.vacme.jax.korrektur;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Consumer;

import javax.validation.constraints.NotEmpty;

import ch.dvbern.oss.vacme.entities.embeddables.Adresse;
import ch.dvbern.oss.vacme.entities.impfen.AuslandArt;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Krankenkasse;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Geschlecht;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.jax.base.AdresseJax;
import ch.dvbern.oss.vacme.jax.migration.RegistrierungMigrationJax;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class PersonendatenKorrekturJax {

	@NonNull
	private String registrierungsnummer;

	@NonNull
	private Geschlecht geschlecht;

	@NonNull
	private String name;

	@NonNull
	private String vorname;

	@NonNull
	private LocalDate geburtsdatum;

	@Nullable
	private Boolean verstorben;

	@NonNull
	private AdresseJax adresse;

	private boolean abgleichElektronischerImpfausweis = false;

	private boolean contactTracing = false;

	@Nullable
	private String mail;

	@Nullable
	private String telefon;

	@NonNull
	private Krankenkasse krankenkasse;

	@NonNull
	private String krankenkasseKartenNr;

	@Nullable
	private AuslandArt auslandArt;

	@Nullable
	private String identifikationsnummer;

	private Boolean schutzstatus;

	private Boolean keinKontakt;

	@Nullable
	private Boolean immunsupprimiert;

	@JsonIgnore
	public static PersonendatenKorrekturJax from(@NonNull Fragebogen fragebogen) {
		Registrierung registrierung = fragebogen.getRegistrierung();
		return new PersonendatenKorrekturJax(
			registrierung.getRegistrierungsnummer(),
			registrierung.getGeschlecht(),
			registrierung.getName(),
			registrierung.getVorname(),
			registrierung.getGeburtsdatum(),
			registrierung.getVerstorben(),
			AdresseJax.from(registrierung.getAdresse()),
			registrierung.isAbgleichElektronischerImpfausweis(),
			registrierung.getContactTracing() != null && registrierung.getContactTracing(),
			registrierung.getMail(),
			registrierung.getTelefon(),
			registrierung.getKrankenkasse(),
			registrierung.getKrankenkasseKartenNr(),
			registrierung.getAuslandArt(),
 			registrierung.getIdentifikationsnummer(),
			registrierung.getSchutzstatus(),
			registrierung.getKeinKontakt(),
			fragebogen.getImmunsupprimiert()
		);
	}

	@JsonIgnore
	public static PersonendatenKorrekturJax from(
		@NotEmpty @NonNull String registrierungsnummer,
		@NonNull RegistrierungMigrationJax migrationJax
	) {
		AdresseJax adresseJax = AdresseJax.from(migrationJax);
		boolean contactTracing = false; // can not be set in RegistrierungMigrationJax, assume false
		String identifikationsnummer = null; // can not be set in RegistrierungMigrationJax, assume null
		boolean schutzstatus = false; // can not be set in RegistrierungMigrationJax, assume false
		boolean keinKontakt = false; // can not be set in RegistrierungMigrationJax, assume false
		Boolean immunsupprimiert = null; // can not be set in RegistrierungMigrationJax, assume null
		return new PersonendatenKorrekturJax(
			registrierungsnummer,
			migrationJax.getGeschlecht(),
			migrationJax.getName(),
			migrationJax.getVorname(),
			migrationJax.getGeburtsdatum(),
			migrationJax.getVerstorben(),
			adresseJax,
			migrationJax.isAbgleichElektronischerImpfausweis(),
			contactTracing,
			migrationJax.getEmail(),
			migrationJax.getTelefon(),
			migrationJax.getKrankenkasse(),
			migrationJax.getKrankenkassennummer(),
			migrationJax.getAuslandArt(),
			identifikationsnummer,
			schutzstatus,
			keinKontakt,
			immunsupprimiert
		);
	}

	@JsonIgnore
	public Consumer<Fragebogen> getUpdateEntityConsumer() {
		return fragebogen -> {
			Registrierung registrierung = fragebogen.getRegistrierung();
			registrierung.setGeschlecht(geschlecht);
			registrierung.setName(name);
			registrierung.setVorname(vorname);
			registrierung.setGeburtsdatum(geburtsdatum);
			registrierung.setVerstorben(verstorben);
			final Consumer<Adresse> updateEntityConsumer = adresse.getUpdateEntityConsumer();
			updateEntityConsumer.accept(registrierung.getAdresse());
			registrierung.setAbgleichElektronischerImpfausweis(abgleichElektronischerImpfausweis);
			registrierung.setContactTracing(contactTracing);
			// Mail und Telefon koennen bei ONLINE nicht geaendert werden
			// Dies waere missverstaendlich, bzw. muesste dann wenn schon auch im KeyCloak angepasst werden
			if (registrierung.getRegistrierungsEingang() != RegistrierungsEingang.ONLINE_REGISTRATION) {
				registrierung.setMail(mail);
				registrierung.setTelefon(telefon);
			}
			registrierung.setKrankenkasse(krankenkasse);
			registrierung.setKrankenkasseKartenNrAndArchive(krankenkasseKartenNr);
			registrierung.setAuslandArt(auslandArt);
			registrierung.setSchutzstatus(schutzstatus);
			registrierung.setKeinKontakt(keinKontakt);
			registrierung.setIdentifikationsnummer(identifikationsnummer);
			fragebogen.setImmunsupprimiert(immunsupprimiert);
		};
	}

	public boolean needsNewZertifikat(@NonNull Impfdossier dataBeforeCorrection, boolean didInfoRelevantToZertifikatChange) {
		if (dataBeforeCorrection.getKrankheitIdentifier().isSupportsZertifikat()
			&& dataBeforeCorrection.abgeschlossenMitVollstaendigemImpfschutz()) {
			return didInfoRelevantToZertifikatChange;
		}
		return false;
	}

	public boolean didInfoRelevantToZertifikatChange(@NotNull Registrierung registrierung) {
		// Im Zertifikat sind Name, Vorname, Geburtsdatum
		final boolean personalienSame = isPersonalienSame(registrierung);
		// Fuer Online Faelle wird die Adresse nicht beachtet
		boolean adresseSameOrNotRelevant = isAdresseSameOrNotRelevant(registrierung);

		return !personalienSame || !adresseSameOrNotRelevant;
	}

	private boolean isAdresseSameOrNotRelevant(@NotNull Registrierung dataBeforeCorrection) {
		boolean adresseSameOrNotRelevant = true;
		// Fuer Online Faelle wird die Adresse nicht beachtet
		if (RegistrierungsEingang.ONLINE_REGISTRATION != dataBeforeCorrection.getRegistrierungsEingang()) {
			// Wir vergleichen aber auch die Adresse, da ja evtl. deswegen das letzte Zertifikat nicht
			// angekommen ist. Damit nicht zwei identische Zertifikate vorhanden sind, wird das alte storniert
			final Adresse adresseBeforeCorrection = dataBeforeCorrection.getAdresse();
			adresseSameOrNotRelevant = adresseBeforeCorrection.getAdresse1().equals(this.adresse.getAdresse1())
				&& sameOrBothEmpty(adresseBeforeCorrection.getAdresse2(), this.adresse.getAdresse2()) // treat null and empty as same
				&& adresseBeforeCorrection.getPlz().equals(this.adresse.getPlz())
				&& adresseBeforeCorrection.getOrt().equals(this.adresse.getOrt());
		}
		return adresseSameOrNotRelevant;
	}

	private boolean sameOrBothEmpty(@Nullable String first,  @Nullable String second) {
		return StringUtils.isEmpty(first) && StringUtils.isEmpty(second) || Objects.equals(first, second);
	}

	private boolean isPersonalienSame(@NonNull Registrierung dataBeforeCorrection) {
		return dataBeforeCorrection.getName().equals(name)
			&& dataBeforeCorrection.getVorname().equals(vorname)
			&& dataBeforeCorrection.getGeburtsdatum().equals(geburtsdatum);
	}

	public boolean needToRevoke(@SuppressWarnings("unused") @NonNull Registrierung dataBeforeCorrection) {
		// Wir revozieren nur noch bei Aenderung von Datum/Impfstoff/Anzahl
		return false;
	}
}
