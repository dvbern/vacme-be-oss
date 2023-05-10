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

package ch.dvbern.oss.vacme.entities.documentqueue.entities;

import ch.dvbern.oss.vacme.entities.documentqueue.DocumentQueueType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;

@SuppressWarnings("JpaMissingIdInspection")
@Entity
@Getter
@Setter
@Slf4j
public class AbrechnungKindDocQueue extends AbrechnungDocQueue {

	private static final long serialVersionUID = 3761304467250446084L;

	public AbrechnungKindDocQueue() {
		super();
		this.setTyp(DocumentQueueType.ABRECHNUNG_KIND);
	}
}
