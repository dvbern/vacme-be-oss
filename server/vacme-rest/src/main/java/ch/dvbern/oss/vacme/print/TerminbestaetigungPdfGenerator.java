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
import java.util.Objects;

import ch.dvbern.oss.vacme.entities.embeddables.Adresse;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import ch.dvbern.oss.vacme.print.base.PdfConstants;
import ch.dvbern.oss.vacme.print.base.PdfGenerator;
import ch.dvbern.oss.vacme.print.base.PdfUtil;
import ch.dvbern.oss.vacme.print.base.VacmePdfGenerator;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * Generator fuer Terminbestaegigungen
 */
@Slf4j
public class TerminbestaetigungPdfGenerator extends VacmePdfGenerator {

	private static final int ABSTAND = 1;
	private final Impfdossier impfdossier;

	@Nullable
	private final Impftermin boosterTerminOrNull;

	public TerminbestaetigungPdfGenerator(
		@NotNull Impfdossier impfdossier,
		@Nullable Impftermin boosterTerminOrNull,
		@NonNull KantonaleBerechtigung kantonaleBerechtigung
	) {
		super(impfdossier.getRegistrierung(), kantonaleBerechtigung);
		this.impfdossier = impfdossier;
		this.boosterTerminOrNull = boosterTerminOrNull;
	}

	@NotNull
	@Override
	protected String getDocumentTitle() {
		return translateAllConfiguredLanguages("print_terminbestaetigung_title", " / ");
	}

	@NotNull
	@Override
	protected PdfGenerator.CustomGenerator getCustomGenerator() {
		return (generator) -> {
			Document document = generator.getDocument();

			addBarcode(document, impfdossier.getKrankheitIdentifier());

			PdfPTable table = new PdfPTable(2);
			table.setHeaderRows(0);
			table.setHorizontalAlignment(Element.ALIGN_LEFT);
			table.setTotalWidth(400);
			table.setWidths(new float[] { 0.5f, 0.5f });
			table.setLockedWidth(true);
			PdfUtil.setTableDefaultStyles(table);
			table.getDefaultCell().setBorder(0);

			OrtDerImpfung ortDerImpfung = null;

			// Der ODI kann auch null sein, wenn ein nicht-verwalteter ODI gewaehlt wurde
			if (!impfdossier.getBuchung().isNichtVerwalteterOdiSelected()) {

				// ODI: Von Termin 1, falls vorhanden, sonst gewuenschter ODI
				ortDerImpfung = getOrtDerImpfung();
				Objects.requireNonNull(ortDerImpfung, "Ort der Impfung muss gesetzt sein");
				final Impftermin termin1 = impfdossier.getBuchung().getImpftermin1();
				final Impftermin termin2 = impfdossier.getBuchung().getImpftermin2();

				if (ortDerImpfung.isTerminverwaltung()) {
					if (ImpfdossierStatus.getAnyStatusOfGrundimmunisiert().contains(impfdossier.getDossierStatus())) {
						if (boosterTerminOrNull != null) {
							table.addCell(createLeftCell(new Phrase(
								translateAllConfiguredLanguages(
									"print_terminbestaetigung_terminN",
									" / "),
								PdfConstants.DEFAULT_FONT_BOLD)));
							table.addCell(createLeftCell(new Phrase(
								boosterTerminOrNull.getTerminZeitfensterStartDateAndTimeString()
									+ "\n ",
								PdfConstants.DEFAULT_FONT)));
						} else {
							LOG.error("Trying to generate Booster-Terminbestaetigung, but no Termin was found! Registrierung {}",
								registrierung.getRegistrierungsnummer());
						}
					} else {
						boolean hasTermin1 = termin1 != null;
						boolean hasTermin2 = termin2 != null;
						if (hasTermin1) {
							// Es werden immer nur offene (nicht vergangene) Termine dargestellt
							if (!ImpfdossierStatus.isErsteImpfungDoneAndZweitePending()
								.contains(impfdossier.getDossierStatus())) {
								table.addCell(createLeftCell(new Phrase(
									translateAllConfiguredLanguages("print_terminbestaetigung_termin1", " / "),
									PdfConstants.DEFAULT_FONT_BOLD)));
								table.addCell(createLeftCell(new Phrase(termin1.getTerminZeitfensterStartDateAndTimeString()
									+ "\n ", PdfConstants.DEFAULT_FONT)));
							} else {
								LOG.debug(
									"Generated Terminbestaetigung without pending Termin 1 fuer Reg {}",
									registrierung.getRegistrierungsnummer());
							}
						} else {
							LOG.error(
								"Trying to generate Terminbestaetigung, but no Termin 1 was found! Registrierung {}",
								registrierung.getRegistrierungsnummer());
						}
						if (hasTermin2) {
							table.addCell(createLeftCell(new Phrase(
								translateAllConfiguredLanguages(
									"print_terminbestaetigung_termin2",
									" / "),
								PdfConstants.DEFAULT_FONT_BOLD)));
							table.addCell(createLeftCell(new Phrase(termin2.getTerminZeitfensterStartDateAndTimeString()
								+ "\n ", PdfConstants.DEFAULT_FONT)));
						}
						// Wenn der Impfiwllige ohne Termin ins ODI erhaelt er nur einen Termin 1, d.h. Termin 2 kann hier null sein
					}

				}
				// Ort der Impfung: Adresse
				final Adresse adresse = ortDerImpfung.getAdresse();
				table.addCell(createLeftCell(new Phrase(ortDerImpfung.getName(), PdfConstants.DEFAULT_FONT_BOLD)));
				if (StringUtils.isNotEmpty(ortDerImpfung.getKommentar())) {
					table.addCell(createLeftCell(new Phrase(
						translateAllConfiguredLanguages(
							"print_terminbestaetigung_kommentar_odi",
							" / "),
						PdfConstants.DEFAULT_FONT_BOLD)));
				} else {
					table.addCell(createEmptyCell());
				}
				table.addCell(createLeftCell(new Phrase(getAdresseString(adresse), PdfConstants.DEFAULT_FONT)));
				if (StringUtils.isNotEmpty(ortDerImpfung.getKommentar())) {
					table.addCell(createLeftCell(new Phrase(ortDerImpfung.getKommentar(), PdfConstants.DEFAULT_FONT)));
				} else {
					table.addCell(createEmptyCell());
				}
				document.add(table);

				document.add(PdfUtil.createParagraph(""));
			}

			EmptyLinesForLocale emptyLinesAfter = (Locale locale) -> locale.equals(Locale.GERMAN) ?
				ABSTAND :
				(locale.equals(Locale.FRENCH) ? getEmptyLinesAfterFrenchText() : 0);

			if (ortDerImpfung != null && ortDerImpfung.isTerminverwaltung()) {
				doForAllLanguagesSeparated(locale -> {
					String translationKeyPostfix = impfdossier.getKrankheitIdentifier().toString().toLowerCase();
					document.add(PdfUtil.createParagraph(
						translate("print_terminbestaetigung_anweisung_mit_termine_"
							+ translationKeyPostfix, locale),
						emptyLinesAfter.get(locale),
						PdfConstants.FONT_RED));
				}, document);
			} else if (ortDerImpfung != null && ortDerImpfung.isMobilerOrtDerImpfung()) {
				doForAllLanguagesSeparated(locale -> {
					document.add(PdfUtil.createParagraph(
						translate(
							"print_terminbestaetigung_anweisung_mobiler_ort_der_impfung",
							locale),
						ABSTAND,
						PdfConstants.FONT_RED));
				}, document);
			} else {
				doForAllLanguagesSeparated(locale -> {
					document.add(PdfUtil.createParagraph(
						translate(
							"print_terminbestaetigung_anweisung_ohne_termine",
							locale),
						ABSTAND,
						PdfConstants.FONT_RED));
				}, document);
			}
		};
	}

