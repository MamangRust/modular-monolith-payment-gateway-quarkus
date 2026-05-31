package com.sanedge.gateway.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.sanedge.gateway.dto.WithdrawDto;
import com.sanedge.gateway.service.WithdrawService;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/withdraws")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Withdraws", description = "Withdraw management endpoints")
public class WithdrawResource {

        @Inject
        WithdrawService withdrawService;

        @GET
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List all withdraws")
        public Uni<Response> listWithdraws(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return withdrawService.listWithdraws(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/by-card/{cardNumber}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Get withdraws by card number")
        public Uni<Response> findByCard(@PathParam("cardNumber") String cardNumber) {
                return withdrawService.findByCard(cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/active")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List active withdraws")
        public Uni<Response> findActiveWithdraws(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return withdrawService.findActiveWithdraws(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/trashed")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List trashed withdraws")
        public Uni<Response> findTrashedWithdraws(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return withdrawService.findTrashedWithdraws(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Get withdraw by ID")
        public Uni<Response> getWithdraw(@PathParam("id") int id) {
                return withdrawService.getWithdraw(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Create a new withdraw")
        public Uni<Response> createWithdraw(WithdrawDto.CreateRequest body) {
                return withdrawService.createWithdraw(body)
                                .map(dto -> Response.status(Response.Status.CREATED)
                                                .entity(dto)
                                                .build());
        }

        @PUT
        @Path("/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Update withdraw")
        public Uni<Response> updateWithdraw(@PathParam("id") int id, WithdrawDto.UpdateRequest body) {
                WithdrawDto.UpdateRequest req = new WithdrawDto.UpdateRequest(
                                id,
                                body.cardNumber(),
                                body.withdrawAmount());
                return withdrawService.updateWithdraw(id, req)
                                .map(dto -> Response.ok(dto).build());
        }

        @DELETE
        @Path("/{id}")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Soft-delete a withdraw")
        public Uni<Response> deleteWithdraw(@PathParam("id") int id) {
                return withdrawService.deleteWithdraw(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @DELETE
        @Path("/{id}/permanent")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Permanently delete a withdraw")
        public Uni<Response> deleteWithdrawPermanent(@PathParam("id") int id) {
                return withdrawService.deleteWithdrawPermanent(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/trash/{id}")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Soft-delete withdraw by ID")
        public Uni<Response> trashWithdraw(@PathParam("id") int id) {
                return withdrawService.trashWithdraw(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/restore/{id}")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Restore withdraw by ID")
        public Uni<Response> restoreWithdraw(@PathParam("id") int id) {
                return withdrawService.restoreWithdraw(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/restore-all")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Restore all withdraws")
        public Uni<Response> restoreAllWithdraws() {
                return withdrawService.restoreAllWithdraws()
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/delete-all")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Delete all withdraws permanently")
        public Uni<Response> deleteAllWithdraws() {
                return withdrawService.deleteAllWithdraws()
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly withdraw amount statistics")
        public Uni<Response> findMonthlyAmounts(@QueryParam("year") int year) {
                return withdrawService.findMonthlyAmounts(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly withdraw amount statistics")
        public Uni<Response> findYearlyAmounts(@QueryParam("year") int year) {
                return withdrawService.findYearlyAmounts(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/monthly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly withdraw amount statistics by card number")
        public Uni<Response> findMonthlyByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return withdrawService.findMonthlyByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/yearly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly withdraw amount statistics by card number")
        public Uni<Response> findYearlyByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return withdrawService.findYearlyByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/success")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly withdraw status success statistics")
        public Uni<Response> findMonthlyStatusSuccess(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month) {
                return withdrawService.findMonthlyStatusSuccess(year, month)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/success")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly withdraw status success statistics")
        public Uni<Response> findYearlyStatusSuccess(@QueryParam("year") int year) {
                return withdrawService.findYearlyStatusSuccess(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/failed")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly withdraw status failed statistics")
        public Uni<Response> findMonthlyStatusFailed(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month) {
                return withdrawService.findMonthlyStatusFailed(year, month)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/failed")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly withdraw status failed statistics")
        public Uni<Response> findYearlyStatusFailed(@QueryParam("year") int year) {
                return withdrawService.findYearlyStatusFailed(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/success/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly withdraw status success statistics by card number")
        public Uni<Response> findMonthlyStatusSuccessByCard(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month,
                        @QueryParam("cardNumber") String cardNumber) {
                return withdrawService.findMonthlyStatusSuccessByCard(year, month, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/success/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly withdraw status success statistics by card number")
        public Uni<Response> findYearlyStatusSuccessByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return withdrawService.findYearlyStatusSuccessByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/failed/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly withdraw status failed statistics by card number")
        public Uni<Response> findMonthlyStatusFailedByCard(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month,
                        @QueryParam("cardNumber") String cardNumber) {
                return withdrawService.findMonthlyStatusFailedByCard(year, month, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/failed/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly withdraw status failed statistics by card number")
        public Uni<Response> findYearlyStatusFailedByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return withdrawService.findYearlyStatusFailedByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }
}
