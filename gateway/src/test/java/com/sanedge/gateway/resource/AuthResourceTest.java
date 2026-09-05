package com.sanedge.gateway.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.gateway.dto.AuthDto;
import com.sanedge.gateway.service.AuthService;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class AuthResourceTest {

    @Mock private AuthService authService;
    private AuthResource authResource;

    @BeforeEach
    void setUp() throws Exception {
        authResource = new AuthResource();
        Field f = AuthResource.class.getDeclaredField("authService");
        f.setAccessible(true); f.set(authResource, authService);
    }

    @Test void register_Success_Returns201() {
        AuthDto.RegisterResponse dto = new AuthDto.RegisterResponse("success", "registered", null);
        lenient().when(authService.register(any())).thenReturn(Uni.createFrom().item(dto));
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest("John", "Doe", "u@e.com", "pwd", "pwd");
        Response r = authResource.register(req).await().indefinitely();
        assertThat(r.getStatus()).isEqualTo(201);
        assertThat(r.getEntity()).isEqualTo(dto);
    }

    @Test void login_Success_Returns200() {
        AuthDto.LoginResponse dto = new AuthDto.LoginResponse("success", "logged in", null);
        lenient().when(authService.login(any())).thenReturn(Uni.createFrom().item(dto));
        Response r = authResource.login(new AuthDto.LoginRequest("u@e.com", "pwd")).await().indefinitely();
        assertThat(r.getStatus()).isEqualTo(200);
    }

    @Test void verify_Success_Returns200() {
        lenient().when(authService.verify(any())).thenReturn(Uni.createFrom().item(new AuthDto.SimpleResponse("success", "ok")));
        Response r = authResource.verify(new AuthDto.VerifyCodeRequest("ABC123")).await().indefinitely();
        assertThat(r.getStatus()).isEqualTo(200);
    }

    @Test void forgotPassword_Success_Returns200() {
        lenient().when(authService.forgotPassword(any())).thenReturn(Uni.createFrom().item(new AuthDto.SimpleResponse("success", "ok")));
        Response r = authResource.forgotPassword(new AuthDto.ForgotPasswordRequest("u@e.com")).await().indefinitely();
        assertThat(r.getStatus()).isEqualTo(200);
    }

    @Test void getMe_Success_Returns200() {
        AuthDto.GetMeResponse dto = new AuthDto.GetMeResponse("success", "me", null);
        lenient().when(authService.getMe(any(int.class))).thenReturn(Uni.createFrom().item(dto));
        Response r = authResource.getMe(1).await().indefinitely();
        assertThat(r.getStatus()).isEqualTo(200);
    }
}
