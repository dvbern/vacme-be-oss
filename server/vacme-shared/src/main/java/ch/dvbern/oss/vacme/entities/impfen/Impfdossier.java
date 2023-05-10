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

package ch.dvbern.oss.vacme.entities.impfen;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.HasImpfdossierIdIdAndKrankheitsidentifier;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.embeddables.Buchung;
import ch.dvbern.oss.vacme.entities.embeddables.ZweiteGrundimmunisierungVerzichtet;
import ch.dvbern.oss.vacme.entities.registration.Personenkontrolle;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.entities.util.ImpfdossierEntityListener;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.wrapper.VacmeDecoratorFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

/**
 * This class is used to store information about Impfungen and Kontrollen for a specific disease
 */
@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
@Table(
	indexes = {
		@Index(name = "IX_Impfdossier_registrierung_krankheit", columnList = "registrierung_id, krankheitIdentifier, id"),
		@Index(name = "IX_Impfdossier_dossierstatus_krankheit_reg", columnList = "dossierStatus, krankheitIdentifier, registrierung_id, id"),
		@Index(name = "IX_Impfdossier_krankheit", columnList = "krankheitIdentifier, id"),
		@Index(name = "IX_Impfdossier_impfschutz_krankheit", columnList = "impfschutz_id, krankheitIdentifier, id"),
		@Index(name = "IX_Impfdossier_registrierung_impfschutz_krankheit", columnList = "registrierung_id, impfschutz_id, krankheitIdentifier, id"),
		@Index(name = "IX_Impfdossier_impftermin1_krankheit_registrierung", columnList = "impftermin1_id, registrierung_id, krankheitIdentifier, id"),
		@Index(name = "IX_Impfdossier_impftermin2_krankheit_registrierung", columnList = "impftermin2_id, registrierung_id, krankheitIdentifier, id"),
	},
	uniqueConstraints = {
		@UniqueConstraint(name = "UC_Impfdossier_registrierung_krankheit", columnNames = {"registrierung_id", "krankheitIdentifier"}),
		@UniqueConstraint(name = "UC_Impfdossier_impfschutz", columnNames = "impfschutz_id"),
		@UniqueConstraint(name = "UC_Impfdossier_impftermin1", columnNames = "impftermin1_id"),
		@UniqueConstraint(name = "UC_Impfdossier_impftermin2", columnNames = "impftermin2_id"),
		@UniqueConstraint(name = "UC_Impfdossier_personenkontrolle", columnNames = "personenkontrolle_id")
	}
)
@Slf4j
@EntityListeners(ImpfdossierEntityListener.class)
public class Impfdossier extends AbstractUUIDEntity<Impfdossier> implements HasImpfdossierIdIdAndKrankheitsidentifier {

	private static final long serialVersionUID = 2255721458819528453L;

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private KrankheitIdentifier krankheitIdentifier;

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private ImpfdossierStatus dossierStatus;

	@NotNull
	@NonNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Impfdossier_registrierung"), nullable = false, updatable = false)
	private Registrierung registrierung;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Impfdossier_impfschutz"), nullable = true, updatable = true)
	private Impfschutz impfschutz;

	@NonNull
	@OneToMany(mappedBy = "impfdossier", fetch = FetchType.LAZY, cascade = { CascadeType.ALL}, orphanRemoval = true)
	private List<Impfdossiereintrag> impfdossierEintraege = new ArrayList<>();

	@NonNull
	@OneToMany(mappedBy = "impfdossier", fetch = FetchType.LAZY, cascade = { CascadeType.ALL}, orphanRemoval = true)
	private List<Erkrankung> erkrankungen = new ArrayList<>();

	/*
	Verwendung: a) fuer die Batchjob-Reihenfolge fuer Immunisiert und BoosterRule
	            b) bei automatisch abgeschlossenen wird auf geimpft angezeigt, wann automatisch abgeschlossen wurde
	Bedeutung: wann abgeschlossen wurde (mit/ohne Grundimmunisierung), wann externes Zertifikat erstellt wurde oder wann geboostert wurde
	 */
	@Nullable
	@Column(nullable = true)
	private LocalDateTime timestampZuletztAbgeschlossen;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private VollstaendigerImpfschutzTyp vollstaendigerImpfschutzTyp; // vollstaendige Grundimmunisierung: in VacMe oder ExternesZertifikat, mit/ohne genesen

	@Valid
	@Embedded
	@NotNull
	@NonNull
	private Buchung buchung = new Buchung();

