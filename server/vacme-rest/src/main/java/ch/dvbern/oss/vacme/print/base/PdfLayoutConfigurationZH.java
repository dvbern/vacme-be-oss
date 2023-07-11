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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.lib.invoicegenerator.dto.BaseLayoutConfiguration;
import ch.dvbern.lib.invoicegenerator.dto.OnPage;
import ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent;
import ch.dvbern.lib.invoicegenerator.dto.component.Logo;
import ch.dvbern.lib.invoicegenerator.dto.component.PhraseRenderer;
import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;
import com.lowagie.text.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent.ADRESSE_HEIGHT;
import static ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent.ADRESSE_WIDTH;

public class PdfLayoutConfigurationZH extends BaseLayoutConfiguration {

	private static final float LEFT_PAGE_MARGIN_MM = 35.0F;
	private static final int ABSENDER_LEFT_IN_MM = 143;
	private static final int ABSENDER_TOP_IN_MM = 18;

	private static final int RECEIVER_TOP_IN_MM = 55;

	public static final int LOGO_LEFT_IN_MM = 10;
	public static final int LOGO_TOP_IN_MM = 8;
	private static final int LOGO_MAX_WIDTH_IN_MM = 130;
	private static final int LOGO_MAX_HEIGHT_IN_MM = 35;

	private static final String LOGO_PATH = "/logo/logo_kanton_zuerich.png";

	private static final Logger LOG = LoggerFactory.getLogger(PdfLayoutConfigurationZH.class);

	public PdfLayoutConfigurationZH(final List<String> absenderHeader) {
		super(new AddressComponent(
			ServerMessageUtil.getMessage("print_absenderHeader", Locale.GERMAN),
			LEFT_PAGE_MARGIN_MM,
			RECEIVER_TOP_IN_MM,
			ADRESSE_WIDTH,
			ADRESSE_HEIGHT,
			OnPage.FIRST));
		applyLogo(getLogoContent());
		setTopMarginInPoints(Utilities.millimetersToPoints(100f));
		setBottomMarginInPoints(Utilities.millimetersToPoints(10f));
		setLeftPageMarginInPoints(Utilities.millimetersToPoints(LEFT_PAGE_MARGIN_MM));
		if (absenderHeader != null && !absenderHeader.isEmpty()) {
			setHeader(new PhraseRenderer(absenderHeader,
				ABSENDER_LEFT_IN_MM,
				ABSENDER_TOP_IN_MM,
				ADRESSE_WIDTH,
				ADRESSE_HEIGHT, PdfConstants.ADRESSE_FONT));
		}
	}

	@Nonnull
	private byte[] getLogoContent() {
		try {
			return VacmePdfGenerator.class.getResourceAsStream(LOGO_PATH).readAllBytes();
		} catch (IOException e) {
			throw new AppFailureException("Could not load logo on path " + LOGO_PATH, e);
		}
	}

	private void applyLogo(final byte[] logo) {
		if (logo == null || logo.length == 0) {
			return;
		}

		try {
			Image image = Image.getInstance(logo);
			final float imageWidthInMm = Utilities.pointsToMillimeters(image.getWidth());
			final float imageHeightInMm = Utilities.pointsToMillimeters(image.getHeight());
			float widthInMm = Math.min(LOGO_MAX_WIDTH_IN_MM, imageWidthInMm);
			if (imageHeightInMm > LOGO_MAX_HEIGHT_IN_MM) {
				final float factor = LOGO_MAX_HEIGHT_IN_MM / imageHeightInMm;
				widthInMm = Math.min(widthInMm, imageWidthInMm * factor);
			}
			Logo logoToApply = new Logo(
				logo,
				LOGO_LEFT_IN_MM,
				LOGO_TOP_IN_MM,
				widthInMm);
			setLogo(logoToApply);
		} catch (IOException | BadElementException e) {
			LOG.error("Failed to read the Logo: {}", e.getMessage(), e);
		}
	}
}
