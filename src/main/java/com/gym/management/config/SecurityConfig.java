package com.gym.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/home", "/index",
                    "/css/**", "/js/**", "/images/**", "/webjars/**",
                    "/login", "/h2-console/**", "/api/**"
                ).permitAll()
                // Admin specific operations
                .requestMatchers("/trainers/new", "/trainers/edit/**", "/trainers/delete/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/plans/new", "/plans/edit/**", "/plans/delete/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/subscriptions/cancel/**").hasAuthority("ROLE_ADMIN")
                // Trainer portal
                .requestMatchers("/trainer/**").hasAnyAuthority("ROLE_TRAINER", "ROLE_ADMIN")
                // Member portal
                .requestMatchers("/member/**").hasAnyAuthority("ROLE_MEMBER", "ROLE_ADMIN")
                // Class creation/editing
                .requestMatchers("/classes/new", "/classes/save", "/classes/edit/**", "/classes/delete/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TRAINER")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/default", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
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
