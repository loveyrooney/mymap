package com.mymap.auth;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
@PropertySource("classpath:mymap_jwt.properties")
public class JwtProvider {
    @Value("${jwt.public-key}")
    private String jwtPublicKey;

    @Value("${jwt.private-key}")
    private String jwtPrivateKey;
    private final long EXPIRED_TIME = 1000 * 60 * 15; // 15분

    // Base64로 인코딩된 개인키를 로드
    public RSAPrivateKey loadPrivateKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(jwtPrivateKey);  // 문자열을 디코딩
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    // Base64로 인코딩된 공개키를 로드
    public RSAPublicKey loadPublicKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(jwtPublicKey);  // 문자열을 디코딩
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    // JWT 생성 (서명: RS256)
    public String generateToken(String userNo) throws Exception {
        RSAPrivateKey privateKey = loadPrivateKey();

        // JWT 생성
        return Jwts.builder()
                .setSubject(userNo)  // 페이로드에 사용자 정보 추가
                .setIssuedAt(new Date())  // 발행 시간
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRED_TIME)) // 만료 시간
                .signWith(privateKey, SignatureAlgorithm.RS256)  // 서명 (RS256)
                .compact();
    }

    // JWT 검증
    public Claims validateToken(String token) throws Exception {
        RSAPublicKey publicKey = loadPublicKey();

        // JWT 검증
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(publicKey)  // 서명에 사용할 공개키
                    .build()
                    .parseClaimsJws(token);  // JWT 파싱 및 검증
            return claimsJws.getBody();  // JWT의 페이로드 반환
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }
}
