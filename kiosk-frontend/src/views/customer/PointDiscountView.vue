<template>
  <div class="point-discount-container">
    <!-- 상단 스텝 바 -->
    <div class="step-header">
      <div class="step active">
        <span class="badge">STEP01</span>
        <span class="title">포인트/할인</span>
      </div>
      <div class="step inactive">
        <span class="badge">STEP02</span>
        <span class="title">쿠폰/결제</span>
      </div>
    </div>

    <!-- 메인 컨텐츠 영역 -->
    <div class="pd-main-content">
      <div class="left-section">
        <h2 class="section-title">해피포인트 회원이신가요?</h2>
        <div class="button-grid">
          <button class="point-btn" @click="openKeypad('earn')">
            <img src="@/assets/images/logo.png" alt="Happy Point" class="hp-logo" />
            <span>적립하기</span>
          </button>
          <button class="point-btn" @click="openKeypad('use')">
            <img src="@/assets/images/logo.png" alt="Happy Point" class="hp-logo" />
            <span>사용하기</span>
          </button>
        </div>
        <div v-if="earnedPoints > 0" class="point-status">
          <p>🎁 결제 완료 시 <strong>{{ earnedPoints.toLocaleString() }}</strong> 포인트가 적립될 예정입니다.</p>
          <p class="phone-number">(번호: {{ phoneNumber }})</p>
        </div>
      </div>

      <div class="right-section">
        <div class="info-block">
          <h3>* 해피포인트 적립하기</h3>
          <p>해피포인트 적립을 선택하시면 결제완료 후 자동 적립됩니다.</p>
        </div>
        <div class="info-block">
          <h3>* 해피포인트 사용하기</h3>
          <p>100포인트 이상 보유시 사용 가능합니다.</p>
        </div>
      </div>
    </div>

    <!-- 하단 결제 금액 서머리 -->
    <div class="summary-footer">
      <div class="final-price-row">
        <span class="label">최종 결제금액</span>
        <span class="value pink">₩ {{ (basketStore.totalPrice - discountAmount).toLocaleString() }}</span>
      </div>
      <div class="detail-price-row">
        <div class="detail-item">
          <span>총 주문 금액</span>
          <span>₩ {{ basketStore.totalPrice.toLocaleString() }}</span>
        </div>
        <div class="detail-divider">-</div>
        <div class="detail-item">
          <span>총 할인 금액</span>
          <span>₩ {{ discountAmount.toLocaleString() }}</span>
        </div>
      </div>
    </div>

    <!-- 하단 네비게이션 버튼 -->
    <div class="nav-buttons">
      <button class="btn-prev" @click="goBack">
        <span class="icon">&lt;</span> 이전
      </button>
      <button class="btn-next" @click="goNext">
        다음(결제하기)
      </button>
    </div>

    <!-- 번호 입력 키패드 모달 -->
    <div v-if="showKeypad" class="modal-overlay">
      <div class="keypad-modal">
        <h3>전화번호 입력</h3>
        <p class="subtitle">해피포인트 적립을 위해<br/>휴대폰 번호를 입력해주세요.</p>
        
        <div class="input-display">{{ formatPhoneNumber(inputNumber) || '휴대폰 번호 입력' }}</div>
        
        <div class="keypad-grid">
          <button v-for="num in ['1','2','3','4','5','6','7','8','9','010','0']" 
                  :key="num" 
                  @click="pressKey(num)" 
                  class="key-btn">
            {{ num }}
          </button>
          <button @click="deleteKey" class="key-btn del-btn">←</button>
        </div>

        <div class="modal-actions">
          <button class="btn-cancel" @click="closeKeypad">취소</button>
          <button class="btn-confirm" @click="confirmNumber">확인</button>
        </div>
      </div>
    </div>

    <!-- 포인트 사용 금액 입력 모달 -->
    <div v-if="showPointKeypad" class="modal-overlay">
      <div class="keypad-modal">
        <h3>포인트 사용</h3>
        <p class="subtitle">보유 포인트: {{ availablePoints.toLocaleString() }} P<br/>사용할 금액을 10 단위로 입력해주세요.</p>
        
        <div class="input-display">{{ inputPointAmount ? parseInt(inputPointAmount).toLocaleString() + ' P' : '0 P' }}</div>
        
        <div class="keypad-grid point-keypad">
          <button v-for="num in ['1','2','3','4','5','6','7','8','9','00','0']" 
                  :key="num" 
                  @click="pressPointKey(num)" 
                  class="key-btn">
            {{ num }}
          </button>
          <button @click="deletePointKey" class="key-btn del-btn">지움</button>
        </div>

        <div class="modal-actions">
          <button class="btn-cancel" @click="closePointKeypad">취소</button>
          <button class="btn-confirm" @click="confirmPointUsage">적용</button>
        </div>
      </div>
    </div>

    <!-- 토스트 메시지 -->
    <div v-if="showToast" class="toast-message">
      {{ toastText }}
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useBasketStore } from '@/stores/customer/basket'
import axios from '@/api/axios'

const router = useRouter()
const route = useRoute()
const basketStore = useBasketStore()

