package com.lab.Uni_Management_System.config;

import tools.jackson.databind.ObjectMapper;
import com.lab.Uni_Management_System.security.CustomUserDetailsService;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper) throws Exception {

        http
                // CSRF configuration
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/login", "/logout")
                )

                // Authentication provider
                .authenticationProvider(authenticationProvider(passwordEncoder()))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // Login page, frontend pages and scripts are public; the API stays protected
                        .requestMatchers("/", "/login", "/error", "/*.html", "/js/**").permitAll()

                        .requestMatchers("/api/admins/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/teachers/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers("/api/students/**")
                        .hasAnyRole("ADMIN", "STUDENT")

                        .anyRequest().authenticated()
                )

                // Login returns JSON instead of redirecting
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {

                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Map.of(
                                            "message", "Login successful",
                                            "email", authentication.getName(),
                                            "role", authentication.getAuthorities()
                                                    .iterator().next().getAuthority()
                                    )
                            );
                        })
                        .failureHandler((request, response, exception) -> {

                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Map.of("message", "Invalid email or password")
                            );
                        })
                )

                // Logout returns JSON instead of redirecting
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {

                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Map.of("message", "Logout successful")
                            );
                        })
                )

                // JSON response for unauthenticated requests
                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint((request, response, authException) -> {

                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Map.of("message", "Authentication required")
                            );
                        })

                        // JSON response for insufficient permissions
                        .accessDeniedHandler((request, response, accessDeniedException) -> {

                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Map.of("message", "You do not have permission to access this resource")
                            );
                        })
                );

        return http.build();
    }
}