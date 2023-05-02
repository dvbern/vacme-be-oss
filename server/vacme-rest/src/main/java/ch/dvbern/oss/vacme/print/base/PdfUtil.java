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

package ch.dvbern.oss.vacme.print.base;

import java.awt.Color;
import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.lib.invoicegenerator.pdf.PdfElementGenerator;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.NEWLINE;

public final class PdfUtil {

	private static final Logger LOG = LoggerFactory.getLogger(PdfUtil.class);

	private PdfUtil() {
		// nop
	}

	@Nonnull
	public static PdfPCell createTitleCell(@Nonnull String title) {
		PdfPCell cell = new PdfPCell(new Phrase(title, PdfConstants.DEFAULT_FONT));
		cell.setBackgroundColor(Color.LIGHT_GRAY);
		return cell;
	}

	public static Paragraph createTitle(@Nonnull String title) {
		Paragraph paragraph = new Paragraph(title, PdfConstants.FONT_TITLE);
		paragraph.setLeading(0.0F, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		paragraph.add(NEWLINE);
		paragraph.setSpacingAfter(PdfConstants.DEFAULT_FONT_SIZE * 2 * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		return paragraph;
	}

	@Nonnull
	public static Paragraph createSubTitle(@Nonnull String string) {
		Paragraph paragraph = new Paragraph(string, PdfConstants.DEFAULT_FONT_BOLD);
		paragraph.setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		paragraph.setSpacingBefore(1 * PdfConstants.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		paragraph.setSpacingAfter(1 * PdfConstants.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		return paragraph;
	}

	@Nonnull
	public static PdfPTable createKeepTogetherTable(
		@Nonnull java.util.List<Element> elements,
		final int emptyLinesBetween,
		final int emptyLinesAfter) {
		PdfPTable table = new PdfPTable(1);
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		table.setKeepTogether(true);
		table.getDefaultCell().setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		table.getDefaultCell().setPadding(0);
		table.getDefaultCell()
			.setPaddingBottom(emptyLinesBetween
				* PdfConstants.DEFAULT_FONT_SIZE
				* PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		elements.forEach(element -> {
			if (element instanceof List) {
				PdfPCell phraseCell = new PdfPCell();
				phraseCell.setBorder(Rectangle.NO_BORDER);
				phraseCell.addElement(element);
				table.addCell(phraseCell);
			}
			if (element instanceof PdfPTable) {
				table.addCell((PdfPTable) element);
			}
			if (element instanceof Paragraph) {
				table.addCell((Paragraph) element);
			}
		});
		table.setSpacingAfter(emptyLinesAfter
			* PdfConstants.DEFAULT_FONT_SIZE
			* PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		return table;
	}

	@Nonnull
	public static Phrase createPhrase(@Nonnull String string) {
		return createPhrase(string, PdfConstants.DEFAULT_FONT);
	}

	@Nonnull
	public static Phrase createPhrase(@Nonnull String string, @NonNull Font font) {
		return new Phrase(string, font);
	}

	@Nonnull
	public static Paragraph createParagraph(@Nonnull String string, final int emptyLinesAfter) {
		return createParagraph(string, emptyLinesAfter, PdfConstants.DEFAULT_FONT);
	}

	@Nonnull
	public static Paragraph createParagraph(final int emptyLinesAfter, @NonNull Phrase... phrases) {
		Paragraph paragraph = new Paragraph();
		for (Phrase phrase : phrases) {
			paragraph.add(phrase);
		}
		paragraph.setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		paragraph.setSpacingAfter(emptyLinesAfter
			* PdfConstants.DEFAULT_FONT_SIZE
			* PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		return paragraph;
	}


	@Nonnull
	public static Paragraph createParagraph(@Nonnull String string, final int emptyLinesAfter, final Font font) {
		Paragraph paragraph = new Paragraph(string, font);
		paragraph.setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		paragraph.setSpacingAfter(emptyLinesAfter
			* PdfConstants.DEFAULT_FONT_SIZE
			* PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		return paragraph;
	}

	@Nonnull
	public static Paragraph createParagraph(@Nonnull String string) {
		return createParagraph(string, 1);
	}

	@Nonnull
	public static Paragraph createListInParagraph(java.util.List<String> list) {
		Paragraph paragraph = new Paragraph();
		final List itextList = createList(list);
		paragraph.add(itextList);
		return paragraph;
	}

	@Nonnull
	public static Paragraph createParagraphListInParagraph(java.util.List<Paragraph> list) {
		Paragraph paragraph = createParagraph("", 1);

		final List itextList = new List(List.UNORDERED);
		list.forEach(item -> itextList.add(new ListItem(item)));

		paragraph.add(itextList);
		return paragraph;
	}

	@Nonnull
	public static List createList(java.util.List<String> list) {
		final List itextList = new List(List.UNORDERED);
		list.forEach(item -> itextList.add(createListItem(item)));
		return itextList;
	}

	@Nonnull
	public static List createListOrdered(java.util.List<String> list) {
		final List itextList = new List(List.ORDERED);
		list.forEach(item -> itextList.add(createListItem(item)));
		return itextList;
	}

	@Nonnull
	public static Paragraph createListInParagraph(java.util.List<String> list, final int emptyLinesAfter) {
		Paragraph paragraph = new Paragraph();
		final List itextList = createList(list);
		paragraph.setSpacingAfter(emptyLinesAfter
			* PdfConstants.DEFAULT_FONT_SIZE
			* PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		paragraph.add(itextList);
		return paragraph;
	}

	public static void setTableDefaultStyles(PdfPTable table) {
		table.setSpacingBefore(0);
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		table.setKeepTogether(true);
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		table.getDefaultCell().setPadding(0);
		table.getDefaultCell().setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
	}

	public static ListItem createListItem(@Nonnull final String string) {
		ListItem listItem = new ListItem(string, PdfConstants.DEFAULT_FONT);
		return listItem;
	}

	@Nonnull
	public static PdfPTable createTable(
		java.util.List<String[]> values,
		final float[] columnWidths,
		final int[] alignement,
		final int emptyLinesAfter
	) {
		PdfPTable table = new PdfPTable(columnWidths.length);
		try {
			table.setWidths(columnWidths);
		} catch (DocumentException e) {
			LOG.error("Failed to set the width: {}", e.getMessage(), e);
		}
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		table.setHeaderRows(1);
		boolean first = true;
		for (String[] value : values) {
			for (int j = 0; j < value.length; j++) {
				PdfPCell cell;
				if (first) {
					cell = PdfUtil.createTitleCell(value[j]);
				} else {
					cell = new PdfPCell(new Phrase(value[j], PdfConstants.DEFAULT_FONT));
				}
				cell.setHorizontalAlignment(alignement[j]);
				cell.setLeading(0.0F, PdfConstants.DEFAULT_CELL_LEADING);
				table.addCell(cell);
			}
			first = false;
		}
		table.setSpacingAfter(DEFAULT_MULTIPLIED_LEADING * PdfConstants.FONT_SIZE * emptyLinesAfter);
		return table;
	}

	@Nonnull
	public static String printString(@Nullable String stringOrNull) {
		if (stringOrNull != null) {
			return stringOrNull;
		}
		return "";
	}


	@Nonnull
	public static String printPercent(@Nullable BigDecimal percent) {
		if (percent != null) {
			return percent + "%";
		}
		return "";
	}

	public static Chunk createSuperTextInText(final String supertext) {
		return createSuperTextInText(supertext, 5, 3);
	}

	public static Chunk createSuperTextInText(final  String supertext, int fontSize, int textRise) {
		final Chunk chunk = new Chunk(supertext, createFontWithSize(PdfConstants.DEFAULT_FONT, fontSize));
		chunk.setTextRise(textRise);
		return chunk;
	}

	@Nonnull
	public static Font createFontWithSize(@Nonnull Font originatingFont, float size) {
		Font newFont = new Font(originatingFont);
		newFont.setSize(size);
		return newFont;
	}

	@Nonnull
	public static Font createFontWithColor(@Nonnull Font originatingFont, @Nonnull Color color) {
		Font newFont = new Font(originatingFont);
		newFont.setColor(color);
		return newFont;
	}

	public static void addLineSeparator(@NonNull Document document, int emptyLinesAfter) {
		LineSeparator separator = new LineSeparator();
		separator.setLineWidth(0.5f);
		document.add(separator);
		document.add(PdfUtil.createParagraph(" ", emptyLinesAfter));
	}
}
