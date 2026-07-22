<template>
  <div class="kiosk-menu-container">
    <!-- 1. 상단 헤더 바 -->
    <header class="menu-header">
      <div class="header-left">
        <img src="@/assets/images/logo.png" alt="Baskin Robbins" class="logo" />
      </div>
      <div class="kiosk-title">{{ $t('주문하기') }}</div>
      <div class="header-right" style="display: flex; flex-direction: column; align-items: flex-end; gap: 5px;">
        <div style="display: flex; gap: 5px;">
          <button class="btn-home" style="background-color: #ff9800; border-color: #f57c00; color: white;" @click="handleCallStaff">🔔 {{ $t('직원 호출') }}</button>
          <button class="btn-home" @click="goHome">🏠 {{ $t('처음으로') }}</button>
        </div>
        <span class="timer-text" v-if="timeoutStore && timeoutStore.timeLeft !== undefined">{{ $t('남은 시간') }}: {{ timeoutStore.timeLeft }}{{ $t('초') }}</span>
      </div>
    </header>

    <!-- 2. 카테고리 탭 메뉴 -->
    <nav class="category-tabs">
      <button 
        v-for="category in categories" 
        :key="category.categoryId"
        :class="['tab-item', { active: currentCategoryId === category.categoryId }]"
        @click="selectCategory(category.categoryId)"
      >
        {{ $t(category.categoryName) }}
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

      <div v-else class="product-grid-container" style="display: flex; flex-direction: column; height: 100%;">
        <div class="product-grid">
          <div 
            v-for="product in paginatedProducts" 
            :key="product.productId" 
            class="product-card"
            @click="openOptionModal(product)"
          >
            <div class="product-img-wrapper">
              <img :src="product.imageUrl || `/images/products/${product.productName}.png`" @error="handleProductImgError" alt="product" class="placeholder-img" />
              <div class="emoji-placeholder fallback-emoji" style="display:none;">🍦</div>
            </div>
            <div class="product-info">
              <div class="product-name">{{ $t(product.productName) }}</div>
              <div class="product-price">₩{{ formatPrice(product.finalPrice) }}</div>
            </div>
          </div>
        </div>

        <!-- 상품 페이징 컨트롤 -->
        <div class="pagination-controls-pink" v-if="productTotalPages > 1" style="margin-top: auto; padding-bottom: 20px;">
          <button class="btn-arrow-pink" :disabled="currentProductPage === 1" @click="currentProductPage--">
            &lt;
          </button>
          <div class="pagination-dots">
            <span 
              v-for="page in productTotalPages" 
              :key="page" 
              :class="['dot', { active: currentProductPage === page }]"
              @click="currentProductPage = page"
            ></span>
          </div>
          <button class="btn-arrow-pink" :disabled="currentProductPage === productTotalPages" @click="currentProductPage++">
            &gt;
          </button>
        </div>
      </div>
    </main>

    <!-- 5. 상품 상세 옵션 모달 팝업 -->
    <div v-if="isModalOpen" class="modal-overlay">
      <div class="modal-content">
        <header class="modal-header">
          <h3>{{ $t(selectedProduct.productName) }} 옵션 선택</h3>
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
                {{ $t(getFlavorName(fId)) }} <span class="badge-remove">✖</span>
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
                <div class="flavor-name">{{ $t(flavor.flavorName) }}</div>
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
          <button class="btn-add-cart" @click="addCurrentItemToCart">
            {{ editingCartIndex !== null ? '수정 완료 ✏️' : '장바구니 담기 🛒' }}
          </button>
        </footer>
      </div>
    </div>

    <!-- 4. 플로팅 장바구니 바 -->
    <footer class="floating-cart-bar">
      <button class="cart-icon-btn" @click="openCartModal">
        <svg viewBox="0 0 24 24" width="28" height="28" stroke="#333" stroke-width="2" fill="none"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path><line x1="3" y1="6" x2="21" y2="6"></line><path d="M16 10a4 4 0 0 1-8 0"></path></svg>
        <span class="cart-badge" v-if="cartCount > 0">{{ cartCount }}</span>
      </button>
      <button class="checkout-btn" @click="goPayment" :disabled="cartCount === 0">
        <span class="checkout-price" v-if="cartCount > 0">₩{{ formatPrice(totalPrice) }}</span>
        <span class="checkout-text">{{ $t('결제하기') }}</span>
        <svg viewBox="0 0 24 24" width="24" height="24" stroke="white" stroke-width="2" fill="none"><polyline points="9 18 15 12 9 6"></polyline></svg>
      </button>
    </footer>

    <!-- 5. 2-Step 상품 옵션 모달 팝업 -->
    <div v-if="isModalOpen" class="modal-overlay">
      <div class="modal-content option-modal">
        <button class="btn-close-circle" @click="closeModal">✖</button>
        <!-- 모달 탭 -->
        <div class="modal-tabs">
          <button :class="['modal-tab-btn', {active: currentModalTab === 'INFO'}]" @click="currentModalTab = 'INFO'">{{ $t('상품정보') }}</button>
          <button :class="['modal-tab-btn', {active: currentModalTab === 'FLAVOR'}]" @click="currentModalTab = 'FLAVOR'">{{ $t('플레이버') }}</button>
        </div>

        <!-- STEP 1: 상품 정보 탭 -->
        <div v-show="currentModalTab === 'INFO'" class="tab-body info-tab">
          <div class="product-detail-header">
            <img :src="selectedProduct?.imageUrl || `/images/products/${selectedProduct?.productName}.png`" @error="handleProductImgError" class="detail-img" />
            <div class="emoji-placeholder-big fallback-emoji" style="display:none;">🍦</div>
            <div class="detail-texts">
              <h3>{{ $t(selectedProduct?.productName) }} <span class="detail-sub" v-if="isContainerSelectable">{{ $t('(콘/컵)') }}</span></h3>
            </div>
            <div class="detail-price-big">₩{{ formatPrice(calculatedItemPrice) }}</div>
          </div>
          <p class="flavor-desc" v-if="currentMaxFlavors > 0">{{ $t('원하는 맛의 아이스크림을 즐기세요!') }}</p>
          <p class="warning-text" v-if="isContainerSelectable">{{ $t('★★ 본 제품은 포장이 불가합니다 ★★') }}</p>

          <div class="container-options" v-if="isContainerSelectable">
            <div class="container-card" :class="{selected: selectedContainer === 'CUP'}" @click="selectedContainer = 'CUP'">
              <div class="container-img-wrap"><span class="container-emoji">🥤</span></div>
              <span class="container-name">{{ $t('컵') }}<br/><span class="sub-text">{{ $t('(포장불가)') }}</span></span>
              <div class="qty-control-simple">
                <span>-</span><span>{{ selectedContainer === 'CUP' ? '1' : '0' }}</span><span>+</span>
              </div>
            </div>
            <div class="container-card" :class="{selected: selectedContainer === 'CONE'}" @click="selectedContainer = 'CONE'">
              <div class="container-img-wrap"><span class="container-emoji">🍦</span></div>
              <span class="container-name">{{ $t('콘') }}<br/><span class="sub-text">{{ $t('(포장불가)') }}</span></span>
              <div class="qty-control-simple">
                <span>-</span><span>{{ selectedContainer === 'CONE' ? '1' : '0' }}</span><span>+</span>
              </div>
            </div>
            <div class="container-card" :class="{selected: selectedContainer === 'WAFFLE'}" @click="selectedContainer = 'WAFFLE'">
              <div class="container-img-wrap"><span class="container-emoji">🧇</span></div>
              <span class="container-name">{{ $t('와플콘') }}<br/><span class="sub-text">{{ $t('(포장불가)') }}</span></span>
              <div class="qty-control-simple">
                <span>-</span><span>{{ selectedContainer === 'WAFFLE' ? '1' : '0' }}</span><span>+</span>
              </div>
            </div>
          </div>

          <div class="modal-bottom-actions">
            <button class="btn-prev-outline" @click="closeModal">
              <svg viewBox="0 0 24 24" width="20" height="20" stroke="#e91e63" stroke-width="2" fill="none"><polyline points="15 18 9 12 15 6"></polyline></svg> {{ $t('이전') }}
            </button>
            <button class="btn-next-pink" @click="currentModalTab = 'FLAVOR'">
              {{ $t('플레이버(맛) 선택') }}
            </button>
          </div>
        </div>

        <!-- STEP 2: 플레이버 탭 -->
        <div v-show="currentModalTab === 'FLAVOR'" class="tab-body flavor-tab">
          <div class="flavor-grid">
            <div 
              v-for="flavor in paginatedFlavors" 
              :key="flavor.flavorId" 
              :class="['flavor-card', { selected: selectedFlavors.includes(flavor.flavorId) }, { disabled: isFlavorMax }]"
              @click="addFlavorSlot(flavor.flavorId)"
            >
              <img v-if="flavor.imageUrl" :src="flavor.imageUrl" :alt="flavor.flavorName" class="flavor-image"/>
              <div v-else class="flavor-image-placeholder">🍦</div>
              <div class="flavor-name">{{ $t(flavor.flavorName) }}</div>
            </div>
          </div>

          <!-- Pagination -->
          <div class="pagination-controls-pink">
            <button class="btn-arrow-pink" :disabled="currentPage === 1" @click="currentPage--">
              &lt;
            </button>
            <div class="pagination-dots">
              <span 
                v-for="page in totalPages" 
                :key="page" 
                :class="['dot', { active: currentPage === page }]"
                @click="currentPage = page"
              ></span>
            </div>
            <button class="btn-arrow-pink" :disabled="currentPage === totalPages" @click="currentPage++">
              &gt;
            </button>
          </div>

          <!-- Selected Slots -->
          <div class="selected-slots-area">
             <div class="slot-container" v-for="i in currentMaxFlavors" :key="i" @click="removeFlavorSlot(i-1)" style="cursor: pointer;">
                <div class="slot-circle" :class="{filled: selectedFlavors[i-1]}">
                   <img v-if="selectedFlavors[i-1] && getFlavorImage(selectedFlavors[i-1])" 
                        :src="getFlavorImage(selectedFlavors[i-1])" 
                        class="slot-flavor-image" />
                </div>
                <div class="slot-badge">{{ i }}</div>
             </div>
          </div>

          <div class="modal-bottom-actions">
            <button class="btn-prev-outline" @click="currentModalTab = 'INFO'">
              <svg viewBox="0 0 24 24" width="20" height="20" stroke="#e91e63" stroke-width="2" fill="none"><polyline points="15 18 9 12 15 6"></polyline></svg> {{ $t('이전') }}
            </button>
            <button class="btn-next-pink" @click="addCurrentItemToCart">
              {{ $t('장바구니 담기') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 6. 장바구니 확인 모달 (축소) -->
    <div v-if="isCartModalOpen" class="modal-overlay cart-overlay">
      <div class="modal-content cart-modal">
        <header class="modal-header">
          <h3>{{ $t('장바구니 확인') }}</h3>
          <button class="btn-close" @click="closeCartModal">❌</button>
        </header>

        <main class="modal-body">
          <div v-if="basketStore.cartItems.length === 0" class="empty-cart-message">
            {{ $t('장바구니가 비어 있습니다.') }}
          </div>
          <div v-else class="cart-item-list">
            <div v-for="(item, index) in basketStore.cartItems" :key="index" class="cart-item-card">
              <div class="cart-item-info">
                <div class="cart-item-name">{{ formatCartItemName(item.productName) }}</div>
                <div class="cart-item-options">
                  <span v-for="(flavor, fIdx) in item.flavors" :key="fIdx">
                    {{ $t(flavor.flavorName) }}{{ flavor.quantity > 1 ? '(' + flavor.quantity + ')' : '' }}{{ fIdx < item.flavors.length - 1 ? ', ' : '' }}
                  </span>
                  <span v-if="item.extraSpoons">
                    {{ $t('(스푼 추가)') }}
                  </span>
                </div>
                <div class="cart-item-price">{{ formatPrice(item.unitPrice * item.quantity) }}원</div>
              </div>
                <div class="cart-item-actions">
                  <button class="btn-edit-option" @click="openEditOptionModal(index, item)">{{ $t('옵션 변경') }} ⚙️</button>
                  <div class="qty-control">
                    <button class="btn-qty" @click="decreaseCartQty(index, item.quantity)">-</button>
                    <span class="qty-text">{{ item.quantity }}</span>
                    <button class="btn-qty" @click="increaseCartQty(index, item.quantity)">+</button>
                  </div>
                  <button class="btn-delete" @click="deleteCartItem(index)">{{ $t('삭제') }} 🗑️</button>
                </div>
            </div>
          </div>
        </main>

        <footer class="modal-footer cart-modal-footer">
          <div class="cart-modal-summary">
            {{ $t('총 결제금액') }}: <strong class="total-price">{{ formatPrice(totalPrice) }}{{ $t('원') }}</strong>
          </div>
          <div class="cart-modal-buttons">
            <button class="btn-add-more" @click="closeCartModal">{{ $t('메뉴 더 담기') }} ➕</button>
            <button class="btn-pay-now" :disabled="cartCount === 0" @click="goPayment">{{ $t('결제하기') }} 💳</button>
          </div>
        </footer>
      </div>
    </div>

    <!-- 7. 토스트 메시지 -->
    <div v-if="showToast" class="toast-message">
      {{ toastText }}
    </div>

    <!-- 8. 포장 시간 선택 모달 -->
    <div class="modal-overlay" v-if="isPackingModalOpen" style="z-index: 2000;">
      <div class="modal-content packing-modal">
        <h2>🛍️ 이동(포장) 시간을 선택해주세요</h2>
        <p class="modal-subtitle">이동 시간에 맞춰 드라이아이스를 준비해 드립니다.</p>
        
        <div class="packing-options">
          <button class="packing-btn" @click="selectPackingTime(10, 1)">
            <span class="time-text">10분 이하</span>
            <span class="dry-ice-text">(드라이아이스 1개)</span>
          </button>
          <button class="packing-btn" @click="selectPackingTime(30, 2)">
            <span class="time-text">30분 이하</span>
            <span class="dry-ice-text">(드라이아이스 2개)</span>
          </button>
          <button class="packing-btn" @click="selectPackingTime(60, 3)">
            <span class="time-text">30분 초과</span>
            <span class="dry-ice-text">(드라이아이스 3개)</span>
          </button>
        </div>

        <button class="btn-close" @click="isPackingModalOpen = false">취소</button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import axios from '@/api/axios'
import { callStaff } from '@/api/customer/callApi'
import { useBasketStore } from '@/stores/customer/basket'
import { useTimeoutStore } from '@/stores/customer/timeout'

const router = useRouter()
const basketStore = useBasketStore()
const timeoutStore = useTimeoutStore()
const { t } = useI18n({ useScope: 'global' })



// === [DB 연동용 반응형 변수] ===
const categories = ref([])
const dbProducts = ref([])
const dbOptions = ref([])
const dbFlavors = ref([])

const currentStoreId = ref(Number(localStorage.getItem('storeId')) || 1) 
const currentCategoryId = ref(null)
const isModalOpen = ref(false)
const isCartModalOpen = ref(false)
const selectedProduct = ref(null)
const isLoadingProducts = ref(false)

let intervalId = null

const editingCartIndex = ref(null)

const currentPage = ref(1)
const itemsPerPage = 12
const selectedFlavors = ref([])

const currentProductPage = ref(1)
const productsPerPage = 9
const spoonCount = ref(1)

// 새로운 UI 전용 상태
const currentModalTab = ref('INFO') // 'INFO' | 'FLAVOR'
const selectedContainer = ref('CUP')

const isPackingModalOpen = ref(false)
const pendingCartItem = ref(null)

const showToast = ref(false)
const toastText = ref('')
let toastTimeout = null

const displayToast = (msg) => {
  toastText.value = msg;
  showToast.value = true;
  if (toastTimeout) clearTimeout(toastTimeout);
  toastTimeout = setTimeout(() => {
    showToast.value = false;
  }, 2000);
}

const isContainerSelectable = computed(() => {
  if (!selectedProduct.value) return false;
  return ['싱글레귤러', '싱글킹', '더블주니어', '더블레귤러'].includes(selectedProduct.value.productName);
});

const cartCount = computed(() => basketStore.totalCount)
const totalPrice = computed(() => basketStore.totalPrice)

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
    console.error('카테고리 로드 실패:', error)
  }

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
  currentProductPage.value = 1
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
  // 아이스크림(카테고리 ID 1)이 아니면 모달을 띄우지 않고 바로 장바구니에 담기
  if (product.categoryId !== 1 && currentCategoryId.value !== 1) {
    const cartItem = {
      productId: product.productId,
      categoryId: product.categoryId || currentCategoryId.value,
      productName: product.productName,
      unitPrice: product.finalPrice || product.basePrice,
      quantity: 1,
      flavors: [],
      options: [],
      extraSpoons: false
    }
    await basketStore.addToCart(cartItem)
    return
  }

  editingCartIndex.value = null
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
    console.error('옵션 로드 실패:', error)
  }
  isModalOpen.value = true
}

