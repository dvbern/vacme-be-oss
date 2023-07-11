package ch.dvbern.oss.vacme.service;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.base.MandantProperty;
import ch.dvbern.oss.vacme.entities.base.MandantPropertyKey;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.repo.MandantPropertyRepo;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MandantPropertyService {

	private final MandantPropertyRepo mandantPropertyRepo;

	@NonNull
	public Optional<MandantProperty> findByKeyAndMandant(
		@NonNull MandantPropertyKey key,
		@NonNull Mandant mandant
	) {
		return mandantPropertyRepo.findByKeyAndMandant(key, mandant);
	}

	@NonNull
	public MandantProperty getByKey(
		@NonNull MandantPropertyKey key,
		@NonNull Mandant mandant
	) {
		return findByKeyAndMandant(key, mandant)
			.orElseThrow(() -> AppFailureException.entityNotFound(MandantProperty.class, key));
	}
}
