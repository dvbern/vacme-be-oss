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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.dvbern.oss.vacme.entities.embeddables.ZweiteGrundimmunisierungVerzichtet;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.print.base.PdfConstants;
import ch.dvbern.oss.vacme.print.base.PdfGenerator;
import ch.dvbern.oss.vacme.print.base.PdfUtil;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import com.lowagie.text.Document;
import org.apache.commons.collections.CollectionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Generator fuer Impfbestaetigung fuer Covid
 */
public class ImpfdokumentationCovidPdfGenerator extends AbstractImpfdokumentationPdfGenerator {

	private final ImpfinformationDto infos;
	private final List<String> impfungen;
	private final boolean hatNurEineVacmeImpfung;

	public ImpfdokumentationCovidPdfGenerator(
		@NonNull ImpfinformationDto infos,
		@NonNull KantonaleBerechtigung kantonaleBerechtigung
	) {
		super(infos.getRegistrierung(), kantonaleBerechtigung);
		this.infos = infos;
		List<Impfung> tmpImpfungen = new ArrayList<>();
		if (infos.getImpfung1() != null) {
			tmpImpfungen.add(infos.getImpfung1());
		}
		if (infos.getImpfung2() != null) {
			tmpImpfungen.add(infos.getImpfung2());
		}
		if (CollectionUtils.isNotEmpty(infos.getBoosterImpfungen())) {
			tmpImpfungen.addAll(infos.getBoosterImpfungen());
		}

		// hat nur genau eine Vacme Impfung
		hatNurEineVacmeImpfung = infos.getImpfung1() != null && tmpImpfungen.stream().filter(Objects::nonNull).count() == 1;
		this.impfungen = tmpImpfungen
			.stream()
			.filter(Objects::nonNull)
			.sorted(Comparator.comparing(Impfung::getTimestampImpfung))
			.map(this::buildImpfungString).collect(Collectors.toList());
	}

	@NotNull
	@Override
	protected String getDocumentTitle() {
		return translateAllLanguages("print_impfdokumentation_title", " / ");
	}

	@NotNull
	@Override
	protected PdfGenerator.CustomGenerator getCustomGenerator() {
		return (generator) -> {
			Document document = generator.getDocument();
			document.add(PdfUtil.createParagraph(
				registrierung.getVorname() + ' ' +
				registrierung.getName() + ", " +
				PdfConstants.DATE_FORMATTER.format(registrierung.getGeburtsdatum() ) + " - " +
				registrierung.getRegistrierungsnummer(), 2));
			document.add(PdfUtil.createParagraph(translateAllLanguages("print_impfdokumentation_covid_intro", "\n\n") + "\n\n\n", 0));
			document.add(PdfUtil.createListInParagraph(this.impfungen, 2));
			if (hatNurEineVacmeImpfung) { //  d.h. Erstimpfung in Vacme, evtl noch nicht grundimmunisiert
				if (infos.getImpfdossier().getTimestampZuletztAbgeschlossen() != null) {
					if (infos.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().getZweiteImpfungVerzichtetZeit() != null) {
						document.add(PdfUtil.createParagraph(buildZweiteImpfungVerzichtetString(), 2));
					} else {
						if (infos.getImpfdossier().getVollstaendigerImpfschutzTyp() == null) {
							document.add(PdfUtil.createParagraph(buildAutomatischAbgeschlossenString(), 2));
						}
					}
				} else {
					if (infos.getImpfdossier().getVollstaendigerImpfschutzTyp() == null) {
						document.add(PdfUtil.createParagraph(translateAllLanguages("print_impfdokumentation_zweite_impfung_ausstehend", "\n\n"), 2));
					}
				}
			}
			document.add(createVaccinationCertificateFooter("print_impfdokumentation_vaccination_certificate"));
		};
	}

	@NonNull
	private String buildZweiteImpfungVerzichtetString() {
		final ZweiteGrundimmunisierungVerzichtet verzichtetData = infos.getImpfdossier().getZweiteGrundimmunisierungVerzichtet();
		Objects.requireNonNull(verzichtetData.getZweiteImpfungVerzichtetZeit());
		if (infos.getImpfdossier().abgeschlossenMitVollstaendigemImpfschutz()) {
			return translateAllLanguagesWithArgs("print_impfdokumentation_zweite_impfung_verzichtet_vollstaendiger_impfschutz", "\n\n",
				DateUtil.formatDate(verzichtetData.getZweiteImpfungVerzichtetZeit()));
		}
		return translateAllLanguagesWithArgs("print_impfdokumentation_zweite_impfung_verzichtet", "\n\n",
			DateUtil.formatDate(verzichtetData.getZweiteImpfungVerzichtetZeit()),
			verzichtetData.getZweiteImpfungVerzichtetGrund());
	}

	@NonNull
	private String buildAutomatischAbgeschlossenString() {
		Objects.requireNonNull(infos.getImpfdossier().getTimestampZuletztAbgeschlossen());
		return translateAllLanguagesWithArgs("print_impfdokumentation_automatisch_abgeschlossen", "\n\n",
			DateUtil.formatDate(infos.getImpfdossier().getTimestampZuletztAbgeschlossen()));
	}
}
