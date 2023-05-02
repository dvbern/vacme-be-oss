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

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import org.apache.poi.ss.usermodel.Sheet;

@Dependent
public class AbrechnungExcelConverter implements ExcelConverter {
	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<? extends AbrechnungDataRow> abrechnung,
		@Nonnull LocalDate dateVon,
		@Nonnull LocalDate dateBis,
		@Nonnull Locale locale
	) {
		ExcelMergerDTO mergerDTO = new ExcelMergerDTO();

		mergerDTO.addValue(MergeFieldsAbrechnung.reportTitle, ServerMessageUtil.getMessage("Reports_abrechnung_reportTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.reportErwachsenTitle, ServerMessageUtil.getMessage("Reports_abrechnung_reportErwachsenTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.reportKindTitle, ServerMessageUtil.getMessage("Reports_abrechnung_reportKindTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.kantonKey, ServerMessageUtil.getMessage("Reports_abrechnung_kanton", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.kanton, ServerMessageUtil.getMessage("Reports_abrechnung_kanton_name", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.vonKey, ServerMessageUtil.getMessage("Reports_abrechnung_von", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.bisKey, ServerMessageUtil.getMessage("Reports_abrechnung_bis", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.von, dateVon);
		mergerDTO.addValue(MergeFieldsAbrechnung.bis, dateBis);

		mergerDTO.addValue(MergeFieldsAbrechnung.impfortTyp, ServerMessageUtil.getMessage("Reports_abrechnung_impfortTyp", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.pauschaleOKPKey, ServerMessageUtil.getMessage("Reports_abrechnung_pauschaleOKP", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.pauschaleRestKey, ServerMessageUtil.getMessage("Reports_abrechnung_pauschaleRest", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.ImpfortTypIMPFZENTRUM, ServerMessageUtil.getMessage("OrtDerImpfungTyp_IMPFZENTRUM", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.ImpfortTypHAUSARZT, ServerMessageUtil.getMessage("OrtDerImpfungTyp_HAUSARZT", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.ImpfortTypALTERSHEIM, ServerMessageUtil.getMessage("OrtDerImpfungTyp_ALTERSHEIM", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.ImpfortTypAPOTHEKE, ServerMessageUtil.getMessage("OrtDerImpfungTyp_APOTHEKE", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.ImpfortTypMOBIL, ServerMessageUtil.getMessage("OrtDerImpfungTyp_MOBIL", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.ImpfortTypSPITAL, ServerMessageUtil.getMessage("OrtDerImpfungTyp_SPITAL", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.ImpfortTypANDERE, ServerMessageUtil.getMessage("OrtDerImpfungTyp_ANDERE", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.ImpfortTypSELBSTZAHLENDE, ServerMessageUtil.getMessage("OrtDerImpfungTyp_SELBSTZAHLENDE", locale));

		mergerDTO.addValue(MergeFieldsAbrechnung.impfort, ServerMessageUtil.getMessage("Reports_abrechnung_impfort", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.verantwortlicherGlnKey, ServerMessageUtil.getMessage("Reports_abrechnung_verantwortlicherGlnKey", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.odiNameKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_name", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.adresse1Key, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_adresse1", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.adresse2Key, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_adresse2", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.PLZKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_plz", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.ortKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_ort", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.ZSRKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_zsrNr", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.IBANKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_IBAN", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.GLNKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_gln", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.TypKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_typ", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.pauschaleODIOKPKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_pauschaleOKP", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.pauschaleODIRestKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_pauschaleRest", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.anzahlImpfung, ServerMessageUtil.getMessage("Reports_abrechnung_impfungen", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.selbstzahlendeKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfungen_selbstzahlende", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.OKPKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfungen_okp", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.AuslandKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfungen_ausland", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.EDAKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfungen_eda", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.andereKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfungen_andere", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.verguetung, ServerMessageUtil.getMessage("Reports_abrechnung_verguetung", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.totalKey, ServerMessageUtil.getMessage("Reports_abrechnung_verguetung_total", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.fachverantwortung, ServerMessageUtil.getMessage("Reports_abrechnung_fachverantwortung", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.fachNameKey, ServerMessageUtil.getMessage("Reports_abrechnung_fachverantwortung_name", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.fachVornameKey, ServerMessageUtil.getMessage("Reports_abrechnung_fachverantwortung_vorname", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.fachMailKey, ServerMessageUtil.getMessage("Reports_abrechnung_fachverantwortung_email", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.fachGLNKey, ServerMessageUtil.getMessage("Reports_abrechnung_fachverantwortung_gln", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.organisationsverantwortung, ServerMessageUtil.getMessage("Reports_abrechnung_organisationsverantwortung", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.orgaVornameKey, ServerMessageUtil.getMessage("Reports_abrechnung_organisationsverantwortung_vorname", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.orgaNameKey, ServerMessageUtil.getMessage("Reports_abrechnung_organisationsverantwortung_name", locale));
		mergerDTO.addValue(MergeFieldsAbrechnung.orgaMailKey, ServerMessageUtil.getMessage("Reports_abrechnung_organisationsverantwortung_email", locale));

		abrechnung.forEach(dataRow -> fillDataRow(dataRow, mergerDTO, locale));
		return mergerDTO;
	}

	public void fillDataRow(AbrechnungDataRow dataRow, ExcelMergerDTO mergerDTO, Locale locale) {
		ExcelMergerDTO excelRowGroup = mergerDTO.createGroup(MergeFieldsAbrechnung.repeatRow);
		final OrtDerImpfung ortDerImpfung = dataRow.getOrtDerImpfung();
		if (ortDerImpfung != null) {
			excelRowGroup.addValue(MergeFieldsAbrechnung.odiName, ortDerImpfung.getName());
			excelRowGroup.addValue(MergeFieldsAbrechnung.verantwortlicherGln, dataRow.getVerantwortlicherGln());
			excelRowGroup.addValue(MergeFieldsAbrechnung.adresse1, ortDerImpfung.getAdresse().getAdresse1());
			excelRowGroup.addValue(MergeFieldsAbrechnung.adresse2, ortDerImpfung.getAdresse().getAdresse2());
			excelRowGroup.addValue(MergeFieldsAbrechnung.PLZ, ortDerImpfung.getAdresse().getPlz());
			excelRowGroup.addValue(MergeFieldsAbrechnung.ort, ortDerImpfung.getAdresse().getOrt());
			excelRowGroup.addValue(MergeFieldsAbrechnung.ZSR, ortDerImpfung.getZsrNummer());
			excelRowGroup.addValue(MergeFieldsAbrechnung.GLN, ortDerImpfung.getGlnNummer());
			excelRowGroup.addValue(MergeFieldsAbrechnung.Typ, ServerMessageUtil.translateEnumValue(ortDerImpfung.getTypForAbrechnungExcel(), locale));
		}
		excelRowGroup.addValue(MergeFieldsAbrechnung.IBAN, "");
		excelRowGroup.addValue(MergeFieldsAbrechnung.selbstzahlende, dataRow.getSelbstzahlendeCount());
		excelRowGroup.addValue(MergeFieldsAbrechnung.OKP, dataRow.getKrankenkasseOKPCount());
		excelRowGroup.addValue(MergeFieldsAbrechnung.Ausland, dataRow.getKrankenkasseAuslandCount());
		excelRowGroup.addValue(MergeFieldsAbrechnung.EDA, dataRow.getKrankenkasseEdaCount());
		excelRowGroup.addValue(MergeFieldsAbrechnung.andere, dataRow.getKrankenkasseAndereCount());

		excelRowGroup.addValue(MergeFieldsAbrechnung.fachName, dataRow.getFvName());
		excelRowGroup.addValue(MergeFieldsAbrechnung.fachVorname, dataRow.getFvVorname());
		excelRowGroup.addValue(MergeFieldsAbrechnung.fachMail, dataRow.getFvMail());
		excelRowGroup.addValue(MergeFieldsAbrechnung.fachGLN, dataRow.getFvGlnNummer());

		excelRowGroup.addValue(MergeFieldsAbrechnung.orgaName, dataRow.getOvName());
		excelRowGroup.addValue(MergeFieldsAbrechnung.orgaVorname, dataRow.getOvVorname());
		excelRowGroup.addValue(MergeFieldsAbrechnung.orgaMail, dataRow.getOvMail());
	}
}
