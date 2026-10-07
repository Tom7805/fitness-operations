import { act, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { describe, expect, it, vi } from 'vitest';
import { renderApp } from '@tests/test-utils';
import {
  errorBody,
  INVALID_CREDENTIALS_MESSAGE,
  LOCKED_MESSAGE,
  managerSession,
} from '@tests/mocks/data/auth';
import { api } from '@tests/mocks/handlers/auth.handlers';
import { server } from '@tests/mocks/server';

describe('Form đăng nhập', () => {
  it('kiểm tra trường bắt buộc trước khi gửi, không gọi máy chủ', async () => {
    const login = vi.fn();
    server.use(http.post(api('/auth/login'), () => login()));
    const user = userEvent.setup();
    renderApp('/login');

    await user.click(await screen.findByRole('button', { name: 'Đăng nhập' }));

    expect(await screen.findByText('Vui lòng nhập tên đăng nhập.')).toBeInTheDocument();
    expect(screen.getByText('Vui lòng nhập mật khẩu.')).toBeInTheDocument();
    expect(screen.getByLabelText('Tên đăng nhập')).toHaveAttribute('aria-invalid', 'true');
    expect(login).not.toHaveBeenCalled();
  });

  it('sai mật khẩu: báo lỗi, xóa mật khẩu và đưa con trỏ về ô mật khẩu', async () => {
    server.use(
      http.post(api('/auth/login'), () =>
        HttpResponse.json(errorBody(401, 'INVALID_CREDENTIALS', INVALID_CREDENTIALS_MESSAGE), {
          status: 401,
        }),
      ),
    );
    const user = userEvent.setup();
    renderApp('/login');

    await user.type(await screen.findByLabelText('Tên đăng nhập'), 'ketoan');
    await user.type(screen.getByLabelText('Mật khẩu'), 'sai-mat-khau');
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));

    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent('Không đăng nhập được');
    expect(alert).toHaveTextContent(INVALID_CREDENTIALS_MESSAGE);
    expect(screen.getByLabelText('Mật khẩu')).toHaveValue('');
    expect(screen.getByLabelText('Mật khẩu')).toHaveFocus();
  });

  it('TC-02: lần thứ sáu bị tạm khóa 15 phút — hiển thị thời gian chờ đếm ngược và khóa nút đăng nhập', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    server.use(
      http.post(api('/auth/login'), () =>
        HttpResponse.json(
          errorBody(423, 'ACCOUNT_TEMPORARILY_LOCKED', LOCKED_MESSAGE, {
            lockedUntil: '2026-10-08T01:15:00Z',
            retryAfterSeconds: 900,
          }),
          { status: 423, headers: { 'Retry-After': '900' } },
        ),
      ),
    );
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    renderApp('/login');

    await user.type(await screen.findByLabelText('Tên đăng nhập'), 'ketoan');
    await user.type(screen.getByLabelText('Mật khẩu'), 'sai-mat-khau');
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));

    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent('Tài khoản tạm khóa');
    expect(alert).toHaveTextContent(LOCKED_MESSAGE);
    expect(alert).toHaveTextContent('Có thể thử lại sau: 15:00');
    expect(screen.getByRole('button', { name: 'Thử lại sau 15:00' })).toBeDisabled();

    await act(async () => {
      vi.advanceTimersByTime(61_000);
    });
    expect(screen.getByRole('button', { name: 'Thử lại sau 13:59' })).toBeDisabled();

    await act(async () => {
      vi.advanceTimersByTime(839_000);
    });
    expect(screen.getByRole('button', { name: 'Đăng nhập' })).toBeEnabled();
    expect(
      screen.getByText('Đã hết thời gian tạm khóa. Bạn có thể đăng nhập lại.'),
    ).toBeInTheDocument();
  });

  it('khóa là của tài khoản: đổi sang tên đăng nhập khác thì đăng nhập được ngay', async () => {
    let attempt = 0;
    server.use(
      http.post(api('/auth/login'), () => {
        attempt += 1;
        return attempt === 1
          ? HttpResponse.json(
              errorBody(423, 'ACCOUNT_TEMPORARILY_LOCKED', LOCKED_MESSAGE, {
                retryAfterSeconds: 900,
              }),
              { status: 423 },
            )
          : HttpResponse.json({
              accessToken: 'token',
              tokenType: 'Bearer',
              expiresAt: managerSession.expiresAt,
              session: managerSession,
            });
      }),
    );
    const user = userEvent.setup();
    renderApp('/login');

    const username = await screen.findByLabelText('Tên đăng nhập');
    await user.type(username, 'ketoan');
    await user.type(screen.getByLabelText('Mật khẩu'), 'x');
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));
    expect(await screen.findByRole('button', { name: /Thử lại sau/ })).toBeDisabled();

    await user.clear(username);
    await user.type(username, 'quanly.caugiay');
    expect(screen.queryByText('Tài khoản tạm khóa')).not.toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));

    expect(await screen.findByText('Xin chào, Lê Thị Quản Lý')).toBeInTheDocument();
  });

  it('mất kết nối máy chủ: báo không kết nối được', async () => {
    server.use(http.post(api('/auth/login'), () => HttpResponse.error()));
    const user = userEvent.setup();
    renderApp('/login');

    await user.type(await screen.findByLabelText('Tên đăng nhập'), 'ketoan');
    await user.type(screen.getByLabelText('Mật khẩu'), 'Fitness@2026');
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Không kết nối được máy chủ');
  });

  it('nút hiện/ẩn mật khẩu', async () => {
    const user = userEvent.setup();
    renderApp('/login');

    const password = await screen.findByLabelText('Mật khẩu');
    expect(password).toHaveAttribute('type', 'password');
    await user.click(screen.getByRole('button', { name: 'Hiện mật khẩu' }));
    expect(password).toHaveAttribute('type', 'text');
    await user.click(screen.getByRole('button', { name: 'Ẩn mật khẩu' }));
    await waitFor(() => expect(password).toHaveAttribute('type', 'password'));
  });
});
