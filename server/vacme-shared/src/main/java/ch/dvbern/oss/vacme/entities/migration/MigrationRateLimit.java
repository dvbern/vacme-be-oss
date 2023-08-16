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

package ch.dvbern.oss.vacme.entities.migration;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity that stores the time of the last request that was made to our Migration Endpoint
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table
public class MigrationRateLimit extends AbstractUUIDEntity<MigrationRateLimit> {

	private static final long serialVersionUID = -6954683399637893875L;

	@NotNull
	@NonNull
	@Column(nullable = false, updatable = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	private LocalDateTime timestampLastRequest = LocalDateTime.now();
}
