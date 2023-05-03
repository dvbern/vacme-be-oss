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

package ch.dvbern.oss.vacme.entities.zertifikat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Queue fuer die Abhandlung aller CovidCert Aktionen (create oder revoke)
 */
@Entity
@Table(
	indexes = {
		@Index(name = "IX_ZertifikatQueue_timestamp_prio", columnList = "timestampErstellt, prioritaet, id")
	}
)
@Getter
@Setter
@NoArgsConstructor
public class ZertifikatQueue extends AbstractUUIDEntity<ZertifikatQueue> {

	private static final long serialVersionUID = -2854345385817970097L;
	public static final int MAX_REVOCATION_RETRIES = 3;

	@NotNull @NonNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	private ZertifikatQueueTyp typ;

	@NotNull @NonNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Queue_zertifikat"), nullable = false, updatable = false)
	private Zertifikat zertifikatToRevoke;

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
	private ZertifikatQueueStatus status = ZertifikatQueueStatus.NEW;

	@NotNull
	@Column(nullable = false)
	private boolean prioritaet = false;


	public static ZertifikatQueue forRevocation(@NonNull Zertifikat zertifikat, boolean prioritaerUndOhneWaittime) {
		ZertifikatQueue queue = new ZertifikatQueue();
		if (RegistrierungsEingang.ONLINE_REGISTRATION == zertifikat.getRegistrierung().getRegistrierungsEingang()) {
			queue.setTyp(ZertifikatQueueTyp.REVOCATION_ONLINE);
		} else {
			queue.setTyp(ZertifikatQueueTyp.REVOCATION_POST);
		}
		queue.setStatus(ZertifikatQueueStatus.NEW);
		queue.setErrorCount(0);
		queue.setZertifikatToRevoke(zertifikat);
		queue.setPrioritaet(prioritaerUndOhneWaittime);
		return queue;
	}

	public boolean needsToRevoke() {
		return (typ == ZertifikatQueueTyp.REVOCATION_ONLINE || typ == ZertifikatQueueTyp.REVOCATION_POST)
			&& (ZertifikatQueueStatus.NEW == status || ZertifikatQueueStatus.FAILED_RETRY == status);
	}

	public boolean isAlreadyRevoked() {
		return zertifikatToRevoke != null && zertifikatToRevoke.getRevoked();
	}

	public void markSuccessful() {
		setStatus(ZertifikatQueueStatus.SUCCESS);
	}

	public void markFailed(@Nullable  String error) {
		if (getErrorCount() >= MAX_REVOCATION_RETRIES) {
			setStatus(ZertifikatQueueStatus.FAILED);
		} else {
			setErrorCount(getErrorCount() + 1);
			setStatus(ZertifikatQueueStatus.FAILED_RETRY);
		}
		setLastError(error);
	}
}
