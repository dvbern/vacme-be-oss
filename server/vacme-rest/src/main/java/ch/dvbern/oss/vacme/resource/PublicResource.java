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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.enums.PartnerMarker;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_PUBLIC))
@Path("/public")
public class PublicResource {

	final RegistrierungService registrierungService;

	@Inject
	public PublicResource(
		@NonNull RegistrierungService registrierungService
	) {
		this.registrierungService = registrierungService;
	}

	@Operation(description = "Load keycloak / oidc configuration for Webclient")
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("keycloakconfig/{appname}")
	@PermitAll
	//	@Cache(maxAge = Constants.TWO_DAYS_IN_SEC)
	public Response loadKeycloakConfig(
		@NonNull @NotNull @PathParam("appname") String appname,
		@Nullable @Parameter(schema = @Schema(implementation = PartnerMarker.class)) @QueryParam("partnerClient") PartnerMarker partnerClient
	) {


		final Optional<String> keycloakConfigOpt;
		String keycloakPath = null;
		keycloakPath = this.getOidcClientConfigFilepathReg(partnerClient);

		if (appname.equals("vacme-web")) {
			keycloakPath = this.getOidcClientConfigFilepathWeb();
		}

		keycloakConfigOpt = readClientKeycloakConfig(keycloakPath);
		if (keycloakConfigOpt.isPresent()) {
			return Response.ok(keycloakConfigOpt.get(), MediaType.APPLICATION_JSON_TYPE).build();
		}
		return Response.noContent().build();
	}

	@NonNull
	String getOidcClientConfigFilepathReg(@Nullable PartnerMarker partnerClient){
		var config = ConfigProvider.getConfig();

		String basepath = "/keycloak-configs/initialreg/";
		String partnerPostfixPath = isWellMode(partnerClient) ? "well/" : "";
		String appConfig = config.getValue("vacme.keycloak.config.reg", String.class);
		String keycloakPath = basepath + partnerPostfixPath + appConfig;

		return keycloakPath;
	}

	@NonNull
	private String getOidcClientConfigFilepathWeb(){
		var config = ConfigProvider.getConfig();
		String basepath = "/keycloak-configs/web/";
		String appConfig = config.getValue("vacme.keycloak.config.web", String.class);
		String keycloakPath = basepath + appConfig;

		return keycloakPath;
	}

	/**
	 * We create a special oidc-client for our partner 'well' since the login process is triggered in the
	 * mobile app and therefore has some special requirements. For example this special client always expects
	 * the login to happen through well as a third party identity provider that is configured in keycloak.
	 * To force that flow we construct a special client that only supports this flow. To check which client
	 * we have to return this method is used
	*/
	private boolean isWellMode(@Nullable PartnerMarker partnerMarker) {
		return partnerMarker == PartnerMarker.WELL;
	}

	private Optional<String> readClientKeycloakConfig(String path) {

		final InputStream is = ApplicationPropertyResource.class.getResourceAsStream(path);
		//	or	final InputStream is = request.getServletContext().getResourceAsStream(path);
		if (is == null) {
			LOG.warn("No web-adapter configuration found at path {}. Keycloak is unconfigured", path);

		} else {
			try {
				return Optional.of(IOUtils.toString(is, StandardCharsets.UTF_8));
			} catch (IOException e) {
				LOG.warn("Error whiel reading keycloak client config file at {}", path);
			}

		}
		return Optional.empty();
	}

	@ConfigProperty(name = "quarkus.application.version")
	String version;

	@GET
	@Path("version")
	@Produces(MediaType.TEXT_PLAIN)
	@PermitAll
	public String getVersion() {
		return "Appversion " + version;
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/sendBenutzername/{registrierungsnummer}")
	@PermitAll
	public Response sendBenutzername(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		registrierungService.sendBenutzernameForRegistrierung(registrierungsnummer);
		return Response.ok().build();
	}

	/**
	 * Exception wie in normalen GET-Requests
	 */
	@GET
	@Path("demoexception/get/{throwException}")
	@Produces(MediaType.WILDCARD)
	@PermitAll
	public Response throwDemoException(@NonNull @NotNull @PathParam("throwException") boolean throwExcetion) {
		if (throwExcetion) {
			throw AppValidationMessage.IMPFTERMIN_IMPFUNG_BETWEEN_OTHERS.create();
		}
		return Response.ok().build();
	}

	/**
	 * Exception wie im Terminbestaetigung-Download
	 */
	@GET
	@Path("demoexception/download/{throwException}")
	@Consumes(MediaType.WILDCARD)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@PermitAll
	public Response throwDemoBlobException(@NonNull @NotNull @PathParam("throwException") boolean throwExcetion) {
		if (throwExcetion) {
			throw AppValidationMessage.TERMINBESTAETIGUNG_KEIN_OFFENER_TERMIN.create();
		}
		return Response.ok().build();
	}

	/**
	 * Exception wie beim Massenimport
	 */
	@POST
	@Path("demoexception/upload/{throwException}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@PermitAll
	public Response throwDemoUploadException(@NonNull @NotNull @PathParam("throwException") boolean throwExcetion) {
		if (throwExcetion) {
			throw AppValidationMessage.ILLEGAL_STATE.create(7);
		}
		return Response.ok().build();
	}
}
