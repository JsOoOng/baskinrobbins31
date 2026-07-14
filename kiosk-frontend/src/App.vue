<script setup>
import { RouterLink, RouterView } from 'vue-router';
import Header from '@/components/common/Header.vue';
import TimeoutModal from '@/components/common/TimeoutModal.vue';
import { onMounted, onUnmounted } from 'vue';
import { Client } from '@stomp/stompjs';
import { ref } from 'vue';

const toastMessage = ref('');
const showToastBox = ref(false);


const showToast = (message)=>{

    toastMessage.value = message;
    showToastBox.value = true;


   

}

let client = null;


onMounted(() => {

  const branchUser =
    JSON.parse(
      localStorage.getItem('branchUser')
    );


  // 지점 로그인 상태가 아니면 연결하지 않음
  if (!branchUser) {
    return;
  }


  client = new Client({

    brokerURL:
      'ws://localhost:8889/ws',

  });


  client.onConnect = () => {


    console.log(
      'WebSocket 연결 성공'
    );


    client.subscribe(
      `/topic/store/${branchUser.storeId}`,
      (message)=>{

          showToast(message.body);

      }
    );


  };


  client.onStompError = (error)=>{

    console.error(
      'WebSocket 오류',
      error
    );

  };


  client.activate();

});



onUnmounted(()=>{

  if(client){

    client.deactivate();

  }

});

// App.vue는 가장 큰 껍데기이므로 특별한 로직 없이 비워둡니다.
</script>

<template>
  <div class="app-container">
    <main class="main-content">
      <router-view />
    </main>
    <TimeoutModal />
    <div 
 v-if="showToastBox"
 class="order-toast"
>
    <div>
        🔔 {{ toastMessage }}
    </div>

    <button @click="showToastBox=false">
        확인
    </button>
</div>
  </div>
</template>

<style>
/* 폰트, 배경색 등 프로젝트 전체에 적용될 공통 CSS만 여기에 작성합니다 */
/* 기본 초기화 */
html,
body,
#app {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100%;
  font-family: 'Pretendard', sans-serif;
  background-color: #f4f4f4; /* 키오스크 기본 배경색 예시 */
}

/* 전체 레이아웃 */
.app-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

/* router-view 영역 */
.main-content {
  width: 100%;
  max-width: 1400px;
  min-height: 100vh;
  padding: 20px;
  box-sizing: border-box;
}

.order-toast{

    position:fixed;

    right:30px;

    bottom:30px;

    background:white;

    padding:20px 30px;

    border-radius:15px;

    box-shadow:
    0 5px 20px rgba(0,0,0,0.2);

    font-size:18px;

    z-index:9999;
}
.order-toast button {

    margin-top: 15px;

    width: 100%;

    padding: 10px 0;

    border: none;

    border-radius: 10px;

    background-color: #4caf50;

    color: white;

    font-size: 16px;

    font-weight: bold;

    cursor: pointer;

}


.order-toast button:hover {

    background-color: #43a047;

}


.order-toast button:active {

    transform: scale(0.98);

}

</style>