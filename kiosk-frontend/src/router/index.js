/*import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ----------------------------------------------------
    // 📱 1. CUSTOMER (키오스크 손님용 주소)
    // ----------------------------------------------------
    {
      path: '/',
      name: 'customer-home',
      component: () => import('../views/customer/HomeView.vue') // 👈 나중에 이 위치에 파일 만드시면 됩니다!
    },
    {
      path: '/menu',
      name: 'customer-menu',
      component: () => import('../views/customer/MenuView.vue')
    },

    // ----------------------------------------------------
    // 🏢 2. BRANCH (지점 관리자용 주소)
    // ----------------------------------------------------
    {
      path: '/branch/login',
      name: 'branch-login',
      component: () => import('../views/branch/BranchLogin.vue')
    },
    {
      path: '/branch/main',
      name: 'branch-main',
      component: () => import('../views/branch/BranchMain.vue')
    },

    // ----------------------------------------------------
    // 👑 3. HEADQUARTER (본사 관리자용 주소)
    // ----------------------------------------------------
    {
      path: '/head/login',
      name: 'head-login',
      component: () => import('../views/headquarter/HeadLogin.vue')
    },
    {
      path: '/head/main',
      name: 'head-main',
      component: () => import('../views/headquarter/HeadMain.vue')
    }
  ]
})

export default router*/


import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),

  routes: [
    // 기본 접속 시 로그인 화면으로 이동
    {
      path: '/',
      redirect: '/branch/login'
    },

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
      path: '/branch/menu',
      name: 'branch-menu',
      component: () => import('../views/branch/BranchMenu.vue')
    }
  ]
})

export default router