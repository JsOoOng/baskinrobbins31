<template>
  <div class="kiosk-container">
    <!-- 상단 헤더 -->
    <header class="header">
      <h1>{{ $t('할인 및 포인트 적립') }}</h1>
    </header>

    <!-- 바디 컨텐츠 -->
    <div class="content-body">
      <!-- 전화번호 미입력 상태: 조회 버튼 섹션 -->
      <div v-if="!phoneNumber" class="auth-section">
        <p>{{ $t('전화번호를 입력하고 포인트와 쿠폰을 확인하세요.') }}</p>
        <button class="open-modal-btn" @click="openKeypad('earn')">
          {{ $t('전화번호 입력 / 조회') }}
        </button>
      </div>

      <!-- 전화번호 입력 완료 후 정보 섹션 -->
      <div v-else>
        <!-- 유저 카드 -->
        <div class="user-card">
          <div class="phone-txt">{{ phoneNumber }}</div>
          <div class="point-txt">{{ $t('보유 포인트') }}: {{ availablePoints.toLocaleString() }} P</div>
          <button class="reset-btn" @click="resetUser">{{ $t('번호 재입력') }}</button>
        </div>

        <!-- 쿠폰 목록 섹션 -->
        <div class="section-box">
          <h3>{{ $t('사용 가능한 쿠폰') }}</h3>
          <div v-if="coupons.length > 0" class="coupon-list">
            <div 
              v-for="coupon in coupons" 
              :key="coupon.userCouponId" 
              class="coupon-card"
              :class="{ selected: selectedCoupon?.userCouponId === coupon.userCouponId }"
              @click="selectCoupon(coupon)"
            >
              <div>
                <div class="c-name">{{ coupon.couponName }}</div>
                <div class="c-disc">
                  {{ coupon.discountValue }}{{ coupon.discountType === 'PERCENT' ? '%' : '원' }} {{ $t('할인') }}
                </div>
              </div>
              <div class="radio-icon">
                {{ selectedCoupon?.userCouponId === coupon.userCouponId ? 'V' : '' }}
              </div>
            </div>
          </div>
          <div v-else class="empty-msg">
            {{ $t('사용 가능한 쿠폰이 없습니다.') }}
          </div>
        </div>

        <!-- 포인트 사용 섹션 -->
        <div class="section-box">
          <h3>{{ $t('포인트 사용') }}</h3>
          <div class="point-input-group" @click="openPointKeypad">
            <input 
              type="text" 
              readonly 
              :value="discountAmount > 0 ? `${discountAmount.toLocaleString()} P` : ''" 
              :placeholder="$t('사용할 포인트를 입력하세요')"
            />
            <button class="point-apply-btn">{{ discountAmount > 0 ? $t('변경') : $t('사용') }}</button>
          </div>
          <div class="tip-msg">{{ $t('100포인트 이상부터 사용 가능합니다.') }}</div>
        </div>

        <!-- 적립 예정 금액 표시 공간 -->
        <div class="section-box earn-preview-box">
          <h3>{{ $t('적립 예정') }}</h3>
          <div class="earn-info-content">
            <span class="earn-label">{{ $t('최종 결제 금액의 5%가 적립됩니다.') }}</span>
            <span class="earn-value">+{{ earnedPoints.toLocaleString() }} P</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 푸터 및 요약 / 네비게이션 섹션 -->
    <div class="footer-section">
      <div class="price-summary-box">
        <div class="summary-top">
          <span class="label">{{ $t('최종 결제금액') }}</span>
          <span class="final-amount">{{ finalPrice.toLocaleString() }}원</span>
        </div>
        <div class="summary-detail">
          <div class="col">
            <span class="sub-label">{{ $t('주문금액') }}</span>
            <span class="sub-val">{{ basketStore.totalPrice.toLocaleString() }}원</span>
          </div>
          <span class="operator">-</span>
          <div class="col">
            <span class="sub-label">{{ $t('할인금액') }}</span>
            <span class="sub-val">{{ totalDiscount.toLocaleString() }}원</span>
          </div>
          <span class="operator">=</span>
          <div class="col">
            <span class="sub-label">{{ $t('적립예정') }}</span>
            <span class="sub-val">{{ earnedPoints.toLocaleString() }}P</span>
          </div>
        </div>
      </div>

      <div class="button-group">
        <button class="prev-btn" @click="goBack">&lt; {{ $t('이전') }}</button>
        <button class="next-btn" @click="proceedPayment">{{ $t('다음(결제하기)') }}</button>
      </div>
    </div>

    <!-- 전화번호 입력 키패드 모달 -->
    <div v-if="showKeypad" class="modal-overlay" @click.self="closeKeypad">
      <div class="keypad-modal-content">
        <h3>{{ $t('전화번호 입력') }}</h3>
        <div class="phone-display">{{ formatPhoneNumber(inputNumber) || 'Ex) 010-0000-0000' }}</div>
        <div class="keypad-grid">
          <button v-for="num in ['1','2','3','4','5','6','7','8','9']" :key="num" @click="pressKey(num)">
            {{ num }}
          </button>
          <button class="action-key" @click="pressPrefix('010')">010</button>
          <button @click="pressKey('0')">0</button>
          <button class="action-key del-btn" @click="deleteKey">DEL</button>
        </div>
        <div class="modal-btns">
          <button class="cancel-btn" @click="closeKeypad">{{ $t('취소') }}</button>
          <button class="confirm-btn" @click="confirmNumber">{{ $t('확인') }}</button>
        </div>
      </div>
    </div>

    <!-- 포인트 사용 키패드 모달 -->
    <div v-if="showPointKeypad" class="modal-overlay" @click.self="closePointKeypad">
      <div class="keypad-modal-content">
        <h3>{{ $t('사용할 포인트 입력') }}</h3>
        <div class="phone-display">{{ inputPointAmount ? Number(inputPointAmount).toLocaleString() + ' P' : '0 P' }}</div>
        
        <!-- 전액 사용 버튼 추가 -->
        <button class="max-point-btn" @click="applyMaxPoints">
          {{ $t('쿠폰 적용 후 남은 금액 전액 사용') }}
        </button>

        <div class="keypad-grid point-keypad">
          <button v-for="num in ['1','2','3','4','5','6','7','8','9','0','00']" :key="num" class="key-btn" @click="pressPointKey(num)">
            {{ num }}
          </button>
          <button class="key-btn del-btn" @click="deletePointKey">DEL</button>
        </div>
        <div class="modal-btns">
          <button class="cancel-btn" @click="closePointKeypad">{{ $t('취소') }}</button>
          <button class="confirm-btn" @click="confirmPointUsage">{{ $t('적용') }}</button>
        </div>
      </div>
    </div>

    <!-- 토스트 메시지 -->
    <div v-if="showToast" class="toast-popup">
      {{ toastText }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useBasketStore } from '@/stores/customer/basket'
