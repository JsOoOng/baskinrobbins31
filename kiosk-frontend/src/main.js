/**
 * [모듈 흐름 안내] main
 * 역할: Vue 애플리케이션을 만들고 Pinia·Router·다국어 설정을 등록한 뒤 화면을 시작한다.
 * 호출 흐름: index.html -> createApp(App) -> Pinia/Router/i18n 등록 -> #app에 최상위 화면 렌더링
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import './assets/main.css'

import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import pinia from './stores/pinia'
import i18n from './i18n'

const app = createApp(App)

/*
 * Pinia를 Router보다 먼저 등록합니다.
 *
 * Router Guard에서 인증 Store를 사용하기 때문입니다.
 */
app.use(pinia)
app.use(router)
app.use(i18n)

app.mount('#app')