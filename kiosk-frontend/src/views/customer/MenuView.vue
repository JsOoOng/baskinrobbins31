<template>
  <div class="kiosk-menu-container">
    <!-- 1. 상단 헤더 -->
    <header class="menu-header">
      <img src="@/assets/images/logo.png" alt="Baskin Robbins" class="logo" />
      <button class="btn-close-app" @click="goHome">
        <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
      </button>
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

      <div v-else class="product-grid">
        <div 
          v-for="product in filteredProducts" 
          :key="product.productId" 
          class="product-card"
          @click="openOptionModal(product)"
        >
          <div class="product-img-wrapper">
            <img :src="product.imageUrl || `/images/products/${product.productName}.png`" @error="handleProductImgError" alt="product" class="placeholder-img" />
            <div class="emoji-placeholder fallback-emoji" style="display:none;">🍦</div>
          </div>
          <div class="product-name">{{ product.productName }}</div>
          <div class="product-price">₩{{ formatPrice(product.basePrice) }}</div>
        </div>
      </div>
    </main>

    <!-- 4. 플로팅 장바구니 바 -->
    <footer class="floating-cart-bar">
      <button class="cart-icon-btn" @click="openCartModal">
        <svg viewBox="0 0 24 24" width="28" height="28" stroke="#333" stroke-width="2" fill="none"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path><line x1="3" y1="6" x2="21" y2="6"></line><path d="M16 10a4 4 0 0 1-8 0"></path></svg>
        <span class="cart-badge" v-if="cartCount > 0">{{ cartCount }}</span>
      </button>
      <button class="checkout-btn" @click="goPayment" :disabled="cartCount === 0">
        <span class="checkout-price" v-if="cartCount > 0">₩{{ formatPrice(totalPrice) }}</span>
        <span class="checkout-text">결제하기</span>
        <svg viewBox="0 0 24 24" width="24" height="24" stroke="white" stroke-width="2" fill="none"><polyline points="9 18 15 12 9 6"></polyline></svg>
      </button>
    </footer>

    <!-- 5. 2-Step 상품 옵션 모달 팝업 -->
    <div v-if="isModalOpen" class="modal-overlay">
      <div class="modal-content option-modal">
        <!-- 모달 탭 -->
        <div class="modal-tabs">
          <button :class="['modal-tab-btn', {active: currentModalTab === 'INFO'}]" @click="currentModalTab = 'INFO'">상품정보</button>
          <button :class="['modal-tab-btn', {active: currentModalTab === 'FLAVOR'}]" @click="currentModalTab = 'FLAVOR'">플레이버</button>
        </div>

        <!-- STEP 1: 상품 정보 탭 -->
        <div v-show="currentModalTab === 'INFO'" class="tab-body info-tab">
          <div class="product-detail-header">
            <img :src="selectedProduct?.imageUrl || `/images/products/${selectedProduct?.productName}.png`" @error="handleProductImgError" class="detail-img" />
            <div class="emoji-placeholder-big fallback-emoji" style="display:none;">🍦</div>
            <div class="detail-texts">
              <h3>{{ selectedProduct?.productName }} <span class="detail-sub">(콘/컵)</span></h3>
            </div>
            <div class="detail-price-big">₩{{ formatPrice(calculatedItemPrice) }}</div>
          </div>
          <p class="flavor-desc">원하는 맛의 아이스크림을 즐기세요!</p>
          <p class="warning-text">★★ 본 제품은 포장이 불가합니다 ★★</p>

          <div class="container-options">
            <div class="container-card" :class="{selected: selectedContainer === 'CUP'}" @click="selectedContainer = 'CUP'">
              <div class="container-img-wrap"><span class="container-emoji">🥤</span></div>
              <span class="container-name">컵<br/><span class="sub-text">(포장불가)</span></span>
              <div class="qty-control-simple">
                <span>-</span><span>{{ selectedContainer === 'CUP' ? '1' : '0' }}</span><span>+</span>
              </div>
            </div>
            <div class="container-card" :class="{selected: selectedContainer === 'CONE'}" @click="selectedContainer = 'CONE'">
              <div class="container-img-wrap"><span class="container-emoji">🍦</span></div>
              <span class="container-name">콘<br/><span class="sub-text">(포장불가)</span></span>
              <div class="qty-control-simple">
                <span>-</span><span>{{ selectedContainer === 'CONE' ? '1' : '0' }}</span><span>+</span>
              </div>
            </div>
            <div class="container-card" :class="{selected: selectedContainer === 'WAFFLE'}" @click="selectedContainer = 'WAFFLE'">
              <div class="container-img-wrap"><span class="container-emoji">🧇</span></div>
              <span class="container-name">와플콘<br/><span class="sub-text">(포장불가)</span></span>
              <div class="qty-control-simple">
                <span>-</span><span>{{ selectedContainer === 'WAFFLE' ? '1' : '0' }}</span><span>+</span>
              </div>
            </div>
          </div>

          <div class="modal-bottom-actions">
            <button class="btn-prev-outline" @click="closeModal">
              <svg viewBox="0 0 24 24" width="20" height="20" stroke="#e91e63" stroke-width="2" fill="none"><polyline points="15 18 9 12 15 6"></polyline></svg> 이전
            </button>
            <button class="btn-next-pink" @click="currentModalTab = 'FLAVOR'">
              플레이버(맛) 선택
            </button>
          </div>
        </div>

        <!-- STEP 2: 플레이버 탭 -->
        <div v-show="currentModalTab === 'FLAVOR'" class="tab-body flavor-tab">
          <div class="flavor-grid">
            <div 
              v-for="flavor in paginatedFlavors" 
              :key="flavor.flavorId" 
              :class="['flavor-card', { selected: selectedFlavors.includes(flavor.flavorId) }, { disabled: isFlavorMax && !selectedFlavors.includes(flavor.flavorId) }]"
              @click="toggleFlavorSlot(flavor.flavorId)"
            >
              <img v-if="flavor.imageUrl" :src="flavor.imageUrl" :alt="flavor.flavorName" class="flavor-image"/>
              <div v-else class="flavor-image-placeholder">🍦</div>
              <div class="flavor-name">{{ flavor.flavorName }}</div>
            </div>
          </div>

          <!-- Pagination -->
          <div class="pagination-dots">
            <span class="dot active"></span>
            <span class="dot"></span>
          </div>

          <!-- Selected Slots -->
          <div class="selected-slots-area">
             <div class="slot-container" v-for="i in currentMaxFlavors" :key="i">
                <div class="slot-circle" :class="{filled: selectedFlavors[i-1]}"></div>
                <div class="slot-badge">{{ i }}</div>
             </div>
          </div>

          <div class="modal-bottom-actions">
            <button class="btn-prev-outline" @click="currentModalTab = 'INFO'">
              <svg viewBox="0 0 24 24" width="20" height="20" stroke="#e91e63" stroke-width="2" fill="none"><polyline points="15 18 9 12 15 6"></polyline></svg> 이전
            </button>
            <button class="btn-next-pink" @click="addCurrentItemToCart">
              장바구니 담기
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 6. 장바구니 확인 모달 (축소) -->
    <div v-if="isCartModalOpen" class="modal-overlay cart-overlay">
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
                </div>
                <div class="cart-item-price">₩{{ formatPrice(item.unitPrice * item.quantity) }}</div>
              </div>
              <div class="cart-item-actions">
                <button class="btn-delete" @click="deleteCartItem(index)">삭제</button>
              </div>
            </div>
          </div>
        </main>
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

