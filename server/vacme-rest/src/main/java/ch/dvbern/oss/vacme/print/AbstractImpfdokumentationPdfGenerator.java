/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of
 *  the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.print;

import java.text.NumberFormat;
import java.util.Locale;

import ch.dvbern.lib.invoicegenerator.pdf.PdfElementGenerator;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.print.base.PdfConstants;
import ch.dvbern.oss.vacme.print.base.PdfUtil;
import ch.dvbern.oss.vacme.print.base.VacmePdfGenerator;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractImpfdokumentationPdfGenerator extends VacmePdfGenerator {

	protected AbstractImpfdokumentationPdfGenerator(
		@NotNull Registrierung registrierung,
		@NonNull KantonaleBerechtigung kantonaleBerechtigung
	) {
		super(registrierung, kantonaleBerechtigung);
	}

	@NonNull
	protected PdfPTable createVaccinationCertificateFooter(@NonNull String key) {
		PdfPTable table = new PdfPTable(1);
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		PdfPTable innerTable = new PdfPTable(1);
		innerTable.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		innerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		innerTable.getDefaultCell().setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		innerTable.addCell(PdfUtil.createParagraph(translate(key, Locale.GERMAN)));
		table.addCell(innerTable);
		return table;
	}

	@NonNull
	protected String buildImpfungString(@NonNull Impfung impfung) {
		return PdfConstants.DATE_FORMATTER.format(impfung.getTimestampImpfung()) + ", " +
			impfung.getImpfstoff().getHersteller() + " - " +
			impfung.getImpfstoff().getName() + ", " +
			"GTIN " + impfung.getImpfstoff().getCode() + ", " +
			"Lot " + impfung.getLot() + ", " +
			NumberFormat.getInstance().format(impfung.getMenge()) + "ml" +
			(impfung.isGrundimmunisierung() ? "" : ' ' + translate("print_impfdokumentation_booster")) +
			(impfung.isExtern() ? "" : ", " + impfung.getTermin().getImpfslot().getOrtDerImpfung().getName()) +
			(impfung.isSelbstzahlende() ? ", " + translate("print_impfdokumentation_selbstzahlende") : "") +
			(impfung.isExtern() ?
				", " + translate("print_impfdokumentation_extern") + impfung.getTermin()
					.getImpfslot()
					.getOrtDerImpfung()
					.getName() :
				"");
	}
}
