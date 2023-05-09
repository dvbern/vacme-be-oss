package ch.dvbern.oss.vacme.fhir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ImpfinformationenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Resource;
import org.jetbrains.annotations.NotNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.vacme.fhir.FhirResourceMapper.URN_UUID;
import static ch.dvbern.oss.vacme.shared.util.Constants.NOVAVAX_UUID;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FhirService {

	//@Devs: If you need to run the download of fhir xml in quarkus:dev mode please change this property in your .env file
	// according to the .env-template (e.g. VACME_FHIR_PATH_CUSTOMNARRATIVES=file:src/main/resources/fhir/customnarratives-local.properties)
	@ConfigProperty(name = "vacme.fhir.path.customnarratives", defaultValue = "classpath:/fhir/customnarratives.properties")
	String customNarrativePropertyFile;

	FhirResourceMapper fhirResourceMapper = new FhirResourceMapper();

	public byte[] createFhirImpfdokumentationXML(ImpfinformationDto impfinformationDto) {

		//FhirContext is an expensive object, create once and reuse
		FhirContext ctxR4 = FhirContext.forR4();
		Bundle bundle = fhirResourceMapper.getBundleWithAttributes();

		mapFhirResource(bundle, impfinformationDto);
		applyCustomNarrativeGenerator(ctxR4);

		return encodeResourceAsByteArray(ctxR4, bundle);
	}

	private void mapFhirResource(Bundle bundle, ImpfinformationDto impfinformationDto) {

		Composition compositionResource;
		Patient patientResource;
		List<Practitioner> practitionerResources = new ArrayList<>();
		List<Organization> organizationResources = new ArrayList<>();
		List<PractitionerRole> practitionerRoleResources = new ArrayList<>();
		List<Immunization> immunizationResources = new ArrayList<>();
		Condition conditionPregnancyResource = null;

		//Create Patient
		Registrierung registrierung = impfinformationDto.getRegistrierung();
		patientResource = fhirResourceMapper.getPatientResource(registrierung);

		List<Impfung> impfungen = getAllImpfungen(impfinformationDto);
		// FHIR Impfdokumentation wird nur erstellt, wenn mind. 1 VacMe-Impfung existiert
		if (impfungen.isEmpty()) {
			throw AppValidationMessage.NO_VACME_IMPFUNG.create();
		}

		for (Impfung i : impfungen) {
			//Get Organization
			OrtDerImpfung ortDerImpfung = i.getTermin().getImpfslot().getOrtDerImpfung();
			UUID odiUUID = ortDerImpfung.getId();
			Organization organizationResource = getExistingOrganizationOrNull(organizationResources, odiUUID);

			//Get Practitioner
			Benutzer benutzerFachverBab = i.getBenutzerVerantwortlicher();
			UUID fachBabUUID = benutzerFachverBab.getId();

			Practitioner practitionerResource = getExistingPractitionerOrNull(practitionerResources, fachBabUUID);

			//Get PractitionerRole - ist eine Kombination aus Organisation und Practitioner
			PractitionerRole practitionerRoleResource = getExistingPractitionerRoleOrNull(practitionerRoleResources, odiUUID, fachBabUUID);

			//Organization N - 1 Practitioner
			if (organizationResource == null) {
				//Create Organization
				organizationResource = fhirResourceMapper.getOrganizationResource(ortDerImpfung);
				organizationResources.add(organizationResource);

				//Practitioner kann in mehreren Organizations vorkommen
				if(practitionerResource == null){
					//Create Practitioner
					practitionerResource = fhirResourceMapper.getPractitionerResource(benutzerFachverBab);
					practitionerResources.add(practitionerResource);
				}

				//New Organization = create new PractitionerRole
				practitionerRoleResource = fhirResourceMapper.getPractitionerRoleResource(practitionerResource, organizationResource);
				practitionerRoleResources.add(practitionerRoleResource);
			}

			//Create Immunization
			Immunization immunizationResource = fhirResourceMapper.getImmunizationResource(i, patientResource, practitionerRoleResource);
			immunizationResources.add(immunizationResource);

			if(conditionPregnancyResource == null && Boolean.TRUE.equals(i.getSchwanger())) {
				//Create Pregnancy-Condition
				conditionPregnancyResource = fhirResourceMapper
					.getConditionPregnancyResource(DateUtil.getDate(i.getTimestampErstellt()), patientResource, practitionerRoleResource);
			}
		}

		//Create Composition - Nur ein Composition-Resource pro Bundle darf existieren
		compositionResource = fhirResourceMapper.getCompositionResource(patientResource, organizationResources, practitionerRoleResources, immunizationResources);

		//Add Resource in Bundle - Fixed order of resources in Bundle
		addResourceEntryInBundle(bundle, compositionResource);
		addResourceEntryInBundle(bundle, patientResource);
		practitionerResources.forEach(p -> addResourceEntryInBundle(bundle, p));
		organizationResources.forEach(o -> addResourceEntryInBundle(bundle, o));
		practitionerRoleResources.forEach(pr -> addResourceEntryInBundle(bundle, pr));
		immunizationResources.forEach(i -> addResourceEntryInBundle(bundle, i));
		addResourceEntryInBundle(bundle, conditionPregnancyResource);

		if (impfinformationDto.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().getPositivGetestetDatum() != null){
			//Create CovidIllness - Erkrankung als Grundimmunisierung
			Condition conditionCovidIllnessTestResource = fhirResourceMapper.getConditionCovidIllnessResource(
				impfinformationDto.getImpfdossier(), patientResource);
			addResourceEntryInBundle(bundle, conditionCovidIllnessTestResource);
		}
	}

	@NotNull
	private List<Impfung> getAllImpfungen(ImpfinformationDto impfinformationDto) {
		//Alle vorhanden Impfungen in eine Liste
		List<Impfung> impfungen =ImpfinformationenUtil.getImpfungenOrderedByImpffolgeNr(impfinformationDto);

		//Externe Impfungen werden entfernt, da sie nicht abgebildet werden koennen
		//Es gibt nur Vaccine-Coding von Fhir für CH-Zugelassene Impfungen und externe mRNA/Vektor-Impfungen
		impfungen.removeIf(Impfung::isExtern);
		//Alle Novavax-Impfungen entfernen solange es noch keinen FIHR Code dafuer gibt
		impfungen.removeIf(x -> x.getImpfstoff().getId().equals(NOVAVAX_UUID));
		return impfungen;
	}

	@Nullable
	private PractitionerRole getExistingPractitionerRoleOrNull(@NonNull List<PractitionerRole> practitionerRoleResources, @NonNull UUID organizationUUID, @NonNull UUID practitionerUUID) {
		return practitionerRoleResources.stream()
			.filter(practitionerRole -> practitionerRole.getOrganization().getResource().getIdElement().getValueAsString().equals(organizationUUID.toString()) &&
				practitionerRole.getPractitioner().getResource().getIdElement().getValueAsString().equals(practitionerUUID.toString()))
			.findAny()
			.orElse(null);
	}

	@Nullable
	private Practitioner getExistingPractitionerOrNull(List<Practitioner> practitionerResources, UUID fachBabUUID) {
		return practitionerResources.stream()
			.filter(practitioner -> fachBabUUID.toString().equals(practitioner.getId()))
			.findAny()
			.orElse(null);
	}

	@Nullable
	private Organization getExistingOrganizationOrNull(List<Organization> organizationResources, UUID odiUUID) {
		return organizationResources.stream()
			.filter(organization -> odiUUID.toString().equals(organization.getId()))
			.findAny()
			.orElse(null);
	}

	private void addResourceEntryInBundle(@NonNull Bundle bundle, @Nullable Resource resource) {
		if (resource == null) {
			return;
		}
		bundle.addEntry()
			.setFullUrl(URN_UUID + resource.getIdElement().getValue())
			.setResource(resource);
	}

	private void applyCustomNarrativeGenerator(FhirContext ctxR4) {
		//NarrativeTemplateManifest.loadResource - must start with classpath: or file:
		CustomThymeleafNarrativeGenerator gen = new CustomThymeleafNarrativeGenerator(customNarrativePropertyFile);

		ctxR4.setNarrativeGenerator(gen);
	}

	private byte[] encodeResourceAsByteArray(FhirContext ctxR4, Bundle bundle){

		try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
			OutputStreamWriter out = new OutputStreamWriter(bos, StandardCharsets.UTF_8))
		{
			ctxR4.newXmlParser().setPrettyPrint(true).encodeResourceToWriter(bundle,out);
			return bos.toByteArray();

		} catch (IOException e) {
			LOG.error("Could not create FHIR Impfungen Report ", e);
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not generate report", e);
		}
	}
}
