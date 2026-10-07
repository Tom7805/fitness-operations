import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import { useAuthStore } from '@/store/authStore';
import { renderApp } from '@tests/test-utils';
import {
  ACCESS_TOKEN,
  COUNTER_DEVICE,
  DEVICE_NOT_REGISTERED_MESSAGE,
  DEVICE_TOKEN,
  errorBody,
  receptionistSession,
} from '@tests/mocks/data/auth';
import { api } from '@tests/mocks/handlers/auth.handlers';
import { server } from '@tests/mocks/server';

describe('Trang đăng nhập', () => {
  it('TC-01: lễ tân đăng nhập trên máy quầy đã đăng ký thì mở trang chính theo vai trò lễ tân, phiên gắn với máy quầy', async () => {
    window.localStorage.setItem('fo.counterDeviceToken', DEVICE_TOKEN);
    const loginRequests: {
      deviceToken: string | null;
      authorization: string | null;
      body: unknown;
    }[] = [];
    server.use(
      http.get(api('/devices/current'), ({ request }) =>
        request.headers.get('X-Device-Token') === DEVICE_TOKEN
          ? HttpResponse.json({ registered: true, device: COUNTER_DEVICE })
          : HttpResponse.json({ registered: false }),
      ),
      http.post(api('/auth/login'), async ({ request }) => {
        loginRequests.push({
          deviceToken: request.headers.get('X-Device-Token'),
          authorization: request.headers.get('Authorization'),
          body: await request.json(),
        });
        return HttpResponse.json({
          accessToken: ACCESS_TOKEN,
          tokenType: 'Bearer',
          expiresAt: receptionistSession.expiresAt,
          session: receptionistSession,
        });
      }),
    );
    const user = userEvent.setup();
    renderApp('/login');

    // Màn hình nhận ra máy quầy (thiết kế S3).
    const banner = await screen.findByTestId('device-banner');
    expect(banner).toHaveTextContent('Máy quầy · Quầy lễ tân 1');
    expect(banner).toHaveTextContent('Fitness Cầu Giấy');
    expect(screen.getByRole('heading', { name: 'Đăng nhập tại quầy' })).toBeInTheDocument();

    await user.type(screen.getByLabelText('Tên đăng nhập'), 'letan.caugiay');
    await user.type(screen.getByLabelText('Mật khẩu'), 'Fitness@2026');
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));

    expect(
      await screen.findByRole('heading', { name: 'Xin chào, Phạm Thu Hà' }),
    ).toBeInTheDocument();
    expect(screen.getByTestId('home-role')).toHaveTextContent(
      'Lễ tân · Quầy lễ tân 1 · Fitness Cầu Giấy',
    );
    expect(screen.getByTestId('workplace')).toHaveTextContent('Quầy lễ tân 1 · Fitness Cầu Giấy');

    const groups = within(screen.getByTestId('function-groups'));
    expect(groups.getByText('Ra vào câu lạc bộ')).toBeInTheDocument();
    expect(groups.getByText('Thu tiền và chốt ca')).toBeInTheDocument();
    expect(groups.queryByText('Phê duyệt')).not.toBeInTheDocument();
    expect(groups.queryByText('Tài khoản và phân quyền')).not.toBeInTheDocument();

    expect(loginRequests).toHaveLength(1);
    expect(loginRequests[0]).toMatchObject({
      deviceToken: DEVICE_TOKEN,
      authorization: null,
      body: { username: 'letan.caugiay', password: 'Fitness@2026', clientType: 'OFFICE' },
    });
    expect(window.sessionStorage.getItem('fo.accessToken')).toBe(ACCESS_TOKEN);
  });

  it('máy tính văn phòng: không có dải máy quầy, có liên kết tới trang máy quầy', async () => {
    renderApp('/login');

    expect(await screen.findByRole('heading', { name: 'Đăng nhập' })).toBeInTheDocument();
    expect(screen.queryByTestId('device-banner')).not.toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Mở trang máy quầy' })).toHaveAttribute(
      'href',
      '/counter',
    );
  });

  it('TC-03: lễ tân đăng nhập trên máy chưa đăng ký được hướng dẫn nhờ quản lý đăng ký máy', async () => {
    server.use(
      http.post(api('/auth/login'), () =>
        HttpResponse.json(errorBody(403, 'DEVICE_NOT_REGISTERED', DEVICE_NOT_REGISTERED_MESSAGE), {
          status: 403,
        }),
      ),
    );
    const user = userEvent.setup();
    renderApp('/login');

    await user.type(await screen.findByLabelText('Tên đăng nhập'), 'letan.caugiay');
    await user.type(screen.getByLabelText('Mật khẩu'), 'Fitness@2026');
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));

    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent('Máy chưa được đăng ký');
    expect(alert).toHaveTextContent(DEVICE_NOT_REGISTERED_MESSAGE);
    expect(within(alert).getByRole('link', { name: 'Đăng ký máy quầy' })).toHaveAttribute(
      'href',
      '/counter/register',
    );
  });

  it('hiển thị lý do khi bị đưa về trang đăng nhập do hết phiên', async () => {
    useAuthStore.setState({
      notice: {
        reason: 'expired',
        message:
          'Phiên làm việc đã hết hạn do không thao tác trong 30 phút. Vui lòng đăng nhập lại.',
      },
    });
    renderApp('/login');

    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent('Phiên đã hết hạn');
    expect(alert).toHaveTextContent('không thao tác trong 30 phút');
  });

  it('chưa đăng nhập mà mở trang chính thì được đưa về trang đăng nhập', async () => {
    const { router } = renderApp('/');
    expect(await screen.findByRole('heading', { name: 'Đăng nhập' })).toBeInTheDocument();
    expect(router.state.location.pathname).toBe('/login');
  });
});
