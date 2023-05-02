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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.base.ApplicationMessage;
import ch.dvbern.oss.vacme.entities.util.Pager;
import ch.dvbern.oss.vacme.jax.base.ApplicationMessageJax;
import ch.dvbern.oss.vacme.jax.base.PagerJax;
import ch.dvbern.oss.vacme.service.ApplicationMessageService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.AS_REGISTRATION_OI;

/**
 * Resource fuer ApplicationMessage
 */
@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_MESSAGES))
@Path("/messages")
public class ApplicationMessageResource {

	private final ApplicationMessageService messageService;

	@Inject
	public  ApplicationMessageResource(
		@NonNull  ApplicationMessageService messageService
	) {
		this.messageService = messageService;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("latest/{amount}")
	@PermitAll
	@NonNull
	public List<ApplicationMessageJax> getApplicationMessageLatest(
		@PathParam("amount") @NonNull Integer amount
	) {
		final List<ApplicationMessage> applicationMessages = this.messageService.getLatestMessage(amount);
		if (CollectionUtils.isNotEmpty(applicationMessages)) {
			return applicationMessages.stream()
				.map(ApplicationMessageJax::from)
				.collect(Collectors.toList());
		}
		return Lists.newArrayList();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("all/{pageSize}/{pageIndex}")
	@RolesAllowed(AS_REGISTRATION_OI)
	@NonNull
	public PagerJax<ApplicationMessageJax> getApplicationMessageAll(
		@PathParam("pageIndex") @NonNull Integer pageIndex,
		@PathParam("pageSize") @NonNull Integer pageSize
	) {
		final Pager<ApplicationMessage> applicationMessages = this.messageService.getMessageAll(pageSize, pageIndex);
		return PagerJax.from(Objects.requireNonNullElseGet(applicationMessages, Pager::empty), ApplicationMessageJax::from);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("message/{messageId}")
	@RolesAllowed(AS_REGISTRATION_OI)
	@Nullable
	public ApplicationMessageJax getApplicationMessageById(
		@PathParam("messageId") @NonNull String messageId
	) {
		final ApplicationMessage message = this.messageService.getById(messageId);
		if (message != null) {
			return ApplicationMessageJax.from(message);
		}
		return null;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("create")
	@RolesAllowed(AS_REGISTRATION_OI)
	public Response create(ApplicationMessageJax applicationMessageJax) {
		Objects.requireNonNull(applicationMessageJax);
		messageService.create(ApplicationMessageJax.toEntity(applicationMessageJax));
		return Response.ok().build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("update/{messageId}")
	@RolesAllowed(AS_REGISTRATION_OI)
	public Response update(
		@PathParam("messageId") @NonNull String messageId,
		ApplicationMessageJax applicationMessageJax
	) {
		Objects.requireNonNull(applicationMessageJax);
		ApplicationMessage formerMessage = this.messageService.getById(messageId);
		Objects.requireNonNull(formerMessage);
		if (!formerMessage.getZeitfenster().getBis().isBefore(LocalDateTime.now())) {
			messageService.update(messageId, ApplicationMessageJax.toEntity(applicationMessageJax));
			return Response.ok().build();
		}
		return Response.serverError().build();
	}
}