const discountAmount = ref(0)
const earnedPoints = ref(0)

const showKeypad = ref(false)
const inputNumber = ref('')
const phoneNumber = ref('')

const showPointKeypad = ref(false)
const availablePoints = ref(0)
const inputPointAmount = ref('')

const showToast = ref(false)
const toastText = ref('')
let toastTimeout = null

const displayToast = (msg) => {
  if (toastTimeout) {
    clearTimeout(toastTimeout)
  }
  toastText.value = msg
  showToast.value = true
  toastTimeout = setTimeout(() => {
    showToast.value = false
  }, 2500)
}

onMounted(() => {
  // 컴포넌트 마운트 시 스토어의 전화번호 초기화
  basketStore.setPhoneNumber('')
  if (basketStore.cartItems.length === 0) {
    displayToast('장바구니가 비어있습니다.')
    setTimeout(() => router.push('/'), 1500)
  }
})

const goBack = async () => {
  if (confirm('현재 결제를 취소하고 장바구니로 돌아가시겠습니까? (장바구니 내역은 유지됩니다)')) {
    router.push('/menu')
  }
}

const goNext = () => {
  // 결제 화면으로 넘어감 (orderId는 아직 없음)
  router.push(`/payment`)
}

// 키패드 관련 로직
const openKeypad = (mode) => {
  basketStore.setPointMode(mode);
  inputNumber.value = '';
  showKeypad.value = true;
}

const closeKeypad = () => {
  showKeypad.value = false;
  inputNumber.value = '';
}

const pressKey = (key) => {
  if (inputNumber.value.length < 11) {
    inputNumber.value += key;
  }
}

const deleteKey = () => {
  inputNumber.value = inputNumber.value.slice(0, -1);
}

const formatPhoneNumber = (number) => {
  if (!number) return '';
  if (number.length <= 3) return number;
  if (number.length <= 7) return number.slice(0, 3) + '-' + number.slice(3);
  return number.slice(0, 3) + '-' + number.slice(3, 7) + '-' + number.slice(7, 11);
}

const confirmNumber = async () => {
  if (inputNumber.value.length < 10) {
    displayToast('올바른 휴대폰 번호를 입력해주세요.');
    return;
  }
  
  phoneNumber.value = formatPhoneNumber(inputNumber.value);
  basketStore.setPhoneNumber(phoneNumber.value);
  
  if (basketStore.pointMode === 'earn') {
    // 상품 금액의 5% 적립
    earnedPoints.value = Math.floor(basketStore.totalPrice * 0.05);
    displayToast(`결제 완료 시 ${earnedPoints.value.toLocaleString()} 포인트가 적립될 예정입니다.`);
    closeKeypad();
  } else if (basketStore.pointMode === 'use') {
    try {
      const res = await axios.get(`/api/users/${phoneNumber.value}/points`);
      const points = res.data;
      if (points < 100) {
        displayToast('100포인트 이상부터 사용 가능합니다.');
      } else {
        availablePoints.value = points;
        inputPointAmount.value = '';
        showKeypad.value = false;
        showPointKeypad.value = true;
      }
    } catch (error) {
      console.error('포인트 조회 실패:', error);
      displayToast('포인트 조회에 실패했습니다.');
    }
  }
}

// 포인트 모달 로직
const pressPointKey = (key) => {
  if (inputPointAmount.value === '' && (key === '0' || key === '00')) return;
  if (inputPointAmount.value.length < 8) {
    inputPointAmount.value += key;
  }
}

const deletePointKey = () => {
  inputPointAmount.value = inputPointAmount.value.slice(0, -1);
}

const closePointKeypad = () => {
  showPointKeypad.value = false;
  inputPointAmount.value = '';
}

const confirmPointUsage = () => {
  let amount = parseInt(inputPointAmount.value || '0');
  
  if (amount === 0) {
    displayToast('사용할 포인트를 입력해주세요.');
    return;
  }
  
  if (amount % 10 !== 0) {
    displayToast('10 포인트 단위로 입력해주세요.');
    return;
  }
  
  if (amount > availablePoints.value) {
    displayToast('보유 포인트를 초과할 수 없습니다.');
    return;
  }
  
  if (amount > basketStore.totalPrice) {
    amount = basketStore.totalPrice;
  }
  
  discountAmount.value = amount;
  basketStore.setUsedPoints(amount);
  
  earnedPoints.value = Math.floor((basketStore.totalPrice - amount) * 0.05);
  
  displayToast(`${amount.toLocaleString()}포인트 사용 적용 (차액 ${earnedPoints.value.toLocaleString()}P 적립 예정)`);
  closePointKeypad();
}

</script>

<style scoped>
.point-discount-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #fff;
  font-family: 'Pretendard', sans-serif;
}

.step-header {
  display: flex;
  height: 80px;
  background-color: #f8f9fa;
}

.step {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  font-weight: bold;
}

.step.active {
  background-color: #fff;
  color: #e91e63;
  border-bottom: 4px solid #e91e63;
}

.step.inactive {
  background-color: #f1f3f5;
  color: #adb5bd;
  border-bottom: 4px solid #dee2e6;
}

