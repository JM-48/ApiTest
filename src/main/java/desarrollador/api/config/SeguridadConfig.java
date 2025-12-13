package desarrollador.api.config;

import desarrollador.api.security.JwtFiltro;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
@EnableMethodSecurity
public class SeguridadConfig {
    private final JwtFiltro jwtFiltro;

    public SeguridadConfig(JwtFiltro jwtFiltro) {
        this.jwtFiltro = jwtFiltro;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/productos/**").permitAll()
                        .requestMatchers("/imagenes/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/users/me").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/productos/**").hasAnyRole("ADMIN","PROD_AD")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/productos/**").hasAnyRole("ADMIN","PROD_AD")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/productos/**").hasAnyRole("ADMIN","PROD_AD")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/users").hasAnyRole("ADMIN","USER_AD")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/users").hasAnyRole("ADMIN","USER_AD")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/users/**").hasAnyRole("ADMIN","USER_AD")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/users/**").hasAnyRole("ADMIN","USER_AD")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(401);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Token requerido\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(403);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"forbidden\",\"message\":\"Acceso denegado\"}");
                        })
                )
                .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public OpenAPI openApi() {
        String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("API Catalogo").version("v1"))
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        ));
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource(org.springframework.core.env.Environment env) {
        String originsProp = env.getProperty("ALLOWED_ORIGINS");
        java.util.List<String> origins = (originsProp != null && !originsProp.isBlank())
                ? java.util.Arrays.stream(originsProp.split(",")).map(String::trim).toList()
                : java.util.Arrays.asList("http://localhost:5173", "https://TU_DOMINIO_WEB");
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(java.util.Arrays.asList("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(java.util.Arrays.asList("Content-Type","Authorization"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
