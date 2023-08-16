package ch.dvbern.oss.vacme.entities.impfen;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum KantonaleBerechtigung {

	KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG, // Kanton hat Einsicht und kann Korrekturen vornehmen
	KANTONALE_IMPFKAMPAGNE,	// Kanton hat keine Einsicht
	LEISTUNGSERBRINGER; // Kanton hat keine Einsicht

	public static boolean isEditableForKanton(@Nullable Impfung impfung) {
		if (impfung != null) {
			return editableForKanton().contains(impfung.getKantonaleBerechtigung());
		}
		return true;
	}

	@NonNull
	public static List<KantonaleBerechtigung> editableForKanton() {
		return List.of(KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG);
	}

	public boolean isLeistungserbringer() {
		return this == LEISTUNGSERBRINGER;
	}
}
