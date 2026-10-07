import { ArrowRight, Clock } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';
import { Alert } from '@/components/ui/Alert';
import { useAuth } from '@/hooks/useAuth';
import { useDocumentTitle } from '@/hooks/useDocumentTitle';
import { useWorkplaceLabel } from '@/layouts/MainLayout/useWorkplaceLabel';
import { functionGroupsFor } from '@/layouts/MainLayout/sidebarItems';
import { cn } from '@/lib/cn';
import type { RoleCode } from '@/constants/roles';
import type { Session } from '@/features/auth';

/** Thiết kế S6: trang chính theo vai trò — chỉ hiện nhóm chức năng của các vai trò người dùng có (QTN-01). */
export function HomePage() {
  const { t } = useTranslation();
  useDocumentTitle(t('home.functionsTitle'));
  const { session, roleCodes } = useAuth();
  if (!session) {
    return null;
  }
  return <HomeContent session={session} roleCodes={roleCodes} />;
}

function HomeContent({ session, roleCodes }: { session: Session; roleCodes: RoleCode[] }) {
  const { t } = useTranslation();
  const workplace = useWorkplaceLabel(session);
  const groups = functionGroupsFor(roleCodes);
  const roleNames = session.user.roles.map((role) => role.name).join(', ');

  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col gap-1">
        <h1 className="text-2xl font-bold tracking-tight sm:text-3xl">
          {t('home.greeting', { name: session.user.fullName })}
        </h1>
        <p className="text-muted-foreground" data-testid="home-role">
          {roleNames ? `${roleNames} · ${workplace}` : workplace}
        </p>
      </section>

      <section className="flex flex-col gap-4" aria-labelledby="functions-title">
        <h2 id="functions-title" className="text-lg font-semibold">
          {t('home.functionsTitle')}
        </h2>
        {groups.length === 0 ? (
          <Alert variant="warning">{t('home.noRoles')}</Alert>
        ) : (
          <ul className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3" data-testid="function-groups">
            {groups.map((group) => {
              const content = (
                <>
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="font-semibold">{group.title}</h3>
                    {group.to ? (
                      <ArrowRight className="mt-0.5 size-4 shrink-0 text-primary" aria-hidden />
                    ) : (
                      <span className="shrink-0 rounded-full bg-muted px-2 py-0.5 text-xs text-muted-foreground">
                        {t('home.comingSoon')}
                      </span>
                    )}
                  </div>
                  <p className="text-sm text-muted-foreground">{group.description}</p>
                </>
              );
              const className = cn(
                'flex h-full flex-col gap-2 rounded-xl border bg-card p-4 shadow-xs',
                group.to
                  ? 'transition-colors hover:border-primary hover:bg-primary-soft/40'
                  : 'opacity-80',
              );
              return (
                <li key={group.key}>
                  {group.to ? (
                    <Link to={group.to} className={className}>
                      {content}
                    </Link>
                  ) : (
                    <div className={className} aria-disabled="true">
                      {content}
                    </div>
                  )}
                </li>
              );
            })}
          </ul>
        )}
      </section>

      <p className="flex items-center gap-2 text-sm text-muted-foreground">
        <Clock className="size-4 shrink-0" aria-hidden />
        {t('home.sessionInfo', {
          client: t(`clientType.${session.clientType}`),
          minutes: Math.round(session.idleTimeoutSeconds / 60),
        })}
      </p>
    </div>
  );
}
