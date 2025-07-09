package ma.ensa.ebankingver1.config;

import ma.ensa.ebankingver1.filter.JwtFilter;
import ma.ensa.ebankingver1.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Import({RestAuthenticationEntryPoint.class, JwtFilter.class, CustomUserDetailsService.class})
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public SecurityConfig() {
        System.out.println(">>> SecurityConfig is loaded");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   @Qualifier("restAuthenticationEntryPoint") RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                                                   @Qualifier("jwtFilter") JwtFilter jwtFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Fichiers statiques
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/"),
                                AntPathRequestMatcher.antMatcher("/index.html"),
                                AntPathRequestMatcher.antMatcher("/favicon.ico"),
                                AntPathRequestMatcher.antMatcher("/*.js"),
                                AntPathRequestMatcher.antMatcher("/*.css"),
                                AntPathRequestMatcher.antMatcher("/*.png"),
                                AntPathRequestMatcher.antMatcher("/*.jpg"),
                                AntPathRequestMatcher.antMatcher("/assets/**"))
                        .permitAll()

                        // Auth public
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/login"),
                                AntPathRequestMatcher.antMatcher("/api/auth/register"),
                                AntPathRequestMatcher.antMatcher("/api/auth/verify-2fa"),
                                AntPathRequestMatcher.antMatcher("/api/auth/resend-2fa"),
                                AntPathRequestMatcher.antMatcher("/api/auth/set-password"),
                                AntPathRequestMatcher.antMatcher("/api/auth/resend-activation"),
                                AntPathRequestMatcher.antMatcher("/api/auth/validate-token/**"))
                        .permitAll()

                        // API sécurisées
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/role")).authenticated()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/**")).hasRole("ADMIN")
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/client/**")).hasRole("CLIENT")
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/employee/**")).hasRole("EMPLOYEE")
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/crypto/**")).hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/qr-payments/**")).hasRole("CLIENT")

                        // Le reste nécessite l'auth
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http,
                                             @Qualifier("customUserDetailsService") CustomUserDetailsService customUserDetailsService,
                                             PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);
        return authManagerBuilder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }
}
