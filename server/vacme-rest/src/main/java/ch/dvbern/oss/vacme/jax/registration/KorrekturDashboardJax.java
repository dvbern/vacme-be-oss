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

package ch.dvbern.oss.vacme.jax.registration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungStatus;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.PlzMappingException;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ValidationUtil;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
public class KorrekturDashboardJax {
	private String vorname;
	private String name;
	private @Nullable UUID benutzerId;
	private String registrierungsnummer;
	private @Nullable String mail;
	private @Nullable String telefon;
	private @Nullable ExternGeimpftJax externGeimpft;
	private @Nullable ImpfungJax impfung1IfEditableForRole;
	private @Nullable ImpfungJax impfung2IfEditableForRole;
	private RegistrierungsEingang eingang;
	private List<ImpfdossiereintragJax> impfdossiereintraegeEditableForRole = new ArrayList<>();
	private RegistrierungStatus status;
	private @NonNull KrankheitIdentifier krankheitIdentifier;
	private boolean elektronischerImpfausweis;
	private boolean vollstaendigerImpfschutz;
	private @Nullable LocalDateTime timestampLetzterPostversand;
	private boolean gueltigeSchweizerAdresse;

	public KorrekturDashboardJax(
		@NonNull ImpfinformationDto infos,
		@Nullable LocalDateTime timestampLetzterPostversand,
		boolean isKantonUser
	) {
		Registrierung registrierung = infos.getRegistrierung();
		final Impfdossier dossierEntity = infos.getImpfdossier();

		this.registrierungsnummer = registrierung.getRegistrierungsnummer();
		this.vorname = registrierung.getVorname();
		this.name = registrierung.getName();
		this.mail = registrierung.getMail();
		this.telefon = registrierung.getTelefon();
		this.status = RegistrierungStatus.toRegistrierungStatus(dossierEntity.getDossierStatus());
		this.krankheitIdentifier = infos.getKrankheitIdentifier();
		this.eingang = registrierung.getRegistrierungsEingang();
		this.elektronischerImpfausweis = registrierung.isAbgleichElektronischerImpfausweis();
		this.vollstaendigerImpfschutz = dossierEntity.abgeschlossenMitVollstaendigemImpfschutz();
		this.benutzerId = registrierung.getBenutzerId();
		this.timestampLetzterPostversand = timestampLetzterPostversand;
		// Wir muessen das Flag auch fuer Online abfuellen, da man den Post-Versand auch dort erzwingen kann
		try {
			ValidationUtil.validateAndNormalizePlz(registrierung.getAdresse().getPlz());
			this.gueltigeSchweizerAdresse = true;
		} catch (PlzMappingException ignore) {
			this.gueltigeSchweizerAdresse = false;
		}

		if (infos.getImpfung1() != null) {
			if (!isKantonUser || KantonaleBerechtigung.isEditableForKanton(infos.getImpfung1())) {
				this.impfung1IfEditableForRole = ImpfungJax.from(infos.getImpfung1(),
					ImpfinformationenService.getImpffolgeNr(infos, infos.getImpfung1()));
			}

		}
		if (infos.getImpfung2() != null) {
			if (!isKantonUser || KantonaleBerechtigung.isEditableForKanton(infos.getImpfung1())) {
				this.impfung2IfEditableForRole = ImpfungJax.from(infos.getImpfung2(),
					ImpfinformationenService.getImpffolgeNr(infos, infos.getImpfung2()));
			}
		}

		if (infos.getKrankheitIdentifier().isSupportsExternesZertifikat()) {
			this.externGeimpft = ExternGeimpftJax.from(infos.getExternesZertifikat());
		}

		List<Impfdossiereintrag> sortedEintraege =
			infos.getImpfdossier().getImpfdossierEintraege().stream().sorted().collect(Collectors.toList());
		for (Impfdossiereintrag impfdossiereintrag : sortedEintraege) {
			if (impfdossiereintrag.getImpftermin() != null && infos.getBoosterImpfungen() != null) {
				Impftermin termin = impfdossiereintrag.getImpftermin();
				Impfung matchingImpfung = infos.getBoosterImpfungen()
					.stream()
					.filter(impfung -> impfung.getTermin().equals(termin))
					.findAny()
					.orElse(null);

				if (isKantonUser && !KantonaleBerechtigung.isEditableForKanton(matchingImpfung)) {
					continue;
				}
				impfdossiereintraegeEditableForRole.add(new ImpfdossiereintragJax(impfdossiereintrag, matchingImpfung));
			}
			impfdossiereintraegeEditableForRole.add(new ImpfdossiereintragJax(impfdossiereintrag));
		}
	}
}
