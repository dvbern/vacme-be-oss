package ch.dvbern.oss.vacme.service.impfinformationen;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import ch.dvbern.oss.vacme.entities.embeddables.Adresse;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.service.ImpfdokumentationService;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import ch.dvbern.oss.vacme.wrapper.VacmeDecoratorFactory;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Setter
public class ImpfinformationBuilder {

	@NonNull
	private Fragebogen fragebogen;

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

	private ImpfdokumentationService impfdokumentationService =
		new ImpfdokumentationService(null, null, null, null, null, null, null, null);

	public ImpfinformationBuilder create(@NonNull KrankheitIdentifier krankheit) {
		// Reset all previous data
		krankheitIdentifier = krankheit;
		impfung1 = null;
		impfung2 = null;
		externesZertifikat = null;
		dossier = new Impfdossier();
		fragebogen = TestdataCreationUtil.createFragebogen();
		fragebogen.getRegistrierung().setRegistrierungsnummer("123456");
		fragebogen.getRegistrierung().setAbgleichElektronischerImpfausweis(true);
		fragebogen.getRegistrierung().setRegistrationTimestamp(LocalDateTime.now());
		dossier.setRegistrierung(fragebogen.getRegistrierung());
		dossier.setDossierStatus(VacmeDecoratorFactory.getDecorator(krankheitIdentifier).getStartStatusImpfdossier());
		dossier.setKrankheitIdentifier(krankheit);
		return this;
	}

	public ImpfinformationBuilder withRegistrierungsEingang(@NonNull RegistrierungsEingang registrierungsEingang) {
		fragebogen.getRegistrierung().setRegistrierungsEingang(registrierungsEingang);
		return this;
	}

	public ImpfinformationBuilder withAge(int age) {
		fragebogen.getRegistrierung().setGeburtsdatum(LocalDate.now().minusYears(age));
		return this;
	}

	public ImpfinformationBuilder withSchnellschema(boolean schnellschema) {
		dossier.setSchnellschema(schnellschema);
		return this;
	}

	public ImpfinformationBuilder withImmunsuprimiert(boolean immunsuprimiert) {
		fragebogen.setImmunsupprimiert(immunsuprimiert);
		return this;
	}

	public ImpfinformationBuilder withBirthday(LocalDate bDay) {
		fragebogen.getRegistrierung().setGeburtsdatum(bDay);
		return this;
	}

	public ImpfinformationBuilder withPrioritaet(Prioritaet prio) {
		fragebogen.getRegistrierung().setPrioritaet(prio);
		return this;
	}

	public ImpfinformationBuilder withName(String name, String vorname) {
		fragebogen.getRegistrierung().setName(name);
		fragebogen.getRegistrierung().setVorname(vorname);
		return this;
	}

	public ImpfinformationBuilder withImpfung1(@NonNull LocalDate dateImpfung1, @NonNull Impfstoff impfstoff) {
		Objects.requireNonNull(fragebogen);
		dossier.setVollstaendigerImpfschutzTyp(null);
		impfung1 =
			TestdataCreationUtil.createImpfungWithImpftermin(dateImpfung1.atStartOfDay(), Impffolge.ERSTE_IMPFUNG);
		impfung1.setImpfstoff(impfstoff);
		final ImpfinformationDto tempInfos = getInfos();
		impfdokumentationService.setNextStatus(
			tempInfos,
			Impffolge.ERSTE_IMPFUNG,
			impfung1,
			false,
			fragebogen.getImmunsupprimiert()); // todo  methode statisch machen und auslagern
		return this;
	}

	public ImpfinformationBuilder withImpfung2(@NonNull LocalDate dateImpfung2, @NonNull Impfstoff impfstoff) {
		Objects.requireNonNull(fragebogen);
		Objects.requireNonNull(impfung1);
		dossier.setVollstaendigerImpfschutzTyp(null);
		impfung2 =
			TestdataCreationUtil.createImpfungWithImpftermin(dateImpfung2.atStartOfDay(), Impffolge.ZWEITE_IMPFUNG);
		impfung2.setImpfstoff(impfstoff);
		final ImpfinformationDto tempInfos = getInfos();
		impfdokumentationService.setNextStatus(
			tempInfos,
			Impffolge.ZWEITE_IMPFUNG,
			impfung2,
			false,
			fragebogen.getImmunsupprimiert()); // todo  methode statisch machen und auslagern
		return this;
	}

	public ImpfinformationBuilder withCoronaTest(@NonNull LocalDate dateOfTest) {
		Objects.requireNonNull(fragebogen);
		Objects.requireNonNull(impfung1);
		dossier.setStatusToAbgeschlossenOhneZweiteImpfung(getInfos(), true, null, dateOfTest);
		return this;
	}

	public ImpfinformationBuilder withExternesZertifikatOhneTest(
		@NonNull Impfstoff impfstoff,
		int anzahl,
		@Nullable LocalDate newestImpfdatum) {
		Objects.requireNonNull(fragebogen);
		externesZertifikat = new ExternesZertifikat();
		externesZertifikat.setImpfstoff(impfstoff);
		externesZertifikat.setAnzahlImpfungen(anzahl);
		externesZertifikat.setLetzteImpfungDate(newestImpfdatum);
		externesZertifikat.setImpfdossier(dossier);
		Objects.requireNonNull(dossier);
		if (krankheitIdentifier.isSupportsImpffolgenEinsUndZwei()) {
			VacmeDecoratorFactory.getDecorator(krankheitIdentifier)
				.setStatusToImmunisiertWithExternZertifikat(getInfos(),
					externesZertifikat,
					fragebogen.getImmunsupprimiert());
		} else {
			final ImpfinformationDto tempInfos = getInfos();
			VacmeDecoratorFactory.getDecorator(krankheitIdentifier)
				.recalculateVollstaendigerImpfschutzTypBasedOnImmunsupprimiert(
					tempInfos,
					fragebogen.getImmunsupprimiert());
		}
		return this;
	}

