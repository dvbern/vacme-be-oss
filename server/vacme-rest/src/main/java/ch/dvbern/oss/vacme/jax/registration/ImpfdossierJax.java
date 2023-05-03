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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import ch.dvbern.oss.vacme.entities.embeddables.ZweiteGrundimmunisierungVerzichtet;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.ErkrankungDatumHerkunft;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class ImpfdossierJax {

	private List<ImpfdossiereintragJax> impfdossiereintraege;
	private List<ErkrankungJax> erkrankungen;

	@Nullable
	private ImpfschutzJax impfschutzJax;

	public ImpfdossierJax(@NonNull Impfdossier impfdossier, @Nullable List<Impfung> boosterImpfungen, @Nullable ExternesZertifikat externesZertifikat) {
		List<ImpfdossiereintragJax> jaxes = new ArrayList<>();

		impfdossier.getImpfdossierEintraege().stream().sorted().forEach(impfdossiereintrag -> {
			if (impfdossiereintrag.getImpftermin() != null && boosterImpfungen != null) {

				Impftermin termin = impfdossiereintrag.getImpftermin();
				Impfung matchingImpfung =
					boosterImpfungen.stream().filter(impfung -> impfung.getTermin().equals(termin))
						.findAny()
						.orElse(null);

				jaxes.add(new ImpfdossiereintragJax(
					impfdossiereintrag,
					matchingImpfung));
			} else {
				jaxes.add(new ImpfdossiereintragJax(
					impfdossiereintrag));
			}
		});
		this.impfdossiereintraege = jaxes;

		this.erkrankungen = readErkrankungen(impfdossier, externesZertifikat);

		if (impfdossier.getImpfschutz() != null) {
			this.impfschutzJax = ImpfschutzJax.from(impfdossier.getImpfschutz());
		}
	}

	@NotNull
	private List<ErkrankungJax> readErkrankungen(@NotNull Impfdossier impfdossier, @Nullable ExternesZertifikat externesZertifikat) {
		List<ErkrankungJax> result = new ArrayList<>();

		appendErkrankungFromGrundimmunisierung(result, impfdossier);
		appendErkrankungFromExternesZertifikat(result, externesZertifikat);
		appendErkrankungenFromManuellErfasst(result, impfdossier);

		return result.stream().sorted(Comparator.comparing(ErkrankungJax::getDate)).collect(Collectors.toList());
	}

	private void appendErkrankungFromGrundimmunisierung(@NonNull List<ErkrankungJax> list, @NotNull @NonNull Impfdossier impfdossier) {
		final ZweiteGrundimmunisierungVerzichtet verzichtetData = impfdossier.getZweiteGrundimmunisierungVerzichtet();
		if (verzichtetData.isGenesen() && verzichtetData.getPositivGetestetDatum() != null) {
			ErkrankungJax erkrankungFromGrundimmunisierungsprozess = new ErkrankungJax(
				verzichtetData.getPositivGetestetDatum(),
				ErkrankungDatumHerkunft.GRUNDIMMUNISIERUNG);
			list.add(erkrankungFromGrundimmunisierungsprozess);
		}
	}

	private void appendErkrankungFromExternesZertifikat(@NonNull List<ErkrankungJax> list, @Nullable ExternesZertifikat externesZertifikat) {
		if (externesZertifikat != null && externesZertifikat.isGenesen() && externesZertifikat.getPositivGetestetDatum() != null) {
			ErkrankungJax erkrankungFromExternesZertifikat = new ErkrankungJax(externesZertifikat.getPositivGetestetDatum(),
				ErkrankungDatumHerkunft.EXT_ZERT);
			list.add(erkrankungFromExternesZertifikat);
		}
	}

	private void appendErkrankungenFromManuellErfasst(@NonNull List<ErkrankungJax> list, @NotNull @NonNull Impfdossier impfdossier) {
		list.addAll(impfdossier.getErkrankungenSorted().stream()
			.map(ErkrankungJax::new)
			.collect(Collectors.toList()));
	}
}
