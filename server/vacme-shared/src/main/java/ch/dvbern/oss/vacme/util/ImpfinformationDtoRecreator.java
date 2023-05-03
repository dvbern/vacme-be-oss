/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.util;

import java.util.List;

import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ImpfinformationDtoRecreator {

	@NonNull
	private Registrierung registrierung;

	@Nullable
	private Impfung impfung1;

	@Nullable
	private Impfung impfung2;

	@NonNull
	private Impfdossier dossier;

	@Nullable
	private List<Impfung> boosterImpfungen;

	@Nullable
	private ExternesZertifikat externesZertifikat;

	@NonNull
	private KrankheitIdentifier krankheitIdentifier;

	public ImpfinformationDtoRecreator(@NonNull ImpfinformationDto infos) {
		krankheitIdentifier = infos.getKrankheitIdentifier();
		registrierung = infos.getRegistrierung();
		impfung1 = infos.getImpfung1();
		impfung2 = infos.getImpfung2();
		dossier = infos.getImpfdossier();
		boosterImpfungen = infos.getBoosterImpfungen();
		externesZertifikat = infos.getExternesZertifikat();
	}

	@NonNull
	public ImpfinformationDtoRecreator withExternemZertifikat(@Nullable ExternesZertifikat newExternesZertifikat) {
		this.externesZertifikat = newExternesZertifikat;
		return this;
	}

	@NonNull
	public ImpfinformationDtoRecreator withBoosterImpfungen(@NonNull List<Impfung> newBoosterImpfungen) {
		this.boosterImpfungen = newBoosterImpfungen;
		return this;
	}

	@NonNull
	public ImpfinformationDtoRecreator withDossier(@NonNull Impfdossier impfdossier) {
		this.dossier = impfdossier;
		this.registrierung = dossier.getRegistrierung();
		return this;
	}

	@NonNull
	public ImpfinformationDto build() {
		return new ImpfinformationDto(
			krankheitIdentifier,
			registrierung,
			impfung1,
			impfung2,
			dossier,
			boosterImpfungen,
			externesZertifikat
		);
	}

	@NonNull
	public static ImpfinformationDtoRecreator from(@NonNull ImpfinformationDto infos) {
		return new ImpfinformationDtoRecreator(infos);
	}
}
