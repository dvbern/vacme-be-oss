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

package ch.dvbern.oss.vacme.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFile;
import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFileTyp;
import ch.dvbern.oss.vacme.entities.embeddables.FileBlob;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.ImpfungArchive;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.jax.ArchivierungDataRow;
import ch.dvbern.oss.vacme.print.archivierung.ArchivierungPdfGenerator;
import ch.dvbern.oss.vacme.repo.FragebogenRepo;
import ch.dvbern.oss.vacme.repo.ImpfdossierFileRepo;
import ch.dvbern.oss.vacme.repo.ImpfungArchiveRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.repo.ZertifikatRepo;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.CleanFileName;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.shared.util.MimeType;
import com.itextpdf.io.source.ByteArrayOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PdfArchivierungService {

	private final FragebogenRepo fragebogenRepo;
	private final ImpfungArchiveRepo archiveRepo;
	private final RegistrierungRepo registrierungRepo;
	private final ImpfdossierFileRepo impfdossierFileRepo;
	private final ImpfinformationenService impfinformationenService;
	private final ZertifikatRepo zertifikatRepo;
	private final ZertifikatService zertifikatService;


	/**
	 * Erstellt das Archivierungspdf fuer die Regs bei denen es noch nicht besteht und speichert es in der DB
	 * @return Liste von Frageboegen (und damit Regs) bei denen es nicht geklappt hat
	 */
	@NonNull
	public List<Pair<Fragebogen, Exception>> createPdfArchivesAndGetFailures(int archivierungZeitDays) {
		LocalDateTime stichtag = LocalDateTime.now().minusDays(archivierungZeitDays);
		List<ArchivierungDataRow> dataRows = fragebogenRepo.getAbgeschlossenNotArchiviertDataOlderThan(stichtag);
		List<Pair<Fragebogen, Exception>> failedArchive = new ArrayList<>();
		for (ArchivierungDataRow dataRow : dataRows) {
			try {
				archive(dataRow);
			} catch (Exception e) {
				LOG.error("VACME-ARCHIVIERUNG: Archivierung Fehlgeschlagen fuer {}", dataRow.getFragebogen().getRegistrierung().getRegistrierungsnummer(), e);
				Pair<Fragebogen, Exception> errorPair = Pair.of(dataRow.getFragebogen(), e);
				failedArchive.add(errorPair);
			}
		}
		return failedArchive;
	}

	@Transactional(TxType.REQUIRES_NEW) // damit transaction requires new verwendet wird muss die Methode public sein
	public void archive(@NonNull ArchivierungDataRow dataRow) {
		try {
			// TODO Affenpocken: Archivierung
			final Optional<ImpfinformationDto> infoOptional =
				impfinformationenService.getImpfinformationenOptional(
					dataRow.getFragebogen().getRegistrierung().getRegistrierungsnummer(),
					KrankheitIdentifier.COVID);
			if (infoOptional.isEmpty()) {
				return;
			}
			ImpfinformationDto infos = infoOptional.get();
			dataRow.setImpfinformationDto(infos);
			if (!impfinformationenService.hasVacmeImpfungen(infos)) {
				// Wenn keine Impfungen in VacMe erfolgt sind, machen wir keine Archivierung.
				// Damit diese Registrierung nicht jedesmal wieder in den Batchjob laeuft, setzen
				// wir den TimestampArchiviert auf etwas "wiedererkennbares"
				final Registrierung registrierung = infos.getRegistrierung();
				registrierung.setTimestampArchiviert(Constants.DATE_ARCHIVIERT_OHNE_VACME_IMPFUNG);
				registrierungRepo.update(registrierung);
				return;
			}

			this.deleteImpfungArchive(dataRow.getFragebogen().getRegistrierung());
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			List<ImpfdossierFile> uploadedFiles = impfdossierFileRepo.getImpfdossierFiles(
				dataRow.getImpfinformationDto().getImpfdossier(),
				ImpfdossierFileTyp.IMPFFREIGABE_DURCH_HAUSARZT);
			final List<Zertifikat> zertifikate =
				zertifikatRepo.getAllNonRevokedZertifikate(infos.getRegistrierung());
			ArchivierungPdfGenerator generator = new ArchivierungPdfGenerator(
				dataRow.getFragebogen(),
				dataRow.getImpfinformationDto(),
				zertifikatService.mapToZertifikatInfo(zertifikate)
			);

			generator.createArchivierung(outputStream, uploadedFiles);

			outputStream.close();
			ImpfungArchive archive = new ImpfungArchive();
			archive.setRegistrierung(dataRow.getFragebogen().getRegistrierung());
			FileBlob fileBlob = FileBlob.of(
				new CleanFileName(generator.getArchiveTitle()),
				MimeType.APPLICATION_PDF,
				outputStream.toByteArray()
			);
			archive.setFileBlob(fileBlob);
			archiveRepo.create(archive);
			Registrierung registrierung = dataRow.getFragebogen().getRegistrierung();
			registrierung.setTimestampArchiviert(LocalDateTime.now());
			registrierungRepo.update(registrierung);
		} catch (Exception e) {
			throw new AppFailureException("Bei der Generierung des Dokuments ist ein Fehler aufgetreten", e);
		}
	}

	@Transactional(TxType.SUPPORTS)
	public void deleteImpfungArchive(@NonNull Registrierung registrierung){
		Registrierung reg = registrierungRepo.getById(Registrierung.toId(registrierung.getId()))
			.orElseThrow(() -> AppFailureException.entityNotFound(Registrierung.class, registrierung.getId().toString()));
		Optional<ImpfungArchive> impfungArchiveOptional = archiveRepo.getByRegistrierung(reg);
		impfungArchiveOptional.ifPresent(impfungArchive -> archiveRepo.deleteById(impfungArchive.toId()));
		reg.setTimestampArchiviert(null);
	}

	public void archiveManually(@NonNull Registrierung registrierung) {
		// Die bestehende Archivierung loeschen
		deleteImpfungArchive(registrierung);
		// Die benoetigten Daten lesen und neu archivieren
		final Fragebogen fragebogen = fragebogenRepo.getByRegistrierung(registrierung)
			.orElseThrow(() -> AppValidationMessage.UNKNOWN_REGISTRIERUNGSNUMMER.create(registrierung.getRegistrierungsnummer()));
		// TODO Affenpocken: Archivierung
		final ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
			registrierung.getRegistrierungsnummer(),
			KrankheitIdentifier.COVID);
		ArchivierungDataRow dataRow = new ArchivierungDataRow(fragebogen, infos);
		archive(dataRow);
	}
}
