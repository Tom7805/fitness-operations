import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import { useAuthStore } from '@/store/authStore';
import { ACCESS_TOKEN, DEVICE_TOKEN, managerSession } from '@tests/mocks/data/auth';
import { api } from '@tests/mocks/handlers/auth.handlers';
import { server } from '@tests/mocks/server';
import { authApi } from '../api/authApi';

describe('authApi', () => {
  it('đăng nhập gửi mã máy quầy nhưng không gửi mã truy cập cũ', async () => {
    window.localStorage.setItem('fo.counterDeviceToken', DEVICE_TOKEN);
    useAuthStore.setState({ accessToken: 'ma-cu' });
    let headers: Headers | undefined;
    server.use(
      http.post(api('/auth/login'), ({ request }) => {
        headers = request.headers;
        return HttpResponse.json({
          accessToken: ACCESS_TOKEN,
          tokenType: 'Bearer',
          session: managerSession,
        });
      }),
    );

    await authApi.login({
      username: 'quanly.caugiay',
      password: 'Fitness@2026',
      clientType: 'OFFICE',
    });

    expect(headers?.get('X-Device-Token')).toBe(DEVICE_TOKEN);
    expect(headers?.get('Authorization')).toBeNull();
  });

  it('yêu cầu cần đăng nhập gửi kèm mã truy cập', async () => {
    useAuthStore.setState({ accessToken: ACCESS_TOKEN });
    let authorization: string | null = null;
    server.use(
      http.get(api('/auth/me'), ({ request }) => {
        authorization = request.headers.get('Authorization');
        return HttpResponse.json(managerSession);
      }),
    );

    await authApi.me();

    expect(authorization).toBe(`Bearer ${ACCESS_TOKEN}`);
  });

  it('chuẩn hóa lỗi máy chủ thành ApiError có mã và thông báo', async () => {
    server.use(
      http.post(api('/auth/login'), () =>
        HttpResponse.json(
          {
            status: 401,
            code: 'INVALID_CREDENTIALS',
            message: 'Tên đăng nhập hoặc mật khẩu không đúng.',
          },
          { status: 401 },
        ),
      ),
    );

    await expect(
      authApi.login({ username: 'x', password: 'y', clientType: 'OFFICE' }),
    ).rejects.toMatchObject({
      name: 'ApiError',
      status: 401,
      code: 'INVALID_CREDENTIALS',
      message: 'Tên đăng nhập hoặc mật khẩu không đúng.',
    });
  });
});
