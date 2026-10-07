-- =====================================================================
-- Chuẩn bị MySQL 8 trên máy phát triển — chạy MỘT LẦN bằng tài khoản root
-- (MySQL Workbench: mở kết nối root, dán toàn bộ file này, bấm biểu tượng tia sét).
-- Bảng do ứng dụng tự tạo (Flyway) khi chạy backend; không tạo bảng bằng tay.
-- =====================================================================

-- Database cho ứng dụng và database riêng cho kiểm thử tự động (bị xóa sạch mỗi lần chạy test).
CREATE DATABASE IF NOT EXISTS fitness_operations
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS fitness_operations_test
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Tài khoản riêng của ứng dụng, không dùng root.
CREATE USER IF NOT EXISTS 'fitness'@'localhost' IDENTIFIED BY 'fitness';
GRANT ALL PRIVILEGES ON fitness_operations.* TO 'fitness'@'localhost';
GRANT ALL PRIVILEGES ON fitness_operations_test.* TO 'fitness'@'localhost';
FLUSH PRIVILEGES;

-- MySQL trên Windows bật binary log, khi đó tài khoản không phải root chỉ tạo được trigger
-- (dùng cho nhật ký chỉ thêm mới, QTN-02) nếu bật tùy chọn này. SET PERSIST giữ lại sau khi khởi động lại.
SET PERSIST log_bin_trust_function_creators = 1;