const currentStoreId = ref(1) 
const currentCategoryId = ref(null)
const isModalOpen = ref(false)
const isCartModalOpen = ref(false)
const selectedProduct = ref(null)
const isLoadingProducts = ref(false)

const selectedFlavors = ref([])
const spoonCount = ref(1)

const currentPage = ref(1)
const itemsPerPage = 12

// 새로운 UI 전용 상태
const currentModalTab = ref('INFO') // 'INFO' | 'FLAVOR'
const selectedContainer = ref('CUP')

const cartCount = computed(() => basketStore.totalCount)
const totalPrice = computed(() => basketStore.totalPrice)


const handleImageError = (e) => {
  e.target.style.display = 'none';
}

const handleProductImgError = (e) => {
  e.target.style.display = 'none';
  if (e.target.nextElementSibling) {
    e.target.nextElementSibling.style.display = 'flex';
  }
}

// API 통신
onMounted(async () => {
  try {
    const catRes = await axios.get('/api/v1/kiosk/categories')
    categories.value = catRes.data
    if (categories.value.length > 0) {
      selectCategory(categories.value[0].categoryId)
    }
  } catch (error) {
    console.error('카테고리 로드 실패', error)
  }

  try {
    const flavorRes = await axios.get(`/api/v1/stores/${currentStoreId.value}/flavors`)
    dbFlavors.value = flavorRes.data
  } catch (error) {
    console.error('맛 로드 실패', error)
  }
})