import { useI18n } from 'vue-i18n'
import axios from '@/api/axios'

const router = useRouter()
const basketStore = useBasketStore()
const { t } = useI18n({ useScope: 'global' })

const phoneNumber = ref('')
const availablePoints = ref(0)
const coupons = ref([])
const selectedCoupon = ref(null)
const discountAmount = ref(0)
const earnedPoints = ref(0)

const showKeypad = ref(false)
const inputNumber = ref('')

const showPointKeypad = ref(false)
const inputPointAmount = ref('')

const showToast = ref(false)
const toastText = ref('')
let toastTimeout = null

const displayToast = (msg) => {
  if (toastTimeout) clearTimeout(toastTimeout)
  toastText.value = msg
  showToast.value = true
  toastTimeout = setTimeout(() => {
    showToast.value = false
  }, 2500)
}

onMounted(() => {
  basketStore.setPhoneNumber('')
  if (basketStore.cartItems.length === 0) {
    displayToast(t('장바구니가 비어있습니다.'))
    setTimeout(() => router.push('/'), 1500)
  }
})

// 총 할인 금액 (쿠폰 할인 + 포인트 할인)
const totalDiscount = computed(() => {
  let couponDiscount = 0
  if (selectedCoupon.value) {
    if (selectedCoupon.value.discountType === 'PERCENT') {
      couponDiscount = Math.floor(basketStore.totalPrice * (selectedCoupon.value.discountValue / 100))
    } else {
      couponDiscount = selectedCoupon.value.discountValue
    }
  }
  return couponDiscount + discountAmount.value
})

// 최종 결제금액
const finalPrice = computed(() => {
  const result = basketStore.totalPrice - totalDiscount.value
  return result > 0 ? result : 0
})

// 전화번호 포맷팅
const formatPhoneNumber = (number) => {
  if (!number) return ''
  if (number.length <= 3) return number
  if (number.length <= 7) return `${number.slice(0, 3)}-${number.slice(3)}`
  return `${number.slice(0, 3)}-${number.slice(3, 7)}-${number.slice(7, 11)}`
}