	@Valid
	@Embedded
	@NotNull
	@NonNull
	private ZweiteGrundimmunisierungVerzichtet zweiteGrundimmunisierungVerzichtet = new ZweiteGrundimmunisierungVerzichtet();

	@NotNull
	@Column(nullable = false)
	private boolean leistungerbringerAgbConfirmationNeeded = true;

	@Nullable
	@OneToOne(optional = true, cascade = {CascadeType.ALL})
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_impfdossier_personenkontrolle_id"), nullable = true)
	private Personenkontrolle personenkontrolle;

	@NotNull
	@Column(nullable = false)
	private boolean schnellschema = false;

	public static ID<Impfdossier> toId(UUID id) {
		return new ID<>(id, Impfdossier.class);
	}

	@NonNull
	public Optional<Impfdossiereintrag> findEintragForImpffolgeNr(@NonNull Integer impffolgeNr) {
		return impfdossierEintraege.stream()
			.filter(eintrag -> eintrag.getImpffolgeNr().equals(impffolgeNr))
			.findFirst();
	}

	@NonNull
	public Impfdossiereintrag getEintragForImpffolgeNr(@NonNull Integer impffolgeNr) {
		return findEintragForImpffolgeNr(impffolgeNr)
			.orElseThrow(() -> AppValidationMessage.ILLEGAL_STATE.create(Impfdossiereintrag.class.getSimpleName()
				+" nicht gefunden fuer Impffolge " + impffolgeNr));

	}

	@NonNull
	public List<Impfdossiereintrag> getOrderedEintraege() {
		Collections.sort(impfdossierEintraege);
		return impfdossierEintraege;
	}
	@NonNull
	public List<Erkrankung> getErkrankungenSorted() {
		Collections.sort(erkrankungen);
		return erkrankungen;
	}

	@JsonIgnore
	public void setStatusToAbgeschlossen(
		@NonNull ImpfinformationDto infos,
		@NotNull Impfung relevanteImpfung
	) {
		Validate.isTrue(this.equals(infos.getImpfdossier()), "Impfdossier fuer Statuschange muss Impfdossier in ImpfinformationDto entsprechen");
		Registrierung reg = infos.getRegistrierung();
		this.setDossierStatus(ImpfdossierStatus.ABGESCHLOSSEN);
		this.setTimestampZuletztAbgeschlossen(LocalDateTime.now());
		this.setVollstaendigerImpfschutzTyp(VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME);
		this.getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetGrund(null);
		this.getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetZeit(null);

		// Erst ganz am Schluss koennen wir ermitteln, ob ein Zertifikat erlaubt ist, sonst berechnen wir aufgrund
		// falscher Daten denn manche fuer die Zertifikatsentscheidung relevanten Felder werden erst gerade gesetzt!
		reg.setGenerateZertifikatTrueIfAllowed(infos, relevanteImpfung);
	}

	@JsonIgnore
	public void setStatusToAbgeschlossenWithExtZertifikatPlusVacme(
		@NonNull ImpfinformationDto infos,
		@NotNull Impfung relevanteImpfung
	) {
		Validate.isTrue(this.equals(infos.getImpfdossier()), "Impfdossier fuer Statuschange muss Impfdossier in ImpfinformationDto entsprechen");
		Registrierung reg = infos.getRegistrierung();
		this.setDossierStatus(ImpfdossierStatus.ABGESCHLOSSEN);
		this.setTimestampZuletztAbgeschlossen(LocalDateTime.now());
		this.setVollstaendigerImpfschutzTyp((infos.getExternesZertifikat() != null && infos.getExternesZertifikat().isGenesen())
			? VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXT_GENESEN_PLUS_VACME
			: VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXT_PLUS_VACME);
		this.getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetGrund(null);
		this.getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetZeit(null);

		// Erst ganz am Schluss koennen wir ermitteln, ob ein Zertifikat erlaubt ist, sonst berechnen wir aufgrund
		// falscher Daten denn manche fuer die Zertifikatsentscheidung relevanten Felder werden erst gerade gesetzt!
		reg.setGenerateZertifikatTrueIfAllowed(infos, relevanteImpfung);
	}

