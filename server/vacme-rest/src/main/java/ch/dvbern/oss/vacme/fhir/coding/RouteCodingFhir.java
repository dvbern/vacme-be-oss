package ch.dvbern.oss.vacme.fhir.coding;

import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsart;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsort;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsseite;

import javax.annotation.Nonnull;

import static ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsseite.LINKS;

/**
 *
 * Enum Codes that define the Route and Site of the Covid vaccination for the FHIR export
 */
public enum RouteCodingFhir {

	ROUTE_INTRA_MUSKULAER(
		"http://standardterms.edqm.eu",
		"20035000",
		"Intramuscular use"
	),
	ROUTE_SUBKUTAN(
		"http://standardterms.edqm.eu",
		"20066000",
		"Subcutaneous use"
	),
	ROUTE_INTRADERMAL(
		"http://standardterms.edqm.eu",
		"20030000",
		"Intradermal use"
	),
	SITE_LEFT_ARM(
		"http://terminology.hl7.org/CodeSystem/v3-ActSite",
		"LA",
		"left arm"
	),
	SITE_RIGHT_ARM(
		"http://terminology.hl7.org/CodeSystem/v3-ActSite",
		"RA",
		"right arm"
	),
	SITE_LEFT_THIGH(
		"http://terminology.hl7.org/CodeSystem/v3-ActSite",
		"LT",
		"left thigh"
	),
	SITE_RIGHT_THIGH(
		"http://terminology.hl7.org/CodeSystem/v3-ActSite",
		"RT",
		"right thigh"
	);


	@Nonnull
	private final String system;
	@Nonnull
	private final String code;
	@Nonnull
	private final String display;

	RouteCodingFhir(@Nonnull String system, @Nonnull String code, @Nonnull String display) {
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


	public static RouteCodingFhir getRouteCoding(Verarbreichungsart route) {

		switch(route) {
		case INTRA_MUSKULAER:
			return ROUTE_INTRA_MUSKULAER;
		case SUBKUTAN:
			return ROUTE_SUBKUTAN;
		case INTRADERMAL:
			return ROUTE_INTRADERMAL;
		default:
			//TODO: Gibt es eine bessere Loesung?
			return null;
		}
	}

	public static RouteCodingFhir getSiteCoding(Verarbreichungsseite seite, Verarbreichungsort ort) {

		switch(ort) {
		case OBERARM:
		case UNTERARM:
			return LINKS.equals(seite) ?  SITE_LEFT_ARM : SITE_RIGHT_ARM;
		case OBERSCHENKEL:
			return LINKS.equals(seite) ?  SITE_LEFT_THIGH : SITE_RIGHT_THIGH;
		default:
			//TODO: Gibt es eine bessere Loesung?
			return null;
		}
	}

}


