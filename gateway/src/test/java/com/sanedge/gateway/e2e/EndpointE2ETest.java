package com.sanedge.gateway.e2e;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.sanedge.gateway.dto.*;
import com.sanedge.gateway.service.*;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;

/**
 * E2E tests that verify ALL endpoints are reachable and return correct HTTP status codes.
 * Each mock returns a minimal valid response so the full HTTP stack is exercised:
 * - HTTP parsing
 * - Request routing
 * - JSON deserialization / serialization
 * - Security filter (if enabled)
 * - Response building
 */
@QuarkusTest
public class EndpointE2ETest {

    // ═══════════════════════════════════════════════════════════════════
    // AUTH ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════
    @Nested
    class AuthEndpointTest {

        @InjectMock
        AuthService authService;

        @BeforeEach
        void setup() {
            when(authService.register(any()))
                    .thenReturn(Uni.createFrom().item(
                            new AuthDto.RegisterResponse("success", "registered", null)));
            when(authService.login(any()))
                    .thenReturn(Uni.createFrom().item(
                            new AuthDto.LoginResponse("success", "logged in", new AuthDto.TokenResponse("at", "rt"))));
            when(authService.verify(any()))
                    .thenReturn(Uni.createFrom().item(new AuthDto.SimpleResponse("success", "verified")));
            when(authService.forgotPassword(any()))
                    .thenReturn(Uni.createFrom().item(new AuthDto.SimpleResponse("success", "email sent")));
            when(authService.resetPassword(any()))
                    .thenReturn(Uni.createFrom().item(new AuthDto.SimpleResponse("success", "reset")));
            when(authService.refresh(any()))
                    .thenReturn(Uni.createFrom().item(
                            new AuthDto.RefreshTokenResponse("success", "refreshed",
                                    new AuthDto.TokenResponse("at2", "rt2"))));
            when(authService.getMe(anyInt()))
                    .thenReturn(Uni.createFrom().item(
                            new AuthDto.GetMeResponse("success", "ok", null)));
        }

        @Test void register_returns201() {
            given().contentType("application/json")
                    .body(Map.of("firstname", "John", "lastname", "Doe",
                            "email", "j@e.com", "password", "p", "confirmPassword", "p"))
                .when().post("/api/auth/register")
                .then().statusCode(201);
        }

        @Test void login_returns200() {
            given().contentType("application/json")
                    .body(Map.of("email", "j@e.com", "password", "p"))
                .when().post("/api/auth/login")
                .then().statusCode(200);
        }

        @Test void verify_returns200() {
            given().contentType("application/json")
                    .body(Map.of("code", "ABC123"))
                .when().post("/api/auth/verify")
                .then().statusCode(200);
        }

        @Test void forgotPassword_returns200() {
            given().contentType("application/json")
                    .body(Map.of("email", "j@e.com"))
                .when().post("/api/auth/forgot-password")
                .then().statusCode(200);
        }

        @Test void resetPassword_returns200() {
            given().contentType("application/json")
                    .body(Map.of("resetToken", "tk", "password", "new", "confirmPassword", "new"))
                .when().post("/api/auth/reset-password")
                .then().statusCode(200);
        }

        @Test void refresh_returns200() {
            given().contentType("application/json")
                    .body(Map.of("refreshToken", "rt"))
                .when().post("/api/auth/refresh")
                .then().statusCode(200);
        }

