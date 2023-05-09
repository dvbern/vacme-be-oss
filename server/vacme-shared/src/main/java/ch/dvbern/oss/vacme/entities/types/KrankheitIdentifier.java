package ch.dvbern.oss.vacme.entities.types;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.dvbern.oss.vacme.entities.base.HasKrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsart;
import ch.dvbern.oss.vacme.shared.visitor.KrankheitVisitor;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.vacme.shared.util.Constants.MIN_DATE_FOR_IMPFUNGEN_AFFENPOCKEN;
import static ch.dvbern.oss.vacme.shared.util.Constants.MIN_DATE_FOR_IMPFUNGEN_COVID;
import static ch.dvbern.oss.vacme.shared.util.Constants.MIN_DATE_FOR_IMPFUNGEN_FSME;

public enum KrankheitIdentifier implements HasKrankheitIdentifier {
	COVID(
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		12,
		true,
		true,
		true,
		false,
		true,
		List.of(Verarbreichungsart.values()),
		MIN_DATE_FOR_IMPFUNGEN_COVID,
		false
	) {
		@Override
		public KrankheitIdentifier getKrankheitIdentifier() {
			return this;
		}

		@Override
		public <T> T accept(KrankheitVisitor<T> visitor) {
			return visitor.visitCovid();
		}
	},
	AFFENPOCKEN(
		false,
		true,
		false,
		true,
		true,
		true,
		false,
		false,
		false,
		false,
		18,
		false,
		false,
		false,
		true,
		false,
		List.of(Verarbreichungsart.SUBKUTAN, Verarbreichungsart.INTRADERMAL),
		MIN_DATE_FOR_IMPFUNGEN_AFFENPOCKEN,
		false
	) {
		@Override
		public KrankheitIdentifier getKrankheitIdentifier() {
			return this;
		}

		@Override
		public <T> T accept(KrankheitVisitor<T> visitor) {
			return visitor.visitAffenpocken();
		}
	},
	FSME(
		false,
		true,
		false,
		true,
		true,
		false,
		false,
		false,
		false,
		false,
		18,
		false,
		false,
		false,
		true,
		false,
		List.of(Verarbreichungsart.values()),
		MIN_DATE_FOR_IMPFUNGEN_FSME,
		true
	) {
		@Override
		public KrankheitIdentifier getKrankheitIdentifier() {
			return this;
		}

		@Override
		public <T> T accept(KrankheitVisitor<T> visitor) {
			return visitor.visitFsme();
		}
	};

	private final boolean supportsZertifikat;
	private final boolean supportsExternesZertifikat;
	private final boolean supportsZweiteImpfungVerzichten;
	private final boolean supportsDossierFileUpload;
	private final boolean supportsTerminbuchung;
	private final boolean supportsVMDL;
	private final boolean supportsImpffolgenEinsUndZwei;
	private final boolean supportsMobileImpfteams;
	private final boolean supportsContactTracing;
	private final boolean supportsTagesStatistik;
	private final Integer warnMinAge;
	private final boolean supportsErkrankungen;
	private final boolean supportsCallcenter;
	private final boolean supportsTerminbuchungBeiNichtAufgefuehrtemOdi;

	private final boolean supportsFreigabeSMS;
	private final boolean supportsBenachrichtigungenPerBrief;
	private List<Verarbreichungsart> supportedVerabreichungsarten  = Collections.emptyList();

	private final LocalDateTime mindateForImpfungen;
	private final boolean wellEnabled;

