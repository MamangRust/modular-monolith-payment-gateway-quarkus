package com.sanedge.gateway.security;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import jakarta.ws.rs.core.Response;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

/**
 * Test alternative that always returns an authenticated identity with ROLE_ADMIN.
 * This bypasses JWT validation during E2E tests.
 */
@Alternative
@Priority(1)
@ApplicationScoped
public class MockJwtAuthenticationMechanism implements HttpAuthenticationMechanism {

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        SecurityIdentity identity = QuarkusSecurityIdentity.builder()
                .setPrincipal(new Principal() {
                    @Override
                    public String getName() {
                        return "test-user";
                    }
                })
                .addRole("ROLE_ADMIN")
                .addRole("ROLE_STAFF")
                .addRole("ROLE_USER")
                .build();
        return Uni.createFrom().item(identity);
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().item(new ChallengeData(
                Response.Status.UNAUTHORIZED.getStatusCode(),
                "WWW-Authenticate",
                "Bearer realm=\"Quarkus\""
        ));
    }

    @Override
    public Uni<HttpCredentialTransport> getCredentialTransport(RoutingContext context) {
        return Uni.createFrom().item(new HttpCredentialTransport(HttpCredentialTransport.Type.AUTHORIZATION, "Bearer"));
    }

    @Override
    public Set<Class<? extends io.quarkus.security.identity.request.AuthenticationRequest>> getCredentialTypes() {
        return Collections.singleton(JwtAuthenticationRequest.class);
    }
}
