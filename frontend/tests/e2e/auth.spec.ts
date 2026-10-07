import { expect, test, type Page, type TestInfo } from '@playwright/test';
import { fileURLToPath } from 'node:url';
import { resetLoginLocks, sql } from './support/db';

/**
 * NCL-01-CN-001 Đăng nhập hệ thống — kịch bản Given–When–Then trên giao diện thật, máy chủ thật, cơ sở dữ liệu thật.
 * Ảnh chụp luồng chính được lưu vào docs/qa/NCL-01-CN-001/screenshots cho báo cáo kiểm thử.
 */
const PASSWORD = 'Fitness@2026';
const SCREENSHOT_DIR = fileURLToPath(
  new URL('../../../docs/qa/NCL-01-CN-001/screenshots/', import.meta.url),
);

async function snap(page: Page, testInfo: TestInfo, name: string) {
  await page.screenshot({
    path: `${SCREENSHOT_DIR}${testInfo.project.name}-${name}.png`,
    fullPage: true,
  });
}

async function expectNoHorizontalScroll(page: Page) {
  const overflow = await page.evaluate(
    () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
  );
  expect(overflow, 'trang không được tràn ngang').toBeLessThanOrEqual(0);
}

async function login(page: Page, username: string, password: string) {
  await page.getByLabel('Tên đăng nhập').fill(username);
  await page.getByLabel('Mật khẩu', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'Đăng nhập', exact: true }).click();
}

test.afterAll(() => resetLoginLocks());

test('Đăng nhập trên máy tính văn phòng / điện thoại (S1, S2)', async ({ page }, testInfo) => {
  await page.goto('/login');
  await expect(page.getByRole('heading', { name: 'Đăng nhập', exact: true })).toBeVisible();
  await expectNoHorizontalScroll(page);
  await snap(page, testInfo, '01-dang-nhap');

  await login(page, 'hlv.caugiay', PASSWORD);
  await expect(page.getByRole('heading', { name: 'Xin chào, Hoàng Gia Huy' })).toBeVisible();
  await expect(page.getByTestId('home-role')).toContainText(
    'Huấn luyện viên cá nhân · Fitness Cầu Giấy',
  );
  await expect(page.getByText('Lịch tập cá nhân')).toBeVisible();
  await expectNoHorizontalScroll(page);
  await snap(page, testInfo, '02-trang-chinh-hlv');

  const client = testInfo.project.name.startsWith('mobile') ? 'điện thoại' : 'máy tính văn phòng';
  await expect(page.getByText(`Phiên làm việc trên ${client}`)).toBeVisible();
});

test('TC-03 → TC-01: máy tính bảng mới tại quầy được quản lý đăng ký, sau đó lễ tân đăng nhập', async ({
  page,
}, testInfo) => {
  // TC-03 Given: máy tính bảng mới chưa được đăng ký. When: lễ tân mở màn hình đăng nhập.
  await page.goto('/counter');
  await expect(
    page.getByRole('heading', { name: 'Máy chưa được đăng ký với câu lạc bộ' }),
  ).toBeVisible();
  // Then: hệ thống yêu cầu quản lý đăng nhập để đăng ký máy với câu lạc bộ trước khi dùng.
  await expect(page.getByRole('link', { name: 'Quản lý đăng nhập để đăng ký máy' })).toBeVisible();
  await expect(page.getByLabel('Tên đăng nhập')).toHaveCount(0);
  await expectNoHorizontalScroll(page);
  await snap(page, testInfo, '03-may-quay-chua-dang-ky');

  await page.getByRole('link', { name: 'Quản lý đăng nhập để đăng ký máy' }).click();
  await expect(page.getByRole('heading', { name: 'Đăng ký máy quầy · Bước 1/2' })).toBeVisible();
  await login(page, 'quanly.caugiay', PASSWORD);

  await expect(page.getByRole('heading', { name: 'Đăng ký máy quầy · Bước 2/2' })).toBeVisible();
  await expect(page.getByRole('radio', { name: 'Fitness Cầu Giấy' })).toBeChecked();
  const deviceName = `Quầy E2E ${testInfo.project.name} ${Date.now() % 100000}`;
  await page.getByLabel('Tên máy quầy').fill(deviceName);
  await expectNoHorizontalScroll(page);
  await snap(page, testInfo, '04-dang-ky-may-buoc-2');
  await page.getByRole('button', { name: 'Đăng ký máy' }).click();

  await expect(
    page.getByText(
      `Đã đăng ký máy quầy "${deviceName}" cho Fitness Cầu Giấy. Lễ tân có thể đăng nhập.`,
    ),
  ).toBeVisible();
  await expect(page.getByTestId('device-banner')).toContainText(deviceName);
  await snap(page, testInfo, '05-dang-nhap-tai-quay');

  // TC-01 Given: lễ tân có tài khoản đang hoạt động. When: đăng nhập đúng trên máy quầy đã đăng ký.
  await login(page, 'letan.caugiay', PASSWORD);
  // Then: mở trang chính theo vai trò lễ tân và ghi nhận phiên gắn với máy quầy đã đăng ký.
  await expect(page.getByRole('heading', { name: 'Xin chào, Phạm Thu Hà' })).toBeVisible();
  await expect(page.getByTestId('home-role')).toContainText(
    `Lễ tân · ${deviceName} · Fitness Cầu Giấy`,
  );
  await expect(page.getByTestId('function-groups')).toContainText('Ra vào câu lạc bộ');
  await expect(page.getByTestId('function-groups')).not.toContainText('Phê duyệt');
  await expect(page.getByText('Phiên làm việc trên máy quầy')).toBeVisible();
  await expectNoHorizontalScroll(page);
  await snap(page, testInfo, '06-trang-chinh-le-tan');

  const [session] = sql(
    `SELECT s.client_type, d.name, b.name FROM user_sessions s
       JOIN users u ON u.id = s.user_id JOIN counter_devices d ON d.id = s.device_id JOIN branches b ON b.id = s.branch_id
      WHERE u.username = 'letan.caugiay' AND s.ended_at IS NULL ORDER BY s.created_at DESC LIMIT 1`,
  );
  expect(session).toEqual(['COUNTER', deviceName, 'Fitness Cầu Giấy']);

  await page.getByRole('button', { name: 'Tài khoản của tôi' }).click();
  await page.getByRole('button', { name: 'Đăng xuất' }).click();
  await expect(page.getByText('Bạn đã đăng xuất.')).toBeVisible();
});

