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

package ch.dvbern.oss.vacme.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.jax.tracing.TracingRegistrierungJax;
import ch.dvbern.oss.vacme.jax.tracing.TracingResponseJax;
import ch.dvbern.oss.vacme.repo.TracingRepo;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.*;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TracingService {

	private static final Set<ImpfdossierStatus> TRACING_RELEVANT_STATUS =
		Set.of( // Es muss mindestens einmal geimpft worden, sonst ist es fuer Tracing nicht relevant
			IMPFUNG_1_DURCHGEFUEHRT,
			IMPFUNG_2_KONTROLLIERT,
			IMPFUNG_2_DURCHGEFUEHRT,
			ABGESCHLOSSEN,
			AUTOMATISCH_ABGESCHLOSSEN,
			ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
			IMMUNISIERT,
			FREIGEGEBEN_BOOSTER,
			ODI_GEWAEHLT_BOOSTER,
			GEBUCHT_BOOSTER,
			KONTROLLIERT_BOOSTER
		);

	private final TracingRepo tracingRepo;
	private final ZertifikatService zertifikatService;
	private final ImpfinformationenService impfinformationenService;

	public TracingResponseJax findByRegistrierungsnummer(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull String registrierungsnummer
	) {
		List<Registrierung> registrierungList = tracingRepo.getByRegistrierungnummerAndStatus(
				krankheitIdentifier,
				registrierungsnummer, TRACING_RELEVANT_STATUS)
			.map(Collections::singletonList)
			.orElseGet(Collections::emptyList);
		return buildTracingResponseJax(mapTracingRegistrierungJaxList(krankheitIdentifier, registrierungList));
	}

	public TracingResponseJax findByCertificatUVCI(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull String uvci
	) {
		List<Registrierung> registrierungList = tracingRepo.getByZertifikatUVCIAndStatus(
				krankheitIdentifier,
				uvci, TRACING_RELEVANT_STATUS)
			.map(Collections::singletonList)
			.orElseGet(Collections::emptyList);
		return buildTracingResponseJax(mapTracingRegistrierungJaxList(krankheitIdentifier, registrierungList));
	}

	public TracingResponseJax findByKrankenkassennummer(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull String krankenkassennummer
	) {
		List<Registrierung> registrierungList = tracingRepo.getByKrankenkassennummerAndStatus(
			krankheitIdentifier,
			krankenkassennummer, TRACING_RELEVANT_STATUS);
		return buildTracingResponseJax(mapTracingRegistrierungJaxList(krankheitIdentifier, registrierungList));
	}

	@NonNull
	private List<TracingRegistrierungJax> mapTracingRegistrierungJaxList(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull List<Registrierung> registrierungList
	) {
		return registrierungList.stream()
			.map(registrierung -> {
				ImpfinformationDto impfinfos =
					impfinformationenService.getImpfinformationen(
						registrierung.getRegistrierungsnummer(),
						krankheitIdentifier);
				if (!impfinformationenService.hasVacmeImpfungen(impfinfos)) {
					return null; // es werden nur solche ueber die Schnittstelle returned die eine Impfung haben bei
					// uns haben
				}
				Impfung impfung1 = impfinfos.getImpfung1();
				Impfung impfung2 = impfinfos.getImpfung2();
				List<Impfung> boosterImpfungen = impfinfos.getBoosterImpfungen();
				final VollstaendigerImpfschutzTyp vollstaendigerImpfschutzTyp =
					impfinfos.getImpfdossier().getVollstaendigerImpfschutzTyp();

				Zertifikat zertifikat =
					zertifikatService.getNewestNonRevokedZertifikat(impfinfos.getImpfdossier()).orElse(null);
				return TracingRegistrierungJax.from(
					registrierung,
					impfung1,
					impfung2,
					boosterImpfungen,
					zertifikat,
					vollstaendigerImpfschutzTyp);
			})
			.filter(Objects::nonNull) // null kommt zurueck wenn es bei uns keine Impfung gibt
			.collect(Collectors.toList());
	}

	@NotNull
	private TracingResponseJax buildTracingResponseJax(@NonNull List<TracingRegistrierungJax> registrierungJaxList) {
		TracingResponseJax responseJax = new TracingResponseJax();
		responseJax.setRegistrierungen(registrierungJaxList);
		return responseJax;
	}
}
