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

package ch.dvbern.oss.vacme.entities.util;

import java.util.List;

import com.google.common.collect.Lists;
import com.querydsl.core.QueryResults;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Pager<T> {
	@NonNull
	private Long pageSize;

	@NonNull
	private Long pageAmount;

	@NonNull
	private Long currentPage;

	@NonNull
	private List<T> pageElements;

	public static <U> Pager<U> fromQueryResults(@NonNull QueryResults<U> results) {
		long pageSize = results.getLimit();
		long pageAmount = (results.getTotal() / pageSize) + 1;
		long currentPage = results.getOffset() / pageSize;
		return new Pager<>(
			pageSize,
			pageAmount,
			currentPage,
			results.getResults()
		);
	}

	public static <U> Pager<U> empty() {
		return new Pager<>(0L, 0L, 0L, Lists.newArrayList());
	}
}
