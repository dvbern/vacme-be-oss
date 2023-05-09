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

package ch.dvbern.oss.vacme.jax.base;

import java.util.function.Consumer;

import ch.dvbern.oss.vacme.entities.embeddables.Adresse;
import ch.dvbern.oss.vacme.jax.migration.RegistrierungMigrationJax;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdresseJax {

	@NonNull
	private String adresse1;

	@Nullable
	private String adresse2;

	@NonNull
	private String plz;

	@NonNull
	private String ort;


	@NonNull
	public static AdresseJax from(@NonNull Adresse adresseEntity) {
		return new AdresseJax(
			adresseEntity.getAdresse1(),
			adresseEntity.getAdresse2(),
			adresseEntity.getPlz(),
			adresseEntity.getOrt()
		);
	}

	@NonNull
	public static AdresseJax from(@NonNull RegistrierungMigrationJax registrierungMigrationJax) {
		return new AdresseJax(
			registrierungMigrationJax.getAdresse().getAdresse1(),
			registrierungMigrationJax.getAdresse().getAdresse2(),
			registrierungMigrationJax.getAdresse().getPlz(),
			registrierungMigrationJax.getAdresse().getOrt()
		);
	}

	@NonNull
	public Adresse toEntity() {
		Adresse adresse = new Adresse();
		adresse.setAdresse1(adresse1);
		adresse.setAdresse2(adresse2);
		adresse.setPlz(plz);
		adresse.setOrt(ort);
		return adresse;
	}

	@JsonIgnore
	public Consumer<Adresse> getUpdateEntityConsumer() {
		return adresse -> {
			adresse.setAdresse1(adresse1);
			adresse.setAdresse2(adresse2);
			adresse.setPlz(plz);
			adresse.setOrt(ort);
		};
	}
}
