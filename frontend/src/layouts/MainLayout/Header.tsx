import { Building2, MonitorSmartphone } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';
import logo from '@/assets/images/logo.svg';
import { ROUTES } from '@/constants/routes';
import type { Session } from '@/features/auth';
import { UserMenu } from './UserMenu';
import { useWorkplaceLabel } from './useWorkplaceLabel';

export function Header({ session }: { session: Session }) {
  const { t } = useTranslation();
  const workplace = useWorkplaceLabel(session);
  const WorkplaceIcon = session.device ? MonitorSmartphone : Building2;

  return (
    <header className="sticky top-0 z-30 border-b bg-card/95 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-6xl items-center gap-3 px-4">
        <Link to={ROUTES.home} className="flex min-w-0 items-center gap-2">
          <img src={logo} alt="" className="size-8 shrink-0" />
          <span className="hidden font-semibold sm:inline">{t('app.name')}</span>
        </Link>
        <div
          className="ml-auto flex min-w-0 items-center gap-2 rounded-full bg-primary-soft px-3 py-1.5 text-sm text-primary-soft-foreground"
          data-testid="workplace"
        >
          <WorkplaceIcon className="size-4 shrink-0" aria-hidden />
          <span className="truncate">{workplace}</span>
        </div>
        <UserMenu user={session.user} />
      </div>
    </header>
  );
}
