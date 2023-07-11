
/*
 *
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

package ch.dvbern.oss.vacme.jax.vmdl;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfungTyp;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class VMDLUploadCovidJax extends VMDLUploadBaseJax {

	private static final int AGE_REASON_LIMIT = 65;


	// MedStat: Wohnort nach Gesundheitsversorgungsregion (MedStat)
	@JsonProperty("medstat")
	@Schema(required = true, maxLength = 4)
	private String medstat;

	@JsonProperty("vacc_reason_age")
	@Schema(required = true, enumeration = { "0", "1" })
	private int vaccReasonAge;

	@JsonProperty("vacc_reason_chronic_disease")
	@Schema(required = true, enumeration = { "0", "1" })
	private int vaccReasonChronicDisease;

	@JsonProperty("vacc_reason_med_prof")
	@Schema(required = true, enumeration = { "0", "1" })
	private int vaccReasonMedProf;

	@JsonProperty("vacc_reason_contact_vuln")
	@Schema(required = true, enumeration = { "0", "1" })
	private int vaccReasonContactVuln;

	@JsonProperty("vacc_reason_contact_comm")
	@Schema(required = true, enumeration = { "0", "1" })
	private int vaccReasonContactComm;


	// Serie: 0 = gehoert zur Grundimmunisierung, 1 =  Boosterimpfung, 2+ = Boosterimpfung 2 etc.
	@JsonProperty("serie")
	@Schema(required = true,  minimum = "0",  maximum = "9")
	private int serie;

	@JsonProperty("person_recovered_from_covid")
	@Schema(enumeration = { "0", "1" })
	private Integer personRecoveredFromCovid;

	@Nullable
	@JsonProperty("pcr_tested_positive_date")
	@Schema(format = OpenApiConst.Format.DATE, minimum = "2020-12-20")
	private LocalDate pcrTestedPositiveDate;


	@JsonIgnore
	private boolean isAusland; // noetig fuer MedStat

	@QueryProjection
	public VMDLUploadCovidJax(
		@NonNull Impfung impfung,
		@NonNull Impfdossier impfdossier,
		@NonNull Fragebogen fragebogen,
		@NonNull String reportingUnitID
	) {
		this(impfung, impfdossier, fragebogen, null, reportingUnitID);

	}

	@QueryProjection
	public VMDLUploadCovidJax(
		@NonNull Impfung impfung,
		@NonNull Impfdossier impfdossier,
		@NonNull Fragebogen fragebogen,
		@Nullable Impfdossiereintrag impfdossiereintrag,
		@NonNull String reportingUnitID
	) {

		super(impfung, impfdossier, fragebogen, impfdossiereintrag, reportingUnitID);

		final Registrierung registrierung = impfdossier.getRegistrierung();
		this.medstat = MEDSTAT_UNKNOWN; // will later be set by mapping plz if possible
		this.vaccReasonAge = isAgeReason(registrierung);
		this.vaccReasonChronicDisease = isChronicDiseaseReason(fragebogen);
		this.vaccReasonMedProf = isReasonMedProf(fragebogen);
		this.vaccReasonContactVuln = isReasonContactVuln(fragebogen);
		this.vaccReasonContactComm = isReasonContactComm(fragebogen);
		this.serie = calculateSerie(impfung, impfdossiereintrag);
		this.personRecoveredFromCovid = isPersonRecoveredFromCovid(impfdossier);
		this.pcrTestedPositiveDate = extractPCRTestDate(impfdossier);

	}

	@Override
	protected KrankheitIdentifier getIntendedKrankheit() {
		return KrankheitIdentifier.COVID;
	}

	private int isAgeReason(Registrierung registrierung) {
		return calculateAge(registrierung) >= AGE_REASON_LIMIT ? 1 : 0; // Ab 65 Jahre
	}

	private int isChronicDiseaseReason(Fragebogen fragebogen) {
		switch (fragebogen.getChronischeKrankheiten()) {
		case KRANKHEIT:
		case SCHWERE_KRANKHEITSVERLAEUFE:
			return 1;
		default:
			return 0;
		}
	}

	private int isReasonMedProf(Fragebogen fragebogen) {
		switch (fragebogen.getBeruflicheTaetigkeit()) {
		case GES_PERSONAL_MIT_PAT_KONTAKT_INTENSIV:
		case GES_PERSONAL_MIT_PAT_KONTAKT:
		case GES_PERSONAL_OHNE_PAT_KONTAKT:
			return 1;
		default:
			return 0;
		}
	}

	private int isReasonContactVuln(Fragebogen fragebogen) {
		switch (fragebogen.getLebensumstaende()) {
		case MIT_BESONDERS_GEFAEHRDETEN_PERSON:
			return 1;
		}
		switch (fragebogen.getBeruflicheTaetigkeit()) {
		case BETREUUNG_VON_GEFAERD_PERSON:
			return 1;
		}
		return 0;
	}

	private int isReasonContactComm(Fragebogen fragebogen) {
		switch (fragebogen.getLebensumstaende()) {
		case GEMEINSCHAFTEN:
		case MASSENUNTERKUENFTEN:
			return 1;
		default:
			return 0;
		}
	}

	@Override
	public int calculateReasonOther(@NonNull Fragebogen fragebogen, @NonNull Impfung impfung) {
		return (
			isAgeReason(fragebogen.getRegistrierung()) +
				isChronicDiseaseReason(fragebogen) +
				isReasonMedProf(fragebogen) +
				isReasonContactVuln(fragebogen) +
				isReasonContactComm(fragebogen)
		) > 0 ? 0 : 1;
	}

	/**
	Alle mit grundimmunisierung=true: 1 (also auch dritte Impfung, wenn sie grundimmunisierung hat)
	nachfolgende Impfungen: hochzaehlen, also 2, 3, 4...
	 */
	private int calculateSerie(@NonNull Impfung impfung, @Nullable Impfdossiereintrag impfdossiereintrag) {
		if (impfung.isGrundimmunisierung()) {
			return 1;
		}
		if (impfdossiereintrag == null) {
			LOG.warn("Fuer Impfung {} war der Impfdossiereintrag null", impfung.getId().toString());
			throw new AppFailureException("VMDL Daten waren Inkonsistent");
		}
		// Wenn man corona hatte oder janssen wuerden wir ohne das max eine 1 schicken beim ersten booster was falsch waere
		return Math.max(impfdossiereintrag.getImpffolgeNr() - 1, 2);
	}

	@Override
	protected int getOdiType(@NotNull @NonNull OrtDerImpfungTyp typ) {

		switch (typ) {
		case IMPFZENTRUM:
		case KINDER_IMPFZENTRUM:
		case MOBIL:
			return 1;
		case ALTERSHEIM:
			return 2;
		case HAUSARZT:
			return 3;
		case APOTHEKE:
			return 4;
		case SPITAL:
			return 6; // note that this is different from Affenpocken
		case ANDERE:
			return 99;
		default:
			throw new IllegalArgumentException();
		}
	}


	private Integer isPersonRecoveredFromCovid(@NonNull Impfdossier impfdossier) {
		return impfdossier.abgeschlossenMitCorona() ? Integer.valueOf(1) : Integer.valueOf(0);
	}

	// VMDL erlaubt nur Daten ab dem 20.12.2020. Im VacMe dagegen erlauben wir Daten ab dem 01.01.2020
	@Nullable
	private LocalDate extractPCRTestDate(@NonNull Impfdossier impfdossier) {
		if (impfdossier.getZweiteGrundimmunisierungVerzichtet().getPositivGetestetDatum() != null &&
			DateUtil.contains(impfdossier.getZweiteGrundimmunisierungVerzichtet().getPositivGetestetDatum(),
				impfdossier.getKrankheitIdentifier().getMindateForImpfungen().toLocalDate(),
				LocalDate.now())) {
			return impfdossier.getZweiteGrundimmunisierungVerzichtet().getPositivGetestetDatum();
		}
		return null;
	}
}
