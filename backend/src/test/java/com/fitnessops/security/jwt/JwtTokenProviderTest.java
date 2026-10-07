package com.fitnessops.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fitnessops.config.properties.JwtProperties;
import com.fitnessops.security.jwt.JwtTokenProvider.TokenParseResult.Status;
import com.fitnessops.support.MutableClock;
import io.jsonwebtoken.security.WeakKeyException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("unit-test-secret-key-with-at-least-256-bits!".getBytes(StandardCharsets.UTF_8));

    private final MutableClock clock = new MutableClock();
    private final JwtTokenProvider provider =
            new JwtTokenProvider(new JwtProperties(SECRET, "fitness-operations"), clock);

    @Test
    void issuedToken_parsesBackToSameSessionAndUser() {
        UUID sessionId = UUID.randomUUID();
        Instant now = clock.instant();
        String token = provider.issue(sessionId, 42L, now, now.plus(Duration.ofHours(12)));

        JwtTokenProvider.TokenParseResult result = provider.parse(token);

        assertThat(result.status()).isEqualTo(Status.VALID);
        assertThat(result.claims().sessionId()).isEqualTo(sessionId);
        assertThat(result.claims().userId()).isEqualTo(42L);
        assertThat(result.claims().expiresAt()).isEqualTo(now.plus(Duration.ofHours(12)));
    }

    @Test
    void tokenPastAbsoluteExpiry_isExpired() {
        Instant now = clock.instant();
        String token = provider.issue(UUID.randomUUID(), 1L, now, now.plus(Duration.ofHours(12)));
        clock.advance(Duration.ofHours(12).plusSeconds(1));

        assertThat(provider.parse(token).status()).isEqualTo(Status.EXPIRED);
    }

    @Test
    void tamperedMalformedOrForeignIssuerToken_isInvalid() {
        Instant now = clock.instant();
        String token = provider.issue(UUID.randomUUID(), 1L, now, now.plusSeconds(60));
        String[] parts = token.split("\\.");
        String tamperedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"iss\":\"fitness-operations\",\"sub\":\"2\"}".getBytes(StandardCharsets.UTF_8));
        String tampered = parts[0] + "." + tamperedPayload + "." + parts[2];
        JwtTokenProvider otherIssuer = new JwtTokenProvider(new JwtProperties(SECRET, "other"), clock);

        assertThat(provider.parse(tampered).status()).isEqualTo(Status.INVALID);
        assertThat(provider.parse("not-a-jwt").status()).isEqualTo(Status.INVALID);
        assertThat(otherIssuer.parse(token).status()).isEqualTo(Status.INVALID);
    }

    @Test
    void secretShorterThan256Bits_failsFast() {
        String weak = Base64.getEncoder().encodeToString("too-short".getBytes(StandardCharsets.UTF_8));
        JwtProperties properties = new JwtProperties(weak, "fitness-operations");
        assertThatThrownBy(() -> new JwtTokenProvider(properties, clock)).isInstanceOf(WeakKeyException.class);
    }
}
