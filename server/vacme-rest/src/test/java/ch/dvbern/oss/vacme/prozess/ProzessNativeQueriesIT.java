package ch.dvbern.oss.vacme.prozess;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.massenverarbeitung.MassenverarbeitungQueue;
import ch.dvbern.oss.vacme.entities.massenverarbeitung.MassenverarbeitungQueueTyp;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.ZertifikatCreationDTO;
import ch.dvbern.oss.vacme.jax.impfslot.ImpfslotValidationJax;
import ch.dvbern.oss.vacme.repo.AudHelperRepo;
import ch.dvbern.oss.vacme.repo.BoosterQueueRepo;
import ch.dvbern.oss.vacme.repo.DocumentQueueRepo;
import ch.dvbern.oss.vacme.repo.ImpfslotRepo;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.repo.MassenverarbeitungQueueRepo;
import ch.dvbern.oss.vacme.repo.OnboardingRepo;
import ch.dvbern.oss.vacme.repo.PLZDataRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.repo.ZertifikatRepo;
import ch.dvbern.oss.vacme.repo.vmdl.VMDLRepo;
import ch.dvbern.oss.vacme.reports.reportingImpfungen.ReportingImpfungenReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingKantonKantonsarzt.ReportingKantonReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingKantonKantonsarzt.ReportingKantonsarztReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingOdis.ReportingOdisReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingTerminslots.ReportingTerminslotsReportServiceBean;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.testing.MariaDBProfile;
import ch.dvbern.oss.vacme.testing.MariaDBTestResource;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import io.agroal.api.AgroalDataSource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(MariaDBTestResource.class)
@TestProfile(MariaDBProfile.class)
@QuarkusTest
public class ProzessNativeQueriesIT extends AbstractProzessIT {

	@Inject
	AudHelperRepo audHelperRepo;
	@Inject
	BoosterQueueRepo boosterQueueRepo;

	@Inject
	DocumentQueueRepo documentQueueRepo;

	@Inject
	ImpfslotRepo impfslotRepo;

	@Inject
	ImpfterminRepo impfterminRepo;

	@Inject
	ImpfungRepo impfungRepo;

	@Inject
	VMDLRepo vmdlRepo;

	@Inject
	OnboardingRepo onboardingRepo;

	@Inject
	PLZDataRepo plzDataRepo;

	@Inject
	RegistrierungRepo registrierungRepo;

	@Inject
	ZertifikatRepo zertifikatRepo;

	@Inject
	ReportingImpfungenReportServiceBean reportingImpfungenReportServiceBean;

	@Inject
	ReportingKantonReportServiceBean reportingKantonReportServiceBean;

	@Inject
	ReportingKantonsarztReportServiceBean reportingKantonsarztReportServiceBean;

	@Inject
	ReportingOdisReportServiceBean reportingOdisReportServiceBean;

	@Inject
	ReportingTerminslotsReportServiceBean reportingTerminslotsReportServiceBean;

	@Inject
	AgroalDataSource defaultDataSource;

	@Inject
	MassenverarbeitungQueueRepo massenverarbeitungQueueRepo;

	@Test
	void deleteFromAuditTable_queryIsWorking() {
		Benutzer benutzer = TestdataCreationUtil.createBenutzer("Test", "Tester", "444");
		audHelperRepo.deleteBenutzerDataInAuditTables(benutzer);
	}

	@Test
	void queueRelevantRegsForImpfschutzRecalculation_queryIsWorking() {
		createSequenceIfNotExists("req_queue_sequence");

		createImpfdossierCovidWithBoosteram(LocalDate.now().minusMonths(7), LocalDate.now().minusMonths(1));
		createImpfdossierAffenpockenWithImpfungenAm(LocalDate.now().minusMonths(4), LocalDate.now());
		int resultAP = boosterQueueRepo.queueRelevantRegsForImpfschutzRecalculationAffenpocken();
		int resultCOV = boosterQueueRepo.queueRelevantRegsForImpfschutzRecalculationCovid();
		Assertions.assertEquals(1, resultAP);
		Assertions.assertEquals(1, resultCOV);
	}

	@Test
	void getNextQueueId_queryIsWorking() {
		createSequenceIfNotExists("req_queue_sequence");
		Long previousQueueId = boosterQueueRepo.getNextQueueId();
		Long nextQueueId = boosterQueueRepo.getNextQueueId();
		Assertions.assertNotEquals(previousQueueId, nextQueueId);
		Assertions.assertTrue(nextQueueId > previousQueueId);
	}

	@Test
	void getNextDocumentQueueId_queryIsWorking() {
		createSequenceIfNotExists("document_queue_sequence");
		long previousDocumentQueueId = documentQueueRepo.getNextDocumentQueueId();
		long nextDocumentQueueId = documentQueueRepo.getNextDocumentQueueId();
		Assertions.assertNotEquals(previousDocumentQueueId, nextDocumentQueueId);
		Assertions.assertTrue(nextDocumentQueueId > previousDocumentQueueId);
	}

