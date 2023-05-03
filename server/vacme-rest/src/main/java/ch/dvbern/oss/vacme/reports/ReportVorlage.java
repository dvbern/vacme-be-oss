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

package ch.dvbern.oss.vacme.reports;

import javax.annotation.Nonnull;

import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import ch.dvbern.oss.vacme.enums.FileNameEnum;
import ch.dvbern.oss.vacme.reports.abrechnung.MergeFieldsAbrechnung;
import ch.dvbern.oss.vacme.reports.abrechnungZH.MergeFieldsAbrechnungZH;
import ch.dvbern.oss.vacme.reports.reportingOdiImpfungenTerminbuchungen.MergeFieldsOdiImpfungenTerminbuchungen;

/**
 * Enum fuer ReportVorlage
 */
public enum ReportVorlage {

	VORLAGE_REPORT_ABRECHNUNG(
		"/reports/Abrechnung.xlsx",
		FileNameEnum.ABRECHNUNG,
		"Data",
		MergeFieldsAbrechnung.class
	),
	VORLAGE_REPORT_ABRECHNUNG_ERWACHSEN(
		"/reports/AbrechnungErwachsen.xlsx",
		FileNameEnum.ABRECHNUNG_ERWACHSEN,
		"Data",
		MergeFieldsAbrechnung.class
	),
	VORLAGE_REPORT_ABRECHNUNG_KIND(
		"/reports/AbrechnungKind.xlsx",
		FileNameEnum.ABRECHNUNG_KIND,
		"Data",
		MergeFieldsAbrechnung.class
	),
	VORLAGE_REPORT_ABRECHNUNG_ZH(
		"/reports/AbrechnungZH.xlsx",
		FileNameEnum.ABRECHNUNG_ZH,
		"Data",
		MergeFieldsAbrechnungZH .class
	),
	VORLAGE_REPORT_ABRECHNUNG_ZH_KIND(
		"/reports/AbrechnungZHKind.xlsx",
		FileNameEnum.ABRECHNUNG_ZH_KIND,
		"Data",
		MergeFieldsAbrechnungZH .class
	),
	VORLAGE_REPORT_ODI_IMPFUNGEN(
		"/reports/OdiImpfungen.xlsx",
		FileNameEnum.ODI_IMPFUNGEN,
		"Data",
		MergeFieldsOdiImpfungenTerminbuchungen.class
	),
	VORLAGE_REPORT_ODI_TERMINBUCHUNGEN(
		"/reports/OdiTerminbuchungen.xlsx",
		FileNameEnum.ODI_TERMINBUCHUNGEN,
		"Data",
		MergeFieldsOdiImpfungenTerminbuchungen.class
	);


	@Nonnull
	private final String templatePath;
	@Nonnull
	private final FileNameEnum defaultExportFilename;
	@Nonnull
	private final Class<? extends MergeFieldProvider> mergeFields;
	@Nonnull
	private final String dataSheetName;

	ReportVorlage(@Nonnull String templatePath, @Nonnull FileNameEnum defaultExportFilename,
		@Nonnull String dataSheetName, @Nonnull Class<? extends MergeFieldProvider> mergeFields) {
		this.templatePath = templatePath;
		this.defaultExportFilename = defaultExportFilename;
		this.mergeFields = mergeFields;
		this.dataSheetName = dataSheetName;
	}

	@Nonnull
	public String getTemplatePath() {
		return templatePath;
	}

	@Nonnull
	public MergeFieldProvider[] getMergeFields() {
		return mergeFields.getEnumConstants();
	}

	@Nonnull
	public String getDataSheetName() {
		return dataSheetName;
	}

	@Nonnull
	public FileNameEnum getDefaultExportFilename() {
		return defaultExportFilename;
	}
}
