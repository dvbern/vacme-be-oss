package ch.dvbern.oss.vacme.fhir.coding;

import javax.annotation.Nonnull;

/**
 *
 * Enum Codes that define the constants used to specify special conditions for the FHIR export
 */
public enum ConditionCodingFhir {

	DISEASE_COVID(
		"http://snomed.info/sct",
		"840539006",
		"COVID-19"
	),
	CONDITION_PREGNANCY(
		"urn:oid:2.16.756.5.30.1.127.3.3.1",
		"77386006",
		"SCHWANGERSCHAFT_UND_POST_PARTUM_PERIODE"
	);


	@Nonnull
	private final String system;
	@Nonnull
	private final String code;
	@Nonnull
	private final String display;

	ConditionCodingFhir(@Nonnull String system, @Nonnull String code, @Nonnull String display) {
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


