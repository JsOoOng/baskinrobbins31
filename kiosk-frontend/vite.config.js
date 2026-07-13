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
    proxy: {
      '/api': {
        target: `http://localhost:${backendPort}`,
        changeOrigin: true
      },
      '/ws': {
        target: `ws://localhost:${backendPort}`,
        ws: true,
        changeOrigin: true
      }
    }
  }
})
