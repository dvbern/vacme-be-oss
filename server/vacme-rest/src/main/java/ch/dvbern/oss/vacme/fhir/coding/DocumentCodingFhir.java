package ch.dvbern.oss.vacme.fhir.coding;

import javax.annotation.Nonnull;

/**
 * Enum Codes that define constants used for the record of the FHIR export
 */
public enum DocumentCodingFhir {

	RECORD_IMMUNIZATION(
		"http://snomed.info/sct",
		"41000179103",
		"Immunization record"
	),
	ADMINISTRATION_IMMUNIZATION(
		"http://loinc.org",
		"11369-6",
		"Hx of Immunization"
	),
	CONFIDENTIALITY_NORMAL(
		"http://snomed.info/sct",
		"17621005",
		"Normal (qualifier value)"
	);


	@Nonnull
	private final String system;
	@Nonnull
	private final String code;
	@Nonnull
	private final String display;

	DocumentCodingFhir(@Nonnull String system, @Nonnull String code, @Nonnull String display) {
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

}


