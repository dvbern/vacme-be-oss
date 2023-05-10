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

package ch.dvbern.oss.vacme.entities.booster;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractLongEntity;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ch.dvbern.oss.vacme.entities.util.DBConst;

/**
 * Queue in welche man die Regs schreibt wenn man ihren Impschutz neu berechnen will
 */
@SuppressWarnings("JpaMissingIdInspection")
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
	indexes = {
		@Index(name = "IX_RegistrierungQueue_krankheit_status", columnList = "krankheitIdentifier, status, id"),
		@Index(name = "IX_RegistrierungQueue_status_timestampErstellt", columnList = "status, timestampErstellt, id"),
	}
)
public class RegistrierungQueue extends AbstractLongEntity<RegistrierungQueue> {

	private static final long serialVersionUID = -7175992968578622163L;
	public static final int MAX_TRIES = 3;


	@NotNull @NonNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	private RegistrierungQueueTyp typ;

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	private String registrierungNummer;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_UUID_LENGTH)
	private String impfungId;

	@NotNull
	@Column(nullable = false)
	private int errorCount = 0;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	@Size(max = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	private String lastError;


	@NotNull @NonNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	private RegistrierungQueueStatus status = RegistrierungQueueStatus.NEW;

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private KrankheitIdentifier krankheitIdentifier;

	public static RegistrierungQueue forRecalculation(@NonNull String regNum) {
		RegistrierungQueue queue = new RegistrierungQueue();
		queue.setTyp(RegistrierungQueueTyp.BOOSTER_RULE_RECALCULATION);
		queue.setStatus(RegistrierungQueueStatus.NEW);
		queue.setErrorCount(0);
		queue.setRegistrierungNummer(regNum);
		return queue;
	}

	public boolean needsToRecalculate() {
		return (typ == RegistrierungQueueTyp.BOOSTER_RULE_RECALCULATION
			|| typ == RegistrierungQueueTyp.BOOSTER_RULE_RECALCULATION_EMPHERAL)
			&& (RegistrierungQueueStatus.NEW == status || RegistrierungQueueStatus.FAILED_RETRY == status);
	}

	public void markSuccessful() {
		setStatus(RegistrierungQueueStatus.SUCCESS);
	}

	public void markFailed(@Nullable  String error) {
		if (getErrorCount() >= MAX_TRIES) {
			setStatus(RegistrierungQueueStatus.FAILED);
		} else {
			setErrorCount(getErrorCount() + 1);
			setStatus(RegistrierungQueueStatus.FAILED_RETRY);
		}
		setLastError(error);
	}
}
