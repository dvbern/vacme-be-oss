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

import javax.persistence.Entity;

import ch.dvbern.oss.vacme.entities.documentqueue.DocumentQueueType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("JpaMissingIdInspection")
@Entity
@Getter
@Setter
@Slf4j
public class RegsKantonCSVDocQueue extends SpracheabhDocQueue {

	private static final long serialVersionUID = -3151977745918538899L;

	public RegsKantonCSVDocQueue() {
		super();
		this.setTyp(DocumentQueueType.REGISTRIERUNGEN_KANTON_CSV);
	}
}
