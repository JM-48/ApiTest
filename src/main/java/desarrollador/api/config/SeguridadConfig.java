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
                        .requestMatchers("/api/v1/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/productos/**").permitAll()
                        .requestMatchers("/api/v1/imagenes", "/api/v1/imagenes/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/users/me").authenticated()
                        .requestMatchers("/api/v1/cart/**").hasAnyRole("CLIENT","ADMIN","USER")
                        .requestMatchers("/api/v1/checkout/**").hasAnyRole("CLIENT","ADMIN","USER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/users").hasAnyRole("ADMIN","USER_AD")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/users").hasAnyRole("ADMIN","USER_AD")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/users/**").hasAnyRole("ADMIN","USER_AD")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/users/**").hasAnyRole("ADMIN","USER_AD")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/users/**").hasAnyRole("ADMIN","USER_AD")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/orders/admin").hasAnyRole("ADMIN","VENDEDOR")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/orders/**").hasAnyRole("ADMIN","VENDEDOR")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(401);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Unauthorized\"}");
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
                : java.util.Arrays.asList("http://localhost:5173", "https://web-gg-accesorios-1.onrender.com");
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(java.util.Arrays.asList("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(java.util.Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
