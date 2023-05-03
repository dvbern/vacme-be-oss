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

package ch.dvbern.oss.vacme.reports.abrechnungZH;

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
import org.checkerframework.checker.nullness.qual.NonNull;

@Dependent
public class AbrechnungZHExcelConverter implements ExcelConverter {
	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<? extends AbrechnungZHDataRow> abrechnung,
		@NonNull LocalDate dateVon,
		@NonNull LocalDate dateBis,
		@Nonnull Locale locale
	) {
		ExcelMergerDTO mergerDTO = new ExcelMergerDTO();

		mergerDTO.addValue(MergeFieldsAbrechnungZH.reportTitle, ServerMessageUtil.getMessage("Reports_abrechnung_reportTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.kantonKey, ServerMessageUtil.getMessage("Reports_abrechnung_kanton", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.kanton, ServerMessageUtil.getMessage("Reports_abrechnung_kanton_name", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.vonKey, ServerMessageUtil.getMessage("Reports_abrechnung_von", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.bisKey, ServerMessageUtil.getMessage("Reports_abrechnung_bis", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.von, dateVon);
		mergerDTO.addValue(MergeFieldsAbrechnungZH.bis, dateBis);

		mergerDTO.addValue(MergeFieldsAbrechnungZH.impfortTyp, ServerMessageUtil.getMessage("Reports_abrechnung_impfortTyp", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.pauschaleOKPKey, ServerMessageUtil.getMessage("Reports_abrechnung_pauschaleOKP", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.pauschaleRestKey, ServerMessageUtil.getMessage("Reports_abrechnung_pauschaleRest", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.ImpfortTypIMPFZENTRUM, ServerMessageUtil.getMessage("OrtDerImpfungTyp_IMPFZENTRUM", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.ImpfortTypHAUSARZT, ServerMessageUtil.getMessage("OrtDerImpfungTyp_HAUSARZT", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.ImpfortTypALTERSHEIM, ServerMessageUtil.getMessage("OrtDerImpfungTyp_ALTERSHEIM", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.ImpfortTypAPOTHEKE, ServerMessageUtil.getMessage("OrtDerImpfungTyp_APOTHEKE", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.ImpfortTypMOBIL, ServerMessageUtil.getMessage("OrtDerImpfungTyp_MOBIL", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.ImpfortTypSPITAL, ServerMessageUtil.getMessage("OrtDerImpfungTyp_SPITAL", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.ImpfortTypANDERE, ServerMessageUtil.getMessage("OrtDerImpfungTyp_ANDERE", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.ImpfortTypSELBSTZAHLENDE, ServerMessageUtil.getMessage("OrtDerImpfungTyp_SELBSTZAHLENDE", locale));

		mergerDTO.addValue(MergeFieldsAbrechnungZH.impfortId, ServerMessageUtil.getMessage("Reports_abrechnung_impfortId", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.impfort, ServerMessageUtil.getMessage("Reports_abrechnung_impfort", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.verantwortlicherGlnKey, ServerMessageUtil.getMessage("Reports_abrechnung_verantwortlicherGlnKey", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.odiNameKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_name", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.odiIdentifierKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_identifier", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.adresse1Key, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_adresse1", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.adresse2Key, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_adresse2", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.PLZKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_plz", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.ortKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_ort", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.ZSRKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_zsrNr", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.IBANKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_IBAN", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.GLNKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_gln", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.TypKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_typ", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.pauschaleODIOKPKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_pauschaleOKP", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.pauschaleODIRestKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfort_pauschaleRest", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.anzahlImpfung, ServerMessageUtil.getMessage("Reports_abrechnung_impfungen", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.selbstzahlendeKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfungen_selbstzahlende", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.OKPKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfungen_okp", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.AuslandKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfungen_ausland", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.EDAKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfungen_eda", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.andereKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfungen_andere", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.verguetung, ServerMessageUtil.getMessage("Reports_abrechnung_verguetung", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.totalKey, ServerMessageUtil.getMessage("Reports_abrechnung_verguetung_total", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.totalImpfungenTitle, ServerMessageUtil.getMessage("Reports_abrechnung_impfungenTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.totalImpfungenKey, ServerMessageUtil.getMessage("Reports_abrechnung_impfungen_total", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.totalGewichtetDosenTitle, ServerMessageUtil.getMessage("Reports_abrechnung_gewDosenTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.totalGewichtetDosenKey, ServerMessageUtil.getMessage("Reports_abrechnung_gewDosen_total", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.fachverantwortung, ServerMessageUtil.getMessage("Reports_abrechnung_fachverantwortung", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.fachNameKey, ServerMessageUtil.getMessage("Reports_abrechnung_fachverantwortung_name", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.fachVornameKey, ServerMessageUtil.getMessage("Reports_abrechnung_fachverantwortung_vorname", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.fachMailKey, ServerMessageUtil.getMessage("Reports_abrechnung_fachverantwortung_email", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.fachGLNKey, ServerMessageUtil.getMessage("Reports_abrechnung_fachverantwortung_gln", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.organisationsverantwortung, ServerMessageUtil.getMessage("Reports_abrechnung_organisationsverantwortung", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.orgaVornameKey, ServerMessageUtil.getMessage("Reports_abrechnung_organisationsverantwortung_vorname", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.orgaNameKey, ServerMessageUtil.getMessage("Reports_abrechnung_organisationsverantwortung_name", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.orgaMailKey, ServerMessageUtil.getMessage("Reports_abrechnung_organisationsverantwortung_email", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.anzahlImpfungAelterTitle, ServerMessageUtil.getMessage("Reports_anzahlImpfungAelterTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.anzahlImpfungJuengerTitle, ServerMessageUtil.getMessage("Reports_anzahlImpfungJuengerTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.anzahlImpfungAelterKindTitle, ServerMessageUtil.getMessage("Reports_anzahlImpfungAelterKindTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.anzahlImpfungJuengerKindTitle, ServerMessageUtil.getMessage("Reports_anzahlImpfungJuengerKindTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.aelterTitle, ServerMessageUtil.getMessage("Reports_aelterTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.juengerTitle, ServerMessageUtil.getMessage("Reports_juengerTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.aelterKindTitle, ServerMessageUtil.getMessage("Reports_aelterKindTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.juengerKindTitle, ServerMessageUtil.getMessage("Reports_juengerKindTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.pauschaleAelterTitle, ServerMessageUtil.getMessage("Reports_pauschaleAelterTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.pauschaleJuengerTitle, ServerMessageUtil.getMessage("Reports_pauschaleJuengerTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.pauschaleAelterKindTitle, ServerMessageUtil.getMessage("Reports_pauschaleAelterKindTitle", locale));
		mergerDTO.addValue(MergeFieldsAbrechnungZH.pauschaleJuengerKindTitle, ServerMessageUtil.getMessage("Reports_pauschaleJuengerKindTitle", locale));

		abrechnung.forEach(dataRow -> fillDataRow(dataRow, mergerDTO, locale));
		return mergerDTO;
	}

	public void fillDataRow(AbrechnungZHDataRow dataRow, ExcelMergerDTO mergerDTO, Locale locale) {
		ExcelMergerDTO excelRowGroup = mergerDTO.createGroup(MergeFieldsAbrechnungZH.repeatRow);
		final OrtDerImpfung ortDerImpfung = dataRow.getOrtDerImpfung();
		if (ortDerImpfung != null) {
			excelRowGroup.addValue(MergeFieldsAbrechnungZH.odiName, ortDerImpfung.getName());
			excelRowGroup.addValue(MergeFieldsAbrechnungZH.odiIdentifier, ortDerImpfung.getIdentifier());
			excelRowGroup.addValue(MergeFieldsAbrechnungZH.verantwortlicherGln, dataRow.getVerantwortlicherGln());
			excelRowGroup.addValue(MergeFieldsAbrechnungZH.adresse1, ortDerImpfung.getAdresse().getAdresse1());
			excelRowGroup.addValue(MergeFieldsAbrechnungZH.adresse2, ortDerImpfung.getAdresse().getAdresse2());
			excelRowGroup.addValue(MergeFieldsAbrechnungZH.PLZ, ortDerImpfung.getAdresse().getPlz());
			excelRowGroup.addValue(MergeFieldsAbrechnungZH.ort, ortDerImpfung.getAdresse().getOrt());
			excelRowGroup.addValue(MergeFieldsAbrechnungZH.ZSR, ortDerImpfung.getZsrNummer());
			excelRowGroup.addValue(MergeFieldsAbrechnungZH.GLN, ortDerImpfung.getGlnNummer());
			excelRowGroup.addValue(MergeFieldsAbrechnungZH.Typ, ServerMessageUtil.translateEnumValue(ortDerImpfung.getTypForAbrechnungExcel(), locale));
		}

		excelRowGroup.addValue(MergeFieldsAbrechnungZH.IBAN, "");

		excelRowGroup.addValue(MergeFieldsAbrechnungZH.selbstzahlendeAelter, dataRow.getSelbstzahlendeCount());
		excelRowGroup.addValue(MergeFieldsAbrechnungZH.OKPAelter, dataRow.getKrankenkasseOKPCount());
		excelRowGroup.addValue(MergeFieldsAbrechnungZH.AuslandAelter, dataRow.getKrankenkasseAuslandCount());
		excelRowGroup.addValue(MergeFieldsAbrechnungZH.EDAAelter, dataRow.getKrankenkasseEdaCount());
		excelRowGroup.addValue(MergeFieldsAbrechnungZH.andereAelter, dataRow.getKrankenkasseAndereCount());

		excelRowGroup.addValue(MergeFieldsAbrechnungZH.selbstzahlendeJuenger, dataRow.getSelbstzahlendeJuengerCount());
		excelRowGroup.addValue(MergeFieldsAbrechnungZH.OKPJuenger, dataRow.getKrankenkasseOKPJuengerCount());
		excelRowGroup.addValue(MergeFieldsAbrechnungZH.AuslandJuenger, dataRow.getKrankenkasseAuslandJuengerCount());
		excelRowGroup.addValue(MergeFieldsAbrechnungZH.EDAJuenger, dataRow.getKrankenkasseEdaJuengerCount());
		excelRowGroup.addValue(MergeFieldsAbrechnungZH.andereJuenger, dataRow.getKrankenkasseAndereJuengerCount());

		excelRowGroup.addValue(MergeFieldsAbrechnungZH.totalImpfungen, dataRow.getTotalImpfungenCount());
		excelRowGroup.addValue(MergeFieldsAbrechnungZH.totalGewichtetDosen, dataRow.getTotalGewichtetDosen());

		excelRowGroup.addValue(MergeFieldsAbrechnungZH.fachName, dataRow.getFvName());
		excelRowGroup.addValue(MergeFieldsAbrechnungZH.fachVorname, dataRow.getFvVorname());
		excelRowGroup.addValue(MergeFieldsAbrechnungZH.fachMail, dataRow.getFvMail());
		excelRowGroup.addValue(MergeFieldsAbrechnungZH.fachGLN, dataRow.getFvGlnNummer());

		excelRowGroup.addValue(MergeFieldsAbrechnungZH.orgaName, dataRow.getOvName());
		excelRowGroup.addValue(MergeFieldsAbrechnungZH.orgaVorname, dataRow.getOvVorname());
		excelRowGroup.addValue(MergeFieldsAbrechnungZH.orgaMail, dataRow.getOvMail());
	}
}
