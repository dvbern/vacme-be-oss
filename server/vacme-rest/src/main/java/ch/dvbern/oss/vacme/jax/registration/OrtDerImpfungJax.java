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

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfungTyp;
import ch.dvbern.oss.vacme.enums.Kundengruppe;
import ch.dvbern.oss.vacme.jax.base.AbstractUUIDEntityJax;
import ch.dvbern.oss.vacme.jax.base.AdresseJax;
import ch.dvbern.oss.vacme.service.GeocodeService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
public class OrtDerImpfungJax extends AbstractUUIDEntityJax {

	@NonNull
	private Kundengruppe kundengruppe;

	@NonNull
	private String name;

	@NonNull
	private AdresseJax adresse;

	@NonNull
	private OrtDerImpfungTyp typ;

	private boolean mobilerOrtDerImpfung;

	private boolean oeffentlich;

	private boolean terminverwaltung;

	@Nullable
	private String externerBuchungslink;

	private boolean personalisierterImpfReport;

	private boolean deaktiviert;

	private boolean booster;

	@Nullable
	private List<ImpfstoffJax> impfstoffe;

	@NonNull @NotNull
	@Schema(required = true)
	private List<KrankheitJax> krankheiten = Collections.emptyList();

	@Nullable
	private String zsrNummer;

	@Nullable
	private String glnNummer;

	@Nullable
	private String kommentar;

	@NonNull
	private String identifier;

	@Nullable
	private String organisationsverantwortung;

	@NonNull
	private String fachverantwortungbab;

	private boolean impfungGegenBezahlung;

	public OrtDerImpfungJax(@NonNull OrtDerImpfung ortDerImpfung) {
		super(ortDerImpfung);
		this.kundengruppe = ortDerImpfung.getKundengruppe();
		this.name = ortDerImpfung.getName();
		this.adresse = AdresseJax.from(ortDerImpfung.getAdresse());
		this.typ = ortDerImpfung.getTyp();
		this.mobilerOrtDerImpfung = ortDerImpfung.isMobilerOrtDerImpfung();
		this.oeffentlich = ortDerImpfung.isOeffentlich();
		this.terminverwaltung = ortDerImpfung.isTerminverwaltung();
		this.externerBuchungslink = ortDerImpfung.getExternerBuchungslink();
		this.personalisierterImpfReport = ortDerImpfung.isPersonalisierterImpfReport();
		this.deaktiviert = ortDerImpfung.isDeaktiviert();
		this.impfstoffe = ortDerImpfung.getImpfstoffs().stream()
			.map(ImpfstoffJax::from)
			.collect(Collectors.toList());
		this.krankheiten = ortDerImpfung.getKrankheiten().stream().map(KrankheitJax::of)
			.collect(Collectors.toList());
		this.booster = ortDerImpfung.isBooster();
		this.zsrNummer = ortDerImpfung.getZsrNummer();
		this.glnNummer = ortDerImpfung.getGlnNummer();
		this.kommentar = ortDerImpfung.getKommentar();
		this.identifier = ortDerImpfung.getIdentifier();
		this.fachverantwortungbab = ortDerImpfung.getFachverantwortungbabKeyCloakId();
		this.organisationsverantwortung = ortDerImpfung.getOrganisationsverantwortungKeyCloakId();
		this.impfungGegenBezahlung = ortDerImpfung.isImpfungGegenBezahlung();
	}

	@JsonIgnore
	public Consumer<OrtDerImpfung> getUpdateEntityConsumer(boolean isAdminUser, GeocodeService geocodeService) {
		return ortDerImpfung -> {
			ortDerImpfung.setAdresse(adresse.toEntity());

			LatLngJax latLng = geocodeService.geocodeAdresse(adresse.toEntity());
			ortDerImpfung.setLat(latLng.getLat());
			ortDerImpfung.setLng(latLng.getLng());

			ortDerImpfung.setTyp(typ);
			// Diese Felder duerfen nur von bestimmten Rollen angepasst werden
			if (isAdminUser) {
				ortDerImpfung.setKundengruppe(kundengruppe);
				ortDerImpfung.setName(name);
				ortDerImpfung.setMobilerOrtDerImpfung(mobilerOrtDerImpfung);
				ortDerImpfung.setOeffentlich(oeffentlich);
				ortDerImpfung.setTerminverwaltung(terminverwaltung);
				ortDerImpfung.setExternerBuchungslink(externerBuchungslink);
				ortDerImpfung.setPersonalisierterImpfReport(personalisierterImpfReport);
				ortDerImpfung.setDeaktiviert(deaktiviert);
				ortDerImpfung.setBooster(booster);
			}
			ortDerImpfung.setZsrNummer(zsrNummer);
			ortDerImpfung.setGlnNummer(glnNummer);
			ortDerImpfung.setKommentar(kommentar);
			ortDerImpfung.setIdentifier(identifier);
			ortDerImpfung.setFachverantwortungbabKeyCloakId(fachverantwortungbab);
			ortDerImpfung.setOrganisationsverantwortungKeyCloakId(organisationsverantwortung);
			ortDerImpfung.setImpfungGegenBezahlung(impfungGegenBezahlung);
		};
	}
}