        @Test void getMe_returns200() {
            given().queryParam("userId", 1)
                .when().get("/api/auth/me")
                .then().statusCode(200);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // USER ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════
    @Nested
    class UserEndpointTest {

        @InjectMock
        UserService userService;

        @BeforeEach
        void setup() {
            when(userService.listUsers(anyInt(), anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(
                            new UserDto.ApiResponsePaginationUser("success", "ok", List.of(), null)));
            when(userService.getUser(anyInt()))
                    .thenReturn(Uni.createFrom().item(
                            new UserDto.ApiResponseUser("success", "ok", new UserDto.UserResponse(1, "John", "Doe", "j@e.com", "2024-01-01", "2024-01-01"))));
            when(userService.createUser(any()))
                    .thenReturn(Uni.createFrom().item(
                            new UserDto.ApiResponseUser("success", "created", new UserDto.UserResponse(1, "John", "Doe", "j@e.com", "2024-01-01", "2024-01-01"))));
            when(userService.updateUser(anyInt(), any()))
                    .thenReturn(Uni.createFrom().item(
                            new UserDto.ApiResponseUser("success", "updated", new UserDto.UserResponse(1, "John", "Doe", "j@e.com", "2024-01-01", "2024-01-01"))));
            when(userService.deleteUser(anyInt()))
                    .thenReturn(Uni.createFrom().item(
                            new UserDto.ApiResponseUserDeleteAt("success", "deleted", null)));
        }

        @Test void list_returns200() {
            given().queryParam("page", 1).queryParam("size", 10).queryParam("search", "")
                .when().get("/api/users")
                .then().statusCode(200);
        }

        @Test void getById_returns200() {
            given().when().get("/api/users/1").then().statusCode(200);
        }

        @Test void create_returns201() {
            given().contentType("application/json")
                    .body(Map.of("firstname", "John", "lastname", "Doe",
                            "email", "j@e.com", "password", "p", "confirmPassword", "p"))
                .when().post("/api/users")
                .then().statusCode(201);
        }

        @Test void update_returns200() {
            given().contentType("application/json")
                    .body(Map.of("firstname", "Jane", "lastname", "Doe",
                            "email", "j@e.com", "password", "p", "confirmPassword", "p"))
                .when().put("/api/users/1")
                .then().statusCode(200);
        }

        @Test void delete_returns200() {
            given().when().delete("/api/users/1").then().statusCode(200);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ROLE ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════
    @Nested
    class RoleEndpointTest {

        @InjectMock
        RoleService roleService;

        @BeforeEach
        void setup() {
            when(roleService.listRoles(anyInt(), anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(
                            new RoleDto.ApiResponsePaginationRole("success", "ok", List.of(), null)));
            when(roleService.getRole(anyInt()))
                    .thenReturn(Uni.createFrom().item(
                            new RoleDto.ApiResponseRole("success", "ok", new RoleDto.RoleResponse(1, "ADMIN", "2024-01-01", "2024-01-01"))));
            when(roleService.createRole(any()))
                    .thenReturn(Uni.createFrom().item(
                            new RoleDto.ApiResponseRole("success", "created", new RoleDto.RoleResponse(1, "ADMIN", "2024-01-01", "2024-01-01"))));
            when(roleService.updateRole(anyInt(), any()))
                    .thenReturn(Uni.createFrom().item(
                            new RoleDto.ApiResponseRole("success", "updated", new RoleDto.RoleResponse(1, "ADMIN", "2024-01-01", "2024-01-01"))));
            when(roleService.deleteRole(anyInt()))
                    .thenReturn(Uni.createFrom().item(
                            new RoleDto.ApiResponseRoleDeleteAt("success", "deleted", null)));
        }

        @Test void list_returns200() {
            given().queryParam("page", 1).queryParam("size", 10).queryParam("search", "")
                .when().get("/api/roles")
                .then().statusCode(200);
        }

        @Test void getById_returns200() {
            given().when().get("/api/roles/1").then().statusCode(200);
        }

        @Test void create_returns201() {
            given().contentType("application/json")
                    .body(Map.of("name", "ROLE_ADMIN"))
                .when().post("/api/roles")
                .then().statusCode(201);
        }

        @Test void update_returns200() {
            given().contentType("application/json")
                    .body(Map.of("name", "ROLE_STAFF"))
                .when().put("/api/roles/1")
                .then().statusCode(200);
        }

        @Test void delete_returns200() {
            given().when().delete("/api/roles/1").then().statusCode(200);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CARD ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════
    @Nested
    class CardEndpointTest {

        @InjectMock
        CardService cardService;

        @BeforeEach
        void setup() {
            CardDto.CardResponse cardResp = new CardDto.CardResponse(1, 1, "4111111111111111", "credit", "12/25", "123", "Visa", "2024-01-01", "2024-01-01");
            CardDto.CardResponseMonthlyAmount monthAmt = new CardDto.CardResponseMonthlyAmount("2024-01", 100000);
            CardDto.CardResponseYearlyAmount yearAmt = new CardDto.CardResponseYearlyAmount("2024", 1200000);
            CardDto.CardResponseMonthlyBalance monthBal = new CardDto.CardResponseMonthlyBalance("2024-01", 500000);
            CardDto.CardResponseYearlyBalance yearBal = new CardDto.CardResponseYearlyBalance("2024", 6000000);
            CardDto.CardResponseDashboard dash = new CardDto.CardResponseDashboard(500000, 200000, 50000, 100000, 50000);
            CardDto.CardResponseDashboardCardNumber dashCard = new CardDto.CardResponseDashboardCardNumber(500000, 200000, 50000, 100000, 30000, 20000);

            when(cardService.listCards(anyInt(), anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponsePaginationCard("success", "ok", List.of(), null)));
            when(cardService.findActiveCards(anyInt(), anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponsePaginationCardDeleteAt("success", "ok", List.of(), null)));
            when(cardService.findTrashedCards(anyInt(), anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponsePaginationCardDeleteAt("success", "ok", List.of(), null)));
            when(cardService.getCard(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseCard("success", "ok", cardResp)));
            when(cardService.findCardByUser(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseCard("success", "ok", cardResp)));
            when(cardService.findCardByNumber(anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseCard("success", "ok", cardResp)));
            when(cardService.createCard(any()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseCard("success", "created", cardResp)));
            when(cardService.updateCard(anyInt(), any()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseCard("success", "updated", cardResp)));
            when(cardService.deleteCard(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseCardDeleteAt("success", "deleted", null)));
            when(cardService.deleteCardPermanent(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.SimpleResponse("success", "deleted")));
            when(cardService.restoreCard(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseCardDeleteAt("success", "restored", null)));
            when(cardService.restoreAllCards())
                    .thenReturn(Uni.createFrom().item(new CardDto.SimpleResponse("success", "restored all")));
            when(cardService.deleteAllCards())
                    .thenReturn(Uni.createFrom().item(new CardDto.SimpleResponse("success", "deleted all")));

            when(cardService.findMonthlyBalance(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseMonthlyBalance("success", "ok", List.of(monthBal))));
            when(cardService.findYearlyBalance(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseYearlyBalance("success", "ok", List.of(yearBal))));
            when(cardService.getMonthlyBalanceByCard(anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseMonthlyBalance("success", "ok", List.of(monthBal))));
            when(cardService.getYearlyBalanceByCard(anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseYearlyBalance("success", "ok", List.of(yearBal))));
            when(cardService.findMonthlyTopupAmount(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseMonthlyAmount("success", "ok", List.of(monthAmt))));
            when(cardService.findYearlyTopupAmount(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseYearlyAmount("success", "ok", List.of(yearAmt))));
            when(cardService.getMonthlyTopupAmountByCard(anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseMonthlyAmount("success", "ok", List.of(monthAmt))));
            when(cardService.getYearlyTopupAmountByCard(anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseYearlyAmount("success", "ok", List.of(yearAmt))));
            when(cardService.findMonthlyTransactionAmount(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseMonthlyAmount("success", "ok", List.of(monthAmt))));
            when(cardService.findYearlyTransactionAmount(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseYearlyAmount("success", "ok", List.of(yearAmt))));
            when(cardService.getMonthlyTransactionAmountByCard(anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseMonthlyAmount("success", "ok", List.of(monthAmt))));
            when(cardService.getYearlyTransactionAmountByCard(anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseYearlyAmount("success", "ok", List.of(yearAmt))));
            when(cardService.findMonthlyTransferAmountSender(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseMonthlyAmount("success", "ok", List.of(monthAmt))));
            when(cardService.findMonthlyTransferAmountReceiver(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseMonthlyAmount("success", "ok", List.of(monthAmt))));
            when(cardService.findYearlyTransferAmountSender(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseYearlyAmount("success", "ok", List.of(yearAmt))));
            when(cardService.findYearlyTransferAmountReceiver(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseYearlyAmount("success", "ok", List.of(yearAmt))));
            when(cardService.getMonthlyTransferAmountByCardSender(anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseMonthlyAmount("success", "ok", List.of(monthAmt))));
            when(cardService.getMonthlyTransferAmountByCardReceiver(anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseMonthlyAmount("success", "ok", List.of(monthAmt))));
            when(cardService.getYearlyTransferAmountByCardSender(anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseYearlyAmount("success", "ok", List.of(yearAmt))));
            when(cardService.getYearlyTransferAmountByCardReceiver(anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseYearlyAmount("success", "ok", List.of(yearAmt))));
            when(cardService.findMonthlyWithdrawAmount(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseMonthlyAmount("success", "ok", List.of(monthAmt))));
            when(cardService.findYearlyWithdrawAmount(anyInt()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseYearlyAmount("success", "ok", List.of(yearAmt))));
            when(cardService.getMonthlyWithdrawAmountByCard(anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseMonthlyAmount("success", "ok", List.of(monthAmt))));
            when(cardService.getYearlyWithdrawAmountByCard(anyInt(), anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseYearlyAmount("success", "ok", List.of(yearAmt))));
            when(cardService.findCardDashboard())
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseDashboardCard("success", "ok", dash)));
            when(cardService.findCardDashboardByCardNumber(anyString()))
                    .thenReturn(Uni.createFrom().item(new CardDto.ApiResponseDashboardCardNumber("success", "ok", dashCard)));
        }

        @Test void list_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/cards").then().statusCode(200); }
        @Test void active_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/cards/active").then().statusCode(200); }
        @Test void trashed_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/cards/trashed").then().statusCode(200); }
        @Test void getById_returns200() { given().when().get("/api/cards/1").then().statusCode(200); }
        @Test void byUser_returns200() { given().when().get("/api/cards/by-user/1").then().statusCode(200); }
        @Test void byNumber_returns200() { given().when().get("/api/cards/by-number/4111111111111111").then().statusCode(200); }
        @Test void create_returns201() { given().contentType("application/json").body(Map.of("userId",1,"cardType","credit","expireDate","12/25","cvv","123","cardProvider","Visa")).when().post("/api/cards").then().statusCode(201); }
        @Test void update_returns200() { given().contentType("application/json").body(Map.of("userId",1,"cardType","debit","expireDate","12/26","cvv","456","cardProvider","Mastercard")).when().put("/api/cards/1").then().statusCode(200); }
        @Test void delete_returns200() { given().when().delete("/api/cards/1").then().statusCode(200); }
        @Test void permanent_returns200() { given().when().delete("/api/cards/1/permanent").then().statusCode(200); }
        @Test void restore_returns200() { given().contentType("application/json").when().post("/api/cards/restore/1").then().statusCode(200); }
        @Test void restoreAll_returns200() { given().contentType("application/json").when().post("/api/cards/restore-all").then().statusCode(200); }
        @Test void deleteAll_returns200() { given().contentType("application/json").when().post("/api/cards/delete-all").then().statusCode(200); }
        @Test void statsBalanceMonthly_returns200() { given().queryParam("year",2024).when().get("/api/cards/stats/balance/monthly").then().statusCode(200); }
        @Test void statsBalanceYearly_returns200() { given().queryParam("year",2024).when().get("/api/cards/stats/balance/yearly").then().statusCode(200); }
        @Test void statsBalanceMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/cards/stats/balance/monthly/by-card").then().statusCode(200); }
        @Test void statsBalanceYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/cards/stats/balance/yearly/by-card").then().statusCode(200); }
        @Test void statsTopupMonthly_returns200() { given().queryParam("year",2024).when().get("/api/cards/stats/topup/monthly").then().statusCode(200); }
        @Test void statsTopupYearly_returns200() { given().queryParam("year",2024).when().get("/api/cards/stats/topup/yearly").then().statusCode(200); }
        @Test void statsTopupMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/cards/stats/topup/monthly/by-card").then().statusCode(200); }
        @Test void statsTopupYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/cards/stats/topup/yearly/by-card").then().statusCode(200); }
        @Test void statsTransactionMonthly_returns200() { given().queryParam("year",2024).when().get("/api/cards/stats/transaction/monthly").then().statusCode(200); }
        @Test void statsTransactionYearly_returns200() { given().queryParam("year",2024).when().get("/api/cards/stats/transaction/yearly").then().statusCode(200); }
        @Test void statsTransactionMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/cards/stats/transaction/monthly/by-card").then().statusCode(200); }
        @Test void statsTransactionYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/cards/stats/transaction/yearly/by-card").then().statusCode(200); }
        @Test void statsTransferMonthlySender_returns200() { given().queryParam("year",2024).when().get("/api/cards/stats/transfer/monthly/sender").then().statusCode(200); }
        @Test void statsTransferMonthlyReceiver_returns200() { given().queryParam("year",2024).when().get("/api/cards/stats/transfer/monthly/receiver").then().statusCode(200); }
        @Test void statsTransferYearlySender_returns200() { given().queryParam("year",2024).when().get("/api/cards/stats/transfer/yearly/sender").then().statusCode(200); }
        @Test void statsTransferYearlyReceiver_returns200() { given().queryParam("year",2024).when().get("/api/cards/stats/transfer/yearly/receiver").then().statusCode(200); }
        @Test void statsTransferMonthlyByCardSender_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/cards/stats/transfer/monthly/by-card/sender").then().statusCode(200); }
        @Test void statsTransferMonthlyByCardReceiver_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/cards/stats/transfer/monthly/by-card/receiver").then().statusCode(200); }
        @Test void statsTransferYearlyByCardSender_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/cards/stats/transfer/yearly/by-card/sender").then().statusCode(200); }
        @Test void statsTransferYearlyByCardReceiver_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/cards/stats/transfer/yearly/by-card/receiver").then().statusCode(200); }
        @Test void statsWithdrawMonthly_returns200() { given().queryParam("year",2024).when().get("/api/cards/stats/withdraw/monthly").then().statusCode(200); }
        @Test void statsWithdrawYearly_returns200() { given().queryParam("year",2024).when().get("/api/cards/stats/withdraw/yearly").then().statusCode(200); }
        @Test void statsWithdrawMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/cards/stats/withdraw/monthly/by-card").then().statusCode(200); }
        @Test void statsWithdrawYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/cards/stats/withdraw/yearly/by-card").then().statusCode(200); }
        @Test void dashboard_returns200() { given().when().get("/api/cards/dashboard").then().statusCode(200); }
        @Test void dashboardByCard_returns200() { given().when().get("/api/cards/dashboard/4111111111111111").then().statusCode(200); }
    }

    // ═══════════════════════════════════════════════════════════════════
    // MERCHANT ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════
    @Nested
    class MerchantEndpointTest {

        @InjectMock
        MerchantService merchantService;

        @BeforeEach
        void setup() {
            MerchantDto.MerchantResponse merchant = new MerchantDto.MerchantResponse(1, "M1", "ak", "active", 1, "2024-01-01", "2024-01-01");
            MerchantDto.MerchantResponseMonthlyAmount mAmt = new MerchantDto.MerchantResponseMonthlyAmount("2024-01", 100000);
            MerchantDto.MerchantResponseYearlyAmount yAmt = new MerchantDto.MerchantResponseYearlyAmount("2024", 1200000);
            MerchantDto.MerchantResponseMonthlyPaymentMethod mMethod = new MerchantDto.MerchantResponseMonthlyPaymentMethod("2024-01", "credit_card", 100000);
            MerchantDto.MerchantResponseYearlyPaymentMethod yMethod = new MerchantDto.MerchantResponseYearlyPaymentMethod("2024", "credit_card", 1200000);
            MerchantDto.MerchantResponseMonthlyTotalAmount mTotal = new MerchantDto.MerchantResponseMonthlyTotalAmount("2024-01", "2024", 100000);
            MerchantDto.MerchantResponseYearlyTotalAmount yTotal = new MerchantDto.MerchantResponseYearlyTotalAmount("2024", 1200000);

            when(merchantService.listMerchants(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponsePaginationMerchant("success", "ok", List.of(), null)));
            when(merchantService.getMerchant(anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchant("success", "ok", merchant)));
            when(merchantService.createMerchant(any())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchant("success", "created", merchant)));
            when(merchantService.updateMerchant(anyInt(), any())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchant("success", "updated", merchant)));
            when(merchantService.deleteMerchant(anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantDeleteAt("success", "deleted", null)));
            when(merchantService.findAllTransactions(anyInt(), anyInt(), anyString(), anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponsePaginationMerchantTransaction("success", "ok", List.of(), null)));
            when(merchantService.findTransactionsById(anyInt(), anyInt(), anyString(), anyString())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponsePaginationMerchantTransaction("success", "ok", List.of(), null)));
            when(merchantService.findTransactionsByApiKey(anyInt(), anyInt(), anyString(), anyString())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponsePaginationMerchantTransaction("success", "ok", List.of(), null)));
            when(merchantService.getMonthlyAmount(anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantMonthlyAmount("success", "ok", List.of(mAmt))));
            when(merchantService.getYearlyAmount(anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantYearlyAmount("success", "ok", List.of(yAmt))));
            when(merchantService.getMonthlyAmountById(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantMonthlyAmount("success", "ok", List.of(mAmt))));
            when(merchantService.getYearlyAmountById(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantYearlyAmount("success", "ok", List.of(yAmt))));
            when(merchantService.getMonthlyAmountByApiKey(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantMonthlyAmount("success", "ok", List.of(mAmt))));
            when(merchantService.getYearlyAmountByApiKey(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantYearlyAmount("success", "ok", List.of(yAmt))));
            when(merchantService.getMonthlyMethod(anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantMonthlyPaymentMethod("success", "ok", List.of(mMethod))));
            when(merchantService.getYearlyMethod(anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantYearlyPaymentMethod("success", "ok", List.of(yMethod))));
            when(merchantService.getMonthlyMethodById(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantMonthlyPaymentMethod("success", "ok", List.of(mMethod))));
            when(merchantService.getYearlyMethodById(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantYearlyPaymentMethod("success", "ok", List.of(yMethod))));
            when(merchantService.getMonthlyMethodByApiKey(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantMonthlyPaymentMethod("success", "ok", List.of(mMethod))));
            when(merchantService.getYearlyMethodByApiKey(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantYearlyPaymentMethod("success", "ok", List.of(yMethod))));
            when(merchantService.getMonthlyTotalAmount(anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantMonthlyTotalAmount("success", "ok", List.of(mTotal))));
            when(merchantService.getYearlyTotalAmount(anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantYearlyTotalAmount("success", "ok", List.of(yTotal))));
            when(merchantService.getMonthlyTotalAmountById(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantMonthlyTotalAmount("success", "ok", List.of(mTotal))));
            when(merchantService.getYearlyTotalAmountById(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantYearlyTotalAmount("success", "ok", List.of(yTotal))));
            when(merchantService.getMonthlyTotalAmountByApiKey(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantMonthlyTotalAmount("success", "ok", List.of(mTotal))));
            when(merchantService.getYearlyTotalAmountByApiKey(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new MerchantDto.ApiResponseMerchantYearlyTotalAmount("success", "ok", List.of(yTotal))));
        }

        @Test void list_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/merchants").then().statusCode(200); }
        @Test void getById_returns200() { given().when().get("/api/merchants/1").then().statusCode(200); }
        @Test void create_returns201() { given().contentType("application/json").body(Map.of("name","M1","userId",1)).when().post("/api/merchants").then().statusCode(201); }
        @Test void update_returns200() { given().contentType("application/json").body(Map.of("name","M2","userId",1,"status","active")).when().put("/api/merchants/1").then().statusCode(200); }
        @Test void delete_returns200() { given().when().delete("/api/merchants/1").then().statusCode(200); }
        @Test void transactions_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").queryParam("merchantId",1).when().get("/api/merchants/transactions").then().statusCode(200); }
        @Test void transactionsById_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").queryParam("id","1").when().get("/api/merchants/transactions/by-id").then().statusCode(200); }
        @Test void transactionsByApiKey_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").queryParam("apiKey","ak").when().get("/api/merchants/transactions/by-api-key").then().statusCode(200); }
        @Test void statsAmountMonthly_returns200() { given().queryParam("year",2024).when().get("/api/merchants/stats/amount/monthly").then().statusCode(200); }
        @Test void statsAmountYearly_returns200() { given().queryParam("year",2024).when().get("/api/merchants/stats/amount/yearly").then().statusCode(200); }
        @Test void statsAmountById_returns200() { given().queryParam("year",2024).queryParam("merchantId",1).when().get("/api/merchants/stats/amount/by-id").then().statusCode(200); }
        @Test void statsAmountYearlyById_returns200() { given().queryParam("year",2024).queryParam("merchantId",1).when().get("/api/merchants/stats/amount/yearly/by-id").then().statusCode(200); }
        @Test void statsAmountByApiKey_returns200() { given().queryParam("year",2024).queryParam("apiKey","ak").when().get("/api/merchants/stats/amount/by-api-key").then().statusCode(200); }
        @Test void statsAmountYearlyByApiKey_returns200() { given().queryParam("year",2024).queryParam("apiKey","ak").when().get("/api/merchants/stats/amount/yearly/by-api-key").then().statusCode(200); }
        @Test void statsMethodMonthly_returns200() { given().queryParam("year",2024).when().get("/api/merchants/stats/method/monthly").then().statusCode(200); }
        @Test void statsMethodYearly_returns200() { given().queryParam("year",2024).when().get("/api/merchants/stats/method/yearly").then().statusCode(200); }
        @Test void statsMethodById_returns200() { given().queryParam("year",2024).queryParam("merchantId",1).when().get("/api/merchants/stats/method/monthly/by-id").then().statusCode(200); }
        @Test void statsMethodYearlyById_returns200() { given().queryParam("year",2024).queryParam("merchantId",1).when().get("/api/merchants/stats/method/yearly/by-id").then().statusCode(200); }
        @Test void statsMethodByApiKey_returns200() { given().queryParam("year",2024).queryParam("apiKey","ak").when().get("/api/merchants/stats/method/monthly/by-api-key").then().statusCode(200); }
        @Test void statsMethodYearlyByApiKey_returns200() { given().queryParam("year",2024).queryParam("apiKey","ak").when().get("/api/merchants/stats/method/yearly/by-api-key").then().statusCode(200); }
        @Test void statsTotalAmountMonthly_returns200() { given().queryParam("year",2024).when().get("/api/merchants/stats/total-amount/monthly").then().statusCode(200); }
        @Test void statsTotalAmountYearly_returns200() { given().queryParam("year",2024).when().get("/api/merchants/stats/total-amount/yearly").then().statusCode(200); }
        @Test void statsTotalAmountById_returns200() { given().queryParam("year",2024).queryParam("merchantId",1).when().get("/api/merchants/stats/total-amount/monthly/by-id").then().statusCode(200); }
        @Test void statsTotalAmountYearlyById_returns200() { given().queryParam("year",2024).queryParam("merchantId",1).when().get("/api/merchants/stats/total-amount/yearly/by-id").then().statusCode(200); }
        @Test void statsTotalAmountByApiKey_returns200() { given().queryParam("year",2024).queryParam("apiKey","ak").when().get("/api/merchants/stats/total-amount/monthly/by-api-key").then().statusCode(200); }
        @Test void statsTotalAmountYearlyByApiKey_returns200() { given().queryParam("year",2024).queryParam("apiKey","ak").when().get("/api/merchants/stats/total-amount/yearly/by-api-key").then().statusCode(200); }
    }

    // ═══════════════════════════════════════════════════════════════════
    // MERCHANT DOCUMENT ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════
    @Nested
    class MerchantDocumentEndpointTest {

        @InjectMock
        MerchantDocumentService merchantDocumentService;

        @BeforeEach
        void setup() {
            MerchantDocumentDto.MerchantDocument doc = new MerchantDocumentDto.MerchantDocument(1, 1, "license", "http://e.com/doc.pdf", "pending", "", "2024-01-01", "2024-01-01");
            when(merchantDocumentService.listMerchantDocuments(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new MerchantDocumentDto.ApiResponsePaginationMerchantDocument("success", "ok", List.of(), null)));
            when(merchantDocumentService.listActiveMerchantDocuments(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new MerchantDocumentDto.ApiResponsePaginationMerchantDocumentAt("success", "ok", List.of(), null)));
            when(merchantDocumentService.listTrashedMerchantDocuments(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new MerchantDocumentDto.ApiResponsePaginationMerchantDocumentAt("success", "ok", List.of(), null)));
            when(merchantDocumentService.getMerchantDocument(anyInt())).thenReturn(Uni.createFrom().item(new MerchantDocumentDto.ApiResponseMerchantDocument("success", "ok", doc)));
            when(merchantDocumentService.createMerchantDocument(any())).thenReturn(Uni.createFrom().item(new MerchantDocumentDto.ApiResponseMerchantDocument("success", "created", doc)));
            when(merchantDocumentService.updateMerchantDocument(anyInt(), any())).thenReturn(Uni.createFrom().item(new MerchantDocumentDto.ApiResponseMerchantDocument("success", "updated", doc)));
            when(merchantDocumentService.updateMerchantDocumentStatus(anyInt(), any())).thenReturn(Uni.createFrom().item(new MerchantDocumentDto.ApiResponseMerchantDocument("success", "updated", doc)));
            when(merchantDocumentService.deleteMerchantDocument(anyInt())).thenReturn(Uni.createFrom().item(new MerchantDocumentDto.ApiResponseMerchantDocumentDeleteAt("success", "deleted", null)));
            when(merchantDocumentService.trashMerchantDocument(anyInt())).thenReturn(Uni.createFrom().item(new MerchantDocumentDto.ApiResponseMerchantDocumentDeleteAt("success", "trashed", null)));
            when(merchantDocumentService.restoreMerchantDocument(anyInt())).thenReturn(Uni.createFrom().item(new MerchantDocumentDto.ApiResponseMerchantDocumentDeleteAt("success", "restored", null)));
            when(merchantDocumentService.deleteMerchantDocumentPermanent(anyInt())).thenReturn(Uni.createFrom().item(new MerchantDocumentDto.SimpleResponse("success", "deleted")));
            when(merchantDocumentService.restoreAllMerchantDocuments()).thenReturn(Uni.createFrom().item(new MerchantDocumentDto.SimpleResponse("success", "restored all")));
            when(merchantDocumentService.deleteAllMerchantDocuments()).thenReturn(Uni.createFrom().item(new MerchantDocumentDto.SimpleResponse("success", "deleted all")));
        }

        @Test void list_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/merchant-documents").then().statusCode(200); }
        @Test void active_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/merchant-documents/active").then().statusCode(200); }
        @Test void trashed_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/merchant-documents/trashed").then().statusCode(200); }
        @Test void getById_returns200() { given().when().get("/api/merchant-documents/1").then().statusCode(200); }
        @Test void create_returns201() { given().contentType("application/json").body(Map.of("merchantId",1,"documentType","license","documentUrl","http://e.com/d.pdf")).when().post("/api/merchant-documents").then().statusCode(201); }
        @Test void update_returns200() { given().contentType("application/json").body(Map.of("merchantId",1,"documentType","license","documentUrl","http://e.com/d.pdf","note","","status","pending")).when().put("/api/merchant-documents/1").then().statusCode(200); }
        @Test void updateStatus_returns200() { given().contentType("application/json").body(Map.of("merchantId",1,"note","","status","approved")).when().put("/api/merchant-documents/1/status").then().statusCode(200); }
        @Test void delete_returns200() { given().when().delete("/api/merchant-documents/1").then().statusCode(200); }
        @Test void trash_returns200() { given().contentType("application/json").when().post("/api/merchant-documents/trash/1").then().statusCode(200); }
        @Test void restore_returns200() { given().contentType("application/json").when().post("/api/merchant-documents/restore/1").then().statusCode(200); }
        @Test void permanent_returns200() { given().when().delete("/api/merchant-documents/1/permanent").then().statusCode(200); }
        @Test void restoreAll_returns200() { given().contentType("application/json").when().post("/api/merchant-documents/restore-all").then().statusCode(200); }
        @Test void deleteAll_returns200() { given().contentType("application/json").when().post("/api/merchant-documents/delete-all").then().statusCode(200); }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SALDO ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════
    @Nested
    class SaldoEndpointTest {

        @InjectMock
        SaldoService saldoService;

        @BeforeEach
        void setup() {
            SaldoDto.SaldoResponse saldo = new SaldoDto.SaldoResponse(1, "4111111111111111", 100000, "", 0, "2024-01-01", "2024-01-01");
            SaldoDto.SaldoMonthBalanceResponse mBal = new SaldoDto.SaldoMonthBalanceResponse("2024-01", 500000);
            SaldoDto.SaldoYearBalanceResponse yBal = new SaldoDto.SaldoYearBalanceResponse("2024", 6000000);
            SaldoDto.SaldoMonthTotalBalanceResponse mTotal = new SaldoDto.SaldoMonthTotalBalanceResponse("2024-01", "2024", 500000);
            SaldoDto.SaldoYearTotalBalanceResponse yTotal = new SaldoDto.SaldoYearTotalBalanceResponse("2024", 6000000);

            when(saldoService.listSaldos(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new SaldoDto.ApiResponsePaginationSaldo("success", "ok", List.of(), null)));
            when(saldoService.getSaldo(anyInt())).thenReturn(Uni.createFrom().item(new SaldoDto.ApiResponseSaldo("success", "ok", saldo)));
            when(saldoService.createSaldo(any())).thenReturn(Uni.createFrom().item(new SaldoDto.ApiResponseSaldo("success", "created", saldo)));
            when(saldoService.updateSaldo(anyInt(), any())).thenReturn(Uni.createFrom().item(new SaldoDto.ApiResponseSaldo("success", "updated", saldo)));
            when(saldoService.deleteSaldo(anyInt())).thenReturn(Uni.createFrom().item(new SaldoDto.ApiResponseSaldoDeleteAt("success", "deleted", null)));
            when(saldoService.findMonthlySaldoBalances(anyInt())).thenReturn(Uni.createFrom().item(new SaldoDto.ApiResponseMonthSaldoBalances("success", "ok", List.of(mBal))));
            when(saldoService.findYearlySaldoBalances(anyInt())).thenReturn(Uni.createFrom().item(new SaldoDto.ApiResponseYearSaldoBalances("success", "ok", List.of(yBal))));
            when(saldoService.findMonthlyTotalSaldoBalance(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new SaldoDto.ApiResponseMonthTotalSaldo("success", "ok", List.of(mTotal))));
            when(saldoService.findYearTotalSaldoBalance(anyInt())).thenReturn(Uni.createFrom().item(new SaldoDto.ApiResponseYearTotalSaldo("success", "ok", List.of(yTotal))));
        }

        @Test void list_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/saldos").then().statusCode(200); }
        @Test void getById_returns200() { given().when().get("/api/saldos/1").then().statusCode(200); }
        @Test void create_returns201() { given().contentType("application/json").body(Map.of("cardNumber","4111111111111111","totalBalance",100000)).when().post("/api/saldos").then().statusCode(201); }
        @Test void update_returns200() { given().contentType("application/json").body(Map.of("cardNumber","4111111111111111","totalBalance",200000)).when().put("/api/saldos/1").then().statusCode(200); }
        @Test void delete_returns200() { given().when().delete("/api/saldos/1").then().statusCode(200); }
        @Test void statsBalanceMonthly_returns200() { given().queryParam("year",2024).when().get("/api/saldos/stats/balance/monthly").then().statusCode(200); }
        @Test void statsBalanceYearly_returns200() { given().queryParam("year",2024).when().get("/api/saldos/stats/balance/yearly").then().statusCode(200); }
        @Test void statsTotalMonthly_returns200() { given().queryParam("year",2024).queryParam("month",1).when().get("/api/saldos/stats/total/monthly").then().statusCode(200); }
        @Test void statsTotalYearly_returns200() { given().queryParam("year",2024).when().get("/api/saldos/stats/total/yearly").then().statusCode(200); }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TOPUP ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════
    @Nested
    class TopupEndpointTest {

        @InjectMock
        TopupService topupService;

        @BeforeEach
        void setup() {
            TopupDto.TopupResponse topup = new TopupDto.TopupResponse(1, "4111111111111111", "TU001", 50000, "bank_transfer", "2024-01-01", "2024-01-01", "2024-01-01");
            TopupDto.TopupMonthAmountResponse mAmt = new TopupDto.TopupMonthAmountResponse("2024-01", 50000);
            TopupDto.TopupYearlyAmountResponse yAmt = new TopupDto.TopupYearlyAmountResponse("2024", 600000);
            TopupDto.TopupMonthMethodResponse mMethod = new TopupDto.TopupMonthMethodResponse("2024-01", "bank_transfer", 10, 50000);
            TopupDto.TopupYearlyMethodResponse yMethod = new TopupDto.TopupYearlyMethodResponse("2024", "bank_transfer", 120, 600000);
            TopupDto.TopupMonthStatusSuccessResponse mSucc = new TopupDto.TopupMonthStatusSuccessResponse("2024", "2024-01", 10, 500000);
            TopupDto.TopupYearStatusSuccessResponse ySucc = new TopupDto.TopupYearStatusSuccessResponse("2024", 120, 6000000);
            TopupDto.TopupMonthStatusFailedResponse mFail = new TopupDto.TopupMonthStatusFailedResponse("2024", "2024-01", 2, 100000);
            TopupDto.TopupYearStatusFailedResponse yFail = new TopupDto.TopupYearStatusFailedResponse("2024", 24, 1200000);

            when(topupService.listTopups(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponsePaginationTopup("success", "ok", List.of(), null)));
            when(topupService.listTopupsByCard(anyString(), anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponsePaginationTopup("success", "ok", List.of(), null)));
            when(topupService.findActiveTopups(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponsePaginationTopupDeleteAt("success", "ok", List.of(), null)));
            when(topupService.findTrashedTopups(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponsePaginationTopupDeleteAt("success", "ok", List.of(), null)));
            when(topupService.getTopup(anyInt())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopup("success", "ok", topup)));
            when(topupService.getTopupByCard(anyString(), anyInt())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopup("success", "ok", topup)));
            when(topupService.createTopup(any())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopup("success", "created", topup)));
            when(topupService.updateTopup(anyInt(), any())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopup("success", "updated", topup)));
            when(topupService.deleteTopupPermanent(anyInt())).thenReturn(Uni.createFrom().item(new TopupDto.SimpleResponse("success", "deleted")));
            when(topupService.trashTopup(anyInt())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupDeleteAt("success", "trashed", null)));
            when(topupService.restoreTopup(anyInt())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupDeleteAt("success", "restored", null)));
            when(topupService.restoreAllTopups()).thenReturn(Uni.createFrom().item(new TopupDto.SimpleResponse("success", "restored all")));
            when(topupService.deleteAllTopups()).thenReturn(Uni.createFrom().item(new TopupDto.SimpleResponse("success", "deleted all")));
            when(topupService.getMonthlyAmounts(anyInt())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupMonthAmount("success", "ok", List.of(mAmt))));
            when(topupService.getYearlyAmounts(anyInt())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupYearAmount("success", "ok", List.of(yAmt))));
            when(topupService.getMonthlyAmountsByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupMonthAmount("success", "ok", List.of(mAmt))));
            when(topupService.getYearlyAmountsByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupYearAmount("success", "ok", List.of(yAmt))));
            when(topupService.getMonthlyMethods(anyInt())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupMonthMethod("success", "ok", List.of(mMethod))));
            when(topupService.getYearlyMethods(anyInt())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupYearMethod("success", "ok", List.of(yMethod))));
            when(topupService.getMonthlyMethodsByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupMonthMethod("success", "ok", List.of(mMethod))));
            when(topupService.getYearlyMethodsByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupYearMethod("success", "ok", List.of(yMethod))));
            when(topupService.getMonthlyStatusSuccess(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupMonthStatusSuccess("success", "ok", List.of(mSucc))));
            when(topupService.getYearlyStatusSuccess(anyInt())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupYearStatusSuccess("success", "ok", List.of(ySucc))));
            when(topupService.getMonthlyStatusFailed(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupMonthStatusFailed("success", "ok", List.of(mFail))));
            when(topupService.getYearlyStatusFailed(anyInt())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupYearStatusFailed("success", "ok", List.of(yFail))));
            when(topupService.getMonthlyStatusSuccessByCard(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupMonthStatusSuccess("success", "ok", List.of(mSucc))));
            when(topupService.getYearlyStatusSuccessByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupYearStatusSuccess("success", "ok", List.of(ySucc))));
            when(topupService.getMonthlyStatusFailedByCard(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupMonthStatusFailed("success", "ok", List.of(mFail))));
            when(topupService.getYearlyStatusFailedByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TopupDto.ApiResponseTopupYearStatusFailed("success", "ok", List.of(yFail))));
        }

        @Test void list_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/topups").then().statusCode(200); }
        @Test void byCard_returns200() { given().queryParam("cardNumber","4111111111111111").queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/topups/by-card").then().statusCode(200); }
        @Test void active_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/topups/active").then().statusCode(200); }
        @Test void trashed_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/topups/trashed").then().statusCode(200); }
        @Test void getById_returns200() { given().when().get("/api/topups/1").then().statusCode(200); }
        @Test void getByCard_returns200() { given().queryParam("year",2024).when().get("/api/topups/card/4111111111111111").then().statusCode(200); }
        @Test void create_returns201() { given().contentType("application/json").body(Map.of("cardNumber","4111111111111111","topupAmount",50000,"topupMethod","bank_transfer")).when().post("/api/topups").then().statusCode(201); }
        @Test void update_returns200() { given().contentType("application/json").body(Map.of("cardNumber","4111111111111111","topupAmount",60000,"topupMethod","bank_transfer")).when().put("/api/topups/1").then().statusCode(200); }
        @Test void permanent_returns200() { given().when().delete("/api/topups/1/permanent").then().statusCode(200); }
        @Test void trash_returns200() { given().contentType("application/json").when().post("/api/topups/trash/1").then().statusCode(200); }
        @Test void restore_returns200() { given().contentType("application/json").when().post("/api/topups/restore/1").then().statusCode(200); }
        @Test void restoreAll_returns200() { given().contentType("application/json").when().post("/api/topups/restore-all").then().statusCode(200); }
        @Test void deleteAll_returns200() { given().contentType("application/json").when().post("/api/topups/delete-all").then().statusCode(200); }
        @Test void statsAmountMonthly_returns200() { given().queryParam("year",2024).when().get("/api/topups/stats/amount/monthly").then().statusCode(200); }
        @Test void statsAmountYearly_returns200() { given().queryParam("year",2024).when().get("/api/topups/stats/amount/yearly").then().statusCode(200); }
        @Test void statsAmountMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/topups/stats/amount/monthly/by-card").then().statusCode(200); }
        @Test void statsAmountYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/topups/stats/amount/yearly/by-card").then().statusCode(200); }
        @Test void statsMethodMonthly_returns200() { given().queryParam("year",2024).when().get("/api/topups/stats/method/monthly").then().statusCode(200); }
        @Test void statsMethodYearly_returns200() { given().queryParam("year",2024).when().get("/api/topups/stats/method/yearly").then().statusCode(200); }
        @Test void statsMethodMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/topups/stats/method/monthly/by-card").then().statusCode(200); }
        @Test void statsMethodYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/topups/stats/method/yearly/by-card").then().statusCode(200); }
        @Test void statsStatusSuccessMonthly_returns200() { given().queryParam("year",2024).queryParam("month",1).when().get("/api/topups/stats/status/monthly/success").then().statusCode(200); }
        @Test void statsStatusSuccessYearly_returns200() { given().queryParam("year",2024).when().get("/api/topups/stats/status/yearly/success").then().statusCode(200); }
        @Test void statsStatusFailedMonthly_returns200() { given().queryParam("year",2024).queryParam("month",1).when().get("/api/topups/stats/status/monthly/failed").then().statusCode(200); }
        @Test void statsStatusFailedYearly_returns200() { given().queryParam("year",2024).when().get("/api/topups/stats/status/yearly/failed").then().statusCode(200); }
        @Test void statsStatusSuccessMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("month",1).queryParam("cardNumber","4111111111111111").when().get("/api/topups/stats/status/monthly/success/by-card").then().statusCode(200); }
        @Test void statsStatusSuccessYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/topups/stats/status/yearly/success/by-card").then().statusCode(200); }
        @Test void statsStatusFailedMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("month",1).queryParam("cardNumber","4111111111111111").when().get("/api/topups/stats/status/monthly/failed/by-card").then().statusCode(200); }
        @Test void statsStatusFailedYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/topups/stats/status/yearly/failed/by-card").then().statusCode(200); }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TRANSACTION ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════
    @Nested
    class TransactionEndpointTest {

        @InjectMock
        TransactionService transactionService;

        @BeforeEach
        void setup() {
            TransactionDto.TransactionResponse tx = new TransactionDto.TransactionResponse(1, "4111111111111111", "TX001", 25000, "credit_card", 1, "2024-01-01", "2024-01-01", "2024-01-01");
            TransactionDto.TransactionMonthAmountResponse mAmt = new TransactionDto.TransactionMonthAmountResponse("2024-01", 25000);
            TransactionDto.TransactionYearlyAmountResponse yAmt = new TransactionDto.TransactionYearlyAmountResponse("2024", 300000);
            TransactionDto.TransactionMonthMethodResponse mMethod = new TransactionDto.TransactionMonthMethodResponse("2024-01", "credit_card", 10, 25000);
            TransactionDto.TransactionYearMethodResponse yMethod = new TransactionDto.TransactionYearMethodResponse("2024", "credit_card", 120, 300000);
            TransactionDto.TransactionMonthStatusSuccessResponse mSucc = new TransactionDto.TransactionMonthStatusSuccessResponse("2024", "2024-01", 10, 250000);
            TransactionDto.TransactionYearStatusSuccessResponse ySucc = new TransactionDto.TransactionYearStatusSuccessResponse("2024", 120, 3000000);
            TransactionDto.TransactionMonthStatusFailedResponse mFail = new TransactionDto.TransactionMonthStatusFailedResponse("2024", "2024-01", 2, 50000);
            TransactionDto.TransactionYearStatusFailedResponse yFail = new TransactionDto.TransactionYearStatusFailedResponse("2024", 24, 600000);

            when(transactionService.listTransactions(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponsePaginationTransaction("success", "ok", List.of(), null)));
            when(transactionService.listTransactionsByCard(anyString(), anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponsePaginationTransaction("success", "ok", List.of(), null)));
            when(transactionService.findActiveTransactions(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponsePaginationTransactionDeleteAt("success", "ok", List.of(), null)));
            when(transactionService.findTrashedTransactions(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponsePaginationTransactionDeleteAt("success", "ok", List.of(), null)));
            when(transactionService.getTransaction(anyInt())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransaction("success", "ok", tx)));
            when(transactionService.findByMerchantId(anyInt())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactions("success", "ok", List.of())));
            when(transactionService.createTransaction(any())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransaction("success", "created", tx)));
            when(transactionService.updateTransaction(anyInt(), any())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransaction("success", "updated", tx)));
            when(transactionService.deleteTransaction(anyInt())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionDeleteAt("success", "deleted", null)));
            when(transactionService.deleteTransactionPermanent(anyInt())).thenReturn(Uni.createFrom().item(new TransactionDto.SimpleResponse("success", "deleted")));
            when(transactionService.trashTransaction(anyInt())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionDeleteAt("success", "trashed", null)));
            when(transactionService.restoreTransaction(anyInt())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionDeleteAt("success", "restored", null)));
            when(transactionService.restoreAllTransactions()).thenReturn(Uni.createFrom().item(new TransactionDto.SimpleResponse("success", "restored all")));
            when(transactionService.deleteAllTransactions()).thenReturn(Uni.createFrom().item(new TransactionDto.SimpleResponse("success", "deleted all")));
            when(transactionService.findMonthlyAmounts(anyInt())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionMonthAmount("success", "ok", List.of(mAmt))));
            when(transactionService.findYearlyAmounts(anyInt())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionYearAmount("success", "ok", List.of(yAmt))));
            when(transactionService.findMonthlyAmountsByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionMonthAmount("success", "ok", List.of(mAmt))));
            when(transactionService.findYearlyAmountsByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionYearAmount("success", "ok", List.of(yAmt))));
            when(transactionService.findMonthlyMethods(anyInt())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionMonthMethod("success", "ok", List.of(mMethod))));
            when(transactionService.findYearlyMethods(anyInt())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionYearMethod("success", "ok", List.of(yMethod))));
            when(transactionService.findMonthlyMethodsByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionMonthMethod("success", "ok", List.of(mMethod))));
            when(transactionService.findYearlyMethodsByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionYearMethod("success", "ok", List.of(yMethod))));
            when(transactionService.findMonthlyStatusSuccess(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionMonthStatusSuccess("success", "ok", List.of(mSucc))));
            when(transactionService.findYearlyStatusSuccess(anyInt())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionYearStatusSuccess("success", "ok", List.of(ySucc))));
            when(transactionService.findMonthlyStatusFailed(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionMonthStatusFailed("success", "ok", List.of(mFail))));
            when(transactionService.findYearlyStatusFailed(anyInt())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionYearStatusFailed("success", "ok", List.of(yFail))));
            when(transactionService.findMonthlyStatusSuccessByCard(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionMonthStatusSuccess("success", "ok", List.of(mSucc))));
            when(transactionService.findYearlyStatusSuccessByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionYearStatusSuccess("success", "ok", List.of(ySucc))));
            when(transactionService.findMonthlyStatusFailedByCard(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionMonthStatusFailed("success", "ok", List.of(mFail))));
            when(transactionService.findYearlyStatusFailedByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransactionDto.ApiResponseTransactionYearStatusFailed("success", "ok", List.of(yFail))));
        }

        @Test void list_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/transactions").then().statusCode(200); }
        @Test void byCard_returns200() { given().queryParam("cardNumber","4111111111111111").queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/transactions/by-card").then().statusCode(200); }
        @Test void active_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/transactions/active").then().statusCode(200); }
        @Test void trashed_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/transactions/trashed").then().statusCode(200); }
        @Test void getById_returns200() { given().when().get("/api/transactions/1").then().statusCode(200); }
        @Test void byMerchant_returns200() { given().when().get("/api/transactions/merchant/1").then().statusCode(200); }
        @Test void create_returns201() { given().contentType("application/json").body(Map.of("apiKey","ak","cardNumber","4111111111111111","amount",25000,"paymentMethod","credit_card","merchantId",1)).when().post("/api/transactions").then().statusCode(201); }
        @Test void update_returns200() { given().contentType("application/json").body(Map.of("apiKey","ak","cardNumber","4111111111111111","amount",30000,"paymentMethod","credit_card","merchantId",1)).when().put("/api/transactions/1").then().statusCode(200); }
        @Test void delete_returns200() { given().when().delete("/api/transactions/1").then().statusCode(200); }
        @Test void permanent_returns200() { given().when().delete("/api/transactions/1/permanent").then().statusCode(200); }
        @Test void trash_returns200() { given().contentType("application/json").when().post("/api/transactions/trash/1").then().statusCode(200); }
        @Test void restore_returns200() { given().contentType("application/json").when().post("/api/transactions/restore/1").then().statusCode(200); }
        @Test void restoreAll_returns200() { given().contentType("application/json").when().post("/api/transactions/restore-all").then().statusCode(200); }
        @Test void deleteAll_returns200() { given().contentType("application/json").when().post("/api/transactions/delete-all").then().statusCode(200); }
        @Test void statsAmountMonthly_returns200() { given().queryParam("year",2024).when().get("/api/transactions/stats/amount/monthly").then().statusCode(200); }
        @Test void statsAmountYearly_returns200() { given().queryParam("year",2024).when().get("/api/transactions/stats/amount/yearly").then().statusCode(200); }
        @Test void statsAmountMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/transactions/stats/amount/monthly/by-card").then().statusCode(200); }
        @Test void statsAmountYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/transactions/stats/amount/yearly/by-card").then().statusCode(200); }
        @Test void statsMethodMonthly_returns200() { given().queryParam("year",2024).when().get("/api/transactions/stats/method/monthly").then().statusCode(200); }
        @Test void statsMethodYearly_returns200() { given().queryParam("year",2024).when().get("/api/transactions/stats/method/yearly").then().statusCode(200); }
        @Test void statsMethodMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/transactions/stats/method/monthly/by-card").then().statusCode(200); }
        @Test void statsMethodYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/transactions/stats/method/yearly/by-card").then().statusCode(200); }
        @Test void statsStatusSuccessMonthly_returns200() { given().queryParam("year",2024).queryParam("month",1).when().get("/api/transactions/stats/status/monthly/success").then().statusCode(200); }
        @Test void statsStatusSuccessYearly_returns200() { given().queryParam("year",2024).when().get("/api/transactions/stats/status/yearly/success").then().statusCode(200); }
        @Test void statsStatusFailedMonthly_returns200() { given().queryParam("year",2024).queryParam("month",1).when().get("/api/transactions/stats/status/monthly/failed").then().statusCode(200); }
        @Test void statsStatusFailedYearly_returns200() { given().queryParam("year",2024).when().get("/api/transactions/stats/status/yearly/failed").then().statusCode(200); }
        @Test void statsStatusSuccessMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("month",1).queryParam("cardNumber","4111111111111111").when().get("/api/transactions/stats/status/monthly/success/by-card").then().statusCode(200); }
        @Test void statsStatusSuccessYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/transactions/stats/status/yearly/success/by-card").then().statusCode(200); }
        @Test void statsStatusFailedMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("month",1).queryParam("cardNumber","4111111111111111").when().get("/api/transactions/stats/status/monthly/failed/by-card").then().statusCode(200); }
        @Test void statsStatusFailedYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/transactions/stats/status/yearly/failed/by-card").then().statusCode(200); }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TRANSFER ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════
    @Nested
    class TransferEndpointTest {

        @InjectMock
        TransferService transferService;

        @BeforeEach
        void setup() {
            TransferDto.TransferResponse tf = new TransferDto.TransferResponse(1, "TF001", "4111111111111111", "4222222222222222", 30000, "2024-01-01", "2024-01-01", "2024-01-01");
            TransferDto.TransferMonthAmountResponse mAmt = new TransferDto.TransferMonthAmountResponse("2024-01", 30000);
            TransferDto.TransferYearAmountResponse yAmt = new TransferDto.TransferYearAmountResponse("2024", 360000);
            TransferDto.TransferMonthStatusSuccessResponse mSucc = new TransferDto.TransferMonthStatusSuccessResponse("2024", "2024-01", 10, 300000);
            TransferDto.TransferYearStatusSuccessResponse ySucc = new TransferDto.TransferYearStatusSuccessResponse("2024", 120, 3600000);
            TransferDto.TransferMonthStatusFailedResponse mFail = new TransferDto.TransferMonthStatusFailedResponse("2024", "2024-01", 2, 60000);
            TransferDto.TransferYearStatusFailedResponse yFail = new TransferDto.TransferYearStatusFailedResponse("2024", 24, 720000);

            when(transferService.listTransfers(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponsePaginationTransfer("success", "ok", List.of(), null)));
            when(transferService.findActiveTransfers(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponsePaginationTransferDeleteAt("success", "ok", List.of(), null)));
            when(transferService.findTrashedTransfers(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponsePaginationTransferDeleteAt("success", "ok", List.of(), null)));
            when(transferService.getTransfer(anyInt())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransfer("success", "ok", tf)));
            when(transferService.findTransfersFrom(anyString())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransfers("success", "ok", List.of())));
            when(transferService.findTransfersTo(anyString())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransfers("success", "ok", List.of())));
            when(transferService.createTransfer(any())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransfer("success", "created", tf)));
            when(transferService.updateTransfer(anyInt(), any())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransfer("success", "updated", tf)));
            when(transferService.deleteTransfer(anyInt())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferDeleteAt("success", "deleted", null)));
            when(transferService.deleteTransferPermanent(anyInt())).thenReturn(Uni.createFrom().item(new TransferDto.SimpleResponse("success", "deleted")));
            when(transferService.trashTransfer(anyInt())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferDeleteAt("success", "trashed", null)));
            when(transferService.restoreTransfer(anyInt())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferDeleteAt("success", "restored", null)));
            when(transferService.restoreAllTransfers()).thenReturn(Uni.createFrom().item(new TransferDto.SimpleResponse("success", "restored all")));
            when(transferService.deleteAllTransfers()).thenReturn(Uni.createFrom().item(new TransferDto.SimpleResponse("success", "deleted all")));
            when(transferService.findMonthlyAmounts(anyInt())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferMonthAmount("success", "ok", List.of(mAmt))));
            when(transferService.findYearlyAmounts(anyInt())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferYearAmount("success", "ok", List.of(yAmt))));
            when(transferService.findMonthlyAmountsFromCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferMonthAmount("success", "ok", List.of(mAmt))));
            when(transferService.findMonthlyAmountsToCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferMonthAmount("success", "ok", List.of(mAmt))));
            when(transferService.findYearlyAmountsFromCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferYearAmount("success", "ok", List.of(yAmt))));
            when(transferService.findYearlyAmountsToCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferYearAmount("success", "ok", List.of(yAmt))));
            when(transferService.findMonthlyStatusSuccess(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferMonthStatusSuccess("success", "ok", List.of(mSucc))));
            when(transferService.findYearlyStatusSuccess(anyInt())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferYearStatusSuccess("success", "ok", List.of(ySucc))));
            when(transferService.findMonthlyStatusFailed(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferMonthStatusFailed("success", "ok", List.of(mFail))));
            when(transferService.findYearlyStatusFailed(anyInt())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferYearStatusFailed("success", "ok", List.of(yFail))));
            when(transferService.findMonthlyStatusSuccessByCard(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferMonthStatusSuccess("success", "ok", List.of(mSucc))));
            when(transferService.findYearlyStatusSuccessByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferYearStatusSuccess("success", "ok", List.of(ySucc))));
            when(transferService.findMonthlyStatusFailedByCard(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferMonthStatusFailed("success", "ok", List.of(mFail))));
            when(transferService.findYearlyStatusFailedByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new TransferDto.ApiResponseTransferYearStatusFailed("success", "ok", List.of(yFail))));
        }

        @Test void list_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/transfers").then().statusCode(200); }
        @Test void active_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/transfers/active").then().statusCode(200); }
        @Test void trashed_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/transfers/trashed").then().statusCode(200); }
        @Test void getById_returns200() { given().when().get("/api/transfers/1").then().statusCode(200); }
        @Test void fromCard_returns200() { given().when().get("/api/transfers/from/4111111111111111").then().statusCode(200); }
        @Test void toCard_returns200() { given().when().get("/api/transfers/to/4222222222222222").then().statusCode(200); }
        @Test void create_returns201() { given().contentType("application/json").body(Map.of("transferFrom","4111111111111111","transferTo","4222222222222222","transferAmount",30000)).when().post("/api/transfers").then().statusCode(201); }
        @Test void update_returns200() { given().contentType("application/json").body(Map.of("transferFrom","4111111111111111","transferTo","4222222222222222","transferAmount",40000)).when().put("/api/transfers/1").then().statusCode(200); }
        @Test void delete_returns200() { given().when().delete("/api/transfers/1").then().statusCode(200); }
        @Test void permanent_returns200() { given().when().delete("/api/transfers/1/permanent").then().statusCode(200); }
        @Test void trash_returns200() { given().contentType("application/json").when().post("/api/transfers/trash/1").then().statusCode(200); }
        @Test void restore_returns200() { given().contentType("application/json").when().post("/api/transfers/restore/1").then().statusCode(200); }
        @Test void restoreAll_returns200() { given().contentType("application/json").when().post("/api/transfers/restore-all").then().statusCode(200); }
        @Test void deleteAll_returns200() { given().contentType("application/json").when().post("/api/transfers/delete-all").then().statusCode(200); }
        @Test void statsAmountMonthly_returns200() { given().queryParam("year",2024).when().get("/api/transfers/stats/amount/monthly").then().statusCode(200); }
        @Test void statsAmountYearly_returns200() { given().queryParam("year",2024).when().get("/api/transfers/stats/amount/yearly").then().statusCode(200); }
        @Test void statsAmountMonthlyFromCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/transfers/stats/amount/monthly/from-card").then().statusCode(200); }
        @Test void statsAmountMonthlyToCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4222222222222222").when().get("/api/transfers/stats/amount/monthly/to-card").then().statusCode(200); }
        @Test void statsAmountYearlyFromCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/transfers/stats/amount/yearly/from-card").then().statusCode(200); }
        @Test void statsAmountYearlyToCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4222222222222222").when().get("/api/transfers/stats/amount/yearly/to-card").then().statusCode(200); }
        @Test void statsStatusSuccessMonthly_returns200() { given().queryParam("year",2024).queryParam("month",1).when().get("/api/transfers/stats/status/monthly/success").then().statusCode(200); }
        @Test void statsStatusSuccessYearly_returns200() { given().queryParam("year",2024).when().get("/api/transfers/stats/status/yearly/success").then().statusCode(200); }
        @Test void statsStatusFailedMonthly_returns200() { given().queryParam("year",2024).queryParam("month",1).when().get("/api/transfers/stats/status/monthly/failed").then().statusCode(200); }
        @Test void statsStatusFailedYearly_returns200() { given().queryParam("year",2024).when().get("/api/transfers/stats/status/yearly/failed").then().statusCode(200); }
        @Test void statsStatusSuccessMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("month",1).queryParam("cardNumber","4111111111111111").when().get("/api/transfers/stats/status/monthly/success/by-card").then().statusCode(200); }
        @Test void statsStatusSuccessYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/transfers/stats/status/yearly/success/by-card").then().statusCode(200); }
        @Test void statsStatusFailedMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("month",1).queryParam("cardNumber","4111111111111111").when().get("/api/transfers/stats/status/monthly/failed/by-card").then().statusCode(200); }
        @Test void statsStatusFailedYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/transfers/stats/status/yearly/failed/by-card").then().statusCode(200); }
    }

    // ═══════════════════════════════════════════════════════════════════
    // WITHDRAW ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════
    @Nested
    class WithdrawEndpointTest {

        @InjectMock
        WithdrawService withdrawService;

        @BeforeEach
        void setup() {
            WithdrawDto.WithdrawResponse wd = new WithdrawDto.WithdrawResponse(1, "WD001", "4111111111111111", 10000, "2024-01-01", "2024-01-01", "2024-01-01");
            WithdrawDto.WithdrawMonthlyAmountResponse mAmt = new WithdrawDto.WithdrawMonthlyAmountResponse("2024-01", 10000);
            WithdrawDto.WithdrawYearlyAmountResponse yAmt = new WithdrawDto.WithdrawYearlyAmountResponse("2024", 120000);
            WithdrawDto.WithdrawMonthStatusSuccessResponse mSucc = new WithdrawDto.WithdrawMonthStatusSuccessResponse("2024", "2024-01", 10, 100000);
            WithdrawDto.WithdrawYearStatusSuccessResponse ySucc = new WithdrawDto.WithdrawYearStatusSuccessResponse("2024", 120, 1200000);
            WithdrawDto.WithdrawMonthStatusFailedResponse mFail = new WithdrawDto.WithdrawMonthStatusFailedResponse("2024", "2024-01", 2, 20000);
            WithdrawDto.WithdrawYearStatusFailedResponse yFail = new WithdrawDto.WithdrawYearStatusFailedResponse("2024", 24, 240000);

            when(withdrawService.listWithdraws(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponsePaginationWithdraw("success", "ok", List.of(), null)));
            when(withdrawService.findByCard(anyString())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponsesWithdraw("success", "ok", List.of())));
            when(withdrawService.findActiveWithdraws(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponsePaginationWithdrawDeleteAt("success", "ok", List.of(), null)));
            when(withdrawService.findTrashedWithdraws(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponsePaginationWithdrawDeleteAt("success", "ok", List.of(), null)));
            when(withdrawService.getWithdraw(anyInt())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdraw("success", "ok", wd)));
            when(withdrawService.createWithdraw(any())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdraw("success", "created", wd)));
            when(withdrawService.updateWithdraw(anyInt(), any())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdraw("success", "updated", wd)));
            when(withdrawService.deleteWithdraw(anyInt())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawDeleteAt("success", "deleted", null)));
            when(withdrawService.deleteWithdrawPermanent(anyInt())).thenReturn(Uni.createFrom().item(new WithdrawDto.SimpleResponse("success", "deleted")));
            when(withdrawService.trashWithdraw(anyInt())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawDeleteAt("success", "trashed", null)));
            when(withdrawService.restoreWithdraw(anyInt())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawDeleteAt("success", "restored", null)));
            when(withdrawService.restoreAllWithdraws()).thenReturn(Uni.createFrom().item(new WithdrawDto.SimpleResponse("success", "restored all")));
            when(withdrawService.deleteAllWithdraws()).thenReturn(Uni.createFrom().item(new WithdrawDto.SimpleResponse("success", "deleted all")));
            when(withdrawService.findMonthlyAmounts(anyInt())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawMonthAmount("success", "ok", List.of(mAmt))));
            when(withdrawService.findYearlyAmounts(anyInt())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawYearAmount("success", "ok", List.of(yAmt))));
            when(withdrawService.findMonthlyByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawMonthAmount("success", "ok", List.of(mAmt))));
            when(withdrawService.findYearlyByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawYearAmount("success", "ok", List.of(yAmt))));
            when(withdrawService.findMonthlyStatusSuccess(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawMonthStatusSuccess("success", "ok", List.of(mSucc))));
            when(withdrawService.findYearlyStatusSuccess(anyInt())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawYearStatusSuccess("success", "ok", List.of(ySucc))));
            when(withdrawService.findMonthlyStatusFailed(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawMonthStatusFailed("success", "ok", List.of(mFail))));
            when(withdrawService.findYearlyStatusFailed(anyInt())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawYearStatusFailed("success", "ok", List.of(yFail))));
            when(withdrawService.findMonthlyStatusSuccessByCard(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawMonthStatusSuccess("success", "ok", List.of(mSucc))));
            when(withdrawService.findYearlyStatusSuccessByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawYearStatusSuccess("success", "ok", List.of(ySucc))));
            when(withdrawService.findMonthlyStatusFailedByCard(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawMonthStatusFailed("success", "ok", List.of(mFail))));
            when(withdrawService.findYearlyStatusFailedByCard(anyInt(), anyString())).thenReturn(Uni.createFrom().item(new WithdrawDto.ApiResponseWithdrawYearStatusFailed("success", "ok", List.of(yFail))));
        }

        @Test void list_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/withdraws").then().statusCode(200); }
        @Test void byCard_returns200() { given().when().get("/api/withdraws/by-card/4111111111111111").then().statusCode(200); }
        @Test void active_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/withdraws/active").then().statusCode(200); }
        @Test void trashed_returns200() { given().queryParam("page",1).queryParam("size",10).queryParam("search","").when().get("/api/withdraws/trashed").then().statusCode(200); }
        @Test void getById_returns200() { given().when().get("/api/withdraws/1").then().statusCode(200); }
        @Test void create_returns201() { given().contentType("application/json").body(Map.of("cardNumber","4111111111111111","withdrawAmount",10000)).when().post("/api/withdraws").then().statusCode(201); }
        @Test void update_returns200() { given().contentType("application/json").body(Map.of("cardNumber","4111111111111111","withdrawAmount",20000)).when().put("/api/withdraws/1").then().statusCode(200); }
        @Test void delete_returns200() { given().when().delete("/api/withdraws/1").then().statusCode(200); }
        @Test void permanent_returns200() { given().when().delete("/api/withdraws/1/permanent").then().statusCode(200); }
        @Test void trash_returns200() { given().contentType("application/json").when().post("/api/withdraws/trash/1").then().statusCode(200); }
        @Test void restore_returns200() { given().contentType("application/json").when().post("/api/withdraws/restore/1").then().statusCode(200); }
        @Test void restoreAll_returns200() { given().contentType("application/json").when().post("/api/withdraws/restore-all").then().statusCode(200); }
        @Test void deleteAll_returns200() { given().contentType("application/json").when().post("/api/withdraws/delete-all").then().statusCode(200); }
        @Test void statsAmountMonthly_returns200() { given().queryParam("year",2024).when().get("/api/withdraws/stats/amount/monthly").then().statusCode(200); }
        @Test void statsAmountYearly_returns200() { given().queryParam("year",2024).when().get("/api/withdraws/stats/amount/yearly").then().statusCode(200); }
        @Test void statsAmountMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/withdraws/stats/amount/monthly/by-card").then().statusCode(200); }
        @Test void statsAmountYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/withdraws/stats/amount/yearly/by-card").then().statusCode(200); }
        @Test void statsStatusSuccessMonthly_returns200() { given().queryParam("year",2024).queryParam("month",1).when().get("/api/withdraws/stats/status/monthly/success").then().statusCode(200); }
        @Test void statsStatusSuccessYearly_returns200() { given().queryParam("year",2024).when().get("/api/withdraws/stats/status/yearly/success").then().statusCode(200); }
        @Test void statsStatusFailedMonthly_returns200() { given().queryParam("year",2024).queryParam("month",1).when().get("/api/withdraws/stats/status/monthly/failed").then().statusCode(200); }
        @Test void statsStatusFailedYearly_returns200() { given().queryParam("year",2024).when().get("/api/withdraws/stats/status/yearly/failed").then().statusCode(200); }
        @Test void statsStatusSuccessMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("month",1).queryParam("cardNumber","4111111111111111").when().get("/api/withdraws/stats/status/monthly/success/by-card").then().statusCode(200); }
        @Test void statsStatusSuccessYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/withdraws/stats/status/yearly/success/by-card").then().statusCode(200); }
        @Test void statsStatusFailedMonthlyByCard_returns200() { given().queryParam("year",2024).queryParam("month",1).queryParam("cardNumber","4111111111111111").when().get("/api/withdraws/stats/status/monthly/failed/by-card").then().statusCode(200); }
        @Test void statsStatusFailedYearlyByCard_returns200() { given().queryParam("year",2024).queryParam("cardNumber","4111111111111111").when().get("/api/withdraws/stats/status/yearly/failed/by-card").then().statusCode(200); }
    }
}
