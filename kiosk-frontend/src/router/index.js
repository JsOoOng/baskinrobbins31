import { createRouter, createWebHistory } from 'vue-router'

// 1. Customer (키오스크) 화면 불러오기
import HomeView from '@/views/customer/HomeView.vue'
import MenuView from '@/views/customer/MenuView.vue'

// 2. Branch (지점) 화면 불러오기
import BranchLogin from '@/views/branch/BranchLogin.vue'

// 3. Head (본사) 화면 불러오기
import HeadLogin from '@/views/head/HeadLogin.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // === [키오스크 경로] ===
    {
      path: '/',          // 기본 주소 (localhost:5173/)
      name: 'KioskHome',
      component: HomeView
    },
    {
      path: '/menu',      // 메뉴판 화면
      name: 'KioskMenu',
      component: MenuView
    },
    
    // === [지점 관리자 경로] ===
    {
      path: '/branch',    // 지점 로그인 (localhost:5173/branch)
      name: 'BranchLogin',
      component: BranchLogin
    },

    // === [본사 관리자 경로] ===
    {
      path: '/head',      // 본사 로그인 (localhost:5173/head)
      name: 'HeadLogin',
      component: HeadLogin
    }
  ]
})

export default router