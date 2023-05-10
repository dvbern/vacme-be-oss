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

package ch.dvbern.oss.vacme.service.plz;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.oss.vacme.entities.plz.PLZData;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
@Slf4j
public class PLZImportService {

	private static final String[] HEADERS = { "REC_ART", "ONRP", "BFSNR", "PLZ_TYP", "POSTLEITZAHL", "PLZ_ZZ", "GPLZ", "ORTBEZ18",
		"ORTBEZ27", "KANTON", "SPRACHCODE", "SPRACHCODE_ ABW", "BRIEFZ_DURCH", "GILT_AB_DAT", "PLZ_BRIEFZUST",
		"PLZ_COFF", "Geo Shape", "Geokoordinaten"
	};

	/**
	 * Dieses File wurde von https://swisspost.opendatasoft.com/explore/dataset/plz_verzeichnis_v2/table/ heruntergeladen. Die Spalten Geo Shape und Geokoordinaten wurden geleert
	 */
	public static final String DEFAULT_PLZ_FILE_PATH = "/plz/plz_verzeichnis_v2_nogeo.csv";

	public PLZImportService() {
	}

	/**
	 * importiert das mit der App mitdeployte File in eine Liste von DTOs
	 * @return Liste von DTOs mit PLZ angaben
	 */
	public List<PLZData> impportDefaultPlzDataDtoFromCSV() {
		try (InputStream inputStreamOfCsv = PLZImportService.class.getResourceAsStream(DEFAULT_PLZ_FILE_PATH)) {
			return this.impportPlzDataDtoFromCSV(inputStreamOfCsv);
		} catch (IOException e) {
			throw new AppFailureException("Problem importing PLZ CSV File", e);
		}
	}

	public List<PLZData> impportPlzDataDtoFromCSV(InputStream inputStreamOfCsv){

		List<PLZData> result = new ArrayList<>();
		try {InputStreamReader in = new InputStreamReader(inputStreamOfCsv, StandardCharsets.UTF_8);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT
			.withHeader(HEADERS)
			.withDelimiter(';')
			.withFirstRecordAsHeader()
			.parse(in);

			for (CSVRecord record : records) {
				PLZData plzData = recordToPlzDto(record);
				result.add(plzData);
			}

		} catch (IOException e) {
			throw new AppFailureException("Problem importing PLZ CSV File", e);
		}
		return result;
	}

	@NotNull
	private PLZData recordToPlzDto(CSVRecord record) {
		String id = record.get("ONRP");
		String plz = record.get("POSTLEITZAHL");
		String kanton = record.get("KANTON");
		String name = record.get("ORTBEZ18");

		Validate.notEmpty(id, "Invalid data in CSV, got empty string while readind ONRP");
		Validate.notEmpty(plz, "Invalid data in CSV, got empty string while readind PLZ");
		Validate.notEmpty(kanton, "Invalid data in CSV, got empty string while readind Kanton");

		return new PLZData(id, plz, name, kanton);
	}
}
