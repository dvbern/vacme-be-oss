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

import ch.dvbern.oss.vacme.entities.base.ApplicationMessage;
import ch.dvbern.oss.vacme.entities.base.ApplicationMessageStatus;
import ch.dvbern.oss.vacme.entities.embeddables.DateTimeRange;
import ch.dvbern.oss.vacme.jax.impfslot.DateTimeRangeJax;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ApplicationMessageJax extends AbstractUUIDEntityJax {

	@NonNull
	private ApplicationMessageStatus status;

	@Nullable
	private String htmlContent;

	@Nullable
	private String title;

	@NonNull
	private DateTimeRangeJax zeitfenster;

	public static ApplicationMessageJax from(
		@NonNull ApplicationMessage applicationMessage
	) {
		ApplicationMessageJax jax = new ApplicationMessageJax();
		jax.setTitle(applicationMessage.getTitle());
		jax.setStatus(applicationMessage.getStatus());
		jax.setHtmlContent(applicationMessage.getHtmlContent());
		jax.setId(applicationMessage.getId());
		jax.setZeitfenster(new DateTimeRangeJax(
			applicationMessage.getZeitfenster().getVon(),
			applicationMessage.getZeitfenster().getBis()
		));
		return jax;
	}

	public static ApplicationMessage toEntity(
		@NonNull ApplicationMessageJax jax
	) {
		ApplicationMessage message = new ApplicationMessage();
		message.setHtmlContent(jax.getHtmlContent());
		message.setTitle(jax.getTitle());
		message.setStatus(jax.getStatus());
		message.setZeitfenster(DateTimeRange.of(
			jax.getZeitfenster().getVon(),
			jax.getZeitfenster().getBis()
		));
		return message;
	}
}
