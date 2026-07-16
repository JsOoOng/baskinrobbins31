import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import fs from 'fs'
import path from 'path'

// application.properties 에서 동적으로 server.port 추출
let backendPort = '8889'; // 기본값
try {
  const propertiesPath = path.resolve(__dirname, '../kiosk/src/main/resources/application.properties');
  if (fs.existsSync(propertiesPath)) {
    const content = fs.readFileSync(propertiesPath, 'utf-8');
    const match = content.match(/^server\.port\s*=\s*(\d+)/m);
    if (match && match[1]) {
      backendPort = match[1];
    }
  }
} catch (e) {
  console.warn('application.properties를 읽을 수 없어 기본 포트를 사용합니다.');
}

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {

    allowedHosts: [
      'trace-discount-appraisal.ngrok-free.dev' // 여기에 ngrok 도메인 추가
    ],
    host: '0.0.0.0',
    proxy: {
      // 이 특별한 접두사가 붙은 데이터 요청만 백엔드로 보냅니다.
      '/proxy-api': {
        target: `http://localhost:${backendPort}`,
        changeOrigin: true,
        // 핵심: 스프링 부트에 도착하기 전에 '/proxy-api'라는 글자를 몰래 지워줍니다.
        rewrite: (path) => path.replace(/^\/proxy-api/, '')
      },
      '/ws': {
        target: `ws://localhost:${backendPort}`,
        ws: true,
        changeOrigin: true
      }
    }
  }
})
