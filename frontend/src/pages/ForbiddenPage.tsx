import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';
import { buttonVariants } from '@/components/ui/Button';
import { ROUTES } from '@/constants/routes';

export function ForbiddenPage() {
  const { t } = useTranslation();
  return (
    <div className="flex min-h-dvh flex-col items-center justify-center gap-4 px-4 text-center">
      <p className="text-5xl font-bold text-destructive">403</p>
      <h1 className="text-xl font-bold">{t('errors.forbiddenTitle')}</h1>
      <p className="text-sm text-muted-foreground">{t('errors.forbiddenBody')}</p>
      <Link to={ROUTES.home} className={buttonVariants()}>
        {t('errors.backHome')}
      </Link>
    </div>
  );
}
