package ch.dvbern.oss.vacme.reports.impfungenMail;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.enums.Kundengruppe;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Transactional
@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MailImpfungenServiceBean {

	private final ImpfungRepo impfungRepo;

	public String generateMailContent(@NonNull KrankheitIdentifier krankheitIdentifier) {
		long anzahl = impfungRepo.getAnzahlImpfungen(krankheitIdentifier);
		return "Kumulierte Anzahl Impfungen " + krankheitIdentifier + ": " + anzahl + "\n\n";
	}

	public String generateMailContent(@NonNull KrankheitIdentifier krankheitIdentifier, @NonNull Kundengruppe kundengruppe) {
		long anzahl = impfungRepo.getAnzahlImpfungen(krankheitIdentifier, kundengruppe);
		return "Kumulierte Anzahl Impfungen " + krankheitIdentifier + ": " + anzahl + "\n\n";
	}
}
