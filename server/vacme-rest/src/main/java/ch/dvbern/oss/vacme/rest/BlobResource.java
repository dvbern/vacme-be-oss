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

package ch.dvbern.oss.vacme.rest;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFile;
import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFileTyp;
import ch.dvbern.oss.vacme.entities.embeddables.FileBytes;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.jax.FileInfoJax;
import ch.dvbern.oss.vacme.rest.auth.Authorizer;
import ch.dvbern.oss.vacme.service.ImpfdossierFileService;
import ch.dvbern.oss.vacme.service.ImpfdossierService;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.MultipartFormToFileConverter;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import ch.dvbern.oss.vacme.util.RestUtil;
import com.github.HonoluluHenk.httpcontentdisposition.Disposition;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_DOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_IMPFVERANTWORTUNG;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_KONTROLLE;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_MEDIZINISCHER_REPORTER;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Transactional
@Slf4j
@ApplicationScoped
@Tags(@Tag(name = OpenApiConst.TAG_DOSSIER))
@Path(VACME_WEB + "/blob")
public class BlobResource {

	private final RegistrierungService registrierungService;
	private final ImpfdossierService impfdossierService;
	private Authorizer authorizer;
	private final ImpfdossierFileService impfdossierFileService;

	@ConfigProperty(name = "vacme.upload.whitelist", defaultValue = "application/pdf,image/png,image/jpg,image/jpeg,image/svg,image/gif,image/bmp")
	List<String> whitelistedMimeTypes;

	@Inject
	public BlobResource(
		@NonNull RegistrierungService registrierungService,
		@NonNull ImpfdossierService impfdossierService,
		@NonNull Authorizer authorizer,
		@NonNull ImpfdossierFileService impfdossierFileService
	) {
		this.registrierungService = registrierungService;
		this.impfdossierService = impfdossierService;
		this.authorizer = authorizer;
		this.impfdossierFileService = impfdossierFileService;
	}

	@SuppressWarnings("ThrowInsideCatchBlockWhichIgnoresCaughtException")
	@POST
	@Path("krankheit/{krankheit}/registrierungsnummer/{registrierungsnummer}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, OI_KONTROLLE })
	public List<FileInfoJax> upload(
		@PathParam("krankheit") KrankheitIdentifier krankheit,
		@PathParam("registrierungsnummer") String registrierungsnummer,
		@Nonnull @NotNull MultipartFormDataInput input) {

		final Registrierung registrierung = this.registrierungService.findRegistrierung(registrierungsnummer);
		final Impfdossier impfdossier = this.impfdossierService.getOrCreateImpfdossier(registrierung, krankheit);
		authorizer.checkUpdateAuthorization(registrierung);

		try {
			final List<ImpfdossierFile> impfdossierFiles = MultipartFormToFileConverter.parse(input, impfdossier,
				ImpfdossierFileTyp.IMPFFREIGABE_DURCH_HAUSARZT);
			impfdossierFiles.stream()
				.filter(impfdossierFile -> !this.checkFiletypeAllowed(impfdossierFile))
				.findFirst()
				.ifPresent(file -> {
					throw AppValidationMessage.UPLOAD_INVALID_FILE_TYPE.create(file.getFileBlob().getMimeType().getMimeType());
				});

			return impfdossierFiles.stream()
				.map(impfdossierFileService::save)
				.map(FileInfoJax::from)
				.collect(Collectors.toList());

		} catch (AppValidationException e) {
			throw e; // rethrow app validation to get the right error message (prob, wrong file type)
		} catch (Exception e) {
			LOG.error("Fehler beim hochladen einer Datei", e);
			throw AppValidationMessage.UPLOAD_FAILED.create();
		}

	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@Path("registrierungsnummer/{registrierungsnummer}/file/{fileId}")
	@RolesAllowed({ OI_MEDIZINISCHER_REPORTER, OI_DOKUMENTATION })
	public Response downloadFile(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @NotNull @PathParam("fileId") UUID fileId
	) {
		Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);

		authorizer.checkReadAuthorization(registrierung);

		ImpfdossierFile file = impfdossierFileService.getDokument(registrierung, fileId);
		return createDownloadResponse(file);
	}

	private boolean checkFiletypeAllowed(ImpfdossierFile fileInfo) {
		ch.dvbern.oss.vacme.shared.util.MimeType contentType = fileInfo.getFileBlob().getMimeType();
		return this.readMimeTypeWhitelist().contains(contentType.getMimeType());
	}

	private Collection<String> readMimeTypeWhitelist() {
		return whitelistedMimeTypes;
	}

	@NonNull
	private Response createDownloadResponse(ImpfdossierFile file) {

		final FileBytes downloadFile = FileBytes.of(
			file.getFileBlob().getFileName(),
			file.getFileBlob().getMimeType(),
			file.getContent(),
			LocalDateTime.now()
		);
		return RestUtil.buildFileResponse(Disposition.INLINE, downloadFile);
	}
}
