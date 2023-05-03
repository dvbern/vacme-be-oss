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

package ch.dvbern.oss.vacme.shared.util;

public final class OpenApiConst {

	public static final String TAG_STAMMDATEN = "stammdaten";
	public static final String TAG_DOSSIER = "dossier";
	public static final String TAG_EXTERNE_IMPFINFO = "externeimpfinfo";
	public static final String TAG_REPORTS = "reports";
	public static final String TAG_REPORTS_SYNC = "reports_sync";
	public static final String TAG_KONTROLLE = "kontrolle";
	public static final String TAG_IMPFDOKU = "impfdokumentation";
	public static final String TAG_DOWNLOAD = "download";
	public static final String TAG_TERMINBUCHUNG = "terminbuchung";
	public static final String TAG_PROPERTIES = "properties";
	public static final String TAG_APPLICATION_HEALTH = "applicationhealth";
	public static final String TAG_SYSTEM_ADMINISTRATION = "systemadministration";
	public static final String TAG_ORT_DER_IMPFUNG = "ortderimpfung";
	public static final String TAG_REGISTRIERUNG = "registrierung";
	public static final String TAG_ONBOARDING = "onboarding";
	public static final String TAG_IMPFSLOT = "impfslot";
	public static final String TAG_IMPFSTOFF = "impfstoff";
	public static final String TAG_KORREKTUR = "korrektur";
	public static final String TAG_AUTH = "auth";
	public static final String TAG_ODIBENUTZER = "odibenutzer";
	public static final String TAG_BENUTZER = "benutzer";
	public static final String TAG_PUBLIC = "public";
	public static final String TAG_STAT = "stat";
	public static final String TAG_DEVELOP = "develop";
	public static final String TAG_GEIMPFT = "geimpft";
	public static final String TAG_MESSAGES = "messages";
	public static final String TAG_DATA_MIGRATION = "data_migration";
	public static final String TAG_ODI_IMPORT = "odiImport";
	public static final String TAG_ZERTIFIKAT = "zertifikat";
	public static final String TAG_TESTDATA_CREATION = "testdata_creation";
	public static final String TAG_PERSONALIEN_SUCHE = "personalien_suche";
	public static final String TAG_SETTINGS = "settings";
	public static final String TAG_UMFRAGE = "umfrage";
	public static final String TAG_TRACING = "tracing";

	public static final class Format {
		public static final String DATE = "date";
		public static final String DATE_TIME = "date-time";
		public static final String DURATION = "duration";
		public static final String EMAIL = "email";
		public static final String UUID = "uuid";

		private Format() {
			// utility class
		}
	}

	private OpenApiConst() {
		// utility class
	}
}
