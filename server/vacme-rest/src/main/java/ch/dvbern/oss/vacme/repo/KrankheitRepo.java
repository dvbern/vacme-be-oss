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

import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.impfen.Krankheit;
import ch.dvbern.oss.vacme.entities.impfen.QKrankheit;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.smartdb.Db;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequestScoped
@Transactional
public class KrankheitRepo {

	private final Db db;

	@Inject
	public KrankheitRepo(Db db) {
		this.db = db;
	}

	public void create(@NonNull Krankheit krankheit) {
		db.persist(krankheit);
		db.flush();
	}

	@NonNull
	public Optional<Krankheit> getByIdentifier(@NonNull KrankheitIdentifier identifier) {
		return db.selectFrom(QKrankheit.krankheit)
				.where(QKrankheit.krankheit.identifier.eq(identifier)).fetchOne();
	}

	@NonNull
	public Krankheit getOrCreateByIdentifier(
		@NonNull KrankheitIdentifier identifier,
		@NonNull KantonaleBerechtigung kantonaleBerechtigung
	) {
		final Optional<Krankheit> optionalKrankheit = getByIdentifier(identifier);
		if (optionalKrankheit.isPresent()) {
			return optionalKrankheit.get();
		}
		Krankheit krankheit = new Krankheit();
		krankheit.setIdentifier(identifier);
		krankheit.setKantonaleBerechtigung(kantonaleBerechtigung);
		create(krankheit);
		return krankheit;
	}

	@NonNull
	public List<Krankheit> findAll() {
		return db.findAll(QKrankheit.krankheit);
	}

	public Krankheit update(@NonNull Krankheit krankheit) {
		return db.merge(krankheit);
	}
}
