import { useTranslation } from 'react-i18next';
import type { Session } from '@/features/auth';

/** Nơi làm việc của phiên: máy quầy, một câu lạc bộ, nhiều câu lạc bộ hoặc toàn chuỗi. */
export function useWorkplaceLabel(session: Session): string {
  const { t } = useTranslation();
  if (session.device) {
    return `${session.device.name} · ${session.device.branch?.name ?? ''}`;
  }
  if (session.activeBranch) {
    return session.activeBranch.name;
  }
  if (session.user.allBranches) {
    return t('layout.chainWide');
  }
  return session.user.branches.length > 0
    ? t('layout.branchCount', { count: session.user.branches.length })
    : t('layout.noBranch');
}
