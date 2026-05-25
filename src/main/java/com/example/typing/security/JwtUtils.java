package com.example.typing.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;
    private static final long OTP_TOKEN_EXPIRY = 1000 * 60 * 10;

    /**
     * 【JWT署名キーの取得】
     * 
     * @return
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * 【ログインJWTトークンの作成】
     * 
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
     * 
     * @param token
     * @return
     */

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getKey()).build()
                    .parseClaimsJws(token).getBody();
            return claims.get("scope") == null;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 【ログインJWTトークンからユーザーIDの取得】
     * 
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
     * 
     * @param email
     * @return
     */
    public String generateRegisterToken(String email) {
        return generateScopedToken(email, "REGISTER", OTP_TOKEN_EXPIRY);
    }

    /**
     * 【登録用JWTの検証とメールアドレスの取得】
     * 署名・有効期限・scope=REGISTER をすべて検証する
     * 
     * @param token
     * @return メールアドレス
     * @throws JwtException トークンが無効な場合
     */
    public String getEmailFromRegisterToken(String token) {
        return getEmailFromScopedToken(token, "REGISTER");
    }

    /**
     * 【パスワード再設定用JWTの作成】
     *
     * @param email パスワード再設定対象のメールアドレス
     * @return パスワード再設定用JWT
     */
    public String generatePasswordResetToken(String email) {
        return generateScopedToken(email, "PASSWORD_RESET", OTP_TOKEN_EXPIRY);
    }

    /**
     * 【パスワード再設定用JWTの検証とメールアドレスの取得】
     *
     * @param token パスワード再設定用JWT
     * @return パスワード再設定対象のメールアドレス
     * @throws JwtException トークンが無効な場合
     */
    public String getEmailFromPasswordResetToken(String token) {
        return getEmailFromScopedToken(token, "PASSWORD_RESET");
    }

    /**
     * 【JWTの作成】
     * scope クレームを含むJWTを作成する
     *
     * @param email  JWTに設定するメールアドレス
     * @param scope  JWTの用途
     * @param expiry 有効期限
     * @return クレームを含むJWT
     */
    private String generateScopedToken(String email, String scope, long expiry) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiry);

        return Jwts.builder()
                .setSubject(email)
                .claim("scope", scope)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getKey())
                .compact();
    }

    /**
     * 【JWTの検証とメールアドレスの取得】
     * 署名・有効期限・scope をすべて検証する
     *
     * @param token         JWT
     * @param expectedScope 期待するscope
     * @return JWTに設定されたメールアドレス
     * @throws JwtException トークンが無効な場合
     */
    private String getEmailFromScopedToken(String token, String expectedScope) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        if (!expectedScope.equals(claims.get("scope"))) {
            throw new JwtException("Invalid token scope");
        }

        return claims.getSubject();
    }

}
