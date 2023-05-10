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
 * DTO welches zum selecten von Tagesstatistikdaten fuer 2. Termine benutzt wird. Geht nicht auf den Client
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
public class ImpfzentrumDayStatTermin2DataRow implements Comparator<ImpfzentrumDayStatTermin2DataRow>, Serializable {

	private static final long serialVersionUID = 4149469629573133984L;

	private @NonNull String registrierungsnummer;
	private @NonNull ImpfdossierStatus status;
	private @NonNull Impfslot slot2;

	private @NonNull LocalDateTime termin1Datum;
	private @NonNull OrtDerImpfung termin1Odi;
	private @NonNull Boolean termin1Gebucht;

	private @NonNull LocalDateTime termin2Datum;
	private @NonNull OrtDerImpfung termin2Odi;
	private @NonNull Boolean termin2Gebucht;

	private @Nullable LocalDateTime impfung1Datum;
	private @Nullable Impfstoff impfung1Impfstoff;

	private @Nullable LocalDateTime impfung2Datum;
	private @Nullable Impfstoff impfung2Impfstoff;


	@QueryProjection
	public ImpfzentrumDayStatTermin2DataRow(
		@NonNull Impfdossier impfdossier,
		@NonNull Impftermin termin1,
		@NonNull Impftermin termin2,
		@NonNull Impfslot slot2,
		@Nullable Impfung impfung1,
		@Nullable Impfung impfung2
	) {
		this.readAttributesOfDossier(impfdossier);
		this.readAttributesOfTermin1(termin1);
		this.readAttributesOfTermin2(termin2);
		this.slot2 = slot2;
		this.readAttributesOfImpfung1(impfung1);
		this.readAttributesOfImpfung2(impfung2);
	}

	private void readAttributesOfDossier(@NonNull Impfdossier impfdossier) {
		this.registrierungsnummer = impfdossier.getRegistrierung().getRegistrierungsnummer();
		this.status = impfdossier.getDossierStatus();
	}

	private void readAttributesOfTermin1(@Nullable Impftermin termin1) {
		if (termin1 != null) {
			this.termin1Datum = termin1.getImpfslot().getZeitfenster().getVon();
			this.termin1Odi = termin1.getImpfslot().getOrtDerImpfung();
			this.termin1Gebucht = termin1.isGebucht();
		}
	}

	private void readAttributesOfTermin2(@Nullable Impftermin termin2) {
		if (termin2 != null) {
			this.termin2Datum = termin2.getImpfslot().getZeitfenster().getVon();
			this.termin2Odi = termin2.getImpfslot().getOrtDerImpfung();
			this.termin2Gebucht = termin2.isGebucht();
		}
	}

	private void readAttributesOfImpfung1(@Nullable Impfung impfung1) {
		if (impfung1 != null) {
			this.impfung1Datum = impfung1.getTimestampImpfung();
			this.impfung1Impfstoff = impfung1.getImpfstoff();
		}
	}

	private void readAttributesOfImpfung2(@Nullable Impfung impfung2) {
		if (impfung2 != null) {
			this.impfung2Datum = impfung2.getTimestampImpfung();
			this.impfung2Impfstoff = impfung2.getImpfstoff();
		}
	}

	@Override
	public int compare(ImpfzentrumDayStatTermin2DataRow a, ImpfzentrumDayStatTermin2DataRow b) {
		return a.termin1Datum.compareTo(b.termin1Datum);
	}
}
