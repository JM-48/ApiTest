package desarrollador.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final Environment env;

    public WebConfig(Environment env) {
        this.env = env;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String originsProp = env.getProperty("ALLOWED_ORIGINS");
        List<String> origins = (originsProp != null && !originsProp.isBlank())
                ? Arrays.stream(originsProp.split(",")).map(String::trim).toList()
                : Arrays.asList("http://localhost:5173", "https://TU_DOMINIO_WEB");
        registry.addMapping("/**")
                .allowedOrigins(origins.toArray(String[]::new))
                .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                .allowedHeaders("Content-Type","Authorization")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
