package com.sanedge.gateway.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.sanedge.gateway.dto.MerchantDto;
import com.sanedge.gateway.service.MerchantService;

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

@Path("/api/merchants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Merchants", description = "Merchant management endpoints")
public class MerchantResource {

        @Inject
        MerchantService merchantService;

        @GET
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List all merchants")
        public Uni<Response> listMerchants(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return merchantService.listMerchants(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Get merchant by ID")
        public Uni<Response> getMerchant(@PathParam("id") int id) {
                return merchantService.getMerchant(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Create a new merchant")
        public Uni<Response> createMerchant(MerchantDto.CreateRequest body) {
                return merchantService.createMerchant(body)
                                .map(dto -> Response.status(Response.Status.CREATED)
                                                .entity(dto)
                                                .build());
        }

        @PUT
        @Path("/{id}")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Update merchant")
        public Uni<Response> updateMerchant(@PathParam("id") int id,
                        MerchantDto.UpdateRequest body) {
                MerchantDto.UpdateRequest req = new MerchantDto.UpdateRequest(id, body.name(), body.userId(),
                                body.status());
                return merchantService.updateMerchant(id, req)
                                .map(dto -> Response.ok(dto).build());
        }

        @DELETE
        @Path("/{id}")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Soft-delete a merchant")
        public Uni<Response> deleteMerchant(@PathParam("id") int id) {
                return merchantService.deleteMerchant(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/transactions")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List all merchant transactions")
        public Uni<Response> findAllTransactions(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search,
                        @QueryParam("merchantId") int merchantId) {
                return merchantService.findAllTransactions(page, size, search, merchantId)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/transactions/by-id")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Find merchant transactions by id")
        public Uni<Response> findTransactionsById(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search,
                        @QueryParam("id") String id) {
                return merchantService.findTransactionsById(page, size, search, id)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/transactions/by-api-key")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Find merchant transactions by API key")
        public Uni<Response> findTransactionsByApiKey(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search,
                        @QueryParam("apiKey") String apiKey) {
                return merchantService.findTransactionsByApiKey(page, size, search, apiKey)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly amount statistics")
        public Uni<Response> getMonthlyAmount(@QueryParam("year") int year) {
                return merchantService.getMonthlyAmount(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly amount statistics")
        public Uni<Response> getYearlyAmount(@QueryParam("year") int year) {
                return merchantService.getYearlyAmount(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/by-id")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly amount statistics by merchant ID")
        public Uni<Response> getMonthlyAmountById(
                        @QueryParam("year") int year,
                        @QueryParam("merchantId") int merchantId) {
                return merchantService.getMonthlyAmountById(year, merchantId)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/yearly/by-id")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly amount statistics by merchant ID")
        public Uni<Response> getYearlyAmountById(
                        @QueryParam("year") int year,
                        @QueryParam("merchantId") int merchantId) {
                return merchantService.getYearlyAmountById(year, merchantId)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/by-api-key")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly amount statistics by API key")
        public Uni<Response> getMonthlyAmountByApiKey(
                        @QueryParam("year") int year,
                        @QueryParam("apiKey") String apiKey) {
                return merchantService.getMonthlyAmountByApiKey(year, apiKey)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/amount/yearly/by-api-key")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly amount statistics by API key")
        public Uni<Response> getYearlyAmountByApiKey(
                        @QueryParam("year") int year,
                        @QueryParam("apiKey") String apiKey) {
                return merchantService.getYearlyAmountByApiKey(year, apiKey)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/method/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly payment method statistics")
        public Uni<Response> getMonthlyMethod(@QueryParam("year") int year) {
                return merchantService.getMonthlyMethod(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/method/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly payment method statistics")
        public Uni<Response> getYearlyMethod(@QueryParam("year") int year) {
                return merchantService.getYearlyMethod(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/method/monthly/by-id")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly payment method statistics by merchant ID")
        public Uni<Response> getMonthlyMethodById(
                        @QueryParam("year") int year,
                        @QueryParam("merchantId") int merchantId) {
                return merchantService.getMonthlyMethodById(year, merchantId)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/method/yearly/by-id")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly payment method statistics by merchant ID")
        public Uni<Response> getYearlyMethodById(
                        @QueryParam("year") int year,
                        @QueryParam("merchantId") int merchantId) {
                return merchantService.getYearlyMethodById(year, merchantId)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/method/monthly/by-api-key")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly payment method statistics by API key")
        public Uni<Response> getMonthlyMethodByApiKey(
                        @QueryParam("year") int year,
                        @QueryParam("apiKey") String apiKey) {
                return merchantService.getMonthlyMethodByApiKey(year, apiKey)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/method/yearly/by-api-key")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly payment method statistics by API key")
        public Uni<Response> getYearlyMethodByApiKey(
                        @QueryParam("year") int year,
                        @QueryParam("apiKey") String apiKey) {
                return merchantService.getYearlyMethodByApiKey(year, apiKey)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/total-amount/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly total amount statistics")
        public Uni<Response> getMonthlyTotalAmount(@QueryParam("year") int year) {
                return merchantService.getMonthlyTotalAmount(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/total-amount/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly total amount statistics")
        public Uni<Response> getYearlyTotalAmount(@QueryParam("year") int year) {
                return merchantService.getYearlyTotalAmount(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/total-amount/monthly/by-id")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly total amount statistics by merchant ID")
        public Uni<Response> getMonthlyTotalAmountById(
                        @QueryParam("year") int year,
                        @QueryParam("merchantId") int merchantId) {
                return merchantService.getMonthlyTotalAmountById(year, merchantId)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/total-amount/yearly/by-id")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly total amount statistics by merchant ID")
        public Uni<Response> getYearlyTotalAmountById(
                        @QueryParam("year") int year,
                        @QueryParam("merchantId") int merchantId) {
                return merchantService.getYearlyTotalAmountById(year, merchantId)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/total-amount/monthly/by-api-key")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly total amount statistics by API key")
        public Uni<Response> getMonthlyTotalAmountByApiKey(
                        @QueryParam("year") int year,
                        @QueryParam("apiKey") String apiKey) {
                return merchantService.getMonthlyTotalAmountByApiKey(year, apiKey)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/total-amount/yearly/by-api-key")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly total amount statistics by API key")
        public Uni<Response> getYearlyTotalAmountByApiKey(
                        @QueryParam("year") int year,
                        @QueryParam("apiKey") String apiKey) {
                return merchantService.getYearlyTotalAmountByApiKey(year, apiKey)
                                .map(dto -> Response.ok(dto).build());
        }
}
