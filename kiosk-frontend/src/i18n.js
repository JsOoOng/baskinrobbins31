/**
 * [모듈 흐름 안내] i18n
 * 역할: 한국어·영어·일본어 메시지와 기본 언어를 Vue에 제공한다.
 * 호출 흐름: main.js에서 등록 -> Vue 화면의 번역 키 조회 -> 선택 언어 또는 한국어 문구 표시
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
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
