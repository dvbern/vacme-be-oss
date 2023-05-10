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
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.oss.vacme.entities.plz.PLZMedstat;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
@Slf4j
public class PLZMedstatImportService {

	private static final String[] HEADERS = { "NPA/PLZ", "PLZ_NAME", "NAME", "KT", "MedStat", "empty", "empty2" };

	/**
	 * Dieses File wurde aus dem Excel-File do-b-14.04.01.02-geocod-03.xlsx exportiert.
	 * Die Spaltenueberschriften "empty"/"empty2" wurden hinzugefuegt, damit der Rest des Files unveraendert uebernommen werden konnte.
	 */
	public static final String DEFAULT_PLZ_FILE_PATH = "/plz/plz_medstat.csv";

	public PLZMedstatImportService() {
	}

	/**
	 * importiert das mit der App mitdeployte File in eine Liste von DTOs
	 *
	 * @return Liste von DTOs mit PLZ angaben
	 */
	@NonNull
	public List<PLZMedstat> impportDefaultPlzDataDtoFromCSV() {
		try (InputStream inputStreamOfCsv = PLZMedstatImportService.class.getResourceAsStream(DEFAULT_PLZ_FILE_PATH)) {
			Objects.requireNonNull(inputStreamOfCsv);
			return this.impportPlzDataDtoFromCSV(inputStreamOfCsv);
		} catch (IOException e) {
			throw new AppFailureException("Problem importing PLZ CSV File", e);
		}
	}

	@NonNull
	public List<PLZMedstat> impportPlzDataDtoFromCSV(@NonNull InputStream inputStreamOfCsv) {
		List<PLZMedstat> result = new ArrayList<>();
		try {
			InputStreamReader in = new InputStreamReader(inputStreamOfCsv, StandardCharsets.UTF_8);
			Iterable<CSVRecord> records = CSVFormat.DEFAULT
				.withHeader(HEADERS)
				.withDelimiter(';')
				.withFirstRecordAsHeader()
				.parse(in);

			for (CSVRecord record : records) {
				PLZMedstat plzData = recordToPlzDto(record);
				result.add(plzData);
			}

		} catch (IOException e) {
			throw new AppFailureException("Problem importing PLZ CSV File", e);
		}
		return result;
	}

	@NotNull
	private PLZMedstat recordToPlzDto(@NonNull CSVRecord record) {
		String plz = record.get("NPA/PLZ");
		String kanton = record.get("KT");
		String medstat = record.get("MedStat");

		Validate.notEmpty(plz, "Invalid data in CSV, got empty string while readind PLZ");
		Validate.notEmpty(medstat, "Invalid data in CSV, got empty string while readind MedStat");
		Validate.notEmpty(kanton, "Invalid data in CSV, got empty string while readind Kanton");

		return new PLZMedstat(plz, kanton, medstat);
	}
}
