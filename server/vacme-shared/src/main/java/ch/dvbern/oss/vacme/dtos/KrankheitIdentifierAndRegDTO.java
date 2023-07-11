/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.dtos;

import ch.dvbern.oss.vacme.entities.base.HasRegistrierungsIdAndKrankheitsidentifier;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@AllArgsConstructor
public class KrankheitIdentifierAndRegDTO implements HasRegistrierungsIdAndKrankheitsidentifier {
	private final  ID<Registrierung> registrierungsId;
	private final KrankheitIdentifier krankheitIdentifier;

	@Override
	public KrankheitIdentifier getKrankheitIdentifier() {
		return krankheitIdentifier;
	}

	@Override
	public @NonNull ID<Registrierung> getRegistrierungsId() {
		return registrierungsId;
	}
}
