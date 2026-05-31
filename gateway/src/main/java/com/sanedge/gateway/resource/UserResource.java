package com.sanedge.gateway.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.sanedge.gateway.dto.UserDto;
import com.sanedge.gateway.service.UserService;

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

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "User management endpoints")
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
    @Operation(summary = "List all users")
    public Uni<Response> listUsers(
                    @QueryParam("page") @DefaultValue("1") int page,
                    @QueryParam("size") @DefaultValue("20") int size,
                    @QueryParam("search") String search) {
            return userService.listUsers(page, size, search)
                    .map(dto -> Response.ok(dto).build());
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ "ROLE_ADMIN", "ROLE_STAFF" })
    @Operation(summary = "Get user by ID")
    public Uni<Response> getUser(@PathParam("id") int id) {
            return userService.getUser(id)
                    .map(dto -> Response.ok(dto).build());
    }

    @POST
    @RolesAllowed("ROLE_ADMIN")
    @Operation(summary = "Create a new user")
    public Uni<Response> createUser(UserDto.CreateRequest body) {
            return userService.createUser(body)
                    .map(dto -> Response.status(Response.Status.CREATED)
                                    .entity(dto)
                                    .build());
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ROLE_ADMIN")
    @Operation(summary = "Update user")
    public Uni<Response> updateUser(@PathParam("id") int id, UserDto.UpdateRequest body) {
            UserDto.UpdateRequest req = new UserDto.UpdateRequest(
                    id,
                    body.firstname(),
                    body.lastname(),
                    body.email(),
                    body.password(),
                    body.confirmPassword()
            );
            return userService.updateUser(id, req)
                    .map(dto -> Response.ok(dto).build());
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ROLE_ADMIN")
    @Operation(summary = "Soft-delete a user")
    public Uni<Response> deleteUser(@PathParam("id") int id) {
            return userService.deleteUser(id)
                    .map(dto -> Response.ok(dto).build());
    }
}