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
    },
    { path: '/payment', 
      name: 'payment', 
      component: () => import('../views/customer/OrderConfirmView.vue') },
    { path: '/order-confirm', 
      name: 'orderConfirm', 
      component: () => import('../views/customer/OrderConfirmView.vue') },
      
    // ----------------------------------------------------
    // 📱 BRANCH (지점용 주소)
    // ----------------------------------------------------
     // 지점 로그인
    {
      path: '/branch/login',
      name: 'branch-login',
      component: () => import('../views/branch/BranchLogin.vue')
    },

    // 지점 메인
    {
      path: '/branch/main',
      name: 'branch-main',
      component: () => import('../views/branch/BranchMain.vue')
    },
    // 주문 관리
    {
      path: '/branch/order',
      name: 'branch-order',
      component: () => import('../views/branch/OrderView.vue')
    },
    
    {
      path: '/branch/inventory',
      name: 'branch-inventory',
      component: () => import('../views/branch/BranchInventory.vue')
    },

    {
      path: '/branch/statistics',
      name: 'branch-statistics',
      component: () => import('../views/branch/BranchStatistics.vue')
    },

    {
      path: '/branch/menu',
      name: 'branch-menu',
      component: () => import('../views/branch/BranchMenu.vue')
    }
  ]
})

export default router