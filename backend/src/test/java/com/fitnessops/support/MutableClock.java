package com.fitnessops.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/** Đồng hồ kiểm thử: đứng yên cho tới khi được cho chạy tới, để kiểm tra thời gian khóa và hết phiên. */
public class MutableClock extends Clock {

    private volatile Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

    public void reset() {
        now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public void advance(Duration duration) {
        now = now.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return now;
    }
}
