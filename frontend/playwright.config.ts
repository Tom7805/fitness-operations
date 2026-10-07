import { defineConfig, devices } from '@playwright/test';

/**
 * Kiểm thử đầu-cuối trên hệ thống thật: frontend (Vite) + backend (profile dev) + MySQL 8 trên máy
 * với dữ liệu mẫu R__seed_dev_sample_data.sql. Chạy ở hai kích thước bắt buộc của DoD: 1280px và 360px.
 */
export default defineConfig({
  testDir: './tests/e2e',
  globalSetup: './tests/e2e/global-setup.ts',
  fullyParallel: false,
  workers: 1,
  timeout: 60_000,
  expect: { timeout: 10_000 },
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:5173',
    // Dùng Chrome đã cài trên máy; đặt PW_CHANNEL= (rỗng) để dùng Chromium của Playwright.
    channel: process.env.PW_CHANNEL ?? 'chrome',
    locale: 'vi-VN',
    timezoneId: 'Asia/Ho_Chi_Minh',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    { name: 'desktop-1280', use: { viewport: { width: 1280, height: 800 } } },
    {
      name: 'mobile-360',
      use: {
        viewport: { width: 360, height: 740 },
        isMobile: true,
        hasTouch: true,
        deviceScaleFactor: 2,
        userAgent: devices['Pixel 7'].userAgent,
      },
    },
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: true,
    timeout: 120_000,
  },
});
