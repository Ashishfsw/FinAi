package com.fintech.marketdata.security;

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
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<String> PUBLIC_PATHS = List.of("/actuator/**", "/health", "/info");

    private final JwtTokenProvider jwtTokenProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        if (!jwtTokenProvider.validateToken(token)) {
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
