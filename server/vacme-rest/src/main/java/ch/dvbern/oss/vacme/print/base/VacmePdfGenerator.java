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
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.lib.invoicegenerator.dto.BaseLayoutConfiguration;
import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import ch.dvbern.oss.vacme.print.base.PdfGenerator.CustomGenerator;
import ch.dvbern.oss.vacme.util.QRCodeUtil;
import com.google.zxing.WriterException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

@Slf4j
public abstract class VacmePdfGenerator {

	@Nonnull
	private PdfGenerator pdfGenerator;

	protected Registrierung registrierung;

	private boolean kantonsHeader;

	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod") // Stimmt nicht, die Methode ist final
	protected VacmePdfGenerator(@Nonnull Registrierung registrierung) {
		this(registrierung, null);
	}

	public VacmePdfGenerator(@NonNull Registrierung registrierung, @Nullable KantonaleBerechtigung kantonaleBerechtigung) {
		this.registrierung = registrierung;
		this.kantonsHeader = kantonaleBerechtigung == null ? true : !kantonaleBerechtigung.isLeistungserbringer();
		initGenerator();
	}

	@Nonnull
	protected abstract String getDocumentTitle();

	@NotNull
	protected List<String> getEmpfaengerAdresse() {
		return Arrays.asList(
			registrierung.getNameVorname(),
			registrierung.getAdresse().getAdresse1(),
			registrierung.getAdresse().getPlz() + ' ' + registrierung.getAdresse().getOrt(),
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			translate("print_datumOrt_ort") + PdfConstants.DATE_FORMATTER.format(LocalDate.now()));
	}

	@Nonnull
	protected abstract CustomGenerator getCustomGenerator();

	public void generate(@Nonnull final OutputStream outputStream) throws InvoiceGeneratorException {
		getPdfGenerator().generate(
			outputStream,
			getDocumentTitle(),
			getEmpfaengerAdresse(),
			getCustomGenerator());
	}

	@Nonnull
	protected PdfGenerator getPdfGenerator() {
		return pdfGenerator;
	}

	@Nonnull
	public PageConfiguration getPageConfiguration() {
		return pdfGenerator.getConfiguration();
	}

	private void initGenerator() {
		BaseLayoutConfiguration layoutConfiguration =
			MandantFactory.getLayoutConfiguration(getAbsenderBlock(), kantonsHeader);
		this.pdfGenerator = PdfGenerator.create(layoutConfiguration);
	}

	@Nonnull
	protected final List<String> getAbsenderBlock() {
		List<String> absender = new ArrayList<>();
		if (kantonsHeader) {
			absender.addAll(getAbsenderAdresse());
			absender.addAll(getKontaktdaten());
		}
		return absender;
	}

	@Nonnull
	protected List<String> getAbsenderAdresse() {
		return Arrays.asList(
			translate("print_absender_line1"),
			translate("print_absender_line2"),
			translate("print_absender_line3"),
			translate("print_absender_line4"),
			translate("print_absender_line5"),
			translate("print_absender_line6"),
			translate("print_absender_line7"),
			translate("print_absender_line8"));
	}

	@Nonnull
	private List<String> getKontaktdaten() {
		return Arrays.asList(
			translate("print_kontakt_line1"),
			translate("print_kontakt_line2"),
			translate("print_kontakt_line3"));
	}

