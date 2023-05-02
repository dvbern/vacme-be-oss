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


import javax.annotation.Nonnull;

import ch.dvbern.oss.lib.excelmerger.mergefields.MergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import ch.dvbern.oss.lib.excelmerger.mergefields.RepeatRowMergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.SimpleMergeField;

import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.BOOLEAN_X_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATETIME_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.INTEGER_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldsOdiImpfungenTerminbuchungen implements MergeFieldProvider {

	reportTitle(new SimpleMergeField<>("reportTitle", STRING_CONVERTER)),
	registrierungsnummerKey(new SimpleMergeField<>("registrierungsnummerKey", STRING_CONVERTER)),
	registrierungsnummer(new SimpleMergeField<>("registrierungsnummer", STRING_CONVERTER)),
	vornameKey(new SimpleMergeField<>("vornameKey", STRING_CONVERTER)),
	vorname(new SimpleMergeField<>("vorname", STRING_CONVERTER)),
	nameKey(new SimpleMergeField<>("nameKey", STRING_CONVERTER)),
	name(new SimpleMergeField<>("name", STRING_CONVERTER)),
	geburtsdatumKey(new SimpleMergeField<>("geburtsdatumKey", STRING_CONVERTER)),
	geburtsdatum(new SimpleMergeField<>("geburtsdatum", DATE_CONVERTER)),
	kkKartenNrKey(new SimpleMergeField<>("kkKartenNrKey", STRING_CONVERTER)),
	kkKartenNr(new SimpleMergeField<>("kkKartenNr", STRING_CONVERTER)),

	odiIdKey(new SimpleMergeField<>("odiIdKey", STRING_CONVERTER)),
	odiId(new SimpleMergeField<>("odiId", STRING_CONVERTER)),
	odiNameKey(new SimpleMergeField<>("odiNameKey", STRING_CONVERTER)),
	odiName(new SimpleMergeField<>("odiName", STRING_CONVERTER)),
	odiGlnKey(new SimpleMergeField<>("odiGlnKey", STRING_CONVERTER)),
	odiGln(new SimpleMergeField<>("odiGln", STRING_CONVERTER)),
	odiTypKey(new SimpleMergeField<>("odiTypKey", STRING_CONVERTER)),
	odiTyp(new SimpleMergeField<>("odiTyp", STRING_CONVERTER)),

	verantwortlichePersonNameKey(new SimpleMergeField<>("verantwortlichePersonNameKey", STRING_CONVERTER)),
	verantwortlichePersonName(new SimpleMergeField<>("verantwortlichePersonName", STRING_CONVERTER)),
	verantwortlichePersonGlnKey(new SimpleMergeField<>("verantwortlichePersonGlnKey", STRING_CONVERTER)),
	verantwortlichePersonGln(new SimpleMergeField<>("verantwortlichePersonGln", STRING_CONVERTER)),


	terminKey(new SimpleMergeField<>("terminKey", STRING_CONVERTER)),
	termin(new SimpleMergeField<>("termin", DATETIME_CONVERTER)),
	impfungDatumKey(new SimpleMergeField<>("impfungDatumKey", STRING_CONVERTER)),
	impfungDatum(new SimpleMergeField<>("impfungDatum", DATETIME_CONVERTER)),
	krankheitKey(new SimpleMergeField<>("krankheitKey", STRING_CONVERTER)),
	krankheit(new SimpleMergeField<>("krankheit", STRING_CONVERTER)),
	impfstoffNameKey(new SimpleMergeField<>("impfstoffNameKey", STRING_CONVERTER)),
	impfstoffName(new SimpleMergeField<>("impfstoffName", STRING_CONVERTER)),
	impfstoffIdKey(new SimpleMergeField<>("impfstoffIdKey", STRING_CONVERTER)),
	impfstoffId(new SimpleMergeField<>("impfstoffId", STRING_CONVERTER)),
	impfungExternKey(new SimpleMergeField<>("impfungExternKey", STRING_CONVERTER)),
	impfungExtern(new SimpleMergeField<>("impfungExtern", BOOLEAN_X_CONVERTER)),
	grundimmunisierungKey(new SimpleMergeField<>("grundimmunisierungKey", STRING_CONVERTER)),
	grundimmunisierung(new SimpleMergeField<>("grundimmunisierung", BOOLEAN_X_CONVERTER)),
	impffolgeNrKey(new SimpleMergeField<>("impffolgeNrKey", STRING_CONVERTER)),
	impffolgeNr(new SimpleMergeField<>("impffolgeNr", INTEGER_CONVERTER)),
	selbstzahlendeKey(new SimpleMergeField<>("selbstzahlendeKey", STRING_CONVERTER)),
	selbstzahlende(new SimpleMergeField<>("selbstzahlende", BOOLEAN_X_CONVERTER)),
	lotKey(new SimpleMergeField<>("lotKey", STRING_CONVERTER)),
	lot(new SimpleMergeField<>("lot", STRING_CONVERTER)),

	letzteImpfungNameKey(new SimpleMergeField<>("letzteImpfungNameKey", STRING_CONVERTER)),
	letzteImpfungName(new SimpleMergeField<>("letzteImpfungName", STRING_CONVERTER)),
	letzteImpfungDatumKey(new SimpleMergeField<>("letzteImpfungDatumKey", STRING_CONVERTER)),
	letzteImpfungDatum(new SimpleMergeField<>("letzteImpfungDatum", DATETIME_CONVERTER)),
	selbstFreigegebenKey(new SimpleMergeField<>("selbstFreigegebenKey", STRING_CONVERTER)),
	selbstFreigegeben(new SimpleMergeField<>("selbstFreigegeben", BOOLEAN_X_CONVERTER)),
	terminOffsetKey(new SimpleMergeField<>("terminOffsetKey", STRING_CONVERTER)),
	terminOffset(new SimpleMergeField<>("terminOffset", DATETIME_CONVERTER)),

	repeatRow(new RepeatRowMergeField("repeatRow"));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldsOdiImpfungenTerminbuchungen(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
