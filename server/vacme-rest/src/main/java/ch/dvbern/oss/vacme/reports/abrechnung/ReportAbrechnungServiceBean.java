
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

package ch.dvbern.oss.vacme.reports.abrechnung;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import ch.dvbern.oss.vacme.repo.AbrechnungRepo;
import ch.dvbern.oss.vacme.reports.AbstractReportServiceBean;
import ch.dvbern.oss.vacme.reports.ReportVorlage;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

@Transactional
@ApplicationScoped
@Slf4j
public class ReportAbrechnungServiceBean extends AbstractReportServiceBean {

	private final AbrechnungRepo abrechnungRepo;
	private final AbrechnungExcelConverter excelConverter;

	@Inject
	public ReportAbrechnungServiceBean(
		@Nonnull AbrechnungRepo abrechnungRepo,
		@Nonnull AbrechnungExcelConverter excelConverter
	) {
		this.abrechnungRepo = abrechnungRepo;
		this.excelConverter = excelConverter;
	}

	@Nonnull
	@Transactional(TxType.NOT_SUPPORTED)
	public byte[] generateExcelReportAbrechnung(@Nonnull Locale locale, @Nonnull LocalDate dateVon, @Nonnull LocalDate dateBis)  {
		return generateExcelReportAbrechnung(
			locale,
			dateVon,
			dateBis,
			ReportVorlage.VORLAGE_REPORT_ABRECHNUNG);
	}

	@Nonnull
	@Transactional(TxType.NOT_SUPPORTED)
	public byte[] generateExcelReportAbrechnungErwachsen(@Nonnull Locale locale, @Nonnull LocalDate dateVon, @Nonnull LocalDate dateBis)  {
		return generateExcelReportAbrechnung(
			locale,
			dateVon,
			dateBis,
			ReportVorlage.VORLAGE_REPORT_ABRECHNUNG_ERWACHSEN);
	}

	@Nonnull
	@Transactional(TxType.NOT_SUPPORTED)
	public byte[] generateExcelReportAbrechnungKind(@Nonnull Locale locale, @Nonnull LocalDate dateVon, @Nonnull LocalDate dateBis)  {
		return generateExcelReportAbrechnung(
			locale,
			dateVon,
			dateBis,
			ReportVorlage.VORLAGE_REPORT_ABRECHNUNG_KIND);
	}

	@Nonnull
	private byte[] generateExcelReportAbrechnung(@Nonnull Locale locale, @Nonnull LocalDate dateVon, @Nonnull LocalDate dateBis,
												 ReportVorlage reportVorlage) {

		InputStream is = ReportAbrechnungServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		Objects.requireNonNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		List<AbrechnungDataRow> abrechnung = abrechnungRepo.findOdiAbrechnung(dateVon, dateBis, reportVorlage.getDefaultExportFilename());
		ExcelMergerDTO excelMergerDTO = excelConverter.toExcelMergerDTO(abrechnung, dateVon, dateBis, locale);
		excelConverter.applyAutoSize(sheet);

		mergeFields(reportVorlage, sheet, excelMergerDTO);
		return createWorkbook(workbook);
	}

	private void mergeFields(@Nonnull ReportVorlage reportVorlage, @Nonnull Sheet sheet, @Nonnull ExcelMergerDTO excelMergerDTO)  {
		try {
			mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		} catch (ExcelMergeException exception) {
			LOG.error("Could not generate Excel 'Abrechnung'", exception);
			throw new AppFailureException("Could not generate Excel Abrechnung", exception);
		}
	}
}
