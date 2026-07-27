<!--
  [화면 흐름 안내] TimeoutModal
  역할: 공통 프론트엔드 화면에서 재사용되는 UI 컴포넌트다.
  진입: 상위 라우트 또는 부모 컴포넌트 -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> props·Pinia·상위 화면 상태 -> 응답/상태 반영
  다음 이동: /
-->
<template>
  <div v-if="timeoutStore.showModal" class="timeout-modal-overlay">
    <div class="timeout-modal-content">
      <h2>⚠️ {{ $t('장시간 입력이 없습니다') }}</h2>
      <p class="desc-text">{{ $t('계속하시겠습니까?') }}</p>
      <div class="countdown-circle">
        <span class="countdown-number">{{ timeoutStore.countdown }}</span>
      </div>
      <p class="sub-text">{{ $t('응답이 없으면 처음 화면으로 돌아갑니다.') }}</p>
      <button class="btn-extend" @click="extendTime">{{ $t('연장하기') }}</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useBasketStore } from '@/stores/customer/basket';
import { useTimeoutStore } from '@/stores/customer/timeout';
import { useI18n } from 'vue-i18n';

const route = useRoute();
const router = useRouter();
const basketStore = useBasketStore();
const timeoutStore = useTimeoutStore();
const { locale } = useI18n({ useScope: 'global' });

const IDLE_TIME = 60; // 60 seconds
const COUNTDOWN_TIME = 10; // 10 seconds

let tickInterval = null;
let isModalPhase = false;

// 키오스크 주문 진행 화면인지 확인
const isActiveRoute = () => {
  const path = route.path;
  // 맨 처음 화면, 지점 관리자, 본사 관리자 화면은 제외
  if (path === '/' || path.startsWith('/branch') || path.startsWith('/head')) {
    return false;
  }
  return true;
};

const resetIdleTimer = () => {
  clearInterval(tickInterval);
  timeoutStore.setShowModal(false);
  isModalPhase = false;

  if (isActiveRoute()) {
    timeoutStore.setTimeLeft(IDLE_TIME);
    
    tickInterval = setInterval(() => {
      if (!isModalPhase) {
        timeoutStore.setTimeLeft(timeoutStore.timeLeft - 1);
        if (timeoutStore.timeLeft <= 0) {
          startCountdown();
        }
      } else {
        timeoutStore.setCountdown(timeoutStore.countdown - 1);
        if (timeoutStore.countdown <= 0) {
          clearInterval(tickInterval);
          handleTimeout();
        }
      }
    }, 1000);
  }
};

const startCountdown = () => {
  isModalPhase = true;
  timeoutStore.setShowModal(true);
  timeoutStore.setCountdown(COUNTDOWN_TIME);
};

const handleTimeout = async () => {
  timeoutStore.setShowModal(false);
  await basketStore.clearCart();
  locale.value = 'ko'; // 세션 만료 시 언어를 한국어로 초기화
  router.push('/');
};

const extendTime = () => {
  resetIdleTimer();
};

onMounted(() => {
  // 사용자의 모든 상호작용 감지
  window.addEventListener('click', resetIdleTimer);
  window.addEventListener('mousemove', resetIdleTimer);
  window.addEventListener('touchstart', resetIdleTimer);
  window.addEventListener('keydown', resetIdleTimer);
  
  // 최초 실행 시 타이머 설정
  resetIdleTimer();
});

onUnmounted(() => {
  window.removeEventListener('click', resetIdleTimer);
  window.removeEventListener('mousemove', resetIdleTimer);
  window.removeEventListener('touchstart', resetIdleTimer);
  window.removeEventListener('keydown', resetIdleTimer);
  
  clearInterval(tickInterval);
});

// 라우트가 변경될 때마다 타이머를 다시 설정 (화면 이동 시에도 초기화)
watch(route, () => {
  resetIdleTimer();
});
</script>

<style scoped>
.timeout-modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999; /* 화면 최상단 */
}

.timeout-modal-content {
  background: white;
  padding: 40px;
  border-radius: 20px;
  text-align: center;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3);
}

.timeout-modal-content h2 {
  color: #ff6b6b;
  margin-top: 0;
  margin-bottom: 10px;
}

.desc-text {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 20px;
}

.countdown-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: #ffecf0;
  border: 4px solid #ff7c98;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px auto;
}

.countdown-number {
  font-size: 3rem;
  font-weight: bold;
  color: #ff7c98;
}

.sub-text {
  color: #888;
  font-size: 0.95rem;
  margin-bottom: 30px;
}

.btn-extend {
  background: #ff7c98;
  color: white;
  border: none;
  padding: 15px 40px;
  font-size: 1.2rem;
  border-radius: 10px;
  cursor: pointer;
  font-weight: bold;
  transition: 0.2s;
}

.btn-extend:hover {
  background: #e66885;
}
</style>
