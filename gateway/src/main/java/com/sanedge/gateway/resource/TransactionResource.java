package com.sanedge.gateway.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.sanedge.gateway.dto.TransactionDto;
import com.sanedge.gateway.service.TransactionService;

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

@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionResource {

        @Inject
        TransactionService transactionService;

        @GET
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List all transactions")
        public Uni<Response> listTransactions(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return transactionService.listTransactions(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List all transactions by card number")
        public Uni<Response> listTransactionsByCard(
                        @QueryParam("cardNumber") String cardNumber,
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return transactionService.listTransactionsByCard(cardNumber, page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/active")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List active transactions")
        public Uni<Response> findActiveTransactions(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return transactionService.findActiveTransactions(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/trashed")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List trashed transactions")
        public Uni<Response> findTrashedTransactions(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return transactionService.findTrashedTransactions(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Get transaction by ID")
        public Uni<Response> getTransaction(@PathParam("id") int id) {
                return transactionService.getTransaction(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/merchant/{merchantId}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Get transactions by merchant ID")
        public Uni<Response> findByMerchantId(@PathParam("merchantId") int merchantId) {
                return transactionService.findByMerchantId(merchantId)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Create a new transaction")
        public Uni<Response> createTransaction(TransactionDto.CreateRequest body) {
                return transactionService.createTransaction(body)
                                .map(dto -> Response.status(Response.Status.CREATED)
                                                .entity(dto)
                                                .build());
        }

        @PUT
        @Path("/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Update transaction")
        public Uni<Response> updateTransaction(@PathParam("id") int id, TransactionDto.UpdateRequest body) {
                TransactionDto.UpdateRequest req = new TransactionDto.UpdateRequest(
                                id,
                                body.apiKey(),
                                body.cardNumber(),
                                body.amount(),
                                body.paymentMethod(),
                                body.merchantId());
                return transactionService.updateTransaction(id, req)
                                .map(dto -> Response.ok(dto).build());
        }

        @DELETE
        @Path("/{id}")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Soft-delete a transaction")
        public Uni<Response> deleteTransaction(@PathParam("id") int id) {
                return transactionService.deleteTransaction(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @DELETE
        @Path("/{id}/permanent")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Permanently delete a transaction")
        public Uni<Response> deleteTransactionPermanent(@PathParam("id") int id) {
                return transactionService.deleteTransactionPermanent(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/trash/{id}")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Soft-delete transaction by ID")
        public Uni<Response> trashTransaction(@PathParam("id") int id) {
                return transactionService.trashTransaction(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/restore/{id}")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Restore transaction by ID")
        public Uni<Response> restoreTransaction(@PathParam("id") int id) {
                return transactionService.restoreTransaction(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/restore-all")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Restore all transactions")
        public Uni<Response> restoreAllTransactions() {
                return transactionService.restoreAllTransactions()
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/delete-all")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Delete all transactions permanently")
        public Uni<Response> deleteAllTransactions() {
                return transactionService.deleteAllTransactions()
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transaction amount statistics")
        public Uni<Response> findMonthlyAmounts(@QueryParam("year") int year) {
                return transactionService.findMonthlyAmounts(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transaction amount statistics")
        public Uni<Response> findYearlyAmounts(@QueryParam("year") int year) {
                return transactionService.findYearlyAmounts(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/monthly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transaction amount statistics by card number")
        public Uni<Response> findMonthlyAmountsByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return transactionService.findMonthlyAmountsByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/yearly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transaction amount statistics by card number")
        public Uni<Response> findYearlyAmountsByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return transactionService.findYearlyAmountsByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/method/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transaction method statistics")
        public Uni<Response> findMonthlyMethods(@QueryParam("year") int year) {
                return transactionService.findMonthlyMethods(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/method/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transaction method statistics")
        public Uni<Response> findYearlyMethods(@QueryParam("year") int year) {
                return transactionService.findYearlyMethods(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/method/monthly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transaction method statistics by card number")
        public Uni<Response> findMonthlyMethodsByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return transactionService.findMonthlyMethodsByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/method/yearly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transaction method statistics by card number")
        public Uni<Response> findYearlyMethodsByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return transactionService.findYearlyMethodsByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/success")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transaction status success statistics")
        public Uni<Response> findMonthlyStatusSuccess(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month) {
                return transactionService.findMonthlyStatusSuccess(year, month)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/success")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transaction status success statistics")
        public Uni<Response> findYearlyStatusSuccess(@QueryParam("year") int year) {
                return transactionService.findYearlyStatusSuccess(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/failed")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transaction status failed statistics")
        public Uni<Response> findMonthlyStatusFailed(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month) {
                return transactionService.findMonthlyStatusFailed(year, month)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/failed")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transaction status failed statistics")
        public Uni<Response> findYearlyStatusFailed(@QueryParam("year") int year) {
                return transactionService.findYearlyStatusFailed(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/success/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transaction status success statistics by card number")
        public Uni<Response> findMonthlyStatusSuccessByCard(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month,
                        @QueryParam("cardNumber") String cardNumber) {
                return transactionService.findMonthlyStatusSuccessByCard(year, month, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/success/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transaction status success statistics by card number")
        public Uni<Response> findYearlyStatusSuccessByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return transactionService.findYearlyStatusSuccessByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/monthly/failed/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly transaction status failed statistics by card number")
        public Uni<Response> findMonthlyStatusFailedByCard(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month,
                        @QueryParam("cardNumber") String cardNumber) {
                return transactionService.findMonthlyStatusFailedByCard(year, month, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/status/yearly/failed/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly transaction status failed statistics by card number")
        public Uni<Response> findYearlyStatusFailedByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return transactionService.findYearlyStatusFailedByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }
}
