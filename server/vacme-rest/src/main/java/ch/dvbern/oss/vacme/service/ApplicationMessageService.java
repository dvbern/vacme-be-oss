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

package ch.dvbern.oss.vacme.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.base.ApplicationMessage;
import ch.dvbern.oss.vacme.entities.util.Pager;
import ch.dvbern.oss.vacme.repo.ApplicationMessageRepo;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@ApplicationScoped
public class ApplicationMessageService {
	private final ApplicationMessageRepo applicationMessageRepo;

	@Inject
	public ApplicationMessageService(
		@NonNull ApplicationMessageRepo applicationMessageRepo
	) {
		this.applicationMessageRepo = applicationMessageRepo;
	}

	@NonNull
	public List<ApplicationMessage> getLatestMessage(@NonNull Integer amount) {
		return this.applicationMessageRepo.getLatestAtDateTime(LocalDateTime.now(), amount);
	}

	@NonNull
	public Pager<ApplicationMessage> getMessageAll(@NonNull Integer pageSize, @NonNull Integer pageIndex) {
		return this.applicationMessageRepo.getAllAtDateTime(LocalDateTime.now(), pageSize, pageIndex);
	}

	@Nullable
	public ApplicationMessage getById(@NonNull String messageId) {
		return this.applicationMessageRepo.getById(messageId).orElse(null);
	}

	public @NonNull Optional<ApplicationMessage> getByTitle(@NonNull String messageTitle) {
		return this.applicationMessageRepo.getByTitle(messageTitle);
	}

	public void create(@NonNull ApplicationMessage applicationMessage) {
		this.applicationMessageRepo.create(applicationMessage);
	}

	public void update(@NonNull String messageId, @NonNull ApplicationMessage applicationMessage) {
		ApplicationMessage formerMessage = getById(messageId);
		Objects.requireNonNull(formerMessage);
		formerMessage.setTitle(applicationMessage.getTitle());
		formerMessage.setZeitfenster(applicationMessage.getZeitfenster());
		formerMessage.setStatus(applicationMessage.getStatus());
		formerMessage.setHtmlContent(applicationMessage.getHtmlContent());
	}
}
