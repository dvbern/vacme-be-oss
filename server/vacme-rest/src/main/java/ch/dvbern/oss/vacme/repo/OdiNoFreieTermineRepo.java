/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.repo;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.terminbuchung.OdiNoFreieTermine;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.QOdiNoFreieTermine;
import ch.dvbern.oss.vacme.entities.terminbuchung.QOrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.smartdb.Db;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequestScoped
@Transactional
@Slf4j
public class OdiNoFreieTermineRepo {

	private final Db db;

	@Inject
	public OdiNoFreieTermineRepo(Db db) {
		this.db = db;
	}

	public void create(@NonNull OdiNoFreieTermine odiNextFreieTermine) {
		db.persist(odiNextFreieTermine);
	}

	@NonNull
	public OdiNoFreieTermine update(@NonNull OdiNoFreieTermine odiNextFreieTermine) {
		return db.merge(odiNextFreieTermine);
	}

	@NonNull
	public Optional<OdiNoFreieTermine> getByOdiAndKrankheit(@NonNull OrtDerImpfung odi, @NonNull KrankheitIdentifier krankheitIdentifier) {
		return db.selectFrom(QOdiNoFreieTermine.odiNoFreieTermine)
			.innerJoin(QOrtDerImpfung.ortDerImpfung)
				.on(QOdiNoFreieTermine.odiNoFreieTermine.ortDerImpfung.eq(QOrtDerImpfung.ortDerImpfung))
			.fetchJoin()
			.where(QOrtDerImpfung.ortDerImpfung.eq(odi)
				.and(QOdiNoFreieTermine.odiNoFreieTermine.krankheitIdentifier.eq(krankheitIdentifier))
			)
			.fetchOne();
	}

}
