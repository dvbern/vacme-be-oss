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

package ch.dvbern.oss.vacme.jax.registration;

import java.util.List;

import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.impfen.Krankheit;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsart;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class KrankheitSettingsJax {

	@NonNull
	@Schema(required = true)
	private KrankheitIdentifier identifier;

	@Schema(required = true)
	private boolean supportsZertifikat;

	@Schema(required = true)
	private boolean supportsExternesZertifikat;

	@Schema(required = true)
	private boolean supportsZweiteImpfungVerzichten;

	@Schema(required = true)
	private boolean supportsDossierFileUpload;

	@Schema(required = true)
	private boolean supportsTerminbuchung;

	@Schema(required = true)
	private boolean supportsImpffolgenEinsUndZwei;

	@Schema(required = true)
	private boolean supportsMobileImpfteams;

	@Schema(required = true)
	private boolean supportsContactTracing;

	@Schema(required = false)
	private Integer warnMinAge;

	@Schema(required = true)
	private boolean supportsErkrankungen;

	@Schema(required = true)
	private boolean supportsCallcenter;

	@Schema(required = true)
	private boolean supportsTerminbuchungBeiNichtAufgefuehrtemOdi;

	@Schema(required = true)
	private List<Verarbreichungsart> supportedVerabreichungsarten;

	@Schema(required = true)
	private KantonaleBerechtigung kantonaleBerechtigung;

	@Schema(required = true)
	private boolean hasAtleastOneImpfungViewableByKanton;

	@Schema(required = true)
	private boolean supportsFreigabeSMS;

	@Schema(required = true)
	private boolean wellEnabled;

	public static KrankheitSettingsJax from(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull Krankheit krankheit
	) {
		return new KrankheitSettingsJax(
			krankheitIdentifier,
			krankheitIdentifier.isSupportsZertifikat(),
			krankheitIdentifier.isSupportsExternesZertifikat(),
			krankheitIdentifier.isSupportsZweiteImpfungVerzichten(),
			krankheitIdentifier.isSupportsDossierFileUpload(),
			krankheitIdentifier.isSupportsTerminbuchung(),
			krankheitIdentifier.isSupportsImpffolgenEinsUndZwei(),
			krankheitIdentifier.isSupportsMobileImpfteams(),
			krankheitIdentifier.isSupportsContactTracing(),
			krankheitIdentifier.getWarnMinAge(),
			krankheitIdentifier.isSupportsErkrankungen(),
			krankheitIdentifier.isSupportsCallcenter(),
			krankheitIdentifier.isSupportsTerminbuchungBeiNichtAufgefuehrtemOdi(),
			krankheitIdentifier.getSupportedVerabreichungsarten(),
			krankheit.getKantonaleBerechtigung(),
			krankheit.isHasAtleastOneImpfungViewableByKanton(),
			krankheitIdentifier.isSupportsFreigabeSMS(),
			krankheitIdentifier.isWellEnabled()
		);
	}
}
