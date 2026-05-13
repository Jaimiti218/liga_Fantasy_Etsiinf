package com.ligainternaetsiinf.config;

import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
// @org.springframework.boot.autoconfigure.EnableAutoConfiguration(exclude = {UserDetailsServiceAutoConfiguration.class})
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("jornada-scheduler-");
        return scheduler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/users/login", "/logout" , "/mercado/**", "/users/register",
                 "/jugadores/**", "/equipos/**", "/ligas-fantasy/**", "/equipos-fantasy/**", "/jugadores-fantasy/**",
                "/partidos/**")
            )
            .formLogin(login -> login.disable())
            .httpBasic(basic -> basic.disable())
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos — siempre públicos
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // Páginas HTML públicas
                .requestMatchers("/", "/fantasy/auth").permitAll()
                // Endpoints públicos de la API
                .requestMatchers("/users/register", "/users/login", "/users/me").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/jugadores").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/equipos").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/partidos", "/partidos/**").permitAll()
                .requestMatchers("/", "/fantasy/auth", "/fantasy/mis-ligas", "/fantasy/liga/**", "/fantasy/plantilla/**", "/fantasy/mercado/**").permitAll()
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .securityContext(securityContext ->
                securityContext.securityContextRepository(
                    new HttpSessionSecurityContextRepository()
                )
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
