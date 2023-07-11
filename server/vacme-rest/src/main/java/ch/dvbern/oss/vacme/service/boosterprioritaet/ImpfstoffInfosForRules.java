/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.service.boosterprioritaet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.ZulassungsStatus;
import lombok.extern.java.Log;
import org.checkerframework.checker.nullness.qual.NonNull;


@Log
public class ImpfstoffInfosForRules {

	@NonNull
	private final Set<Impfstoff> specifiedEmpfohleneBoosterImpfstoffe;


	public ImpfstoffInfosForRules(
		@NonNull List<Impfstoff> allSpecifiedImpfst
	){
		List<Impfstoff> allSpecifiedImpstoffeNichtEingestellt = allSpecifiedImpfst
			.stream()
			.filter(impfstoff -> !impfstoff.isEingestellt())
			.collect(Collectors.toList());
		this.specifiedEmpfohleneBoosterImpfstoffe = findImpfstoffeEmpfohlenForBooster(allSpecifiedImpstoffeNichtEingestellt);
	}

	@NonNull
	public Set<Impfstoff> getImpfstoffeEmpfohlenForBoosterForKrankheit(@NonNull KrankheitIdentifier krankheitIdentifier) {
		return specifiedEmpfohleneBoosterImpfstoffe
			.stream()
			.filter(impfstoff -> impfstoff.getKrankheiten()
				.stream()
				.anyMatch(krankheitEntity -> krankheitEntity.getKrankheitIdentifier() == krankheitIdentifier))
			.collect(Collectors.toSet());
	}

	@NonNull
	private Set<Impfstoff> findImpfstoffeEmpfohlenForBooster(@NonNull List<Impfstoff> allSpecifiedImpfstoffe) {
		Set<Impfstoff> impfstoffe = allSpecifiedImpfstoffe.stream()
			.filter(impfstoff -> impfstoff.getZulassungsStatusBooster() == ZulassungsStatus.EMPFOHLEN)
			.collect(Collectors.toUnmodifiableSet());
		if (impfstoffe.isEmpty()) {
			LOG.warning("VACME-CONFIG: No Impfstoff that is not eingestellt has Zulasstungstatus Empfohlen. Check configuration");
		}

		return impfstoffe;
	}
}
