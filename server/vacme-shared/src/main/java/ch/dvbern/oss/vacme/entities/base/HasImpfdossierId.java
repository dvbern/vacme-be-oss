/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.entities.base;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface HasImpfdossierId {

	@NonNull
	ID<Impfdossier> getImpfdossierId();
}
