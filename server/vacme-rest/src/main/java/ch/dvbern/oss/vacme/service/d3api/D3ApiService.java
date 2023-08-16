/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of
 *  the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.service.d3api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFile;
import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFileTyp;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.print.archivierung.ArchivierungPdfGenerator;
import ch.dvbern.oss.vacme.repo.FragebogenRepo;
import ch.dvbern.oss.vacme.repo.ImpfdossierFileRepo;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.repo.ZertifikatRepo;
import ch.dvbern.oss.vacme.service.MandantPropertyService;
import ch.dvbern.oss.vacme.service.ZertifikatService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import com.itextpdf.io.source.ByteArrayOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.vacme.entities.base.MandantPropertyKey.ARCHIVIERUNG_D3_DISABLED;

@ApplicationScoped
@Slf4j
@Transactional(TxType.NOT_SUPPORTED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class D3ApiService {

	private final ImpfungRepo impfungRepo;
	private final FragebogenRepo fragebogenRepo;
	private final ImpfdossierFileRepo impfdossierFileRepo;
	private final ZertifikatRepo zertifikatRepo;
	private final ZertifikatService zertifikatService;
	private final ImpfinformationenService impfinformationenService;
	private final MandantPropertyService mandantPropertyService;

	@Transactional(TxType.REQUIRES_NEW)
	public boolean saveInD3(@NonNull ID<Impfung> impfungId) {
		final Optional<Impfung> impfungOptional = impfungRepo.getById(impfungId);
		if (impfungOptional.isPresent()) {
			Impfung impfung = impfungOptional.get();
			Mandant mandant = impfung.getMandant();
			if (mandantPropertyService.getByKey(ARCHIVIERUNG_D3_DISABLED, mandant).getValueAsBoolean()) {
				LOG.warn(
					"VACME-ARCHIVIERUNG: Archivierung aufgerufen fuer Impfung {} aber ARCHIVIERUNG_D3_DISABLED "
						+ "ist true fuer Mandant {}",
					impfungId,
					mandant);
				return false;
			}
			final KrankheitIdentifier krankheitIdentifier =
				impfung.getTermin().getImpfslot().getKrankheitIdentifier();
			if (krankheitIdentifier != KrankheitIdentifier.COVID) {
				throw new IllegalStateException("Aktuell koennen nur COVID Impfungen archiviert werden");
			}
			final Registrierung registrierung = impfungRepo.getRegistrierungForImpfung(impfungId).orElseThrow();
			try {
				final ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
					registrierung.getRegistrierungsnummer(),
					krankheitIdentifier);
				byte[] pdfContent = generatePdf(infos);

				// TODO VACME-2648: schicken, evtl mit bremse damit wir nicht ueberlasten

				impfung.setArchiviertAm(LocalDateTime.now());
				impfungRepo.update(impfung);

				return true;
			} catch (Exception e) {
				LOG.error(
					"VACME-ARCHIVIERUNG: Archivierung Fehlgeschlagen fuer Impfung {}, Registrierung {}",
					impfungId,
					registrierung.getRegistrierungsnummer(),
					e);
				return false;
			}
		}
		return false;
	}

	@Transactional(TxType.REQUIRES_NEW)
	public void deleteInD3(@NonNull Impfung impfung) {
		// TODO VACME-2692: Loeschung melden wenn impfung im archiv sein koennte (e.g. kein FSME)
	}

	private byte[] generatePdf(@NonNull ImpfinformationDto infos) {
		try {
			final Fragebogen fragebogen = fragebogenRepo.getByRegistrierung(infos.getRegistrierung()).orElseThrow();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			List<ImpfdossierFile> uploadedFiles = impfdossierFileRepo.getImpfdossierFiles(
				infos.getImpfdossier(),
				ImpfdossierFileTyp.IMPFFREIGABE_DURCH_HAUSARZT);
			final List<Zertifikat> zertifikate =
				zertifikatRepo.getAllNonRevokedZertifikate(infos.getImpfdossier());
			ArchivierungPdfGenerator generator = new ArchivierungPdfGenerator(
				fragebogen,
				infos,
				zertifikatService.mapToZertifikatInfo(zertifikate)
			);

			generator.createArchivierung(outputStream, uploadedFiles);
			outputStream.close();
			return outputStream.toByteArray();
		} catch (Exception e) {
			throw new AppFailureException(
				"VACME-ARCHIVIERUNG: Bei der Generierung des Dokuments ist ein Fehler aufgetreten",
				e);
		}
	}
}
