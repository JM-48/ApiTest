package desarrollador.api.security;

import desarrollador.api.models.Role;
import desarrollador.api.models.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtServicio {
    private final Key key;

    public JwtServicio(Environment env) {
        String secret = env.getProperty("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else {
            byte[] bytes = Decoders.BASE64.decode(secret);
            this.key = Keys.hmacShaKeyFor(bytes);
        }
    }

    public String generar(User user, long ttlMillis) {
        long now = System.currentTimeMillis();
        Date iat = new Date(now);
        Date exp = new Date(now + ttlMillis);
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole() == null ? Role.CLIENT.name() : user.getRole().name())
                .setIssuedAt(iat)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public io.jsonwebtoken.Claims validar(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
