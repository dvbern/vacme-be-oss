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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.dvbern.oss.vacme.entities.util.Pager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PagerJax<T> {
	@NonNull
	private Long pageSize;

	@NonNull
	private Long pageAmount;

	@NonNull
	private Long currentPage;

	@NonNull
	private List<T> pageElements;

	public static <U, V> PagerJax<U> from(Pager<V> pager, Function<V, U> jaxConvertor) {
		return new PagerJax<>(
			pager.getPageSize(),
			pager.getPageAmount(),
			pager.getCurrentPage(),
			pager.getPageElements().stream().map(jaxConvertor).collect(Collectors.toList())
		);
	}
}
