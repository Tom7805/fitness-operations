import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { describe, expect, it, vi } from 'vitest';
import { renderApp } from '@tests/test-utils';
import {
  BRANCH_CAU_GIAY,
  BRANCH_HAI_BA_TRUNG,
  COUNTER_DEVICE,
  managerSession,
  receptionistSession,
} from '@tests/mocks/data/auth';
import { api } from '@tests/mocks/handlers/auth.handlers';
import { server } from '@tests/mocks/server';

const loginAs = (session: typeof managerSession) =>
  http.post(api('/auth/login'), () =>
    HttpResponse.json({
      accessToken: `token-${session.user.username}`,
      tokenType: 'Bearer',
      expiresAt: session.expiresAt,
      session,
    }),
  );

describe('Đăng ký máy quầy', () => {
  it('quản lý đăng nhập, chọn câu lạc bộ, đặt tên máy; máy lưu mã máy quầy, quản lý được đăng xuất', async () => {
    const logout = vi.fn();
    let registerBody: unknown;
    server.use(
      loginAs(managerSession),
      http.get(api('/devices/registrable-branches'), () => HttpResponse.json([BRANCH_CAU_GIAY])),
      http.post(api('/devices'), async ({ request }) => {
        registerBody = await request.json();
        return HttpResponse.json(
          { device: COUNTER_DEVICE, deviceToken: 'ma-may-quay-moi' },
          { status: 201 },
        );
      }),
      http.post(api('/auth/logout'), ({ request }) => {
        logout(request.headers.get('Authorization'));
        return new HttpResponse(null, { status: 204 });
      }),
    );
    const user = userEvent.setup();
    const { router } = renderApp('/counter/register');

    await user.type(await screen.findByLabelText('Tên đăng nhập'), 'quanly.caugiay');
    await user.type(screen.getByLabelText('Mật khẩu'), 'Fitness@2026');
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));

    expect(
      await screen.findByRole('heading', { name: 'Đăng ký máy quầy · Bước 2/2' }),
    ).toBeInTheDocument();
    expect(screen.getByText('Người xác nhận: Lê Thị Quản Lý')).toBeInTheDocument();
    expect(await screen.findByRole('radio', { name: 'Fitness Cầu Giấy' })).toBeChecked();
    expect(screen.getByLabelText('Tên máy quầy')).toHaveValue('Quầy lễ tân 1');

    await user.click(screen.getByRole('button', { name: 'Đăng ký máy' }));

    expect(
      await screen.findByText(
        'Đã đăng ký máy quầy "Quầy lễ tân 1" cho Fitness Cầu Giấy. Lễ tân có thể đăng nhập.',
      ),
    ).toBeInTheDocument();
    expect(registerBody).toEqual({ branchId: BRANCH_CAU_GIAY.id, name: 'Quầy lễ tân 1' });
    expect(window.localStorage.getItem('fo.counterDeviceToken')).toBe('ma-may-quay-moi');
    expect(logout).toHaveBeenCalledWith('Bearer token-quanly.caugiay');
    expect(window.sessionStorage.getItem('fo.accessToken')).toBeNull();
    expect(router.state.location.pathname).toBe('/counter');
    expect(screen.getByTestId('device-banner')).toHaveTextContent('Quầy lễ tân 1');
  });

  it('quản lý nhiều câu lạc bộ phải chọn câu lạc bộ trước khi đăng ký', async () => {
    const registered = vi.fn();
    server.use(
      loginAs(managerSession),
      http.get(api('/devices/registrable-branches'), () =>
        HttpResponse.json([BRANCH_CAU_GIAY, BRANCH_HAI_BA_TRUNG]),
      ),
      http.post(api('/devices'), () => {
        registered();
        return HttpResponse.json({}, { status: 201 });
      }),
    );
    const user = userEvent.setup();
    renderApp('/counter/register');

    await user.type(await screen.findByLabelText('Tên đăng nhập'), 'quanly.caugiay');
    await user.type(screen.getByLabelText('Mật khẩu'), 'Fitness@2026');
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));
    await screen.findByRole('radio', { name: 'Fitness Hai Bà Trưng' });
    await user.click(screen.getByRole('button', { name: 'Đăng ký máy' }));

    expect(await screen.findByText('Vui lòng chọn câu lạc bộ.')).toBeInTheDocument();
    expect(registered).not.toHaveBeenCalled();
  });

  it('tài khoản không phải quản lý hay quản trị viên: báo không có quyền và tự đăng xuất', async () => {
    const logout = vi.fn();
    server.use(
      loginAs(receptionistSession),
      http.post(api('/auth/logout'), () => {
        logout();
        return new HttpResponse(null, { status: 204 });
      }),
    );
    const user = userEvent.setup();
    renderApp('/counter/register');

    await user.type(await screen.findByLabelText('Tên đăng nhập'), 'letan.caugiay');
    await user.type(screen.getByLabelText('Mật khẩu'), 'Fitness@2026');
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));

    expect(
      await screen.findByText('Chỉ quản lý câu lạc bộ hoặc quản trị viên được đăng ký máy quầy.'),
    ).toBeInTheDocument();
    await vi.waitFor(() => expect(logout).toHaveBeenCalledTimes(1));
    expect(window.sessionStorage.getItem('fo.accessToken')).toBeNull();
    expect(
      screen.getByRole('heading', { name: 'Đăng ký máy quầy · Bước 1/2' }),
    ).toBeInTheDocument();
  });
});
