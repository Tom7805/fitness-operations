package com.fitnessops.modules.auth.exception;

import com.fitnessops.common.enums.ErrorCode;
import com.fitnessops.common.exception.BusinessException;
import com.fitnessops.common.exception.GlobalExceptionHandler;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

/** Tài khoản đang tạm khóa đăng nhập do nhập sai mật khẩu nhiều lần liên tiếp; kèm thời gian chờ còn lại. */
@Getter
public class AccountLockedException extends BusinessException {

    private final Instant lockedUntil;
    private final long retryAfterSeconds;

    public AccountLockedException(Instant lockedUntil, Instant now, int maxFailedAttempts) {
        this(lockedUntil, Math.max(1, Duration.between(now, lockedUntil).toSeconds()), maxFailedAttempts);
    }

    private AccountLockedException(Instant lockedUntil, long retryAfterSeconds, int maxFailedAttempts) {
        super(ErrorCode.ACCOUNT_TEMPORARILY_LOCKED,
                "Tài khoản tạm khóa do nhập sai mật khẩu " + maxFailedAttempts + " lần liên tiếp. "
                        + "Vui lòng thử lại sau " + toMinutesCeil(retryAfterSeconds) + " phút.",
                details(lockedUntil, retryAfterSeconds));
        this.lockedUntil = lockedUntil;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    private static long toMinutesCeil(long seconds) {
        return (seconds + 59) / 60;
    }

    private static Map<String, Object> details(Instant lockedUntil, long retryAfterSeconds) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("lockedUntil", lockedUntil);
        details.put(GlobalExceptionHandler.RETRY_AFTER_SECONDS, retryAfterSeconds);
        return details;
    }
}
