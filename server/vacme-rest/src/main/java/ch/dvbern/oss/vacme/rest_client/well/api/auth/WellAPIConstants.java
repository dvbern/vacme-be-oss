/*
 *
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.oss.vacme.rest_client.well.api.auth;

public final class WellAPIConstants {

	public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
	public static final String AUDIENCE_PARTNER_API = "partner-api";
	public static final String BEARER = "Bearer";

	/**
	 * The date format used by the Well API. Needed because if there is an Appintment at
	 * 1. of May at 09:00 2023 local time, then the Well api wants to receive that as
	 *  2023-05-01T07:00:00.000Z which is a UTC date. The default we would send without special configuration is
	 *  2023-05-01T07:00:00.000+00:00 which is also UTC but it lacks the Z as a timezone indicator and uses an offset
	 *  instead. So this pattern is used to tell Jackson via @JsonFormat to format dates accordingly
	 *  This is more or less ISO-8601
	 */
	public static final String WELL_API_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
}
