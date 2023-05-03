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

package ch.dvbern.oss.vacme.print.archivierung;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.dvbern.oss.vacme.entities.base.ZertifikatInfo;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFile;
import ch.dvbern.oss.vacme.entities.embeddables.Adresse;
import ch.dvbern.oss.vacme.entities.embeddables.FileBlob;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.ImpfungkontrolleTermin;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsart;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsort;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsseite;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import ch.dvbern.oss.vacme.service.PdfArchivierungService;
import ch.dvbern.oss.vacme.service.covidcertificate.CovidCertUtils;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.util.MimeType;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ImpfinformationenUtil;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Tab;
import com.itextpdf.layout.element.TabStop;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.property.TabAlignment;
import com.itextpdf.pdfa.PdfADocument;
import com.lowagie.text.Utilities;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * PDF Generator fuer das Archivierungspdf
 */
public class ArchivierungPdfGenerator {

	private static final String INTENT = "/font/sRGB.profile";
	private static final String FONT_ARIAL = "/font/arial.ttf";
	private static final float TAB_WIDTH = 150f;
	private static final float LINE_SEPARATION = 15f;
	private static final float PAGE_MARGIN_MM = 30.0F;

	@NonNull
	private final Fragebogen fragebogen;

	@NonNull
	private final ImpfinformationDto impfinformationDto;

	@NonNull
	private final List<ZertifikatInfo> zertifikate;

	private PdfADocument pdf;
	private PdfFont font;

	public ArchivierungPdfGenerator(
		@NonNull Fragebogen fragebogen,
		@NonNull ImpfinformationDto impfinformationDto,
		@NonNull List<ZertifikatInfo> zertifikate
	) {
		this.fragebogen = fragebogen;
		this.impfinformationDto = impfinformationDto;
		// Zertifikate sortieren, beginnend mit dem aeltesten
		this.zertifikate = zertifikate
			.stream()
			.sorted(Comparator.comparing(ZertifikatInfo::getTimestampZertifikatErstellt))
			.collect(Collectors.toList());
	}

	public void createArchivierung(@NonNull OutputStream outputStream) {
		createArchivierung(outputStream, null);
	}

	public void createArchivierung(@NonNull OutputStream outputStream, @Nullable List<ImpfdossierFile> uploadedFiles) {
		PdfOutputIntent outputIntent = getOutputIntent();
		this.font = getFontArial();
		this.pdf = new PdfADocument(new PdfWriter(outputStream), PdfAConformanceLevel.PDF_A_3U, outputIntent);
		fillPDF(uploadedFiles);
	}

	public static PdfOutputIntent getOutputIntent() {
		try {
			InputStream intentStream = PdfArchivierungService.class.getResourceAsStream(INTENT);
			PdfOutputIntent outputIntent = new PdfOutputIntent("", "", "", "sRGB", intentStream);
			if (intentStream != null) {
				intentStream.close();
			}
			return outputIntent;
		} catch (IOException e) {
			throw new AppFailureException("Bei der Generierung des Dokuments ist ein Fehler aufgetreten", e);
		}
	}

	public static PdfFont getFontArial() {
		try {
			final InputStream fontResource = PdfArchivierungService.class.getResourceAsStream(FONT_ARIAL);
			Objects.requireNonNull(fontResource);
			byte[] fontBytes = fontResource.readAllBytes();
			return PdfFontFactory.createFont(fontBytes, PdfEncodings.WINANSI, true);
		} catch (IOException e) {
			throw new AppFailureException("Bei der Generierung des Dokuments ist ein Fehler aufgetreten", e);
		}
	}

	public String getArchiveTitle() {
		return String.format("%s, %s",
			getRegistrierung().getNameVorname(),
			DateUtil.formatDate(getRegistrierung().getGeburtsdatum(), Locale.GERMAN)
		);
	}

