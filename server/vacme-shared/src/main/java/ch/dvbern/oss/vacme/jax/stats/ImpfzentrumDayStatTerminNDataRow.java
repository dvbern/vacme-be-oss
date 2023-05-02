/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.jax.stats;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * DTO welches zum selecten von Tagesstatistikdaten fuer N. Termine benutzt wird. Geht nicht auf den Client
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
public class ImpfzentrumDayStatTerminNDataRow implements Comparator<ImpfzentrumDayStatTerminNDataRow>, Serializable {

	private static final long serialVersionUID = 8824691978799366267L;

	private @NonNull String registrierungsnummer;
	private @NonNull ImpfdossierStatus status;
	private @NonNull Impfslot slotN;

	private @NonNull LocalDateTime terminNDatum;
	private @NonNull OrtDerImpfung terminNOdi;
	private @NonNull Boolean terminNGebucht;

	private @Nullable LocalDateTime impfungNDatum;
	private @Nullable Impfstoff impfungNImpfstoff;

	@QueryProjection
	public ImpfzentrumDayStatTerminNDataRow(
		@NonNull Impfdossier impfdossier,
		@NonNull Impftermin terminN,
		@NonNull Impfslot slotN,
		@Nullable Impfung impfungN
	) {
		this.readAttributesOfDossier(impfdossier);
		this.readAttributesOfTerminN(terminN);
		this.slotN = slotN;
		this.readAttributesOfImpfungN(impfungN);
	}

	private void readAttributesOfDossier(@NonNull Impfdossier impfdossier) {
		this.registrierungsnummer = impfdossier.getRegistrierung().getRegistrierungsnummer();
		this.status = impfdossier.getDossierStatus();
	}

	private void readAttributesOfTerminN(@Nullable Impftermin terminN) {
		if (terminN != null) {
			this.terminNDatum = terminN.getImpfslot().getZeitfenster().getVon();
			this.terminNOdi = terminN.getImpfslot().getOrtDerImpfung();
			this.terminNGebucht = terminN.isGebucht();
		}
	}

	private void readAttributesOfImpfungN(@Nullable Impfung impfungN) {
		if (impfungN != null) {
			this.impfungNDatum = impfungN.getTimestampImpfung();
			this.impfungNImpfstoff = impfungN.getImpfstoff();
		}
	}

	@Override
	public int compare(ImpfzentrumDayStatTerminNDataRow a, ImpfzentrumDayStatTerminNDataRow b) {
		return a.terminNDatum.compareTo(b.terminNDatum);
	}
}
