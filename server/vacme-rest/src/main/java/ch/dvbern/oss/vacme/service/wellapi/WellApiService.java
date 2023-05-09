/*
 *
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.oss.vacme.service.wellapi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.enums.Kundengruppe;
import ch.dvbern.oss.vacme.rest_client.well.api.WellRestClientService;
import ch.dvbern.oss.vacme.rest_client.well.model.VacMeAppointmentRequestDto;
import ch.dvbern.oss.vacme.rest_client.well.model.VacMeApprovalPeriodRequestDto;
import ch.dvbern.oss.vacme.service.BenutzerService;
import ch.dvbern.oss.vacme.service.ImpfdossierService;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import static ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService.getNumberOfImpfung;
import static ch.dvbern.oss.vacme.service.wellapi.WellApiServiceDTOMapperUtil.extractRelevantDateForApprovalNotification;
import static ch.dvbern.oss.vacme.service.wellapi.WellApiServiceDTOMapperUtil.mapImpfterminToWellAppointment;

@ApplicationScoped
@Slf4j
@Transactional(TxType.NOT_SUPPORTED)
public class WellApiService {

	@Inject
	@RestClient
	WellRestClientService wellRestClientService;

	@Inject
	BenutzerService benutzerService;

	@Inject
	ImpfdossierService impfdossierService;

	@CheckIfWellDisabledInterceptor
	public void sendAccountLink(
		@NonNull UUID wellId
	) {
		sendAccountLinkAsync(wellId);
	}

	void sendAccountLinkAsync(@NonNull UUID wellId) {
		Uni.createFrom().item(wellId)
			.emitOn(Infrastructure.getDefaultWorkerPool())
			.onItem()
			.invoke(uuid -> wellRestClientService.createAccountLink(wellId)) // sent to well
			.subscribe().with(
				item -> {
					LOG.debug(String.format("WELL-API: Sent wellId '%s'", wellId));
				}, throwable -> {
					LOG.error(String.format("WELL-API: Could not send wellId '%s'", wellId), throwable);
				}
			);
	}

	@CheckIfWellDisabledInterceptor
	public void deleteAccountLink(
		@NonNull UUID wellId
	) {
		deleteAccountLinkAsync(wellId);
	}

	void deleteAccountLinkAsync(@NonNull UUID wellId) {
		Uni.createFrom().item(wellId)
			.emitOn(Infrastructure.getDefaultWorkerPool())
			.onItem()
			.invoke(uuid ->
				wellRestClientService.deleteAccountLink(wellId) // send delete to well
			)
			.subscribe().with(
				item -> {
					LOG.info(String.format("WELL-API: Unlinked wellId '%s'", wellId));
				}, throwable -> {
					LOG.error(String.format("WELL-API: Could not unlinke  wellId '%s'", wellId), throwable);
				}
			);
	}

	@Transactional(TxType.NOT_SUPPORTED)
	@CheckIfWellDisabledInterceptor
	public void sendAppointmentInfoToWell(
		@Nullable Impfdossiereintrag impfdossiereintrag
	) {
		final Optional<Impftermin> terminOptional =
			getImpfterminIfExistingAndWellAndGalenicaAndfutureEnabled(impfdossiereintrag);
		if (terminOptional.isPresent()) {

			Objects.requireNonNull(impfdossiereintrag);
			final Registrierung registrierung = impfdossiereintrag.getImpfdossier().getRegistrierung();
			final Optional<UUID> wellIdIfWellbenutzer = getWellIdIfWellbenutzer(registrierung);
			if (wellIdIfWellbenutzer.isPresent()) {
				int doseNumberOfTermin = impfdossiereintrag.getImpffolgeNr();
				VacMeAppointmentRequestDto body = mapImpfterminToWellAppointment(
					wellIdIfWellbenutzer.get(),
					doseNumberOfTermin,
					terminOptional.get());
				sendAppointmentInfoToWellAsync(body);
			}
		}
	}

	private static boolean terminIsInFuture(@NonNull Impftermin impftermin) {
		LocalDateTime terminStart = impftermin.getImpfslot().getZeitfenster().getVon();
		// Tagesgenau, Termine Heute werden noch geschickt
		boolean isInVergangenheit = terminStart.toLocalDate().isBefore(LocalDate.now());
		return !isInVergangenheit;
	}

	private static boolean terminIsInGalenicaOdi(@NonNull Impftermin impftermin) {
		Kundengruppe k = impftermin.getImpfslot().getOrtDerImpfung().getKundengruppe();
		boolean isGalenica = k == Kundengruppe.GALENICA;
		return isGalenica;
	}

	void sendAppointmentInfoToWellAsync(@NonNull VacMeAppointmentRequestDto vacMeAppointmentRequestDto) {
		Uni.createFrom().item(vacMeAppointmentRequestDto)
			.emitOn(Infrastructure.getDefaultWorkerPool())
			.onItem()
			.transform(item ->
				wellRestClientService.upsertAppointment(
					vacMeAppointmentRequestDto,
					vacMeAppointmentRequestDto.getVacmeAppointmentId()
				)
			)
			.subscribe().with(
				response -> {
					LOG.debug(String.format("WELL-API: Impftermin was sent to well '%s'", response));
				}, throwable -> {
					LOG.error(
						String.format("WELL-API: Could not send Impftermin to well '%s'", vacMeAppointmentRequestDto),
						throwable);
				}
			);
	}

	@Transactional(TxType.NOT_SUPPORTED)
	@CheckIfWellDisabledInterceptor
	public void deleteAppointmentInfoInWell(
		@Nullable Impfdossiereintrag impfdossiereintrag
	) {
		final Optional<Impftermin> termin =
			getImpfterminIfExistingAndWellAndGalenicaAndfutureEnabled(impfdossiereintrag);
		if (termin.isPresent()) {
			Objects.requireNonNull(impfdossiereintrag);
			final Registrierung registrierung = impfdossiereintrag.getImpfdossier().getRegistrierung();
			final Optional<UUID> wellIdIfWellbenutzer = getWellIdIfWellbenutzer(registrierung);
			if (wellIdIfWellbenutzer.isPresent()) {
				deleteAppointmentInfoInWellAsync(termin.get().toId());
			}
		}
	}

	void deleteAppointmentInfoInWellAsync(@NonNull ID<Impftermin> impftermin) {
		Uni.createFrom()
			.item(impftermin.getId().toString())
			.emitOn(Infrastructure.getDefaultWorkerPool())
			.onItem()
			.transform(item ->
				wellRestClientService.deleteAppointment(item)
			)
			.subscribe().with(appointmentEntityModel -> {
					LOG.debug(String.format(
						"WELL-API: Impftermin was deleted in well '%s'",
						appointmentEntityModel));
				}
				, throwable -> {
					LOG.error(String.format(
						"WELL-API: Could not send Impftermin cancelation to well '%s'",
						impftermin.getId().toString()), throwable);
				}
			);
	}

	@Transactional(TxType.NOT_SUPPORTED)
	@CheckIfWellDisabledInterceptor
	public void sendApprovalPeriod(@NonNull ImpfinformationDto impfinformationDto, @NonNull Impfschutz impfschutz) {
		int nextImpfungNumber = getNumberOfImpfung(impfinformationDto) + 1;
		Optional<UUID> wellIdOpt = getWellIdIfWellbenutzer(impfinformationDto.getRegistrierung());
		wellIdOpt.ifPresent(wellId ->
			sendApprovalPeriod(wellId, impfinformationDto.getKrankheitIdentifier(), nextImpfungNumber, impfschutz)
		);
	}

	@Transactional(TxType.NOT_SUPPORTED)
	@CheckIfWellDisabledInterceptor
	public void sendApprovalPeriod(
		@NonNull UUID wellUserId,
		@NonNull KrankheitIdentifier krankheitIdentifier,
		int nextImpfungNumber,
		@NonNull Impfschutz impfschutz
	) {
		if (!krankheitIdentifier.isWellEnabled()) {
			return;
		}
		// do not send impfschutz for first impfung
		if (nextImpfungNumber == 1) {
			return;
		}

		// if no freigabe date is set we send a delete request, also if impfschutz forbids benachrichtigung send a
		// delete
		// request because the rules might have changed
		boolean sendDelete = extractRelevantDateForApprovalNotification(impfschutz) == null
			|| !impfschutz.isBenachrichtigungBeiFreigabe();

		if (sendDelete) {
			deleteApprovalperiodAsync(impfschutz.toId());
			return;
		}

		// otherwise we send an update request
		VacMeApprovalPeriodRequestDto body = WellApiServiceDTOMapperUtil.mapImpfschutzToApprovalPeriod(
			wellUserId,
			krankheitIdentifier,
			nextImpfungNumber,
			impfschutz
		);
		sendApprovalPeriodAsync(body);

	}

	void deleteApprovalperiodAsync(ID<Impfschutz> impfschutzId) {
		Uni.createFrom().item(impfschutzId)
			.onItem()
			.invoke(impfschutzID -> {
				wellRestClientService.deleteApprovalPeriod(impfschutzId.getId().toString());
			}).subscribe().with(
				item -> {
					LOG.debug(String.format(
						"WELL-API: deleted Impfschutz with id in well '%s'",
						impfschutzId.getId()));
				}, throwable -> {
					LOG.error(String.format(
						"WELL-API: could not delete  Impfschutz with id '%s' in well",
						impfschutzId.getId()), throwable);
				}
			);

	}

	void sendApprovalPeriodAsync(
		@NonNull VacMeApprovalPeriodRequestDto approvalPeriod
	) {
		Uni.createFrom().item(approvalPeriod)
			.emitOn(Infrastructure.getDefaultWorkerPool())
			.onItem()
			.transform(item ->
				wellRestClientService.upsertApprovalPeriod(
					approvalPeriod,
					approvalPeriod.getVacmeApprovalPeriodId())
			)
			.subscribe().with(
				approvalPeriodEntityModel -> {
					LOG.debug(String.format(
						"WELL-API: Approval Period was sent to Well '%s'",
						approvalPeriodEntityModel));
				}
				, throwable -> {
					LOG.error(
						String.format(
							"WELL-API: Could not send Approval Period to Well '%s'",
							approvalPeriod.toString()), throwable);
				}
			);
	}

	@NonNull
	private static Optional<Impftermin> getImpfterminIfExistingAndWellAndGalenicaAndfutureEnabled(@Nullable Impfdossiereintrag impfdossiereintrag) {
		if (impfdossiereintrag == null) {
			return Optional.empty();
		}
		final Impftermin termin = impfdossiereintrag.getImpftermin();
		if (termin == null) {
			return Optional.empty();
		}
		if (!termin.getImpfslot().getKrankheitIdentifier().isWellEnabled()) {
			return Optional.empty();
		}
		if (!terminIsInFuture(termin)) {
			return Optional.empty();
		}
		if (!terminIsInGalenicaOdi(termin)) {
			return Optional.empty();
		}

		return Optional.of(termin);
	}

	@NonNull
	private Optional<UUID> getWellIdIfWellbenutzer(@NonNull Registrierung registrierung) {
		if (registrierung.getRegistrierungsEingang() == RegistrierungsEingang.ONLINE_REGISTRATION) {
			final Benutzer targetBenutzer = benutzerService.getBenutzerOfOnlineRegistrierung(registrierung);
			UUID uuid = targetBenutzer.getWellId() != null ? UUID.fromString(targetBenutzer.getWellId()) : null;

			return Optional.ofNullable(uuid);
		}
		return Optional.empty();
	}
}
