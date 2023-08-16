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

import java.util.List;

import ch.dvbern.lib.invoicegenerator.dto.BaseLayoutConfiguration;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import org.checkerframework.checker.nullness.qual.NonNull;



/**
 * Factory, welche die jeweils richtigen Konfigurationen (Texte, Layout der PDFs, etc.) fuer verschiedene
 * Mandanten zurueckliefert
 */
public final class MandantFactory {


	private MandantFactory() {
		// util
	}

	@NonNull
	public static BaseLayoutConfiguration getLayoutConfiguration(@NonNull List<String> absenderBlock, boolean kantonsHeader) {
		if (Mandant.BE == MandantUtil.getMandant()) {
			return new PdfLayoutConfigurationBE(absenderBlock, kantonsHeader);
		}
		if (Mandant.ZH == MandantUtil.getMandant()) {
			return new PdfLayoutConfigurationZH(absenderBlock);
		}
		throw new IllegalArgumentException("Unbekannter Mandant " + MandantUtil.getMandantProperty());
	}


}
