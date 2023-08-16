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

import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.rest_client.well.model.Address;
import ch.dvbern.oss.vacme.rest_client.well.model.VacMeAppointmentRequestDto;
import ch.dvbern.oss.vacme.rest_client.well.model.VacMeApprovalPeriodRequestDto;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final  class WellApiServiceDTOMapperUtil {

	private WellApiServiceDTOMapperUtil() {
		// util
	}

	public static VacMeAppointmentRequestDto mapImpfterminToWellAppointment(
		@NonNull UUID wellUserId,
		int doseNumberOfTermin,
		@NonNull Impftermin impftermin
	) {

		VacMeAppointmentRequestDto vacMeAppointmentRequestDto = new VacMeAppointmentRequestDto();

		Date dateVon = DateUtil.getDate(impftermin.getImpfslot().getZeitfenster().getVon());
		Date dateBis = DateUtil.getDate(impftermin.getImpfslot().getZeitfenster().getBis());
		vacMeAppointmentRequestDto.setAppointmentStart(dateVon);
		vacMeAppointmentRequestDto.setAppointmentEnd(dateBis);
		vacMeAppointmentRequestDto.setVacmeAppointmentId(impftermin.getId().toString());
		vacMeAppointmentRequestDto.setDoseNumber(doseNumberOfTermin);
		vacMeAppointmentRequestDto.setDiseaseName(impftermin.getImpfslot().getKrankheitIdentifier().name()); // e.g "FSME"
		vacMeAppointmentRequestDto.setUserId(wellUserId);

		Address address = mapOdiAdresse(impftermin.getImpfslot().getOrtDerImpfung());
		vacMeAppointmentRequestDto.setAddress(address);
		return vacMeAppointmentRequestDto;

	}

	 static Address mapOdiAdresse(@NotNull @NonNull OrtDerImpfung odi) {

		Address address = new Address();
		address.setName(odi.getName());

		String combinedAddrString =
			Stream.of(odi.getAdresse().getAdresse1(), odi.getAdresse().getAdresse2()).filter(Objects::nonNull)
				.collect(
				Collectors.joining(", "));

		address.setStreet(combinedAddrString);
		address.setZipCode(odi.getAdresse().getPlz());
		address.setCity(odi.getAdresse().getOrt());

		return address;

	}

	public static VacMeApprovalPeriodRequestDto mapImpfschutzToApprovalPeriod(
		@NonNull UUID wellUserId,
		@NonNull KrankheitIdentifier krankheitIdentifier,
		int nextImpfungNumber,
		@NonNull Impfschutz impfschutz) {
		VacMeApprovalPeriodRequestDto vacMeApprovalPeriodRequestDto = new VacMeApprovalPeriodRequestDto();
		vacMeApprovalPeriodRequestDto.setVacmeApprovalPeriodId(impfschutz.getId().toString());
		vacMeApprovalPeriodRequestDto.setDoseNumber(nextImpfungNumber);
		vacMeApprovalPeriodRequestDto.setDiseaseName(krankheitIdentifier.name());
		vacMeApprovalPeriodRequestDto.setUserId(wellUserId);

		Date date = extractRelevantDateForApprovalNotification(impfschutz);
		vacMeApprovalPeriodRequestDto.setApprovalPeriodDate(date);

		return vacMeApprovalPeriodRequestDto;
	}

	@Nullable
	public static Date extractRelevantDateForApprovalNotification(Impfschutz impfschutz) {

		if (impfschutz.getFreigegebenNaechsteImpfungAb() != null) {
			return DateUtil.getDate(impfschutz.getFreigegebenNaechsteImpfungAb());
		}
		return null;
	}
}
