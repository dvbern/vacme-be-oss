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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public final class Constants {


	public static final String MEMBER_OF_CLAIM = "memberof";
	public static final String SESSION_ID_CLAIM = "sid";
	public static final String GLN_CLAIM = "gln"; // name of gln attribut claim in oidc tokens
	public static final String MOBILE_NUMMER_CLAIM = "MobileNummer";
	public static final String WELL_ID_CLAIM = "WELL_ID";
	public static final Integer MAX_KEYCLOAK_RESULTS = 5000;
	public static final String LOG_MDC_VACMEUSER_ID = "vacmeuser_id";
	public static final String LOG_MDC_FORWARDED_FOR = "vacmeuser_ip";
	public static final String SMS_CONNECTION_TIMEOUT = "15";
	public static final String GEOLOCATION_CONNECTION_TIMEOUT = "5";
	public static final String DB_QUERY_SLOW_THRESHOLD = "10000";
	public static final int DB_QUERY_SLOW_THRESHOLD_LONG = 20000;
	public static final UUID DUMMY_MIGRATION_BENUTZER = UUID.fromString("de340daa-3ff7-40e3-b084-efff6b2fca0a");

	public static final String DUMMY_MIGRATION_ODI_SPITAL = "MIGRATION-SPITAL";
	public static final String DUMMY_MIGRATION_ODI_ALTERSHEIM = "MIGRATION-ALTERSHEIM";
	public static final String DUMMY_MIGRATION_ODI_IMPFTENTRUM = "MIGRATION-IMPFZENTRUM";
	public static final String DUMMY_MIGRATION_ODI_HAUSARZT = "MIGRATION-HAUSARZT";
	public static final String DUMMY_MIGRATION_ODI_APOTHEKE = "MIGRATION-APOTHEKE";
	public static final String DUMMY_MIGRATION_ODI_MOBIL = "MIGRATION-MOBIL";
	public static final String DUMMY_MIGRATION_ODI_ANDERE = "MIGRATION-ANDERE";

	// Covid-19 Impfstoffe
	public static final String PFIZER_BIONTECH_ID_STRING = "141fca55-ab78-4c0e-a2fd-edf2fe4e9b30";
	public static final UUID PFIZER_BIONTECH_UUID = UUID.fromString(PFIZER_BIONTECH_ID_STRING);
	public static final String PFIZER_BIONTECH_BIVALENT_ID_STRING = "765dd8e2-5294-4d85-87bb-6fce77362348";
	public static final UUID PFIZER_BIONTECH_BIVALENT_UUID = UUID.fromString(PFIZER_BIONTECH_BIVALENT_ID_STRING);
	public static final String MODERNA_ID_STRING  =  "c5abc3d7-f80d-44fd-be6e-0aba4cf03643";
	public static final UUID MODERNA_UUID = UUID.fromString(MODERNA_ID_STRING);
	public static final String MODERNA_BIVALENT_ID_STRING  =  "313769d0-a3e1-4c0f-92e2-264e32dd9b15";
	public static final UUID MODERNA_BIVALENT_UUID = UUID.fromString(MODERNA_BIVALENT_ID_STRING);
	public static final String ASTRA_ZENECA_ID_STRING =  "7ff61fb9-0993-11ec-b1f1-0242ac140003";
	public static final UUID ASTRA_ZENECA_UUID = UUID.fromString(ASTRA_ZENECA_ID_STRING);
	public static final String JANSSEN_ID_STRING = "12c5d49e-ce77-464a-a951-3c840e5a1d1b";
	public static final UUID JANSSEN_UUID = UUID.fromString(JANSSEN_ID_STRING);
	public static final String SINOVAC_ID_STRING = "ef4d4e99-583a-4e5d-9759-823002c6a260";
	public static final UUID SINOVAC_UUID = UUID.fromString(SINOVAC_ID_STRING);
	public static final String SINOPHARM_ID_STRING = "eb08558f-8d2b-4f64-adee-cdf0c642a387";
	public static final UUID SINOPHARM_UUID = UUID.fromString(SINOPHARM_ID_STRING);
	public static final String COVAXIN_ID_STRING = "54513128-c6ed-4fd6-b61d-c23e6a73b20c";
	public static final UUID COVAXIN_UUID = UUID.fromString(COVAXIN_ID_STRING);
	public static final String PFIZER_BIONTECH_KINDER_ID_STRING = "4ebb48c1-cc96-4a8e-9832-77092bb968db";
	public static final UUID PFIZER_BIONTECH_KINDER_UUID = UUID.fromString(PFIZER_BIONTECH_KINDER_ID_STRING);
	public static final String NOVAVAX_ID_STRING = "58f34c3a-b07b-48c8-a6a4-ae4e1305ba8d";
	public static final UUID NOVAVAX_UUID = UUID.fromString(NOVAVAX_ID_STRING);
	public static final String COVISHIELD_ID_STRING = "c065820c-ee51-411c-aef4-daf17fd5799a";
	public static final UUID COVISHIELD_UUID = UUID.fromString(COVISHIELD_ID_STRING);
	public static final String COVOVAX_ID_STRING = "7227720b-8764-465a-a430-dfc51c388709";
	public static final UUID COVOVAX_UUID = UUID.fromString(COVOVAX_ID_STRING);
	// Evt brauchen wir diese fuer etwas irgendwann? Wenn nicht gerne loeschen.
	public static final String SPUTNIK_LIGHT_ID_STRING = "9d8debb1-dbd6-4251-82ff-4c5da6ae19d5";
	public static final UUID SPUTNIK_LIGHT_UUID = UUID.fromString(SPUTNIK_LIGHT_ID_STRING);
	public static final String SPUTNIK_V_ID_STRING = "fb8d982c-61b3-4efb-adaf-9fad9160a501";
	public static final UUID SPUTNIK_V_UUID = UUID.fromString(SPUTNIK_V_ID_STRING);
	public static final String CONVIDECIA_ID_STRING = "c1542f26-6d64-4639-85c5-95a16acec687";
	public static final UUID CONVIDECIA_UUID = UUID.fromString(CONVIDECIA_ID_STRING);
	public static final String KAZAKHSTAN_RIBSP_ID_STRING = "882cff0d-ce48-4143-90a5-98f83730c3eb";
	public static final UUID KAZAKHSTAN_RIBSP_UUID = UUID.fromString(KAZAKHSTAN_RIBSP_ID_STRING);
	public static final String SARSCOV2_ID_STRING = "015b0cd2-e79a-41c4-92bd-62b83a64259d";
	public static final UUID SARSCOV2_UUID = UUID.fromString(SARSCOV2_ID_STRING);
	public static final String KOVIVAC_ID_STRING = "c8115c11-2dab-4e61-bfcc-ea1ee2192a08";
	public static final UUID KOVIVAC_UUID = UUID.fromString(KOVIVAC_ID_STRING);
	public static final String EPIVACCORONA_ID_STRING = "d012eedc-4e85-49cd-b4c8-003954577ccf";
	public static final UUID EPIVACCORONA_UUID = UUID.fromString(EPIVACCORONA_ID_STRING);
	public static final String RBD_ZIFIVAX_ID_STRING = "c91c87ed-93d1-4c7f-a93b-acb317768bff";
	public static final UUID RBD_ZIFIVAX_UUID = UUID.fromString(RBD_ZIFIVAX_ID_STRING);
	public static final String ABADALA_CIGB66_ID_STRING = "08794c7e-6447-4556-a1fb-070d89bc86cb";
	public static final UUID ABADALA_CIGB66_UUID = UUID.fromString(ABADALA_CIGB66_ID_STRING);
	public static final String PLUS_CIGB66_ID_STRING = "1ed24fff-dffc-4955-b2c8-876d3679f58d";
	public static final UUID PLUS_CIGB66_UUID = UUID.fromString(PLUS_CIGB66_ID_STRING);
	public static final String MVC_COV1901_ID_STRING = "030c6b57-b771-44f4-9163-fe8c03ecf35a";
	public static final UUID MVC_COV1901_UUID = UUID.fromString(MVC_COV1901_ID_STRING);
	public static final String ZYCOVD_ID_STRING = "3b15ef6d-db8d-403b-9bd0-291943443cc9";
	public static final UUID ZYCOVD_UUID = UUID.fromString(ZYCOVD_ID_STRING);

	// Affenpocken-Impfstoffe
	public static final String MVA_BN_ID_STRING = "adea588d-edfd-4955-9794-d120cbddbdf2";
	public static final UUID MVA_BN_UUID = UUID.fromString(MVA_BN_ID_STRING);
	public static final String UNBEKANNTE_POCKENIMPFUNG_IN_KINDHEIT_ID_STRING = "038f7b38-a6e8-46c0-876d-c8868623d1e1";
	public static final UUID UNBEKANNTE_POCKENIMPFUNG_IN_KINDHEIT_UUID = UUID.fromString(UNBEKANNTE_POCKENIMPFUNG_IN_KINDHEIT_ID_STRING);

	// FSME-Impfstoffe
	public static final String FSME_IMMUNE_ID_STRING = "b013cc91-3865-4150-93cf-2d1799d15061";
	public static final UUID FSME_IMMUNE_UUID = UUID.fromString(FSME_IMMUNE_ID_STRING);
	public static final String ENCEPUR_ID_STRING = "1b4b44f9-6726-4d6e-8f22-7266b1848bbf";
	public static final UUID ENCEPUR_UUID = UUID.fromString(ENCEPUR_ID_STRING);

	public static final int DAY_IN_SECONDS = 86400;

	public static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
	public static final int COOKIE_TIMEOUT_SECONDS = 60 * 60 * 12;
	public static final String COOKIE_XSRF_TOKEN = "XSRF-TOKEN";
	// we have no inform about login timestamps before this date
	public static final LocalDateTime EARLIEST_COLLECTED_LOGIN_TIMESTAMP = LocalDateTime.of(2022, 2, 24, 0, 0);

	public static final int LOADBALANCER_TIMEOUT_SECONDS = 300; // Da nach 4 min Timeout im LoadBalancer

	public static final String DEFAULT_CUTOFF_TIME_MONTHS_FOR_FREE_TERMINE = "3";
	public static final int TOLERANZ_SPAN_DAYS = 7;

	public static final String REGEX_TELEFON = "(0|\\+41|0041|\\+42|0042)[ ]*(([\\d]{1,2}))[ ]*[\\d]{3}[ ]*[\\d]{2}[ ]*[\\d]{2}";
	public static final String[] MOBILE_VORWAHLEN = {"75", "76", "77", "78", "79"};

	public static final int MIN_ABSTAND_ZWISCHEN_BENUTZERNAME_ABFRAGE_IN_MINUTEN = 30;

	public static final String STATISTIK_TERMINSLOTS_DTO_MAPPING = "statistik_terminslots_dto_mapping";
	public static final String STATISTIK_ODIS_DTO_MAPPING = "statistik_odis_dto_mapping";
	public static final String REPORTING_KANTON_DTO_MAPPING = "reporting_kanton_dto_mapping";
	public static final String REPORTING_KANTONSARZT_DTO_MAPPING = "reporting_kantonsarzt_dto_mapping";
	public static final String REPORTING_IMPFUNGEN_DTO_MAPPING = "reporting_impfungen_dto_mapping";
	public static final String REGISTTIERUNGSNUMMER_IMPFUNGID_DTO_MAPPING = "registrierungsnummer_impfungid_dto_mapping";

	public static final LocalDateTime MIN_DATE_FOR_IMPFUNGEN_COVID = LocalDate.of(2020, 12, 20).atStartOfDay();
	public static final LocalDateTime MIN_DATE_FOR_IMPFUNGEN_FSME = LocalDate.of(1900, 1, 1).atStartOfDay();
	public static final LocalDateTime MIN_DATE_FOR_IMPFUNGEN_AFFENPOCKEN = LocalDate.of(2022, 11, 1).atStartOfDay();
	public static final LocalDate MIN_DATE_FOR_PCR_TEST = LocalDate.of(2020, 1, 1);
	public static final LocalDate MIN_DATE_FOR_GEBURTSDATUM = LocalDate.of(1900, 1, 1);
	public static final LocalDateTime DATE_ARCHIVIERT_OHNE_VACME_IMPFUNG = LocalDate.of(1900, 1, 1).atStartOfDay();
	public static final int MAX_NAME_LENGTH_COVIDCERT = 80;

	public static final int SWISS_PLZ_LENGTH = 4;

	public static final String GENERAL_INFOTEXT_DE = "GENERAL_INFOTEXT_DE";
	public static final String GENERAL_INFOTEXT_FR = "GENERAL_INFOTEXT_FR";
	public static final String GENERAL_INFOTEXT_EN = "GENERAL_INFOTEXT_EN";

	private Constants() {
	}
}
