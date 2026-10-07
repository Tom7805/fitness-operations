import { MonitorX } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';
import { buttonVariants } from '@/components/ui/Button';
import { ROUTES } from '@/constants/routes';

/** Thiết kế S4: máy quầy chưa đăng ký — yêu cầu quản lý đăng nhập để đăng ký máy (TC-03). */
export function UnregisteredDeviceNotice() {
  const { t } = useTranslation();
  return (
    <section
      className="flex flex-col items-center gap-4 text-center"
      aria-labelledby="unregistered-title"
    >
      <span className="flex size-14 items-center justify-center rounded-full bg-warning-soft text-warning-soft-foreground">
        <MonitorX className="size-7" aria-hidden />
      </span>
      <h1 id="unregistered-title" className="text-xl font-bold tracking-tight">
        {t('auth.device.unregisteredTitle')}
      </h1>
      <p className="text-sm leading-relaxed text-muted-foreground">
        {t('auth.device.unregisteredBody')}
      </p>
      <Link
        to={ROUTES.counterRegister}
        className={buttonVariants({ size: 'lg', className: 'w-full' })}
      >
        {t('auth.device.registerAction')}
      </Link>
      <p className="text-sm text-muted-foreground">
        {t('auth.login.standardLink')}{' '}
        <Link to={ROUTES.login} className="font-medium text-primary hover:underline">
          {t('auth.login.standardLinkAction')}
        </Link>
      </p>
    </section>
  );
}
