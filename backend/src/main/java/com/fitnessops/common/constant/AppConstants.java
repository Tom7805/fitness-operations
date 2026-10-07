package com.fitnessops.common.constant;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Hằng số nghiệp vụ dùng chung. */
public final class AppConstants {

    /** Múi giờ hiển thị thời điểm cho người dùng; dữ liệu luôn lưu theo UTC. */
    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").withZone(BUSINESS_ZONE);

    private AppConstants() {
    }
}
