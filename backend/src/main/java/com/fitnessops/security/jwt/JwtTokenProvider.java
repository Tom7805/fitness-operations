package com.fitnessops.security.jwt;

import com.fitnessops.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * Cấp và kiểm chứng mã truy cập JWT ký HMAC-SHA256. Mã chỉ mang mã phiên và mã tài khoản; quyền và
 * phạm vi được đọc lại từ phiên ở máy chủ cho mỗi yêu cầu nên thay đổi có hiệu lực ngay.
 */
@Component
public class JwtTokenProvider {

    private static final String SESSION_CLAIM = "sid";

    private final SecretKey key;
    private final String issuer;
    private final JwtParser parser;

    /**
     * @throws io.jsonwebtoken.security.WeakKeyException khi khóa ngắn hơn 256 bit — ứng dụng dừng khởi động
     */
    public JwtTokenProvider(JwtProperties properties, Clock clock) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.issuer = properties.issuer();
        this.parser = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .clock(() -> Date.from(clock.instant()))
                .build();
    }

    public String issue(UUID sessionId, Long userId, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .claim(SESSION_CLAIM, sessionId.toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public TokenParseResult parse(String token) {
        try {
            Claims claims = parser.parseSignedClaims(token).getPayload();
            return TokenParseResult.valid(new JwtClaims(
                    UUID.fromString(claims.get(SESSION_CLAIM, String.class)),
                    Long.valueOf(claims.getSubject()),
                    claims.getIssuedAt().toInstant(),
                    claims.getExpiration().toInstant()));
        } catch (ExpiredJwtException ex) {
            return TokenParseResult.expired();
        } catch (JwtException | IllegalArgumentException | NullPointerException ex) {
            return TokenParseResult.invalid();
        }
    }

    /** Kết quả kiểm chứng mã truy cập. */
    public record TokenParseResult(Status status, JwtClaims claims) {

        public enum Status { VALID, EXPIRED, INVALID }

        static TokenParseResult valid(JwtClaims claims) {
            return new TokenParseResult(Status.VALID, claims);
        }

        static TokenParseResult expired() {
            return new TokenParseResult(Status.EXPIRED, null);
        }

        static TokenParseResult invalid() {
            return new TokenParseResult(Status.INVALID, null);
        }
    }
}
