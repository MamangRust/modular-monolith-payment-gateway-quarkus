package com.sanedge.gateway.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.gateway.dto.RoleDto;
import com.sanedge.gateway.telemetry.TelemetryHelper;

import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private TelemetryHelper telemetryHelper;

    @Mock
    private pb.role.MutinyRoleServiceGrpc.MutinyRoleServiceStub roleQueryService;

    @Mock
    private pb.role.MutinyRoleCommandServiceGrpc.MutinyRoleCommandServiceStub roleCommandService;

    private RoleServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(telemetryHelper.traceAndMetric(anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Supplier<Uni<?>> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        service = new RoleServiceImpl();
        inject("telemetryHelper", telemetryHelper);
        inject("roleQueryService", roleQueryService);
        inject("roleCommandService", roleCommandService);
    }

    private void inject(String name, Object value) throws Exception {
        Field f = RoleServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    @Test
    void listRoles_PropagatesPaginationResponse() {
        pb.role.RoleQuery.ApiResponsePaginationRole proto = pb.role.RoleQuery.ApiResponsePaginationRole.newBuilder()
                .setStatus("success").setMessage("ok").build();
        lenient().when(roleQueryService.findAllRole(any(pb.role.Role.FindAllRoleRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        RoleDto.ApiResponsePaginationRole result = service.listRoles(1, 10, "").await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void getRole_PropagatesResponse() {
        pb.role.Role.ApiResponseRole proto = pb.role.Role.ApiResponseRole.newBuilder()
                .setStatus("success").setMessage("ok").build();
        lenient().when(roleQueryService.findByIdRole(any(pb.role.Role.FindByIdRoleRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        RoleDto.ApiResponseRole result = service.getRole(1).await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void createRole_PropagatesResponse() {
        RoleDto.CreateRequest req = new RoleDto.CreateRequest("ROLE_USER");
        pb.role.Role.ApiResponseRole proto = pb.role.Role.ApiResponseRole.newBuilder()
                .setStatus("success").setMessage("created").build();
        lenient().when(roleCommandService.createRole(any(pb.role.RoleCommand.CreateRoleRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        RoleDto.ApiResponseRole result = service.createRole(req).await().indefinitely();
        assertThat(result.message()).isEqualTo("created");
    }

    @Test
    void updateRole_PropagatesResponse() {
        RoleDto.UpdateRequest req = new RoleDto.UpdateRequest(1, "ROLE_ADMIN");
        pb.role.Role.ApiResponseRole proto = pb.role.Role.ApiResponseRole.newBuilder()
                .setStatus("success").setMessage("updated").build();
        lenient().when(roleCommandService.updateRole(any(pb.role.RoleCommand.UpdateRoleRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        RoleDto.ApiResponseRole result = service.updateRole(1, req).await().indefinitely();
        assertThat(result.message()).isEqualTo("updated");
    }

    @Test
    void deleteRole_PropagatesResponse() {
        pb.role.Role.ApiResponseRoleDeleteAt proto = pb.role.Role.ApiResponseRoleDeleteAt.newBuilder()
                .setStatus("success").setMessage("trashed").build();
        lenient().when(roleCommandService.trashedRole(any(pb.role.Role.FindByIdRoleRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        RoleDto.ApiResponseRoleDeleteAt result = service.deleteRole(1).await().indefinitely();
        assertThat(result.message()).isEqualTo("trashed");
    }
}