test('TC-03: lễ tân đăng nhập trên máy chưa đăng ký bị từ chối, được hướng dẫn nhờ quản lý', async ({
  page,
}, testInfo) => {
  await page.goto('/login');
  await login(page, 'letan.haibatrung', PASSWORD);
  const alert = page.getByRole('alert');
  await expect(alert).toContainText('Máy chưa được đăng ký');
  await expect(alert.getByRole('link', { name: 'Đăng ký máy quầy' })).toBeVisible();
  await snap(page, testInfo, '07-le-tan-may-chua-dang-ky');
});

test('TC-02: nhập sai mật khẩu năm lần liên tiếp, lần thứ sáu bị tạm khóa 15 phút và báo thời gian chờ', async ({
  page,
}, testInfo) => {
  const username = testInfo.project.name.startsWith('mobile') ? 'kythuat.caugiay' : 'ketoan';
  await page.goto('/login');

  for (let attempt = 1; attempt <= 4; attempt++) {
    await login(page, username, 'sai-mat-khau');
    await expect(page.getByRole('alert')).toContainText('Tên đăng nhập hoặc mật khẩu không đúng.');
  }
  await login(page, username, 'sai-mat-khau');
  await expect(page.getByRole('alert')).toContainText('Tài khoản tạm khóa');

  // Lần thứ sáu, kể cả mật khẩu đúng.
  await page.getByLabel('Mật khẩu', { exact: true }).fill(PASSWORD);
  await page.getByLabel('Tên đăng nhập').fill(username);
  const lockedButton = page.getByRole('button', { name: /Thử lại sau \d\d:\d\d/ });
  await expect(lockedButton).toBeDisabled();
  const alert = page.getByRole('alert');
  await expect(alert).toContainText(
    'Tài khoản tạm khóa do nhập sai mật khẩu 5 lần liên tiếp. Vui lòng thử lại sau 15 phút.',
  );
  await expect(alert).toContainText(/Có thể thử lại sau: 1[45]:\d\d/);
  await expectNoHorizontalScroll(page);
  await snap(page, testInfo, '08-tam-khoa-15-phut');

  // Máy chủ cũng từ chối lần thứ sáu dù mật khẩu đúng (gọi API trực tiếp, bỏ qua nút đã bị khóa).
  const response = await page.request.post('/api/v1/auth/login', {
    data: { username, password: PASSWORD, clientType: 'OFFICE' },
  });
  expect(response.status()).toBe(423);
  expect(Number(response.headers()['retry-after'])).toBeGreaterThan(14 * 60);
  expect((await response.json()).code).toBe('ACCOUNT_TEMPORARILY_LOCKED');

  const [state] = sql(
    `SELECT failed_login_attempts, locked_until > UTC_TIMESTAMP() FROM users WHERE username = '${username}'`,
  );
  expect(state).toEqual(['5', '1']);
});

test('TC-04: mọi lần đăng nhập thành công hoặc thất bại đều có nhật ký người thực hiện, nội dung, thời điểm', async ({
  page,
}, testInfo) => {
  // Thời điểm trong cơ sở dữ liệu lưu theo UTC, dạng 'YYYY-MM-DD HH:MM:SS'.
  const marker = new Date(Date.now() - 2000).toISOString().slice(0, 19).replace('T', ' ');
  await page.goto('/login');
  await login(page, 'tuvan.caugiay', 'sai-mat-khau');
  await expect(page.getByRole('alert')).toBeVisible();
  await login(page, 'tuvan.caugiay', PASSWORD);
  await expect(page.getByRole('heading', { name: 'Xin chào, Vũ Đức Tư' })).toBeVisible();

  const rows = sql(
    `SELECT l.event_type, coalesce(l.reason_code, ''), u.username, l.client_type, l.detail, l.occurred_at IS NOT NULL
       FROM auth_audit_logs l JOIN users u ON u.id = l.user_id
      WHERE l.username = 'tuvan.caugiay' AND l.occurred_at >= '${marker}' ORDER BY l.id`,
  );
  const client = testInfo.project.name.startsWith('mobile') ? 'MOBILE' : 'OFFICE';
  const clientLabel = client === 'MOBILE' ? 'điện thoại' : 'máy tính văn phòng';
  expect(rows).toEqual([
    [
      'LOGIN_FAILED',
      'INVALID_PASSWORD',
      'tuvan.caugiay',
      client,
      'Đăng nhập thất bại: sai mật khẩu (lần 1/5 liên tiếp)',
      '1',
    ],
    [
      'LOGIN_SUCCEEDED',
      '',
      'tuvan.caugiay',
      client,
      `Đăng nhập thành công trên ${clientLabel}`,
      '1',
    ],
  ]);

  // Nhật ký chỉ được thêm mới (QTN-02).
  expect(() => sql(`DELETE FROM auth_audit_logs WHERE username = 'tuvan.caugiay'`)).toThrow();
});
