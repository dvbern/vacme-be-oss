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

import java.time.LocalDate;

import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import ch.dvbern.oss.vacme.print.base.PdfGenerator;
import ch.dvbern.oss.vacme.print.base.PdfUtil;
import ch.dvbern.oss.vacme.print.base.VacmePdfGenerator;
import com.lowagie.text.Document;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class FreigabeBoosterPdfGenerator extends VacmePdfGenerator {

	private final @NonNull LocalDate datumLetzteImpfung;

	public FreigabeBoosterPdfGenerator(
		@NonNull Registrierung registrierung,
		@NonNull LocalDate datumLetzteImpfung,
		@NonNull KantonaleBerechtigung kantonaleBerechtigung
	) {
		super(registrierung, kantonaleBerechtigung);
		this.datumLetzteImpfung = datumLetzteImpfung;
	}

	@NotNull
	@Override
	protected String getDocumentTitle() {
		return translateAllConfiguredLanguages("print_freigabe_booster_title", " / ");
	}

	@NotNull
	@Override
	protected PdfGenerator.CustomGenerator getCustomGenerator() {
		return (generator) -> {
			String telefon = translate("print_kontakt_telefon");
			Document document = generator.getDocument();
			if (Mandant.BE == MandantUtil.getMandant()) {
				// todo Affenpocken: Booster Freigabe notifications are a COVID only feature as of now
				addBarcode(document, KrankheitIdentifier.COVID);
			}

			doForAllLanguagesSeparated(locale -> {
					document.add(PdfUtil.createParagraph(translate("print_freigabe_booster_text", locale, registrierung.getVorname(), registrierung.getName(),
						DateUtil.formatDate(datumLetzteImpfung, locale), telefon, registrierung.getRegistrierungsnummer()), 1));
					if (Mandant.ZH == MandantUtil.getMandant()) {
						document.add(PdfUtil.createParagraph(translate("print_freigabe_booster_gruss", locale)));
					}
				}, document);

		};
	}
}
