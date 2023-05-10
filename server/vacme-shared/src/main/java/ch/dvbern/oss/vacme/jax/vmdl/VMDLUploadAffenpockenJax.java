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

import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfungTyp;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
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
public class VMDLUploadAffenpockenJax extends VMDLUploadBaseJax {

	@JsonProperty("vacc_reason_risk_behavior")
	@Schema(required = true, enumeration = { "0", "1" })
	private int vaccReasonRiskBehavior;

	@JsonProperty("vacc_reason_prof")
	@Schema(required = true, enumeration = { "0", "1" })
	private int vaccReasonProf;

	@JsonProperty("vacc_reason_contact_infected")
	@Schema(required = true, enumeration = { "0", "1" })
	private int vaccReasonContactInfected;

	// Serie: 1 = first vaccination , 2 =  Boosterimpfung, 2+ = Boosterimpfung 2 etc.
	@JsonProperty("vacc_serie")
	@Schema(required = true, minimum = "0", maximum = "9")
	private int vaccSerie;

	@JsonProperty("vacc_type")
	@Schema(required = true, enumeration = { "1", "2" })
	private int vaccType;


	@QueryProjection
	public VMDLUploadAffenpockenJax(
		@NonNull Impfung impfung,
		@NonNull Impfdossier impfdossier,
		@NonNull Fragebogen fragebogen,
		@Nullable Impfdossiereintrag impfdossiereintrag,
		@NonNull String reportingUnitID
	) {
		super(impfung, impfdossier, fragebogen, impfdossiereintrag, reportingUnitID);

		this.vaccReasonRiskBehavior = isReasonRiskBehavior(impfung);
		this.vaccReasonProf = isReasonProf(impfung);
		this.vaccReasonContactInfected = isReasonContactInfected(impfung);
		this.vaccSerie = calculateSerie(impfung, impfdossiereintrag);
		this.vaccType = calculateVaccType(impfung);

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
			return 5; // note that this is different from Covid
		case ANDERE:
			return 99;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 *
	 * @param impfung the impfung
	 * @return 1 = Subcautaneous; 2 = Intradermal
	 */
	private int calculateVaccType(Impfung impfung) {
		switch (impfung.getVerarbreichungsart()){
		case SUBKUTAN:
			return 1;
		case INTRADERMAL:
			return 2;
		default:
			throw new IllegalStateException("Unexpected value: " + impfung.getVerarbreichungsart() + " not supported for "
				+ "VMDL for Affenpocken");
		}
	}

	private int isReasonRiskBehavior(@NonNull Impfung impfung) {
		return Boolean.TRUE.equals(impfung.getRisikoreichesSexualleben()) ? 1 : 0;
	}

	private int isReasonProf(@NonNull Impfung impfung) {
		return Boolean.TRUE.equals(impfung.getImpfungAusBeruflichenGruenden()) ? 1 : 0;
	}

	private int isReasonContactInfected(Impfung impfung) {
		return Boolean.TRUE.equals(impfung.getKontaktMitPersonAusRisikogruppe()) ? 1 : 0;
	}

	/**
	 * Alle mit grundimmunisierung=true: 1 (also auch dritte Impfung, wenn sie grundimmunisierung hat)
	 * nachfolgende Impfungen: hochzaehlen, also 2, 3, 4...
	 */
	private int calculateSerie(@NonNull Impfung impfung, @Nullable Impfdossiereintrag impfdossiereintrag) {

		if (impfung.isGrundimmunisierung()) {
			return 1;
		}
		if (impfdossiereintrag == null) {
			LOG.warn("Fuer Impfung {} war der Impfdossiereintrag null", impfung.getId().toString());
			throw new AppFailureException("VMDL Daten waren Inkonsistent");
		}
		// Wenn die Impfung keine Grundimmunisierung ist schicken wir mindestens 2 oder sonst die Impffolgenr
		return Math.max(impfdossiereintrag.getImpffolgeNr() , 2);
	}

	@Override
	protected KrankheitIdentifier getIntendedKrankheit() {
		return KrankheitIdentifier.AFFENPOCKEN;
	}

	@Override
	public int calculateReasonOther(@NonNull Fragebogen fragebogen,@NonNull  Impfung impfung) {
		return (isReasonRiskBehavior(impfung) + isReasonProf(impfung) + isReasonContactInfected(impfung)) > 0 ? 0 : 1;
	}
}
