package com.hmall.gateway.it;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.hmall.gateway.utils.JwtTool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Integration test for Gateway auth filter.
 *
 * Routes are defined in application-test.yaml with paths like /test-public
 * that forward to /internal/* paths. The controller only handles /internal/*
 * paths, so RequestMappingHandlerMapping does NOT match the external routes.
 * This ensures RoutePredicateHandlerMapping runs first and AuthGlobalFilter
 * intercepts requests as expected.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class GatewayAuthIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtTool jwtTool;

    @Test
    @DisplayName("GET /test-public returns 200 (excludePaths bypasses auth)")
    void publicPathBypassesAuth() {
        webTestClient.get().uri("/test-public")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("public ok");
    }

    @Test
    @DisplayName("GET /test-read returns 200 (excludeReadPaths bypasses auth for GET)")
    void readPathGetBypassesAuth() {
        webTestClient.get().uri("/test-read")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("read ok");
    }

    @Test
    @DisplayName("POST /test-read returns 401 (excludeReadPaths only applies to GET/HEAD/OPTIONS)")
    void readPathPostRequiresAuth() {
        webTestClient.post().uri("/test-read")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("No auth token on protected path returns 401")
    void protectedPathWithoutTokenReturns401() {
        webTestClient.get().uri("/test-protected")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Valid auth token on protected path returns 200")
    void protectedPathWithValidTokenReturns200() {
        String token = jwtTool.createToken(1L, "user", Duration.ofMinutes(30));
        webTestClient.get().uri("/test-protected")
                .header("authorization", token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("protected ok");
    }

    // ---- Test configuration & controller ----
    //
    // Controller handles /internal/* paths only. Gateway routes defined in
    // application-test.yaml forward external paths (/test-public, etc.) to
    // these internal paths. This avoids RequestMappingHandlerMapping matching
    // external paths before RoutePredicateHandlerMapping can process them.

    @org.springframework.boot.test.context.TestConfiguration
    static class GatewayTestConfig {

        @Bean
        TestController testController() {
            return new TestController();
        }

        @Bean
        NacosConfigManager nacosConfigManager() throws NacosException {
            NacosConfigManager mock = mock(NacosConfigManager.class);
            ConfigService configService = mock(ConfigService.class);
            given(mock.getConfigService()).willReturn(configService);
            given(configService.getConfigAndSignListener(anyString(), anyString(), anyLong(), any()))
                    .willReturn("[]");
            return mock;
        }
    }

    @RestController
    static class TestController {

        @GetMapping("/internal/test-public")
        String publicEndpoint() {
            return "public ok";
        }

        @GetMapping("/internal/test-read")
        String readEndpoint() {
            return "read ok";
        }

        @PostMapping("/internal/test-read")
        String readPostEndpoint() {
            return "read post";
        }

        @GetMapping("/internal/test-protected")
        String protectedEndpoint() {
            return "protected ok";
        }

        @PostMapping("/internal/test-protected")
        String protectedPostEndpoint() {
            return "protected post";
        }
    }
}
