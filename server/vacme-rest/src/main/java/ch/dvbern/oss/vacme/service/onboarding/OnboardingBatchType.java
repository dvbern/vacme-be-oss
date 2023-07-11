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

package ch.dvbern.oss.vacme.service.onboarding;

import ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * There might be different ways to onboard the user
 */
public enum OnboardingBatchType {

	POST;

	@NonNull
	public static ApplicationPropertyKey mapBatchTypeToLockKey(@NonNull OnboardingBatchType type){
		if (type == OnboardingBatchType.POST) {
			return ApplicationPropertyKey.VACME_ONBOARDING_BRIEF_BATCHJOB_LOCK;
		}
		throw new AppFailureException(String.format("Could not map BatchType %s to lock mode", type));
	}
}
