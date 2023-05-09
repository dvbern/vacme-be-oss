/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.entities.statistik;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.shared.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

@SqlResultSetMapping(
	name = Constants.REPORTING_KANTON_DTO_MAPPING,
	classes = @ConstructorResult(
		targetClass = StatistikReportingKantonDTO.class,
		columns = {
			@ColumnResult(name = "Registrierungs_ID", type = String.class),
			@ColumnResult(name = "Registriert_am", type = String.class),
			@ColumnResult(name = "RegistrierungsEingang", type = String.class),
			@ColumnResult(name = "Geschlecht", type = String.class),
			@ColumnResult(name = "Immobil", type = String.class),
			@ColumnResult(name = "Geburtsjahr", type = String.class),
			@ColumnResult(name = "PLZ", type = String.class),
			@ColumnResult(name = "Abgleich_elektronischer_Impfausweis", type = String.class),
			@ColumnResult(name = "Abgleich_Contact_tracing", type = String.class),
			@ColumnResult(name = "Vollstaendiger_Impfschutz", type = String.class),
			@ColumnResult(name = "Chronische_Krankheiten", type = String.class),
			@ColumnResult(name = "Lebensumstaende", type = String.class),
			@ColumnResult(name = "Beruf", type = String.class),
			@ColumnResult(name = "Imfgruppe", type = String.class),
			@ColumnResult(name = "Verstorben", type = String.class),
			@ColumnResult(name = "Immunisiert_bis", type = String.class),
			@ColumnResult(name = "Freigegeben_naechste_Impfung_ab", type = String.class),
			@ColumnResult(name = "erlaubte_impfstoffe_fuer_booster", type = String.class),
		}
	)
)

@SqlResultSetMapping(
	name = Constants.REPORTING_KANTONSARZT_DTO_MAPPING,
	classes = @ConstructorResult(
		targetClass = StatistikReportingKantonsarztDTO.class,
		columns = {
			@ColumnResult(name = "Registrierungs_ID", type = String.class),
			@ColumnResult(name = "Registriert_am", type = String.class),
			@ColumnResult(name = "RegistrierungsEingang", type = String.class),
			@ColumnResult(name = "Geschlecht", type = String.class),
			@ColumnResult(name = "Immobil", type = String.class),
			@ColumnResult(name = "Geburtsdatum", type = String.class),
			@ColumnResult(name = "Registrierungsnummer", type = String.class),
			@ColumnResult(name = "Name", type = String.class),
			@ColumnResult(name = "Vorname", type = String.class),
			@ColumnResult(name = "Adresse_1", type = String.class),
			@ColumnResult(name = "Adresse_2", type = String.class),
			@ColumnResult(name = "PLZ", type = String.class),
			@ColumnResult(name = "Ort", type = String.class),
			@ColumnResult(name = "Abgleich_elektronischer_Impfausweis", type = String.class),
			@ColumnResult(name = "Abgleich_Contact_tracing", type = String.class),
			@ColumnResult(name = "Vollstaendiger_Impfschutz", type = String.class),
			@ColumnResult(name = "Chronische_Krankheiten", type = String.class),
			@ColumnResult(name = "Lebensumstaende", type = String.class),
			@ColumnResult(name = "Beruf", type = String.class),
			@ColumnResult(name = "Imfgruppe", type = String.class),
			@ColumnResult(name = "Verstorben", type = String.class),
			@ColumnResult(name = "Immunisiert_bis", type = String.class),
			@ColumnResult(name = "Freigegeben_naechste_Impfung_ab", type = String.class),
			@ColumnResult(name = "erlaubte_impfstoffe_fuer_booster", type = String.class),
			@ColumnResult(name = "Genesen", type = String.class),
			@ColumnResult(name = "Datum_positiver_Test", type = String.class),
			@ColumnResult(name = "Selbstzahler", type = String.class),
		}
	)
)