	/**
	 * handhabt Abschluss einer Reg nach 1. Impfung weil sie mit einem Impfstoff impft der nur 1 Dosis braucht oder
	 * weil sie ein externes Zertifikat hat welches belegt dass sie nur noch eine 1 Impfung braucht
	 */
	@JsonIgnore
	public void setStatusToAbgeschlossenAfterErstimpfung(
		@NonNull ImpfinformationDto infos,
		@NotNull Impfung erstimpfung
	) {
		Validate.isTrue(this.equals(infos.getImpfdossier()), "Impfdossier fuer Statuschange muss Impfdossier in ImpfinformationDto entsprechen");
		if (erstimpfung.getImpfstoff().getAnzahlDosenBenoetigt() == 1) {
			this.setStatusToAbgeschlossen(infos, erstimpfung); // vollstaendige grundimmunisierung in VacMe, weil Impfstoff nur 1 Impfung braucht
		} else {
			this.setStatusToAbgeschlossenWithExtZertifikatPlusVacme(infos, erstimpfung); // externe Impfungen plus 1 VacMe-Impfung
		}
	}

	@JsonIgnore
	public void setStatusToAutomatischAbgeschlossen(@NonNull ImpfinformationDto infos) {
		Validate.isTrue(this.equals(infos.getImpfdossier()), "Impfdossier fuer Statuschange muss Impfdossier in ImpfinformationDto entsprechen");
		this.setDossierStatus(ImpfdossierStatus.AUTOMATISCH_ABGESCHLOSSEN);
		this.setTimestampZuletztAbgeschlossen(LocalDateTime.now());
		this.setVollstaendigerImpfschutzTyp(null);
		this.getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetGrund(null);
		this.getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetZeit(null);
	}

	@JsonIgnore
	public void setStatusToNichtAbgeschlossenStatus(@NonNull ImpfinformationDto infos, @NonNull ImpfdossierStatus status, @Nullable Impfung ersteImpfung) {
		Validate.isTrue(this.equals(infos.getImpfdossier()), "Impfdossier fuer Statuschange muss Impfdossier in ImpfinformationDto entsprechen");
		validateStatusAllowedForKrankheit(infos.getKrankheitIdentifier(), status);
		// Darf nur fuer VOR-Booster-Status verwendet werden. Darum ist hier nur die Impfung 1 relevant
		Validate.isTrue(!ImpfdossierStatus.getMindestensGrundimmunisiertOrAbgeschlossen().contains(status));
		this.setDossierStatus(status);
		this.setTimestampZuletztAbgeschlossen(null);
		this.setVollstaendigerImpfschutzTyp(null);
		this.getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetGrund(null);
		this.getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetZeit(null);
		this.getZweiteGrundimmunisierungVerzichtet().setGenesen(false);
		this.getZweiteGrundimmunisierungVerzichtet().setPositivGetestetDatum(null);
		if (ersteImpfung != null) {
			ersteImpfung.setGenerateZertifikat(false);
		}
	}

	private void validateStatusAllowedForKrankheit(KrankheitIdentifier krankheit, ImpfdossierStatus status) {
		if (!krankheit.isSupportsImpffolgenEinsUndZwei()) {
			Validate.isTrue(!ImpfdossierStatus.getImpfung1Or2Exclusive().contains(status),
				String.format(
					"Status %s ist fuer  %s nicht erlaubt da diese Krankheit keine Impffolgen 1/2 unterstuetzt",
					status,
					krankheit));
		}
	}

	private void validatePositivGetestetDatum(@Nullable LocalDate positivGetestetDatum) {
		if (positivGetestetDatum != null) {
			// Vergangenheit
			if (positivGetestetDatum.isAfter(LocalDate.now())) {
				throw AppValidationMessage.POSITIVER_TEST_DATUM_INVALID.create(positivGetestetDatum);
			}
			// nicht vor 1.1.2020
			if (positivGetestetDatum.isBefore(Constants.MIN_DATE_FOR_PCR_TEST)) {
				throw AppValidationMessage.POSITIVER_TEST_DATUM_INVALID.create(positivGetestetDatum);
			}
		}
	}

