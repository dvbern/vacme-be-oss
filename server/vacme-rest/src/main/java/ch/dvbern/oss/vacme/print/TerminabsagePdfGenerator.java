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

package ch.dvbern.oss.vacme.print;

import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.print.base.PdfGenerator;
import ch.dvbern.oss.vacme.print.base.PdfUtil;
import ch.dvbern.oss.vacme.print.base.VacmePdfGenerator;
import com.lowagie.text.Document;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public class TerminabsagePdfGenerator extends VacmePdfGenerator {

	@NonNull
	private final Impftermin termin1or2orBooster;

	@Nullable
	private final Impftermin termin2;

	@NonNull
	private final String terminEffectiveStart;

	@Nullable
	private final String termin2EffectiveStart;

	public TerminabsagePdfGenerator(@NonNull Registrierung registrierung, @NonNull Impftermin termin1or2orBooster, @Nullable Impftermin termin2, @NonNull String terminEffectiveStart, @Nullable String termin2EffectiveStart) {
		super(registrierung);
		this.termin1or2orBooster = termin1or2orBooster;
		this.termin2 = termin2;
		this.terminEffectiveStart = terminEffectiveStart;
		this.termin2EffectiveStart = termin2EffectiveStart;
	}

	@NotNull
	@Override
	protected String getDocumentTitle() {
		return translateAllConfiguredLanguages("print_terminabsage_title", " / ");
	}

	@NotNull
	@Override
	protected PdfGenerator.CustomGenerator getCustomGenerator() {
		return (generator) -> {
			Document document = generator.getDocument();
			String datum = terminEffectiveStart;
			String telefon = translate("print_kontakt_telefon");

			if (termin2 != null) {
				String datum2 = termin2EffectiveStart != null ? termin2EffectiveStart : termin2.getTerminZeitfensterStartDateAndTimeString();
				printAllLanguages(document, "print_terminabsage_beide_termine_content", datum, datum2, telefon);
			} else {
				switch (termin1or2orBooster.getImpffolge()) {
				case ERSTE_IMPFUNG:
					printAllLanguages(document, "print_terminabsage_content", "1", datum, telefon);
					break;
				case ZWEITE_IMPFUNG:
					printAllLanguages(document, "print_terminabsage_content", "2", datum, telefon);
					break;
				case BOOSTER_IMPFUNG:
					printAllLanguages(document, "print_terminabsage_booster_content", datum, telefon);
					break;
				}
			}
		};
	}

	private void printAllLanguages(@NonNull Document document, @NonNull String key, @NonNull Object... args) {
		doForAllLanguagesSeparated(locale -> document.add(
			PdfUtil.createParagraph(
				translate("print_hallo", locale) + '\n' + translate(key, locale, args))), document);
	}
}
