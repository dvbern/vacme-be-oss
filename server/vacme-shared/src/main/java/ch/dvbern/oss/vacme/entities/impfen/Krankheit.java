package ch.dvbern.oss.vacme.entities.impfen;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.HasKrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(name = "UC_Krankheit_identifier", columnNames = "identifier"))
public class Krankheit extends AbstractUUIDEntity<Krankheit> implements HasKrankheitIdentifier {
	private static final long serialVersionUID = 7022703154306228173L;

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private KrankheitIdentifier identifier;

	@NotNull
	@Column(nullable = false)
	private boolean noFreieTermine = false;

	/**
	 * This is the value we set for NEW Impfungen of a given Krankheit
	 */
	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private KantonaleBerechtigung kantonaleBerechtigung;

	@NotNull
	@Column(nullable = false)
	private boolean hasAtleastOneImpfungViewableByKanton = false;

	@Override
	public KrankheitIdentifier getKrankheitIdentifier() {
		return identifier;
	}
}
