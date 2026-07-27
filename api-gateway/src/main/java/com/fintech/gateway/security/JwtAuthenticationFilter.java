package com.fintech.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<String> PUBLIC_PATHS = List.of("/api/v1/auth/**", "/actuator/**", "/health", "/info");

    private final JwtTokenProvider jwtTokenProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return unauthorized(exchange);
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("Invalid JWT token for path: {}", path);
            return unauthorized(exchange);
        }

        String username = jwtTokenProvider.extractUsername(token);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext context = new SecurityContextImpl(authentication);

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
