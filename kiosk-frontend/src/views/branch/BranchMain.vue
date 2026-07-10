<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const user = JSON.parse(localStorage.getItem('user'))

const moveOrder = () => {
  router.push('/branch/order')
}

const goMenu = () => {
    router.push('/branch/menu')
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')

  alert('로그아웃되었습니다.')

  router.push('/branch/login')
}
</script>

<template>
  <div class="container">

    <h1>{{ user.storeName }}</h1>

    <p>관리자 : {{ user.name }}</p>

    <hr>

    <button @click="moveOrder">
      주문 관리
    </button>

    <button @click="goMenu">
      메뉴 관리
    </button>

    <button>
      재고 관리
    </button>

    <button>
      매출 관리
    </button>

    <hr>

    <button @click="logout">
      로그아웃
    </button>

  </div>
</template>

<style scoped>

.container{
    width:600px;
    margin:50px auto;
}

button{
    width:200px;
    padding:12px;
    margin:10px 0;
    display:block;
}

</style>