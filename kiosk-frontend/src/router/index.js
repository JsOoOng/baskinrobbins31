import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ----------------------------------------------------
    // 📱 CUSTOMER (키오스크 손님용 주소)
    // ----------------------------------------------------
    {
      path: '/',
      name: 'customer-home',
      // 💡 여기서 customer 폴더 안으로 제대로 찾아가도록 경로를 수정했습니다!
      component: () => import('../views/customer/HomeView.vue') 
    },
    {
      path: '/menu',
      name: 'customer-menu',
      // MenuView.vue 파일이 아직 없다면, 나중에 만들면 됩니다. (에러가 나면 일단 이 블록은 주석 처리하세요)
      component: () => import('../views/customer/MenuView.vue')
    },
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
      path: '/branch/menu',
      name: 'branch-menu',
      component: () => import('../views/branch/BranchMenu.vue')
    }
  ]
})

export default router