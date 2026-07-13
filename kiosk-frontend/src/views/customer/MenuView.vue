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
      <div class="cart-buttons">
        <button class="btn-view-cart" :disabled="cartCount === 0" @click="openCartModal">
          장바구니 확인 🛒
        </button>
        <button class="btn-pay" :disabled="cartCount === 0" @click="goPayment">
          결제하기 💳
        </button>
      </div>
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
            <div class="flavor-counter-header">
              <p class="flavor-counter">선택된 맛: <strong>{{ selectedFlavors.length }} / {{ currentMaxFlavors }}</strong></p>
            </div>
            
            <div class="selected-flavor-badges" v-if="selectedFlavors.length > 0">
              <span v-for="(fId, idx) in selectedFlavors" :key="idx" class="flavor-badge" @click="removeFlavorSlot(idx)">
                {{ getFlavorName(fId) }} <span class="badge-remove">✖</span>
              </span>
            </div>
            
            <div class="flavor-selection-grid-with-images">
              <div 
                v-for="flavor in paginatedFlavors" 
                :key="flavor.flavorId" 
                :class="['flavor-card', { selected: selectedFlavors.includes(flavor.flavorId) }, { disabled: isFlavorMax }]"
                @click="addFlavorSlot(flavor.flavorId)"
              >
                <img v-if="flavor.imageUrl" :src="flavor.imageUrl" :alt="flavor.flavorName" class="flavor-image"/>
                <div v-else class="flavor-image-placeholder">🍦</div>
                <div class="flavor-name">{{ flavor.flavorName }}</div>
              </div>
            </div>

            <div class="pagination-controls">
              <button class="btn-page" :disabled="currentPage === 1" @click="currentPage--">이전</button>
              <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
              <button class="btn-page" :disabled="currentPage === totalPages" @click="currentPage++">다음</button>
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

    <!-- 6. 장바구니 확인 모달 -->
    <div v-if="isCartModalOpen" class="modal-overlay">
      <div class="modal-content cart-modal">
        <header class="modal-header">
          <h3>장바구니 확인</h3>
          <button class="btn-close" @click="closeCartModal">❌</button>
        </header>

        <main class="modal-body">
          <div v-if="basketStore.cartItems.length === 0" class="empty-cart-message">
            장바구니가 비어 있습니다.
          </div>
          <div v-else class="cart-item-list">
            <div v-for="(item, index) in basketStore.cartItems" :key="index" class="cart-item-card">
              <div class="cart-item-info">
                <div class="cart-item-name">{{ item.productName }}</div>
                <div class="cart-item-options">
                  <span v-for="(flavor, fIdx) in item.flavors" :key="fIdx">
                    {{ flavor.flavorName }}{{ flavor.quantity > 1 ? '(' + flavor.quantity + ')' : '' }}{{ fIdx < item.flavors.length - 1 ? ', ' : '' }}
                  </span>
                  <span v-if="item.extraSpoons">
                    (스푼 추가)
                  </span>
                </div>
                <div class="cart-item-price">{{ formatPrice(item.unitPrice * item.quantity) }}원</div>
              </div>
              <div class="cart-item-actions">
                <div class="qty-control">
                  <button class="btn-qty" @click="decreaseCartQty(index, item.quantity)">-</button>
                  <span class="qty-text">{{ item.quantity }}</span>
                  <button class="btn-qty" @click="increaseCartQty(index, item.quantity)">+</button>
                </div>
                <button class="btn-delete" @click="deleteCartItem(index)">삭제 🗑️</button>
              </div>
            </div>
          </div>
        </main>

        <footer class="modal-footer cart-modal-footer">
          <div class="cart-modal-summary">
            총 결제금액: <strong class="total-price">{{ formatPrice(totalPrice) }}원</strong>
          </div>
          <div class="cart-modal-buttons">
            <button class="btn-add-more" @click="closeCartModal">메뉴 더 담기 ➕</button>
            <button class="btn-pay-now" :disabled="cartCount === 0" @click="goPayment">결제하기 💳</button>
          </div>
        </footer>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
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
const isCartModalOpen = ref(false)
const selectedProduct = ref(null)
const isLoadingProducts = ref(false)

let intervalId = null

const selectedFlavors = ref([])
const spoonCount = ref(1)

