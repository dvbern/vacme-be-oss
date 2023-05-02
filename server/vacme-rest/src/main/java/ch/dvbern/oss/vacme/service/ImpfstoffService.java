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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.ImpfempfehlungChGrundimmunisierung;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Krankheit;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.ZulassungsStatus;
import ch.dvbern.oss.vacme.jax.registration.ImpfstoffJax;
import ch.dvbern.oss.vacme.jax.registration.KrankheitJax;
import ch.dvbern.oss.vacme.repo.ImpfempfehlungChGrundimmunisierungRepo;
import ch.dvbern.oss.vacme.repo.ImpfstoffRepo;
import ch.dvbern.oss.vacme.repo.KrankheitRepo;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class ImpfstoffService {

	private final ImpfstoffRepo impfstoffRepo;
	private final ImpfempfehlungChGrundimmunisierungRepo empfehlungRepo;
	private final KrankheitRepo krankheitRepo;

	static final int MAX_DOSEN_CH = 2;
	static final int MAX_DOSEN_NON_WHO = 4;

	@Inject
	public ImpfstoffService(
		@NonNull ImpfstoffRepo impfstoffRepo,
		@NonNull ImpfempfehlungChGrundimmunisierungRepo impfempfehlungChGrundimmunisierung,
		@NonNull KrankheitRepo krankheitRepo
	) {
		this.impfstoffRepo = impfstoffRepo;
		this.empfehlungRepo = impfempfehlungChGrundimmunisierung;
		this.krankheitRepo = krankheitRepo;
	}

	@NonNull
	public Impfstoff findById(@NonNull ID<Impfstoff> impfstoffId) {
		return impfstoffRepo.getById(impfstoffId)
			.orElseThrow(() -> AppFailureException.entityNotFound(Impfstoff.class, impfstoffId));
	}

	@NonNull
	public List<Impfstoff> findAll() {
		return impfstoffRepo.findAll()
			.stream()
			.sorted((impfstoffA, impfstoffB) -> {
				// zuerst zugelassene, dann extern zugelassene, dann nicht zugelassene
				if (impfstoffA.getZulassungsStatus() != impfstoffB.getZulassungsStatus()) {
					return ((Integer) impfstoffA.getZulassungsStatus()
						.ordinal()).compareTo(impfstoffB.getZulassungsStatus().ordinal());
				}

				// innerhalb der gleichen Zulassung: alphabetisch nach Hersteller
				return impfstoffA.getDisplayName().compareTo(impfstoffB.getDisplayName());
			}).collect(Collectors.toList());
	}

	@NonNull
	public List<Impfstoff> findAllImpfstoffeWithKrankheitenUnsorted() {
		return impfstoffRepo.findAllImpfstoffeWithKrankheiten();
	}

	@NonNull
	public Impfstoff findByCovidCertProdCode(@NonNull String covidCertProdCode) {
		return impfstoffRepo.findAll()
			.stream()
			.filter(impfstoff -> covidCertProdCode.equals(impfstoff.getCovidCertProdCode()))
			.findFirst().orElseThrow();
	}

	@NonNull
	public List<ImpfstoffJax> getImpfstoffeForKrankheitAndStatus(@NonNull Set<ZulassungsStatus> stati) {
		return this.findAll()
			.stream()
			.filter(impfstoff -> stati.contains(impfstoff.getZulassungsStatus()))
			.map(ImpfstoffJax::from)
			.collect(Collectors.toList());
	}

	@NonNull
	public List<ImpfstoffJax> getImpfstoffeForKrankheitAndStatus(
		@NonNull Set<ZulassungsStatus> stati,
		@NonNull KrankheitIdentifier requestedKrankheit
	) {

		// todo Affenpocken: lesen mit Query und wenn moeglich grad krankheiten per fetch-join mitlesen
		return this.findAll()
			.stream()
			.filter(impfstoff -> {
				boolean statusOk =  stati.contains(impfstoff.getZulassungsStatus());
				Collection<KrankheitIdentifier> krankheitIdentifiers = impfstoff.getKrankheiten()
					.stream()
					.map(Krankheit::getIdentifier)
					.collect(Collectors.toSet());
				boolean krankheitOk =  krankheitIdentifiers.contains(requestedKrankheit);
				return krankheitOk && statusOk;

			})
			.map(ImpfstoffJax::from)
			.collect(Collectors.toList());
	}

	@NonNull
	public ImpfstoffJax updateImpfstoff(@NonNull ImpfstoffJax impfstoffJax) {
		Impfstoff dbImpfstoff = findById(Impfstoff.toId(impfstoffJax.getId()));

		impfstoffJax.applyTo(dbImpfstoff);

		List<ImpfempfehlungChGrundimmunisierung> empfehlungen = computeEmpfehlungen(impfstoffJax, dbImpfstoff);
		updateEmpfehlungen(dbImpfstoff, empfehlungen);

		Set<Krankheit> krankheit = computeKrankheiten(impfstoffJax.getKrankheiten());
		dbImpfstoff.getKrankheiten().clear();
		dbImpfstoff.getKrankheiten().addAll(krankheit);

		validate(dbImpfstoff);

		impfstoffRepo.update(dbImpfstoff);
		return ImpfstoffJax.from(dbImpfstoff);
	}

	@NonNull
	private Set<Krankheit> computeKrankheiten(@NonNull List<KrankheitJax> krankheiten) {
		return krankheiten
			.stream()
			.map(KrankheitJax::getIdentifier)
			.map(krankheitIdentifier -> this.krankheitRepo.getByIdentifier(krankheitIdentifier)
				.orElseThrow(() -> AppFailureException.entityNotFound(Krankheit.class, krankheitIdentifier)))
			.collect(Collectors.toSet());
	}

	public static void validate(@NonNull Impfstoff impfstoff) {
		validateDosenBenoetigt(impfstoff);
		validateImpfempfehlungen(impfstoff);
	}

	public static void validateDosenBenoetigt(@NonNull Impfstoff impfstoff) {
		var dosen = impfstoff.getAnzahlDosenBenoetigt();
		// minimum 1 Dosis
		if (dosen < 1) {
			throw AppValidationMessage.ILLEGAL_STATE.create("invalid anzahl dosen benoetigt: " + dosen);
		}
		if (dosen > getAnzahlDosenForValidierung(impfstoff.getZulassungsStatus())) {
			throw AppValidationMessage.ILLEGAL_STATE.create("invalid anzahl dosen benoetigt: " + dosen);
		}
	}

	private static int getAnzahlDosenForValidierung(@NonNull ZulassungsStatus zulassungsStatus) {
		// maximum 4 Dosen, und bei CH zugelassenen maximal 2
		return (zulassungsStatus == ZulassungsStatus.ZUGELASSEN || zulassungsStatus == ZulassungsStatus.EMPFOHLEN)
			? MAX_DOSEN_CH
			: MAX_DOSEN_NON_WHO;
	}

	public static void validateImpfempfehlungen(@NonNull Impfstoff impfstoff) {
		if (impfstoff.getImpfempfehlungenChGrundimmunisierung().size() > (getAnzahlDosenForValidierung(impfstoff.getZulassungsStatus()) + 1)) {
			throw AppValidationMessage.ILLEGAL_STATE.create("too many impfempfehlungen: " + impfstoff.getImpfempfehlungenChGrundimmunisierung().size());
		}
		for (ImpfempfehlungChGrundimmunisierung impfempfehlung : impfstoff.getImpfempfehlungenChGrundimmunisierung()) {
			validateImpfempfehlung(impfstoff.getZulassungsStatus(), impfempfehlung);
		}
	}

	public static void validateImpfempfehlung(
		@NonNull ZulassungsStatus zulassungsStatus,
		@NonNull ImpfempfehlungChGrundimmunisierung impfempfehlung
	) {
		var notwendig = impfempfehlung.getNotwendigFuerChGrundimmunisierung();
		// kein Wert unter 0
		if (notwendig < 0) {
			throw AppValidationMessage.ILLEGAL_STATE.create("invalid notwendigFuerChGrundimmunisierung: " + notwendig);
		}
		// maximum 2 notwendig, denn mehr als 2 brauchen wir nie fuer die Grundimmunisierung
		if (notwendig > MAX_DOSEN_CH) {
			throw AppValidationMessage.ILLEGAL_STATE.create("invalid notwendigFuerChGrundimmunisierung: " + notwendig);
		}

		var verabreicht = impfempfehlung.getAnzahlVerabreicht();
		// kein verabreicht-Wert unter 1
		if (verabreicht < 1) {
			throw AppValidationMessage.ILLEGAL_STATE.create("invalid anzahlVerabreicht: " + verabreicht);
		}
		// kein verabreicht-Wert groesser als 4
		if (verabreicht > getAnzahlDosenForValidierung(zulassungsStatus)) {
			throw AppValidationMessage.ILLEGAL_STATE.create("invalid anzahlVerabreicht: " + verabreicht);
		}
	}

	private void updateEmpfehlungen(@NonNull Impfstoff dbImpfstoff, @NonNull List<ImpfempfehlungChGrundimmunisierung> empfehlungen) {
		dbImpfstoff.getImpfempfehlungenChGrundimmunisierung().clear();
		dbImpfstoff.getImpfempfehlungenChGrundimmunisierung().addAll(empfehlungen);

		for (ImpfempfehlungChGrundimmunisierung empfehlung : empfehlungen) {
			empfehlung.setImpfstoff(dbImpfstoff);
		}
	}

	@NonNull
	private List<ImpfempfehlungChGrundimmunisierung> computeEmpfehlungen(
		@NonNull ImpfstoffJax impfstoffJax,
		@NonNull Impfstoff impfstoff) {
		return impfstoffJax.getImpfempfehlungen().stream().map(empfehlungJax -> {
			ImpfempfehlungChGrundimmunisierung empfehlung = empfehlungRepo.getOrCreateById(impfstoff, empfehlungJax.getId());
			return empfehlungJax.applyTo(empfehlung);
		}).collect(Collectors.toList());
	}
}
