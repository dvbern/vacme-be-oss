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

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a one-stop shop for sending all available data to Well for a given user.
 * It syns the wellId of the user and then i t checks all well-enabled Impfdossiers
 * and sends Termine and Impfschutz to well
 */
@ApplicationScoped
@Slf4j
@Transactional(TxType.REQUIRES_NEW)
@AllArgsConstructor(onConstructor_ = @Inject)
public class WellApiInitalDataSenderService {

	private final WellApiService wellApiService;
	private final RegistrierungService registrierungService;
	private final ImpfinformationenService impfinformationenService;

	/**
	 * This is a one-stop shop for sending all available data to Well for a given user.
	 * It syns the wellId of the user and then it checks all well-enabled Impfdossiers
	 * and sends Termine and Impfschutz to well
	 */
	@CheckIfWellDisabledInterceptor
	public void sendAllAvailableDataToWell(String wellId, Benutzer benutzer) {
		wellApiService.sendAccountLink(UUID.fromString(wellId));

		Arrays.stream(KrankheitIdentifier.values())
			.filter(KrankheitIdentifier::isWellEnabled)
			.forEach(krankheitIdentifier -> {

				var infosOpt = findImpfdossierForKrankheitAndBenutzer(krankheitIdentifier, benutzer);
				infosOpt.ifPresent(infos -> {
					// send Termine
					this.sendRelevantTermineForKrankheitToWell(infos.getImpfdossier());

					// send Impfschutz if available
					if (infos.getImpfdossier().getImpfschutz() != null) {
						wellApiService.sendApprovalPeriod(infos, infos.getImpfdossier().getImpfschutz());
					}
				} );
			});
	}
	private void sendRelevantTermineForKrankheitToWell(Impfdossier impfdossier) {
		impfdossier.getImpfdossierEintraege()
			.forEach(
				wellApiService::sendAppointmentInfoToWell
			);
	}

	private Optional<ImpfinformationDto> findImpfdossierForKrankheitAndBenutzer(
		KrankheitIdentifier krankheitIdentifier,
		Benutzer benutzer) {
		Registrierung regOfBenutzer = registrierungService.findRegistrierungByUser(benutzer.getId());
		if (regOfBenutzer != null) {
			var impfinfosOpt =
				impfinformationenService.getImpfinformationenOptional(
					regOfBenutzer.getRegistrierungsnummer(),
					krankheitIdentifier);
			return impfinfosOpt;

		}
		return Optional.empty();
	}
}
