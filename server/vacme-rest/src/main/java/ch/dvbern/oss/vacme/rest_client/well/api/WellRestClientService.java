/*
 *
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.oss.vacme.rest_client.well.api;

import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import ch.dvbern.oss.vacme.rest_client.RestClientLoggingFilter;
import ch.dvbern.oss.vacme.rest_client.well.api.auth.WellAuthorizationHeaderFactory;
import ch.dvbern.oss.vacme.rest_client.well.model.AccountLinkEntityModel;
import ch.dvbern.oss.vacme.rest_client.well.model.AppointmentEntityModel;
import ch.dvbern.oss.vacme.rest_client.well.model.ApprovalPeriodEntityModel;
import ch.dvbern.oss.vacme.rest_client.well.model.VacMeAppointmentRequestDto;
import ch.dvbern.oss.vacme.rest_client.well.model.VacMeApprovalPeriodRequestDto;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * VacMe API
 *
 * <p>API interface for VacMe to provide vaccination appointment data for Well users
 *
 */
@RegisterRestClient(configKey="well-api")
@RegisterProvider(RestClientLoggingFilter.class)
@RegisterClientHeaders(WellAuthorizationHeaderFactory.class)
@Path("/")
public interface WellRestClientService {

	/**
	 * Create account link
	 *
	 * Save link between Well and VacMe accounts
	 *
	 */
	@POST
	@Path("/v1/vacme/accountlinks/{userId}")
	@Produces({ "application/vnd.api+json" })
	@Operation(summary = "Create account link")
	@APIResponses(value = {
		@APIResponse(responseCode = "200", description = "Account link has been created", content = @Content(mediaType = "application/vnd.api+json", schema = @Schema(implementation = AccountLinkEntityModel.class))) })
	@PermitAll
	public AccountLinkEntityModel createAccountLink(@PathParam("userId") UUID userId);

	/**
	 * Delete account link
	 *
	 * Delete account link of the provided Well user id
	 *
	 */
	@DELETE
	@Path("/v1/vacme/accountlinks/{userId}")
	@Produces({ "application/vnd.api+json" })
	@Operation(summary = "Delete account link")
	@APIResponses(value = {
		@APIResponse(responseCode = "200", description = "Account link has been removed", content = @Content(mediaType = "application/vnd.api+json", schema = @Schema(implementation = AccountLinkEntityModel.class))) })
	@PermitAll
	public AccountLinkEntityModel deleteAccountLink(@PathParam("userId") UUID userId);

	/**
	 * Cancel appointment
	 *
	 * Set the booking status of an appointment to CANCELLED
	 *
	 */
	@DELETE
	@Path("/v1/vacme/appointments/{appointmentId}")
	@Produces({ "application/vnd.api+json" })
	@Operation(summary = "Cancel appointment")
	@APIResponses(value = {
		@APIResponse(responseCode = "200", description = "Appointment has been removed", content = @Content(mediaType = "application/vnd.api+json", schema = @Schema(implementation = AppointmentEntityModel.class))) })
	@PermitAll
	public AppointmentEntityModel deleteAppointment(@PathParam("appointmentId") String appointmentId);

	/**
	 * Remove approval period
	 *
	 * Remove approval period
	 *
	 */
	@DELETE
	@Path("/v1/vacme/approvalperiods/{approvalPeriodId}")
	@Operation(summary = "Remove approval period")
	@APIResponses(value = {
		@APIResponse(responseCode = "200", description = "Approval period has been removed") })
	@PermitAll
	public void deleteApprovalPeriod(@PathParam("approvalPeriodId") String approvalPeriodId);

	/**
	 * Create or update appointment
	 *
	 * Create or update existing appointment
	 *
	 */
	@PUT
	@Path("/v1/vacme/appointments/{appointmentId}")
	@Consumes({ "application/json" })
	@Produces({ "application/vnd.api+json" })
	@Operation(summary = "Create or update appointment")
	@APIResponses(value = {
		@APIResponse(responseCode = "200", description = "Appointment created or updated", content = @Content(mediaType = "application/vnd.api+json", schema = @Schema(implementation = AppointmentEntityModel.class))) })
	@PermitAll
	public AppointmentEntityModel upsertAppointment(VacMeAppointmentRequestDto body, @PathParam("appointmentId") String appointmentId);

	/**
	 * Create or update approval period
	 *
	 * Create or update existing approval period
	 *
	 */
	@PUT
	@Path("/v1/vacme/approvalperiods/{approvalPeriodId}")
	@Consumes({ "application/json" })
	@Produces({ "application/vnd.api+json" })
	@Operation(summary = "Create or update approval period")
	@APIResponses(value = {
		@APIResponse(responseCode = "200", description = "Approval period created or updated", content = @Content(mediaType = "application/vnd.api+json", schema = @Schema(implementation = ApprovalPeriodEntityModel.class))) })
	@PermitAll
	public ApprovalPeriodEntityModel upsertApprovalPeriod(VacMeApprovalPeriodRequestDto body, @PathParam("approvalPeriodId") String approvalPeriodId);
}
