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
import java.time.format.DateTimeFormatter;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;

import static com.lowagie.text.pdf.BaseFont.EMBEDDED;
import static com.lowagie.text.pdf.BaseFont.IDENTITY_H;

public final class PdfConstants {

	// Muss vor den FontFactory.getFont aufrufen definiert werden
	static {
		FontFactory.register("/font/arial.ttf", "arial");
	}

	public static final String FONT_ARIAL = "arial";

	public static final float ADRESSE_FONT_SIZE = 8.5f;
	public static final float ABSENDER_FONT_SIZE = 6.5f;

	public static final float DEFAULT_FONT_SIZE = 10.5f;
	public static final float FONT_SIZE = 10.5f;
	public static final float FONT_SIZE_H1 = 10.5f;
	public static final float FONT_SIZE_H2 = 10.5f;

	public static final Font DEFAULT_FONT = FontFactory.getFont(FONT_ARIAL, IDENTITY_H, EMBEDDED,
		FONT_SIZE, Font.NORMAL, Color.BLACK);
	public static final Font DEFAULT_FONT_BOLD = FontFactory.getFont(FONT_ARIAL, IDENTITY_H, EMBEDDED,
		FONT_SIZE, Font.BOLD, Color.BLACK);
	public static final Font ADRESSE_FONT = FontFactory.getFont(FONT_ARIAL, IDENTITY_H, EMBEDDED,
		ADRESSE_FONT_SIZE, Font.NORMAL, Color.BLACK);
	public static final Font ABSENDER_FONT = FontFactory.getFont(FONT_ARIAL, IDENTITY_H, EMBEDDED,
		ABSENDER_FONT_SIZE, Font.NORMAL, Color.BLACK);
	public static final Font FONT_TITLE = FontFactory.getFont(FONT_ARIAL, IDENTITY_H, EMBEDDED,
		FONT_SIZE_H1, Font.BOLD, Color.BLACK);
	public static final Font FONT_H1 = FontFactory.getFont(FONT_ARIAL, IDENTITY_H, EMBEDDED,
		FONT_SIZE_H1, Font.BOLD, Color.BLACK);
	public static final Font FONT_H2 = FontFactory.getFont(FONT_ARIAL, IDENTITY_H, EMBEDDED,
		FONT_SIZE_H2, Font.BOLD, Color.BLACK);
	public static final Font FONT_RED = FontFactory.getFont(FONT_ARIAL, IDENTITY_H, EMBEDDED,
		FONT_SIZE, Font.NORMAL, Color.RED);

	public static final float DEFAULT_CELL_LEADING = 1.0F;

	public static final String PATTERN_DATE = "dd.MM.yyyy";
	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_DATE);

	private PdfConstants() {
		// nop
	}
}
