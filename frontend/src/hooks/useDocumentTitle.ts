import { useEffect } from 'react';
import { useTranslation } from 'react-i18next';

/** Đặt tiêu đề tab trình duyệt: "Trang · Vận Hành Phòng Tập". */
export function useDocumentTitle(title: string): void {
  const { t } = useTranslation();
  useEffect(() => {
    document.title = `${title} · ${t('app.name')}`;
  }, [title, t]);
}
