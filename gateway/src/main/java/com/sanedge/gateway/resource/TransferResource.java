package com.sanedge.gateway.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.sanedge.gateway.dto.TransferDto;
import com.sanedge.gateway.service.TransferService;

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

@Path("/api/transfers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Transfers", description = "Transfer management endpoints")
public class TransferResource {

        @Inject
        TransferService transferService;

        @GET
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List all transfers")
        public Uni<Response> listTransfers(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return transferService.listTransfers(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/active")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List active transfers")
        public Uni<Response> findActiveTransfers(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return transferService.findActiveTransfers(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/trashed")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List trashed transfers")
        public Uni<Response> findTrashedTransfers(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return transferService.findTrashedTransfers(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get transfer by ID")
        public Uni<Response> getTransfer(@PathParam("id") int id) {
                return transferService.getTransfer(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/from/{cardNumber}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Get transfers from card number")
        public Uni<Response> findTransfersFrom(@PathParam("cardNumber") String cardNumber) {
                return transferService.findTransfersFrom(cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/to/{cardNumber}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Get transfers to card number")
        public Uni<Response> findTransfersTo(@PathParam("cardNumber") String cardNumber) {
                return transferService.findTransfersTo(cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Create a new transfer")
        public Uni<Response> createTransfer(TransferDto.CreateRequest body) {
                return transferService.createTransfer(body)
                                .map(dto -> Response.status(Response.Status.CREATED)
                                                .entity(dto)
                                                .build());
        }

        @PUT
        @Path("/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Update transfer")
        public Uni<Response> updateTransfer(@PathParam("id") int id,
                        TransferDto.UpdateRequest body) {
                TransferDto.UpdateRequest req = new TransferDto.UpdateRequest(
                                id,
                                body.transferFrom(),
                                body.transferTo(),
                                body.transferAmount());
                return transferService.updateTransfer(id, req)
                                .map(dto -> Response.ok(dto).build());
        }

        @DELETE
        @Path("/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Soft-delete a transfer")
        public Uni<Response> deleteTransfer(@PathParam("id") int id) {
                return transferService.deleteTransfer(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @DELETE
        @Path("/{id}/permanent")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Permanently delete a transfer")
        public Uni<Response> deleteTransferPermanent(@PathParam("id") int id) {
                return transferService.deleteTransferPermanent(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/trash/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Soft-delete transfer by ID")
        public Uni<Response> trashTransfer(@PathParam("id") int id) {
                return transferService.trashTransfer(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/restore/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Restore transfer by ID")
        public Uni<Response> restoreTransfer(@PathParam("id") int id) {
                return transferService.restoreTransfer(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/restore-all")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Restore all transfers")
        public Uni<Response> restoreAllTransfers() {
                return transferService.restoreAllTransfers()
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/delete-all")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Delete all transfers permanently")
        public Uni<Response> deleteAllTransfers() {
                return transferService.deleteAllTransfers()
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transfer amount statistics")
        public Uni<Response> findMonthlyAmounts(@QueryParam("year") int year) {
                return transferService.findMonthlyAmounts(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transfer amount statistics")
        public Uni<Response> findYearlyAmounts(@QueryParam("year") int year) {
                return transferService.findYearlyAmounts(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/monthly/from-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transfer amount statistics from card number")
        public Uni<Response> findMonthlyAmountsFromCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return transferService.findMonthlyAmountsFromCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/monthly/to-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transfer amount statistics to card number")
        public Uni<Response> findMonthlyAmountsToCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return transferService.findMonthlyAmountsToCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/yearly/from-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transfer amount statistics from card number")
        public Uni<Response> findYearlyAmountsFromCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return transferService.findYearlyAmountsFromCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/yearly/to-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transfer amount statistics to card number")
        public Uni<Response> findYearlyAmountsToCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return transferService.findYearlyAmountsToCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/success")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transfer status success statistics")
        public Uni<Response> findMonthlyStatusSuccess(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month) {
                return transferService.findMonthlyStatusSuccess(year, month)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/success")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transfer status success statistics")
        public Uni<Response> findYearlyStatusSuccess(@QueryParam("year") int year) {
                return transferService.findYearlyStatusSuccess(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/failed")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transfer status failed statistics")
        public Uni<Response> findMonthlyStatusFailed(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month) {
                return transferService.findMonthlyStatusFailed(year, month)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/failed")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transfer status failed statistics")
        public Uni<Response> findYearlyStatusFailed(@QueryParam("year") int year) {
                return transferService.findYearlyStatusFailed(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/success/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transfer status success statistics by card number")
        public Uni<Response> findMonthlyStatusSuccessByCard(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month,
                        @QueryParam("cardNumber") String cardNumber) {
                return transferService.findMonthlyStatusSuccessByCard(year, month, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/success/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transfer status success statistics by card number")
        public Uni<Response> findYearlyStatusSuccessByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return transferService.findYearlyStatusSuccessByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/failed/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transfer status failed statistics by card number")
        public Uni<Response> findMonthlyStatusFailedByCard(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month,
                        @QueryParam("cardNumber") String cardNumber) {
                return transferService.findMonthlyStatusFailedByCard(year, month, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/failed/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transfer status failed statistics by card number")
        public Uni<Response> findYearlyStatusFailedByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return transferService.findYearlyStatusFailedByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }
}
