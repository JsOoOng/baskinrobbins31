<template>
  <div class="kiosk-menu-container">
    <!-- 1. 상단 헤더 바 -->
    <header class="menu-header">
      <button class="btn-home" @click="goHome">🏠 처음으로</button>
      <div class="kiosk-title">주문하기</div>
      <div class="header-spacer"></div>
    </header>

    <!-- 2. 카테고리 탭 메뉴 -->
    <nav class="category-tabs">
      <button 
        v-for="category in categories" 
        :key="category.categoryId"
        :class="['tab-item', { active: currentCategoryId === category.categoryId }]"
        @click="selectCategory(category.categoryId)"
      >
        {{ category.categoryName }}
      </button>
    </nav>

    <!-- 3. 상품 목록 그리드 영역 -->
    <main class="product-grid-area">
      <!-- 데이터 로딩 중 표시 -->
      <div v-if="isLoadingProducts" class="status-message">상품 불러오는 중...</div>
      
      <!-- 상품 목록이 비어있을 때 -->
      <div v-else-if="filteredProducts.length === 0" class="status-message">
        이 카테고리에 등록된 상품이 없습니다.
      </div>

      <div 
        v-else
        v-for="product in filteredProducts" 
        :key="product.productId" 
        class="product-card"
        @click="openOptionModal(product)"
      >
        <div class="product-img-wrapper">
          <span class="placeholder-icon">🍦</span>
        </div>
        <div class="product-info">
          <div class="product-name">{{ product.productName }}</div>
          <div class="product-price">{{ formatPrice(product.basePrice) }}원</div>
        </div>
      </div>
    </main>

    <!-- 4. 하단 장바구니 요약바 -->
    <footer class="cart-bottom-bar">
      <div class="cart-summary">
        <div>선택한 상품: <strong>{{ cartCount }}</strong>개</div>
        <div>총 결제금액: <strong class="total-price">{{ formatPrice(totalPrice) }}원</strong></div>
      </div>
      <button class="btn-pay" :disabled="cartCount === 0" @click="goPayment">
        결제하기 💳
      </button>
    </footer>

    <!-- 5. 상품 상세 옵션 모달 팝업 -->
    <div v-if="isModalOpen" class="modal-overlay">
      <div class="modal-content">
        <header class="modal-header">
          <h3>{{ selectedProduct.productName }} 옵션 선택</h3>
          <button class="btn-close" @click="closeModal">❌</button>
        </header>

        <main class="modal-body">
          <!-- 아이스크림 맛 선택 제약 조건 화면 -->
          <div v-if="currentMaxFlavors > 0" class="option-section">
            <h4>🍦 아이스크림 맛 선택 (최대 {{ currentMaxFlavors }}가지)</h4>
            <p class="flavor-counter">선택된 맛: {{ selectedFlavors.length }} / {{ currentMaxFlavors }}</p>
            
            <div class="flavor-selection-grid">
              <label 
                v-for="flavor in dbFlavors" 
                :key="flavor.flavorId" 
                :class="['flavor-label', { selected: selectedFlavors.includes(flavor.flavorId) }]"
              >
                <input 
                  type="checkbox" 
                  :value="flavor.flavorId" 
                  v-model="selectedFlavors"
                  :disabled="isFlavorMax && !selectedFlavors.includes(flavor.flavorId)"
                />
                {{ flavor.flavorName }}
              </label>
            </div>
          </div>

          <!-- 스푼 선택 화면 -->
          <div class="option-section">
            <h4>🥄 스푼 선택</h4>
            <p class="spoon-guide">4개까지 무료, 이후 1개당 50원 추가</p>
            <div class="spoon-counter">
              <button class="btn-count" @click="changeSpoon(-1)">-</button>
              <span class="spoon-text">{{ spoonCount }}개</span>
              <button class="btn-count" @click="changeSpoon(1)">+</button>
            </div>
          </div>
        </main>

        <footer class="modal-footer">
          <div class="realtime-price">금액: {{ formatPrice(calculatedItemPrice) }}원</div>
          <button class="btn-add-cart" @click="addCurrentItemToCart">장바구니 담기 🛒</button>
        </footer>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '@/api/axios'

import { useBasketStore } from '@/stores/customer/basket'

const router = useRouter()
const basketStore = useBasketStore()

// === [DB 연동용 반응형 변수] ===
const categories = ref([])
const dbProducts = ref([])
const dbOptions = ref([])
const dbFlavors = ref([])

