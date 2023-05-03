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


import javax.annotation.Nonnull;

import ch.dvbern.oss.lib.excelmerger.mergefields.MergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import ch.dvbern.oss.lib.excelmerger.mergefields.RepeatRowMergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.SimpleMergeField;

import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.LONG_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldsAbrechnung implements MergeFieldProvider {

	reportTitle(new SimpleMergeField<>("reportTitle", STRING_CONVERTER)),
	reportErwachsenTitle(new SimpleMergeField<>("reportErwachsenTitle", STRING_CONVERTER)),
	reportKindTitle(new SimpleMergeField<>("reportKindTitle", STRING_CONVERTER)),
	kantonKey(new SimpleMergeField<>("kantonKey", STRING_CONVERTER)),
	kanton(new SimpleMergeField<>("kanton", STRING_CONVERTER)),
	vonKey(new SimpleMergeField<>("vonKey", STRING_CONVERTER)),
	von(new SimpleMergeField<>("von", DATE_CONVERTER)),
	bisKey(new SimpleMergeField<>("bisKey", STRING_CONVERTER)),
	bis(new SimpleMergeField<>("bis", DATE_CONVERTER)),
	impfortTyp(new SimpleMergeField<>("impfortTyp", STRING_CONVERTER)),
	pauschaleOKPKey(new SimpleMergeField<>("pauschaleOKPKey", STRING_CONVERTER)),
	pauschaleRestKey(new SimpleMergeField<>("pauschaleRestKey", STRING_CONVERTER)),
	impfort(new SimpleMergeField<>("impfort", STRING_CONVERTER)),
	anzahlImpfung(new SimpleMergeField<>("anzahlImpfung", STRING_CONVERTER)),
	verguetung(new SimpleMergeField<>("verguetung", STRING_CONVERTER)),
	fachverantwortung(new SimpleMergeField<>("fachverantwortung", STRING_CONVERTER)),
	organisationsverantwortung(new SimpleMergeField<>("organisationsverantwortung", STRING_CONVERTER)),
	odiNameKey(new SimpleMergeField<>("odiNameKey", STRING_CONVERTER)),
	verantwortlicherGlnKey(new SimpleMergeField<>("verantwortlicherGlnKey", STRING_CONVERTER)),
	adresse1Key(new SimpleMergeField<>("adresse1Key", STRING_CONVERTER)),
	adresse2Key(new SimpleMergeField<>("adresse2Key", STRING_CONVERTER)),
	PLZKey(new SimpleMergeField<>("PLZKey", STRING_CONVERTER)),
	ortKey(new SimpleMergeField<>("ortKey", STRING_CONVERTER)),
	ZSRKey(new SimpleMergeField<>("ZSRKey", STRING_CONVERTER)),
	IBANKey(new SimpleMergeField<>("IBANKey", STRING_CONVERTER)),
	ImpfortTypIMPFZENTRUM(new SimpleMergeField<>("ImpfortTyp_IMPFZENTRUM", STRING_CONVERTER)),
	ImpfortTypHAUSARZT(new SimpleMergeField<>("ImpfortTyp_HAUSARZT", STRING_CONVERTER)),
	ImpfortTypALTERSHEIM(new SimpleMergeField<>("ImpfortTyp_ALTERSHEIM", STRING_CONVERTER)),
	ImpfortTypAPOTHEKE(new SimpleMergeField<>("ImpfortTyp_APOTHEKE", STRING_CONVERTER)),
	ImpfortTypMOBIL(new SimpleMergeField<>("ImpfortTyp_MOBIL", STRING_CONVERTER)),
	ImpfortTypSPITAL(new SimpleMergeField<>("ImpfortTyp_SPITAL", STRING_CONVERTER)),
	ImpfortTypANDERE(new SimpleMergeField<>("ImpfortTyp_ANDERE", STRING_CONVERTER)),
	ImpfortTypSELBSTZAHLENDE(new SimpleMergeField<>("ImpfortTyp_SELBSTZAHLENDE", STRING_CONVERTER)),
	GLNKey(new SimpleMergeField<>("GLNKey", STRING_CONVERTER)),
	TypKey(new SimpleMergeField<>("TypKey", STRING_CONVERTER)),
	pauschaleODIOKPKey(new SimpleMergeField<>("pauschaleODIOKPKey", STRING_CONVERTER)),
	pauschaleODIRestKey(new SimpleMergeField<>("pauschaleODIRestKey", STRING_CONVERTER)),
	selbstzahlendeKey(new SimpleMergeField<>("selbstzahlendeKey", STRING_CONVERTER)),
	OKPKey(new SimpleMergeField<>("OKPKey", STRING_CONVERTER)),
	AuslandKey(new SimpleMergeField<>("AuslandKey", STRING_CONVERTER)),
	EDAKey(new SimpleMergeField<>("EDAKey", STRING_CONVERTER)),
	andereKey(new SimpleMergeField<>("andereKey", STRING_CONVERTER)),
	totalKey(new SimpleMergeField<>("totalKey", STRING_CONVERTER)),
	fachNameKey(new SimpleMergeField<>("fachNameKey", STRING_CONVERTER)),
	fachVornameKey(new SimpleMergeField<>("fachVornameKey", STRING_CONVERTER)),
	fachMailKey(new SimpleMergeField<>("fachMailKey", STRING_CONVERTER)),
	fachGLNKey(new SimpleMergeField<>("fachGLNKey", STRING_CONVERTER)),
	orgaNameKey(new SimpleMergeField<>("orgaNameKey", STRING_CONVERTER)),
	orgaVornameKey(new SimpleMergeField<>("orgaVornameKey", STRING_CONVERTER)),
	orgaMailKey(new SimpleMergeField<>("orgaMailKey", STRING_CONVERTER)),

	repeatRow(new RepeatRowMergeField("repeatRow")),
	odiName(new SimpleMergeField<>("odiName", STRING_CONVERTER)),
	verantwortlicherGln(new SimpleMergeField<>("verantwortlicherGln", STRING_CONVERTER)),
	adresse1(new SimpleMergeField<>("adresse1", STRING_CONVERTER)),
	adresse2(new SimpleMergeField<>("adresse2", STRING_CONVERTER)),
	ort(new SimpleMergeField<>("ort", STRING_CONVERTER)),
	PLZ(new SimpleMergeField<>("PLZ", STRING_CONVERTER)),
	ZSR(new SimpleMergeField<>("ZSR", STRING_CONVERTER)),
	GLN(new SimpleMergeField<>("GLN", STRING_CONVERTER)),
	Typ(new SimpleMergeField<>("Typ", STRING_CONVERTER)),
	IBAN(new SimpleMergeField<>("IBAN", STRING_CONVERTER)),
	pauschaleODIOKP(new SimpleMergeField<>("pauschaleODIOKP", STRING_CONVERTER)),
	pauschaleODIRest(new SimpleMergeField<>("pauschaleODIRest", STRING_CONVERTER)),
	selbstzahlende(new SimpleMergeField<>("selbstzahlende", LONG_CONVERTER)),
	OKP(new SimpleMergeField<>("OKP", LONG_CONVERTER)),
	Ausland(new SimpleMergeField<>("Ausland", LONG_CONVERTER)),
	EDA(new SimpleMergeField<>("EDA", LONG_CONVERTER)),
	andere(new SimpleMergeField<>("andere", LONG_CONVERTER)),
	total(new SimpleMergeField<>("total", STRING_CONVERTER)),
	fachName(new SimpleMergeField<>("fachName", STRING_CONVERTER)),
	fachVorname(new SimpleMergeField<>("fachVorname", STRING_CONVERTER)),
	fachMail(new SimpleMergeField<>("fachMail", STRING_CONVERTER)),
	fachGLN(new SimpleMergeField<>("fachGLN", STRING_CONVERTER)),
	orgaName(new SimpleMergeField<>("orgaName", STRING_CONVERTER)),
	orgaVorname(new SimpleMergeField<>("orgaVorname", STRING_CONVERTER)),
	orgaMail(new SimpleMergeField<>("orgaMail", STRING_CONVERTER));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldsAbrechnung(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
