import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

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
  // 🚀 스프링 부트(백엔드)와 통신하기 위한 프록시 설정
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8888', // 본인의 스프링 부트 포트 번호에 맞게 확인!
        changeOrigin: true
      }
    }
  }
})
