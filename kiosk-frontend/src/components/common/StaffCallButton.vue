<template>
  <div class="staff-call-wrapper">
    <!-- 플로팅 버튼 -->
    <button class="call-btn" @click="handleCall">직원 호출 🔔</button>

    <!-- 오버레이 (키오스크 화면 비활성화) -->
    <div v-if="isCalling" class="call-overlay">
      <div class="call-content">
        <h2>🔔 직원을 호출했습니다.</h2>
        <p>잠시만 기다려주세요...</p>
        <button class="close-btn" @click="closeCall">돌아가기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const emit = defineEmits(['call-staff']);
const isCalling = ref(false);

const handleCall = () => {
  isCalling.value = true;
  emit('call-staff'); // App.vue로 직원 호출 이벤트 전달
};

const closeCall = () => {
  isCalling.value = false;
};
</script>

<style scoped>
.staff-call-wrapper {
  position: absolute;
  z-index: 1000;
}

.call-btn {
  position: absolute; /* .kiosk-mode(relative) 기준으로 위치 */
  bottom: 30px;
  left: 30px;
  background-color: #ff7c98;
  color: white;
  border: none;
  border-radius: 50px;
  padding: 15px 25px;
  font-size: 1.2rem;
  font-weight: bold;
  box-shadow: 0 4px 10px rgba(0,0,0,0.3);
  cursor: pointer;
  transition: 0.2s;
  z-index: 9998;
}

.call-btn:hover {
  background-color: #e66885;
}

.call-btn:active {
  transform: scale(0.95);
}

.call-overlay {
  position: absolute; /* .kiosk-mode 화면 전체를 덮도록 absolute */
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.call-content {
  background: white;
  padding: 40px;
  border-radius: 20px;
  text-align: center;
  width: 80%;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3);
}

.call-content h2 {
  color: #ff6b6b;
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 1.8rem;
}

.call-content p {
  font-size: 1.3rem;
  color: #333;
  margin-bottom: 30px;
}

.close-btn {
  background: #666;
  color: white;
  border: none;
  padding: 15px 40px;
  font-size: 1.2rem;
  border-radius: 10px;
  cursor: pointer;
  font-weight: bold;
  transition: 0.2s;
}

.close-btn:hover {
  background: #555;
}

.close-btn:active {
  transform: scale(0.95);
}
</style>
