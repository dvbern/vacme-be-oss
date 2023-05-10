/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.service;

import java.time.LocalDate;
import java.util.Optional;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.MissingForGrundimmunisiert;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.helper.TestImpfstoff;
import ch.dvbern.oss.vacme.jax.registration.ExternGeimpftJax;
import ch.dvbern.oss.vacme.jax.registration.ImpfstoffJax;
import ch.dvbern.oss.vacme.repo.ExternesZertifikatRepo;
import ch.dvbern.oss.vacme.repo.ImpfempfehlungChGrundimmunisierungRepo;
import ch.dvbern.oss.vacme.repo.ImpfstoffRepo;
import ch.dvbern.oss.vacme.repo.KrankheitRepo;
import ch.dvbern.oss.vacme.service.booster.BoosterService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationBuilder;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;

class ExternesZertifikatServiceWithZertifikatAdditionTest {

	private ExternesZertifikatService serviceUnderTest;

	private ExternesZertifikatRepo externesZertifikatRepo;
	private ImpfstoffService impfstoffService;
	private ExternesZertifikat persistedZertifikat;
	private BoosterService boosterService;
	private DossierService dossierService;

	@BeforeEach
	void setUp() {

		// ImpfstoffService
		ImpfstoffRepo impfstoffRepo = Mockito.mock(ImpfstoffRepo.class);
		ImpfempfehlungChGrundimmunisierungRepo empfehlungRepo =
			Mockito.mock(ImpfempfehlungChGrundimmunisierungRepo.class);
		KrankheitRepo krankheitRepo = Mockito.mock(KrankheitRepo.class);
		impfstoffService = new ImpfstoffService(impfstoffRepo, empfehlungRepo, krankheitRepo);
		Mockito.when(impfstoffRepo.getById(any())).thenAnswer(invocation -> {
			ID<Impfstoff> argument = invocation.getArgument(0, ID.class);
			return Optional.ofNullable(TestImpfstoff.getImpfstoffById(argument.getId()));
		});

		// ExternesZertifikatRepo
		externesZertifikatRepo = Mockito.mock(ExternesZertifikatRepo.class);
		// - find

		boosterService = Mockito.mock(BoosterService.class);
		dossierService =  Mockito.mock(DossierService.class);
		serviceUnderTest = new ExternesZertifikatService(
			externesZertifikatRepo,
			impfstoffService,
			Mockito.mock(UserPrincipal.class),
			Mockito.mock(ImpfinformationenService.class),
			boosterService,
			dossierService,
			Mockito.mock(ImpfdossierService.class)
		);
	}

	@Test
	void when_externes_Zertifikat_is_added_it_should_be_passed_to_calculation() {
		// prepare mocks to simulate ExternesZertifikat creation

		Mockito
			.doAnswer(invocation -> Optional.empty())
			.when(externesZertifikatRepo).findExternesZertifikatForDossier(any());
		// - create
		Mockito.doAnswer(invocation -> {
			persistedZertifikat = invocation.getArgument(0, ExternesZertifikat.class);
			return persistedZertifikat;
		}).when(externesZertifikatRepo).create(any(ExternesZertifikat.class));

		// setup service call
		ExternGeimpftJax grundimmunisiertesExternesZertifikat = getExternGeimpftJaxWithGrundimmunisierung();
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		ImpfinformationDto infos = builder.create(KrankheitIdentifier.COVID).getInfos();

		serviceUnderTest.saveExternGeimpftImpfling(infos, grundimmunisiertesExternesZertifikat, false);

		// verify results
		Mockito.verify(externesZertifikatRepo, Mockito.times(1)).create(any(ExternesZertifikat.class));
		Mockito.verify(boosterService, Mockito.times(1)).recalculateImpfschutzAndStatusmovesForSingleReg(any(ImpfinformationDto.class), any(Boolean.class));
		ArgumentCaptor<ImpfinformationDto> argumentCaptor = ArgumentCaptor.forClass(ImpfinformationDto.class);
		Mockito.verify(boosterService).recalculateImpfschutzAndStatusmovesForSingleReg(argumentCaptor.capture(), any(Boolean.class));
		ImpfinformationDto passedInfosForRecalculation = argumentCaptor.getValue();
		Assertions.assertNotNull(
			passedInfosForRecalculation.getExternesZertifikat(),
			"After passing an external Zertifikat along it should be created and passed to the boosterRecalculation Method"
		);
		Assertions.assertEquals(grundimmunisiertesExternesZertifikat.getAnzahlImpfungen(), passedInfosForRecalculation.getExternesZertifikat().getAnzahlImpfungen());
		Assertions.assertEquals(grundimmunisiertesExternesZertifikat.getLetzteImpfungDate(), passedInfosForRecalculation.getExternesZertifikat().getLetzteImpfungDate());
		Assertions.assertEquals(grundimmunisiertesExternesZertifikat.getPositivGetestetDatum(), passedInfosForRecalculation.getExternesZertifikat().getPositivGetestetDatum());
		Mockito.verify(dossierService, Mockito.times(1)).freigabestatusUndTermineEntziehenFallsImpfschutzNochNichtFreigegeben(passedInfosForRecalculation);
	}

