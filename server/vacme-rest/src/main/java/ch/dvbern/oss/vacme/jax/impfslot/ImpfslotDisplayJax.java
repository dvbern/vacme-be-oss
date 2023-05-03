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

package ch.dvbern.oss.vacme.jax.impfslot;

import java.util.UUID;
import java.util.function.Consumer;

import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class ImpfslotDisplayJax {
	@NonNull
	@Schema(required = true)
	private UUID id;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Schema(required = false)
	private DateTimeRangeJax zeitfenster;

	@Schema(required = true, minimum = "0", description = "Max. Kapazitaet der ersten Impfung")
	private int kapazitaetErsteImpfung;

	@Schema(required = true, minimum = "0", description = "Max. Kapazitaet der zweiten Impfung")
	private int kapazitaetZweiteImpfung;

	@Schema(required = true, minimum = "0", description = "Max. Kapazitaet fuer Booster-Impfungen")
	private int kapazitaetBoosterImpfung;

	public static ImpfslotDisplayJax of(@NonNull Impfslot impfslot) {
		return new ImpfslotDisplayJax(
			impfslot.getId(),
			new DateTimeRangeJax(
				impfslot.getZeitfenster().getVon(),
				impfslot.getZeitfenster().getBis()),
			impfslot.getKapazitaetErsteImpfung(),
			impfslot.getKapazitaetZweiteImpfung(),
			impfslot.getKapazitaetBoosterImpfung());
	}

	@JsonIgnore
	public Consumer<Impfslot> getUpdateEntityConsumer() {
		return impfslot -> {
			impfslot.setKapazitaetErsteImpfung(kapazitaetErsteImpfung);
			impfslot.setKapazitaetZweiteImpfung(kapazitaetZweiteImpfung);
			impfslot.setKapazitaetBoosterImpfung(kapazitaetBoosterImpfung);
		};
	}
}
