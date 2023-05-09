/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of
 *  the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.jax.registration;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
public class ImpfdossierSummaryJax {

	@NonNull
	@Schema(required = true)
	private KrankheitJax krankheit;

	@NonNull
	@Schema(required = true)
	private String registrierungsnummer;

	@NonNull
	@Schema(required = true)
	private ImpfdossierStatus status;

	@Nullable
	@Schema(required = false)
	private ImpfungJax letzteImpfung;

	@Nullable
	@Schema(required = false)
	private ImpfterminJax nextTermin;

	@Schema(required = true)
	private boolean nichtVerwalteterOdiSelected;

	@Schema(required = true)
	private boolean externGeimpftConfirmationNeeded = false;

	@Schema(required = true)
	private boolean leistungerbringerAgbConfirmationNeeded = false;

	@Schema(required = false)
	private LocalDateTime freigabeNaechsteImpfung;

	@NonNull
	public static ImpfdossierSummaryJax of(@NonNull ImpfinformationDto infos, boolean noFreieTermine) {
		Objects.requireNonNull(infos.getImpfdossier());
		final Impfung newestVacmeImpfung = ImpfinformationenService.getNewestVacmeImpfung(infos);

		ImpfterminJax pendingTermin = null;

		final Optional<Impftermin> pendingBoosterTerminOptional =
			ImpfinformationenService.getPendingBoosterTermin(infos);
		if (pendingBoosterTerminOptional.isPresent()) {
			pendingTermin = new ImpfterminJax(pendingBoosterTerminOptional.get());
		}

		if (infos.getKrankheitIdentifier().isSupportsImpffolgenEinsUndZwei()) {
			final Impftermin impfterminZweiteImpffolge = infos.getImpfdossier().getBuchung().getImpftermin2();
			if (impfterminZweiteImpffolge != null && infos.getImpfung2() == null) {
				pendingTermin = new ImpfterminJax(impfterminZweiteImpffolge);
			}

			final Impftermin impfterminErsteImpffolge = infos.getImpfdossier().getBuchung().getImpftermin1();
			if (impfterminErsteImpffolge != null && infos.getImpfung1() == null) {
				pendingTermin = new ImpfterminJax(impfterminErsteImpffolge);
			}
		}

		LocalDateTime freigabeNaechsteImpfung = null;
		if (infos.getImpfdossier().getImpfschutz() != null) {
			freigabeNaechsteImpfung = infos.getImpfdossier().getImpfschutz().getFreigegebenNaechsteImpfungAb();
		}

		final Integer impffolgeNr =
			newestVacmeImpfung != null ? ImpfinformationenService.getImpffolgeNr(infos, newestVacmeImpfung) : null;
		return new ImpfdossierSummaryJax(
			new KrankheitJax(infos.getKrankheitIdentifier(), noFreieTermine),
			infos.getRegistrierung().getRegistrierungsnummer(),
			infos.getImpfdossier().getDossierStatus(),
			newestVacmeImpfung != null ? ImpfungJax.from(newestVacmeImpfung, impffolgeNr) : null,
			pendingTermin,
			infos.getImpfdossier().getBuchung().isNichtVerwalteterOdiSelected(),
			infos.getImpfdossier().getBuchung().isExternGeimpftConfirmationNeeded(),
			infos.getImpfdossier().isLeistungerbringerAgbConfirmationNeeded(),
			freigabeNaechsteImpfung
		);
	}
}
