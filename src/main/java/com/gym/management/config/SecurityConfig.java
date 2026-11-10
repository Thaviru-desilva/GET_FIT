package com.gym.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Publicly accessible pages and static resources
                .requestMatchers(
                    "/", "/home", "/index",
                    "/css/**", "/js/**", "/images/**", "/webjars/**",
                    "/login", "/register", "/access-denied",
                    "/h2-console/**", "/api/**"
                ).permitAll()
                // Class schedule is publicly viewable
                .requestMatchers(HttpMethod.GET, "/classes").permitAll()

                // Admin dedicated operations
                .requestMatchers(
                    "/dashboard",
                    "/members", "/members/new", "/members/save", "/members/edit/**", "/members/delete/**", "/members/view/**",
                    "/trainers", "/trainers/new", "/trainers/save", "/trainers/edit/**", "/trainers/delete/**",
                    "/plans", "/plans/new", "/plans/save", "/plans/edit/**", "/plans/delete/**",
                    "/subscriptions", "/subscriptions/new", "/subscriptions/save", "/subscriptions/cancel/**"
                ).hasAuthority("ROLE_ADMIN")

                // Attendance Check-In Desk
                .requestMatchers("/attendance/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TRAINER")

                // Trainer Coach Portal
                .requestMatchers("/trainer/**").hasAnyAuthority("ROLE_TRAINER", "ROLE_ADMIN")

                // Member Athlete Portal
                .requestMatchers("/member/**").hasAnyAuthority("ROLE_MEMBER", "ROLE_ADMIN")

                // Class scheduling, roster viewing, and editing
                .requestMatchers("/classes/new", "/classes/save", "/classes/edit/**", "/classes/delete/**", "/classes/view/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TRAINER")

                // Class booking and cancellation
                .requestMatchers("/classes/book/**", "/classes/cancel-booking/**").hasAnyAuthority("ROLE_MEMBER", "ROLE_ADMIN", "ROLE_TRAINER")

                // Default post-login route
                .requestMatchers("/default").authenticated()

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("getufit-secure-remember-key-2026")
                .tokenValiditySeconds(86400 * 14) // 14 days
                .rememberMeParameter("remember-me")
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/h2-console/**"),
                    new AntPathRequestMatcher("/api/**")
                )
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }
}
