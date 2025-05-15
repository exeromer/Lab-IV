package com.utn.frm.instrumentos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Si la usas
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // Si la usas

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new ChangeSessionIdAuthenticationStrategy();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        // Usamos HttpSessionSecurityContextRepository que permite la creación de sesiones
        // y es el estándar para la persistencia basada en sesión.
        HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();
        repository.setAllowSessionCreation(true); // Es true por defecto, pero lo hacemos explícito.
        return repository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // Configurar Spring Security para usar nuestro bean SecurityContextRepository.
                // Esto asegura que el SecurityContextPersistenceFilter use esta instancia.
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository)
                )
                // En SecurityConfig.java
// ...
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()

                        // --- REGLA ÚNICA Y GENERAL PARA INSTRUMENTOS ---
                        // Todo bajo /api/instrumentos/ requiere rol ADMIN
                        .requestMatchers(new AntPathRequestMatcher("/api/instrumentos/**")).hasRole("ADMIN")

                        // --- REGLAS PARA CATEGORÍAS (Ejemplo, ajustar si es necesario) ---
                        // Si quieres que GET de categorías sea para todos los autenticados, y el resto para ADMIN:
                        .requestMatchers(new AntPathRequestMatcher("/api/categoria/**", HttpMethod.GET.name())).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/categoria/**")).hasRole("ADMIN") // Para POST, PUT, DELETE

                        // --- REGLAS PARA PEDIDOS (Ejemplo, ajustar si es necesario) ---
                        .requestMatchers(new AntPathRequestMatcher("/api/pedidos/**", HttpMethod.POST.name())).hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers(new AntPathRequestMatcher("/api/pedidos/**", HttpMethod.GET.name())).hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers(new AntPathRequestMatcher("/api/pedidos/*/preferencia", HttpMethod.POST.name())).hasAnyRole("ADMIN", "OPERADOR")

                        .anyRequest().authenticated()
                )
                // --- AÑADIR CONFIGURACIÓN DE LOGOUT ---
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout") // La URL que interceptará el LogoutFilter
                        // .logoutRequestMatcher(new AntPathRequestMatcher("/api/auth/logout", "POST")) // Si quieres ser explícito con el método POST
                        .invalidateHttpSession(true) // Invalida la sesión HTTP
                        .deleteCookies("JSESSIONID") // Le pide al cliente que borre la cookie de sesión
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)) // Devuelve un 200 OK en logout exitoso
                        .permitAll() // Permite a cualquiera acceder a la URL de logout
                )
                .exceptionHandling(e -> e // Configurar cómo se manejan las excepciones de autenticación
                        // Para APIs REST, en lugar de redirigir a una página de login, devolvemos un 401
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())); // Para H2 Console

        return http.build();
    }
}