/*
 *
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

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.base.ImpfInfo;
import ch.dvbern.oss.vacme.entities.impfen.Erkrankung;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.AffenpockenImpfschutzRule;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.IBoosterPrioritaetRule;
import ch.dvbern.oss.vacme.service.impfschutz.ImpfschutzCalculationService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * This Service can be used to calculate the Impfschutz for Affenpocken
 */
@Slf4j
@ApplicationScoped
public class AffenpockenImpfschutzcalculationService implements ImpfschutzCalculationService {

	final List<IBoosterPrioritaetRule> rules = new ArrayList<>();

	@ConfigProperty(name = "boosterrule.affenpocken.minage", defaultValue = "18")
	public int minAge = 18;

	@ConfigProperty(name = "boosterrule.affenpocken.freigabeoffset.withimpfschutz.amount", defaultValue = "2")
	public int freigabeOffsetImpfungWithImpfschutz = 2;

	@ConfigProperty(name = "boosterrule.affenpocken.freigabeoffset.withimpfschutz.unit", defaultValue = "YEARS")
	public ChronoUnit freigabeoffsetLetzteImpfungWithImpfschutzUnit = ChronoUnit.YEARS;

	@ConfigProperty(name = "boosterrule.affenpocken.freigabeoffset.noimpfschutz.amount", defaultValue = "4")
	public int freigabeOffsetImpfungWithoutImpfschutz = 4;

	@ConfigProperty(name = "boosterrule.affenpocken.freigabeoffset.noimpfschutz.unit", defaultValue = "WEEKS")
	public ChronoUnit freigabeoffsetLetzteImpfungWithoutImpfschutzUnit = ChronoUnit.WEEKS;

	@NonNull
	private ImpfstoffInfosForRules specifiedImpfstoffe;

	@Inject
	public AffenpockenImpfschutzcalculationService(@NonNull ImpfstoffInfosForRules specifiedImpfstoffe) {
		this.specifiedImpfstoffe = specifiedImpfstoffe;
	}

	/**
	 * Initialisiere die Regeln mit ihren Parametern
	 */
	@PostConstruct
	void initRules() {
		if (MandantUtil.getMandant().equals(Mandant.BE)) {
			addCommonRules();
		} else {
			throw AppValidationMessage.ILLEGAL_STATE.create("Mandant " + MandantUtil.getMandant() + " does not support Affenpocken");
		}
		// do init in postConstruct so all injected configs are ready to be used
		orderListByAnzahlMonateBisFreigabe();
	}

	private void addCommonRules() {
		// rule that handels all cases,
		rules.add(AffenpockenImpfschutzRule.createMinAgeRule(
			specifiedImpfstoffe,
			minAge,
			freigabeOffsetImpfungWithoutImpfschutz, freigabeoffsetLetzteImpfungWithoutImpfschutzUnit,
			freigabeOffsetImpfungWithImpfschutz, freigabeoffsetLetzteImpfungWithImpfschutzUnit,
			0, null
		));

		// rule die alle Regs matched und nur das immunisiertBis berechnet
		rules.add(AffenpockenImpfschutzRule.createOhneFreigabeRule(specifiedImpfstoffe));
	}

	private void orderListByAnzahlMonateBisFreigabe() {
		BoosterPrioUtil.orderListByAnzahlMonateBisFreigabe(this.rules);
	}

	/**
	 * Funktion welche fuer die gegebene Registrierung berechnen soll ob und wie lange ihr Impfschutz noch besteht
	 *
	 * @param fragebogen Am Fragebogen haengt auch die Registrierung
	 * @param impfinformationDto DTO mit Impfinformationen welche fuer die Berechnung des Impfschutz relevant sind
	 * @return Impfschutz wenn die Person bereits einen Impfschutz hat oder null wenn nicht. Achtung es wird immer ein
	 * neues Object zurueckgegeben auch wenn im impfinformationDto schon eines besteht
	 */
	@Override
	@NonNull
	public Optional<Impfschutz> calculateImpfschutz(
		@NonNull Fragebogen fragebogen,
		@NonNull ImpfinformationDto impfinformationDto
	) {
		return findImpfschutzMitFruehesterFreigabe(fragebogen, impfinformationDto);
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
				orderedErkrankungen)) // pro Regel den Impfschutz ausrechnen
			.reduce(BoosterPrioUtil::mergeImpfschutzOptionals)
			.orElse(Optional.empty());
	}
}
