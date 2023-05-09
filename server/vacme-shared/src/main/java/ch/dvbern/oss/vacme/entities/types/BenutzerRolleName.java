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

package ch.dvbern.oss.vacme.entities.types;

public class BenutzerRolleName {

	// Default
	public static final String IMPFWILLIGER = "IMPFWILLIGER";
	public static final String IMPFTERMINCLIENT = "IMPFTERMINCLIENT";

	// Callcenter
	//	CC
	public static final String CC_AGENT = "CC_AGENT";
	public static final String CC_BENUTZER_VERWALTER = "CC_BENUTZER_VERWALTER";

	// Ort der Impfung
	// OI
	public static final String OI_KONTROLLE = "OI_KONTROLLE";
	public static final String OI_DOKUMENTATION = "OI_DOKUMENTATION";
	public static final String OI_IMPFVERANTWORTUNG = "OI_IMPFVERANTWORTUNG";
	public static final String OI_FACHBAB_DELEGIEREN = "OI_FACHBAB_DELEGIEREN";
	public static final String OI_BENUTZER_VERWALTER = "OI_BENUTZER_VERWALTER";
	public static final String OI_ORT_VERWALTER = "OI_ORT_VERWALTER";
	public static final String OI_BENUTZER_REPORTER = "OI_BENUTZER_REPORTER";
	public static final String OI_LOGISTIK_REPORTER = "OI_LOGISTIK_REPORTER";
	public static final String OI_MEDIZINISCHER_REPORTER = "OI_MEDIZINISCHER_REPORTER";

	// Kanton
	//	KT
	public static final String KT_IMPFVERANTWORTUNG = "KT_IMPFVERANTWORTUNG";
	public static final String KT_BENUTZER_REPORTER = "KT_BENUTZER_REPORTER";
	public static final String KT_LOGISTIK_REPORTER = "KT_LOGISTIK_REPORTER";
	public static final String KT_MEDIZINISCHER_REPORTER = "KT_MEDIZINISCHER_REPORTER";
	public static final String KT_BENUTZER_VERWALTER = "KT_BENUTZER_VERWALTER";
	public static final String KT_ZERTIFIKAT_AUSSTELLER = "KT_ZERTIFIKAT_AUSSTELLER";
	public static final String KT_NACHDOKUMENTATION = "KT_NACHDOKUMENTATION";
	public static final String KT_MEDIZINISCHE_NACHDOKUMENTATION = "KT_MEDIZINISCHE_NACHDOKUMENTATION";
	public static final String KT_IMPFDOKUMENTATION = "KT_IMPFDOKUMENTATION";

	public static final String AS_REGISTRATION_OI = "AS_REGISTRATION_OI";
	public static final String AS_BENUTZER_VERWALTER = "AS_BENUTZER_VERWALTER";

	// Migrationsuser
	public static final String MIG_MIGRATION_ADM = "MIG_MIGRATION_ADM";

	// interne Timer laufen mit dieser Rolle
	public static final String SYSTEM_INTERNAL_ADMIN = "SYSTEM_INTERNAL_ADMIN";

	// Tracing
	public static final String TRACING = "TRACING";
}
