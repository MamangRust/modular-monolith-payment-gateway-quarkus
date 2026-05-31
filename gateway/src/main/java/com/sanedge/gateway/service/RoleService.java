package com.sanedge.gateway.service;

import com.sanedge.gateway.dto.RoleDto;
import io.smallrye.mutiny.Uni;

public interface RoleService {
    Uni<RoleDto.ApiResponsePaginationRole> listRoles(int page, int size, String search);
    Uni<RoleDto.ApiResponseRole> getRole(int id);
    Uni<RoleDto.ApiResponseRole> createRole(RoleDto.CreateRequest body);
    Uni<RoleDto.ApiResponseRole> updateRole(int id, RoleDto.UpdateRequest body);
    Uni<RoleDto.ApiResponseRoleDeleteAt> deleteRole(int id);
}
