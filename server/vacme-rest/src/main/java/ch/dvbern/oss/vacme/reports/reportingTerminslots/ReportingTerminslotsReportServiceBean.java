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

package ch.dvbern.oss.vacme.reports.reportingTerminslots;

import java.io.BufferedWriter;
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
import javax.ws.rs.core.StreamingOutput;

import ch.dvbern.oss.vacme.entities.statistik.StatistikReportingTerminslotsDTO;
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
public class ReportingTerminslotsReportServiceBean extends AbstractReportServiceBean {

	private final Db db;

	// Careful, there are potential duplicates in this list if multiple odis have multiple Krankheiten with
	// kantonaleBerechtigung = KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG
	private static final String QUERY = "select o.name as 'Ort_der_Impfung_Name', "
		+ "o.glnNummer as 'Ort_der_Impfung_GLN', "
		+ "i.kapazitaetErsteImpfung as 'Slot_Kapazitaet_Impfung_1', "
		+ "i.kapazitaetZweiteImpfung as 'Slot_Kapazitaet_Impfung_2', "
		+ "i.kapazitaetBoosterImpfung as 'Slot_Kapazitaet_Impfung_N', "
		+ "date_format(i.von, '%d.%m.%Y') as 'Slot_Datum', "
		+ "date_format(i.von, '%T') as 'Slot_Von', "
		+ "date_format(i.bis, '%T') as 'Slot_Bis' "
		+ "from Impfslot i "
		+ "left join OrtDerImpfung o " // left join ist viel schneller als inner join. Dafuer kann man nicht nach odi sortieren, aber das ist nicht schlimm.
		+ "on i.ortDerImpfung_id = o.id "
		+ "left join Krankheit K on i.krankheitIdentifier = K.identifier "
		+ "where K.kantonaleBerechtigung = 'KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG';";

	@Inject
	public ReportingTerminslotsReportServiceBean(@NonNull Db db) {
		this.db = db;
	}

	/**
	 * @return Statistik mit Angaben zu den Slots als CSV
	 */
	@Transactional(TxType.SUPPORTS)
	public StreamingOutput generateStatisticsExport() {
		return output -> {
			LOG.info("Starting export of Impfslot statistics CSV");
			try (
				Writer writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
				CSVPrinter printer = CSVFormat.DEFAULT.withHeader(StatistikReportingTerminslotsDTO.CSV_HEADER).print(writer);
			) {
				// to improve: it might be more memory efficent to read the query results orderd and paged instead of all at once
				// however then we would also need to write out sequentially probably)
				runStatisticsQueryAsStream().forEach(statistikDTO -> {
					try {
						printer.printRecord((Object[]) statistikDTO.getFieldList());
						printer.flush();
					} catch (IOException e) {
						LOG.error("Could not create Statistics Report ", e);
						throw AppValidationMessage.ILLEGAL_STATE.create("Could not generate report");
					}
				});
				LOG.info("... Reporting TerminslotCSV beendet");
			} catch (IOException e) {
				LOG.error("Could not create Statistics Report ", e);
				throw AppValidationMessage.ILLEGAL_STATE.create("Could not generate report");
			}
		};
	}

	private Stream<StatistikReportingTerminslotsDTO> runStatisticsQueryAsStream() {
		Query nativeQuery = this.db.getEntityManager().createNativeQuery(QUERY, Constants.STATISTIK_TERMINSLOTS_DTO_MAPPING);
		return  nativeQuery.getResultStream();
	}
}
