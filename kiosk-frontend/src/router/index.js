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
    }
  ]
})

export default router