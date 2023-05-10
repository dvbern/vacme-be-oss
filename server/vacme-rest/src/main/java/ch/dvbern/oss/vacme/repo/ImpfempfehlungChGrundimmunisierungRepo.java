/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.impfen.ImpfempfehlungChGrundimmunisierung;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.smartdb.Db;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@RequestScoped
@Transactional
public class ImpfempfehlungChGrundimmunisierungRepo {

	private final Db db;

	@Inject
	public ImpfempfehlungChGrundimmunisierungRepo(Db db) {
		this.db = db;
	}

	@NonNull
	public ImpfempfehlungChGrundimmunisierung getOrCreateById(Impfstoff impfstoff, @Nullable UUID uuid) {
		return impfstoff.getImpfempfehlungenChGrundimmunisierung().stream()
			.filter(empfehlung -> empfehlung.getId().equals(uuid))
			.findFirst()
			.orElseGet(ImpfempfehlungChGrundimmunisierung::new);
	}

}
