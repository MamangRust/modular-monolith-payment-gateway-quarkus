package com.sanedge.gateway.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.sanedge.gateway.dto.SaldoDto;
import com.sanedge.gateway.service.SaldoService;

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

@Path("/api/saldos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Saldos", description = "Saldo management endpoints")
public class SaldoResource {

        @Inject
        SaldoService saldoService;

        @GET
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "List all saldos")
        public Uni<Response> listSaldos(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return saldoService.listSaldos(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get saldo by ID")
        public Uni<Response> getSaldo(@PathParam("id") int id) {
                return saldoService.getSaldo(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        @Operation(summary = "Create a new saldo")
        public Uni<Response> createSaldo(SaldoDto.CreateRequest body) {
                return saldoService.createSaldo(body)
                                .map(dto -> Response.status(Response.Status.CREATED)
                                                .entity(dto)
                                                .build());
        }

        @PUT
        @Path("/{id}")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Update saldo")
        public Uni<Response> updateSaldo(@PathParam("id") int id, SaldoDto.UpdateRequest body) {
                SaldoDto.UpdateRequest req = new SaldoDto.UpdateRequest(id, body.cardNumber(), body.totalBalance());
                return saldoService.updateSaldo(id, req)
                                .map(dto -> Response.ok(dto).build());
        }

        @DELETE
        @Path("/{id}")
        @RolesAllowed("ROLE_ADMIN")
        @Operation(summary = "Soft-delete a saldo")
        public Uni<Response> deleteSaldo(@PathParam("id") int id) {
                return saldoService.deleteSaldo(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/balance/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly saldo balance statistics")
        public Uni<Response> findMonthlySaldoBalances(@QueryParam("year") int year) {
                return saldoService.findMonthlySaldoBalances(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/balance/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly saldo balance statistics")
        public Uni<Response> findYearlySaldoBalances(@QueryParam("year") int year) {
                return saldoService.findYearlySaldoBalances(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/total/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get monthly total saldo balance statistics")
        public Uni<Response> findMonthlyTotalSaldoBalance(
                        @QueryParam("year") int year,
                        @QueryParam("month") int month) {
                return saldoService.findMonthlyTotalSaldoBalance(year, month)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/total/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        @Operation(summary = "Get yearly total saldo balance statistics")
        public Uni<Response> findYearTotalSaldoBalance(@QueryParam("year") int year) {
                return saldoService.findYearTotalSaldoBalance(year)
                                .map(dto -> Response.ok(dto).build());
        }
}
