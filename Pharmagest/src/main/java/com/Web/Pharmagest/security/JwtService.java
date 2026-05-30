package com.Web.Pharmagest.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    // Récupère les valeurs depuis application.properties
    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // ===== GÉNÉRATION DU TOKEN =====

    // Génère un token simple (sans claims supplémentaires)
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // Génère un token avec des claims supplémentaires
    // (ex: on peut y ajouter le rôle, le nom, l'id...)
    public String generateToken(Map<String, Object> extraClaims,
                                UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                // "sub" = subject = identifiant de l'utilisateur
                .subject(userDetails.getUsername())
                // Date de création du token
                .issuedAt(new Date(System.currentTimeMillis()))
                // Date d'expiration = maintenant + 24h
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                // Signature avec notre clé secrète
                .signWith(getSigningKey())
                .compact();
    }

    // ===== VALIDATION DU TOKEN =====

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Vérifie que le token appartient bien à cet utilisateur
        // ET que le token n'est pas expiré
        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    // ===== EXTRACTION DES INFORMATIONS =====

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Convertit la clé secrète (String base64) en clé cryptographique
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}