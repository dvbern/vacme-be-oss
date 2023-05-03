/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.service.boosterprioritaet.rules;

import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.service.ImpfstoffService;
import ch.dvbern.oss.vacme.service.boosterprioritaet.ImpfstoffInfosForRules;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This producer keeps a cached instance of the Impfstoff Information obejct that is used in the rule Engine
 * It is "cached" because the engine runs millions of times but the Impfstoffe seldom change
 */
@ApplicationScoped
@Slf4j
public class ImpfstoffinfosForRulesProducer {

	private final ImpfstoffService impfstoffService;

	@NonNull
	private final Supplier<ImpfstoffInfosForRules> specifiedImpfstoffeCache;

	@Inject
	public ImpfstoffinfosForRulesProducer(ImpfstoffService impfstoffService) {
		this.impfstoffService = impfstoffService;
		this.specifiedImpfstoffeCache = Suppliers.memoizeWithExpiration(() -> {
			LOG.info("ImpfstoffinfosForRule are recreated");
			return new ImpfstoffInfosForRules(this.impfstoffService
				.findAllImpfstoffeWithKrankheitenUnsorted());
		}, 8, TimeUnit.MINUTES);
	}

	@Produces
	@NonNull
	public ImpfstoffInfosForRules produceSpecifiedImpfstoffeList() {
		return this.specifiedImpfstoffeCache.get();
	}
}
