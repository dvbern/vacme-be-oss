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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.BeruflicheTaetigkeit;
import ch.dvbern.oss.vacme.entities.registration.ChronischeKrankheiten;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Geschlecht;
import ch.dvbern.oss.vacme.entities.registration.Lebensumstaende;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.OdiFilter;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.enums.Kundengruppe;
import ch.dvbern.oss.vacme.jax.registration.OdiFilterJax;
import ch.dvbern.oss.vacme.repo.OdiFilterRepo;
import ch.dvbern.oss.vacme.repo.OrtDerImpfungRepo;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.PrioritaetUtil;
import io.opentelemetry.extension.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class OdiFilterService {

	private final OdiFilterRepo odiFilterRepo;
	private final OrtDerImpfungRepo odiRepo;

	@NonNull
	public List<OrtDerImpfung> filterOdisForRegistrierung(
		@NonNull Fragebogen fragebogen,
		@NonNull ImpfinformationDto infos,
		@Nullable Kundengruppe kundengruppe
	) {
		final ImpfdossierStatus currentStatus = infos.getImpfdossier().getDossierStatus();
		boolean isBoosterStatus = ImpfdossierStatus.getAnyStatusOfGrundimmunisiert().contains(currentStatus);
		Set<UUID> allowedImpfstoffe = new HashSet<>();
		if (isRegistrierungBetweenImpfungen(infos.getImpfdossier())) {
			// Da wir hier noch keinen Impfschutz haben, beachten wir den Impfstoff der ersten Impfung
			final Impfung firstVacmeImpfung = getCovidImpfungErsteImpffolge(infos);
			if (!firstVacmeImpfung.isExtern()) {
				allowedImpfstoffe = Set.of(firstVacmeImpfung.getImpfstoff().getId());
			}
		} else {
			allowedImpfstoffe = getAllowedImpfstoffUUIds(infos.getImpfdossier());
		}

		List <OrtDerImpfung> odis = odiRepo.findOdisAvailableForRegistrierung(
			infos, isBoosterStatus, allowedImpfstoffe, kundengruppe);
		return odis
			.stream()
			.filter(odi -> registrierungPassesAllOdiFilters(odi, fragebogen))
			.collect(Collectors.toList());
	}

	private boolean isRegistrierungBetweenImpfungen(@NonNull Impfdossier impfdossier) {
		return ImpfdossierStatus.isErsteImpfungDoneAndZweitePending().contains(impfdossier.getDossierStatus());
	}

	@NonNull
	private Impfung getCovidImpfungErsteImpffolge(@NonNull ImpfinformationDto infos) {
		final Registrierung registrierung = infos.getRegistrierung();
		String msg = String.format("Registrierung %s: Impfung1 must exist if state is %s for Krankheit %s",
			registrierung.getRegistrierungsnummer(),
			infos.getImpfdossier().getDossierStatus(), infos.getImpfdossier().getKrankheitIdentifier());
		if (infos.getImpfung1() == null) {
			LOG.error(msg);
			throw AppValidationMessage.ILLEGAL_STATE.create(msg);
		}
		return infos.getImpfung1();
	}

	@NonNull
	private Set<UUID> getAllowedImpfstoffUUIds(
		@NonNull Impfdossier dossier
	) {
		Set<UUID> allowedImpfstoffUUIDs = Collections.emptySet();
		final Impfschutz impfschutz = dossier.getImpfschutz();
		if (impfschutz != null) {
			allowedImpfstoffUUIDs = impfschutz.getErlaubteImpfstoffeCollection();
		}
		return allowedImpfstoffUUIDs;
	}

	public boolean registrierungPassesAllOdiFilters(@NonNull OrtDerImpfung odi, @NonNull Fragebogen fragebogen) {
		return odi.getFilters().stream().allMatch(filter -> registrierungPassesFilter(filter, fragebogen));
	}

	@WithSpan
	public boolean isOdiAvailableForOneOfImpfstoff(@NonNull OrtDerImpfung ortDerImpfung, @NonNull Collection<UUID> allowedImpfstoffe) {
		return ortDerImpfung.getImpfstoffs().stream().map(Impfstoff::getId).anyMatch(allowedImpfstoffe::contains);
	}

	public void updateFilters(@NonNull OrtDerImpfung ortDerImpfung, @NonNull List<OdiFilterJax> filters) {
		List<OdiFilter> sentFilterEntities = convertToFilter(filters);
		List<OdiFilter> previousFilters = odiFilterRepo.getAll();
		Set<OdiFilter> notPersisted = new HashSet<>(sentFilterEntities);

		Set<OdiFilter> updateSet = sentFilterEntities.stream()
			.map(filter -> {
				// Replace with saved filter if present
				int index = previousFilters.indexOf(filter);
				if (index != -1) {
					// Remove from filters to persist
					notPersisted.remove(filter);
					return previousFilters.get(index);
				}
				return filter;
			})
			.collect(Collectors.toSet());

		notPersisted.forEach(odiFilterRepo::create);
		ortDerImpfung.setFilters(updateSet);
		odiRepo.update(ortDerImpfung);
	}

	private boolean registrierungPassesFilter(@NonNull OdiFilter filter, @NonNull Fragebogen fragebogen) {
		switch (filter.getTyp()) {
		case AGE_FILTER:
			return passesAgeFilter(filter, fragebogen);
		case GESCHLECHT_FILTER:
			return passesGeschlechtFilter(filter, fragebogen);
		case PRIORITAET_FILTER:
			return passesPrioritaetFilter(filter, fragebogen);
		case PRIORITAET_PUNKT_FILTER:
			return passesPrioritaetPunktFilter(filter, fragebogen);
		case REGISTRIERUNGS_EINGANG:
			return passesRegistrierungsEingang(filter, fragebogen);
		case CHRONISCHE_KRANKHEITEN:
			return passesChronischeKrankheitenFilter(filter, fragebogen);
		case BERUFLICHE_TAETIGKEIT:
			return passesBeruflicheTaetigkeitFilter(filter, fragebogen);
		case LEBENSUMSTAENDE:
			return passesLebensumstaendeFilter(filter, fragebogen);
		}
		throw new AppFailureException("Unknown or unimplemented filter");
	}

	boolean passesAgeFilter(@NonNull OdiFilter filter, @NonNull Fragebogen fragebogen) {
		Registrierung registrierung = fragebogen.getRegistrierung();
		boolean passesMax = true;
		if (filter.getMaximalWert() != null) {
			int maxAge = filter.getMaximalWert().intValue();
			passesMax = LocalDate.now().minusYears(maxAge)
				.isBefore(registrierung.getGeburtsdatum());
		}
		boolean passesMin = true;
		if (filter.getMinimalWert() != null) {
			int minAge = filter.getMinimalWert().intValue();
			passesMin = LocalDate.now().minusYears(minAge)
				.isAfter(registrierung.getGeburtsdatum());
		}

		return passesMax && passesMin;
	}

	private void validateAgeFilter(@NonNull OdiFilter filter) {
		if (filter.getStringArgument() != null) {
			throw AppValidationMessage.ODI_FILTER_AGE_VALIDATION.create();
		}
	}

	boolean passesGeschlechtFilter(@NonNull OdiFilter filter, @NonNull Fragebogen fragebogen) {
		String stringArgument = filter.getStringArgument();
		if (stringArgument == null) {
			return false;
		}
		List<String> parsedArgument = parseString(stringArgument);
		try {
			Set<Geschlecht> geschlecht = parsedArgument.stream()
				.map(Geschlecht::valueOf)
				.collect(Collectors.toSet());
			return geschlecht.contains(fragebogen.getRegistrierung().getGeschlecht());
		} catch (IllegalArgumentException e) {
			LOG.error("Error in the parsing of Geschlecht parameters", e);
			return false;
		}
	}

	private void validateGeschlechtFilter(@NonNull OdiFilter filter) {
		if (filter.getMaximalWert() != null || filter.getMinimalWert() != null) {
			throw AppValidationMessage.ODI_FILTER_GESCHLECHT_VALIDATION.create();
		}
		String argument = filter.getStringArgument();
		if (argument != null) {
			parseString(argument).forEach(geschlechtString -> {
				try {
					Geschlecht.valueOf(geschlechtString);
				} catch (IllegalArgumentException ignored) {
					throw AppValidationMessage.ODI_FILTER_GESCHLECHT_VALIDATION_UNPARSABLE.create();
				}
			});
		}
	}

	boolean passesPrioritaetFilter(@NonNull OdiFilter filter, @NonNull Fragebogen fragebogen) {
		String stringArgument = filter.getStringArgument();
		if (stringArgument == null) {
			return false;
		}
		List<String> parsedArgument = parseString(stringArgument);
		return parsedArgument.stream()
			.anyMatch(argument -> argument.equals(fragebogen.getRegistrierung().getPrioritaet().getCode()));
	}

	private void validatePrioritaetFilter(@NonNull OdiFilter filter) {
		if (filter.getMaximalWert() != null || filter.getMinimalWert() != null) {
			throw AppValidationMessage.ODI_FILTER_PRIORITAET_VALIDATION.create();
		}

		String argument = filter.getStringArgument();
		if (argument != null) {
			parseString(argument).forEach(prioString -> {
				try {
					Prioritaet.valueOf(prioString);
				} catch (IllegalArgumentException ignored) {
					throw AppValidationMessage.ILLEGAL_STATE.create(prioString);
				}
			});
		}
	}

	private boolean passesPrioritaetPunktFilter(@NonNull OdiFilter filter, @NonNull Fragebogen fragebogen) {
		boolean passes = true;
		if (filter.getMaximalWert() != null) {
			int maxPunkt = filter.getMaximalWert().intValue();
			passes = !PrioritaetUtil.hasMorePrioritaetPunktThan(maxPunkt, fragebogen);
		}
		if (filter.getMinimalWert() != null) {
			int minPunkt = filter.getMinimalWert().intValue();
			passes &= PrioritaetUtil.hasMorePrioritaetPunktThan(minPunkt, fragebogen);
		}

		return passes;
	}

	private void validatePrioritaetPunktFilter(@NonNull OdiFilter filter) {
		if (filter.getStringArgument() != null) {
			throw AppValidationMessage.ODI_FILTER_PRIORITAET_PUNKT_VALIDATION.create();
		}
	}

	private boolean passesRegistrierungsEingang(@NonNull OdiFilter filter, @NonNull Fragebogen fragebogen) {
		String stringArgument = filter.getStringArgument();
		if (stringArgument == null) {
			return false;
		}
		List<String> parsedArgument = parseString(stringArgument);
		try {
			Set<RegistrierungsEingang> eingangSet = parsedArgument.stream()
				.map(RegistrierungsEingang::valueOf)
				.collect(Collectors.toSet());
			return eingangSet.contains(fragebogen.getRegistrierung().getRegistrierungsEingang());
		} catch (IllegalArgumentException e) {
			LOG.error("Error in the parsing of RegistrierungsEingang parameters", e);
			return false;
		}
	}

	private boolean passesChronischeKrankheitenFilter(@NonNull OdiFilter filter, @NonNull Fragebogen fragebogen) {
		String stringArgument = filter.getStringArgument();
		if (stringArgument == null) {
			return false;
		}
		List<String> parsedArgument = parseString(stringArgument);
		try {
			Set<ChronischeKrankheiten> zugelassenSet = parsedArgument.stream()
				.map(ChronischeKrankheiten::valueOf)
				.collect(Collectors.toSet());
			return zugelassenSet.contains(fragebogen.getChronischeKrankheiten());
		} catch (IllegalArgumentException e) {
			LOG.error("Error in the parsing of ChronischeKrankheiten parameters", e);
			return false;
		}
	}

	private boolean passesBeruflicheTaetigkeitFilter(@NonNull OdiFilter filter, @NonNull Fragebogen fragebogen) {
		String stringArgument = filter.getStringArgument();
		if (stringArgument == null) {
			return false;
		}
		List<String> parsedArgument = parseString(stringArgument);
		try {
			Set<BeruflicheTaetigkeit> zugelassenSet = parsedArgument.stream()
				.map(BeruflicheTaetigkeit::valueOf)
				.collect(Collectors.toSet());
			return zugelassenSet.contains(fragebogen.getBeruflicheTaetigkeit());
		} catch (IllegalArgumentException e) {
			LOG.error("Error in the parsing of BeruflicheTaetigkeit parameters", e);
			return false;
		}
	}

	private boolean passesLebensumstaendeFilter(@NonNull OdiFilter filter, @NonNull Fragebogen fragebogen) {
		String stringArgument = filter.getStringArgument();
		if (stringArgument == null) {
			return false;
		}
		List<String> parsedArgument = parseString(stringArgument);
		try {
			Set<Lebensumstaende> zugelassenSet = parsedArgument.stream()
				.map(Lebensumstaende::valueOf)
				.collect(Collectors.toSet());
			return zugelassenSet.contains(fragebogen.getLebensumstaende());
		} catch (IllegalArgumentException e) {
			LOG.error("Error in the parsing of Lebensumstaende parameters", e);
			return false;
		}
	}

	private void validateRegistrierungsEingang(@NonNull OdiFilter filter) {
		if (filter.getMaximalWert() != null || filter.getMinimalWert() != null) {
			throw AppValidationMessage.ODI_FILTER_REGISTRIERUNGSEINGANG_VALIDATION.create();
		}

		String argument = filter.getStringArgument();
		final List<RegistrierungsEingang> validValues = Arrays.asList(RegistrierungsEingang.values());
		validateEnumValues(argument, validValues, AppValidationMessage.ODI_FILTER_REGISTRIERUNGSEINGANG_VALIDATION_UNPARSABLE);
	}

	private void validateChronischeKrankheitenFilter(@NonNull OdiFilter filter) {
		if (filter.getMaximalWert() != null || filter.getMinimalWert() != null) {
			throw AppValidationMessage.ODI_FILTER_CHRONISCHE_KRANKHEITEN_VALIDATION.create();
		}
		String argument = filter.getStringArgument();
		final List<ChronischeKrankheiten> validValues = Arrays.stream(ChronischeKrankheiten.values())
			.filter(value -> value != ChronischeKrankheiten.UNBEKANNT)
			.collect(Collectors.toList());
		validateEnumValues(argument, validValues, AppValidationMessage.ODI_FILTER_CHRONISCHE_KRANKHEITEN_VALIDATION_UNPARSABLE);
	}

	private void validateBeruflicheTaetigkeitFilter(@NonNull OdiFilter filter) {
		if (filter.getMaximalWert() != null || filter.getMinimalWert() != null) {
			throw AppValidationMessage.ODI_FILTER_BERUFLICHE_TAETIGKEIT_VALIDATION.create();
		}
		String argument = filter.getStringArgument();
		final List<BeruflicheTaetigkeit> validValues = Arrays.stream(BeruflicheTaetigkeit.values())
			.filter(value -> value != BeruflicheTaetigkeit.UNBEKANNT)
			.collect(Collectors.toList());
		validateEnumValues(argument, validValues, AppValidationMessage.ODI_FILTER_BERUFLICHE_TAETIGKEIT_VALIDATION_UNPARSABLE);
	}

	private void validateLebensumstaendeFilter(@NonNull OdiFilter filter) {
		if (filter.getMaximalWert() != null || filter.getMinimalWert() != null) {
			throw AppValidationMessage.ODI_FILTER_LEBENSUMSTAENDE_VALIDATION.create();
		}

		String argument = filter.getStringArgument();
		final List<Lebensumstaende> validValues = Arrays.stream(Lebensumstaende.values())
			.filter(value -> value != Lebensumstaende.UNBEKANNT)
			.collect(Collectors.toList());
		validateEnumValues(argument, validValues, AppValidationMessage.ODI_FILTER_LEBENSUMSTAENDE_VALIDATION_UNPARSABLE);
	}

	private void validateEnumValues(@Nullable String argument, @NonNull List<? extends Enum> values, @NonNull AppValidationMessage errorMsg) {
		// Leerer Filter ist nicht erlaubt
		if (argument == null) {
			throw errorMsg.create();
		}
		parseString(argument).forEach(filterarg -> {
			boolean valid =
				values.stream().anyMatch(anEnum -> anEnum.name().equals(filterarg));
			if (!valid) {
				throw errorMsg.create();
			}
		});
	}

	@NonNull
	private List<OdiFilter> convertToFilter(@NonNull List<OdiFilterJax> filters) {
		return filters.stream()
			.map(filter -> {
				OdiFilter entity = filter.toEntity();
				validateFilter(entity);
				return entity;
			})
			.collect(Collectors.toList());
	}

	private void validateFilter(@NonNull OdiFilter filter) {
		switch (filter.getTyp()) {
		case AGE_FILTER:
			validateAgeFilter(filter);
			break;
		case GESCHLECHT_FILTER:
			validateGeschlechtFilter(filter);
			break;
		case PRIORITAET_FILTER:
			validatePrioritaetFilter(filter);
			break;
		case PRIORITAET_PUNKT_FILTER:
			validatePrioritaetPunktFilter(filter);
			break;
		case REGISTRIERUNGS_EINGANG:
			validateRegistrierungsEingang(filter);
			break;
		case CHRONISCHE_KRANKHEITEN:
			validateChronischeKrankheitenFilter(filter);
			break;
		case BERUFLICHE_TAETIGKEIT:
			validateBeruflicheTaetigkeitFilter(filter);
			break;
		case LEBENSUMSTAENDE:
			validateLebensumstaendeFilter(filter);
			break;
		default:
			throw new AppFailureException("Unknown or unimplemented filter");
		}
	}

	private List<String> parseString(@NonNull String argument) {
		return Stream.of(argument.toUpperCase().split(";"))
			.map(String::strip).collect(Collectors.toList());
	}

	public void removeOrphaned() {
		this.odiFilterRepo.removeOrphaned();
	}
}
