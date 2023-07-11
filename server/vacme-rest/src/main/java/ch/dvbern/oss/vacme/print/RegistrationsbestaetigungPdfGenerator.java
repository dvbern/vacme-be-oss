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

import java.util.Locale;

import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.print.base.PdfGenerator;
import ch.dvbern.oss.vacme.print.base.PdfUtil;
import ch.dvbern.oss.vacme.print.base.VacmePdfGenerator;
import com.lowagie.text.Document;
import org.jetbrains.annotations.NotNull;

public class RegistrationsbestaetigungPdfGenerator extends VacmePdfGenerator {

	private final String link;

	public RegistrationsbestaetigungPdfGenerator(@NotNull Registrierung registrierung, @NotNull String link) {
		super(registrierung);
		this.link = link;
	}

	@NotNull
	@Override
	protected String getDocumentTitle() {
		return translateAllConfiguredLanguages("print_registrierung_title", " / ");
	}

	@NotNull
	@Override
	protected PdfGenerator.CustomGenerator getCustomGenerator() {
		return (generator) -> {
			Document document = generator.getDocument();

			addBarcode(document, null);	// We don't know anything about Krankheit yet

			// Der Text ist je nach Eingangsart unterschiedlich
			String msgKeyContent;
			String placeholder;
			if (registrierung.getRegistrierungsEingang() == RegistrierungsEingang.ONLINE_REGISTRATION) {
				msgKeyContent = "print_registrierung_content_online";
				placeholder = link;
			} else {
				msgKeyContent = "print_registrierung_content_telefon";
				placeholder = translate("print_kontakt_telefon");
			}

			doForAllLanguagesSeparated(locale -> {
				int emptyLinesAfter = locale.equals(Locale.ENGLISH) ? 0 : 1;
				document.add(PdfUtil.createParagraph(translate("print_hallo", locale) + '\n' + translate(msgKeyContent, locale, placeholder), emptyLinesAfter));
			}, document);

		};
	}
}