const currentPage = ref(1)
const itemsPerPage = 10

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

  // 🌟 실시간 갱신을 위한 주기적(5초마다) 폴링 설정
  intervalId = setInterval(async () => {
    // 1. 조용히 맛 리스트 업데이트
    try {
      const flavorRes = await axios.get(`/api/v1/stores/${currentStoreId.value}/flavors`)
      dbFlavors.value = flavorRes.data
    } catch (error) {
      console.error('맛 리스트 실시간 동기화 실패:', error)
    }

    // 2. 조용히 현재 선택된 카테고리 상품 리스트 업데이트
    if (currentCategoryId.value) {
      try {
        const prodRes = await axios.get(`/api/v1/kiosk/stores/${currentStoreId.value}/categories/${currentCategoryId.value}/products`)
        dbProducts.value = prodRes.data
      } catch (error) {
        console.error('상품 목록 실시간 동기화 실패:', error)
      }
    }
  }, 5000)
})

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId)
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
  currentPage.value = 1
  
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

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(dbFlavors.value.length / itemsPerPage))
})

const paginatedFlavors = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return dbFlavors.value.slice(start, end)
})

const getFlavorName = (fId) => {
  const f = dbFlavors.value.find(x => x.flavorId === fId);
  return f ? f.flavorName : '알 수 없는 맛';
}

const addFlavorSlot = (id) => {
  if (!isFlavorMax.value) {
    selectedFlavors.value.push(id)
  }
}

const removeFlavorSlot = (idx) => {
  selectedFlavors.value.splice(idx, 1)
}

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
  if (currentMaxFlavors.value > 0 && selectedFlavors.value.length < currentMaxFlavors.value) {
    alert(`아이스크림 맛을 ${currentMaxFlavors.value}가지 선택해주세요. (현재 ${selectedFlavors.value.length}개)`);
    return;
  }

  // 🌟 1. ID 목록을 그룹화해서 수량(quantity)까지 구하기!
  const flavorCounts = selectedFlavors.value.reduce((acc, fId) => {
    acc[fId] = (acc[fId] || 0) + 1;
    return acc;
  }, {});

  const flavorData = Object.keys(flavorCounts).map(fIdStr => {
    const fId = parseInt(fIdStr);
    const foundFlavor = dbFlavors.value.find(f => f.flavorId === fId);
    return { 
      flavorId: fId, 
      flavorName: foundFlavor ? foundFlavor.flavorName : '알 수 없는 맛', 
      quantity: flavorCounts[fId]
    };
  });

  const validOptions = [];
  if (dbOptions.value && dbOptions.value.length > 0) {
    validOptions.push(dbOptions.value[0].optionId);
  }

  const requestData = {
    productId: selectedProduct.value.productId,
    productName: selectedProduct.value.productName,
    quantity: 1,
    unitPrice: calculatedItemPrice.value,
    flavors: flavorData, // 💡 이름이 포함된 맛 데이터 배열을 넣음!
    options: validOptions, // 실제 DB에 존재하는 첫 번째 옵션을 넘김 (용기/사이즈 등)
    extraSpoons: spoonCount.value > 4 // 999 대신 별도 필드로 스푼 추가 여부 전달
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

const openCartModal = async () => {
  await basketStore.fetchBasket();
  isCartModalOpen.value = true;
}

const closeCartModal = () => {
  isCartModalOpen.value = false;
}

const increaseCartQty = (index, currentQty) => {
  basketStore.updateQuantity(index, currentQty + 1);
}

const decreaseCartQty = (index, currentQty) => {
  if (currentQty > 1) {
    basketStore.updateQuantity(index, currentQty - 1);
  }
}

const deleteCartItem = (index) => {
  if (confirm("이 상품을 장바구니에서 삭제하시겠습니까?")) {
    basketStore.removeFromCart(index);
    if (basketStore.cartItems.length === 0) {
      closeCartModal();
    }
  }
}

const goHome = async () => { 
  if (basketStore.cartItems.length > 0) {
    if (confirm('처음으로 돌아가시면 장바구니가 모두 지워집니다. 진행하시겠습니까?')) {
      await basketStore.clearCart();
      router.push('/');
    }
  } else {
    router.push('/');
  }
}
const goPayment = async () => { 
  try {
    const res = await axios.post('/api/orders', {
      orderType: basketStore.orderType || 'TOGO',
      dryIceCount: basketStore.dryIceCount || 0,
      dryIceMins: basketStore.dryIceMins || 0,
      kioskId: 1,
      storeId: 1
    });
    const orderId = res.data;
    // 주문 번호를 가지고 결제 화면으로 넘어갑니다.
    router.push(`/payment?orderId=${orderId}`);
  } catch (error) {
    console.error('주문 생성 에러:', error);
    alert('결제를 진행할 수 없습니다. (장바구니가 비어있는지 확인해주세요)');
  }
}
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
  width: 95%;
  max-width: 800px;
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

.flavor-counter-header {
  margin-bottom: 10px;
}

.flavor-counter {
  font-size: 1.1rem;
  color: #333;
}

.selected-flavor-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 10px;
  border: 1px dashed #ccc;
  min-height: 44px;
}

