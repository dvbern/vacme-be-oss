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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.repo.ApplicationHealthRepo;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Transactional
@ApplicationScoped
@Slf4j
public class ApplicationHealthCorrectionService {

	private final ApplicationHealthRepo applicationHealthRepo;

	@Inject
	public ApplicationHealthCorrectionService(
		@NonNull ApplicationHealthRepo applicationHealthRepo
	) {
		this.applicationHealthRepo = applicationHealthRepo;
	}

	public void handleInkonsistenzenTermine() {
		applicationHealthRepo.handleInkonsistenzenTermine();
	}
}