	@Test
	void when_no_Zertifikat_is_added_it_should_not_be_passed_to_calculation() {
		// prepare mocks to simulate ExternesZertifikat creation

		Mockito
			.doAnswer(invocation -> Optional.empty())
			.when(externesZertifikatRepo).findExternesZertifikatForDossier(any());

		// setup service call
		ExternGeimpftJax grundimmunisiertesExternesZertifikat = getExternGeimpftJaxWithNoExternenImpfungen();
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		ImpfinformationDto infos = builder.create(KrankheitIdentifier.COVID).getInfos();

		serviceUnderTest.saveExternGeimpftImpfling(infos, grundimmunisiertesExternesZertifikat, false);

		// verify results
		Mockito.verify(externesZertifikatRepo, Mockito.times(0)).create(any(ExternesZertifikat.class));
		Mockito.verify(boosterService, Mockito.times(1)).recalculateImpfschutzAndStatusmovesForSingleReg(any(ImpfinformationDto.class), any(Boolean.class));

		ArgumentCaptor<ImpfinformationDto> argumentCaptor = ArgumentCaptor.forClass(ImpfinformationDto.class);
		Mockito.verify(boosterService).recalculateImpfschutzAndStatusmovesForSingleReg(argumentCaptor.capture(), any(Boolean.class));
		ImpfinformationDto passedInfosForRecalculation = argumentCaptor.getValue();
		Assertions.assertNull(
			passedInfosForRecalculation.getExternesZertifikat(),
			"After specifiying no external Impfungen no externes Zertifikat should exist"
		);
		Mockito.verify(dossierService, Mockito.times(1)).freigabestatusUndTermineEntziehenFallsImpfschutzNochNichtFreigegeben(passedInfosForRecalculation);
	}

	@NotNull
	private ExternGeimpftJax getExternGeimpftJaxWithGrundimmunisierung() {
		ExternGeimpftJax grundimmunisiertesExternesZertifikat = new ExternGeimpftJax(
			true,
			false,
			LocalDate.of(2021, 1, 1),
			ImpfstoffJax.from(TestdataCreationUtil.createImpfstoffPfizer()),
			2,
			false,
			null,
			true,
			false,
			MissingForGrundimmunisiert.BRAUCHT_0_IMPFUNGEN,
			false,
			MissingForGrundimmunisiert.BRAUCHT_0_IMPFUNGEN,
			false);
		return grundimmunisiertesExternesZertifikat;
	}

	private ExternGeimpftJax getExternGeimpftJaxWithNoExternenImpfungen() {
		ExternGeimpftJax noExterneImpfungenExtJax = new ExternGeimpftJax(
			false,
			false,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			false
		);
		return noExterneImpfungenExtJax;
	}
}
