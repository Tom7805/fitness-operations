import { CircleCheck } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Outlet } from 'react-router';
import logo from '@/assets/images/logo.svg';

/** Bố cục trang đăng nhập (thiết kế S1): cột thương hiệu từ 1024px, form ở giữa trên màn hình nhỏ. */
export function AuthLayout() {
  const { t } = useTranslation();
  const highlights = [
    t('app.highlights.access'),
    t('app.highlights.training'),
    t('app.highlights.payments'),
  ];

  return (
    <div className="grid min-h-dvh lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)]">
      <aside className="hidden flex-col justify-between bg-brand p-12 text-brand-foreground lg:flex">
        <div className="flex items-center gap-3">
          <img src={logo} alt="" className="size-10" />
          <span className="text-lg font-semibold">{t('app.name')}</span>
        </div>
        <div className="flex max-w-md flex-col gap-6">
          <p className="text-3xl leading-tight font-bold">{t('app.tagline')}</p>
          <ul className="flex flex-col gap-3">
            {highlights.map((item) => (
              <li key={item} className="flex items-center gap-3 text-base opacity-90">
                <CircleCheck className="size-5 shrink-0" aria-hidden />
                {item}
              </li>
            ))}
          </ul>
        </div>
        <p className="text-sm opacity-70">
          © {new Date().getFullYear()} {t('app.name')}
        </p>
      </aside>

      <main className="flex flex-col items-center px-4 py-8 sm:justify-center sm:py-12">
        <div className="w-full max-w-md">
          <div className="mb-8 flex items-center gap-3 lg:hidden">
            <img src={logo} alt="" className="size-9" />
            <span className="text-lg font-semibold">{t('app.name')}</span>
          </div>
          <div className="rounded-xl border bg-card p-6 shadow-sm sm:p-8">
            <Outlet />
          </div>
        </div>
      </main>
    </div>
  );
}