// 키패드 제어
const openKeypad = () => {
  inputNumber.value = ''
  showKeypad.value = true
}

const closeKeypad = () => {
  showKeypad.value = false
  inputNumber.value = ''
}

const pressKey = (key) => {
  if (inputNumber.value.length < 11) {
    inputNumber.value += key
  }
}

const pressPrefix = (prefix) => {
  if (inputNumber.value.length + prefix.length <= 11) {
    inputNumber.value += prefix
  }
}

const deleteKey = () => {
  inputNumber.value = inputNumber.value.slice(0, -1)
}

const confirmNumber = async () => {
  if (inputNumber.value.length < 10) {
    displayToast(t('올바른 휴대폰 번호를 입력해주세요.'))
    return
  }

  const formattedInputPhone = formatPhoneNumber(inputNumber.value)
  
  try {
    const res = await axios.get('/api/users/points', {
      params: { phone: formattedInputPhone }
    })

    phoneNumber.value = formattedInputPhone
    basketStore.setPhoneNumber(formattedInputPhone)
    
    availablePoints.value = res.data.pointBalance ?? 0
    coupons.value = res.data.coupons ? res.data.coupons.filter(c => !c.isUsed) : []
    
    earnedPoints.value = Math.floor(finalPrice.value * 0.05)
    
    closeKeypad()
    displayToast(t('회원 정보가 조회되었습니다.'))
  } catch (error) {
    console.error('회원 조회 실패:', error)
    displayToast(t('회원 조회 중 오류가 발생했습니다.'))
  }
}

const resetUser = () => {
  phoneNumber.value = ''
  availablePoints.value = 0
  coupons.value = []
  selectedCoupon.value = null
  discountAmount.value = 0
  basketStore.setPhoneNumber('')
  basketStore.setUsedPoints(0)
}

const selectCoupon = (coupon) => {
  if (selectedCoupon.value?.userCouponId === coupon.userCouponId) {
    selectedCoupon.value = null
    displayToast(t('쿠폰 적용이 해제되었습니다.'))
  } else {
    selectedCoupon.value = coupon
  }

  if (discountAmount.value > 0) {
    let currentCouponDiscount = 0
    if (selectedCoupon.value) {
      if (selectedCoupon.value.discountType === 'PERCENT') {
        currentCouponDiscount = Math.floor(basketStore.totalPrice * (selectedCoupon.value.discountValue / 100))
      } else {
        currentCouponDiscount = selectedCoupon.value.discountValue
      }
    }
    const maxAllowedPrice = basketStore.totalPrice - currentCouponDiscount

    if (discountAmount.value > maxAllowedPrice) {
      discountAmount.value = maxAllowedPrice > 0 ? maxAllowedPrice : 0
      basketStore.setUsedPoints(discountAmount.value)
    }
  }

  earnedPoints.value = Math.floor(finalPrice.value * 0.05)
}

// 포인트 모달 제어
const openPointKeypad = () => {
  if (!phoneNumber.value) {
    displayToast(t('먼저 전화번호를 조회해주세요.'))
    return
  }
  if (availablePoints.value < 100) {
    displayToast(t('100포인트 이상부터 사용 가능합니다.'))
    return
  }
  inputPointAmount.value = ''
  showPointKeypad.value = true
}

const closePointKeypad = () => {
  showPointKeypad.value = false
  inputPointAmount.value = ''
}

// 쿠폰 적용 후 남은 금액 전액 자동 입력 버튼 함수
const applyMaxPoints = () => {
  let currentCouponDiscount = 0
  if (selectedCoupon.value) {
    if (selectedCoupon.value.discountType === 'PERCENT') {
      currentCouponDiscount = Math.floor(basketStore.totalPrice * (selectedCoupon.value.discountValue / 100))
    } else {
      currentCouponDiscount = selectedCoupon.value.discountValue
    }
  }
  const maxAllowedPrice = basketStore.totalPrice - currentCouponDiscount
  const maxLimit = Math.min(availablePoints.value, maxAllowedPrice)

  if (maxLimit < 100) {
    displayToast(t('100포인트 이상부터 사용 가능합니다.'))
    return
  }

  // 10원 단위 절삭 (10단위로 맞춤)
  const adjustedMax = Math.floor(maxLimit / 10) * 10
  inputPointAmount.value = adjustedMax.toString()
}

