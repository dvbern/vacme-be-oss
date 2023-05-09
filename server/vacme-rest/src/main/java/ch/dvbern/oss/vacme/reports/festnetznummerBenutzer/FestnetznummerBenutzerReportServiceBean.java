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

package ch.dvbern.oss.vacme.reports.festnetznummerBenutzer;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.benutzer.QBenutzer;
import ch.dvbern.oss.vacme.entities.registration.QRegistrierung;
import ch.dvbern.oss.vacme.reports.AbstractReportServiceBean;
import ch.dvbern.oss.vacme.smartdb.Db;
import ch.dvbern.oss.vacme.util.PhoneNumberUtil;
import com.querydsl.core.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Transactional
@ApplicationScoped
@Slf4j
public class FestnetznummerBenutzerReportServiceBean extends AbstractReportServiceBean {

	private final Db db;

	@Inject
	public FestnetznummerBenutzerReportServiceBean(@NonNull Db db) {
		this.db = db;
	}

	public List<String> fetchAllValidFestnetznummerBenutzer() {
		QBenutzer qBenutzer = new QBenutzer("benutzer");
		QRegistrierung qRegistrierung = new QRegistrierung("registrierung");
		List<Tuple> benutzer = this.db
			.select(
				qBenutzer.name,
				qBenutzer.vorname,
				qBenutzer.email,
				qBenutzer.mobiltelefon,
				qRegistrierung.registrierungsnummer)
			.from(qBenutzer)
			.innerJoin(qRegistrierung).on(qRegistrierung.benutzerId.eq(qBenutzer.id))
			.fetch();
		return benutzer.stream()
			.filter(tuple -> PhoneNumberUtil.isValidNumber(tuple.get(3, String.class)))
			.filter(tuple -> !PhoneNumberUtil.isMobileNumber(tuple.get(3, String.class)))
			.map(tuple -> tuple.get(0, String.class) + ", "
				+ tuple.get(1, String.class) + ", "
				+ tuple.get(2, String.class) + ", "
				+ tuple.get(3, String.class) + ", "
				+ tuple.get(4, String.class))
			.collect(
				Collectors.toList());
	}
}