.badge {
  font-size: 0.8rem;
  background-color: currentColor;
  color: #fff;
  padding: 2px 8px;
  border-radius: 12px;
  margin-bottom: 5px;
}

.step.inactive .badge {
  background-color: #adb5bd;
}

.pd-main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 40px;
  gap: 20px;
}

.left-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.section-title {
  font-size: 1.8rem;
  margin-bottom: 30px;
  color: #333;
}

.button-grid {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.point-btn {
  width: 150px;
  height: 150px;
  border: 1px solid #dee2e6;
  border-radius: 15px;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.point-btn:hover {
  border-color: #e91e63;
  box-shadow: 0 4px 12px rgba(233, 30, 99, 0.1);
}

.hp-logo {
  width: 60px;
  height: auto;
  margin-bottom: 15px;
}

.point-btn span {
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
}

.point-status {
  background: #fff0f3;
  padding: 20px;
  border-radius: 10px;
  border: 1px solid #ffc8d5;
}

.point-status p {
  margin: 0;
  color: #e91e63;
  font-size: 1.1rem;
}

.point-status .phone-number {
  font-size: 0.9rem;
  color: #666;
  margin-top: 5px;
}

.right-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.info-block {
  margin-bottom: 30px;
}

.info-block h3 {
  color: #e91e63;
  font-size: 1.3rem;
  margin-bottom: 10px;
}

.info-block p {
  color: #888;
  font-size: 1.1rem;
  line-height: 1.5;
  margin: 0;
}

.summary-footer {
  background-color: #f8f9fa;
  padding: 30px 40px;
}

.final-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.final-price-row .label {
  font-size: 1.5rem;
  font-weight: bold;
  color: #e91e63;
}

.final-price-row .value {
  font-size: 2rem;
  font-weight: bold;
  color: #e91e63;
}

.detail-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10%;
}

.detail-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.detail-item span:first-child {
  font-size: 1.2rem;
  font-weight: bold;
  color: #333;
}

.detail-item span:last-child {
  font-size: 1.5rem;
  font-weight: bold;
  color: #000;
}

.detail-divider {
  font-size: 2rem;
  font-weight: bold;
  color: #333;
}

.nav-buttons {
  display: flex;
  justify-content: space-between;
  padding: 20px 40px;
  background: #fff;
  border-top: 1px solid #eee;
}

.btn-prev {
  background: #fff;
  border: 2px solid #ddd;
  color: #e91e63;
  font-size: 1.5rem;
  font-weight: bold;
  padding: 15px 40px;
  border-radius: 40px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  width: 300px;
  justify-content: center;
}

.btn-prev .icon {
  font-size: 2rem;
  font-weight: 900;
}

.btn-next {
  background: #e91e63;
  border: none;
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
  padding: 15px 40px;
  border-radius: 40px;
  cursor: pointer;
  width: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Keypad Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
}

.keypad-modal {
  background: #fff;
  border-radius: 20px;
  padding: 30px;
  width: 400px;
  text-align: center;
}

.keypad-modal h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 1.5rem;
}

.keypad-modal .subtitle {
  color: #666;
  margin-bottom: 20px;
}

.input-display {
  font-size: 2rem;
  font-weight: bold;
  color: #e91e63;
  background: #f8f9fa;
  padding: 15px;
  border-radius: 10px;
  margin-bottom: 20px;
  letter-spacing: 2px;
  min-height: 40px;
}

.keypad-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}

.key-btn {
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 1.8rem;
  font-weight: bold;
  padding: 15px 0;
  cursor: pointer;
  transition: 0.1s;
}

.key-btn:active {
  background: #f1f3f5;
}

.del-btn {
  background: #e9ecef;
  color: #e91e63;
}

.modal-actions {
  display: flex;
  gap: 10px;
}

.btn-cancel {
  flex: 1;
  padding: 15px;
  border: none;
  background: #adb5bd;
  color: white;
  font-size: 1.2rem;
  border-radius: 10px;
  font-weight: bold;
  cursor: pointer;
}

.btn-confirm {
  flex: 2;
  padding: 15px;
  border: none;
  background: #e91e63;
  color: white;
  font-size: 1.2rem;
  border-radius: 10px;
  font-weight: bold;
  cursor: pointer;
}

/* Toast Message */
.toast-message {
  position: fixed;
  bottom: 120px;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(255, 124, 152, 0.85);
  color: white;
  padding: 16px 30px;
  border-radius: 30px;
  font-size: 1.1rem;
  font-weight: bold;
  box-shadow: 0 4px 15px rgba(255, 124, 152, 0.4);
  z-index: 4000;
  backdrop-filter: blur(4px);
  animation: fadeInOut 2.5s ease-in-out forwards;
}

@keyframes fadeInOut {
  0% { opacity: 0; transform: translate(-50%, 20px); }
  15% { opacity: 1; transform: translate(-50%, 0); }
  85% { opacity: 1; transform: translate(-50%, 0); }
  100% { opacity: 0; transform: translate(-50%, -20px); }
}
</style>
