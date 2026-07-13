import './assets/main.css'

import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import pinia from './stores/pinia'

const app = createApp(App)

/*
 * Pinia를 Router보다 먼저 등록합니다.
 *
 * Router Guard에서 인증 Store를 사용하기 때문입니다.
 */
app.use(pinia)
app.use(router)

app.mount('#app')