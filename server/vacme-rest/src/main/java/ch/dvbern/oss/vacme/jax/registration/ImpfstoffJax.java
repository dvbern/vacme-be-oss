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
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfstofftyp;
import ch.dvbern.oss.vacme.entities.types.ZulassungsStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ImpfstoffJax {

	@NonNull
	@Schema(required = true)
	private UUID id;

	@NonNull
	@Schema(required = true)
	private String name;

	@Schema(required = false)
	private String displayName;

	@NonNull
	@Schema(required = true)
	private String hersteller;

	@NonNull
	@Schema(required = true)
	private String code;

	@NonNull
	@Schema(required = true)
	private String covidCertProdCode;

	@Nullable
	private String hexFarbe;

	@Schema(required = true)
	private int anzahlDosenBenoetigt;

	@NonNull
	@Schema(required = true)
	private ZulassungsStatus zulassungsStatus;

	@Nullable
	private String informationsLink;

	@NonNull
	@Schema(required = true)
	private Impfstofftyp impfstofftyp;

	@NonNull
	@Schema(required = true)
	private ZulassungsStatus zulassungsStatusBooster;

	@Nullable
	private String wHoch2Code;

	@NonNull
	@Schema(required = true)
	private List<ImpfempfehlungChGrundimmunisierungJax> impfempfehlungen;

	@Schema(required = true)
	private boolean eingestellt;

	@NonNull @NotNull
	@Schema(required = true)
	private List<KrankheitJax> krankheiten;

	public static ImpfstoffJax from(@NonNull Impfstoff entity) {
		return new ImpfstoffJax(
			entity.getId(),
			entity.getName(),
			entity.getDisplayName(),
			entity.getHersteller(),
			entity.getCode(),
			entity.getCovidCertProdCode(),
			entity.getHexFarbe(),
			entity.getAnzahlDosenBenoetigt(),
			entity.getZulassungsStatus(),
			entity.getInformationsLink(),
			entity.getImpfstofftyp(),
			entity.getZulassungsStatusBooster(),
			entity.getWHoch2Code(),
			entity.getImpfempfehlungenChGrundimmunisierung()
				.stream().map(ImpfempfehlungChGrundimmunisierungJax::from)
				.collect(Collectors.toList()),
			entity.isEingestellt(),
			entity.getKrankheiten().stream()
				.map(KrankheitJax::of)
				.collect(Collectors.toList())
		);
	}


	public void applyTo(@NonNull Impfstoff impfstoff) {
		impfstoff.setName(name);
		impfstoff.setCode(code);
		impfstoff.setAnzahlDosenBenoetigt(anzahlDosenBenoetigt);
		impfstoff.setHersteller(hersteller);
		impfstoff.setCovidCertProdCode(covidCertProdCode);
		impfstoff.setHexFarbe(hexFarbe);
		impfstoff.setInformationsLink(informationsLink);
		impfstoff.setZulassungsStatus(zulassungsStatus);
		impfstoff.setImpfstofftyp(impfstofftyp);
		impfstoff.setZulassungsStatusBooster(zulassungsStatusBooster);
		impfstoff.setWHoch2Code(wHoch2Code);
		impfstoff.setEingestellt(eingestellt);
	}
}