	public void addBarcode(@NonNull Document document, @Nullable KrankheitIdentifier krankheitIdentifier) {
		try {
			String url = QRCodeUtil.generateQrCodeUrl(registrierung.getRegistrierungsnummer(), krankheitIdentifier);

			Image image = Image.getInstance(QRCodeUtil.createQRImage(url, 100));
			image.setWidthPercentage(100);

			PdfPTable table = new PdfPTable(1);
			table.setHeaderRows(0);
			table.setHorizontalAlignment(Element.ALIGN_LEFT);
			table.setTotalWidth(420);
			table.setWidths(new float[] { 1.0f });
			table.setLockedWidth(true);
			PdfUtil.setTableDefaultStyles(table);
			table.getDefaultCell().setBorder(0);

			// Ihr persoenlicher Code
			PdfPCell cell1 =
				new PdfPCell(new Phrase(
					translateAllConfiguredLanguages("qrcode_title", "\n"),
					PdfConstants.DEFAULT_FONT));
			cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell1.setBorder(0);
			table.addCell(cell1);

			// QR Code
			table.addCell(createCell(image));

			// Registrierungsnummer
			Chunk chunk = new Chunk(registrierung.getRegistrierungsnummer(), PdfConstants.FONT_TITLE);
			chunk.setCharacterSpacing(5);
			Phrase phrase = new Phrase(chunk);
			table.addCell(createCell(phrase));

			// Ihre Prioritaet
			table.addCell(createCell(new Phrase(
				translateAllConfiguredLanguages("qrcode_priorisierung", " / "),
				PdfConstants.DEFAULT_FONT)));

			// Prio-Code
			table.addCell(createCell(new Phrase(
				getPrioritaetText(registrierung.getPrioritaet()),
				PdfConstants.FONT_TITLE)));

			document.add(table);
			document.add(PdfUtil.createParagraph(" ", 1));

		} catch (IOException | DocumentException | WriterException e) {
			LOG.error("Failed to add the Barcode: {}", e.getMessage(), e);
		}
	}

	@NonNull
	private String getPrioritaetText(final Prioritaet prioritaet) {
		if (prioritaet == Prioritaet.Z) {
			return translateAllConfiguredLanguages("print_ortDerImpfung", " / ");
		}
		return prioritaet.getCode();
	}

	@NonNull
	public PdfPCell createEmptyCell() {
		PdfPCell cell = new PdfPCell();
		cell.setBorder(0);
		return cell;
	}

	@NonNull
	public PdfPCell createCell(@NonNull Phrase content) {
		PdfPCell cell = new PdfPCell(content);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setBorder(0);
		return cell;
	}

	@NonNull
	public PdfPCell createLeftCell(@NonNull Phrase content) {
		PdfPCell cell = new PdfPCell(content);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		cell.setBorder(0);
		return cell;
	}

	@NonNull
	public PdfPCell createCell(@NonNull Image content) {
		PdfPCell cell = new PdfPCell(content);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setBorder(0);
		return cell;
	}

	@NonNull
	protected String translate(@NonNull String key) {
		return translate(key, Locale.GERMAN);
	}

	@Nonnull
	protected String translate(@NonNull String key, @NonNull Locale locale) {
		return ServerMessageUtil.getMessage(key, locale);
	}

	@Nonnull
	protected String translate(@NonNull String key, @NonNull Locale locale, @NonNull Object... args) {
		return ServerMessageUtil.getMessage(key, locale, args);
	}

	@Nonnull
	protected String translateAllConfiguredLanguages(@NonNull String key, @NonNull String separator) {
		return ServerMessageUtil.getMessageAllConfiguredLanguages(key, separator);
	}

	@Nonnull
	protected String translateAllLanguages(@NonNull String key, @NonNull String separator) {
		return ServerMessageUtil.getMessageAllLanguages(key, separator);
	}

	@Nonnull
	protected String translateAllLanguagesWithArgs(@NonNull String key, @NonNull String separator, Object... args) {
		return ServerMessageUtil.getMessageAllLanguagesWithArgs(key, separator, args);
	}

	protected interface BlockPerLanguage {
		void run(Locale locale);
	}

	protected void doForAllLanguagesSeparated(
		@NonNull BlockPerLanguage blockPerLanguage,
		@NonNull Runnable separatorBlock) {
		blockPerLanguage.run(Locale.GERMAN);
		separatorBlock.run();
		blockPerLanguage.run(Locale.FRENCH);
		if (Mandant.ZH == MandantUtil.getMandant()) {
			separatorBlock.run();
			blockPerLanguage.run(Locale.ENGLISH);
		}
	}

	protected void doForAllLanguagesSeparated(@NonNull BlockPerLanguage blockPerLanguage, @NonNull Document document) {
		Runnable defaultSeparator = () -> PdfUtil.addLineSeparator(document, 0);
		doForAllLanguagesSeparated(blockPerLanguage, defaultSeparator);
	}
}
