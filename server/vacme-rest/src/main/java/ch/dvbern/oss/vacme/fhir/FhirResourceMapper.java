package ch.dvbern.oss.vacme.fhir;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.embeddables.Adresse;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Geschlecht;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.fhir.coding.RouteCodingFhir;
import ch.dvbern.oss.vacme.fhir.coding.VaccineCodingFhir;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Composition.DocumentConfidentiality;
import org.hl7.fhir.r4.model.Composition.SectionComponent;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Immunization.ImmunizationProtocolAppliedComponent;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;

import static ch.dvbern.oss.vacme.fhir.coding.ConditionCodingFhir.CONDITION_PREGNANCY;
import static ch.dvbern.oss.vacme.fhir.coding.ConditionCodingFhir.DISEASE_COVID;
import static ch.dvbern.oss.vacme.fhir.coding.DocumentCodingFhir.ADMINISTRATION_IMMUNIZATION;
import static ch.dvbern.oss.vacme.fhir.coding.DocumentCodingFhir.CONFIDENTIALITY_NORMAL;
import static ch.dvbern.oss.vacme.fhir.coding.DocumentCodingFhir.RECORD_IMMUNIZATION;


public class FhirResourceMapper {

	static final String URN_UUID = "urn:uuid:";
	private static final String STRUCTURE_PROFILE_DOCUMENT = "http://fhir.ch/ig/ch-vacd/StructureDefinition/ch-vacd-document-immunization-administration";
	private static final String STRUCTURE_PROFILE_COMPOSITION = "http://fhir.ch/ig/ch-vacd/StructureDefinition/ch-vacd-composition-immunization-administration";
	public static final String STRUCTURE_PROFILE_CONFIDENTIALITYCODE = "http://fhir.ch/ig/ch-core/StructureDefinition/ch-ext-epr-confidentialitycode";
	private static final String STRUCTURE_PROFILE_PATIENT = "http://fhir.ch/ig/ch-core/StructureDefinition/ch-core-patient-epr";
	private static final String STRUCTURE_PROFILE_PRACTITIONER = "http://fhir.ch/ig/ch-core/StructureDefinition/ch-core-practitioner-epr";
	private static final String STRUCTURE_PROFILE_ORGANIZATION = "http://fhir.ch/ig/ch-core/StructureDefinition/ch-core-organization-epr";
	private static final String STRUCTURE_PROFILE_PRACTITIONERROLE = "http://fhir.ch/ig/ch-core/StructureDefinition/ch-core-practitionerrole-epr";
	private static final String STRUCTURE_PROFILE_IMMUNIZATION = "http://fhir.ch/ig/ch-vacd/StructureDefinition/ch-vacd-immunization";
	private static final String STRUCTURE_PROFILE_MEDICALPROBLEMS = "http://fhir.ch/ig/ch-vacd/StructureDefinition/ch-vacd-medical-problems";
	private static final String STRUCTURE_PROFILE_PASTILLNESSES = "http://fhir.ch/ig/ch-vacd/StructureDefinition/ch-vacd-pastillnesses";
	public static final String URN_IETF_RFC_3986 = "urn:ietf:rfc:3986";
	public static final String TITLE_IMMUNIZATION_ADMINISTRATION = "Immunization Administration";
	public static final String URN_OID_KRANKENKASSEKARTENNR = "urn:oid:2.16.756.5.30.1.123.100.1.1.1";
	public static final String URN_OID_GLN = "urn:oid:2.51.1.3";


	Bundle getBundleWithAttributes() {
		Bundle bundle = new Bundle();

		String valueUUID = UUID.randomUUID().toString();
		bundle.setId(valueUUID);

		Date currDate = new Date();
		bundle.getMeta()
			.addProfile(STRUCTURE_PROFILE_DOCUMENT)
			.setLastUpdated(currDate);

		bundle.getIdentifier()
			.setSystem(URN_IETF_RFC_3986)
			.setValue(URN_UUID + valueUUID);

		bundle.setTimestamp(currDate);
		bundle.setType(Bundle.BundleType.DOCUMENT);

		return bundle;
	}

