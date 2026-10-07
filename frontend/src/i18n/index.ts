import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import common from './locales/vi/common.json';
import validation from './locales/vi/validation.json';

/** Giao diện dùng tiếng Việt; thông báo lỗi nghiệp vụ do máy chủ trả về cũng bằng tiếng Việt. */
void i18n.use(initReactI18next).init({
  resources: { vi: { common, validation } },
  lng: 'vi',
  fallbackLng: 'vi',
  ns: ['common', 'validation'],
  defaultNS: 'common',
  interpolation: { escapeValue: false },
  returnNull: false,
});

export default i18n;
