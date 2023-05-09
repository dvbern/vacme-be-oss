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

import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent;
import ch.dvbern.lib.invoicegenerator.dto.component.AddressRenderer;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.ColumnText;

import static ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent.PP_PADDING_BOTTOM;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.joinListToString;

import javax.annotation.Nonnull;
import java.util.List;

public class CustomAdressRenderer extends AddressRenderer {

	public CustomAdressRenderer(@Nonnull AddressComponent componentConfiguration, @Nonnull List<String> payload) {
		super(componentConfiguration, payload);
	}

	@Override
	protected void render(
		@Nonnull ColumnText columnText,
		@Nonnull List<String> payload,
		@Nonnull PageConfiguration pageConfiguration) {
		AddressComponent componentConfiguration = getComponentConfiguration();
		String pp = componentConfiguration.getPp();
		if (pp != null) {
			Font underlined = PdfConstants.ABSENDER_FONT;
			underlined.setStyle(Font.UNDERLINE);
			Paragraph ppParagraph = new Paragraph(pp, underlined);
			ppParagraph.setSpacingAfter(PP_PADDING_BOTTOM);
			columnText.addElement(ppParagraph);
		}
		Paragraph address = new Paragraph(joinListToString(payload), PdfConstants.ADRESSE_FONT);
		address.setMultipliedLeading(1);
		columnText.addElement(address);
	}
}