	private interface EmptyLinesForLocale {
		int get(Locale locale);
	}

	private int getEmptyLinesAfterFrenchText() {
		return Mandant.ZH == MandantUtil.getMandant() ? 1 : 0;
	}

	private String getAdresseString(final Adresse adresse) {
		String adresseString = "";
		if (StringUtils.isNotEmpty(adresse.getAdresse1())) {
			adresseString = adresseString + adresse.getAdresse1() + '\n';
		}
		if (StringUtils.isNotEmpty(adresse.getAdresse2())) {
			adresseString = adresseString + adresse.getAdresse2() + '\n';
		}
		if (StringUtils.isNotEmpty(adresse.getPlz())) {
			adresseString = adresseString + adresse.getPlz() + ' ';
		}
		if (StringUtils.isNotEmpty(adresse.getOrt())) {
			adresseString = adresseString + adresse.getOrt();
		}
		return adresseString;
	}

	@NonNull
	private OrtDerImpfung getOrtDerImpfung() {
		final boolean isAlreadyGrundimmunisiert =
			ImpfdossierStatus.getAnyStatusOfGrundimmunisiert().contains(impfdossier.getDossierStatus());
		final boolean isErsteImpfungDurch =
			ImpfdossierStatus.isErsteImpfungDoneAndZweitePending().contains(impfdossier.getDossierStatus());
		OrtDerImpfung relevantOdi = null;
		if (isAlreadyGrundimmunisiert) {
			if (boosterTerminOrNull != null) {
				relevantOdi = boosterTerminOrNull.getImpfslot().getOrtDerImpfung();
			} else {
				relevantOdi = impfdossier.getBuchung().getGewuenschterOdi();
			}
		} else if (isErsteImpfungDurch) {
			final Impftermin impfterminZweiteImpffolge = impfdossier.getBuchung().getImpftermin2();
			if (impfterminZweiteImpffolge != null) {
				relevantOdi = impfterminZweiteImpffolge.getImpfslot().getOrtDerImpfung();
			} else {
				relevantOdi = impfdossier.getBuchung().getGewuenschterOdi();
			}
		} else {
			final Impftermin impfterminErsteImpffolge = impfdossier.getBuchung().getImpftermin1();
			if (impfterminErsteImpffolge != null) {
				relevantOdi = impfterminErsteImpffolge.getImpfslot().getOrtDerImpfung();
			} else {
				relevantOdi = impfdossier.getBuchung().getGewuenschterOdi();
			}
		}
		if (relevantOdi == null) {
			throw AppValidationMessage.TERMINBESTAETIGUNG_KEIN_OFFENER_TERMIN.create();
		}
		return relevantOdi;
	}
}