	Composition getCompositionResource(Patient patient, List<Organization> organizations, List<PractitionerRole> practitionerRoles, List<Immunization> immunizations) {
		String sectionId = "administration";
		String lang = "en-US";
		String sectionNarrativText = "<div><p>This is the section containing all immunization entries.</p></div>";

		Composition composition = new Composition();

		String valueUUID = UUID.randomUUID().toString();
		composition.setId(valueUUID);
		composition.getMeta().addProfile(FhirResourceMapper.STRUCTURE_PROFILE_COMPOSITION);

		//TODO: für jetzt fix "en-US"
		composition.setLanguage(lang);

		Identifier identifier = composition.getIdentifier();
		identifier.setSystem(URN_IETF_RFC_3986);
		identifier.setValue(URN_UUID + valueUUID);

		composition.setStatus(Composition.CompositionStatus.FINAL);

		CodeableConcept type = composition.getType();
		type.addCoding()
			.setSystem(RECORD_IMMUNIZATION.getSystem())
			.setCode(RECORD_IMMUNIZATION.getCode())
			.setDisplay(RECORD_IMMUNIZATION.getDisplay());

		composition.getSubject().setResource(patient);
		composition.setDate(new java.util.Date());

		List<Reference> references = new ArrayList<>();
		practitionerRoles.forEach(pr -> {
			Reference reference = new Reference();
			reference.setResource(pr);
			references.add(reference);
		});
		composition.setAuthor(references);
		composition.setTitle(TITLE_IMMUNIZATION_ADMINISTRATION);

		composition.setConfidentiality(DocumentConfidentiality.N);
		Extension extension = composition.getConfidentialityElement().addExtension();
		extension.setUrl(STRUCTURE_PROFILE_CONFIDENTIALITYCODE);

		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding()
			.setSystem(CONFIDENTIALITY_NORMAL.getSystem())
			.setCode(CONFIDENTIALITY_NORMAL.getCode())
			.setDisplay(CONFIDENTIALITY_NORMAL.getDisplay());
		extension.setValue(codeableConcept);

		organizations.forEach(o -> composition.getCustodian().setResource(o));

		SectionComponent sectionComponent = new SectionComponent();
		sectionComponent.setId(sectionId);
		sectionComponent.setTitle(TITLE_IMMUNIZATION_ADMINISTRATION);
		sectionComponent.getCode().addCoding()
			.setSystem(ADMINISTRATION_IMMUNIZATION.getSystem())
			.setCode(ADMINISTRATION_IMMUNIZATION.getCode())
			.setDisplay(ADMINISTRATION_IMMUNIZATION.getDisplay());
		sectionComponent.getText()
			.setStatus(org.hl7.fhir.r4.model.Narrative.NarrativeStatus.GENERATED)
			.setDivAsString(sectionNarrativText);
		immunizations.forEach(i -> sectionComponent.addEntry().setResource(i));
		composition.addSection(sectionComponent);

		return composition;
	}

	Patient getPatientResource(Registrierung registrierung) {
		Patient patient = new Patient();

		patient.setId(registrierung.getId().toString());
		patient.getMeta().addProfile(STRUCTURE_PROFILE_PATIENT);

		//UUID from Registrierung-Entry
		Identifier idFirst = patient.addIdentifier();
		idFirst.setSystem(URN_UUID + registrierung.getId());
		idFirst.setValue(registrierung.getId().toString());

		//KK-Nr from Registrierung (KK-Nr alleine ist nicht unique (ZH migration, EDA))
		Identifier idSecond = patient.addIdentifier();
		idSecond.setSystem(URN_OID_KRANKENKASSEKARTENNR);
		idSecond.setValue(registrierung.getKrankenkasseKartenNr());

		HumanName name = patient.addName();
		name.setFamily(registrierung.getName());
		name.addGiven(registrierung.getVorname());

		ContactPoint telecom = patient.addTelecom();
		telecom.setSystem(ContactPoint.ContactPointSystem.PHONE);
		telecom.setValue(registrierung.getTelefon());

		patient.setGender(getAdministrativeGender(registrierung.getGeschlecht()));

		patient.setBirthDateElement(new DateType(DateUtil.getDate(registrierung.getGeburtsdatum())));

		Address address = patient.addAddress();
		Adresse regAdresse = registrierung.getAdresse();
		address.addLine(regAdresse.getAdresse1() + " " + regAdresse.getAdresse2());
		address.setCity(regAdresse.getOrt());
		address.setPostalCode(regAdresse.getPlz());

		return patient;
	}



