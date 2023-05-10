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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import ch.dvbern.oss.lib.excelmerger.RowFiller;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeField;
import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import ch.dvbern.oss.vacme.jax.OdiImpfungenDataRow;
import ch.dvbern.oss.vacme.jax.OdiTerminbuchungenDataRow;
import org.apache.poi.ss.usermodel.Sheet;
import org.checkerframework.checker.nullness.qual.NonNull;

@Dependent
public class OdiImpfungenExcelConverter implements ExcelConverter {
	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	public Sheet mergeHeaderFieldsStichtag(
		@NonNull Sheet sheet,
		@NonNull Locale locale
	) throws ExcelMergeException {

		ExcelMergerDTO mergerDTO = new ExcelMergerDTO();
		List<MergeField<?>> mergeFields = new ArrayList<>();

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.reportTitle.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.reportTitle.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_reportTitle", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.registrierungsnummerKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.registrierungsnummerKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_registrierungsnummerKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.vornameKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.vornameKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_vornameKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.nameKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.nameKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_nameKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.geburtsdatumKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.geburtsdatumKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_geburtsdatumKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.kkKartenNrKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.kkKartenNrKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_kkKartenNrKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.odiIdKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.odiIdKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_odiIdKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.odiNameKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.odiNameKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_odiNameKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.odiGlnKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.odiGlnKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_odiGlnKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.odiTypKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.odiTypKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_odiTypKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.verantwortlichePersonNameKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.verantwortlichePersonNameKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_verantwortlichePersonNameKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.verantwortlichePersonGlnKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.verantwortlichePersonGlnKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_verantwortlichePersonGlnKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.terminKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.terminKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_terminKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.impfungDatumKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.impfungDatumKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_impfungDatumKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.krankheitKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.krankheitKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_krankheitKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.impfstoffNameKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.impfstoffNameKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_impfstoffNameKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.impfstoffIdKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.impfstoffIdKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_impfstoffIdKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.impfungExternKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.impfungExternKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_impfungExternKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.grundimmunisierungKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.grundimmunisierungKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_grundimmunisierungKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.impffolgeNrKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.impffolgeNrKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_impffolgeNrKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.selbstzahlendeKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.selbstzahlendeKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_selbstzahlendeKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.lotKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.lotKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiImpfungen_lotKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.letzteImpfungNameKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.letzteImpfungNameKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiTerminbuchungen_letzteImpfungNameKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.letzteImpfungDatumKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.letzteImpfungDatumKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiTerminbuchungen_letzteImpfungDatumKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.selbstFreigegebenKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.selbstFreigegebenKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiTerminbuchungen_selbstFreigegebenKey", locale));

		mergeFields.add(MergeFieldsOdiImpfungenTerminbuchungen.terminOffsetKey.getMergeField());
		mergerDTO.addValue(MergeFieldsOdiImpfungenTerminbuchungen.terminOffsetKey.getMergeField(), ServerMessageUtil.getMessage("Reports_odiTerminbuchungen_terminOffsetKey", locale));

		ExcelMerger.mergeData(sheet, mergeFields, mergerDTO);


		return sheet;
	}

	public void mergeTerminbuchungenRows(
		@NonNull RowFiller rowFiller,
		@Nonnull List<? extends OdiTerminbuchungenDataRow> odiTerminbuchungen) {

		odiTerminbuchungen.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = new ExcelMergerDTO();

			mergeImpfungRow(dataRow, excelRowGroup);

			excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.letzteImpfungName, dataRow.getLetzteImpfungName());
			excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.letzteImpfungDatum, dataRow.getLetzteImpfungDatum());
			excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.selbstFreigegeben, dataRow.getSelbstFreigegeben());
			excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.terminOffset, dataRow.getTerminOffset());

			rowFiller.fillRow(excelRowGroup);
		});
	}

	public void mergeImpfungenRows(
		@NonNull RowFiller rowFiller,
		@Nonnull List<? extends OdiImpfungenDataRow> odiImpfungen
	) {
		odiImpfungen.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = new ExcelMergerDTO();

			mergeImpfungRow(dataRow, excelRowGroup);

			rowFiller.fillRow(excelRowGroup);
		});
	}

	private void mergeImpfungRow(OdiImpfungenDataRow dataRow, ExcelMergerDTO excelRowGroup) {
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.registrierungsnummer, dataRow.getRegistrierungsnummer());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.vorname, dataRow.getVorname());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.name, dataRow.getName());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.geburtsdatum, dataRow.getGeburtsdatum());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.kkKartenNr, dataRow.getKkKartenNr());

		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.odiId, dataRow.getOdiId());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.odiName, dataRow.getOdiName());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.odiGln, dataRow.getOdiGln());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.odiTyp, dataRow.getOdiTyp());

		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.verantwortlichePersonName, dataRow.getVerantwortlichePersonName());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.verantwortlichePersonGln, dataRow.getVerantwortlichePersonGln());

		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.termin, dataRow.getTermin());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.impfungDatum, dataRow.getImpfungDatum());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.krankheit, dataRow.getKrankheit());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.impfstoffName, dataRow.getImpfstoffName());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.impfstoffId, dataRow.getImpfstoffId());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.impfungExtern, dataRow.getImpfungExtern());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.grundimmunisierung, dataRow.getGrundimmunisierung());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.impffolgeNr, dataRow.getImpffolgeNr());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.selbstzahlende, dataRow.getSelbstzahlende());
		excelRowGroup.addValue(MergeFieldsOdiImpfungenTerminbuchungen.lot, dataRow.getLot());
	}
}