@SqlResultSetMapping(
	name = Constants.REPORTING_IMPFUNGEN_DTO_MAPPING,
	classes = @ConstructorResult(
		targetClass = StatistikReportingImpfungenDTO.class,
		columns = {
			@ColumnResult(name = "Registrierungs_ID", type = String.class),
			@ColumnResult(name = "Ort_der_Impfung_ID", type = String.class),
			@ColumnResult(name = "Ort_der_Impfung_Name", type = String.class),
			@ColumnResult(name = "Ort_der_Impfung_GLN", type = String.class),
			@ColumnResult(name = "Ort_der_Impfung_Typ", type = String.class),
			@ColumnResult(name = "Termin_Impfung", type = String.class),
			@ColumnResult(name = "Impfung_am", type = String.class),
			@ColumnResult(name = "Impfstoff_Name", type = String.class),
			@ColumnResult(name = "Impfstoff_ID", type = String.class),
			@ColumnResult(name = "Impfung_extern", type = String.class),
			@ColumnResult(name = "Grundimmunisierung", type = String.class),
			@ColumnResult(name = "Impffolgenummer", type = String.class),
			@ColumnResult(name = "Impfung_selbstzahlende", type = String.class),
			@ColumnResult(name = "Immunsupprimiert", type = String.class),
			@ColumnResult(name = "Krankheit", type = String.class)
		}
	)
)

@SqlResultSetMapping(
	name = Constants.STATISTIK_TERMINSLOTS_DTO_MAPPING,
	classes = @ConstructorResult(
		targetClass = StatistikReportingTerminslotsDTO.class,
		columns = {
			@ColumnResult(name = "Ort_der_Impfung_Name", type = String.class),
			@ColumnResult(name = "Ort_der_Impfung_GLN", type = String.class),
			@ColumnResult(name = "Slot_Kapazitaet_Impfung_1", type = String.class),
			@ColumnResult(name = "Slot_Kapazitaet_Impfung_2", type = String.class),
			@ColumnResult(name = "Slot_Kapazitaet_Impfung_N", type = String.class),
			@ColumnResult(name = "Slot_Datum", type = String.class),
			@ColumnResult(name = "Slot_Von", type = String.class),
			@ColumnResult(name = "Slot_Bis", type = String.class),
		}
	)
)

@SqlResultSetMapping(
	name = Constants.STATISTIK_ODIS_DTO_MAPPING,
	classes = @ConstructorResult(
		targetClass = StatistikReportingOdisDTO.class,
		columns = {
			@ColumnResult(name = "Name", type = String.class),
			@ColumnResult(name = "GLN", type = String.class),
			@ColumnResult(name = "Adresse_1", type = String.class),
			@ColumnResult(name = "Adresse_2", type = String.class),
			@ColumnResult(name = "PLZ", type = String.class),
			@ColumnResult(name = "Ort", type = String.class),
			@ColumnResult(name = "Identifier", type = String.class),
			@ColumnResult(name = "Mobil", type = String.class),
			@ColumnResult(name = "Oeffentlich", type = String.class),
			@ColumnResult(name = "Terminverwaltung", type = String.class),
			@ColumnResult(name = "Typ", type = String.class),
			@ColumnResult(name = "ZSR", type = String.class),
			@ColumnResult(name = "Deaktiviert", type = String.class),
		}
	)
)

@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
@Table(name = "STAT_StatistikKennzahlEintrag")
public class StatistikKennzahlEintrag extends AbstractUUIDEntity<StatistikKennzahlEintrag> {

	private static final long serialVersionUID = 4519542592232541096L;

	@NotNull
	@NonNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, length = DBConst.DB_ENUM_LENGTH)
	private StatistikKennzahl statistikKennzahl;

	@NotNull
	@NonNull
	@Column(nullable = false, updatable = false)
	private Long wert;


	@NotNull @NonNull
	@Column(nullable = false)
	private LocalDateTime erfassungszeitpunkt;

	@NonNull
	public static ID<StatistikKennzahlEintrag> toId(@NonNull UUID id) {
		return new ID<>(id, StatistikKennzahlEintrag.class);
	}

	public static StatistikKennzahlEintrag create(StatistikKennzahl kennzahl, long value) {
		StatistikKennzahlEintrag statistikKennzahlEintrag = new StatistikKennzahlEintrag();
		statistikKennzahlEintrag.setWert(value);
		statistikKennzahlEintrag.setStatistikKennzahl(kennzahl);
		statistikKennzahlEintrag.setErfassungszeitpunkt(LocalDateTime.now());
		return statistikKennzahlEintrag;
	}
}
