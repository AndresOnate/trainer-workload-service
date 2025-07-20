package com.epam.gymapp.config;


import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;

@Component
public class JwtUtils {

    private final String jwtSecret = "supersecretkey1234567890123456supersecretkey1234567890123456";

    public String getUsernameFromJwt(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecret.getBytes()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getSubjectFromJwt(String token) {
        return Jwts.parser().setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
