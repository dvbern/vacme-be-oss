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

import ch.dvbern.oss.vacme.util.PhoneNumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public abstract class AbstractSmsProvider {

	public static final String ECALL = "ecall";
	public static final String SWISSPHONE = "swissphone";

	@NonNull
	private String url;

	@NonNull
	private String username;

	@NonNull
	private String password;

	@NonNull
	private String jobId;

	@NonNull
	private String callback;

	/**
	 * Callback for foreign numbers not starting with the default prefix
	 */
	@NonNull
	private String callbackExt;


	public AbstractSmsProvider(
		@NonNull String url,
		@NonNull String username,
		@NonNull String password,
		@NonNull String jobId,
		@NonNull String callback,
		@NonNull String callbackExt
	) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.jobId = jobId;
		this.callback = callback;
		this.callbackExt = callbackExt;
	}


	public abstract String getUsernameAttribute();

	public abstract String getPasswordAttribute();

	public abstract String getReceiverAttribute();

	public abstract String getMessageAttribute();

	public abstract String getJobIdAttribute();

	public abstract String getCallbackAttribute();

	public URI getUriWithParams(String to, String message) throws URISyntaxException {

		var countrySpecificCallback = to.startsWith(PhoneNumberUtil.MSG_MOBILE_PREFIX_DEFAULT)
			? callback
			: callbackExt;

		var uriBuilder = new URIBuilder(url)
			.setParameter(getUsernameAttribute(), username)
			.setParameter(getPasswordAttribute(), password)
			.setParameter(getReceiverAttribute(), to)
			.setParameter(getMessageAttribute(), message)
			.setParameter(getJobIdAttribute(), jobId)
			.setParameter(getCallbackAttribute(), countrySpecificCallback);
		return uriBuilder.build();
	}
}
