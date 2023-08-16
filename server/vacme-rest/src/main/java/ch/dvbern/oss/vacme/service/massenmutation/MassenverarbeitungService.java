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

package ch.dvbern.oss.vacme.service.massenmutation;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.massenverarbeitung.MassenverarbeitungQueue;
import ch.dvbern.oss.vacme.entities.massenverarbeitung.MassenverarbeitungQueueTyp;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.jax.korrektur.ImpfungOdiKorrekturJax;
import ch.dvbern.oss.vacme.jax.registration.LatLngJax;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLUploadBaseJax;
import ch.dvbern.oss.vacme.repo.ImpfdossierRepo;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.repo.MassenverarbeitungQueueRepo;
import ch.dvbern.oss.vacme.repo.OrtDerImpfungRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.service.FragebogenService;
import ch.dvbern.oss.vacme.service.GeocodeService;
import ch.dvbern.oss.vacme.service.ImpfdossierService;
import ch.dvbern.oss.vacme.service.KorrekturService;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.service.vmdl.VMDLServiceAbstract;
import ch.dvbern.oss.vacme.service.vmdl.VMDLServiceFactory;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ImpfinformationenUtil;
import com.google.common.collect.Iterators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MassenverarbeitungService {

	private final MassenverarbeitungQueueRepo massenverarbeitungQueueRepo;
	private final VMDLServiceFactory vmdlServiceFactory;
	private final ImpfungRepo impfungRepo;
	private final KorrekturService korrekturService;
	private final OrtDerImpfungRepo ortDerImpfungRepo;
	private final RegistrierungRepo registrierungRepo;
	private final RegistrierungService registrierungService;
	private final FragebogenService fragebogenService;
	private final GeocodeService geocodeService;
	private final ImpfdossierService impfdossierService;
	private final ImpfdossierRepo impfdossierRepo;

	@NonNull
	public List<UUID> parseAsImpfungenUUIDs(@NonNull String csvWithImpfids) {
		List<UUID> impfungenIds = new ArrayList<>();
		try (StringReader reader = new StringReader(csvWithImpfids);
			final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)
		) {
			for (final CSVRecord record : parser) {
				final String impfungId = record.get(0);
				impfungenIds.add(parseAsUUID(impfungId));
				if (Iterators.size(record.iterator()) != 1) {
					throw AppValidationMessage.ILLEGAL_STATE.create("Should only have one column in record "
						+ impfungId);
				}
			}
		} catch (IOException e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not parse CSV Inpfut");
		} catch (ArrayIndexOutOfBoundsException e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Eingabe hat nicht dir richtige Anzahl Colums");
		}
		return impfungenIds;
	}

	@NonNull
	private UUID parseAsUUID(@NonNull String impfungId) {
		try {
			return UUID.fromString(impfungId);
		} catch (Exception e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Der String '"
				+ impfungId
				+ "' ist keine gueltige UUID "
				+ e.getMessage());
		}
	}

	@NonNull
	public List<Pair<UUID, UUID>> parseAsImpfungenAndOdiUUIDs(@NonNull String csvWithImpfids) {
		List<Pair<UUID, UUID>> impfungenToOdiIds = new ArrayList<>();
		try (StringReader reader = new StringReader(csvWithImpfids);
			final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)
		) {
			for (final CSVRecord record : parser) {
				final String impfungId = record.get(0).strip();
				final String odiId = record.get(1).strip();

				UUID impfungUUID = parseAsUUID(impfungId);
				UUID odiUUID = parseAsUUID(odiId);
				impfungenToOdiIds.add(Pair.of(impfungUUID, odiUUID));
			}
		} catch (IOException e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not parse CSV Input");
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Eingabe hat nicht dir richtige Anzahl Colums");
		}

		return impfungenToOdiIds;
	}

	public void addOdiToLatLngQueue(@NonNull List<ID<OrtDerImpfung>> odiIds) {
		massenverarbeitungQueueRepo.addOdiToLatLngQueue(odiIds);
	}

	public void addImpfungenToExternalizeQueue(@NonNull List<ID<Impfung>> impfungenIds) {
		List<ID<Impfung>> result = new ArrayList<>(impfungenIds);
		massenverarbeitungQueueRepo.addImpfungenToExternalizeQueue(result);
	}

	public void addImpfungenToLoeschenQueue(@NonNull List<ID<Impfung>> impfungenIds) {
		List<ID<Impfung>> result = new ArrayList<>(impfungenIds);
		massenverarbeitungQueueRepo.addImpfungenToLoeschenQueue(result);
	}

	public void addImpfungenToMoveToOdiQueue(@NonNull List<Pair<ID<Impfung>, ID<OrtDerImpfung>>> listWithIds) {
		massenverarbeitungQueueRepo.addImpfungenToMoveToOdiQueue(listWithIds);
	}

	public void addRegistrierungenToDeleteQueue(@NonNull List<String> registrierungsnummern) {
		massenverarbeitungQueueRepo.addRegistrierungenToDeleteQueue(registrierungsnummern);
	}

	public void addImpfgruppeToFreigebenQueue(List<UUID> impfdossiers) {
		massenverarbeitungQueueRepo.addImpfgruppeToFreigebenQueue(impfdossiers);
	}

	public List<MassenverarbeitungQueue> findMassenverarbeitungQueueItemsToProcess(
		long massenverarbeitungQueueProcessingJobBatchSize,
		@NonNull MassenverarbeitungQueueTyp typ
	) {
		return massenverarbeitungQueueRepo.findMassenverarbeitungQueueItemsToProcess(
			massenverarbeitungQueueProcessingJobBatchSize,
			typ);
	}

	@Transactional(TxType.REQUIRES_NEW)
	public boolean externalizeImpfungAndRemoveFromVmdl(@NonNull MassenverarbeitungQueue queueItem) {
		String impfungId = queueItem.getImpfungId();
		try {
			Validate.isTrue(queueItem.getTyp() == MassenverarbeitungQueueTyp.IMPFUNG_EXTERALIZE);
			if (!queueItem.notYetProcessed()) {
				LOG.warn(
					"VACME-MASSENVERARBEITUNG: queueItem {} is is in wrong status {}. Maybe it was already processed?",
					queueItem.getId(),
					queueItem.getStatus());
				return true;
			}

			Objects.requireNonNull(impfungId);
			Optional<Impfung> impfungOpt = impfungRepo.getById(Impfung.toId(UUID.fromString(impfungId)));
			if (impfungOpt.isEmpty()) {
				String s = String.format("Impfung %s not found for  externalization", impfungId);
				logFailureForQueueItemAndMarkAsFailed(queueItem, s);
				return false;
			} else {
				Impfung impfung = impfungOpt.get();
				VMDLServiceAbstract<? extends VMDLUploadBaseJax> vmdlService = getVmdlService(impfung);
				// Aus VMDL entfernen, falls sie ueberhaupt schon geschickt wurde
				if (vmdlService.wasSentToVMDL(impfung)) {
					vmdlService.deleteImpfung(impfung);
				}
				sleepForAShortTime();
				if (impfung.isExtern()) {
					LOG.warn("VACME-MASSENVERARBEITUNG: Impfung {} was already set to extern", impfungId);
				}
				impfung.setExtern(true);
				queueItem.markSuccessful();
				LOG.debug("VACME-MASSENVERARBEITUNG: successfully externalized Impfung with id {}", impfungId);
			}
			return true;

		} catch (Exception exception) {
			LOG.error(
				"VACME-MASSENVERARBEITUNG: Error during externalization of Impfung  for {} ",
				queueItem.getImpfungId(),
				exception);
			queueItem.markFailed(exception.getMessage());
			return false;
		} finally {
			massenverarbeitungQueueRepo.updateQueueItemNewTransaction(queueItem);
		}
	}

	@NonNull
	private VMDLServiceAbstract<? extends VMDLUploadBaseJax> getVmdlService(@NonNull Impfung impfung) {
		return vmdlServiceFactory.createVMDLService(impfung.getTermin().getImpfslot().getKrankheitIdentifier());
	}

	private void sleepForAShortTime() {
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			LOG.warn("Sleep was interrupted");
		}
	}

	@Transactional(TxType.REQUIRES_NEW)
	public boolean loeschenImpfungInVacmeAndVmdlAndRevokeZertifikat(@NonNull MassenverarbeitungQueue queueItem) {
		String impfungId = queueItem.getImpfungId();
		try {
			Validate.isTrue(queueItem.getTyp() == MassenverarbeitungQueueTyp.IMPFUNG_LOESCHEN);
			if (!queueItem.notYetProcessed()) {
				LOG.warn(
					"VACME-MASSENVERARBEITUNG: queueItem {} is in wrong status {}. Maybe it was already processed?",
					queueItem.getId(),
					queueItem.getStatus());
				return true;
			}
			Objects.requireNonNull(impfungId);
			Optional<Impfung> impfungOpt = impfungRepo.getById(Impfung.toId(UUID.fromString(impfungId)));

			if (impfungOpt.isEmpty()) {
				String s = String.format("VACME-MASSENVERARBEITUNG: Impfung %s not found for removal", impfungId);
				logFailureForQueueItemAndMarkAsFailed(queueItem, s);
				return false;
			}
			Impfung impfung = impfungOpt.get();
			final KrankheitIdentifier krankheitIdentifier = impfung.getTermin().getImpfslot().getKrankheitIdentifier();
			final ImpfinformationDto infos =
				impfungRepo.getImpfinformationenOptional(impfung.toId(), krankheitIdentifier).orElse(null);
			if (infos == null) {
				String s =
					String.format("VACME-MASSENVERARBEITUNG: Impfinformationen %s not found for removal", impfungId);
				logFailureForQueueItemAndMarkAsFailed(queueItem, s);
				return false;
			}
			// Die Impfung wurde gefunden und kann geloescht werden
			final Impffolge impffolge = impfung.getTermin().getImpffolge();
			Integer impffolgeNr = null; // Fuer ERSTE und ZWEITE Impfung nicht benoetigt
			if (Impffolge.BOOSTER_IMPFUNG == impffolge) {
				final Impfdossiereintrag dossiereintragForImpfung =
					ImpfinformationenUtil.getDossiereintragForImpfung(infos, impfung);
				Objects.requireNonNull(dossiereintragForImpfung);
				impffolgeNr = dossiereintragForImpfung.getImpffolgeNr();
			}
			// Verwenden des KorrekturService stellt sicher, dass die Impfung in Vacme und
			// VMDL gelöscht wird, die Impfdokumentation gelöscht, die Zertifikate revoziert
			// und der Status wieder korrekt gesetzt wird.
			korrekturService.impfungLoeschen(infos, impffolge, impffolgeNr);

			sleepForAShortTime();
			queueItem.markSuccessful();
			LOG.info("VACME-MASSENVERARBEITUNG: successfully removed Impfung with id={} and revoked certificate. triggering user={}", impfungId, queueItem.getUserErstellt());
			return true;

		} catch (Exception exception) {
			LOG.error(
				"VACME-MASSENVERARBEITUNG: Error during removal of Impfung for {} ",
				queueItem.getImpfungId(),
				exception);
			queueItem.markFailed(exception.getMessage());
			return false;
		} finally {
			massenverarbeitungQueueRepo.updateQueueItemNewTransaction(queueItem);
		}
	}

	@Transactional(TxType.REQUIRES_NEW)
	public boolean moveImpfungToOdi(@NonNull MassenverarbeitungQueue queueItem) {
		String impfungId = queueItem.getImpfungId();
		try {
			Validate.isTrue(queueItem.getTyp() == MassenverarbeitungQueueTyp.IMPFUNG_ODI_MOVE);
			Validate.notNull(
				queueItem.getOdiId(),
				"Odi ID war nicht gesetzt fuer Queue Item des Typs "
					+ MassenverarbeitungQueueTyp.IMPFUNG_ODI_MOVE.name());
			if (!queueItem.notYetProcessed()) {
				LOG.warn(
					"VACME-MASSENVERARBEITUNG: queueItem {} is is in wrong status {}. Maybe it was already processed?",
					queueItem.getId(),
					queueItem.getStatus());
				return true;
			}

			Objects.requireNonNull(impfungId);
			Optional<Impfung> impfungOpt = impfungRepo.getById(Impfung.toId(UUID.fromString(impfungId)));

			if (impfungOpt.isEmpty()) {
				String s = String.format("Impfung %s not found for move to odi", impfungId);
				logFailureForQueueItemAndMarkAsFailed(queueItem, s);
				return false;
			}
			Impfung impfung = impfungOpt.get();
			final KrankheitIdentifier krankheitIdentifier = impfung.getTermin().getImpfslot().getKrankheitIdentifier();
			Optional<ImpfinformationDto> impfinformationenOptional =
				impfungRepo.getImpfinformationenOptional(Impfung.toId(UUID.fromString(impfungId)),
						krankheitIdentifier);
			if (impfinformationenOptional.isEmpty()) {
				String s = String.format("Registrierung for Impfung %s not found for  move to odi", impfungId);
				logFailureForQueueItemAndMarkAsFailed(queueItem, s);
				return false;
			}
			ImpfinformationDto impfinformationDto = impfinformationenOptional.get();
			Optional<OrtDerImpfung> odiToMoveToOpt =
				ortDerImpfungRepo.getById(OrtDerImpfung.toId(UUID.fromString(queueItem.getOdiId())));
			if (odiToMoveToOpt.isEmpty()) {
				String s =
					String.format(
						"Ort der Impfung with id %s does not exist. Can not move impfung %s for Reg %s",
						queueItem.getOdiId(),
						queueItem.getImpfungId(),
						impfinformationDto.getRegistrierung().getRegistrierungsnummer());
				logFailureForQueueItemAndMarkAsFailed(queueItem, s);
				return false;
			}
			OrtDerImpfung ortDerImpfung = odiToMoveToOpt.get();
			ImpfungOdiKorrekturJax korrekturJax =
				mapKorrekturJaxFromMassenverarbeitungQueue(impfung, impfinformationDto, ortDerImpfung);

			korrekturService.impfungOdiKorrigieren(korrekturJax, impfinformationDto, impfung);

			queueItem.markSuccessful();
			LOG.info("VACME-MASSENVERARBEITUNG: Successfully moved Impfung with id {} to OdI {} for Reg {}",
				impfungId, ortDerImpfung.getName(), impfinformationDto.getRegistrierung().getRegistrierungsnummer());
			return true;

		} catch (Exception exception) {
			LOG.error("VACME-MASSENVERARBEITUNG: Error during Odi move of Impfung {} to Odi {}",
				queueItem.getImpfungId(), queueItem.getOdiId(), exception);
			queueItem.markFailed(exception.getMessage());
			return false;
		} finally {
			massenverarbeitungQueueRepo.updateQueueItemNewTransaction(queueItem);
		}
	}

	@NonNull
	private ImpfungOdiKorrekturJax mapKorrekturJaxFromMassenverarbeitungQueue(
		@NonNull Impfung impfung,
		@NonNull ImpfinformationDto impfinformationDto,
		@NonNull OrtDerImpfung ortDerImpfung
	) {
		Impffolge impffolge = impfung.getTermin().getImpffolge();
		Integer impffolgeNr = ImpfinformationenService.getImpffolgeNr(impfinformationDto, impfung);
		return new ImpfungOdiKorrekturJax(
			impffolge,
			ortDerImpfung.getId(),
			impffolgeNr,
			impfinformationDto.getKrankheitIdentifier());
	}

	private void logFailureForQueueItemAndMarkAsFailed(
		@NonNull MassenverarbeitungQueue queueItem,
		@NonNull String message) {
		LOG.warn("VACME-MASSENVERARBEITUNG: {}", message);
		queueItem.setLastError(message);
		queueItem.markFailedNoRetry(message);
	}

	@NonNull
	public List<String> parseAsRegistrierungsnummern(@NonNull String csvWithRegistrierungsnummern) {
		List<String> registrierungsnummern = new ArrayList<>();
		try (StringReader reader = new StringReader(csvWithRegistrierungsnummern);
			final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)
		) {
			for (final CSVRecord record : parser) {
				final String regNummer = record.get(0);
				if (!StringUtils.isAlphanumeric(regNummer)) {
					throw AppValidationMessage.ILLEGAL_STATE.create("Could not parse non-alphanumeric Input "
						+ regNummer);
				}
				if (Iterators.size(record.iterator()) != 1) {
					throw AppValidationMessage.ILLEGAL_STATE.create("Should only have one column in record "
						+ regNummer);
				}
				registrierungsnummern.add(regNummer);

			}
		} catch (IOException e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not parse CSV Input");
		} catch (ArrayIndexOutOfBoundsException e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Eingabe hat nicht die richtige Anzahl Colums");
		}
		return registrierungsnummern;
	}

	@Transactional(TxType.REQUIRES_NEW)
	public boolean deleteRegistrierung(MassenverarbeitungQueue queueItem) {
		String registrierungNummer = queueItem.getRegistrierungNummer();
		try {
			Validate.isTrue(queueItem.getTyp() == MassenverarbeitungQueueTyp.REGISTRIERUNG_DELETE);
			if (!queueItem.notYetProcessed()) {
				LOG.warn(
					"VACME-MASSENVERARBEITUNG: queueItem {} is in wrong status {}. Maybe it was already processed?",
					queueItem.getId(),
					queueItem.getStatus());
				return true;
			}
			Objects.requireNonNull(registrierungNummer);

			final Optional<Registrierung> registrierungOpt =
				registrierungRepo.getByRegistrierungnummer(registrierungNummer);
			if (registrierungOpt.isEmpty()) {
				LOG.info(String.format("Registrierung %s not found to delete", registrierungNummer));
				queueItem.markSuccessful();
				return false;
			} else {
				final Registrierung registrierung = registrierungService.findRegistrierung(registrierungNummer);
				final Fragebogen fragebogen =
					fragebogenService.findFragebogenByRegistrierungsnummer(registrierungNummer);
				registrierungService.deleteRegistrierung(registrierung, fragebogen);
				queueItem.markSuccessful();
				LOG.debug(
					"VACME-MASSENVERARBEITUNG: successfully removed registration with regnummer {}",
					registrierungNummer);
				return true;
			}

		} catch (Exception exception) {
			LOG.error(
				"VACME-MASSENVERARBEITUNG: Error during deletion of Registration  for {} ",
				queueItem.getRegistrierungNummer(),
				exception);
			queueItem.markFailed(exception.getMessage());
			return false;
		} finally {
			massenverarbeitungQueueRepo.updateQueueItemNewTransaction(queueItem);
		}
	}

	@Transactional(TxType.REQUIRES_NEW)
	public boolean calculateOdiLatLng(MassenverarbeitungQueue queueItem) {
		try {
			Validate.isTrue(queueItem.getTyp() == MassenverarbeitungQueueTyp.ODI_LAT_LNG_CALCULATE);
			if (!queueItem.notYetProcessed()) {
				LOG.warn(
					"VACME-MASSENVERARBEITUNG: queueItem {} is in wrong status {}. Maybe it was already processed?",
					queueItem.getId(),
					queueItem.getStatus());
				return true;
			}

			Objects.requireNonNull(queueItem.getOdiId());
			Optional<OrtDerImpfung> odiOptional =
				ortDerImpfungRepo.getById(OrtDerImpfung.toId(UUID.fromString(queueItem.getOdiId())));
			if (odiOptional.isEmpty()) {
				String s = String.format(
					"Ort der Impfung with id %s does not exist. Can not move calculate LatLng",
					queueItem.getOdiId());
				logFailureForQueueItemAndMarkAsFailed(queueItem, s);
				return false;
			}

			OrtDerImpfung odi = odiOptional.get();
			LatLngJax latLng = geocodeService.geocodeAdresse(odi.getAdresse());
			odi.setLat(latLng.getLat());
			odi.setLng(latLng.getLng());

			ortDerImpfungRepo.update(odi);
			queueItem.markSuccessful();

			return true;
		} catch (Exception exception) {
			LOG.error(
				"VACME-MASSENVERARBEITUNG: Error during calculation of LatLng for Odi {} ",
				queueItem.getOdiId(),
				exception);
			queueItem.markFailed(exception.getMessage());
			return false;
		} finally {
			massenverarbeitungQueueRepo.updateQueueItemNewTransaction(queueItem);
		}
	}

	@Transactional(TxType.REQUIRES_NEW)
	public boolean impfgruppeFreigeben(@NonNull MassenverarbeitungQueue queueItem) {
		UUID impfdossierID = queueItem.getImpfdossierId();
		try {
			Validate.isTrue(queueItem.getTyp() == MassenverarbeitungQueueTyp.IMPFGRUPPE_FREIGEBEN);
			if (!queueItem.notYetProcessed()) {
				LOG.warn(
					"VACME-MASSENVERARBEITUNG: queueItem {} is in wrong status {}. Maybe it was already processed?",
					queueItem.getId(),
					queueItem.getStatus());
				return true;
			}
			Objects.requireNonNull(impfdossierID);

			Impfdossier impfdossier =
				impfdossierRepo.getImpfdossier(Impfdossier.toId(impfdossierID));
			impfdossierService.setStatusAccordingToPrioritaetFreischaltung(impfdossier);

			queueItem.markSuccessful();
			LOG.debug(
				"VACME-MASSENVERARBEITUNG: successfully unlocked the vaccination group for vaccination dossier with UUID {} ",
				impfdossierID);
			return true;
		} catch (Exception exception) {
			LOG.error(
				"VACME-MASSENVERARBEITUNG: Error during priority unlocking with UUID {} ",
				impfdossierID, exception);
			queueItem.markFailed(exception.getClass().getSimpleName() + ": " + exception.getMessage());
			return false;
		} finally {
			massenverarbeitungQueueRepo.updateQueueItemNewTransaction(queueItem);
		}
	}

}