	private void fillPDF(@Nullable List<ImpfdossierFile> uploadedFiles) {
		String title = getArchiveTitle();
		pdf.getDocumentInfo().setTitle(title);
		Document document = new Document(pdf, PageSize.A4);
		document.add(createTitle(title, document));
		document.setFontSize(10);
		document.add(createKeyValue("Registrierungsnummer", getRegistrierung().getRegistrierungsnummer()));
		document.add(createKeyValue("Registrierung Eingang", getRegistrierungEingangString()));
		document.add(createKeyValue("Status", impfinformationDto.getImpfdossier().getDossierStatus().name()));
		addPersonalien(document);
		addEinwilligung(document);
		addSelbstevaluation(document);

		if (uploadedFiles != null) {
			String filenamesString = uploadedFiles.stream()
				.map(file -> file.getFileBlob().getFileName().getFileName())
				.collect(Collectors.joining(", "));
			document.add(createKeyValue("Angehaengte Dokumente", filenamesString));
		}
		if (impfinformationDto.getImpfung1() != null) {
			addImpfung(1, impfinformationDto.getImpfung1(),
				impfinformationDto.getImpfdossier().getImpfungkontrolleTermin1(), document);
		}
		if (impfinformationDto.getImpfung2() != null) {
			addImpfung(2, impfinformationDto.getImpfung2(),
				impfinformationDto.getImpfdossier().getImpfungkontrolleTermin2(), document);
		}
		if (impfinformationDto.getBoosterImpfungen() != null) {
			for (Impfung impfung : impfinformationDto.getBoosterImpfungen()) {
				final Impfdossiereintrag dossiereintrag = ImpfinformationenUtil.getDossiereintragForImpfung(impfinformationDto, impfung);
				Objects.requireNonNull(dossiereintrag);
				final ImpfungkontrolleTermin impfungkontrolleTermin =
					dossiereintrag.getImpfungkontrolleTermin();
				addImpfung(dossiereintrag.getImpffolgeNr(), impfung, impfungkontrolleTermin, document);
			}
		}

		if (uploadedFiles != null) {
			mergeFile(uploadedFiles, document);
		}

		document.close();
	}

	private void addPersonalien(@NonNull Document document) {
		document.add(createParagraphTitle("Personalien"));
		document.add(createKeyValue("Name", getRegistrierung().getName()));
		document.add(createKeyValue("Vorname", getRegistrierung().getVorname()));
		document.add(createKeyValue("Geburtsdatum", DateUtil.formatDate(getRegistrierung().getGeburtsdatum(), Locale.GERMAN)));
		document.add(createKeyValue("Geschlecht", ServerMessageUtil.translateEnumValue(getRegistrierung().getGeschlecht(), Locale.GERMAN)));

		Adresse adresse = getRegistrierung().getAdresse();
		List<String> adresseList = new ArrayList<>();
		adresseList.add(adresse.getAdresse1());
		if (adresse.getAdresse2() != null) {
			adresseList.add(adresse.getAdresse2());
		}
		adresseList.add(adresse.getPlzOrt());

		addKeyValues("Adresse", adresseList, document);
	}

	private void addEinwilligung(@NonNull Document document) {
		document.add(createParagraphTitle("Einwilligung"));
		document.add(createKeyValue("Einverständniserklärung", convertBool(true)));
		document.add(createKeyValue("Nutzungsbedingungen", convertBool(true)));
		document.add(createKeyValue("Zustimmung elektronischer Impfausweis",
			convertBool(getRegistrierung().isAbgleichElektronischerImpfausweis())));
		document.add(createKeyValue("Informationen zur Impfung", convertBool(true)));
		document.add(createKeyValue("Bemerkungen bei Registration",
			getRegistrierung().getBemerkung() != null ? getRegistrierung().getBemerkung() : ""));
	}

	private void addSelbstevaluation(@NonNull Document document) {
		Paragraph paragraph = createParagraphTitle("Selbstevaluation Fragebogen:");
		paragraph.addTabStops(new TabStop(TAB_WIDTH));
		paragraph.add(new Tab());
		Text text;
		switch (fragebogen.getAmpel()) {
		case RED:
			text = new Text("Rot");
			break;
		case GREEN:
			text = new Text("Grün");
			break;
		case ORANGE:
			text = new Text("Orange");
			break;
		default:
			text = new Text("Unbekannt");
			break;
		}
		text.setUnderline(0, 0);
		text.setFont(font);
		paragraph.add(text);
		document.add(paragraph);
	}

