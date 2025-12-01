package desarrollador.api.config;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.nio.charset.StandardCharsets;

@RestControllerAdvice
public class JsonCharsetAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(org.springframework.core.MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  org.springframework.core.MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        MediaType type = response.getHeaders().getContentType();
        if (type == null || ("application".equals(type.getType()) && "json".equals(type.getSubtype()))) {
            response.getHeaders().setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        }
        return body;
    }
}
