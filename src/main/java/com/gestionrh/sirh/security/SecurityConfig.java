package com.gestionrh.sirh.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // forward/include internes vers les JSP (/WEB-INF/jsp/...) : sinon boucle de redirection vers /connexion
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR).permitAll()
                // pages et ressources publiques
                .requestMatchers("/", "/connexion", "/error", "/css/**", "/js/**", "/logo*").permitAll()
                .requestMatchers("/offres", "/offres/**", "/candidatures/nouvelle").permitAll()
                // espaces réservés à un rôle (à adapter à vos URL)
                .requestMatchers("/responsable/**").hasRole("RESPONSABLE")
                .requestMatchers("/rh/**").hasRole("RH")
                .requestMatchers("/finance/**").hasRole("FINANCE")
                .requestMatchers("/dg/**").hasRole("DG")
                .requestMatchers("/candidat/**").hasRole("CANDIDAT")
                // tout le reste : être connecté suffit
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/connexion")
                .loginProcessingUrl("/connexion")
                .usernameParameter("login")
                .passwordParameter("mdp")
                .defaultSuccessUrl("/accueil", true)
                .failureUrl("/connexion?erreur")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/deconnexion")
                .logoutSuccessUrl("/connexion?deconnecte")
            )
            .exceptionHandling(e -> e.accessDeniedPage("/acces-refuse"));

        return http.build();
    }
}
