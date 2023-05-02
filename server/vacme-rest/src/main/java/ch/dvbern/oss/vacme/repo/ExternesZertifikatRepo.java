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

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.HasImpfdossierId;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.QExternesZertifikat;
import ch.dvbern.oss.vacme.smartdb.Db;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequestScoped
@Transactional
public class ExternesZertifikatRepo {

	private final Db db;

	@Inject
	public ExternesZertifikatRepo(Db db) {
		this.db = db;
	}

	// darf nicht flushen, sonst kann man z.B. bei einer neuen Person kein externes Zertifikat speichern
	public void create(@NonNull ExternesZertifikat externesZertifikat) {
		db.persist(externesZertifikat);
	}

	@NonNull
	public Optional<ExternesZertifikat> findExternesZertifikatForDossier(@NonNull HasImpfdossierId hasDossierId) {
		var result = db.select(QExternesZertifikat.externesZertifikat)
			.from(QExternesZertifikat.externesZertifikat)
			.where(QExternesZertifikat.externesZertifikat.impfdossier.id
				.eq(hasDossierId.getImpfdossierId().getId())).fetchOne();
		return result;
	}

	public void update(@NonNull ExternesZertifikat externesZertifikat) {
		db.merge(externesZertifikat);
	}

	public void remove(@NonNull ExternesZertifikat externesZertifikat) {
		db.remove(externesZertifikat);
	}
}