const pressPointKey = (key) => {
  if (inputPointAmount.value === '' && (key === '0' || key === '00')) return
  
  let nextVal = inputPointAmount.value + key
  let numericVal = parseInt(nextVal, 10)
  
  let currentCouponDiscount = 0
  if (selectedCoupon.value) {
    if (selectedCoupon.value.discountType === 'PERCENT') {
      currentCouponDiscount = Math.floor(basketStore.totalPrice * (selectedCoupon.value.discountValue / 100))
    } else {
      currentCouponDiscount = selectedCoupon.value.discountValue
    }
  }
  const maxAllowedPrice = basketStore.totalPrice - currentCouponDiscount
  const maxLimit = Math.min(availablePoints.value, maxAllowedPrice)

  if (numericVal > maxLimit) {
    inputPointAmount.value = maxLimit.toString()
    return
  }

  if (inputPointAmount.value.length < 8) {
    inputPointAmount.value = nextVal
  }
}

const deletePointKey = () => {
  inputPointAmount.value = inputPointAmount.value.slice(0, -1)
}

const confirmPointUsage = () => {
  let amount = parseInt(inputPointAmount.value || '0')

  if (amount > 0 && amount < 100) {
    displayToast(t('100포인트 이상부터 사용 가능합니다.'))
    return
  }
// 10단위
/*
  if (amount % 10 !== 0) {
    displayToast(t('10 포인트 단위로 입력해주세요.'))
    return
  }
*/

  let currentCouponDiscount = 0
  if (selectedCoupon.value) {
    if (selectedCoupon.value.discountType === 'PERCENT') {
      currentCouponDiscount = Math.floor(basketStore.totalPrice * (selectedCoupon.value.discountValue / 100))
    } else {
      currentCouponDiscount = selectedCoupon.value.discountValue
    }
  }
  const maxAllowedPrice = basketStore.totalPrice - currentCouponDiscount

  if (amount > availablePoints.value) {
    amount = availablePoints.value
  }

  if (amount > maxAllowedPrice) {
    amount = maxAllowedPrice
  }

  discountAmount.value = amount
  basketStore.setUsedPoints(amount)
  earnedPoints.value = Math.floor(finalPrice.value * 0.05)

  closePointKeypad()
}

const goBack = async () => {
  if (confirm(t('현재 결제를 취소하고 장바구니로 돌아가시겠습니까? (장바구니 내역은 유지됩니다)'))) {
    router.push('/menu')
  }
}

const proceedPayment = async () => {
  try {
    // 스토어 함수 대신 직접 상태 값에 할당 (에러 방지)
    basketStore.usedPoints = discountAmount.value;
    basketStore.usedCouponId = selectedCoupon.value ? selectedCoupon.value.userCouponId : null;

    // 만약 setUsedPoints 함수는 있다면 안전하게 호출
    if (typeof basketStore.setUsedPoints === 'function') {
      basketStore.setUsedPoints(discountAmount.value);
    }

    await router.push('/payment');
  } catch (error) {
    console.error('결제 페이지 이동 실패:', error);
    displayToast(t('결제 페이지로 이동할 수 없습니다.'));
  }
}
</script>

<style scoped>
.kiosk-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #fcfcfc;
  font-family: 'Pretendard', sans-serif;
  max-width: 480px;
  margin: 0 auto;
  box-sizing: border-box;
  border: 1px solid #eaeaea;
}

.header {
  height: 60px;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #ddd;
}
.header h1 {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.content-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.auth-section {
  text-align: center;
  margin-top: 100px;
}
.open-modal-btn {
  margin-top: 20px;
  padding: 15px 30px;
  background-color: #ff2d55;
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
}

.user-card {
  background-color: #fff0f3;
  padding: 15px;
  border-radius: 10px;
  margin-bottom: 20px;
  border: 1px solid #ffccd5;
}
.phone-txt {
  font-weight: bold;
  font-size: 16px;
  color: #333;
}
.point-txt {
  margin-top: 5px;
  font-size: 15px;
  color: #ff2d55;
}
.reset-btn {
  margin-top: 10px;
  padding: 5px 10px;
  background-color: #666;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.section-box {
  background-color: #fff;
  padding: 15px;
  border-radius: 10px;
  border: 1px solid #ddd;
  margin-bottom: 20px;
}
.section-box h3 {
  font-size: 16px;
  margin-bottom: 12px;
  color: #333;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.coupon-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  background-color: #fafafa;
}
.coupon-card.selected {
  border-color: #ff2d55;
  background-color: #fff5f7;
}
.c-name {
  font-weight: bold;
  font-size: 14px;
}
.c-disc {
  color: #ff2d55;
  font-size: 13px;
  margin-top: 3px;
}
.radio-icon {
  font-size: 13px;
  font-weight: bold;
  color: #ff2d55;
}
.empty-msg {
  color: #888;
  font-size: 14px;
  text-align: center;
  padding: 10px;
}

.point-input-group {
  display: flex;
  gap: 10px;
  cursor: pointer;
}
.point-input-group input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 15px;
  pointer-events: none;
  background-color: #fff;
}
.point-apply-btn {
  padding: 0 15px;
  background-color: #333;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
}
.tip-msg {
  font-size: 12px;
  color: #666;
  margin-top: 6px;
}

