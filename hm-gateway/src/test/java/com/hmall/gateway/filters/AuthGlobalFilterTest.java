package com.hmall.gateway.filters;

import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthGlobalFilterTest {

    @Mock
    private AuthProperties authProperties;

    @Mock
    private JwtTool jwtTool;

    @InjectMocks
    private AuthGlobalFilter filter;

    @Mock
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        // lenient: only pass-through tests call chain.filter; 401/403 tests never do
        lenient().when(chain.filter(any())).thenReturn(Mono.empty());
    }

    // -- helpers -----------------------------------------------------------

    private static MockServerWebExchange exchange(HttpMethod method, String path) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.method(method, path).build());
    }

    private static MockServerWebExchange exchangeWithToken(
            HttpMethod method, String path, String token) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.method(method, path)
                        .header("Authorization", token)
                        .build());
    }

    // -- tests -------------------------------------------------------------

    @Test
    @DisplayName("1a. excludePaths pass through for any HTTP method")
    void excludePath_anyMethod_passesThrough() {
        when(authProperties.getExcludePaths()).thenReturn(List.of("/api/public/**"));

        MockServerWebExchange exchange = exchange(HttpMethod.POST, "/api/public/test");
        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("1b. excludeReadPaths with GET pass through")
    void excludeReadPath_get_passesThrough() {
        when(authProperties.getExcludePaths()).thenReturn(List.of());
        when(authProperties.getExcludeReadPaths()).thenReturn(List.of("/api/items/**"));

        MockServerWebExchange exchange = exchange(HttpMethod.GET, "/api/items/1");
        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("2. POST to excludeReadPath requires auth (not excluded)")
    void postToExcludeReadPath_requiresAuth() {
        when(authProperties.getExcludePaths()).thenReturn(List.of());
        // excludeReadPaths only applies to GET/HEAD/OPTIONS, so POST to such a
        // path is NOT excluded.  Provide a token to reach token validation and
        // prove the filter did not short-circuit at the exclusion check.
        when(jwtTool.parseToken("Bearer token"))
                .thenThrow(new RuntimeException("Unauthorized"));

        MockServerWebExchange exchange =
                exchangeWithToken(HttpMethod.POST, "/api/items/1", "Bearer token");
        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("3. Missing Authorization header returns 401")
    void missingAuthHeader_returns401() {
        when(authProperties.getExcludePaths()).thenReturn(List.of());
        when(jwtTool.parseToken(any())).thenThrow(new RuntimeException("Unauthorized"));

        MockServerWebExchange exchange = exchange(HttpMethod.GET, "/api/protected");
        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("4. Invalid token returns 401")
    void invalidToken_returns401() {
        when(authProperties.getExcludePaths()).thenReturn(List.of());
        when(jwtTool.parseToken("Bearer invalid"))
                .thenThrow(new RuntimeException("Invalid token"));

        MockServerWebExchange exchange =
                exchangeWithToken(HttpMethod.GET, "/api/protected", "Bearer invalid");
        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("5. Valid token passes through with user-info header set")
    void validToken_passesThrough() {
        when(authProperties.getExcludePaths()).thenReturn(List.of());
        JwtTool.TokenInfo tokenInfo = new JwtTool.TokenInfo(42L, "user");
        when(jwtTool.parseToken("Bearer valid-token")).thenReturn(tokenInfo);

        MockServerWebExchange exchange =
                exchangeWithToken(HttpMethod.GET, "/api/protected", "Bearer valid-token");

        ArgumentCaptor<ServerWebExchange> captor =
                ArgumentCaptor.forClass(ServerWebExchange.class);
        filter.filter(exchange, chain).block();

        verify(chain).filter(captor.capture());
        ServerWebExchange modified = captor.getValue();
        assertThat(modified.getRequest().getHeaders().getFirst("user-info"))
                .isEqualTo("42");
        assertThat(modified.getRequest().getHeaders().getFirst("role-info"))
                .isEqualTo("user");
    }

    @Test
    @DisplayName("6. Non-admin accessing /admin/** returns 403")
    void nonAdminAccessingAdmin_returns403() {
        when(authProperties.getExcludePaths()).thenReturn(List.of());
        JwtTool.TokenInfo tokenInfo = new JwtTool.TokenInfo(1L, "user");
        when(jwtTool.parseToken("Bearer token")).thenReturn(tokenInfo);

        MockServerWebExchange exchange =
                exchangeWithToken(HttpMethod.GET, "/admin/users", "Bearer token");
        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("7. Admin accessing /admin/** passes through")
    void adminAccessingAdmin_passesThrough() {
        when(authProperties.getExcludePaths()).thenReturn(List.of());
        JwtTool.TokenInfo tokenInfo = new JwtTool.TokenInfo(1L, "admin");
        when(jwtTool.parseToken("Bearer admin-token")).thenReturn(tokenInfo);

        MockServerWebExchange exchange =
                exchangeWithToken(HttpMethod.GET, "/admin/users", "Bearer admin-token");

        ArgumentCaptor<ServerWebExchange> captor =
                ArgumentCaptor.forClass(ServerWebExchange.class);
        filter.filter(exchange, chain).block();

        verify(chain).filter(captor.capture());
        ServerWebExchange modified = captor.getValue();
        assertThat(modified.getRequest().getHeaders().getFirst("user-info"))
                .isEqualTo("1");
        assertThat(modified.getRequest().getHeaders().getFirst("role-info"))
                .isEqualTo("admin");
    }
}
