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

import java.util.List;
import java.util.Locale;

import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import ch.dvbern.oss.vacme.print.base.PdfConstants;
import ch.dvbern.oss.vacme.print.base.PdfGenerator;
import ch.dvbern.oss.vacme.print.base.PdfUtil;
import ch.dvbern.oss.vacme.print.base.VacmePdfGenerator;
import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.NEWLINE;

public class OnboardingPdfGenerator extends VacmePdfGenerator {

	@NonNull
	private String code;

	public OnboardingPdfGenerator(@NotNull Registrierung registrierung, @NonNull String code) {
		super(registrierung);
		this.code = code;
	}

	@NotNull
	@Override
	protected String getDocumentTitle() {
		return translate("print_onboarding_title", Locale.GERMAN);
	}

	@NotNull
	@Override
	protected PdfGenerator.CustomGenerator getCustomGenerator() {
		return (generator) -> {
			final Locale secondaryLocale = Mandant.ZH == MandantUtil.getMandant() ? Locale.ENGLISH : Locale.FRENCH;

			Document document = generator.getDocument();
			printText(document, "print_onboarding_text", code, Locale.GERMAN);

			document.add(new Paragraph("\n\n" + translate("print_onboarding_text_anhang", Locale.GERMAN), PdfConstants.FONT_TITLE));

			document.newPage();

			Paragraph paragraph = new Paragraph(translate("print_onboarding_title", secondaryLocale), PdfConstants.FONT_TITLE);
			paragraph.setLeading(0.0F, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
			paragraph.add(NEWLINE);
			paragraph.setSpacingAfter(PdfConstants.DEFAULT_FONT_SIZE * 2 * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
			document.add(paragraph);

			printText(document, "print_onboarding_text", code, secondaryLocale);
		};
	}

	private void printText(@NonNull Document document, @NonNull String key, @NonNull String code, Locale locale) {
		final Paragraph text = PdfUtil.createParagraph(translate("print_hallo", locale) + " " + registrierung.getVorname() + " " + registrierung.getName() + "\n\n", 0);
		addParagraph(text, "1", locale);
		text.add("\n\n");
		text.add(translate("print_onboarding_text_2", locale));
		document.add(text);

		List<Paragraph> list = Lists.newArrayList();
		list.add(addParagraph(PdfUtil.createParagraph("", 0),"2_1", locale));
		list.add(addParagraph(PdfUtil.createParagraph("", 0),"2_2", locale));
		list.add(addParagraph(PdfUtil.createParagraph("", 0),"2_3", locale));
		document.add(PdfUtil.createParagraphListInParagraph(list));

		Paragraph appendix = PdfUtil.createParagraph("",1);
		addParagraph(appendix, "3", locale);
		appendix.add("\n\n");

		appendix.add(new Chunk(translate("print_onboarding_text_vorgehen_1", locale), PdfConstants.DEFAULT_FONT_BOLD));
		appendix.add(translate("print_onboarding_text_vorgehen_2", locale));
		appendix.add(new Chunk(translate("print_onboarding_text_vorgehen_3", locale), PdfConstants.DEFAULT_FONT_BOLD));
		appendix.add(translate("print_onboarding_text_vorgehen_4", locale));
		appendix.add(new Chunk(translate("print_onboarding_text_vorgehen_5", locale) + " " + code + ". ", PdfConstants.DEFAULT_FONT_BOLD));
		appendix.add(translate("print_onboarding_text_vorgehen_6", locale));

		appendix.add("\n\n");

		addParagraph(appendix, "4", locale);

		appendix.add("\n\n\n\n");

		appendix.add(translate("print_onboarding_text_5", locale));

		document.add(appendix);
	}

	private Paragraph addParagraph(Paragraph text, String index, Locale locale) {
		text.add(translate("print_onboarding_text_" + index + "_1", locale));
		text.add(new Chunk(translate("print_onboarding_text_" + index + "_2", locale), PdfConstants.DEFAULT_FONT_BOLD));
		text.add(translate("print_onboarding_text_" + index + "_3", locale));
		return text;
	}
}