.earn-preview-box {
  background-color: #f4fdf6;
  border: 1px solid #c3e6cb;
}
.earn-info-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.earn-label {
  font-size: 14px;
  color: #555;
}
.earn-value {
  font-size: 16px;
  font-weight: bold;
  color: #27ae60;
}

.footer-section {
  background-color: #fff;
  padding: 20px;
  border-top: 1px solid #eaeaea;
}
.price-summary-box {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 10px;
  margin-bottom: 15px;
}
.summary-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #dee2e6;
}
.summary-top .label {
  font-size: 16px;
  font-weight: bold;
  color: #ff2d55;
}
.summary-top .final-amount {
  font-size: 22px;
  font-weight: bold;
  color: #ff2d55;
}
.summary-detail {
  display: flex;
  justify-content: space-between;
  align-items: center;
  text-align: center;
}
.summary-detail .col {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.summary-detail .sub-label {
  font-size: 12px;
  color: #666;
}
.summary-detail .sub-val {
  font-size: 15px;
  font-weight: bold;
  color: #333;
  margin-top: 4px;
}
.summary-detail .operator {
  font-size: 18px;
  font-weight: bold;
  color: #888;
  padding: 0 5px;
}

.button-group {
  display: flex;
  gap: 10px;
}
.prev-btn {
  flex: 1;
  padding: 15px;
  background-color: #fff;
  border: 1px solid #ccc;
  color: #333;
  font-size: 16px;
  font-weight: bold;
  border-radius: 30px;
  cursor: pointer;
}
.next-btn {
  flex: 2;
  padding: 15px;
  background-color: #ff2d55;
  border: none;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-radius: 30px;
  cursor: pointer;
}

.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.keypad-modal-content {
  background: white;
  padding: 20px;
  border-radius: 16px;
  width: 340px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}
.keypad-modal-content h3 {
  margin-bottom: 15px;
  font-size: 18px;
  color: #333;
}
.phone-display {
  background-color: #f1f3f5;
  padding: 12px;
  font-size: 20px;
  font-weight: bold;
  border-radius: 8px;
  margin-bottom: 12px;
  min-height: 24px;
  color: #333;
  letter-spacing: 1px;
}

.max-point-btn {
  width: 100%;
  padding: 10px;
  margin-bottom: 12px;
  background-color: #fff0f3;
  color: #ff2d55;
  border: 1px solid #ffccd5;
  border-radius: 8px;
  font-weight: bold;
  font-size: 13px;
  cursor: pointer;
}

.keypad-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 15px;
}
.keypad-grid button {
  padding: 15px;
  font-size: 20px;
  font-weight: bold;
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  cursor: pointer;
}
.keypad-grid button.action-key {
  font-size: 16px;
  font-weight: bold;
  background-color: #e9ecef;
}

.point-keypad {
  grid-template-columns: repeat(3, 1fr);
}
.key-btn {
  padding: 14px;
  font-size: 18px;
  font-weight: bold;
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  cursor: pointer;
}
.del-btn {
  background-color: #e9ecef !important;
  font-size: 15px !important;
}

.modal-btns {
  display: flex;
  gap: 10px;
}
.cancel-btn, .confirm-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 8px;
  font-weight: bold;
  font-size: 15px;
  cursor: pointer;
}
.cancel-btn {
  background-color: #dee2e6;
  color: #495057;
}
.confirm-btn {
  background-color: #ff2d55;
  color: white;
}

.toast-popup {
  position: fixed;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.85);
  color: #fff;
  padding: 12px 24px;
  border-radius: 20px;
  font-size: 15px;
  z-index: 1100;
}
</style>