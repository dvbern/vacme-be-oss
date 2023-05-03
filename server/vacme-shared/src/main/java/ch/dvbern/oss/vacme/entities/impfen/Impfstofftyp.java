/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.entities.impfen;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(enumeration = {"MRNA", "ANDERE"})
public enum Impfstofftyp {

	MRNA,
	ANDERE,
}
