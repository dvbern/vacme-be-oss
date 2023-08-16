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

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

@Getter
@AllArgsConstructor
public enum BenutzerRolle {
	// Default
	IMPFWILLIGER,
	IMPFTERMINCLIENT,

	// Callcenter
	//	CC
	CC_AGENT,
	CC_BENUTZER_VERWALTER,

	// Ort der Impfung
	//	OI
	OI_KONTROLLE,
	OI_DOKUMENTATION,
	OI_IMPFVERANTWORTUNG,
	OI_FACHBAB_DELEGIEREN,
	OI_BENUTZER_VERWALTER,
	OI_ORT_VERWALTER,
	OI_BENUTZER_REPORTER,
	OI_LOGISTIK_REPORTER,
	OI_MEDIZINISCHER_REPORTER,

	// Kanton
	//	KT
	KT_IMPFVERANTWORTUNG,
	KT_BENUTZER_REPORTER,
	KT_LOGISTIK_REPORTER,
	KT_MEDIZINISCHER_REPORTER,
	KT_BENUTZER_VERWALTER,
	KT_ZERTIFIKAT_AUSSTELLER,
	KT_NACHDOKUMENTATION,
	KT_MEDIZINISCHE_NACHDOKUMENTATION,
	KT_IMPFDOKUMENTATION,

	AS_REGISTRATION_OI,
	AS_BENUTZER_VERWALTER,


	// Migrationsuser
	MIG_MIGRATION_ADM,

	// Systemintern
	SYSTEM_INTERNAL_ADMIN,

	// Tracing
	TRACING;


	@NonNull
	public static List<BenutzerRolle> getImpfwilligerRoles() {
		return Arrays.asList(IMPFWILLIGER);
	}

	@NonNull
	public static List<BenutzerRolle> getCallCenterRoles() {
		return Arrays.asList(CC_AGENT, CC_BENUTZER_VERWALTER);
	}

	@NonNull
	public static List<BenutzerRolle> getOrtDerImpfungRoles() {
		return Arrays.asList(OI_KONTROLLE, OI_DOKUMENTATION, OI_BENUTZER_VERWALTER, OI_ORT_VERWALTER, OI_BENUTZER_REPORTER, OI_LOGISTIK_REPORTER, OI_MEDIZINISCHER_REPORTER);
	}

	@NonNull
	public static List<BenutzerRolle> getKantonRoles() {
		return Arrays.asList(KT_IMPFVERANTWORTUNG, KT_BENUTZER_REPORTER, KT_LOGISTIK_REPORTER, KT_MEDIZINISCHE_NACHDOKUMENTATION, KT_ZERTIFIKAT_AUSSTELLER, KT_MEDIZINISCHER_REPORTER, KT_BENUTZER_VERWALTER, KT_IMPFDOKUMENTATION, KT_NACHDOKUMENTATION);
	}

	@NonNull
	public static List<BenutzerRolle> getAdminRoles() {
		return Arrays.asList(AS_REGISTRATION_OI, AS_BENUTZER_VERWALTER);
	}
}
