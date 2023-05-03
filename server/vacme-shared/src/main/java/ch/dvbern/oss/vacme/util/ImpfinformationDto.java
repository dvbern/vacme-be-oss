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

package ch.dvbern.oss.vacme.util;

import java.util.List;

import ch.dvbern.oss.vacme.entities.base.HasImpfdossierIdIdAndKrankheitsidentifier;
import ch.dvbern.oss.vacme.entities.base.HasRegistrierungsIdAndKrankheitsidentifier;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Volatile object holding relevant data of the Impfungen of a specific Registrierung.
 * For the business logic please use the ImpfinformationenService.java
 */
@Getter
public class ImpfinformationDto implements HasRegistrierungsIdAndKrankheitsidentifier, HasImpfdossierIdIdAndKrankheitsidentifier {

	@NonNull
	private final KrankheitIdentifier krankheitIdentifier;

	@NonNull
	private final Registrierung registrierung;

	@Nullable
	private final Impfung impfung1;

	@Nullable
	private final Impfung impfung2;

	@NonNull
	private final Impfdossier impfdossier;

	@Nullable
	private final List<Impfung> boosterImpfungen;

	@Nullable
	private final ExternesZertifikat externesZertifikat;

	@QueryProjection
	public ImpfinformationDto(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull Registrierung registrierung,
		@Nullable Impfung impfung1,
		@Nullable Impfung impfung2,
		@NonNull Impfdossier impfdossier,
		@Nullable ExternesZertifikat externesZertifikat
	) {
		this(
			krankheitIdentifier,
			registrierung,
			impfung1,
			impfung2,
			impfdossier,
			null,
			externesZertifikat);
	}

	@QueryProjection
	public ImpfinformationDto(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull Registrierung registrierung,
		@NonNull Impfdossier impfdossier,
		@Nullable ExternesZertifikat externesZertifikat
	) {
		this(
			krankheitIdentifier,
			registrierung,
			null,
			null,
			impfdossier,
			null,
			externesZertifikat);
	}


	public ImpfinformationDto(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull Registrierung registrierung,
		@Nullable Impfung impfung1,
		@Nullable Impfung impfung2,
		@NonNull Impfdossier impfdossier,
		@Nullable List<Impfung> boosterImpfungen,
		@Nullable ExternesZertifikat externesZertifikat
	) {
		this.krankheitIdentifier = krankheitIdentifier;
		this.registrierung = registrierung;
		if (this.krankheitIdentifier.isSupportsImpffolgenEinsUndZwei()) {
			this.impfung1 = impfung1;
			this.impfung2 = impfung2;
		} else {
			this.impfung1 = null;
			this.impfung2 = null;
		}
		this.impfdossier = impfdossier;
		this.boosterImpfungen = boosterImpfungen;
		if (this.krankheitIdentifier.isSupportsExternesZertifikat()) {
			this.externesZertifikat = externesZertifikat;
		} else {
			this.externesZertifikat = null;
		}
	}

	@Override
	public @NonNull ID<Registrierung> getRegistrierungsId() {
		return registrierung.toId();
	}

	@Override
	public @NonNull ID<Impfdossier> getImpfdossierId() {
		return getImpfdossier().toId();
	}
}
