import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { describe, expect, it, vi } from 'vitest';
import { renderApp } from '@tests/test-utils';
import { COUNTER_DEVICE, DEVICE_TOKEN } from '@tests/mocks/data/auth';
import { api } from '@tests/mocks/handlers/auth.handlers';
import { server } from '@tests/mocks/server';

describe('Trang máy quầy', () => {
  it('TC-03: máy tính bảng mới chưa đăng ký — yêu cầu quản lý đăng nhập để đăng ký ngay khi mở màn hình', async () => {
    const deviceCheck = vi.fn();
    server.use(
      http.get(api('/devices/current'), () => {
        deviceCheck();
        return HttpResponse.json({ registered: false });
      }),
    );
    const user = userEvent.setup();
    const { router } = renderApp('/counter');

    expect(
      await screen.findByRole('heading', { name: 'Máy chưa được đăng ký với câu lạc bộ' }),
    ).toBeInTheDocument();
    expect(screen.getByText(/Vui lòng nhờ quản lý đăng nhập trên máy này/)).toBeInTheDocument();
    expect(screen.queryByLabelText('Tên đăng nhập')).not.toBeInTheDocument();
    // Máy chưa từng có mã máy quầy: biết ngay là chưa đăng ký, không cần hỏi máy chủ.
    expect(deviceCheck).not.toHaveBeenCalled();

    await user.click(screen.getByRole('link', { name: 'Quản lý đăng nhập để đăng ký máy' }));
    expect(
      await screen.findByRole('heading', { name: 'Đăng ký máy quầy · Bước 1/2' }),
    ).toBeInTheDocument();
    expect(router.state.location.pathname).toBe('/counter/register');
  });

  it('mã máy quầy đã bị thu hồi: báo chưa đăng ký và xóa mã cũ khỏi trình duyệt', async () => {
    window.localStorage.setItem('fo.counterDeviceToken', 'ma-cu-da-thu-hoi');
    renderApp('/counter');

    expect(
      await screen.findByRole('heading', { name: 'Máy chưa được đăng ký với câu lạc bộ' }),
    ).toBeInTheDocument();
    expect(window.localStorage.getItem('fo.counterDeviceToken')).toBeNull();
  });

  it('máy quầy đã đăng ký: hiển thị đăng nhập tại quầy kèm tên máy và câu lạc bộ', async () => {
    window.localStorage.setItem('fo.counterDeviceToken', DEVICE_TOKEN);
    server.use(
      http.get(api('/devices/current'), () =>
        HttpResponse.json({ registered: true, device: COUNTER_DEVICE }),
      ),
    );
    renderApp('/counter');

    expect(await screen.findByTestId('device-banner')).toHaveTextContent('Quầy lễ tân 1');
    expect(screen.getByRole('heading', { name: 'Đăng nhập tại quầy' })).toBeInTheDocument();
    expect(screen.getByLabelText('Tên đăng nhập')).toBeInTheDocument();
  });

  it('không hỏi được máy chủ: báo lỗi và cho thử lại', async () => {
    window.localStorage.setItem('fo.counterDeviceToken', DEVICE_TOKEN);
    let calls = 0;
    server.use(
      http.get(api('/devices/current'), () => {
        calls += 1;
        return calls === 1
          ? HttpResponse.error()
          : HttpResponse.json({ registered: true, device: COUNTER_DEVICE });
      }),
    );
    const user = userEvent.setup();
    renderApp('/counter');

    expect(await screen.findByText('Không kiểm tra được trạng thái máy quầy.')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Thử lại' }));
    expect(await screen.findByTestId('device-banner')).toBeInTheDocument();
  });
});