const selectCategory = async (id) => { 
  currentCategoryId.value = id 
  isLoadingProducts.value = true
  try {
    const prodRes = await axios.get(`/api/v1/kiosk/stores/${currentStoreId.value}/categories/${id}/products`)
    dbProducts.value = prodRes.data
  } catch (error) {
    console.error('상품 로드 실패', error)
  } finally {
    isLoadingProducts.value = false
  }
}

const openOptionModal = async (product) => {
  selectedProduct.value = product
  selectedFlavors.value = []
  currentModalTab.value = 'INFO'
  spoonCount.value = 1
  currentPage.value = 1
  selectedContainer.value = 'CUP'
  
  try {
    const detailRes = await axios.get(`/api/v1/kiosk/stores/${currentStoreId.value}/products/${product.productId}/detail`)
    dbOptions.value = detailRes.data.options || [] 
  } catch (error) {
    console.error('옵션 로드 실패', error)
  }
  isModalOpen.value = true
}

const filteredProducts = computed(() => {
  return dbProducts.value.filter(p => p.categoryId === currentCategoryId.value)
})

const currentMaxFlavors = computed(() => {
  if (!selectedProduct.value) return 0
  const opt = dbOptions.value.find(o => o.productId === selectedProduct.value.productId)
  // 강제로 1 이상으로 설정하여 UI가 표시되게 함 (옵션 데이터가 없을 경우 방어)
  return opt ? opt.maxFlavorCount : 1
})

const isFlavorMax = computed(() => {
  return selectedFlavors.value.length >= currentMaxFlavors.value
})

const paginatedFlavors = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return dbFlavors.value.slice(start, end)
})

const toggleFlavorSlot = (id) => {
  const idx = selectedFlavors.value.indexOf(id);
  if (idx > -1) {
    selectedFlavors.value.splice(idx, 1);
  } else if (!isFlavorMax.value) {
    selectedFlavors.value.push(id);
  }
}

const calculatedItemPrice = computed(() => {
  if (!selectedProduct.value) return 0
  return selectedProduct.value.basePrice
})

const formatPrice = (val) => val?.toLocaleString()
const closeModal = () => { isModalOpen.value = false }

const addCurrentItemToCart = async () => { 
  if (currentMaxFlavors.value > 0 && selectedFlavors.value.length < currentMaxFlavors.value) {
    alert(`아이스크림 맛을 ${currentMaxFlavors.value}가지 모두 선택해주세요.`);
    return;
  }

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

  const validOptions = dbOptions.value.length > 0 ? [dbOptions.value[0].optionId] : [];

  const requestData = {
    productId: selectedProduct.value.productId,
    productName: selectedProduct.value.productName + ` (${selectedContainer.value})`,
    quantity: 1,
    unitPrice: calculatedItemPrice.value,
    flavors: flavorData, 
    options: validOptions,
    extraSpoons: false
  }

  try {
    await axios.post('/api/customer/basket', requestData);
    await basketStore.fetchBasket();
    closeModal();
  } catch (error) {
    alert('장바구니 담기에 실패했습니다.');
  }
}

const openCartModal = async () => {
  await basketStore.fetchBasket();
  isCartModalOpen.value = true;
}

const closeCartModal = () => { isCartModalOpen.value = false; }

const deleteCartItem = (index) => {
  if (confirm("삭제하시겠습니까?")) {
    basketStore.removeFromCart(index);
    if (basketStore.cartItems.length === 0) closeCartModal();
  }
}

