package com.gestionrh.sirh.config.recrutement;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** Chaîne publique limitée à la vitrine ; les autres routes gardent la chaîne existante. */
@Configuration
public class VitrineRecrutementSecurityConfiguration {

    @Bean
    @Order(0)
    public SecurityFilterChain vitrinePublique(HttpSecurity http) throws Exception {
        http.securityMatcher("/offres", "/offres/**", "/candidatures/**")
                .authorizeHttpRequests(regles -> regles.anyRequest().permitAll());
        return http.build();
    }
}
