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
import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.util.ImpfinformationenUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO zum korrigieren des Datums eines Impftermins
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
@Slf4j
public class ImpfungDatumKorrekturJax {

	@NonNull
	@Schema(required = true)
	private Impffolge impffolge;

	@NonNull
	@NotNull
	private LocalDateTime terminTime;

	@Nullable
	private Integer impffolgeNr;

	@NonNull
	@Schema(required = true)
	private KrankheitIdentifier krankheitIdentifier;

	@SuppressWarnings("UnnecessaryLocalVariable")
	public boolean needsNewZertifikat(
		@NonNull ImpfinformationDto impfinformationDto,
		@NotNull @NonNull LocalDateTime timestampImpfungBeforeCorrection
	) {
		if (!impfinformationDto.getKrankheitIdentifier().isSupportsZertifikat()) {
			return false; // Don't need Zertificate if we don't support it
		}
		// Bei einer Datumskorrektur muss das Zertifikat immer erstellt werden, falls der Impfschutz
		// vollstaendig war
		if (impfinformationDto.getImpfdossier().abgeschlossenMitVollstaendigemImpfschutz()) {
			// Wenn nur die Uhrzeit geaendert hat, muss ebenfalls kein neues Zertifikat erstellt werden
			if (DateUtil.isSameDay(timestampImpfungBeforeCorrection, terminTime)) {
				return false;
			}
			// Wenn das Datum der ersten Impfung geaendert wurde, er aber mehr als 1 Impfung hatte,
			// ist das Datum der ersten irrelevant
			if (Impffolge.ERSTE_IMPFUNG == impffolge) {
				boolean hasSecondGrundimmunisierungsimpfung = impfinformationDto.getImpfung2() != null;
				if (hasSecondGrundimmunisierungsimpfung) {
					return false;
				} else {
					// wenn das Datum der ersten Impfung geandert wurde, und er eine Genesung hatte dann muessen wir
					// pruefen  ob die Genesung vor (Zertifikat ja) oder nach (Zertifikat nein) der Impfung war.
					Impfung relevantImpfung = ImpfinformationenService.readImpfungForImpffolge(impfinformationDto, impffolge);
					Validate.notNull(relevantImpfung, "Wenn mit vollstaendiger Impfschutz erreicht wurde MUSS eine "
						+ "Impfung bestehen (wir sind in Impffolge ERSTE_IMPFUNG)");
					boolean impfungAfterGenesung =
						!ImpfinformationenUtil.isRelevantImpfungBeforeCoronaTest(impfinformationDto.getImpfdossier(), terminTime);
					return impfungAfterGenesung;
				}
			}

			return true;
		}
		return false;
	}



	public boolean needToRevoke(
		@NonNull ImpfinformationDto impfinformationDto,
		@NotNull @NonNull LocalDateTime timestampImpfungBeforeCorrection
	) {
		if (!impfinformationDto.getKrankheitIdentifier().isSupportsZertifikat()) {
			return false; // Don't need Zertificate if we don't support it
		}
		// wenn das Datum der Impfung vor der Korrektur spaeter als der Test war und nach der Korrektur frueher als der
		// Test ist muessen wir revozieren
		final LocalDate pcrDatum = impfinformationDto.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().getPositivGetestetDatum();
		if (impfinformationDto.getImpfdossier().getVollstaendigerImpfschutzTyp() == VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME_GENESEN
			&& Impffolge.ERSTE_IMPFUNG == impffolge
			&& impfinformationDto.getImpfung2() == null
			&& pcrDatum != null) {

			boolean impfungWasAfterTestBeforeCorrection = timestampImpfungBeforeCorrection.toLocalDate().isAfter(pcrDatum);

			Impfung relevantImpfung = ImpfinformationenService.readImpfungForImpffolge(impfinformationDto, impffolge);
			Validate.notNull(relevantImpfung, "Wenn vollstaendiger Impfschutz erreicht wurde MUSS eine "
				+ "Impfung bestehen (wir sind in Impffolge ERSTE_IMPFUNG)");
			boolean impfungIsBeforeTestAfterCorrection =
				ImpfinformationenUtil.isRelevantImpfungBeforeCoronaTest(impfinformationDto.getImpfdossier(), terminTime);

			if (impfungWasAfterTestBeforeCorrection && impfungIsBeforeTestAfterCorrection) {
				LOG.debug("VACME-KORREKTUR: impfdatum ist neu NACH Testdatum -> allfaellig vorhandenes Zertifikat revozieren");
				return true;
			}
		}


		// same as needsNewZertifikat
		return needsNewZertifikat(impfinformationDto, timestampImpfungBeforeCorrection);
	}
}