	KrankheitIdentifier(
		boolean supportsZertifikat,
		boolean supportsExternesZertifikat,
		boolean supportsZweiteImpfungVerzichten,
		boolean supportsDossierFileUpload,
		boolean supportsTerminbuchung,
		boolean supportsVMDL,
		boolean supportsImpffolgenEinsUndZwei,
		boolean supportsMobileImpfteams,
		boolean supportsContactTracing,
		boolean supportsTagesStatistik,
		Integer warnMinAge,
		boolean supportsErkrankungen,
		boolean supportsCallcenter,
		boolean supportsTerminbuchungBeiNichtAufgefuehrtemOdi,
		boolean supportsFreigabeSMS,
		boolean supportsBenachrichtigungenPerBrief,
		@NonNull List<Verarbreichungsart> supportedVerabreichungsarten,
		@NonNull LocalDateTime mindateForImpfungen,
		boolean wellEnabled
	) {
		this.supportsZertifikat = supportsZertifikat;
		this.supportsExternesZertifikat = supportsExternesZertifikat;
		this.supportsZweiteImpfungVerzichten = supportsZweiteImpfungVerzichten;
		this.supportsDossierFileUpload = supportsDossierFileUpload;
		this.supportsTerminbuchung = supportsTerminbuchung;
		this.supportsVMDL = supportsVMDL;
		this.supportsImpffolgenEinsUndZwei = supportsImpffolgenEinsUndZwei;
		this.supportsMobileImpfteams = supportsMobileImpfteams;
		this.supportsContactTracing = supportsContactTracing;
		this.supportsTagesStatistik = supportsTagesStatistik;
		this.warnMinAge = warnMinAge;
		this.supportsErkrankungen = supportsErkrankungen;
		this.supportsCallcenter = supportsCallcenter;
		this.supportsTerminbuchungBeiNichtAufgefuehrtemOdi = supportsTerminbuchungBeiNichtAufgefuehrtemOdi;
		this.supportsFreigabeSMS = supportsFreigabeSMS;
		this.supportsBenachrichtigungenPerBrief = supportsBenachrichtigungenPerBrief;
		this.supportedVerabreichungsarten = supportedVerabreichungsarten;
		this.mindateForImpfungen = mindateForImpfungen;
		this.wellEnabled = wellEnabled;
	}

	public abstract <T> T accept(KrankheitVisitor<T> visitor);

	public static List<KrankheitIdentifier> getVMDLSupportedKrankheiten() {
		return Stream.of(KrankheitIdentifier.values()).filter(KrankheitIdentifier::isSupportsVMDL).collect(Collectors.toList());
	}

	public static List<KrankheitIdentifier> getTagesStatistikSupportedKrankheiten() {
		return Stream.of(KrankheitIdentifier.values()).filter(KrankheitIdentifier::isSupportsTagesStatistik).collect(Collectors.toList());
	}

	public boolean isSupportsZertifikat() {
		return supportsZertifikat;
	}

	public boolean isSupportsExternesZertifikat() {
		return supportsExternesZertifikat;
	}

	public boolean isSupportsZweiteImpfungVerzichten() {
		return supportsZweiteImpfungVerzichten;
	}

	public boolean isSupportsDossierFileUpload() {
		return supportsDossierFileUpload;
	}

	public boolean isSupportsTerminbuchung() {
		return supportsTerminbuchung;
	}

	public boolean isSupportsVMDL() {
		return supportsVMDL;
	}

	public boolean isSupportsImpffolgenEinsUndZwei() {
		return supportsImpffolgenEinsUndZwei;
	}

	public boolean isSupportsNImpfungenWithoutGrundimmunisierung() {
		return !supportsImpffolgenEinsUndZwei;
	}

	public boolean isSupportsMobileImpfteams() {
		return supportsMobileImpfteams;
	}

	public boolean isSupportsContactTracing() {
		return supportsContactTracing;
	}

	public boolean isSupportsTagesStatistik() {
		return supportsTagesStatistik;
	}

	public Integer getWarnMinAge() {
		return warnMinAge;
	}

	public boolean isSupportsErkrankungen() {
		return supportsErkrankungen;
	}

	public boolean isSupportsCallcenter() {
		return supportsCallcenter;
	}

	public boolean isSupportsTerminbuchungBeiNichtAufgefuehrtemOdi() {
		return supportsTerminbuchungBeiNichtAufgefuehrtemOdi;
	}

	public boolean isSupportsFreigabeSMS() {
		return supportsFreigabeSMS;
	}

	public boolean isSupportsBenachrichtigungenPerBrief() {
		return supportsBenachrichtigungenPerBrief;
	}

	@NonNull
	public List<Verarbreichungsart> getSupportedVerabreichungsarten() {
		return supportedVerabreichungsarten;
	}

	@NonNull
	public LocalDateTime getMindateForImpfungen() {
		return mindateForImpfungen;
	}

	public boolean isWellEnabled() {
		return wellEnabled;
	}
}
