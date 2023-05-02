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

package ch.dvbern.oss.vacme.service.sms;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
public class ECallSmsProvider extends AbstractSmsProvider {

	public ECallSmsProvider(
		@NonNull String url,
		@NonNull String username,
		@NonNull String password,
		@NonNull String jobId,
		@NonNull String callback,
		@NonNull String callbackExt
	) {
		super(url, username, password, jobId, callback, callbackExt);
	}

	@Override
	public String getUsernameAttribute() {
		return "username";
	}

	@Override
	public String getPasswordAttribute() {
		return "password";
	}

	@Override
	public String getReceiverAttribute() {
		return "address";
	}

	@Override
	public String getMessageAttribute() {
		return "message";
	}

	@Override
	public String getJobIdAttribute() {
		return "jobid";
	}

	@Override
	public String getCallbackAttribute() {
		return "callback";
	}
}