	private void addImpfung(
		@NonNull Integer impffolgeNr,
		@NonNull Impfung impfung,
		@Nullable ImpfungkontrolleTermin kontrolle,
		@NonNull Document document
	) {
		document.add(createParagraphTitle(String.format("%d. Impfung", impffolgeNr)));
		document.add(createKeyValue("Impfschritt", impfung.getTermin().getImpffolge().name()));
		document.add(createKeyValue("Einverständnis", convertBool(impfung.isEinwilligung())));
		document.add(createKeyValue("Fieber", convertBool(impfung.isFieber())));
		document.add(createKeyValue("Neue Erkrankungen", convertBool(impfung.isNeueKrankheit())));
		document.add(createKeyValue("Keine besonderen Umstände", convertBool(
			impfung.getKeineBesonderenUmstaende() != null ? impfung.getKeineBesonderenUmstaende() : false)));
		document.add(createKeyValue("Schwanger", convertBool(impfung.getSchwanger())));
		String kontrolleBemerkung = "";
		if (kontrolle != null && kontrolle.getBemerkung() != null) {
			kontrolleBemerkung = kontrolle.getBemerkung();
		}
		document.add(createKeyValue("Bemerkungen bei Kontrolle", kontrolleBemerkung));
		document.add(createKeyValue("Bemerkungen bei Impfung",
			impfung.getBemerkung() != null ? impfung.getBemerkung() : ""));
		LocalDateTime impfungDateTime = impfung.getTimestampImpfung();
		document.add(createKeyValue("Datum", DateUtil.formatDate(impfungDateTime)));
		document.add(createKeyValue("Zeit", DateUtil.formatLocalTime(impfungDateTime)));
		document.add(createKeyValue("Impfstoff", impfung.getImpfstoff().getName()));
		document.add(createKeyValue("Charge/Lot", impfung.getLot()));
		document.add(createKeyValue("Menge", impfung.getMenge().toPlainString()));
		document.add(createKeyValue("Verarbreichungsart", getVerarbreichungsArtString(impfung.getVerarbreichungsart())));
		document.add(createKeyValue("Verarbreichungsort", getVerarbreichungsOrtString(impfung.getVerarbreichungsort())));
		document.add(createKeyValue("Verarbreichungsseite", getVerarbreichungsSeiteString(impfung.getVerarbreichungsseite())));

		OrtDerImpfung odi = impfung.getTermin().getImpfslot().getOrtDerImpfung();
		String odiString = odi.getName();
		if (odi.getGlnNummer() != null) {
			odiString +=", " + odi.getGlnNummer();
		}
		document.add(createKeyValue("Ort der impfung", odiString));

		Benutzer fachverantwortungPerson = impfung.getBenutzerVerantwortlicher();
		String fachverantwortungString = fachverantwortungPerson.getName() + ' ' + fachverantwortungPerson.getVorname();
		if (fachverantwortungPerson.getGlnNummer() != null) {
			fachverantwortungString += ", " + fachverantwortungPerson.getGlnNummer();
		}
		document.add(createKeyValue("Fachverantwortung Person", fachverantwortungString));

		Benutzer ausfuehrendePerson = impfung.getBenutzerDurchfuehrend();
		String ausfuehrendeString = ausfuehrendePerson.getName() + ' ' + ausfuehrendePerson.getVorname();
		if (ausfuehrendePerson.getGlnNummer() != null) {
			ausfuehrendeString += ", " + ausfuehrendePerson.getGlnNummer();
		}
		document.add(createKeyValue("Ausführende Person", ausfuehrendeString));

		String key = "Zertifkate"; // Label nur beim ersten Zertifikat
		for (ZertifikatInfo zertifikatInfo : zertifikate) {
			if (impfung.getId().equals(zertifikatInfo.getImpfungId())) {
				document.add(createKeyValue(key,
					DateUtil.DEFAULT_DATETIME_FORMAT.apply(Locale.GERMAN).format(zertifikatInfo.getTimestampZertifikatErstellt())
						+ ", " + CovidCertUtils.readImpfungCounterString(zertifikatInfo)
						+ ", " + zertifikatInfo.getUvci()));
				key = "";
			}
		}
	}

	private Paragraph createTitle(@NonNull String title, @NonNull Document document) {
		Paragraph paragraph = getCenteredParagraph(title, document);
		paragraph.setFixedLeading(50);
		paragraph.setBold().setFontSize(20f);
		return paragraph;
	}

	private Paragraph createParagraphTitle(@NonNull String text) {
		Paragraph paragraph = new Paragraph(text);
		paragraph.setFixedLeading(25);
		paragraph.setFont(font);
		paragraph.setBold();
		paragraph.setUnderline();
		return paragraph;
	}

	private Paragraph createKeyValue(@NonNull String key, @NonNull String value) {
		Paragraph paragraph = new Paragraph();
		paragraph.setMargin(0);
		paragraph.setFixedLeading(LINE_SEPARATION);
		paragraph.addTabStops(new TabStop(TAB_WIDTH));
		paragraph.setFirstLineIndent(-TAB_WIDTH);
		paragraph.setPaddingLeft(TAB_WIDTH);
		if (StringUtils.isNotEmpty(key)) {
			paragraph.add(String.format("%s:", key));
		}
		paragraph.add(new Tab());
		paragraph.add(value);
		paragraph.setFont(font);
		return paragraph;
	}

	private void addKeyValues(@NonNull String key, @NonNull List<String> values, @NonNull Document document
	) {
		Paragraph keyParagraph = new Paragraph();
		keyParagraph.setFixedLeading(LINE_SEPARATION);
		keyParagraph.setFirstLineIndent(-TAB_WIDTH);
		keyParagraph.setPaddingLeft(TAB_WIDTH);
		keyParagraph.setMargin(0);
		keyParagraph.add(String.format("%s:", key));
		keyParagraph.setFont(font);

		if (values.size() > 0) {
			keyParagraph.addTabStops(new TabStop(TAB_WIDTH));
			keyParagraph.add(new Tab());
			keyParagraph.add(values.get(0));
			document.add(keyParagraph);

			for (String value : values.subList(1, values.size())) {
				Paragraph paragraph = new Paragraph();
				paragraph.setFixedLeading(LINE_SEPARATION);
				paragraph.setFont(font);
				paragraph.setMargin(0);
				paragraph.setPaddingLeft(TAB_WIDTH);
				paragraph.add(value);
				document.add(paragraph);
			}
		} else {
			document.add(keyParagraph);
		}
	}

