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

package ch.dvbern.oss.vacme.repo;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.MandantProperty;
import ch.dvbern.oss.vacme.entities.base.MandantPropertyKey;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.smartdb.Db;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.vacme.entities.base.QMandantProperty.mandantProperty;

@RequestScoped
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MandantPropertyRepo {

	private final Db db;

	@NonNull
	public List<MandantProperty> findAll() {
		return db.findAll(mandantProperty);
	}

	@NonNull
	public Optional<MandantProperty> findByKeyAndMandant(
		@NonNull MandantPropertyKey key,
		@NonNull Mandant mandant) {
		var result = db.selectFrom(mandantProperty)
			.where(mandantProperty.name.eq(key).and(mandantProperty.mandant.eq(mandant)))
			.fetchOne();
		return result;
	}
}
