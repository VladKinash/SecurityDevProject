package com.nyc.hosp.security;

import com.nyc.hosp.service.HospuserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {




    @Autowired
    private HospuserDetailsService hospuserDetailsService;

    @Autowired
    private CustomAuthFailureHandler customAuthFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/public/**",
                                "/auth-failure",
                                "/auth-failure/**"
                        ).permitAll()
                        .requestMatchers("/hospusers", "/hospuser/**").hasAnyRole("ADMIN", "SECRETARIAT")
                        .requestMatchers("/roles", "/roles/**").hasAnyRole( "ADMIN")
                        .requestMatchers("/patientvisits", "/patientvisits/**").hasAnyRole("DOCTOR", "ADMIN")
                        .requestMatchers("/patientvisits/add", "/patientvisits/add/**").hasAnyRole("DOCTOR")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureHandler(customAuthFailureHandler)
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )


                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                        .expiredUrl("/login?expired=true")
                );


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(hospuserDetailsService).passwordEncoder(passwordEncoder());
        return builder.build();
    }



}
