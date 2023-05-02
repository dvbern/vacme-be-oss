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

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.jax.PersonalienSucheJax;
import ch.dvbern.oss.vacme.repo.ImpfdossierRepo;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Dieser Service ermoeglicht es Personen nach Name, Vorname, Geburtsdatum zu suchen
 */
@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PersonalienSucheService {

	private final RegistrierungRepo registrierungRepo;
	private final ImpfdossierRepo impfdossierRepo;
	private final ZertifikatService zertifikatService;
	private final UserPrincipal userPrincipal;
	private final ImpfterminRepo impfterminRepo;

	/**
	 * Diese Suche ist sehr grosszuegig. Wichtig ist hier, dass das Callcenter keine Reg-Codes erhaelt, sondern nur die Reg-UUID (fuer Adressaenderungen)
	 */
	@NonNull
	public Set<Registrierung> suchen(@NonNull String vorname, @NonNull String name, @NonNull Date geburtsdatum) {
		LocalDate geburtsdatumLocalDate = LocalDate.ofInstant(geburtsdatum.toInstant(), ZoneId.systemDefault());
		List<PersonalienSucheJax> sucheJaxes = registrierungRepo.findRegistrierungByGeburtsdatumGeimpft(geburtsdatumLocalDate);
		List<UUID> matchingDossierIds = filterMatchingJaxes(vorname, name, sucheJaxes);
		return matchingDossierIds.stream()
			.map(id -> impfdossierRepo.getImpfdossier(Impfdossier.toId(id)).getRegistrierung())
			.collect(Collectors.toSet());
	}

	/**
	 * Suche nuer fuer bestimmte ODIs
	 */
	@NonNull
	public Set<Registrierung> suchenFuerODI(@NonNull String vorname, @NonNull String name, @NonNull Date geburtsdatum, @NonNull Benutzer benutzer) {
		LocalDate geburtsdatumLocalDate = LocalDate.ofInstant(geburtsdatum.toInstant(), ZoneId.systemDefault());
		List<PersonalienSucheJax> sucheJaxes = registrierungRepo.findRegistrierungByGeburtsdatum(geburtsdatumLocalDate);
		List<UUID> matchingIds = filterMatchingJaxes(vorname, name, sucheJaxes);
		return matchingIds.stream()
			.map(id -> impfdossierRepo.getImpfdossier(Impfdossier.toId(id)))
			.filter(impfdossier -> registrierungHasTerminInODI(impfdossier, benutzer.getOrtDerImpfung()))
			.map(Impfdossier::getRegistrierung)
			.collect(Collectors.toSet());
	}

	private boolean registrierungHasTerminInODI(@NonNull Impfdossier impfdossier, @NonNull Set<OrtDerImpfung> odis) {
		if (!userPrincipal.isCallerInAnyOfRole(BenutzerRolle.getOrtDerImpfungRoles())) {
			// Nicht ODI-Rollen duerfen immer alle Regs sehen
			return true;
		}
		// Termin 1?
		if (impfdossier.getBuchung().getImpftermin1() != null &&
			odis.contains(impfdossier.getBuchung().getImpftermin1().getImpfslot().getOrtDerImpfung())) {
			return true;
		}
		// Termin 2?
		if (impfdossier.getBuchung().getImpftermin2() != null &&
			odis.contains(impfdossier.getBuchung().getImpftermin2().getImpfslot().getOrtDerImpfung())) {
			return true;
		}
		// Gewuenschter ODI?
		if (odis.contains(impfdossier.getBuchung().getGewuenschterOdi())) {
			return true;
		}
		// Boostertermin?
		return impfterminRepo.hasAnyBoosterTerminInOdis(impfdossier, odis);
	}

	/**
	 * Diese Suche ist sehr eingeschraenkt (weil UVCI), dafuer geben wir dem Callcenter den Reg-Code zurueck!
	 */
	@NonNull
	public List<Registrierung> suchen(@NonNull String vorname, @NonNull String name, @NonNull Date geburtsdatum, @NonNull String uvci) {
		LocalDate geburtsdatumLocalDate = LocalDate.ofInstant(geburtsdatum.toInstant(), ZoneId.systemDefault());
		List<PersonalienSucheJax> sucheJaxes = registrierungRepo.findRegistrierungByGeburtsdatumGeimpft(geburtsdatumLocalDate);
		List<Registrierung> matchingRegistrierungen = filterMatchingJaxes(vorname, name, uvci, sucheJaxes);
		return matchingRegistrierungen;

	}

	/**
	 * goes through the passed list and filters it for matching vorname and name
	 *
	 * @param vorname to match
	 * @param name to match
	 * @param dataToSearchIn list of DTOs to find matches in
	 * @return DTOs where name and vorname matched
	 */
	@NonNull
	List<UUID> filterMatchingJaxes(@NonNull String vorname, @NonNull String name, @NonNull List<PersonalienSucheJax> dataToSearchIn) {
		String normalizedVorname = normalizeSplitString(vorname);
		String normalizedName = normalizeSplitString(name);
		return dataToSearchIn.stream()
			.filter(dataJax -> matchesNormalized(normalizedVorname, dataJax.getVorname()) &&
				matchesNormalized(normalizedName, dataJax.getName()))
			.map(PersonalienSucheJax::getImpfdossierId)
			.collect(Collectors.toList());
	}

	List<Registrierung> filterMatchingJaxes(@NonNull String vorname, @NonNull String name, @NonNull String uvci, @NonNull List<PersonalienSucheJax> dataToSearchIn) {
		String normalizedVorname = normalizeSplitString(vorname);
		String normalizedName = normalizeSplitString(name);
		return dataToSearchIn.stream()
			.filter(dataJax -> matchesNormalized(normalizedVorname, dataJax.getVorname()) &&
				matchesNormalized(normalizedName, dataJax.getName()))
			.map(PersonalienSucheJax::getImpfdossierId)
			.map(impfdossierId -> {
				Impfdossier impfdossier = impfdossierRepo.getImpfdossier(Impfdossier.toId(impfdossierId));
				return impfdossier.getRegistrierung();

			}).collect(Collectors.toSet())
			.stream()
			.filter(reg -> hasUvci(reg, uvci))
			.collect(Collectors.toList());
	}

	private boolean hasUvci(@NonNull Registrierung registrierung, @NonNull String uvci) {

		List<Zertifikat> zertifikatList = zertifikatService.getAllZertifikateRegardlessOfRevocation(registrierung);
		return zertifikatList.stream()
			.anyMatch(zertifikat -> zertifikat.getUvci().toLowerCase(Locale.GERMAN).endsWith(uvci.toLowerCase(Locale.GERMAN)));
	}

	/**
	 * normalizes  the input
	 *
	 * @param normalizedSearch the normalized search-string
	 * @param comparedString data to find match in
	 */
	private boolean matchesNormalized(String normalizedSearch, String comparedString) {
		String normalizedData = normalizeString(comparedString);
		String[] splitData = normalizedData.split("[ \\-]");
		String concatenated = "";

		for (String singleDataString : splitData) {
			concatenated = concatenated.concat(singleDataString);
			if (singleDataString.equals(normalizedSearch) || concatenated.equals(normalizedSearch)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Normalizes and split the string returning a list of to which we can compare other
	 * normalized strings for matches
	 *
	 * @param toNormalizeSplit the string to be worked
	 * @return a list of normalized string to compare with .equals
	 */
	private String normalizeSplitString(String toNormalizeSplit) {
		String normalizedString = normalizeString(toNormalizeSplit); // remove special chars
		return normalizedString.replaceAll("[ \\-]", "");
	}

	/**
	 * remove special characters
	 *
	 * @param toNormalize string to normalize
	 * @return normalized string
	 */
	private String normalizeString(String toNormalize) {
		return Normalizer.normalize(toNormalize, Normalizer.Form.NFD)
			.replaceAll("[^\\p{ASCII}]", "") // remove non ascii symbols
			.toLowerCase();
	}
}
