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

package ch.dvbern.oss.vacme.resource;

import java.util.Objects;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.rest.auth.Authorizer;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_DOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_KONTROLLE;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Tags(@Tag(name = OpenApiConst.TAG_GEIMPFT))
@Path(VACME_WEB + "/geimpft")
public class GeimpftResource {

	private final Authorizer authorizer;
	private final RegistrierungService registrierungService;
	private final ImpfinformationenService impfinformationenService;

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("saveBemerkung/{registrierungsnummer}/{impffolge}/{impffolgeNr}")
	@RolesAllowed({ OI_DOKUMENTATION })
	public Response saveBemerkung(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @NotNull @PathParam("impffolge") Impffolge impffolge,
		@NonNull @NotNull @PathParam("impffolgeNr") Integer impffolgeNr,
		String bemerkung
	) {
		// TODO Affenpocken
		ImpfinformationDto impfinformationen = this.impfinformationenService.getImpfinformationen(
			registrierungsnummer,
			KrankheitIdentifier.COVID);
		Registrierung registrierung = impfinformationen.getRegistrierung();
		authorizer.checkUpdateAuthorization(registrierung);

		if (impffolge == Impffolge.ZWEITE_IMPFUNG && impfinformationen.getImpfdossier().verzichtetOhneVollstaendigemImpfschutz()) {
			// Die zweite impfung ist verzichtet, dann mussen wir das grund updaten
			if (impfinformationen.getImpfung1() == null || impfinformationen.getImpfung2() != null ||
				(impfinformationen.getBoosterImpfungen() != null && !impfinformationen.getBoosterImpfungen().isEmpty())) {
				// Diese faelle sollen nicht passieren
				throw new AppFailureException("Inconsistent state");
			}
			if (!Objects.equals(bemerkung, impfinformationen.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().getZweiteImpfungVerzichtetGrund())) {
				impfinformationen.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetGrund(bemerkung);
			}
		} else {
			if (impfinformationen.getImpfdossier().verzichtetOhneVollstaendigemImpfschutz()) {
				throw new AppFailureException("Versuch, die Bemerkung zu speichern, aber es ist verzichtetOhneVollstaendigemImpfschutz und nicht ZWEITE_IMPFUNG");
			}
			if (impffolgeNr != ImpfinformationenService.getNumberOfImpfung(impfinformationen)) {
				throw new AppFailureException("Versuch die bemerkung für impffolgeNr " + impffolgeNr + " für registrierung " + registrierungsnummer);
			}
			Impfung impfung = ImpfinformationenService.readImpfungForImpffolgeNr(impfinformationen, impffolge, impffolgeNr);
			if (impfung == null) {
				throw new AppFailureException("Inconsistent state");
			}
			if (!Objects.equals(bemerkung, impfung.getBemerkung())) {
				// Um die Bemerkungen speichern zu koennen, muss ich fuer das ODI berechtigt sein
				// in welchem geimpft wurde
				authorizer.checkReadAuthorization(impfung.getTermin().getImpfslot().getOrtDerImpfung());
				impfung.setBemerkung(bemerkung);
			}
		}
		return Response.ok().build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("acceptElektronischerImpfausweis/{registrierungsnummer}")
	@RolesAllowed({ OI_KONTROLLE, OI_DOKUMENTATION  })
	@Operation(summary = "Akzeptiert fuer eine bestimmte Registrierung den abgleich mit dem elektronischen Impfausweis")
	public Response acceptElektronischerImpfausweis(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);
		authorizer.checkUpdateAuthorization(registrierung);
		registrierungService.acceptElektronischerImpfausweis(registrierung);

		return Response.ok().build();
	}
}

