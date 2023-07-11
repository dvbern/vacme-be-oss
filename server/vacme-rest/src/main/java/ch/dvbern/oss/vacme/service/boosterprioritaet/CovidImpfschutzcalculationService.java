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

package ch.dvbern.oss.vacme.service.boosterprioritaet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.base.ImpfInfo;
import ch.dvbern.oss.vacme.entities.impfen.Erkrankung;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.BoosterAgePunktePrioritaetImpfstoffRule;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.IBoosterPrioritaetRule;
import ch.dvbern.oss.vacme.service.impfschutz.ImpfschutzCalculationService;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CovidImpfschutzcalculationService implements ImpfschutzCalculationService {

	final List<IBoosterPrioritaetRule> rules = new ArrayList<>();

	@NonNull
	private ImpfstoffInfosForRules specifiedImpfstoffe;

	private final VacmeSettingsService vacmeSettingsService;

	/**
	 * Initialisiere die Regeln mit ihren Parametern
	 */
	@PostConstruct
	void initRules() {
		final CovidImpfschutzcalculationConfigDTO config =
			vacmeSettingsService.getCovidImpfschutzcalculationConfigDTO();
		if (MandantUtil.getMandant().equals(Mandant.ZH)) {
			addZhRules(config);
		} else if (MandantUtil.getMandant().equals(Mandant.BE)) {
			addBeRules(config);
		}
		// do init in postConstruct so all injected configs are ready to be used
		orderListByAnzahlMonateBisFreigabe();
	}

	void addBeRules(@NonNull CovidImpfschutzcalculationConfigDTO config) {
		// 1. Booster
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createMinAgeRule(
			specifiedImpfstoffe,
			config.getMinAgeBe(),
			config.getFreigabeOffsetImpfungMonate(), config.getFreigabeOffsetImpfungTage(),
			config.getFreigabeOffsetKrankheitMonate(), config.getFreigabeOffsetKrankheitTage(),
			0, null,
			config.isEnablePfizerOnlyForU30(), false, null));

		addCommonRules(config);
	}

	void addZhRules(@NonNull CovidImpfschutzcalculationConfigDTO config) {
		// Alle ab 16 sind freigegeben
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createMinAgeRule(
			specifiedImpfstoffe,
			config.getMinAgeZh(),
			config.getFreigabeOffsetImpfungMonate(), config.getFreigabeOffsetImpfungTage(),
			config.getFreigabeOffsetKrankheitMonate(), config.getFreigabeOffsetKrankheitTage(),
			0, null,
			config.isEnablePfizerOnlyForU30(), false, config.getCutoffDateSelbstzahler()));

		// Alle ab 12 sind als Selbstzahler freigegeben
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createMinAgeRule(
			specifiedImpfstoffe,
			12,
			config.getFreigabeOffsetImpfungMonate(), config.getFreigabeOffsetImpfungTage(),
			config.getFreigabeOffsetKrankheitMonate(), config.getFreigabeOffsetKrankheitTage(),
			0, null,
			config.isEnablePfizerOnlyForU30(), true, null));

		addCommonRules(config);
	}

	private void addCommonRules(@NonNull CovidImpfschutzcalculationConfigDTO config) {
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createOhneFreigabeRule(
			specifiedImpfstoffe,
			config.isEnablePfizerOnlyForU30())); // rule die alle Regs matched und nur das immunisiertBis berechnet
	}

	private void orderListByAnzahlMonateBisFreigabe() {
		BoosterPrioUtil.orderListByAnzahlMonateBisFreigabe(this.rules);
	}

	/**
	 * Funktion welche fuer die gegeebene Registrierung berechnen soll ob und wie lange ihr Impfschutz noch besteht
	 *
	 * @param fragebogen Am Fragebogen haengt auch die Registrierung
	 * @param impfinformationDto DTO mit Impfinformationen zu Impfung 1/2 und Boosterimpfungen
	 * @return Impfschutz wenn die Person bereits einen Impfschutz hat oder null wenn nicht. Achtung es wird immer ein neues
	 * Object zurueckgegeben auch wenn im impfinformationDto schon eines besteht
	 */
	@Override
	@NonNull
	public Optional<Impfschutz> calculateImpfschutz(
		@NonNull Fragebogen fragebogen,
		@NonNull ImpfinformationDto impfinformationDto
	) {
		if (isGrundimmunisiertGemaessVacMeOrSelbstdeklaration(impfinformationDto.getImpfdossier())) {
			return findImpfschutzMitFruehesterFreigabe(fragebogen, impfinformationDto);
		}
		LOG.debug(
			"Registrierung {} war nicht grundimmunisiert und kann daher keinen Impfschutz haben ",
			fragebogen.getRegistrierung().getRegistrierungsnummer());
		return Optional.empty();
	}

	@NonNull
	private Optional<Impfschutz> findImpfschutzMitFruehesterFreigabe(
		@NonNull Fragebogen fragebogen,
		@NonNull ImpfinformationDto impfinformationDto
	) {
		List<ImpfInfo> orderedImpfInfos = BoosterPrioUtil.getImpfinfosOrderedByTimestampImpfung(impfinformationDto);
		List<Erkrankung> orderedErkrankungen = impfinformationDto.getImpfdossier().getErkrankungenSorted();
		return rules.stream()
			.map(rule -> rule.calculateImpfschutz(
				fragebogen,
				impfinformationDto,
				orderedImpfInfos,
				orderedErkrankungen)
			) // pro Regel den Impfschutz ausrechnen
			.reduce(BoosterPrioUtil::mergeImpfschutzOptionals)
			.orElse(Optional.empty());
	}

	/**
	 * berechnet ob eine Registrierung ueberhaupt schon grundimmunisiert ist und somit fuer eine Boosterimpfung in Frage
	 * kommt
	 */
	boolean isGrundimmunisiertGemaessVacMeOrSelbstdeklaration(@NonNull Impfdossier impfdossier) {
		return impfdossier.getVollstaendigerImpfschutzTyp() != null;
	}
}
