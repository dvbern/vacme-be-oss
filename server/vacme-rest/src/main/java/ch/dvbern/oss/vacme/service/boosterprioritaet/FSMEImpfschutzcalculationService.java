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
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.base.ImpfInfo;
import ch.dvbern.oss.vacme.entities.impfen.Erkrankung;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.FSMEImpfschutzRule;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.IBoosterPrioritaetRule;
import ch.dvbern.oss.vacme.service.impfschutz.ImpfschutzCalculationService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * This Service can be used to calculate the Impfschutz for FSME
 */
@Slf4j
@ApplicationScoped
public class FSMEImpfschutzcalculationService implements ImpfschutzCalculationService {

	final List<IBoosterPrioritaetRule> konventionelleRules = new ArrayList<>();
	final List<IBoosterPrioritaetRule> schnellschemaRules = new ArrayList<>();

	@ConfigProperty(name = "boosterrule.fsme.minage", defaultValue = "6")
	public int minAge;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.konventionell.withimpfschutz.amount", defaultValue = "10")
	public int freigabeOffsetKonventionellWithImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.konventionell.withimpfschutz.unit", defaultValue = "YEARS")
	public ChronoUnit freigabeOffsetKonventionellWithImpfschutzUnit;

	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.firstbooster.schnellschema.withimpfschutz.amount",
		defaultValue = "12")
	public int freigabeOffsetFirstEncepurBoosterSchnellschemaWithImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.firstbooster.schnellschema.withimpfschutz.unit",
		defaultValue = "MONTHS")
	public ChronoUnit freigabeOffsetFirstEncepurBoosterSchnellschemaWithImpfschutzUnit;

	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.firstimpfung.konventionell.noimpfschutz.amount",
		defaultValue = "4")
	public int freigabeOffsetKonventionellWithoutImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.firstimpfung.konventionell.noimpfschutz.unit",
		defaultValue = "WEEKS")
	public ChronoUnit freigabeOffsetKonventionellWithoutImpfschutzUnit;

	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.fsmeimmune.firstimpfung.schnellschema.noimpfschutz.amount",
		defaultValue = "14")
	public int freigabeOffsetFirstFSMEImmuneImpfungSchnellschemaWithoutImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.fsmeimmune.firstimpfung.schnellschema.noimpfschutz.unit",
		defaultValue = "DAYS")
	public ChronoUnit freigabeOffsetFirstFSMEImmuneImpfungSchnellschemaWithoutImpfschutzUnit;

	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.firstimpfung.schnellschema.noimpfschutz.amount",
		defaultValue = "7")
	public int freigabeOffsetFirstEncepurImpfungSchnellschemaWithoutImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.firstimpfung.schnellschema.noimpfschutz.unit",
		defaultValue = "DAYS")
	public ChronoUnit freigabeOffsetFirstEncepurImpfungSchnellschemaWithoutImpfschutzUnit;

	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.fsmeimmune.secondimpfung.noimpfschutz.amount",
		defaultValue = "5")
	public int freigabeOffsetSecondFSMEImmuneImpfungWithoutImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.fsmeimmune.secondimpfung.noimpfschutz.unit",
		defaultValue = "MONTHS")
	public ChronoUnit freigabeOffsetSecondFSMEImmuneImpfungWithoutImpfschutzUnit;

	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.secondimpfung.konventionell.noimpfschutz.amount",
		defaultValue = "9")
	public int freigabeOffsetSecondEncepurImpfungKonventionellWithoutImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.secondimpfung.konventionell.noimpfschutz.unit",
		defaultValue = "MONTHS")
	public ChronoUnit freigabeOffsetSecondEncepurImpfungKonventionellWithoutImpfschutzUnit;

	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.secondimpfung.schnellschema.noimpfschutz.amount",
		defaultValue = "14")
	public int freigabeOffsetSecondEncepurImpfungSchnellschemaWithoutImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.secondimpfung.schnellschema.noimpfschutz.unit",
		defaultValue = "DAYS")
	public ChronoUnit freigabeOffsetSecondEncepurImpfungSchnellschemaWithoutImpfschutzUnit;

	@NonNull
	private ImpfstoffInfosForRules specifiedImpfstoffe;

	@Inject
	public FSMEImpfschutzcalculationService(@NonNull ImpfstoffInfosForRules specifiedImpfstoffe) {
		this.specifiedImpfstoffe = specifiedImpfstoffe;
	}

	/**
	 * Initialisiere die Regeln mit ihren Parametern
	 */
	@PostConstruct
	void initRules() {
		if (MandantUtil.getMandant().equals(Mandant.BE)) {
			addKonventionellRules();
			addSchnellschemaRules();
		} else {
			throw AppValidationMessage.ILLEGAL_STATE.create("Mandant "
				+ MandantUtil.getMandant()
				+ " does not support FSME");
		}
		// do init in postConstruct so all injected configs are ready to be used
		orderListByAnzahlMonateBisFreigabe();
	}

	private void addSchnellschemaRules() {
		schnellschemaRules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID, Constants.ENCEPUR_UUID),
			minAge, null,
			0,
			ChronoUnit.DAYS,
			0, 0
		));

		// For FSME-Immune schnellschema first offset is 14 days.
		schnellschemaRules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID),
			minAge, null,
			freigabeOffsetFirstFSMEImmuneImpfungSchnellschemaWithoutImpfschutz,
			freigabeOffsetFirstFSMEImmuneImpfungSchnellschemaWithoutImpfschutzUnit,
			1, 1
		));

		// For Encepur schnellschema first offset is 7 days.
		schnellschemaRules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.ENCEPUR_UUID),
			minAge, null,
			freigabeOffsetFirstEncepurImpfungSchnellschemaWithoutImpfschutz,
			freigabeOffsetFirstEncepurImpfungSchnellschemaWithoutImpfschutzUnit,
			1, 1
		));

		// For FSME-Immune schnellschema the second offset is 5 months
		schnellschemaRules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID),
			minAge, null,
			freigabeOffsetSecondFSMEImmuneImpfungWithoutImpfschutz,
			freigabeOffsetSecondFSMEImmuneImpfungWithoutImpfschutzUnit,
			2, 2
		));

		// For Encepur schnellschema the second offset is 14 days
		schnellschemaRules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.ENCEPUR_UUID),
			minAge, null,
			freigabeOffsetSecondEncepurImpfungSchnellschemaWithoutImpfschutz,
			freigabeOffsetSecondEncepurImpfungSchnellschemaWithoutImpfschutzUnit,
			2, 2
		));

		// For FSME-Immune schnellschema the booster offset is 10 years.
		schnellschemaRules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID),
			minAge, null,
			freigabeOffsetKonventionellWithImpfschutz,
			freigabeOffsetKonventionellWithImpfschutzUnit,
			3, null
		));

		// For Encepur schnellschema the first booster offset is 12 months.
		schnellschemaRules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.ENCEPUR_UUID),
			minAge, null,
			freigabeOffsetFirstEncepurBoosterSchnellschemaWithImpfschutz,
			freigabeOffsetFirstEncepurBoosterSchnellschemaWithImpfschutzUnit,
			3, 3
		));

		// For Encepur schnellschema the offset for second booster onward is 10 years.
		schnellschemaRules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.ENCEPUR_UUID),
			minAge, null,
			freigabeOffsetKonventionellWithImpfschutz,
			freigabeOffsetKonventionellWithImpfschutzUnit,
			4, null
		));
		// rule die alle Regs matched und nur das immunisiertBis berechnet
		schnellschemaRules.add(FSMEImpfschutzRule.createOhneFreigabeRule(specifiedImpfstoffe));
	}

	private void addKonventionellRules() {
		// first Impfung can be done immediatley
		konventionelleRules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID, Constants.ENCEPUR_UUID),
			minAge, null,
			0,
			ChronoUnit.DAYS,
			0, 0
		));

		// For both Impfstoffe the first offset is 4 weeks.
		konventionelleRules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID, Constants.ENCEPUR_UUID),
			minAge, null,
			freigabeOffsetKonventionellWithoutImpfschutz,
			freigabeOffsetKonventionellWithoutImpfschutzUnit,
			1, 1
		));

		// For FSME-Immune the second offset is 6 months
		konventionelleRules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID),
			minAge, null,
			freigabeOffsetSecondFSMEImmuneImpfungWithoutImpfschutz,
			freigabeOffsetSecondFSMEImmuneImpfungWithoutImpfschutzUnit,
			2, 2
		));

		// For Encepur the second offset is 10 months
		konventionelleRules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.ENCEPUR_UUID),
			minAge, null,
			freigabeOffsetSecondEncepurImpfungKonventionellWithoutImpfschutz,
			freigabeOffsetSecondEncepurImpfungKonventionellWithoutImpfschutzUnit,
			2, 2
		));

		// For both Impfstoffe the booster offset is 10 years.
		konventionelleRules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID, Constants.ENCEPUR_UUID),
			minAge, null,
			freigabeOffsetKonventionellWithImpfschutz,
			freigabeOffsetKonventionellWithImpfschutzUnit,
			3, null
		));

		// rule die alle Regs matched und nur das immunisiertBis berechnet
		konventionelleRules.add(FSMEImpfschutzRule.createOhneFreigabeRule(specifiedImpfstoffe));
	}

	private void orderListByAnzahlMonateBisFreigabe() {
		BoosterPrioUtil.orderListByAnzahlMonateBisFreigabe(this.konventionelleRules);
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
		boolean schnellschema = impfinformationDto.getImpfdossier().isSchnellschema();

		return (schnellschema ? schnellschemaRules : konventionelleRules).stream()
			.map(rule -> rule.calculateImpfschutz(
				fragebogen,
				impfinformationDto,
				orderedImpfInfos,
				orderedErkrankungen)) // pro Regel den Impfschutz ausrechnen
			.reduce(BoosterPrioUtil::mergeImpfschutzOptionals)
			.orElse(Optional.empty());
	}
}
