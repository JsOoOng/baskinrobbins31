import { createI18n } from 'vue-i18n'
import ko from './locales/ko.json'
import en from './locales/en.json'
import ja from './locales/ja.json'

const i18n = createI18n({
  legacy: false, // Vue 3 Composition API 사용 시 false
  locale: 'ko', // 기본 언어
  fallbackLocale: 'ko', // 키를 찾을 수 없을 때 사용할 언어
  messages: {
    ko,
    en,
    ja
  }
})

export default i18n
