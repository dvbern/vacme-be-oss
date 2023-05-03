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

package ch.dvbern.oss.vacme.entities.embeddables;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.util.DBConst;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Embeddable
public class Adresse implements Serializable {

	private static final long serialVersionUID = -6268323532357774697L;

	@NotEmpty
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String adresse1;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String adresse2;

	@NotEmpty
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String plz;

	@NotEmpty
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String ort;


	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(adresse1)
				.addValue(adresse2)
				.addValue(plz)
				.addValue(ort)
				.toString();
	}

	@NonNull
	@JsonIgnore
	public String getPlzOrt() {
		return plz + ' ' + ort;
	}


	@NonNull
	@JsonIgnore
	public String getFullAdresseString() {
		String plzOrt = plz + ' ' + ort;
		String[] strings = { adresse1, adresse2, plzOrt };
		List<String> nonEmptyStrings = Arrays.stream(strings).filter(Objects::nonNull).collect(Collectors.toList());
		return StringUtils.join(nonEmptyStrings, ",");
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(adresse1)
			.append(adresse2)
			.append(plz)
			.append(ort).build();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Adresse) {
			Adresse compared = (Adresse) obj;
			return new EqualsBuilder()
				.append(adresse1, compared.adresse1)
				.append(adresse2, compared.adresse2)
				.append(plz, compared.plz)
				.append(ort, compared.ort)
				.build();
		}
		return false;
	}
}