.flavor-badge {
  background: #ff7c98;
  color: white;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.95rem;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s;
}

.flavor-badge:hover {
  background: #e66a85;
}

.badge-remove {
  font-size: 0.8rem;
  opacity: 0.8;
}

.flavor-selection-grid-with-images {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 15px;
  margin-bottom: 15px;
}

.flavor-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 15px 5px;
  border: 2px solid #ddd;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
  min-height: 140px;
  box-sizing: border-box;
  width: 100%;
}

.flavor-card.selected {
  background-color: #fff0f3;
  border-color: #ff7c98;
  color: #ff7c98;
  font-weight: bold;
}

.flavor-card.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.flavor-image {
  width: 60px;
  height: 60px;
  object-fit: contain;
  margin-bottom: 10px;
}

.flavor-image-placeholder {
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.flavor-name {
  font-size: 0.85rem;
  text-align: center;
  word-break: keep-all;
  overflow-wrap: break-word;
  line-height: 1.3;
  width: 100%;
}

.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 15px;
}

.btn-page {
  background: #ff7c98;
  color: white;
  border: none;
  padding: 5px 15px;
  border-radius: 5px;
  cursor: pointer;
  font-weight: bold;
}

.btn-page:disabled {
  background: #ccc;
  cursor: not-allowed;
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

.cart-buttons {
  display: flex;
  gap: 15px;
}

.btn-view-cart {
  background-color: #fff;
  color: #ff7c98;
  border: 2px solid #ff7c98;
  padding: 15px 30px;
  font-size: 1.3rem;
  font-weight: bold;
  border-radius: 10px;
  cursor: pointer;
}

.btn-view-cart:disabled {
  border-color: #ccc;
  color: #ccc;
  cursor: not-allowed;
}

.cart-modal {
  max-width: 600px;
}

.empty-cart-message {
  text-align: center;
  padding: 40px;
  color: #888;
  font-size: 1.2rem;
}

.cart-item-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.cart-item-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 10px;
  background: #fafafa;
}

.cart-item-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex: 1;
}

.cart-item-name {
  font-size: 1.5rem;
  color: #000;
  font-weight: bold;
}

.cart-item-options {
  font-size: 0.9rem;
  color: #888;
}

.cart-item-price {
  font-size: 1.1rem;
  color: #ff7c98;
  font-weight: bold;
  margin-top: 5px;
}

.cart-item-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.qty-control {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 5px;
}

.btn-qty {
  width: 30px;
  height: 30px;
  background: #eee;
  border: none;
  border-radius: 5px;
  font-size: 1.2rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qty-text {
  font-size: 1.1rem;
  font-weight: bold;
  min-width: 20px;
  text-align: center;
}

.btn-delete {
  background: transparent;
  color: #ff4d4f;
  border: 1px solid #ff4d4f;
  padding: 5px 10px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.9rem;
}

.cart-modal-footer {
  background: white;
}

.cart-modal-summary {
  font-size: 1.2rem;
  text-align: right;
  margin-bottom: 15px;
}

.cart-modal-buttons {
  display: flex;
  gap: 10px;
}

.btn-add-more {
  flex: 1;
  padding: 15px;
  background: #fff;
  color: #333;
  border: 2px solid #ddd;
  border-radius: 10px;
  font-size: 1.2rem;
  font-weight: bold;
  cursor: pointer;
}

.btn-pay-now {
  flex: 1;
  padding: 15px;
  background: #ff7c98;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1.2rem;
  font-weight: bold;
  cursor: pointer;
}
</style>