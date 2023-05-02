/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */
// TODO falsches copyright, glaubs überall
package ch.dvbern.oss.vacme.service.impfschutz;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.service.boosterprioritaet.AffenpockenImpfschutzcalculationService;
import ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioritaetService;
import ch.dvbern.oss.vacme.service.boosterprioritaet.FSMEImpfschutzcalculationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This is a factory to create the appropriate CalculationService for a given Krankheit
 */
@Slf4j
@ApplicationScoped
public class ImpfschutzCalculationServiceFactory {

	@Inject
	Instance<BoosterPrioritaetService> boosterPrioritaetService;

	@Inject
	Instance<AffenpockenImpfschutzcalculationService> affenpockenImpfschutzCalculatorService;

	@Inject
	Instance<FSMEImpfschutzcalculationService> fsmeImpfschutzcalculationService;

	@NonNull
	public ImpfschutzCalculationService createImpfschutzCalculationService(
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		switch (krankheitIdentifier) {
		case COVID:
			return this.boosterPrioritaetService.get();
		case AFFENPOCKEN:
			return this.affenpockenImpfschutzCalculatorService.get();
		case FSME:
			return this.fsmeImpfschutzcalculationService.get();
		default:
			String msg = String.format(
				"No Service implementing %s for Krankheit %s found",
				ImpfschutzCalculationService.class.getSimpleName(),
				krankheitIdentifier);
			throw new NotImplementedException(msg);
		}
	}
}