	@Test
	void getJaxList_queryIsWorking() {
		UUID ortderimpfungId = UUID.randomUUID();
		LocalDate vonDate = LocalDate.now().minusDays(30);
		LocalDate bisDate = LocalDate.now();
		List<ImpfslotValidationJax> result =
			impfslotRepo.validateImpfslotsByOdi(ortderimpfungId, vonDate, bisDate, 30);
		Assertions.assertNotNull(result);
	}

	@Test
	void hasAtLeastFreieImpfslots_queryIsWorking() {
		boolean hasAtLeastFreieImpfslots = impfterminRepo.hasAtLeastFreieImpfslots(100, KrankheitIdentifier.COVID);
		Assertions.assertFalse(hasAtLeastFreieImpfslots);
	}

	@Test
	void wasSentToVMDL_queryIsWorking() {
		ImpfinformationDto infos = createImpfdossierCovidWithBoosterAm(LocalDate.now().minusDays(2));
		Impfung impfungToCheck = Objects.requireNonNull(infos.getBoosterImpfungen()).stream().findFirst().orElseThrow();

		boolean sent = vmdlRepo.wasSentToVMDL(impfungToCheck);
		Assertions.assertFalse(sent);

		impfungToCheck.setTimestampVMDL(LocalDateTime.now());
		impfungRepo.update(impfungToCheck);
		sent = vmdlRepo.wasSentToVMDL(impfungToCheck);
		Assertions.assertTrue(sent);
	}

	@Test
	void getNextOnboardingCode_queryIsWorking() {
		createSequenceIfNotExists("onboarding_sequence");
		String nextOnboardingCode = onboardingRepo.getNextOnboardingCode();
		Assertions.assertNotNull(nextOnboardingCode);
	}

	@Test
	void dropAll_queryIsWorking() {
		plzDataRepo.dropAll();
	}

	@Test
	void dropAllMedstatz_queryIsWorking() {
		plzDataRepo.dropAllMedstat();
	}

	@Test
	void getNextRegistrierungnummer_queryIsWorking() {
		createSequenceIfNotExists("register_sequence");
		String nextRegistrierungnummer = registrierungRepo.getNextRegistrierungnummer();
		Assertions.assertNotNull(nextRegistrierungnummer);
	}

	@Test
	void findAllMigrationImpfungenForZertifikatsGenerationPostRegexbased_queryIsWorking() {
		List<ZertifikatCreationDTO> result =
			zertifikatRepo.findAllMigrationImpfungenForZertifikatsGenerationPostRegexbased("", 1);
		Assertions.assertNotNull(result);
	}

	@Test
	void reportingImpfungen1Query_reportingImpfungen2Query_reportingImpfungenNQuery_queriesAreWorking() {
		StreamingOutput streamingOutput = reportingImpfungenReportServiceBean.generateStatisticsExport();
		Assertions.assertNotNull(readStreamingOutput(streamingOutput));
	}

	@Test
	void reportingKantonQuery_queryIsWorking() {
		StreamingOutput streamingOutput = reportingKantonReportServiceBean.generateStatisticsExport();
		Assertions.assertNotNull(readStreamingOutput(streamingOutput));
	}

	@Test
	void reportingKantonsarztQuery_queryIsWorking() {
		StreamingOutput streamingOutput = reportingKantonsarztReportServiceBean.generateStatisticsExport();
		Assertions.assertNotNull(readStreamingOutput(streamingOutput));
	}

	@Test
	void odisReport_queryIsWorking() {
		byte[] bytes = reportingOdisReportServiceBean.generateStatisticsExport();
		Assertions.assertNotNull(bytes);
	}

	@Test
	void terminslotReport_queryIsWorking() {
		StreamingOutput streamingOutput = reportingTerminslotsReportServiceBean.generateStatisticsExport();
		Assertions.assertNotNull(readStreamingOutput(streamingOutput));
	}

	@Test
	void createQueueItemsJDBC_queryIsWorking() {

		List<UUID> uuidList = new ArrayList<>();
		int numToCreate = 50;
		for (int i = 0; i < numToCreate; i++) {
			uuidList.add(UUID.randomUUID());
		}
		uuidList.add(null); // check if null is a problem
		massenverarbeitungQueueRepo.addImpfgruppeToFreigebenQueue(uuidList);
		List<MassenverarbeitungQueue> foundEntries =
			massenverarbeitungQueueRepo.findMassenverarbeitungQueueItemsToProcess(
				100,
				MassenverarbeitungQueueTyp.IMPFGRUPPE_FREIGEBEN);

		Assertions.assertNotNull(foundEntries);
		Assertions.assertEquals(numToCreate +1, foundEntries.size(), "Expected to find 50 +1 entries that were created");

	}

	// It is important to actually read the stream, else it never gets executed and errors slip through
	private static String readStreamingOutput(StreamingOutput streamingOutput) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			streamingOutput.write(out);
			return out.toString();
		} catch (Exception e) {
			throw new AppFailureException("problem writing to stream ", e);
		}
	}

	private void createSequenceIfNotExists(String sequenceName) {
		try (Connection con = this.defaultDataSource.getConnection(); PreparedStatement ps = con.prepareStatement(
			"CREATE SEQUENCE IF NOT EXISTS " + sequenceName + " START WITH 1 INCREMENT BY 1;")) {
			ps.execute();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
