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
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.base.ImpfInfo;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import ch.dvbern.oss.vacme.jax.OdiTerminbuchungenDataRow;
import ch.dvbern.oss.vacme.repo.OrtDerImpfungRepo;
import ch.dvbern.oss.vacme.reports.AbstractReportServiceBean;
import ch.dvbern.oss.vacme.reports.ReportVorlage;
import ch.dvbern.oss.vacme.service.BenutzerService;
import ch.dvbern.oss.vacme.service.ExternesZertifikatService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

@Transactional(TxType.SUPPORTS)
@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ReportingOdiTerminbuchungenReportServiceBean extends AbstractReportServiceBean {

	private final BenutzerService benutzerService;
	private final OrtDerImpfungRepo ortDerImpfungRepo;
	private final OdiImpfungenExcelConverter excelConverter;
	private final ImpfinformationenService impfinformationenService;
	private final ExternesZertifikatService externesZertifikatService;

	@Nonnull
	@Transactional(TxType.SUPPORTS)
	public byte[] generateExcelReportOdiTerminbuchungen(@Nonnull Locale locale, @NonNull ID<Benutzer> requestingBenutzerId) {

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

			final ReportVorlage reportResource = ReportVorlage.VORLAGE_REPORT_ODI_TERMINBUCHUNGEN;

			InputStream is =
				ReportingOdiTerminbuchungenReportServiceBean.class.getResourceAsStream(reportResource.getTemplatePath());
			requireNonNull(is, VORLAGE + reportResource.getTemplatePath() + NICHT_GEFUNDEN);

			Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
			Sheet sheet = workbook.getSheet(reportResource.getDataSheetName());

			List<OdiTerminbuchungenDataRow> reportData = ortDerImpfungRepo.getOdiTerminbuchungenReport(allowedUUIDs);

			StopWatch stopwatchEnhanceReportwithLastImpfung = StopWatch.createStarted();

			reportData.forEach(dataRow -> {
				Objects.requireNonNull(dataRow.getImpffolgeNr());
				Objects.requireNonNull(dataRow.getRegistrierungsnummer());
				if (dataRow.getImpffolgeNr() != 1 && dataRow.getLetzteImpfungName() == null) {
					ImpfinformationDto impfinformationDto =
						impfinformationenService.getImpfinformationen(
							dataRow.getRegistrierungsnummer(),
							KrankheitIdentifier.valueOf(dataRow.getKrankheit()));

					// Includes Vacme and Externe Impfungen
					ImpfInfo letzteImpfung = getLetzteImpfung(impfinformationDto);

					dataRow.setLetzteImpfung(letzteImpfung);
				}
			});

			logIfSlow("Finding the last Impfung (timestamp and Impfstoff) for TerminbuchungenReport",  stopwatchEnhanceReportwithLastImpfung, reportData.size());

			if (reportData.isEmpty()) {
				addEmptyDataRow(reportData, locale);
			}

			StopWatch stopwatchMergeToXSSF = StopWatch.createStarted();
			final XSSFSheet xsslSheet =
				(XSSFSheet) excelConverter.mergeHeaderFieldsStichtag(
					sheet,
					locale);

			RowFiller rowFiller = RowFiller.initRowFiller(
				xsslSheet,
				MergeFieldProvider.toMergeFields(reportResource.getMergeFields()),
				reportData.size());

			excelConverter.mergeTerminbuchungenRows(rowFiller, reportData);

			excelConverter.applyAutoSize(sheet);

			logIfSlow("Merging to TerminbuchungReport Excel", stopwatchMergeToXSSF, reportData.size());
			final byte[] content = createWorkbook(rowFiller.getSheet().getWorkbook());

			// dispose of temporary files backing this workbook on disk
			rowFiller.getSheet().getWorkbook().dispose();

			return content;

		} catch (ExcelMergeException exception) {
			LOG.error("VACME-REPORTING: Could not generate Excel 'OdiTerminbuchungReport'", exception);
			throw new AppFailureException("Could not generate Excel OdiTerminbuchungReport", exception);
		}
	}

	private void addEmptyDataRow(List<OdiTerminbuchungenDataRow> reportData, Locale locale) {
		OrtDerImpfung odi = TestdataCreationUtil.createOrtDerImpfung();
		odi.setName("-");
		odi.setGlnNummer("-");
		Impftermin impftermin = TestdataCreationUtil.createImpftermin(odi, LocalDate.EPOCH);
		Fragebogen fragebogen = TestdataCreationUtil.createFragebogen();
		Registrierung registrierung = fragebogen.getRegistrierung();
		registrierung.setRegistrierungsnummer(ServerMessageUtil.getMessage("Reports_noContent", locale));
		registrierung.setName("-");
		registrierung.setVorname("-");
		registrierung.setGeburtsdatum(LocalDate.EPOCH);
		Impfdossier impfdossier = new Impfdossier();
		impfdossier.setRegistrierung(registrierung);
		reportData.add(new OdiTerminbuchungenDataRow(impftermin.getImpfslot(), odi, impftermin, impfdossier, 1));
	}

	private void logIfSlow(@NonNull String msg, @NonNull StopWatch stopwatch, int resultCnt) {
		stopwatch.stop();
		if (stopwatch.getTime(TimeUnit.MILLISECONDS) > OrtDerImpfungRepo.SLOW_THRESHOLD_MS) {
			LOG.warn("VACME-REPORTING: {}. resultcount {}, Time taken {}ms", msg, resultCnt, stopwatch.getTime(TimeUnit.MILLISECONDS));
		}
	}

	@Nullable
	private ImpfInfo getLetzteImpfung(ImpfinformationDto impfinformationDto) {
		Impfung letzteImpfung = ImpfinformationenService.getNewestVacmeImpfung(impfinformationDto);

		// If there is no Vacme Impfung, check ExterneZertifikate
		if (letzteImpfung == null) {
			return externesZertifikatService.findExternesZertifikatForDossier(impfinformationDto.getImpfdossier())
				.orElse(null);
		}

		return letzteImpfung;
	}
}
