import { act, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { describe, expect, it, vi } from 'vitest';
import { serverActivity } from '@/services/http/serverActivity';
import { useAuthStore } from '@/store/authStore';
import { renderApp } from '@tests/test-utils';
import { ACCESS_TOKEN, managerSession } from '@tests/mocks/data/auth';
import { api } from '@tests/mocks/handlers/auth.handlers';
import { server } from '@tests/mocks/server';

const MINUTE = 60_000;

function signedIn() {
  useAuthStore.setState({ accessToken: ACCESS_TOKEN, session: managerSession, notice: null });
  serverActivity.mark();
}

describe('Hết phiên sau 30 phút không thao tác', () => {
  it('cảnh báo trước 60 giây; bấm "Tiếp tục làm việc" thì giữ phiên ở máy chủ', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const keepAlive = vi.fn();
    server.use(
      http.get(api('/auth/me'), ({ request }) => {
        keepAlive(request.headers.get('Authorization'));
        return HttpResponse.json(managerSession);
      }),
    );
    signedIn();
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    renderApp('/');
    await screen.findByRole('heading', { name: 'Xin chào, Lê Thị Quản Lý' });

    await act(async () => {
      vi.advanceTimersByTime(29 * MINUTE);
    });
    expect(
      await screen.findByRole('dialog', { name: 'Phiên làm việc sắp hết hạn' }),
    ).toBeInTheDocument();
    expect(screen.getByText(/Phiên sẽ tự kết thúc sau 60 giây/)).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Tiếp tục làm việc' }));
    expect(keepAlive).toHaveBeenCalledWith(`Bearer ${ACCESS_TOKEN}`);
    await act(async () => {
      vi.advanceTimersByTime(1000);
    });
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('không thao tác đủ 30 phút thì tự đăng xuất và báo lý do ở trang đăng nhập', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const logout = vi.fn();
    server.use(
      http.post(api('/auth/logout'), () => {
        logout();
        return new HttpResponse(null, { status: 204 });
      }),
    );
    signedIn();
    const { router } = renderApp('/');
    await screen.findByRole('heading', { name: 'Xin chào, Lê Thị Quản Lý' });

    await act(async () => {
      vi.advanceTimersByTime(30 * MINUTE + 1000);
    });

    expect(
      await screen.findByText(
        'Phiên làm việc đã hết hạn do không thao tác trong 30 phút. Vui lòng đăng nhập lại.',
      ),
    ).toBeInTheDocument();
    expect(logout).toHaveBeenCalledTimes(1);
    expect(router.state.location.pathname).toBe('/login');
    expect(window.sessionStorage.getItem('fo.accessToken')).toBeNull();
  });

  it('máy chủ báo phiên đã hết hạn thì đăng xuất ngay và hiển thị thông báo của máy chủ', async () => {
    server.use(
      http.get(api('/auth/me'), () =>
        HttpResponse.json(
          {
            status: 401,
            code: 'SESSION_EXPIRED',
            message:
              'Phiên làm việc đã hết hạn do không thao tác trong 30 phút. Vui lòng đăng nhập lại.',
          },
          { status: 401 },
        ),
      ),
    );
    useAuthStore.setState({ accessToken: ACCESS_TOKEN, session: null, notice: null });
    const { router } = renderApp('/');

    expect(await screen.findByText('Phiên đã hết hạn')).toBeInTheDocument();
    expect(router.state.location.pathname).toBe('/login');
  });

  it('đăng xuất từ menu tài khoản', async () => {
    signedIn();
    const user = userEvent.setup();
    renderApp('/');
    await user.click(await screen.findByRole('button', { name: 'Tài khoản của tôi' }));
    await user.click(screen.getByRole('button', { name: 'Đăng xuất' }));

    expect(await screen.findByText('Bạn đã đăng xuất.')).toBeInTheDocument();
  });
});
