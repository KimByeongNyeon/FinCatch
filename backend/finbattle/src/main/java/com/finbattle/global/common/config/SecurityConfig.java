package com.finbattle.global.common.config;

import com.finbattle.domain.oauth.handler.CustomSuccessHandler;
import com.finbattle.domain.oauth.service.CustomOAuth2UserService;
import com.finbattle.global.common.exception.exception.security.CustomAccessDeniedHandler;
import com.finbattle.global.common.exception.exception.security.CustomAuthenticationEntryPoint;
import com.finbattle.global.common.filter.JWTFilter;
import com.finbattle.global.common.filter.LoggingFilter;
import com.finbattle.global.common.model.enums.PublicEndpoint;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTFilter jwtFilter;
    private final LoggingFilter loggingFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Value("${app.baseUrl}")
    private String baseUrl;

    @Value("${app.clientUrl}")
    private String clientUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                    .userService(customOAuth2UserService))
                .successHandler(customSuccessHandler)
            )
            .authorizeHttpRequests((auth) -> auth
                .requestMatchers(PublicEndpoint.getAll().toArray(new String[0])).permitAll()
                .requestMatchers("/ws/firechat/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(loggingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception ->
                exception
                    .defaultAuthenticationEntryPointFor(
                        authenticationEntryPoint,
                        new AntPathRequestMatcher("/api/**")
                    ) // 401 예외 핸들러 적용
                    .accessDeniedHandler(accessDeniedHandler) // 403 예외 핸들러 적용
            )
            .formLogin(form -> form.disable())
            .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        //config.addAllowedOriginPattern("*");
        config.setAllowedOrigins(Arrays.asList(baseUrl, clientUrl)); // 프론트엔드 주소
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}