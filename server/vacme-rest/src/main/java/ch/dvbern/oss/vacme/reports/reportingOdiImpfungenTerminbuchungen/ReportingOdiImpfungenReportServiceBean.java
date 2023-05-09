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

package ch.dvbern.oss.vacme.reports.reportingOdiImpfungenTerminbuchungen;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.RowFiller;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.jax.OdiImpfungenDataRow;
import ch.dvbern.oss.vacme.repo.OrtDerImpfungRepo;
import ch.dvbern.oss.vacme.reports.AbstractReportServiceBean;
import ch.dvbern.oss.vacme.reports.ReportVorlage;
import ch.dvbern.oss.vacme.service.BenutzerService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Objects.requireNonNull;

@Transactional(TxType.SUPPORTS)
@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ReportingOdiImpfungenReportServiceBean extends AbstractReportServiceBean {

	private final UserPrincipal userPrincipal;
	private final OrtDerImpfungRepo ortDerImpfungRepo;
	private final OdiImpfungenExcelConverter excelConverter;

	private final BenutzerService benutzerService;

	@Nonnull
	@Transactional(TxType.SUPPORTS)
	public byte[] generateExcelReportOdiImpfungen(@Nonnull Locale locale, @NonNull ID<Benutzer> requestingBenutzerId) {

		try {
			Validate.notNull(requestingBenutzerId, "requestingBenutzer must be set for async report generation");
			Set<OrtDerImpfung> allowedOdis = benutzerService.getOdisOfBenutzer(requestingBenutzerId);
			if (allowedOdis.isEmpty()) {
				LOG.warn("VACME-REPORTING: User with id {} has no associated odis", requestingBenutzerId.toString());
			}
			final List<UUID> allowedUUIDs = allowedOdis
				.stream()
				.map(AbstractUUIDEntity::getId)
				.collect(Collectors.toList());

			final ReportVorlage reportResource = ReportVorlage.VORLAGE_REPORT_ODI_IMPFUNGEN;

			InputStream is = ReportingOdiImpfungenReportServiceBean.class.getResourceAsStream(reportResource.getTemplatePath());
			requireNonNull(is, VORLAGE + reportResource.getTemplatePath() + NICHT_GEFUNDEN);

			Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
			Sheet sheet = workbook.getSheet(reportResource.getDataSheetName());

			List<OdiImpfungenDataRow> reportData = ortDerImpfungRepo.getOdiImpfungenReport(allowedUUIDs);
			if (reportData.isEmpty()) {
				throw AppValidationMessage.REPORT_EMPTY.create();
			}

			final XSSFSheet xsslSheet =
				(XSSFSheet) excelConverter.mergeHeaderFieldsStichtag(sheet, locale);

			RowFiller rowFiller = RowFiller.initRowFiller(
				xsslSheet,
				MergeFieldProvider.toMergeFields(reportResource.getMergeFields()),
				reportData.size());

			excelConverter.mergeImpfungenRows(rowFiller, reportData);

			excelConverter.applyAutoSize(sheet);

			final byte[] content = createWorkbook(rowFiller.getSheet().getWorkbook());

			// dispose of temporary files backing this workbook on disk
			rowFiller.getSheet().getWorkbook().dispose();

			return content;

		} catch (ExcelMergeException exception) {
			LOG.error("VACME-REPORTING: Could not generate Excel 'OdiImpfungenReport'", exception);
			throw new AppFailureException("Could not generate Excel OdiImpfungenReport", exception);
		}
	}
}
