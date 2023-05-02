/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */
// TODO falsches copyright, glaubs überall
package ch.dvbern.oss.vacme.service.impfschutz;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.service.boosterprioritaet.queue.AffenpockenImpfschutzCalcQueueService;
import ch.dvbern.oss.vacme.service.boosterprioritaet.queue.CovidImpfschutzCalcQueueService;
import ch.dvbern.oss.vacme.service.boosterprioritaet.queue.FsmeImpfschutzCalcQueueService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This is a factory to create the appropriate CalculationService for a given Krankheit
 */
@Slf4j
@ApplicationScoped
public class ImpfschutzCalculationQueueServiceFactory {

	@Inject
	Instance<CovidImpfschutzCalcQueueService> covidImpfschutzCalcQueueServices;

	@Inject
	Instance<AffenpockenImpfschutzCalcQueueService> affenpockenImpfschutzCalcQueueServices;

	@Inject
	Instance<FsmeImpfschutzCalcQueueService> fsmeImpfschutzCalcQueueService;

	@NonNull
	public ImpfschutzCalculationQueueService createImpfschutzCalculationService(
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		switch (krankheitIdentifier) {
		case COVID:
			return this.covidImpfschutzCalcQueueServices.get();
		case AFFENPOCKEN:
			return this.affenpockenImpfschutzCalcQueueServices.get();
		case FSME:
			return this.fsmeImpfschutzCalcQueueService.get();
		default:
			String msg = String.format(
				"No Service implementing %s for Krankheit %s found",
				ImpfschutzCalculationService.class.getSimpleName(),
				krankheitIdentifier);
			throw new NotImplementedException(msg);
		}
	}
}