const goHome = async () => { 
  if (basketStore.cartItems.length > 0) {
    if (confirm('장바구니가 비워집니다. 진행하시겠습니까?')) {
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
      kioskId: 1, storeId: 1
    });
    const orderId = res.data;
    router.push(`/payment?orderId=${orderId}`);
  } catch (error) {
    alert('주문 생성 에러가 발생했습니다.');
  }
}
</script>

<style scoped>
.kiosk-menu-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #fff; /* White background */
  font-family: 'Pretendard', sans-serif;
  position: relative;
  overflow: hidden;
}

/* Header */
.menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 30px;
  background-color: #fff;
}
.logo {
  height: 40px;
}
.btn-close-app {
  background: #fce4ec;
  border: none;
  border-radius: 50%;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  cursor: pointer;
}

/* Categories */
.category-tabs {
  display: flex;
  padding: 10px 30px;
  overflow-x: auto;
  gap: 20px;
  white-space: nowrap;
}
.category-tabs::-webkit-scrollbar { display: none; }
.tab-item {
  background: transparent;
  border: none;
  font-size: 1rem;
  font-weight: bold;
  color: #e91e63;
  cursor: pointer;
  padding: 10px 20px;
  border-radius: 25px;
}
.tab-item.active {
  background-color: #e91e63;
  color: white;
}

/* Products Grid */
.product-grid-area {
  flex: 1;
  padding: 20px 30px;
  overflow-y: auto;
  padding-bottom: 150px; /* space for bottom bar */
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.product-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}
.product-img-wrapper {
  width: 100px;
  height: 100px;
  margin-bottom: 10px;
}
.placeholder-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.product-name {
  font-size: 0.95rem;
  color: #333;
  text-align: center;
  margin-bottom: 5px;
  word-break: keep-all;
  font-weight: bold;
}
.product-price {
  font-size: 1rem;
  font-weight: bold;
  color: #e91e63;
}

/* Floating Cart Bar */
.floating-cart-bar {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 15px;
  z-index: 100;
}
.cart-icon-btn {
  background: #fff;
  border: 1px solid #eee;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  border-radius: 50px;
  width: 90px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
}
.cart-badge {
  position: absolute;
  top: 5px;
  right: 15px;
  background: #e91e63;
  color: white;
  border-radius: 50%;
  width: 22px;
  height: 22px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}
.checkout-btn {
  background: #e91e63;
  color: white;
  border: none;
  border-radius: 50px;
  height: 60px;
  padding: 0 40px;
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 1.2rem;
  font-weight: bold;
  box-shadow: 0 4px 15px rgba(233, 30, 99, 0.4);
  cursor: pointer;
}
.checkout-btn:disabled {
  background: #ccc;
  box-shadow: none;
  cursor: not-allowed;
}
.checkout-price {
  margin-right: 10px;
}