const currentStoreId = ref(1) // 지점 식별을 위한 기본 storeId 설정 (기본값 1)
const currentCategoryId = ref(null)
const isModalOpen = ref(false)
const selectedProduct = ref(null)
const isLoadingProducts = ref(false)

const selectedFlavors = ref([])
const spoonCount = ref(1)

const cartCount = computed(() => basketStore.totalCount)
const totalPrice = computed(() => basketStore.totalPrice)

// [API 통신 1]: 카테고리 리스트와 지점 맛 리스트 조회
onMounted(async () => {
  // 1. 카테고리 목록 조회
  try {
    const catRes = await axios.get('/api/v1/kiosk/categories')
    categories.value = catRes.data
    
    if (categories.value.length > 0) {
      selectCategory(categories.value[0].categoryId)
    }
  } catch (error) {
    console.error('카테고리 목록 로드 실패:', error)
  }

  // 2. 특정 지점의 맛 리스트 조회
  try {
    const flavorRes = await axios.get(`/api/v1/stores/${currentStoreId.value}/flavors`)
    dbFlavors.value = flavorRes.data
  } catch (error) {
    console.error('맛 리스트 로드 실패:', error)
  }
})

// [API 통신 2]: 카테고리 탭 선택 시 해당 카테고리의 상품 리스트 조회
const selectCategory = async (id) => { 
  currentCategoryId.value = id 
  isLoadingProducts.value = true
  
  try {
    const prodRes = await axios.get(`/api/v1/kiosk/stores/${currentStoreId.value}/categories/${id}/products`)
    dbProducts.value = prodRes.data
  } catch (error) {
    console.error('상품 목록 조회 실패:', error)
  } finally {
    isLoadingProducts.value = false
  }
}

// [API 통신 3]: 상품을 클릭했을 때 해당 상품의 사이즈 및 최대 선택 맛 수(옵션) 조회
const openOptionModal = async (product) => {
  selectedProduct.value = product
  selectedFlavors.value = []
  spoonCount.value = 1
  
  try {
    const detailRes = await axios.get(`/api/v1/kiosk/stores/${currentStoreId.value}/products/${product.productId}/detail`)
    
    dbOptions.value = detailRes.data.options || [] 
    
    // 만약 이 API에서 맛 목록도 같이 묶어서 보내준다면, 여기서 한 번에 처리할 수도 있어!
    // dbFlavors.value = detailRes.data.flavors || [] 

  } catch (error) {
    console.error('상품 상세(옵션) 조회 실패:', error)
  }

  isModalOpen.value = true
}

const filteredProducts = computed(() => {
  return dbProducts.value.filter(p => p.categoryId === currentCategoryId.value)
})

const currentMaxFlavors = computed(() => {
  if (!selectedProduct.value) return 0
  const opt = dbOptions.value.find(o => o.productId === selectedProduct.value.productId)
  return opt ? opt.maxFlavorCount : 0
})

const isFlavorMax = computed(() => {
  return selectedFlavors.value.length >= currentMaxFlavors.value
})

const calculatedItemPrice = computed(() => {
  if (!selectedProduct.value) return 0
  let extraSpoonPrice = 0
  if (spoonCount.value > 4) {
    extraSpoonPrice = (spoonCount.value - 4) * 50
  }
  return selectedProduct.value.basePrice + extraSpoonPrice
})

const formatPrice = (val) => val?.toLocaleString()
const closeModal = () => { isModalOpen.value = false }

const changeSpoon = (amount) => {
  const next = spoonCount.value + amount
  if (next >= 0 && next <= 10) spoonCount.value = next
}

const addCurrentItemToCart = async () => { // 💡 백엔드 통신을 위해 async 추가
  if (currentMaxFlavors.value > 0 && selectedFlavors.value.length !== currentMaxFlavors.value) {
    alert(`맛을 ${currentMaxFlavors.value}가지 모두 선택해주세요!`)
    return
  }

  // 🌟 1. ID만 있던 맛 데이터에 '이름(flavorName)'까지 찾아서 붙여주기!
  const flavorData = selectedFlavors.value.map(fId => {
    // dbFlavors 배열에서 현재 선택된 맛 ID와 일치하는 객체 찾기
    const foundFlavor = dbFlavors.value.find(f => f.flavorId === fId);
    return { 
      flavorId: fId, 
      flavorName: foundFlavor ? foundFlavor.flavorName : '알 수 없는 맛', 
      quantity: 1 
    };
  });

  const requestData = {
    productId: selectedProduct.value.productId,
    productName: selectedProduct.value.productName,
    quantity: 1,
    unitPrice: calculatedItemPrice.value,
    flavors: flavorData, // 💡 이름이 포함된 맛 데이터 배열을 넣음!
    options: spoonCount.value > 4 ? [999] : [] 
  }

  try {
    // 🌟 2. 백엔드 세션 장바구니에 데이터 전송 (아까 잘 만들어둔 컨트롤러 호출!)
    await axios.post('/api/customer/basket', requestData);
    
    // 🌟 3. 백엔드에 잘 담겼으니 Pinia 스토어 갱신 (화면 UI 실시간 반영)
    await basketStore.fetchBasket();
    
    alert(`${selectedProduct.value.productName} 상품이 장바구니에 담겼습니다!`);
    closeModal();
  } catch (error) {
    console.error('장바구니 담기 실패:', error);
    alert('장바구니에 담는데 실패했습니다.');
  }
}

