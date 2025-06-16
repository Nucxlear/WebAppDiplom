package com.example.SalesAnalytics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/sales", "/sales/catalog", "/sales/analytics", "/sales/create-request", "/sales/submit-request", "/sales/request/**", "/sales/add-product", "/sales/delete-product", "/sales/sell-product", "/sales/receive-product").hasRole("MANAGER")
                        .requestMatchers("/sales/production", "/sales/update-request-status").hasRole("PRODUCTION_LEAD")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customSuccessHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/perform-logout") // Изменяем URL для POST-запроса
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
                response.sendRedirect("/sales/analytics");
            } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PRODUCTION_LEAD"))) {
                response.sendRedirect("/sales/production");
            }
        };
    }

    @Bean
    public UserDetailsService userDetailsService() {
        PasswordEncoder encoder = passwordEncoder();
        UserDetails manager = User.builder()
                .username("manager")
                .password(encoder.encode("password"))
                .roles("MANAGER")
                .build();
        UserDetails productionLead = User.builder()
                .username("production")
                .password(encoder.encode("password"))
                .roles("PRODUCTION_LEAD")
                .build();

        System.out.println("SecurityConfig: Users configured - manager (MANAGER), production (PRODUCTION_LEAD)");
        return new InMemoryUserDetailsManager(manager, productionLead);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}