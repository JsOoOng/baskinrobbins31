<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { RouterLink, RouterView, useRoute } from 'vue-router';
import Header from '@/components/common/Header.vue';
import TimeoutModal from '@/components/common/TimeoutModal.vue';
import StaffCallButton from '@/components/common/StaffCallButton.vue';
import { Client } from '@stomp/stompjs';

const toastMessage = ref('');
const showToastBox = ref(false);

const route = useRoute();
const isKiosk = computed(() => {
  if (!route) return false;
  const path = route.path;
  if (path.startsWith('/branch') || path.startsWith('/head') || path === '/') {
    return false;
  }
  return true;
});


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


  // branchUser가 없어도 키오스크 모드에서 메시지 발송을 위해 연결을 시도하도록 수정
  // if (!branchUser) {
  //   return;
  // }

  client = new Client({
    brokerURL: `ws://${window.location.hostname}:8889/ws`,
  });

  client.onConnect = () => {
    console.log('WebSocket 연결 성공');

    // 지점 관리자로 로그인되어 있을 때만 알림을 수신(Subscribe)
    if (branchUser) {
      client.subscribe(
        `/topic/store/${branchUser.storeId}`,
        (message) => {
          showToast(message.body);
        }
      );
    }
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

// 키오스크 직원 호출 핸들러
const handleStaffCall = () => {
  if (client && client.connected) {
    // 키오스크는 보통 특정 storeId에 매핑됩니다 (여기서는 임시로 1번 매장 사용).
    const storeId = 1;
    client.publish({
      destination: `/topic/store/${storeId}`,
      body: '🔔 키오스크에서 직원을 호출했습니다.'
    });
    console.log('직원 호출 메시지 전송 완료');
  } else {
    console.error('웹소켓이 연결되지 않아 직원을 호출할 수 없습니다.');
  }
};

// App.vue는 가장 큰 껍데기이므로 특별한 로직 없이 비워둡니다.
</script>

<template>
  <div class="app-container" :class="{ 'kiosk-wrapper': isKiosk }">
    <main class="main-content" :class="{ 'kiosk-mode': isKiosk }">
      <router-view />
      
      <!-- 키오스크 화면일 때 직원 호출 버튼 렌더링 -->
      <StaffCallButton v-if="isKiosk" @call-staff="handleStaffCall" />
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
.app-container.kiosk-wrapper {
  height: 100vh;
  align-items: center;
  background-color: #f0f0f0;
}

/* router-view 영역 */
.main-content {
  width: 100%;
  max-width: 1400px;
  min-height: 100vh;
  padding: 20px;
  box-sizing: border-box;
}

.main-content.kiosk-mode {
  max-width: 600px;
  height: 100vh;
  min-height: auto;
  padding: 0;
  background-color: white;
  box-shadow: 0 0 20px rgba(0,0,0,0.2);
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
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