const goHome = () => { router.push('/') }
const goPayment = () => { router.push('/payment') }
</script>

<style scoped>
.kiosk-menu-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f8f9fa;
  font-family: 'Pretendard', sans-serif;
  position: relative;
}

.menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ff7c98; 
  color: white;
  padding: 15px 20px;
  font-size: 1.2rem;
  font-weight: bold;
}

.btn-home {
  background: white;
  color: #ff7c98;
  border: none;
  padding: 8px 15px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
}

.header-spacer {
  width: 80px; /* 좌측 처음으로 버튼과의 레이아웃 대칭 유지 */
}

.category-tabs {
  display: flex;
  gap: 10px;
  padding: 15px;
  background: white;
  overflow-x: auto;
  border-bottom: 2px solid #eee;
}

.tab-item {
  padding: 12px 25px;
  border: 2px solid #ddd;
  border-radius: 30px;
  background: white;
  font-size: 1.1rem;
  cursor: pointer;
  white-space: nowrap;
}

.tab-item.active {
  background-color: #ff7c98;
  color: white;
  border-color: #ff7c98;
  font-weight: bold;
}

.product-grid-area {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  padding: 20px;
  overflow-y: auto;
}

.status-message {
  grid-column: span 2;
  text-align: center;
  padding: 50px;
  color: #888;
  font-size: 1.1rem;
}

.product-card {
  background: white;
  border-radius: 15px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: transform 0.1s;
}

.product-card:active {
  transform: scale(0.95);
}

.placeholder-icon {
  font-size: 3rem;
}

.product-name {
  font-weight: bold;
  margin-top: 10px;
  font-size: 1.1rem;
}

.product-price {
  color: #ff7c98;
  font-weight: bold;
  margin-top: 5px;
}

.cart-bottom-bar {
  background: white;
  padding: 20px;
  border-top: 2px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-summary {
  font-size: 1.1rem;
}

.total-price {
  color: #ff7c98;
  font-size: 1.5rem;
  margin-left: 10px;
}

.btn-pay {
  background-color: #ff7c98;
  color: white;
  border: none;
  padding: 15px 40px;
  font-size: 1.3rem;
  font-weight: bold;
  border-radius: 10px;
  cursor: pointer;
}

.btn-pay:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.modal-overlay {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  width: 90%;
  max-width: 500px;
  border-radius: 20px;
  overflow: hidden;
}

.modal-header {
  background: #333;
  color: white;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-close {
  background: transparent;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.option-section {
  margin-bottom: 25px;
}

.option-section h4 {
  margin: 0 0 10px 0;
  color: #ff7c98;
}

.flavor-counter, .spoon-guide {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 15px;
}

.flavor-selection-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.flavor-label {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 10px;
  cursor: pointer;
}

.flavor-label.selected {
  background-color: #fff0f3;
  border-color: #ff7c98;
  color: #ff7c98;
  font-weight: bold;
}

.spoon-counter {
  display: flex;
  align-items: center;
  gap: 15px;
}

.btn-count {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid #ddd;
  background: white;
  font-size: 1.5rem;
  cursor: pointer;
}

.spoon-text {
  font-size: 1.2rem;
  font-weight: bold;
  width: 30px;
  text-align: center;
}

.modal-footer {
  padding: 20px;
  background: #f8f9fa;
  border-top: 1px solid #ddd;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.realtime-price {
  text-align: right;
  font-size: 1.3rem;
  font-weight: bold;
  color: #333;
}

.btn-add-cart {
  width: 100%;
  padding: 15px;
  background-color: #ff7c98;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1.2rem;
  font-weight: bold;
  cursor: pointer;
}
</style>