/* 2-Step Modal */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: flex-end; /* Bottom sheet */
  justify-content: center;
  z-index: 1000;
}
.modal-content.option-modal {
  background: white;
  width: 100%;
  height: 85vh;
  border-top-left-radius: 30px;
  border-top-right-radius: 30px;
  display: flex;
  flex-direction: column;
}
.modal-tabs {
  display: flex;
  background: #f8f9fa;
  border-top-left-radius: 30px;
  border-top-right-radius: 30px;
}
.modal-tab-btn {
  flex: 1;
  padding: 20px;
  font-size: 1.1rem;
  font-weight: bold;
  border: none;
  background: transparent;
  color: #ccc;
  cursor: pointer;
}
.modal-tab-btn.active {
  color: #e91e63;
  background: white;
  border-top-left-radius: 30px;
  border-top-right-radius: 30px;
}
.tab-body {
  flex: 1;
  padding: 30px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

/* Step 1: Info */
.product-detail-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30px;
  margin-bottom: 20px;
}
.detail-img { width: 120px; }
.detail-texts h3 { font-size: 1.3rem; margin: 0;}
.detail-sub { font-size: 0.9rem; color: #666; font-weight: normal; }
.detail-price-big { font-size: 1.5rem; font-weight: bold; color: #e91e63; margin-left: 20px;}
.flavor-desc { text-align: center; color: #999; margin-bottom: 20px; font-size: 0.9rem;}
.warning-text { text-align: center; color: red; font-weight: bold; font-size: 0.9rem; margin-bottom: 30px;}

.container-options {
  display: flex;
  justify-content: center;
  gap: 15px;
  flex: 1;
}
.container-card {
  border: 1px solid #ddd;
  border-radius: 15px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  min-width: 120px;
}
.container-card.selected { border-color: #e91e63; border-width: 2px; }
.container-img { width: 60px; margin-bottom: 10px; }
.container-img-wrap { height: 70px; display: flex; align-items:flex-end; justify-content:center; width: 100%; }
.container-name { text-align: center; color: #e91e63; font-size: 0.9rem; margin-bottom: 15px; font-weight: bold;}
.container-emoji { font-size: 3.5rem; line-height: 1; margin-bottom: 5px; }
.emoji-placeholder { font-size: 4rem; display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; }
.emoji-placeholder-big { font-size: 6rem; width: 120px; display: flex; align-items: center; justify-content: center; }
.sub-text { font-size: 0.75rem; color:#e91e63; font-weight:normal;}
.qty-control-simple { display: flex; gap: 10px; color: #333; font-size: 1rem; font-weight: bold; }

/* Step 2: Flavor */
.flavor-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}
.flavor-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  cursor: pointer;
  padding: 10px;
}
.flavor-card.selected .flavor-image { border: 3px solid #e91e63; border-radius: 50%; }
.flavor-card.disabled { opacity: 0.4; pointer-events: none; }
.flavor-image { width: 80px; height: 80px; object-fit: contain; margin-bottom: 10px; }
.flavor-name { font-size: 0.85rem; color: #333;}
.pagination-dots { text-align: center; margin-bottom: 20px; }
.dot { display: inline-block; width: 8px; height: 8px; background: #ddd; border-radius: 50%; margin: 0 4px; }
.dot.active { background: #e91e63; }

.selected-slots-area {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: auto;
  padding-bottom: 20px;
}
.slot-container { position: relative; }
.slot-circle {
  width: 60px; height: 60px;
  border-radius: 50%;
  border: 1px dashed #e91e63;
  background: #fff0f5;
}
.slot-circle.filled { background: #e91e63; border-style: solid; }
.slot-badge {
  position: absolute; top: -5px; right: -5px;
  background: #e91e63; color: white; border-radius: 50%; width: 20px; height: 20px;
  display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold;
}

/* Modal Bottom Actions */
.modal-bottom-actions {
  display: flex;
  justify-content: space-between;
  gap: 15px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
.btn-prev-outline {
  flex: 1; padding: 20px;
  border: 1px solid #eee; background: white; color: #e91e63; border-radius: 30px;
  font-size: 1.1rem; font-weight: bold; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 10px;
}
.btn-next-pink {
  flex: 2; padding: 20px;
  border: none; background: #e91e63; color: white; border-radius: 30px;
  font-size: 1.1rem; font-weight: bold; cursor: pointer;
}

/* Cart Overlay */
.cart-overlay {
  align-items: center;
}
.modal-content.cart-modal { height: auto; max-height: 70vh; max-width: 600px; padding: 20px; border-radius: 20px; margin: 20px;}
.modal-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 15px; border-bottom: 1px solid #eee; margin-bottom: 15px;}
.modal-header h3 { margin:0; color: #e91e63; }
.btn-close { background: transparent; border: none; font-size: 1.5rem; cursor: pointer; }
.cart-item-card { border-bottom: 1px solid #eee; padding: 15px 0; display: flex; justify-content: space-between; align-items: center;}
.cart-item-name { font-weight: bold; font-size: 1.1rem;}
.cart-item-price { color: #e91e63; font-weight: bold; margin-top: 5px; }
.cart-item-options { font-size: 0.9rem; color: #666; margin-top: 5px;}
.btn-delete { background: #f8f9fa; border: 1px solid #ddd; padding: 5px 10px; border-radius: 5px; cursor: pointer; color: #333;}
</style>