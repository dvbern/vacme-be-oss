package ch.dvbern.oss.vacme.fhir.coding;

import javax.annotation.Nonnull;

import ch.dvbern.oss.vacme.shared.util.Constants;

/**
 * Enum Codes that define the constants used to specify the vaccine for the FHIR export
 */
public enum VaccineCodingFhir {

	VACCINE_SPIKEVAX(
		"http://fhir.ch/ig/ch-vacd/CodeSystem/ch-vacd-swissmedic-cs",
		"68267",
		"Spikevax (COVID-19 Vaccine, Moderna)"
	),
	VACCINE_COMIRNATY(
		"http://fhir.ch/ig/ch-vacd/CodeSystem/ch-vacd-swissmedic-cs",
		"68225",
		"Comirnaty (COVID-19 Vaccine, Pfizer)"
	),
	VACCINE_JANSSEN(
		"http://fhir.ch/ig/ch-vacd/CodeSystem/ch-vacd-swissmedic-cs",
		"68235",
		"COVID-19 Vaccine Janssen"
	),
	VACCINE_EXTERN_MRNA(
		"http://snomed.info/sct",
		"1119349007",
		"mRNA-Impfstoff gegen COVID-19"
	),
	VACCINE_EXTERN_VEKTOR(
		"http://snomed.info/sct",
		"29061000087103",
		"Nicht replizierender viraler Vektorimpfstoff gegen COVID-19"
	),
	VACCINE_EXTERN_UNKNOWN(
		"UNKNOWN_SYSTEM",
		"UNKNOWN_CODE",
		"UNKNOWN_VACCINE"
	);


	@Nonnull
	private final String system;
	@Nonnull
	private final String code;
	@Nonnull
	private final String display;

	VaccineCodingFhir(@Nonnull String system, @Nonnull String code, @Nonnull String display) {
		this.system = system;
		this.code = code;
		this.display = display;
	}

	@Nonnull
	public String getSystem() {
		return system;
	}

	@Nonnull
	public String getCode() {
		return code;
	}

	@Nonnull
	public String getDisplay() {
		return display;
	}


	public static VaccineCodingFhir getVaccineCoding(String uuidVaccine) {

		switch(uuidVaccine) {
		case Constants.MODERNA_ID_STRING:
		case Constants.MODERNA_BIVALENT_ID_STRING:
			return VACCINE_SPIKEVAX;
		case Constants.PFIZER_BIONTECH_ID_STRING:
		case Constants.PFIZER_BIONTECH_BIVALENT_ID_STRING:
		case Constants.PFIZER_BIONTECH_KINDER_ID_STRING:
			return VACCINE_COMIRNATY;
		case Constants.JANSSEN_ID_STRING:
			return VACCINE_JANSSEN;
		default:
			//Unknown Vaccine, darf hier nicht rein!
			//VaccineCoding ist ein Required-Feld in FHIR document
			return VACCINE_EXTERN_UNKNOWN;
		}
	}

}