	Practitioner getPractitionerResource(Benutzer fachverBab) {
		Practitioner practitioner = new Practitioner();

		practitioner.setId(fachverBab.getId().toString());
		practitioner.getMeta().addProfile(STRUCTURE_PROFILE_PRACTITIONER);

		//UUID from Benutzer-Entry
		practitioner.addIdentifier()
			.setSystem(URN_UUID + fachverBab.getId())
			.setValue(fachverBab.getId().toString());

		//GLN from FachverBAB (GLN alleine ist nicht unique)
		Identifier idSecond = practitioner.addIdentifier();
		idSecond.setSystem(URN_OID_GLN);
		idSecond.setValue(fachverBab.getGlnNummer());

		practitioner.setActive(!fachverBab.isDeaktiviert());

		HumanName name = practitioner.addName();
		name.setFamily(fachverBab.getName());
		name.addGiven(fachverBab.getVorname());

		ContactPoint telecomPhone = practitioner.addTelecom();
		telecomPhone.setSystem(ContactPoint.ContactPointSystem.PHONE);
		telecomPhone.setValue(fachverBab.getMobiltelefon());

		ContactPoint telecomMail = practitioner.addTelecom();
		telecomMail.setSystem(ContactPoint.ContactPointSystem.EMAIL);
		telecomMail.setValue(fachverBab.getEmail());

		return practitioner;
	}

	Organization getOrganizationResource(OrtDerImpfung ortDerImpfung) {
		String ch = "CH";

		Organization organization = new Organization();

		organization.setId(ortDerImpfung.getId().toString());
		organization.getMeta().addProfile(STRUCTURE_PROFILE_ORGANIZATION);

		//UUID from OrtDerImpfung-Entry
		Identifier idFirst = organization.addIdentifier();
		idFirst.setSystem(URN_UUID + ortDerImpfung.getId());
		idFirst.setValue(ortDerImpfung.getId().toString());

		//GLN from OrtDerImpfung
		Identifier idSecond = organization.addIdentifier();
		idSecond.setSystem(URN_OID_GLN);
		idSecond.setValue(ortDerImpfung.getGlnNummer());

		organization.setName(ortDerImpfung.getName());

		Address address = organization.addAddress();
		Adresse odiAdresse = ortDerImpfung.getAdresse();
		address.addLine(odiAdresse.getAdresse1() + " " + odiAdresse.getAdresse2());
		address.setCity(odiAdresse.getOrt());
		address.setPostalCode(odiAdresse.getPlz());
		address.setCountry(ch); //fix CH

		return organization;
	}

	PractitionerRole getPractitionerRoleResource(Practitioner practitioner, Organization organization) {
		PractitionerRole practitionerRole = new PractitionerRole();

		practitionerRole.setId(UUID.randomUUID().toString());
		practitionerRole.getMeta().addProfile(STRUCTURE_PROFILE_PRACTITIONERROLE);

		//TODO: isUserInGroup ist vorübergehen = practitioner.active
		//final Optional<UserRepresentation> userOptional = keyCloakService.findUserByUsernameAndEmail(userJax.getUsername(), userJax.getEmail());
		//boolean isUserInGroup = keyCloakService.isUserInGroup(foundUser, groupName);
		boolean isUserInGroup = practitioner.getActive();

		practitionerRole.setActive(isUserInGroup);

		practitionerRole.getPractitioner().setResource(practitioner);
		practitionerRole.getOrganization().setResource(organization);

		return practitionerRole;
	}

	Immunization getImmunizationResource(Impfung impfung, Patient patient, @Nullable PractitionerRole practitionerRole) {
		Immunization immunization = new Immunization();

		immunization.setId(impfung.getId().toString());
		immunization.getMeta().addProfile(STRUCTURE_PROFILE_IMMUNIZATION);

		//UUID from Impfung-Entry
		Identifier idFirst = immunization.addIdentifier();
		idFirst.setSystem(URN_UUID + impfung.getId());
		idFirst.setValue(impfung.getId().toString());

		//Always completed (Impfung-Eintrag existiert nicht, wenn nicht complete)
		immunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);

		Coding coding = immunization.getVaccineCode().addCoding();
		VaccineCodingFhir vaccineCoding = VaccineCodingFhir.getVaccineCoding(impfung.getImpfstoff().getId().toString());
		coding.setSystem(vaccineCoding.getSystem())
			.setCode(vaccineCoding.getCode())
			.setDisplay(vaccineCoding.getDisplay());

		immunization.getPatient().setResource(patient);

