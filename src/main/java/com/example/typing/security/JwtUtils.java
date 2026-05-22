package com.example.typing.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;
    private final long REGISTER_TOKEN_EXPIRY = 1000 * 60 * 10;

    /**
     * 【ログインJWTトークンの取得】
     * @return
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * 【ログインJWTトークンの作成】
     * @param id
     * @return
     */
    public String generateToken(Long id) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getKey())
                .compact();
    }

        /**
     * [ログインJWTトークンの有効性確認]
     * @param token
     * @return
     */

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 【ログインJWTトークンからユーザーIDの取得】
     * @param token
     * @return
     */
    public Long getUserIdFromToken(String token) {
        String subject = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return Long.parseLong(subject);
    }

    /**
     * 【ユーザー登録用JWTの作成】
     * scope=REGISTER クレームを含む10分限定トークン
     * @param email
     * @return
     */
    public String generateRegisterToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REGISTER_TOKEN_EXPIRY);
        return Jwts.builder()
                .setSubject(email)
                .claim("scope", "REGISTER")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getKey())
                .compact();
    }

    /**
     * 【登録用JWTの検証とメールアドレスの取得】
     * 署名・有効期限・scope=REGISTER をすべて検証する
     * @param token
     * @return メールアドレス
     * @throws JwtException トークンが無効な場合
     */
    public String getEmailFromRegisterToken(String token) {
        io.jsonwebtoken.Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        if (!"REGISTER".equals(claims.get("scope"))) {
            throw new JwtException("Invalid token scope");
        }
        return claims.getSubject();
    }


}
