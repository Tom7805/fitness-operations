-- =====================================================================
-- Dữ liệu mẫu CHỈ dùng trên máy phát triển (profile dev nạp thêm classpath:db/seed).
-- Mọi tài khoản dùng chung mật khẩu: Fitness@2026
-- Chạy lại an toàn: chỉ thêm khi chưa có.
-- =====================================================================

INSERT IGNORE INTO branches (code, name, status) VALUES
    ('CLB-CG',  'Fitness Cầu Giấy',     'ACTIVE'),
    ('CLB-HBT', 'Fitness Hai Bà Trưng', 'ACTIVE');

INSERT IGNORE INTO users (username, password_hash, full_name, job_title, phone_number, all_branches) VALUES
    ('admin',            '$2a$12$CYFWuzx2//1wydqp7IgSBuUuh24QiHY4P4gptu02xNJOtXWLeyX4.', 'Trần Quản Trị',     'Quản trị viên',               '0900000001', TRUE),
    ('chuphongtap',      '$2a$12$CYFWuzx2//1wydqp7IgSBuUuh24QiHY4P4gptu02xNJOtXWLeyX4.', 'Nguyễn Văn Chủ',    'Chủ phòng tập',               '0900000002', TRUE),
    ('quanly.caugiay',   '$2a$12$CYFWuzx2//1wydqp7IgSBuUuh24QiHY4P4gptu02xNJOtXWLeyX4.', 'Lê Thị Quản Lý',    'Quản lý câu lạc bộ',          '0900000003', FALSE),
    ('letan.caugiay',    '$2a$12$CYFWuzx2//1wydqp7IgSBuUuh24QiHY4P4gptu02xNJOtXWLeyX4.', 'Phạm Thu Hà',       'Lễ tân',                      '0900000004', FALSE),
    ('letan.haibatrung', '$2a$12$CYFWuzx2//1wydqp7IgSBuUuh24QiHY4P4gptu02xNJOtXWLeyX4.', 'Đỗ Minh Anh',       'Lễ tân',                      '0900000005', FALSE),
    ('tuvan.caugiay',    '$2a$12$CYFWuzx2//1wydqp7IgSBuUuh24QiHY4P4gptu02xNJOtXWLeyX4.', 'Vũ Đức Tư',         'Nhân viên tư vấn',            '0900000006', FALSE),
    ('hlv.caugiay',      '$2a$12$CYFWuzx2//1wydqp7IgSBuUuh24QiHY4P4gptu02xNJOtXWLeyX4.', 'Hoàng Gia Huy',     'Huấn luyện viên cá nhân',     '0900000007', FALSE),
    ('hlvnhom.caugiay',  '$2a$12$CYFWuzx2//1wydqp7IgSBuUuh24QiHY4P4gptu02xNJOtXWLeyX4.', 'Bùi Thanh Yoga',    'Huấn luyện viên lớp nhóm',    '0900000008', FALSE),
    ('ketoan',           '$2a$12$CYFWuzx2//1wydqp7IgSBuUuh24QiHY4P4gptu02xNJOtXWLeyX4.', 'Ngô Thị Kế',        'Kế toán',                     '0900000009', TRUE),
    ('kythuat.caugiay',  '$2a$12$CYFWuzx2//1wydqp7IgSBuUuh24QiHY4P4gptu02xNJOtXWLeyX4.', 'Đinh Văn Kỹ',       'Nhân viên kỹ thuật',          '0900000010', FALSE),
    ('chamsoc.caugiay',  '$2a$12$CYFWuzx2//1wydqp7IgSBuUuh24QiHY4P4gptu02xNJOtXWLeyX4.', 'Mai Chăm Sóc',      'Nhân viên chăm sóc hội viên', '0900000011', FALSE);

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM (VALUES
    ROW('admin',            'ADMIN'),
    ROW('chuphongtap',      'CHAIN_OWNER'),
    ROW('quanly.caugiay',   'CLUB_MANAGER'),
    ROW('letan.caugiay',    'RECEPTIONIST'),
    ROW('letan.haibatrung', 'RECEPTIONIST'),
    ROW('tuvan.caugiay',    'SALES_CONSULTANT'),
    ROW('hlv.caugiay',      'PERSONAL_TRAINER'),
    ROW('hlvnhom.caugiay',  'GROUP_TRAINER'),
    ROW('ketoan',           'ACCOUNTANT'),
    ROW('kythuat.caugiay',  'TECHNICIAN'),
    ROW('chamsoc.caugiay',  'MEMBER_CARE')
) AS seed (username, role_code)
JOIN users u ON u.username = seed.username
JOIN roles r ON r.code = seed.role_code;

INSERT IGNORE INTO user_branch_scopes (user_id, branch_id)
SELECT u.id, b.id
FROM (VALUES
    ROW('quanly.caugiay',   'CLB-CG'),
    ROW('letan.caugiay',    'CLB-CG'),
    ROW('letan.haibatrung', 'CLB-HBT'),
    ROW('tuvan.caugiay',    'CLB-CG'),
    ROW('hlv.caugiay',      'CLB-CG'),
    ROW('hlvnhom.caugiay',  'CLB-CG'),
    ROW('kythuat.caugiay',  'CLB-CG'),
    ROW('chamsoc.caugiay',  'CLB-CG')
) AS seed (username, branch_code)
JOIN users u ON u.username = seed.username
JOIN branches b ON b.code = seed.branch_code;
