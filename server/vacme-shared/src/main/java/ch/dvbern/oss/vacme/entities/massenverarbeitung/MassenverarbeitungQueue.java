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

package ch.dvbern.oss.vacme.entities.massenverarbeitung;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.annotations.Type;

/**
 * Queue in welche man die Impfungen schreibt fuer die man eine Massenverarbeitung plant
 */
@SuppressWarnings("JpaMissingIdInspection")
@Entity
@Getter
@Setter
@NoArgsConstructor
// IdNotNullEntityListener is NOT set as an Entity Listener here beacause we autogenerate the id
public class MassenverarbeitungQueue extends AbstractEntity<Long> {

	private static final long serialVersionUID = 8718148073668223848L;

	public static final int MAX_TRIES = 2;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO) // auto will use hibernate_sequence on persist
	private Long id;

	@NotNull
	@NonNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	private MassenverarbeitungQueueTyp typ;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_UUID_LENGTH)
	private String impfungId;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_UUID_LENGTH)
	private String odiId;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_ENUM_LENGTH)
	private String registrierungNummer;

	@NotNull
	@Column(nullable = false)
	private int errorCount = 0;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	@Size(max = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	private String lastError;

	@NotNull
	@NonNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	private MassenverarbeitungQueueStatus status = MassenverarbeitungQueueStatus.NEW;

	@Nullable
	@Type(type = "org.hibernate.type.UUIDCharType")
	@Column(nullable = true, length = DBConst.DB_UUID_LENGTH)
	private UUID impfdossierId;

	public Long getId() {
		return id;
	}

	public AbstractEntity<Long> setId(Long id) {
		this.id = id;
		return this;
	}

	@NonNull
	public static MassenverarbeitungQueue forExternalization(@NonNull ID<Impfung> impfungId) {
		MassenverarbeitungQueue queue = new MassenverarbeitungQueue();
		queue.setTyp(MassenverarbeitungQueueTyp.IMPFUNG_EXTERALIZE);
		queue.setStatus(MassenverarbeitungQueueStatus.NEW);
		queue.setErrorCount(0);
		queue.setImpfungId(impfungId.getId().toString());
		return queue;
	}

	@NonNull
	public static MassenverarbeitungQueue forImpfungOdiMove(
		@NonNull ID<Impfung> impfungId,
		@NonNull ID<OrtDerImpfung> ortDerImpfungID) {
		MassenverarbeitungQueue queue = new MassenverarbeitungQueue();
		queue.setTyp(MassenverarbeitungQueueTyp.IMPFUNG_ODI_MOVE);
		queue.setStatus(MassenverarbeitungQueueStatus.NEW);
		queue.setErrorCount(0);

		queue.setImpfungId(impfungId.getId().toString());
		queue.setOdiId(ortDerImpfungID.getId().toString());
		return queue;
	}

	@NonNull
	public static MassenverarbeitungQueue forRegistrierungDelete(@NonNull String registrierungNummer) {
		MassenverarbeitungQueue queue = new MassenverarbeitungQueue();
		queue.setTyp(MassenverarbeitungQueueTyp.REGISTRIERUNG_DELETE);
		queue.setStatus(MassenverarbeitungQueueStatus.NEW);
		queue.setErrorCount(0);
		queue.setRegistrierungNummer(registrierungNummer);
		return queue;
	}

	@NonNull
	public static MassenverarbeitungQueue forOdiLatLngCalculation(@NonNull ID<OrtDerImpfung> odiId) {
		MassenverarbeitungQueue queue = new MassenverarbeitungQueue();
		queue.setTyp(MassenverarbeitungQueueTyp.ODI_LAT_LNG_CALCULATE);
		queue.setStatus(MassenverarbeitungQueueStatus.NEW);
		queue.setErrorCount(0);
		queue.setOdiId(odiId.getId().toString());
		return queue;
	}

	@NonNull
	public static MassenverarbeitungQueue forImpfungenLoeschen(@NonNull ID<Impfung> impfungId) {
		MassenverarbeitungQueue queue = new MassenverarbeitungQueue();
		queue.setTyp(MassenverarbeitungQueueTyp.IMPFUNG_LOESCHEN);
		queue.setStatus(MassenverarbeitungQueueStatus.NEW);
		queue.setErrorCount(0);
		queue.setImpfungId(impfungId.getId().toString());
		return queue;
	}

	@NonNull
	public static MassenverarbeitungQueue forImpfgruppeFreigegeben(@NonNull UUID impfdossierID) {
		MassenverarbeitungQueue queue = new MassenverarbeitungQueue();
		queue.setTyp(MassenverarbeitungQueueTyp.IMPFGRUPPE_FREIGEBEN);
		queue.setStatus(MassenverarbeitungQueueStatus.NEW);
		queue.setErrorCount(0);
		queue.setImpfdossierId(impfdossierID);
		return queue;
	}

	public void markSuccessful() {
		setStatus(MassenverarbeitungQueueStatus.SUCCESS);
	}

	public void markFailed(@Nullable String error) {
		if (getErrorCount() >= MAX_TRIES) {
			setStatus(MassenverarbeitungQueueStatus.FAILED);
		} else {
			setErrorCount(getErrorCount() + 1);
			setStatus(MassenverarbeitungQueueStatus.FAILED_RETRY);
		}
		setLastError(error);
	}

	public void markFailedNoRetry(@Nullable String error) {
		setErrorCount(getErrorCount() + 1);
		setStatus(MassenverarbeitungQueueStatus.FAILED);
		setLastError(error);
	}

	public boolean notYetProcessed() {
		return (MassenverarbeitungQueueStatus.NEW == status || MassenverarbeitungQueueStatus.FAILED_RETRY == status);
	}
}
