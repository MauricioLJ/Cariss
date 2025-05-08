package com.mauledji.app.cariss.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component // Indica que esta clase será gestionada como un componente por Spring (puede inyectarse con @Autowired)
public class JwtUtil {

    // Lee el valor de la propiedad jwt.secret desde application.properties
    @Value("${jwt.secret}")
    private String secretKey;

    // Tiempo de expiración del token, también desde application.properties
    @Value("${jwt.expiration}")
    private long expirationTime;

    // Método interno que convierte la clave secreta a un objeto SecretKey válido para firmar tokens JWT
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8); // Convierte la clave en bytes
        return Keys.hmacShaKeyFor(keyBytes); // Usa el algoritmo HMAC SHA para generar la clave
    }

    /**
     * Genera un JWT válido con el usuario como "subject"
     * @param subject el identificador que irá dentro del token (normalmente el username)
     * @return el JWT en formato String
     */
    public String generateToken(String subject, String fullName) {
        return Jwts.builder()
            .setSubject(subject) // Define el subject del token (quién es el usuario)
            .claim("fullName", fullName) // Incluye el nombre completo en los claims
            .setIssuedAt(new Date(System.currentTimeMillis())) // Fecha de emisión
            .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Fecha de expiración
            .signWith(getSigningKey()) // Firma el token con la clave secreta
            .compact(); // Genera el token final como string
    }

    /**
     * Extrae el username (subject) desde un token JWT
     * @param token JWT recibido
     * @return el subject del token, normalmente el username
     */
    public String extractUsername(String token) {
        Claims claims = Jwts.parserBuilder() // Inicia el parser de JWT
            .setSigningKey(getSigningKey()) // Usa la misma clave para verificar
            .build()
            .parseClaimsJws(token) // Parsea y valida el token
            .getBody(); // Obtiene el contenido (claims) del token
        return claims.getSubject(); // Devuelve el subject (username)
    }

    /**
     * Verifica si un token JWT es válido (firma correcta y no expirado)
     * @param token JWT a verificar
     * @return true si es válido, false si tiene errores
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Firma esperada
                .build()
                .parseClaimsJws(token); // Lanza excepción si es inválido
            return true; // Si no hubo excepción, el token es válido
        } catch (Exception e) {
            return false; // Si hay cualquier error, el token no es válido
        }
    }
}
