import { CircleAlert, ShieldAlert } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router';
import { Alert } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { DEVICE_REGISTRAR_ROLES } from '@/constants/roles';
import { ROUTES } from '@/constants/routes';
import { useDocumentTitle } from '@/hooks/useDocumentTitle';
import { DeviceChecking, DeviceCheckFailed } from '../components/DeviceCheckState';
import { DeviceRegistrationForm } from '../components/DeviceRegistrationForm';
import { LoginForm } from '../components/LoginForm';
import { useRegistrableBranches } from '../hooks/useDeviceRegistration';
import { useLogout } from '../hooks/useLogout';
import { useSessionBootstrap } from '../hooks/useSessionBootstrap';
import type { DeviceRegisteredState, DeviceRegistrationResult, Session } from '../types/auth.types';

const canRegisterDevice = (session: Session) =>
  session.user.roles.some((role) => DEVICE_REGISTRAR_ROLES.includes(role.code));

/**
 * Thiết kế S5: quản lý câu lạc bộ (hoặc quản trị viên) đăng nhập ngay trên máy cần đăng ký, chọn câu lạc bộ và
 * đặt tên máy. Xong thì đăng xuất quản lý để máy sẵn sàng cho lễ tân.
 */
export function DeviceRegistrationPage() {
  const { t } = useTranslation();
  useDocumentTitle(t('auth.register.step1Title'));
  const navigate = useNavigate();
  const logout = useLogout();
  const { session, isLoading, error, retry } = useSessionBootstrap();
  const [denied, setDenied] = useState(false);
  const registrar = session !== null && canRegisterDevice(session);
  const branches = useRegistrableBranches(registrar);

  const endManagerSession = () => logout.mutateAsync({ notice: null });
  const { mutate: logoutMutate, isPending: loggingOut } = logout;

  // Tài khoản không phải quản lý câu lạc bộ hoặc quản trị viên: báo không có quyền và đăng xuất ngay.
  useEffect(() => {
    if (session !== null && !registrar && !loggingOut) {
      logoutMutate({ notice: null }, { onSettled: () => setDenied(true) });
    }
  }, [session, registrar, loggingOut, logoutMutate]);

  const handleRegistered = async (result: DeviceRegistrationResult) => {
    await endManagerSession();
    const state: DeviceRegisteredState = {
      registeredDevice: { name: result.device.name, branchName: result.device.branch?.name ?? '' },
    };
    navigate(ROUTES.counter, { replace: true, state });
  };

  const handleCancel = async () => {
    await endManagerSession();
    navigate(ROUTES.counter, { replace: true });
  };

  if (isLoading) {
    return <DeviceChecking />;
  }
  if (error && !session) {
    return <DeviceCheckFailed message={error.message} onRetry={retry} />;
  }

  if (!session || !registrar) {
    return (
      <div className="flex flex-col gap-6">
        <header className="flex flex-col gap-1.5">
          <h1 className="text-2xl font-bold tracking-tight">{t('auth.register.step1Title')}</h1>
          <p className="text-sm text-muted-foreground">{t('auth.register.step1Description')}</p>
        </header>
        {denied ? (
          <Alert variant="error" icon={<ShieldAlert aria-hidden />}>
            {t('auth.register.notAllowed')}
          </Alert>
        ) : null}
        {/* Đăng nhập thành công đưa thông tin phiên vào store; trang tự chuyển sang bước 2 hoặc báo không có quyền. */}
        <LoginForm onSuccess={() => undefined} />
        <Button variant="ghost" onClick={() => navigate(ROUTES.counter)}>
          {t('auth.register.cancel')}
        </Button>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <header className="flex flex-col gap-1.5">
        <h1 className="text-2xl font-bold tracking-tight">{t('auth.register.step2Title')}</h1>
        <p className="text-sm text-muted-foreground">{t('auth.register.step2Description')}</p>
        <p className="text-sm font-medium">
          {t('auth.register.confirmedBy', { name: session.user.fullName })}
        </p>
      </header>
      {branches.isPending ? <DeviceChecking /> : null}
      {branches.isError ? (
        <Alert variant="error" icon={<CircleAlert aria-hidden />}>
          {branches.error.message}
        </Alert>
      ) : null}
      {branches.data && branches.data.length === 0 ? (
        <Alert variant="warning" icon={<CircleAlert aria-hidden />}>
          {t('auth.register.noBranches')}
        </Alert>
      ) : null}
      {branches.data && branches.data.length > 0 ? (
        <DeviceRegistrationForm
          branches={branches.data}
          onRegistered={(result) => void handleRegistered(result)}
          onCancel={() => void handleCancel()}
          cancelling={logout.isPending}
        />
      ) : null}
    </div>
  );
}
