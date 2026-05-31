package com.sanedge.gateway.resource;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.sanedge.gateway.dto.CardDto;
import com.sanedge.gateway.service.CardService;

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

@Path("/api/cards")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Cards", description = "Card management endpoints")
public class CardResource {

        @Inject
        CardService cardService;

        @GET
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> listCards(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return cardService.listCards(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/active")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findActiveCards(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return cardService.findActiveCards(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/trashed")
        @RolesAllowed("ROLE_ADMIN")
        public Uni<Response> findTrashedCards(
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("size") @DefaultValue("20") int size,
                        @QueryParam("search") String search) {
                return cardService.findTrashedCards(page, size, search)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> getCard(@PathParam("id") int id) {
                return cardService.getCard(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/by-user/{user_id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findCardByUser(@PathParam("user_id") int userId) {
                return cardService.findCardByUser(userId)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/by-number/{card_number}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findCardByNumber(@PathParam("card_number") String cardNumber) {
                return cardService.findCardByNumber(cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> createCard(CardDto.CreateRequest body) {
                return cardService.createCard(body)
                                .map(dto -> Response.status(Response.Status.CREATED)
                                                .entity(dto)
                                                .build());
        }

        @PUT
        @Path("/{id}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> updateCard(@PathParam("id") int id, CardDto.UpdateRequest body) {
                CardDto.UpdateRequest req = new CardDto.UpdateRequest(
                                id,
                                body.userId(),
                                body.cardType(),
                                body.expireDate(),
                                body.cvv(),
                                body.cardProvider());
                return cardService.updateCard(id, req)
                                .map(dto -> Response.ok(dto).build());
        }

        @DELETE
        @Path("/{id}")
        @RolesAllowed("ROLE_ADMIN")
        public Uni<Response> deleteCard(@PathParam("id") int id) {
                return cardService.deleteCard(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @DELETE
        @Path("/{id}/permanent")
        @RolesAllowed("ROLE_ADMIN")
        public Uni<Response> deleteCardPermanent(@PathParam("id") int id) {
                return cardService.deleteCardPermanent(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/restore/{id}")
        @RolesAllowed("ROLE_ADMIN")
        public Uni<Response> restoreCard(@PathParam("id") int id) {
                return cardService.restoreCard(id)
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/restore-all")
        @RolesAllowed("ROLE_ADMIN")
        public Uni<Response> restoreAllCards() {
                return cardService.restoreAllCards()
                                .map(dto -> Response.ok(dto).build());
        }

        @POST
        @Path("/delete-all")
        @RolesAllowed("ROLE_ADMIN")
        public Uni<Response> deleteAllCards() {
                return cardService.deleteAllCards()
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/balance/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findMonthlyBalance(@QueryParam("year") int year) {
                return cardService.findMonthlyBalance(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/balance/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findYearlyBalance(@QueryParam("year") int year) {
                return cardService.findYearlyBalance(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/balance/monthly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> getMonthlyBalanceByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return cardService.getMonthlyBalanceByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/balance/yearly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> getYearlyBalanceByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return cardService.getYearlyBalanceByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/topup/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findMonthlyTopupAmount(@QueryParam("year") int year) {
                return cardService.findMonthlyTopupAmount(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/topup/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findYearlyTopupAmount(@QueryParam("year") int year) {
                return cardService.findYearlyTopupAmount(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/topup/monthly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> getMonthlyTopupAmountByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return cardService.getMonthlyTopupAmountByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/topup/yearly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> getYearlyTopupAmountByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return cardService.getYearlyTopupAmountByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/transaction/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findMonthlyTransactionAmount(@QueryParam("year") int year) {
                return cardService.findMonthlyTransactionAmount(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/transaction/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findYearlyTransactionAmount(@QueryParam("year") int year) {
                return cardService.findYearlyTransactionAmount(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/transaction/monthly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> getMonthlyTransactionAmountByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return cardService.getMonthlyTransactionAmountByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/transaction/yearly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> getYearlyTransactionAmountByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return cardService.getYearlyTransactionAmountByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/transfer/monthly/sender")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findMonthlyTransferAmountSender(@QueryParam("year") int year) {
                return cardService.findMonthlyTransferAmountSender(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/transfer/monthly/receiver")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findMonthlyTransferAmountReceiver(@QueryParam("year") int year) {
                return cardService.findMonthlyTransferAmountReceiver(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/transfer/yearly/sender")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findYearlyTransferAmountSender(@QueryParam("year") int year) {
                return cardService.findYearlyTransferAmountSender(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/transfer/yearly/receiver")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findYearlyTransferAmountReceiver(@QueryParam("year") int year) {
                return cardService.findYearlyTransferAmountReceiver(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/transfer/monthly/by-card/sender")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> getMonthlyTransferAmountByCardSender(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return cardService.getMonthlyTransferAmountByCardSender(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/transfer/monthly/by-card/receiver")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> getMonthlyTransferAmountByCardReceiver(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return cardService.getMonthlyTransferAmountByCardReceiver(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/transfer/yearly/by-card/sender")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> getYearlyTransferAmountByCardSender(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return cardService.getYearlyTransferAmountByCardSender(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/transfer/yearly/by-card/receiver")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> getYearlyTransferAmountByCardReceiver(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return cardService.getYearlyTransferAmountByCardReceiver(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/withdraw/monthly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findMonthlyWithdrawAmount(@QueryParam("year") int year) {
                return cardService.findMonthlyWithdrawAmount(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/withdraw/yearly")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findYearlyWithdrawAmount(@QueryParam("year") int year) {
                return cardService.findYearlyWithdrawAmount(year)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/withdraw/monthly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> getMonthlyWithdrawAmountByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return cardService.getMonthlyWithdrawAmountByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/stats/withdraw/yearly/by-card")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> getYearlyWithdrawAmountByCard(
                        @QueryParam("year") int year,
                        @QueryParam("cardNumber") String cardNumber) {
                return cardService.getYearlyWithdrawAmountByCard(year, cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/dashboard")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
        public Uni<Response> findCardDashboard() {
                return cardService.findCardDashboard()
                                .map(dto -> Response.ok(dto).build());
        }

        @GET
        @Path("/dashboard/{card_number}")
        @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER" })
        public Uni<Response> findCardDashboardByCardNumber(@PathParam("card_number") String cardNumber) {
                return cardService.findCardDashboardByCardNumber(cardNumber)
                                .map(dto -> Response.ok(dto).build());
        }
}
