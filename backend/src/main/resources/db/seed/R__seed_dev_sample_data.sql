-- =====================================================================
-- Dữ liệu mẫu CHỈ dùng trên máy phát triển (profile dev nạp thêm classpath:db/seed).
-- Mọi tài khoản dùng chung mật khẩu: Fitness@2026
-- Chạy lại an toàn: chỉ thêm khi chưa có.
-- =====================================================================

INSERT INTO branches (code, name, status) VALUES
    ('CLB-CG',  'Fitness Cầu Giấy',     'ACTIVE'),
    ('CLB-HBT', 'Fitness Hai Bà Trưng', 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

INSERT INTO users (username, password_hash, full_name, job_title, phone_number, all_branches) VALUES
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
    ('chamsoc.caugiay',  '$2a$12$CYFWuzx2//1wydqp7IgSBuUuh24QiHY4P4gptu02xNJOtXWLeyX4.', 'Mai Chăm Sóc',      'Nhân viên chăm sóc hội viên', '0900000011', FALSE)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM (VALUES
    ('admin',            'ADMIN'),
    ('chuphongtap',      'CHAIN_OWNER'),
    ('quanly.caugiay',   'CLUB_MANAGER'),
    ('letan.caugiay',    'RECEPTIONIST'),
    ('letan.haibatrung', 'RECEPTIONIST'),
    ('tuvan.caugiay',    'SALES_CONSULTANT'),
    ('hlv.caugiay',      'PERSONAL_TRAINER'),
    ('hlvnhom.caugiay',  'GROUP_TRAINER'),
    ('ketoan',           'ACCOUNTANT'),
    ('kythuat.caugiay',  'TECHNICIAN'),
    ('chamsoc.caugiay',  'MEMBER_CARE')
) AS seed (username, role_code)
JOIN users u ON u.username = seed.username
JOIN roles r ON r.code = seed.role_code
ON CONFLICT DO NOTHING;

INSERT INTO user_branch_scopes (user_id, branch_id)
SELECT u.id, b.id
FROM (VALUES
    ('quanly.caugiay',   'CLB-CG'),
    ('letan.caugiay',    'CLB-CG'),
    ('letan.haibatrung', 'CLB-HBT'),
    ('tuvan.caugiay',    'CLB-CG'),
    ('hlv.caugiay',      'CLB-CG'),
    ('hlvnhom.caugiay',  'CLB-CG'),
    ('kythuat.caugiay',  'CLB-CG'),
    ('chamsoc.caugiay',  'CLB-CG')
) AS seed (username, branch_code)
JOIN users u ON u.username = seed.username
JOIN branches b ON b.code = seed.branch_code
ON CONFLICT DO NOTHING;
