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
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.print.base.PdfConstants;
import ch.dvbern.oss.vacme.print.base.PdfGenerator;
import ch.dvbern.oss.vacme.print.base.PdfUtil;
import ch.dvbern.oss.vacme.print.base.VacmePdfGenerator;
import com.lowagie.text.Document;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * generates a document that informs a vaccinated person that her certificate was revoked.
 * This is needed for registrations that do not have registrierungseingang ONLINE_REGISTRATION
 */
public class ZertifikatStornierungPdfGenerator extends VacmePdfGenerator {

	@NonNull
	private final Zertifikat zertifikat;

	public ZertifikatStornierungPdfGenerator(@NotNull Registrierung registrierung, @NonNull Zertifikat zertifikat) {
		super(registrierung);
		this.zertifikat = zertifikat;
	}

	@NotNull
	@Override
	protected String getDocumentTitle() {
		return translateAllConfiguredLanguages("print_zertifikat_storniert_title", " / ");
	}

	@NotNull
	@Override
	protected PdfGenerator.CustomGenerator getCustomGenerator() {
		return (generator) -> {
			Document document = generator.getDocument();
			printContent(document);
		};
	}

	private void printContent(@NonNull Document document) {
		doForAllLanguagesSeparated(
			locale -> printContentInLanguage(document, locale),
			() -> PdfUtil.addLineSeparator(document, 1));
	}

	private void printContentInLanguage(@NonNull Document document, @NonNull Locale locale) {
		document.add(PdfUtil.createParagraph(translate("print_zertifikat_hallo", locale), 1));
		document.add(PdfUtil.createParagraph(translate("print_zertifikat_storniert_intro", locale), 1));
		document.add(PdfUtil.createParagraph(translate("print_zertifikat_storniert_grund1_title", locale), 0, PdfConstants.DEFAULT_FONT_BOLD));
		document.add(PdfUtil.createParagraph(translate("print_zertifikat_storniert_grund1_content", locale), 1));
		if (locale.equals(Locale.FRENCH)) {
			document.newPage();
		}
		document.add(PdfUtil.createParagraph(translate("print_zertifikat_storniert_grund2_title", locale), 0, PdfConstants.DEFAULT_FONT_BOLD));
		document.add(PdfUtil.createParagraph(translate("print_zertifikat_storniert_grund2_content", locale), 1));
		document.add(PdfUtil.createParagraph(translate("print_zertifikat_storniert_gruss", locale), 1));
		document.add(PdfUtil.createParagraph(1,
			PdfUtil.createPhrase(translate("print_zertifikat_storniert_uvci", locale), PdfConstants.DEFAULT_FONT_BOLD),
			PdfUtil.createPhrase(zertifikat.getUvci())));
	}
}
