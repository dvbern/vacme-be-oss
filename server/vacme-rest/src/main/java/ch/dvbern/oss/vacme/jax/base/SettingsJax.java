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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SettingsJax {

	private int distanceImpfungenDesired;
	private int distanceImpfungenToleranceBefore;
	private int distanceImpfungenToleranceAfter;
	private int minutesBetweenInfoUpdates;
	private int minutesBetweenNumberUpdates;
	private boolean showOnboardingWelcomeText;
	private boolean emailKorrekturEnabled;
	private List<TranslatedTextJax> generalInfotexts = new ArrayList<>();
	private boolean geocodingEnabled;
	private boolean boosterFreigabeNotificationDisabled;
	private boolean selbstzahlerFachapplikationEnabled;
	private boolean selbstzahlerPortalEnabled;
	@Schema(required = true)
	private boolean reservationsEnabled;
	@Schema(required = true)
	private boolean odiFilterWellForGalenicaEnabled;
	@Schema(required = true)
	private String wellUrl;

	@JsonIgnore
	public int getDistanceImpfungenMinimal() {
		return this.distanceImpfungenDesired - this.distanceImpfungenToleranceBefore;
	}

	@JsonIgnore
	public int getDistanceImpfungenMaximal() {
		return this.distanceImpfungenDesired + this.distanceImpfungenToleranceAfter;
	}
}
