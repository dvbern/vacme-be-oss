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

package ch.dvbern.oss.vacme.entities.covidcertificate;

/**
 * See the valid Covid Vaccines at
 * @see "https://github.com/admin-ch/CovidCertificate-Examples/blob/main/valuesets/vaccine-medicinal-product.json"
 * NEU: Branch covid-cert-smoketest
 */
public enum CovidCertificateVaccinesNames {

	PFIZER_BIONTECH(
		"EU/1/20/1528",
		"Comirnaty",
		"en",
		true,
		"https://ec.europa.eu/health/documents/community-register/html/",
		""),

	PFIZER_BIONTECH_BIVALENT(
		"EU/1/20/1528",
		"Comirnaty Bivalent Original/Omicron BA.1®",
		"en",
		true,
		"https://ec.europa.eu/health/documents/community-register/html/",
		""),
	MODERNA(
		"EU/1/20/1507",
		"COVID-19 Vaccine Moderna",
		"en",
		true,
		"https://ec.europa.eu/health/documents/community-register/html/",
		""),

	MODERNA_BIVALENT(
		"EU/1/20/1507",
		"Spikevax® Bivalent Original / Omicron",
		"en",
		true,
		"https://ec.europa.eu/health/documents/community-register/html/",
		""),

	EU_1_21_1529(
		"EU/1/21/1529",
		"Vaxzevria",
		"en",
		true,
		"https://ec.europa.eu/health/documents/community-register/html/",
		""),

	EU_1_20_1525(
		"EU/1/20/1525",
		"COVID-19 Vaccine Janssen",
		"en",
		true,
		"https://ec.europa.eu/health/documents/community-register/html/",
		""),

	CVnCoV(
		"CVnCoV",
		"CVnCoV",
		"en",
		true,
		"http://ec.europa.eu/temp/vaccineproductname",
		"1.0"),

	Sputnik_V(
		"Sputnik-V",
		"Sputnik-V",
		"en",
		true,
		"http://ec.europa.eu/temp/vaccineproductname",
		"1.0"),

	Convidecia(
		"Convidecia",
		"Convidecia",
		"en",
		true,
		"http://ec.europa.eu/temp/vaccineproductname",
		"1.0"),

	EpiVacCorona(
		"EpiVacCorona",
		"EpiVacCorona",
		"en",
		true,
		"http://ec.europa.eu/temp/vaccineproductname",
		"1.0"),

	BBIBP_CorV(
		"BBIBP-CorV",
		"BBIBP-CorV",
		"en",
		true,
		"http://ec.europa.eu/temp/vaccineproductname",
		"1.0"),

	Inactivated_SARS_CoV_2_Vero_Cell(
		"Inactivated-SARS-CoV-2-Vero-Cell",
		"Inactivated SARS-CoV-2 (Vero Cell)",
		"en",
		true,
		"http://ec.europa.eu/temp/vaccineproductname",
		"1.0"),

	CoronaVac(
		"CoronaVac",
		"CoronaVac",
		"en",
		true,
		"http://ec.europa.eu/temp/vaccineproductname",
		"1.0"),

	Covaxin(
		"Covaxin",
		"Covaxin (also known as BBV152 A, B, C)",
		"en",
		true,
		"http://ec.europa.eu/temp/vaccineproductname",
		"1.0"),

	Novavax(
		"EU/1/21/1618",
		"Nuvaxovid",
		"en",
		true,
		"http://ec.europa.eu/temp/vaccineproductname",
		"1.0"),

	Covishield(
		"Covishield",
		"Covishield (ChAdOx1_nCoV-19)",
		"en",
		true,
		"http://ec.europa.eu/temp/vaccineproductname",
		"1.0"),

	COVOVAX(
		"COVOVAX",
		"COVOVAX (Novavax formulation)",
		"en",
		true,
		"http://ec.europa.eu/temp/vaccineproductname",
		"1.0");

	private String code;
	private String display;
	private String lang;
	private boolean active;
	private String system;
	private String version;

	CovidCertificateVaccinesNames(String code, String display, String lang, boolean active, String system,
		String version) {
		this.code = code;
		this.display = display;
		this.lang = lang;
		this.active = active;
		this.system = system;
		this.version = version;
	}

	public String getCode() {
		return code;
	}
}
