package com.sanedge.gateway.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.sanedge.gateway.dto.TopupDto;
import com.sanedge.gateway.service.TopupService;

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

@Path("/api/topups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Topups", description = "Topup management endpoints")
public class TopupResource {

        @Inject
        TopupService topupService;

        @GET
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List all topups")
        public Uni<Response> listTopups(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return topupService.listTopups(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List topups by card number")
        public Uni<Response> listTopupsByCard(
                        @QueryParam("cardNumber") String cardNumber,
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return topupService.listTopupsByCard(cardNumber, page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/active")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List active topups")
        public Uni<Response> findActiveTopups(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return topupService.findActiveTopups(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/trashed")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List trashed topups")
        public Uni<Response> findTrashedTopups(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return topupService.findTrashedTopups(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Get topup by ID")
        public Uni<Response> getTopup(@PathParam("id") int id) {
                return topupService.getTopup(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/card/{cardNumber}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Get topup by card number and year")
        public Uni<Response> getTopupByCard(@PathParam("cardNumber") String cardNumber, @QueryParam("year") int year) {
                return topupService.getTopupByCard(cardNumber, year)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Create a new topup")
        public Uni<Response> createTopup(TopupDto.CreateRequest body) {
                return topupService.createTopup(body)
                                .map(dto -> Response.status(Response.Status.CREATED)
                                                 .entity(dto)
                                                 .build());
        }

        @PUT
        @Path("/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Update topup")
        public Uni<Response> updateTopup(@PathParam("id") int id, TopupDto.UpdateRequest body) {
                TopupDto.UpdateRequest req = new TopupDto.UpdateRequest(id, body.cardNumber(), body.topupAmount(), body.topupMethod());
                return topupService.updateTopup(id, req)
                                .map(dto -> Response.ok(dto).build());
        }

        @DELETE
        @Path("/{id}/permanent")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Permanently delete topup by ID")
        public Uni<Response> deleteTopupPermanent(@PathParam("id") int id) {
                return topupService.deleteTopupPermanent(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/trash/{id}")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Soft-delete topup by ID")
        public Uni<Response> trashTopup(@PathParam("id") int id) {
                return topupService.trashTopup(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/restore/{id}")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Restore topup by ID")
        public Uni<Response> restoreTopup(@PathParam("id") int id) {
                return topupService.restoreTopup(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/restore-all")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Restore all topups")
        public Uni<Response> restoreAllTopups() {
                return topupService.restoreAllTopups()
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/delete-all")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Delete all topups permanently")
        public Uni<Response> deleteAllTopups() {
                return topupService.deleteAllTopups()
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly topup amount statistics")
        public Uni<Response> getMonthlyAmounts(@QueryParam("year") int year) {
                return topupService.getMonthlyAmounts(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly topup amount statistics")
        public Uni<Response> getYearlyAmounts(@QueryParam("year") int year) {
                return topupService.getYearlyAmounts(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/monthly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly topup amount statistics by card number")
        public Uni<Response> getMonthlyAmountsByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return topupService.getMonthlyAmountsByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/yearly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly topup amount statistics by card number")
        public Uni<Response> getYearlyAmountsByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return topupService.getYearlyAmountsByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/method/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly topup method statistics")
        public Uni<Response> getMonthlyMethods(@QueryParam("year") int year) {
                return topupService.getMonthlyMethods(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/method/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly topup method statistics")
        public Uni<Response> getYearlyMethods(@QueryParam("year") int year) {
                return topupService.getYearlyMethods(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/method/monthly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly topup method statistics by card number")
        public Uni<Response> getMonthlyMethodsByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return topupService.getMonthlyMethodsByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/method/yearly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly topup method statistics by card number")
        public Uni<Response> getYearlyMethodsByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return topupService.getYearlyMethodsByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/success")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly topup status success statistics")
        public Uni<Response> getMonthlyStatusSuccess(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month) {
                return topupService.getMonthlyStatusSuccess(year, month)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/success")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly topup status success statistics")
        public Uni<Response> getYearlyStatusSuccess(@QueryParam("year") int year) {
                return topupService.getYearlyStatusSuccess(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/failed")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly topup status failed statistics")
        public Uni<Response> getMonthlyStatusFailed(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month) {
                return topupService.getMonthlyStatusFailed(year, month)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/failed")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly topup status failed statistics")
        public Uni<Response> getYearlyStatusFailed(@QueryParam("year") int year) {
                return topupService.getYearlyStatusFailed(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/success/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly topup status success statistics by card number")
        public Uni<Response> getMonthlyStatusSuccessByCard(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month,
                        @QueryParam("cardNumber") String cardNumber) {
                return topupService.getMonthlyStatusSuccessByCard(year, month, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/success/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly topup status success statistics by card number")
        public Uni<Response> getYearlyStatusSuccessByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return topupService.getYearlyStatusSuccessByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/failed/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly topup status failed statistics by card number")
        public Uni<Response> getMonthlyStatusFailedByCard(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month,
                        @QueryParam("cardNumber") String cardNumber) {
                return topupService.getMonthlyStatusFailedByCard(year, month, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/failed/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly topup status failed statistics by card number")
        public Uni<Response> getYearlyStatusFailedByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return topupService.getYearlyStatusFailedByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }
}
