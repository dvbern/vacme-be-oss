package ch.dvbern.oss.vacme.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.MissingForGrundimmunisiert;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ImpfinformationenUtil {

	private ImpfinformationenUtil() {
		// util
	}

	@NonNull
	public static List<Impfung> getImpfungenOrderedByImpffolgeNr(@NonNull ImpfinformationDto infos) {
		List<Impfung> list = new ArrayList<>();
		if (infos.getImpfung1() != null) {
			list.add(infos.getImpfung1());
		}
		if (infos.getImpfung2() != null) {
			list.add(infos.getImpfung2());
		}
		if (infos.getBoosterImpfungen() != null) {
			// todo homa make sure impfungen are really ordered (currently done by query)
			List<Impfung> orderedBooster = orderBoosterImpfungenByImpffolgeNr(infos, infos.getBoosterImpfungen());
			list.addAll(orderedBooster);
		}
		return list;
	}

	@NonNull
	public static List<Impfung> orderBoosterImpfungenByImpffolgeNr(
		@NonNull ImpfinformationDto infos,
		@Nullable List<Impfung> impfungen
	) {
		if (impfungen == null) {
			return Collections.emptyList();
		}
		return pairImpfungToDossiereintrag(infos, impfungen).stream()
			.sorted(Comparator.comparingInt(value -> value.getRight().getImpffolgeNr()))
			.map(Pair::getLeft)
			.collect(Collectors.toList());
	}

	public static boolean isRelevantImpfungBeforeCoronaTest(@NonNull Impfdossier impfdossier, @NonNull LocalDateTime relevantImpfungTimestamp) {
		boolean vacmeGenesen =
			impfdossier.getVollstaendigerImpfschutzTyp() == VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME_GENESEN;
		if (vacmeGenesen) {
			// Wir pruefen das Datum nur, wenn es ueberhaupt erfasst war (war Anfangs nicht vorhanden)
			// Wenn kein PositivGetestetDatum -> Die Regel wird nicht geprueft
			final LocalDate pcrDatum = impfdossier.getZweiteGrundimmunisierungVerzichtet().getPositivGetestetDatum();
			if (pcrDatum != null) {
				final LocalDate impfungDatum = relevantImpfungTimestamp.toLocalDate();
				return pcrDatum.isAfter(impfungDatum);
			}
			return false;
		}
		return false;
	}

	public static boolean willBeGrundimmunisiertAfterErstimpfungImpfstoff(
		@NonNull @NotNull Impfstoff erstImpfungImpfstoff,
		@Nullable ExternesZertifikat externesZertifikat
	) {
		// Erstimpfung z.B. mit J&J
		if (erstImpfungImpfstoff.getAnzahlDosenBenoetigt() == 1) {
			return true;
		}
		// Externes Zertifikat braucht nur noch 1 VacMe-Impfung
		if (externesZertifikat != null
			&& externesZertifikat.getMissingForGrundimmunisiert(externesZertifikat.getImpfdossier()
			.getKrankheitIdentifier()) == MissingForGrundimmunisiert.BRAUCHT_1_IMPFUNG) {
			return true;
		}
		// sonst: z.B. Erstimpfung mit Moderna -> braucht noch Zweitimpfung
		return false;
	}

	@Nullable
	public static Impfdossiereintrag getDossiereintragForImpfung(@NonNull ImpfinformationDto infos, @NonNull Impfung impfung) {
		if (infos.getImpfdossier() != null) {
			for (Impfdossiereintrag eintrag : infos.getImpfdossier().getImpfdossierEintraege()) {
				if (impfung.getTermin().equals(eintrag.getImpftermin())) {
					return eintrag;
				}
			}
		}
		return null;
	}


	@NonNull
	private static List<Pair<Impfung, Impfdossiereintrag>> pairImpfungToDossiereintrag(
		@NonNull ImpfinformationDto infos,
		@NonNull List<Impfung> impfungen
	) {
		return impfungen
			.stream()
			.map(impfung -> {
				Impfdossiereintrag dossiereintragForImpfung =
					getDossiereintragForImpfung(infos, impfung);
				if (dossiereintragForImpfung == null) {
					String msg = String.format("Keinn Impfdossiereintrag in Impfinfos fuer Impfung %s gefunden", impfung.getId());
					throw AppValidationMessage.ILLEGAL_STATE.create(msg);
				}
				return Pair.of(impfung, dossiereintragForImpfung);
			}).collect(Collectors.toList());
	}
}