	public ImpfinformationBuilder withExternesZertifikat(
		@NonNull Impfstoff impfstoff,
		int anzahl,
		@NonNull LocalDate newestImpfdatum,
		@Nullable LocalDate dateOfTest) {
		Objects.requireNonNull(fragebogen);
		externesZertifikat = new ExternesZertifikat();
		externesZertifikat.setImpfstoff(impfstoff);
		if (dateOfTest != null) {
			externesZertifikat.setGenesen(true);
			externesZertifikat.setPositivGetestetDatum(dateOfTest);
		} else {
			externesZertifikat.setGenesen(false);
			externesZertifikat.setPositivGetestetDatum(null);
		}
		externesZertifikat.setAnzahlImpfungen(anzahl);
		externesZertifikat.setLetzteImpfungDate(newestImpfdatum);
		externesZertifikat.setImpfdossier(dossier);
		Objects.requireNonNull(dossier);
		if (krankheitIdentifier.isSupportsImpffolgenEinsUndZwei()) {
			VacmeDecoratorFactory.getDecorator(krankheitIdentifier)
				.setStatusToImmunisiertWithExternZertifikat(getInfos(),
					externesZertifikat,
					fragebogen.getImmunsupprimiert());
		} else {
			final ImpfinformationDto tempInfos = getInfos();
			VacmeDecoratorFactory.getDecorator(krankheitIdentifier)
				.recalculateVollstaendigerImpfschutzTypBasedOnImmunsupprimiert(
					tempInfos,
					fragebogen.getImmunsupprimiert());
		}
		return this;
	}

	public ImpfinformationBuilder withUnbekannterAffenpockenimpfungInKindheit(
		@Nullable LocalDate newestImpfdatum,
		int anzahl) {
		Objects.requireNonNull(fragebogen);
		externesZertifikat = new ExternesZertifikat();
		externesZertifikat.setImpfstoff(TestdataCreationUtil.createImpfstoffForUnbekannteAffenpockenimpfstoffInKindheit());
		if (newestImpfdatum != null) {
			externesZertifikat.setLetzteImpfungDate(newestImpfdatum);
			externesZertifikat.setLetzteImpfungDateUnknown(false);
		} else {
			externesZertifikat.setLetzteImpfungDate(null);
			externesZertifikat.setLetzteImpfungDateUnknown(true);
		}
		externesZertifikat.setAnzahlImpfungen(anzahl);
		externesZertifikat.setGenesen(false);
		externesZertifikat.setPositivGetestetDatum(null);
		Objects.requireNonNull(dossier);
		if (krankheitIdentifier.isSupportsImpffolgenEinsUndZwei()) {
			VacmeDecoratorFactory.getDecorator(krankheitIdentifier)
				.setStatusToImmunisiertWithExternZertifikat(getInfos(),
					externesZertifikat,
					fragebogen.getImmunsupprimiert());
		} else {
			final ImpfinformationDto tempInfos = getInfos();
			VacmeDecoratorFactory.getDecorator(krankheitIdentifier)
				.recalculateVollstaendigerImpfschutzTypBasedOnImmunsupprimiert(
					tempInfos,
					fragebogen.getImmunsupprimiert());
		}
		return this;
	}

	public ImpfinformationBuilder withAdresse(@NonNull Adresse adresse) {
		fragebogen.getRegistrierung().setAdresse(adresse);
		return this;
	}

	public ImpfinformationBuilder withBooster(@NonNull LocalDate date, @NonNull Impfstoff impfstoff) {
		return withBooster(date, impfstoff, false);
	}

	public ImpfinformationBuilder withBooster(
		@NonNull LocalDate date,
		@NonNull Impfstoff impfstoff,
		boolean isGrundimmunisierung) {
		Impfdossier tempDossier = new Impfdossier();
		tempDossier.setRegistrierung(fragebogen.getRegistrierung());
		ImpfinformationDto tempInfos = getInfos();
		tempInfos = TestdataCreationUtil.addBoosterImpfung(tempInfos, date, impfstoff);
		boosterImpfungen = tempInfos.getBoosterImpfungen();
		Objects.requireNonNull(boosterImpfungen);
		dossier = Objects.requireNonNull(tempInfos.getImpfdossier());
		dossier.setKrankheitIdentifier(krankheitIdentifier);
		final Impfung relevanteImpfung = boosterImpfungen.get(boosterImpfungen.size() - 1);
		relevanteImpfung.setGrundimmunisierung(isGrundimmunisierung);
		dossier.setStatusToImmunisiertAfterBooster(tempInfos, relevanteImpfung, fragebogen.getImmunsupprimiert());
		return this;
	}

	public ImpfinformationBuilder withDossiereintrag(int impffolgeNr) {
		Impfdossiereintrag eintrag = new Impfdossiereintrag();
		eintrag.setImpfdossier(dossier);
		eintrag.setImpffolgeNr(impffolgeNr);
		dossier.getImpfdossierEintraege().add(eintrag);
		return this;
	}

	@NonNull
	public Fragebogen getFragebogen() {
		return fragebogen;
	}

	@NonNull
	public Registrierung getRegistrierung() {
		return fragebogen.getRegistrierung();
	}

	@NonNull
	public ImpfinformationDto getInfos() {
		return new ImpfinformationDto(
			krankheitIdentifier,
			fragebogen.getRegistrierung(),
			impfung1,
			impfung2,
			dossier,
			boosterImpfungen,
			externesZertifikat);
	}

}