const filteredProducts = computed(() => {
  return dbProducts.value.filter(p => p.categoryId === currentCategoryId.value)
})

const productTotalPages = computed(() => {
  return Math.max(1, Math.ceil(filteredProducts.value.length / productsPerPage))
})

const paginatedProducts = computed(() => {
  const start = (currentProductPage.value - 1) * productsPerPage
  const end = start + productsPerPage
  return filteredProducts.value.slice(start, end)
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

const getFlavorImage = (fId) => {
  const f = dbFlavors.value.find(x => x.flavorId === fId);
  return f ? f.imageUrl : null;
}

const formatCartItemName = (name) => {
  if (name.includes(' (')) {
    const parts = name.split(' (');
    const base = t(parts[0]);
    let container = parts[1].replace(')', '');
    if (container === 'CUP') container = t('컵');
    if (container === 'CONE') container = t('콘');
    if (container === 'WAFFLE') container = t('와플콘');
    return `${base} (${container})`;
  }
  return t(name);
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
  return selectedProduct.value.finalPrice + extraSpoonPrice
})

const formatPrice = (val) => val?.toLocaleString()
const closeModal = () => { 
  isModalOpen.value = false 
  editingCartIndex.value = null
}

const openEditOptionModal = async (index, cartItem) => {
  editingCartIndex.value = index
  
  // 1. 선택 상품 객체 복원 (finalPrice 계산: 스푼이 true면 개당 50원을 뺀 값으로 추정)
  const finalPrice = cartItem.extraSpoons ? cartItem.unitPrice - 50 : cartItem.unitPrice;
  selectedProduct.value = { 
    productId: cartItem.productId, 
    productName: cartItem.productName, 
    finalPrice: finalPrice 
  };
  
  // 2. 맛 배열 복원
  selectedFlavors.value = [];
  if (cartItem.flavors) {
    cartItem.flavors.forEach(f => {
      for(let i=0; i<f.quantity; i++) selectedFlavors.value.push(f.flavorId);
    });
  }
  
  // 3. 스푼 카운트 복원
  spoonCount.value = cartItem.extraSpoons ? 5 : 1;
  currentPage.value = 1;
  
  // 4. 상품 상세(옵션) 정보 다시 조회
  try {
    const detailRes = await axios.get(`/api/v1/kiosk/stores/${currentStoreId.value}/products/${cartItem.productId}/detail`)
    dbOptions.value = detailRes.data.options || [] 
  } catch (error) {
    console.error('상품 상세(옵션) 조회 실패:', error)
  }

  isCartModalOpen.value = false;
  isModalOpen.value = true;
}

const changeSpoon = (amount) => {
  const next = spoonCount.value + amount
  if (next >= 0 && next <= 10) spoonCount.value = next
}

const toggleFlavorSlot = (id) => {
  const idx = selectedFlavors.value.indexOf(id);
  if (idx > -1) {
    selectedFlavors.value.splice(idx, 1);
  } else if (!isFlavorMax.value) {
    selectedFlavors.value.push(id);
  }
}



const addCurrentItemToCart = async () => { 
  if (currentMaxFlavors.value > 0 && selectedFlavors.value.length < currentMaxFlavors.value) {
    displayToast(t('아이스크림 맛을 {count}가지 모두 선택해주세요.', { count: currentMaxFlavors.value }));
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

  const validOptions = [];
  if (dbOptions.value && dbOptions.value.length > 0) {
    validOptions.push(dbOptions.value[0].optionId);
  }

  const requestData = {
    productId: selectedProduct.value.productId,
    categoryId: currentCategoryId.value,
    productName: selectedProduct.value.productName + (isContainerSelectable.value && selectedContainer.value ? ` (${selectedContainer.value})` : ''),
    quantity: editingCartIndex.value !== null ? basketStore.cartItems[editingCartIndex.value].quantity : 1, // 수량 유지
    unitPrice: calculatedItemPrice.value,
    flavors: flavorData,
    options: validOptions,
    extraSpoons: spoonCount.value > 4
  }

  const targetProducts = ['파인트', '쿼터', '패밀리', '하프갤론', '하프갤런'];
  if (basketStore.orderType === 'TOGO' && targetProducts.includes(selectedProduct.value.productName) && !basketStore.dryIceMins) {
    pendingCartItem.value = requestData;
    isPackingModalOpen.value = true;
    return;
  }

  await processAddToCart(requestData);
}

const selectPackingTime = async (mins, count) => {
  basketStore.setDryIceMins(mins);
  basketStore.setDryIceCount(count);
  isPackingModalOpen.value = false;
  
  if (pendingCartItem.value) {
    await processAddToCart(pendingCartItem.value);
    pendingCartItem.value = null;
  }
}

const processAddToCart = async (requestData) => {
  try {
    if (editingCartIndex.value !== null) {
      await axios.put(`/api/customer/basket/item/${editingCartIndex.value}`, requestData);
      displayToast(t('{product} 상품 옵션이 수정되었습니다!', { product: t(selectedProduct.value.productName) }));
    } else {
      await axios.post('/api/customer/basket', requestData);
      displayToast(t('{product} 상품이 장바구니에 담겼습니다!', { product: t(selectedProduct.value.productName) }));
    }
    
    await basketStore.fetchBasket();
    closeModal();
    if (editingCartIndex.value !== null) {
      isCartModalOpen.value = true;
    }
  } catch (error) {
    console.error('장바구니 로직 실패:', error);
    displayToast(t('작업을 완료하는데 실패했습니다.'));
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
  basketStore.removeFromCart(index);
  displayToast(t("상품이 장바구니에서 삭제되었습니다."));
  if (basketStore.cartItems.length === 0) {
    closeCartModal();
  }
}

const goHome = async () => { 
  if (basketStore.cartItems.length > 0) {
    await basketStore.clearCart();
    router.push('/kiosk');
  } else {
    router.push('/kiosk');
  }
}

const goPayment = async () => { 
  if (basketStore.cartItems.length === 0) {
    displayToast(t('결제를 진행할 수 없습니다. (장바구니가 비어있는지 확인해주세요)'));
    return;
  }
  // 바로 포인트/할인 화면으로 넘어갑니다. (DB 주문 생성은 결제 최종 단계로 지연)
  router.push(`/point-discount`);
}

const handleCallStaff = async () => {
  try {
    const kioskNo = Number(localStorage.getItem('kioskId')) || 1;
    await callStaff({
      storeId: currentStoreId.value,
      kioskNo: kioskNo,
      reason: 'ORDER_SCREEN'
    });
    displayToast(t('직원을 호출했습니다. 잠시만 기다려주세요.'));
  } catch (error) {
    displayToast(t('직원 호출에 실패했습니다.'));
  }
}
</script>

<style scoped>
.kiosk-menu-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #fff;
  font-family: 'Pretendard', sans-serif;
  position: relative;
  overflow: hidden;
}

/* Header */
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
  width: 150px;
  text-align: right;
}

.timer-text {
  font-size: 1.2rem;
  font-weight: bold;
  color: #ff6b6b;
  background: #fff;
  padding: 5px 15px;
  border-radius: 20px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
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

.logo {
  height: 80px;
}

.kiosk-title{
  font-size : 32px;
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
  padding: 10px 15px;
  overflow-x: auto;
  gap: 10px;
  white-space: nowrap;
}
.category-tabs::-webkit-scrollbar { display: none; }
.tab-item {
  background: transparent;
  border: none;
  font-size: 0.95rem;
  font-weight: bold;
  color: #e91e63;
  cursor: pointer;
  padding: 10px 15px;
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
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  align-content: start;
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
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.1rem;
  font-weight: bold;
  box-shadow: 0 4px 15px rgba(233, 30, 99, 0.4);
  cursor: pointer;
  white-space: nowrap;
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
  position: relative;
}
.btn-close-circle {
  position: absolute;
  top: 15px;
  right: 15px;
  background-color: #ff4d4f;
  color: white;
  border: none;
  border-radius: 50%;
  width: 35px;
  height: 35px;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 5px rgba(0,0,0,0.2);
  z-index: 10;
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
  align-items: center;
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

.flavor-card.selected .flavor-image { border: 3px solid #e91e63; border-radius: 50%; }
.flavor-card.disabled { opacity: 0.4; pointer-events: none; }
.flavor-image { width: 80px; height: 80px; object-fit: contain; margin-bottom: 10px; }
.flavor-name { font-size: 0.85rem; color: #333;}
.pagination-controls-pink {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}
.btn-arrow-pink {
  background: #e91e63;
  color: white;
  border: none;
  border-radius: 50%;
  width: 25px;
  height: 25px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
}
.btn-arrow-pink:disabled {
  background: #f8bbd0;
  cursor: not-allowed;
}
.pagination-dots { display: flex; align-items: center; }
.dot { display: inline-block; width: 8px; height: 8px; background: #ddd; border-radius: 50%; margin: 0 4px; cursor: pointer; }
.dot.active { background: #e91e63; width: 10px; height: 10px; }

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
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.slot-circle.filled { background: white; border-style: solid; border-width: 2px; border-color: #e91e63; }
.slot-flavor-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
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

/* Toast Message */
.toast-message {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(233, 30, 99, 0.85);
  color: white;
  padding: 15px 30px;
  border-radius: 30px;
  font-size: 1.2rem;
  font-weight: bold;
  z-index: 2000;
  pointer-events: none;
  animation: fadeInOut 2s ease-in-out forwards;
}

@keyframes fadeInOut {
  0% { opacity: 0; transform: translate(-50%, -40%); }
  15% { opacity: 1; transform: translate(-50%, -50%); }
  85% { opacity: 1; transform: translate(-50%, -50%); }
  100% { opacity: 0; transform: translate(-50%, -60%); }
}

/* Packing Modal Styles */
.packing-modal h2 { color: #ff7c98; margin-bottom: 10px; }
.modal-subtitle { color: #666; margin-bottom: 25px; }
.packing-options { display: flex; flex-direction: column; gap: 15px; margin-bottom: 25px; }
.packing-btn { background: #f8f9fa; border: 2px solid #e9ecef; padding: 20px; border-radius: 10px; font-size: 1.2rem; display: flex; justify-content: space-between; align-items: center; cursor: pointer; transition: 0.2s; }
.packing-btn:hover { border-color: #ff7c98; background: #fff0f3; color: #ff7c98; }
.dry-ice-text { font-size: 1rem; color: #888; }
.packing-btn:hover .dry-ice-text { color: #ff7c98; }
</style>