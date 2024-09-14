package br.com.verbi.verbi.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import br.com.verbi.verbi.entity.User;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

@Component
public class JWTGenerator {

    private String jwtSecret;
    private long jwtExpirationDate;

    public JWTGenerator() {
        // Carregar variáveis de ambiente
        this.jwtSecret = System.getenv("JWT_SECRET");
        this.jwtExpirationDate = Long.parseLong(System.getenv("JWT_EXPIRATION"));
    }

    public String generateToken(String email) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .signWith(key())
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // extract username from JWT token
    public String getUsername(String token) {

        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // validate JWT token
    public boolean validateToken(String token) {
        Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parse(token);
        return true;

    }
}
