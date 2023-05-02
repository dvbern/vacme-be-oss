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
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import ch.dvbern.oss.vacme.print.base.PdfGenerator;
import ch.dvbern.oss.vacme.print.base.PdfUtil;
import ch.dvbern.oss.vacme.print.base.VacmePdfGenerator;
import com.lowagie.text.Document;
import org.jetbrains.annotations.NotNull;

public class ZertifikatCounterNeuerstelltPdfGenerator extends VacmePdfGenerator {

	static final String PREFIX = "print_zertifikat_counterneuerstellt_";

	public ZertifikatCounterNeuerstelltPdfGenerator(Registrierung registrierung) {
		super(registrierung);
	}

	@NotNull
	@Override
	protected String getDocumentTitle() {
		return translateAllConfiguredLanguages(PREFIX + "title", " / ");
	}

	@NotNull
	@Override
	protected PdfGenerator.CustomGenerator getCustomGenerator() {
		return (generator) -> {
			Document document = generator.getDocument();
			printContent(document, Locale.GERMAN);
			PdfUtil.addLineSeparator(document, 0);
			printContent(document, Locale.FRENCH);
			if (Mandant.ZH == MandantUtil.getMandant()) {
				document.newPage();
				printContent(document, Locale.ENGLISH);
			}
		};
	}

	private void printContent(Document document, Locale locale) {
		document.add(PdfUtil.createParagraph(translate(PREFIX + "text_1", locale)));
		document.add(PdfUtil.createParagraph(translate(PREFIX + "text_2", locale)));
		document.add(PdfUtil.createParagraph(translate(PREFIX + "text_3", locale)));
		if (Mandant.ZH == MandantUtil.getMandant()) {
			document.add(PdfUtil.createParagraph(translate(PREFIX + "gruss", locale)));
		}
	}

}
