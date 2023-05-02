package ch.dvbern.oss.vacme.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.impfen.Krankheit;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.repo.KrankheitRepo;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class KrankheitService {

	private final KrankheitRepo krankheitRepo;

	private final ImpfungRepo impfungRepo;

	@NonNull
	public Krankheit getByIdentifier(@NonNull KrankheitIdentifier krankheitIdentifier) {
		return krankheitRepo.getByIdentifier(krankheitIdentifier)
			.orElseThrow(() -> AppFailureException.entityNotFound(Krankheit.class, krankheitIdentifier));
	}

	@NonNull
	public boolean getNoFreieTermine(@NonNull KrankheitIdentifier krankheitIdentifier) {
		Krankheit krankheit = getByIdentifier(krankheitIdentifier);
		return krankheit.isNoFreieTermine();
	}

	@NonNull
	public KantonaleBerechtigung getKantonaleBerechtigung(@NonNull KrankheitIdentifier krankheitIdentifier) {
		Krankheit krankheit = getByIdentifier(krankheitIdentifier);
		return krankheit.getKantonaleBerechtigung();
	}

	public boolean getHasAtleastOneImpfungViewableByKanton(
		@NonNull KrankheitIdentifier krankheit,
		@NonNull KantonaleBerechtigung kantonaleBerechtigung
	) {
		return impfungRepo.getOneByKrankheitAndKantonaleBerechtigung(krankheit, kantonaleBerechtigung).isPresent();
	}
}
