/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of
 *  the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.visitor;

import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.print.ImpfdokumentationAffenpockenPdfGenerator;
import ch.dvbern.oss.vacme.print.ImpfdokumentationCovidPdfGenerator;
import ch.dvbern.oss.vacme.print.ImpfdokumentationFsmePdfGenerator;
import ch.dvbern.oss.vacme.print.base.VacmePdfGenerator;
import ch.dvbern.oss.vacme.shared.visitor.KrankheitVisitor;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ImpfdokumentationPdfGeneratorVisitor implements KrankheitVisitor<VacmePdfGenerator> {

	private final ImpfinformationDto infos;
	private final KantonaleBerechtigung kantonaleBerechtigung;

	public ImpfdokumentationPdfGeneratorVisitor(
		@NonNull ImpfinformationDto infos,
		@NonNull KantonaleBerechtigung kantonaleBerechtigung
	) {
		this.infos = infos;
		this.kantonaleBerechtigung = kantonaleBerechtigung;
	}

	@NonNull
	public VacmePdfGenerator getImpfdokumentationPdfGenerator() {
		return infos.getKrankheitIdentifier().accept(this);
	}

	@Override
	public VacmePdfGenerator visitCovid() {
		ImpfdokumentationCovidPdfGenerator pdfGenerator = new ImpfdokumentationCovidPdfGenerator(infos, kantonaleBerechtigung);
		return pdfGenerator;
	}

	@Override
	public VacmePdfGenerator visitAffenpocken() {
		ImpfdokumentationAffenpockenPdfGenerator pdfGenerator = new ImpfdokumentationAffenpockenPdfGenerator(
			infos.getRegistrierung(),
			infos.getBoosterImpfungen(),
			kantonaleBerechtigung);
		return pdfGenerator;
	}

	@Override
	public VacmePdfGenerator visitFsme() {
		ImpfdokumentationFsmePdfGenerator pdfGenerator = new ImpfdokumentationFsmePdfGenerator(
			infos.getRegistrierung(),
			infos.getBoosterImpfungen(),
			kantonaleBerechtigung);
		return pdfGenerator;
	}
}