	private void mergeFile(@NonNull List<ImpfdossierFile> registrierungFileList,  @NonNull Document document) {
		for (ImpfdossierFile impfdossierFile : registrierungFileList) {
			FileBlob fileBlob = impfdossierFile.getFileBlob();
			if (fileBlob.getMimeType().equals(MimeType.APPLICATION_OCTET_STREAM)) {
				throw new AppFailureException("Bei der Generierung des Dokuments ist ein Fehler aufgetreten");
			}
			if (fileBlob.getMimeType().equals(MimeType.APPLICATION_PDF)) {
				mergePDF(fileBlob);
			} else {
				document.add(new AreaBreak(AreaBreakType.LAST_PAGE));
				document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
				com.itextpdf.layout.element.Image image = new com.itextpdf.layout.element.Image(ImageDataFactory.create(impfdossierFile.getContent()));
				image.scaleToFit(com.lowagie.text.PageSize.A4.getWidth() - Utilities.millimetersToPoints(PAGE_MARGIN_MM)  , com.lowagie.text.PageSize.A4.getHeight() - Utilities.millimetersToPoints(PAGE_MARGIN_MM) );
				document.add(image);
			}
		}
	}

	private void mergePDF(@NonNull FileBlob fileBlob) {
		PdfMerger pdfMerger = new PdfMerger(pdf);
		try {
			PdfDocument regFile = new PdfDocument(new PdfReader(fileBlob.getData().getBinaryStream()));
			// Fuer PDF A darf der interpolate Key nicht gesetzt sein
			for (int i = 0; i < regFile.getNumberOfPdfObjects(); i++) {
				PdfObject object = regFile.getPdfObject(i);
				if (object instanceof PdfStream) {
					((PdfStream) object).remove(PdfName.Interpolate);
				}
			}
			pdfMerger.merge(regFile, 1, regFile.getNumberOfPages());
		} catch (IOException | SQLException e) {
			throw new AppFailureException("Bei der Generierung des Dokuments ist ein Fehler aufgetreten", e);
		}
	}

	private String convertBool(@Nullable Boolean value) {
		if (value == null) {
			return "";
		}
		return value ? "Ja" : "Nein";
	}

	private Paragraph getCenteredParagraph(@NonNull String text, @NonNull Document document) {
		Rectangle pageSize = pdf.getDefaultPageSize();
		float width = pageSize.getWidth() - document.getLeftMargin() - document.getRightMargin();

		Paragraph paragraph = new Paragraph()
			.addTabStops(new TabStop(width / 2, TabAlignment.CENTER));
		paragraph.setFont(font);
		paragraph.setBold();
		paragraph
			.add(new Tab())
			.add(text);

		return paragraph;
	}

	private String getVerarbreichungsArtString(Verarbreichungsart art) {
		switch (art) {
		case SUBKUTAN:
			return "Subkutan";
		case INTRA_MUSKULAER:
			return "Intramuskulär";
		case INTRADERMAL:
			return "Intradermal";
		}
		return "";
	}

	private String getVerarbreichungsSeiteString(Verarbreichungsseite seite) {
		switch (seite) {
		case LINKS:
			return "Links";
		case RECHTS:
			return "Rechts";
		}
		return "";
	}

	private String getVerarbreichungsOrtString(Verarbreichungsort ort) {
		switch (ort) {
		case OBERARM:
			return "Oberarm";
		case UNTERARM:
			return "Unterarm";
		case OBERSCHENKEL:
			return "Oberschenkel";
		case ANDERER_ORT:
			return "Anderer Ort";
		}
		return "";
	}

	private String getRegistrierungEingangString() {
		switch (getRegistrierung().getRegistrierungsEingang()) {
		case ONLINE_REGISTRATION:
			return "Online";
		case CALLCENTER_REGISTRATION:
			return "Callcenter";
		case ORT_DER_IMPFUNG:
			return "Ort der Impfung";
		case MASSENUPLOAD:
			return "Massenupload";
		case NOTFALL_PROZESS:
			return "Notfall prozess";
		case UNBEKANNT:
			return "Unbekannt";
		case DATA_MIGRATION:
			return "Data migration";
		}
		throw new AppFailureException(String.format(
			"Die registrierung %s hat keine RegistrierungsEingang",
			getRegistrierung().getRegistrierungsnummer()));
	}

	@NonNull
	private Registrierung getRegistrierung() {
		return this.impfinformationDto.getRegistrierung();
	}
}
