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

package ch.dvbern.oss.vacme.reports.reportingImpfungen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.core.StreamingOutput;

import ch.dvbern.oss.vacme.entities.statistik.StatistikReportingImpfungenDTO;
import ch.dvbern.oss.vacme.reports.AbstractReportServiceBean;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.smartdb.Db;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.checkerframework.checker.nullness.qual.NonNull;

@Transactional(TxType.SUPPORTS)
@ApplicationScoped
@Slf4j
public class ReportingImpfungenReportServiceBean extends AbstractReportServiceBean {

	private final String impfungen1Query = "reportingImpfungen1Query.sql";
	private final String impfungen2Query = "reportingImpfungen2Query.sql";
	private final String impfungenNQuery = "reportingImpfungenNQuery.sql";

	private final Db db;

	@Inject
	public ReportingImpfungenReportServiceBean(@NonNull Db db) {
		this.db = db;
	}

	/**
	 * @return query aus Filesystem lesen
	 */
	private String loadStatisticsQuery(String queryFilename) {
		InputStream inputStream = ReportingImpfungenReportServiceBean.class.getResourceAsStream(queryFilename);
		Objects.requireNonNull(inputStream);

		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			var sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		} catch (IOException e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query for statistics from file " + queryFilename);
		}
	}

	/**
	 * @return Statistik als CSV Download
	 */
	@Transactional(TxType.SUPPORTS)
	public StreamingOutput generateStatisticsExport() {
		return output -> {
			LOG.info("Starting export of Statistics Impfungen CSV");
			try (
				Writer writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
				CSVPrinter printer = CSVFormat.DEFAULT.withHeader(StatistikReportingImpfungenDTO.CSV_HEADER).print(writer);
			) {
				// to improve: it might be more memory efficent to read the query results orderd and paged instead of all at once
				// however then we would also need to write out sequentially probably)
				runStatisticsQueryAsStream(impfungen1Query).forEach(statistikDTO -> {
					try {
						printer.printRecord((Object[]) statistikDTO.getFieldList());
						printer.flush();
					} catch (IOException e) {
						LOG.error("Could not create Statistics Report Impfungen ", e);
						throw AppValidationMessage.ILLEGAL_STATE.create("Could not generate report", e);
					}
				});
				runStatisticsQueryAsStream(impfungen2Query).forEach(statistikDTO -> {
					try {
						printer.printRecord((Object[]) statistikDTO.getFieldList());
						printer.flush();
					} catch (IOException e) {
						LOG.error("Could not create Statistics Report Impfungen ", e);
						throw AppValidationMessage.ILLEGAL_STATE.create("Could not generate report", e);
					}
				});
				runStatisticsQueryAsStream(impfungenNQuery).forEach(statistikDTO -> {
					try {
						printer.printRecord((Object[]) statistikDTO.getFieldList());
						printer.flush();
					} catch (IOException e) {
						LOG.error("Could not create Statistics Report Impfungen ", e);
						throw AppValidationMessage.ILLEGAL_STATE.create("Could not generate Impfungen report", e);
					}
				});
				LOG.info("... Reporting Impfungen CSV beendet");
			} catch (IOException e) {
				LOG.error("Could not create Statistics Report ", e);
				throw AppValidationMessage.ILLEGAL_STATE.create("Could not generate Impfungen report", e);
			}
		};
	}

	private Stream<StatistikReportingImpfungenDTO> runStatisticsQueryAsStream(String queryFilename) {
		Query nativeQuery = this.db.getEntityManager().createNativeQuery(loadStatisticsQuery(queryFilename), Constants.REPORTING_IMPFUNGEN_DTO_MAPPING);
		return  nativeQuery.getResultStream();
	}
}
