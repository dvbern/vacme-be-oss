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

package ch.dvbern.oss.vacme.reports.reportingOdis;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.statistik.StatistikReportingOdisDTO;
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
public class ReportingOdisReportServiceBean extends AbstractReportServiceBean {

	private final Db db;

	// Careful, there are potential duplicates in this list if multiple odis have multiple Krankheiten with
	// kantonaleBerechtigung = KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG
	private static final String QUERY = "select "
		+ "name as 'Name', "
		+ "glnNummer as 'GLN', "
		+ "adresse1 as 'Adresse_1', "
		+ "adresse2 as 'Adresse_2', "
		+ "plz as 'PLZ', "
		+ "ort as 'Ort', "
		+ "ODI.identifier as 'Identifier', "
		+ "CASE WHEN mobilerOrtDerImpfung = 1 THEN '1' ELSE '0' END as 'Mobil', "
		+ "CASE WHEN oeffentlich = 1 THEN '1' ELSE '0' END as 'Oeffentlich', "
		+ "CASE WHEN terminverwaltung = 1 THEN '1' ELSE '0' END as 'Terminverwaltung', "
		+ "typ as 'Typ', "
		+ "CASE WHEN deaktiviert = 1 THEN 'TRUE' ELSE 'FALSE' END as 'Deaktiviert', "
		+ "zsrNummer as 'ZSR' "
		+ "from OrtDerImpfung ODI "
		+ "left join OrtDerImpfung_Krankheit ODIK on ODI.id = ODIK.ortDerImpfung_id "
		+ "left join Krankheit K on ODIK.krankheit_id = K.id "
		+ "where K.kantonaleBerechtigung = 'KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG';";

	@Inject
	public ReportingOdisReportServiceBean(@NonNull Db db) {
		this.db = db;
	}

	/**
	 * @return Statistik mit Angaben zu den OdI als CSV Download
	 */
	@Transactional(TxType.SUPPORTS)
	public byte[] generateStatisticsExport() {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (
			Writer writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8));
			CSVPrinter printer = CSVFormat.DEFAULT.withHeader(StatistikReportingOdisDTO.CSV_HEADER).print(writer)) {
			// to improve: it might be more memory efficent to read the query results orderd and paged instead of all at once
			// however then we would also need to write out sequentially probably)
			runStatisticsQueryAsStream().forEach(statistikDTO -> {
				try {
					printer.printRecord((Object[]) statistikDTO.getFieldList());
				} catch (IOException e) {
					LOG.error("Could not create Statistics Report ", e);
					throw AppValidationMessage.ILLEGAL_STATE.create("Could not generate report");
				}
			});
			printer.flush();

		} catch (IOException e) {
			LOG.error("Could not create Statistics Report ", e);
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not generate report");
		}
		return byteArrayOutputStream.toByteArray();
	}

	private Stream<StatistikReportingOdisDTO> runStatisticsQueryAsStream() {
		Query nativeQuery = this.db.getEntityManager().createNativeQuery(QUERY, Constants.STATISTIK_ODIS_DTO_MAPPING);
		return  nativeQuery.getResultStream();
	}
}
