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

package ch.dvbern.oss.vacme.entities.impfen;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Offizielle Liste der Krankenkassen
 */

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Schema(enumeration = { "AGRISANO", "AMB", "AQUILANA", "ARCOSANA", "ASSURA", "ATUPRI", "AVENIR", "BILDENDE_KUENSTLER",
	"BIRCHMEIER", "COMPACT", "CONCORDIA", "CSS", "EASY_SANA", "EGK", "EINSIEDLER", "FENACO", "GALENOS",
	"GEWERBLICHE_KRANKENKASSE_BERN", "GLARNER", "HELSANA", "HOTELA", "INSTITUT_INGENBOHL", "INTRAS", "KKV", "KLUG",
	"KOLPING", "KPT", "KVF", "LUMNEZIANA", "LUZERNER_HINTERLAND", "METALLBAUFIRMEN", "MOOVE_SYMPANY", "MUTUEL",
	"MUTUELLE_NEUCHATELOISE", "OKK", "PHILOS", "PROGRES", "PROVITA", "RHENUSANA", "SANA24", "SANAGATE", "SANAVALS",
	"SANITAS", "SIMPLON", "SLKK", "SODALIS", "STEFFISBURG", "STIFTUNG_WADENSWIL", "STOFFEL", "SUMISWALDER", "SUPRA",
	"SWICA", "VALLEE_ENTREMONT", "VISANA", "VITA_SURSELVA", "VIVACARE", "VIVAO_SYMPANY", "ANDERE", "AUSLAND", "EDA" })
public enum Krankenkasse {
	CSS("00008"),
	AQUILANA("00032"),
	MOOVE_SYMPANY("00057"),
	SUPRA("00062"),
	EINSIEDLER("00134"),
	PROVITA("00182"),
	SUMISWALDER("00194"),
	STEFFISBURG("00246"),
	CONCORDIA("00290"),
	ATUPRI("00312"),
	AVENIR("00343"),
	LUZERNER_HINTERLAND("00360"),
	KPT("00376"),
	OKK("00455"),
	VIVAO_SYMPANY("00509"),
	KVF("00558"),
	KOLPING("00762"),
	EASY_SANA("00774"),
	GLARNER("00780"),
	LUMNEZIANA("00820"),
	KLUG("00829"),
	EGK("00881"),
	SANAVALS("00901"),
	SLKK("00923"),
	SODALIS("00941"),
	VITA_SURSELVA("00966"),
	PROGRES("00994"),
	KKV("01040"),
	VALLEE_ENTREMONT("01113"),
	INSTITUT_INGENBOHL("01142"),
	MUTUELLE_NEUCHATELOISE("01179"),
	STIFTUNG_WADENSWIL("01318"),
	BIRCHMEIER("01322"),
	STOFFEL("01331"),
	SIMPLON("01362"),
	SWICA("01384"),
	GALENOS("01386"),
	RHENUSANA("01401"),
	BILDENDE_KUENSTLER("01402"),
	MUTUEL("01479"),
	GEWERBLICHE_KRANKENKASSE_BERN("01491"),
	AMB("01507"),
	SANITAS("01509"),
	HOTELA("01520"),
	METALLBAUFIRMEN("01522"),
	INTRAS("01529"),
	PHILOS("01535"),
	FENACO("01540"),
	ASSURA("01542"),
	VISANA("01555"),
	AGRISANO("01560"),
	HELSANA("01562"),
	SANA24("01568"),
	ARCOSANA("01569"),
	VIVACARE("01570"),
	COMPACT("01575"),
	SANAGATE("01577"),
	ANDERE("99999"), // FIXME. Do we need this unknown???
	AUSLAND("00000000000000000000"),
	EDA("00000000000000000000");

	@NonNull
	private final String bagNummer; // official BAG Number. 5 Digits

	Krankenkasse(@NonNull final String newValue) {
		bagNummer = newValue;
	}


	@SuppressWarnings("unused") // Wird fuer Json Mapping gebraucht
	public String getName() {
		return name();
	}


	@SuppressWarnings("unused") // Wird fuer Json Mapping gebraucht
	@NonNull
	public String getBagNummer() { return bagNummer; }


}