		immunization.getOccurrenceDateTimeType().setValue(DateUtil.getDate(impfung.getTimestampImpfung()));
		immunization.setRecorded(DateUtil.getDate(impfung.getTimestampErstellt()));
		immunization.setLotNumber(impfung.getLot());

		Coding site = immunization.getSite().addCoding();
		RouteCodingFhir siteCoding = RouteCodingFhir.getSiteCoding(impfung.getVerarbreichungsseite(), impfung.getVerarbreichungsort());
		if(siteCoding != null) {
			site.setSystem(siteCoding.getSystem())
				.setCode(siteCoding.getCode())
				.setDisplay(siteCoding.getDisplay());
		}

		Coding route = immunization.getRoute().addCoding();
		RouteCodingFhir routeCoding = RouteCodingFhir.getRouteCoding(impfung.getVerarbreichungsart());
		if(routeCoding != null) {
			route.setSystem(routeCoding.getSystem())
				.setCode(routeCoding.getCode())
				.setDisplay(routeCoding.getDisplay());
		}

		immunization.getDoseQuantity().setUnit("ml");
		immunization.getDoseQuantity().setValue(impfung.getMenge());

		immunization.addPerformer().getActor().setResource(practitionerRole);

		ImmunizationProtocolAppliedComponent protocol = immunization.addProtocolApplied();
		protocol.addTargetDisease().addCoding()
			.setSystem(DISEASE_COVID.getSystem())
			.setCode(DISEASE_COVID.getCode())
			.setDisplay(DISEASE_COVID.getDisplay());

		protocol.getDoseNumberPositiveIntType().setValue(1);

		return immunization;
	}

	Condition getConditionPregnancyResource(Date recordedTimestamp, Patient patient, @Nullable PractitionerRole practitionerRole) {
		Condition conditionPregnancy = new Condition();

		conditionPregnancy.setId(UUID.randomUUID().toString());
		conditionPregnancy.getMeta().addProfile(STRUCTURE_PROFILE_MEDICALPROBLEMS);

		Coding pregnancyCode = conditionPregnancy.getCode().addCoding();
		pregnancyCode.setSystem(CONDITION_PREGNANCY.getSystem())
			.setCode(CONDITION_PREGNANCY.getCode())
			.setDisplay(CONDITION_PREGNANCY.getDisplay());

		conditionPregnancy.getSubject().setResource(patient);
		conditionPregnancy.setRecordedDate(recordedTimestamp);

		conditionPregnancy.getRecorder().setResource(practitionerRole);

		return conditionPregnancy;
	}

	Condition getConditionCovidIllnessResource(@NonNull Impfdossier impfdossier, Patient patient) {
		Validate.notNull(impfdossier.getZweiteGrundimmunisierungVerzichtet().getPositivGetestetDatum(), "Positiv Getestet Datum is required");
		Validate.notNull(impfdossier.getZweiteGrundimmunisierungVerzichtet().getZweiteImpfungVerzichtetZeit(), "Zweite Impfung is required");
		return getConditionCovidIllnessResource(DateUtil.getDate(impfdossier.getZweiteGrundimmunisierungVerzichtet().getPositivGetestetDatum()),
			DateUtil.getDate(impfdossier.getZweiteGrundimmunisierungVerzichtet().getZweiteImpfungVerzichtetZeit()),patient);
	}

	private Condition getConditionCovidIllnessResource(Date onSetDate, Date recordedDate, Patient patient) {
		Condition conditionIllness = new Condition();

		conditionIllness.setId(UUID.randomUUID().toString());
		conditionIllness.getMeta().addProfile(STRUCTURE_PROFILE_PASTILLNESSES);

		Coding illnessCode = conditionIllness.getCode().addCoding();
		illnessCode
			.setSystem(DISEASE_COVID.getSystem())
			.setCode(DISEASE_COVID.getCode())
			.setDisplay(DISEASE_COVID.getDisplay());

		conditionIllness.getSubject().setResource(patient);

		conditionIllness.getOnsetDateTimeType().setValue(onSetDate);
		conditionIllness.setRecordedDate(recordedDate);

		return conditionIllness;
	}

	private AdministrativeGender getAdministrativeGender(Geschlecht geschlecht) {
		switch (geschlecht){
		case WEIBLICH:
			return AdministrativeGender.FEMALE;
		case MAENNLICH:
			return AdministrativeGender.MALE;
		case ANDERE:
			return AdministrativeGender.OTHER;
		default:
			return AdministrativeGender.UNKNOWN;
		}
	}

}
