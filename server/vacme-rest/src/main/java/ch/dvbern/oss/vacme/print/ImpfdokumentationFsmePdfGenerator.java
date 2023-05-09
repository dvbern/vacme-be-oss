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

import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.print.base.PdfConstants;
import ch.dvbern.oss.vacme.print.base.PdfGenerator;
import ch.dvbern.oss.vacme.print.base.PdfUtil;
import com.lowagie.text.Document;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generator fuer Impfbestaetigung fuer FSME
 */
public class ImpfdokumentationFsmePdfGenerator extends AbstractImpfdokumentationPdfGenerator {

	private final List<String> impfungen;

	public ImpfdokumentationFsmePdfGenerator(
		@NotNull Registrierung registrierung,
		@Nullable List<Impfung> boosterImpfungen,
		@NonNull KantonaleBerechtigung kantonaleBerechtigung
	) {
		super(registrierung, kantonaleBerechtigung);
		if (boosterImpfungen == null) {
			boosterImpfungen = new ArrayList<>();
		}
		this.impfungen = boosterImpfungen
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
			document.add(PdfUtil.createParagraph(translateAllLanguages("print_impfdokumentation_fsme_intro", "\n\n") + "\n\n\n", 0));
			document.add(PdfUtil.createListInParagraph(this.impfungen, 2));
		};
	}
}