	@JsonIgnore
	public void setStatusToAbgeschlossenOhneZweiteImpfung(
		@NonNull ImpfinformationDto infos,
		boolean vollstaendigerImpfschutz,
		@Nullable String begruendung,
		@Nullable LocalDate positivGetestetDatum
	) {
		Validate.isTrue(this.equals(infos.getImpfdossier()), "Impfdossier fuer Statuschange muss Impfdossier in ImpfinformationDto entsprechen");
		Registrierung reg = infos.getRegistrierung();

		LocalDateTime abgeschlossenTime = LocalDateTime.now();
		Impfung impfung1 = infos.getImpfung1();
		Objects.requireNonNull(impfung1);

		this.setDossierStatus(ImpfdossierStatus.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG);
		this.setTimestampZuletztAbgeschlossen(abgeschlossenTime);
		if (vollstaendigerImpfschutz) {
			// Vollstaendiger Impfschutz ohne zweite Impfung kann nur im Fall von Corona vorkommen, daher:
			// Hier kommen wir nur bei neu erfassten Corona-Infektionen hin, wir haben also immer ein PCR-Datum
			Objects.requireNonNull(positivGetestetDatum);
			this.getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetGrund(null);
			this.getZweiteGrundimmunisierungVerzichtet().setGenesen(true);
			validatePositivGetestetDatum(positivGetestetDatum);
			this.getZweiteGrundimmunisierungVerzichtet().setPositivGetestetDatum(positivGetestetDatum);
			this.setVollstaendigerImpfschutzTyp(VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME_GENESEN);
		} else {
			impfung1.setGenerateZertifikat(false);
			this.getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetGrund(begruendung);
			this.getZweiteGrundimmunisierungVerzichtet().setGenesen(false);
			this.getZweiteGrundimmunisierungVerzichtet().setPositivGetestetDatum(null);
			this.setVollstaendigerImpfschutzTyp(null);
		}
		this.getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetZeit(abgeschlossenTime);

		// Erst ganz am Schluss koennen wir ermitteln, ob ein Zertifikat erlaubt ist,  sonst berechnen wir aufgrund
		// falscher Daten denn manche fuer die Zertifikatsentscheidung relevanten Felder werden erst gerade gesetzt!
		reg.setGenerateZertifikatTrueIfAllowed(infos, impfung1);
	}

	@JsonIgnore
	public void setStatusToImmunisiertAfterBooster(
		@NonNull ImpfinformationDto infos,
		@NonNull Impfung relevanteImpfung,
		@Nullable Boolean immunsupprimiert
	) {
		VacmeDecoratorFactory.getDecorator(infos.getKrankheitIdentifier())
			.setStatusToImmunisiertAfterBooster(infos, relevanteImpfung, immunsupprimiert);
	}

	public boolean abgeschlossenMitCorona() {
		return this.getZweiteGrundimmunisierungVerzichtet().isGenesen()
			&& abgeschlossenMitVollstaendigemImpfschutz();
	}

	public boolean abgeschlossenMitVollstaendigemImpfschutz() {
		return this.getVollstaendigerImpfschutzTyp() != null;
	}

	public boolean verzichtetOhneVollstaendigemImpfschutz() {
		return !abgeschlossenMitVollstaendigemImpfschutz() &&
			this.getZweiteGrundimmunisierungVerzichtet().getZweiteImpfungVerzichtetZeit() != null;
	}

	/**
	 * setzt den vollstaendigenImpfschutzTyp und das vollstaendigerImpfschutz Flag
	 *
	 * @param vollstaendigerImpfschutzTyp impfschutz welcher gesetzt wird. Wenn ein Wert vorhanden ist wird auch
	 * vollsteandigerImpfschutz gesetzt. Wenn null wird vollstaendigerImpfschutz auf null gesetzt
	 */
	public void setVollstaendigerImpfschutzTyp(@Nullable VollstaendigerImpfschutzTyp vollstaendigerImpfschutzTyp) {
		// validieren, dass vollstaendigerImpfschutzTyp nicht direkt wechselt (man sollte immer ueber null gehen)
		if (vollstaendigerImpfschutzTyp != null
			&& this.vollstaendigerImpfschutzTyp != null
			&& vollstaendigerImpfschutzTyp != this.vollstaendigerImpfschutzTyp) {
			throw AppValidationMessage.ILLEGAL_STATE.create(String.format("vollstaendigerImpfschutzTyp changed from %s"
				+ " to %s. This is illegal", this.vollstaendigerImpfschutzTyp, vollstaendigerImpfschutzTyp));
		}
		this.vollstaendigerImpfschutzTyp = vollstaendigerImpfschutzTyp;
	}

	@NonNull
	public Personenkontrolle getOrCreatePersonenkontrolle() {
		if (personenkontrolle == null) {
			personenkontrolle = new Personenkontrolle();
		}
		return personenkontrolle;
	}

	@Nullable
	public ImpfungkontrolleTermin getImpfungkontrolleTermin1() {
		if (personenkontrolle != null) {
			return personenkontrolle.getKontrolleTermin1();
		}
		return null;
	}

	@Nullable
	public ImpfungkontrolleTermin getImpfungkontrolleTermin2() {
		if (personenkontrolle != null) {
			return personenkontrolle.getKontrolleTermin2();
		}
		return null;
	}


	@Override
	public @NonNull ID<Impfdossier> getImpfdossierId() {
		return toId(getId());
	}
}
