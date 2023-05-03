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

package ch.dvbern.oss.vacme.jax.applicationhealth;

import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ResultDTO {

	private StopWatch stopwatch;

	private String title ="";
	private boolean success;
	private String info = "";

	public void start(@NonNull String titleOfQuery) {
		title = titleOfQuery;
		stopwatch = StopWatch.createStarted();
		LOG.info("VACME-HEALTH: {} started", getTitle());
	}

	public void finish(boolean wasSuccessful) {
		if (wasSuccessful) {
			this.addInfo("... Keine Fehler gefunden");
		}
		success = wasSuccessful;
		long durationMs = -1;
		if (checkStopwatchInitializedAndWarnOtherwise()) {
			stopwatch.stop();
			durationMs = stopwatch.getTime(TimeUnit.MILLISECONDS);

		}
		LOG.info("VACME-HEALTH: {} beendet in {}ms: {}", getTitle(), durationMs, getInfo());
	}

	public void addInfo(@NonNull String additionalInfo) {
		if (StringUtils.isNotEmpty(this.info)) {
			this.info = this.info + '\n' + additionalInfo;
		} else {
			this.info = additionalInfo;
		}
		if (checkStopwatchInitializedAndWarnOtherwise() && this.stopwatch.isStopped()) {
			this.success = false;
			this.info = this.info
				+ "\nEs wurde Info angehaengt, nachdem ResultDTO#finish aufgerufen wurde! Dies ist nicht erlaubt.";
		}
	}

	private boolean checkStopwatchInitializedAndWarnOtherwise(){
		if (stopwatch == null) {
			this.success = false;
			this.info = this.info + "\nEs wurde Info angehaengt, ohne dass zuerst ResultDTO#start aufgerufen wurde! Dies ist nicht erlaubt.";
			return false;
		}
		return true;
	}

	public void setSuccess(boolean wasSuccessful) {
		this.success = wasSuccessful;
	}
}
