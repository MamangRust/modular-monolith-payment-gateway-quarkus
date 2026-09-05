package com.sanedge.gateway.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.gateway.dto.UserDto;
import com.sanedge.gateway.service.UserService;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class UserResourceTest {

    @Mock private UserService userService;
    private UserResource userResource;

    @BeforeEach
    void setUp() throws Exception {
        userResource = new UserResource();
        Field f = UserResource.class.getDeclaredField("userService");
        f.setAccessible(true); f.set(userResource, userService);
    }

    @Test void listUsers_Success() {
        UserDto.ApiResponsePaginationUser dto = new UserDto.ApiResponsePaginationUser("success", "ok", List.of(), null);
        lenient().when(userService.listUsers(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(dto));
        Response r = userResource.listUsers(1, 10, "").await().indefinitely();
        assertThat(r.getStatus()).isEqualTo(200);
    }

    @Test void getUser_Success() {
        UserDto.ApiResponseUser dto = new UserDto.ApiResponseUser("success", "ok", null);
        lenient().when(userService.getUser(anyInt())).thenReturn(Uni.createFrom().item(dto));
        Response r = userResource.getUser(1).await().indefinitely();
        assertThat(r.getStatus()).isEqualTo(200);
    }

    @Test void createUser_Success_Returns201() {
        UserDto.ApiResponseUser dto = new UserDto.ApiResponseUser("success", "ok", null);
        lenient().when(userService.createUser(any())).thenReturn(Uni.createFrom().item(dto));
        Response r = userResource.createUser(new UserDto.CreateRequest("John", "Doe", "j@d.com", "p", "p")).await().indefinitely();
        assertThat(r.getStatus()).isEqualTo(201);
    }

    @Test void deleteUser_Success() {
        UserDto.ApiResponseUserDeleteAt dto = new UserDto.ApiResponseUserDeleteAt("success", "ok", null);
        lenient().when(userService.deleteUser(anyInt())).thenReturn(Uni.createFrom().item(dto));
        Response r = userResource.deleteUser(1).await().indefinitely();
        assertThat(r.getStatus()).isEqualTo(200);
    }
}
