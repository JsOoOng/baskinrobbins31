<!--
  [화면 흐름 안내] HeadProduct
  역할: 본사 관리에서 사용자가 보는 화면이다.
  진입: /head/products -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/head/headProductApi, @/api/head/headCategoryApi -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<script setup>
import {
  computed,
  onMounted,
  reactive,
  ref
} from 'vue'

import {
  createHeadProduct,
  extractProductData,
  extractProductErrorMessage,
  getHeadProductDetail,
  getHeadProducts,
  updateHeadProduct,
  updateHeadProductDisplay
} from '@/api/head/headProductApi'

import {
  getHeadCategories
} from '@/api/head/headCategoryApi'

import AppMessageToast
  from '@/components/common/AppMessageToast.vue'


/*
 * 서버 데이터
 */
const products = ref([])
const categories = ref([])

/*
 * 화면 상태
 */
const loading = ref(false)
const saving = ref(false)
const detailLoading = ref(false)
const displayUpdatingId = ref(null)

const searchKeyword = ref('')
const categoryFilter = ref('ALL')
const displayFilter = ref('ALL')


const message = ref('')
const messageType = ref('success')

/*
 * 등록·수정 모달
 */
const formModal = reactive({
  open: false,
  mode: 'create',
  productId: null
})

/*
 * 상세 모달
 */
const detailModal = reactive({
  open: false,
  product: null
})

/*
 * 상품 입력 폼
 */
 const form = reactive({
  categoryId: '',
  productName: '',
  description: '',
  basePrice: 0,
  discountRate: 0,
  isDisplay: true,
  storeIdsText: ''
})

/*
 * 백엔드 DTO 필드명이 약간 달라도
 * 화면에서는 동일한 이름으로 사용합니다.
 */
const normalizeProduct = (product = {}) => {
  return {
    productId:
      product.productId ??
      product.id ??
      null,

    categoryId:
      product.categoryId ??
      product.category?.categoryId ??
      product.category?.id ??
      null,

    categoryName:
      product.categoryName ??
      product.category?.categoryName ??
      '미분류',

    productName:
      product.productName ??
      product.name ??
      '상품명 없음',

    description:
      product.description ??
      product.productDescription ??
      '',

    basePrice:
      Number(
        product.basePrice ??
        product.price ??
        0
      ),

    discountRate:
      Number(
        product.discountRate ??
        0
      ),

    isDisplay:
      product.isDisplay ??
      product.display ??
      product.displayed ??
      true,

    options:
      Array.isArray(product.options)
        ? product.options
        : [],

    storeIds:
      Array.isArray(product.storeIds)
        ? product.storeIds
        : [],

    imageUrl:
      product.imageUrl ??
      product.productImage ??
      product.image ??
      '',

    raw: product
  }
}

/*
 * 검색 및 필터 결과
 */
const filteredProducts = computed(() => {
  const keyword =
    searchKeyword.value
      .trim()
      .toLowerCase()

  return products.value.filter((product) => {
    const matchesKeyword =
      !keyword ||
      product.productName
        .toLowerCase()
        .includes(keyword) ||
      String(product.productId)
        .includes(keyword)

    const matchesCategory =
      categoryFilter.value === 'ALL' ||
      String(product.categoryId) ===
        String(categoryFilter.value)

    const matchesDisplay =
      displayFilter.value === 'ALL' ||
      (
        displayFilter.value === 'DISPLAY' &&
        product.isDisplay
      ) ||
      (
        displayFilter.value === 'HIDDEN' &&
        !product.isDisplay
      )

    return (
      matchesKeyword &&
      matchesCategory &&
      matchesDisplay
    )
  })
})

const displayedProductCount = computed(() => {
  return products.value.filter(
    (product) => product.isDisplay
  ).length
})

const hiddenProductCount = computed(() => {
  return products.value.filter(
    (product) => !product.isDisplay
  ).length
})

const discountedProductCount = computed(() => {
  return products.value.filter(
    (product) =>
      Number(product.discountRate) > 0
  ).length
})

/*
 * 최종 판매가 계산
 */
const calculateSalePrice = (product) => {
  const basePrice =
    Number(product.basePrice || 0)

  const discountRate =
    Number(product.discountRate || 0)

  if (discountRate <= 0) {
    return basePrice
  }

  return Math.floor(
    basePrice *
    (1 - discountRate / 100)
  )
}

const formatPrice = (price) => {
  return `${Number(price || 0)
    .toLocaleString('ko-KR')}원`
}

/*
 * 상품 목록 조회
 */
const loadProducts = async () => {
  loading.value = true

  try {
    const responseBody =
      await getHeadProducts()

    const responseData =
      extractProductData(responseBody)

    const productList =
      Array.isArray(responseData)
        ? responseData
        : []

    products.value =
      productList.map(normalizeProduct)

  } catch (error) {
    showMessage(
      extractProductErrorMessage(
        error,
        '상품 목록을 불러오지 못했습니다.'
      ),
      'error'
    )

  } finally {
    loading.value = false
  }
}

/*
 * 카테고리 목록 조회
 */
const loadCategories = async () => {
  try {
    const responseBody =
      await getHeadCategories()

    const responseData =
      extractProductData(responseBody)

    categories.value =
      Array.isArray(responseData)
        ? responseData
        : []

  } catch (error) {
    showMessage(
      extractProductErrorMessage(
        error,
        '카테고리 목록을 불러오지 못했습니다.'
      ),
      'error'
    )
  }
}

/*
 * 입력 폼 초기화
 */
const resetForm = () => {
  form.categoryId = ''
  form.productName = ''
  form.description = ''
  form.basePrice = 0
  form.discountRate = 0
  form.isDisplay = true
  form.storeIdsText = ''
  }

/*
 * 상품 등록 모달
 */
const openCreateModal = () => {
  resetForm()

  formModal.mode = 'create'
  formModal.productId = null
  formModal.open = true
}

/*
 * 상품 수정 모달
 */
const openEditModal = async (productId) => {
  saving.value = false
  detailLoading.value = true
  clearMessage()

  try {
    const responseBody =
      await getHeadProductDetail(productId)

    const product =
      normalizeProduct(
        extractProductData(responseBody)
      )

    form.categoryId =
      product.categoryId ?? ''

    form.productName =
      product.productName

    form.description =
      product.description

    form.basePrice =
      product.basePrice

    form.discountRate =
      product.discountRate

    form.isDisplay =
      product.isDisplay

    form.storeIdsText =
      product.storeIds.join(', ')

    formModal.mode = 'edit'
    formModal.productId = productId
    formModal.open = true

  } catch (error) {
    showMessage(
      extractProductErrorMessage(
        error,
        '상품 정보를 불러오지 못했습니다.'
      ),
      'error'
    )

  } finally {
    detailLoading.value = false
  }
}

const closeFormModal = () => {
  if (
    saving.value ||
    flavorSaving.value
  ) {
    return
  }

  formModal.open = false
}

/*
 * 쉼표 문자열을 숫자 배열로 변환
 *
 * "1, 2, 3"
 * →
 * [1, 2, 3]
 */
const parseStoreIds = () => {
  if (!form.storeIdsText.trim()) {
    return []
  }

  const storeIds = form.storeIdsText
    .split(',')
    .map((value) =>
      Number(value.trim())
    )
    .filter((value) =>
      Number.isInteger(value) &&
      value > 0
    )

  return [...new Set(storeIds)]
}

/*
 * 상품 등록·수정 요청 데이터 생성
 *
 * 아이스크림 맛 정보는 포함하지 않습니다.
 */
 const createRequestPayload = () => {
  const payload = {
    categoryId:
      Number(form.categoryId),

    productName:
      form.productName.trim(),

    description:
      form.description.trim(),

    basePrice:
      Number(form.basePrice),

    discountRate:
      Number(form.discountRate),

    isDisplay:
      Boolean(form.isDisplay)
  }

  /*
   * 판매 지점은 신규 상품 등록 시에만 전송합니다.
   */
  if (formModal.mode === 'create') {
    payload.storeIds =
      parseStoreIds()
  }

  return payload
}


/*
 * 입력값 검증
 */
const validateForm = () => {
  if (!form.categoryId) {
    showMessage(
      '카테고리를 선택해주세요.',
      'error'
    )

    return false
  }

  if (!form.productName.trim()) {
    showMessage(
      '상품명을 입력해주세요.',
      'error'
    )

    return false
  }

  if (
    Number.isNaN(Number(form.basePrice)) ||
    Number(form.basePrice) < 0
  ) {
    showMessage(
      '기본 가격은 0 이상의 숫자로 입력해주세요.',
      'error'
    )

    return false
  }

  if (
    Number.isNaN(Number(form.discountRate)) ||
    Number(form.discountRate) < 0 ||
    Number(form.discountRate) > 100
  ) {
    showMessage(
      '할인율은 0부터 100 사이로 입력해주세요.',
      'error'
    )

    return false
  }

  if (formModal.mode === 'create') {
    if (!form.storeIdsText.trim()) {
      showMessage(
        '판매 지점 ID를 한 개 이상 입력해주세요.',
        'error'
      )

      return false
    }

    const storeIdTexts = form.storeIdsText
      .split(',')
      .map((value) => value.trim())

    const hasInvalidStoreId =
      storeIdTexts.some((value) => {
        const storeId = Number(value)

        return (
          value === '' ||
          !Number.isInteger(storeId) ||
          storeId <= 0
        )
      })

    if (hasInvalidStoreId) {
      showMessage(
        '판매 지점 ID는 1 이상의 숫자만 입력해주세요.',
        'error'
      )

      return false
    }

    if (parseStoreIds().length === 0) {
      showMessage(
        '판매 지점 ID를 한 개 이상 입력해주세요.',
        'error'
      )

      return false
    }
  }
  return true
}

/*
 * 상품 저장
 */
const submitProduct = async () => {
  if (!validateForm()) {
    return
  }

  saving.value = true
  clearMessage()

  try {
    const payload =
      createRequestPayload()

    if (formModal.mode === 'create') {
      await createHeadProduct(payload)

      showMessage(
        '상품이 등록되었습니다.',
        'success'
      )

    } else {
      await updateHeadProduct(
        formModal.productId,
        payload
      )

      showMessage(
        '상품 정보가 수정되었습니다.',
        'success'
      )
    }

    formModal.open = false

    await loadProducts()

  } catch (error) {
    showMessage(
      extractProductErrorMessage(
        error,
        formModal.mode === 'create'
          ? '상품 등록에 실패했습니다.'
          : '상품 수정에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    saving.value = false
  }
}

/*
 * 상품 상세 조회
 */
const openDetailModal = async (productId) => {
  detailModal.open = true
  detailModal.product = null
  detailLoading.value = true
  clearMessage()

  try {
    const responseBody =
      await getHeadProductDetail(productId)

    detailModal.product =
      normalizeProduct(
        extractProductData(responseBody)
      )

  } catch (error) {
    detailModal.open = false

    showMessage(
      extractProductErrorMessage(
        error,
        '상품 상세 정보를 불러오지 못했습니다.'
      ),
      'error'
    )

  } finally {
    detailLoading.value = false
  }
}

const closeDetailModal = () => {
  detailModal.open = false
  detailModal.product = null
}

const editDetailProduct = () => {
  const productId =
    detailModal.product?.productId

  if (!productId) {
    return
  }

  closeDetailModal()
  openEditModal(productId)
}

/*
 * 고객 화면 노출 상태 변경
 */
const toggleDisplay = async (product) => {
  const nextDisplay =
    !product.isDisplay

  const actionText =
    nextDisplay
      ? '노출'
      : '숨김'

  const confirmed = window.confirm(
    `"${product.productName}" 상품을 고객 화면에서 ${actionText} 처리하시겠습니까?`
  )

  if (!confirmed) {
    return
  }

  displayUpdatingId.value =
    product.productId

  clearMessage()

  try {
    await updateHeadProductDisplay(
      product.productId,
      nextDisplay
    )

    product.isDisplay =
      nextDisplay

    showMessage(
      `상품이 ${actionText} 처리되었습니다.`,
      'success'
    )

  } catch (error) {
    showMessage(
      extractProductErrorMessage(
        error,
        '상품 노출 상태 변경에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    displayUpdatingId.value = null
  }
}

const showMessage = (
  text,
  type = 'success'
) => {
  message.value = text
  messageType.value = type
}

const clearMessage = () => {
  message.value = ''
}


/*
 * 선택된 카테고리
 */
const selectedCategory =
  computed(() => {
    return categories.value.find(
      (category) =>
        String(category.categoryId) ===
        String(form.categoryId)
    )
  })

/*
 * 아이스크림 카테고리에서만
 * 독립적인 맛 등록 영역을 표시합니다.
 *
 * 케이크 상품명과 일반 상품명은
 * icecream_flavors에 저장하지 않습니다.
 */
const isFlavorCategory =
  computed(() => {
    const categoryName =
      selectedCategory.value
        ?.categoryName
        ?.trim()

    return categoryName === '아이스크림'
  })

const normalizeFlavorName = (
  value
) => {
  return String(value ?? '')
    .trim()
    .toLocaleLowerCase('ko-KR')
}

const isDuplicateFlavorName = (
  flavorName
) => {
  const normalizedName =
    normalizeFlavorName(flavorName)

  return flavors.value.some(
    (flavor) =>
      normalizeFlavorName(
        flavor.flavorName
      ) === normalizedName
  )
}

const validateFlavorInput = () => {
  const flavorName =
    newFlavorName.value.trim()

  const imageUrl =
    newFlavorImageUrl.value.trim()

  if (!flavorName) {
    showMessage(
      '추가할 맛 이름을 입력해주세요.',
      'error'
    )

    return false
  }

  if (!imageUrl) {
    showMessage(
      '맛 이미지 URL을 입력해주세요.',
      'error'
    )

    return false
  }

  /*
   * 허용 예:
   * /images/flavors/black_sorbet.png
   * /images/flavors/black_sorbet.jpg
   */
  const flavorImagePattern =
    /^\/images\/flavors\/[a-z0-9]+(?:_[a-z0-9]+)*\.(?:png|jpg)$/

  if (
    !flavorImagePattern.test(
      imageUrl
    )
  ) {
    showMessage(
      '이미지 URL은 /images/flavors/파일명.png 또는 /images/flavors/파일명.jpg 형식이어야 하며, 파일명에는 영문 소문자·숫자·언더스코어만 사용할 수 있습니다.',
      'error'
    )

    return false
  }

  return true
}


onMounted(async () => {
  await Promise.all([
    loadCategories(),
    loadProducts()
  ])
})
</script>

<template>
  <AppMessageToast
    :message="message"
    :type="messageType"
    @close="clearMessage"
  />

  <section class="product-page">

    <!-- 요약 카드 -->
    <div class="summary-grid">
      <article class="summary-card">
        <div>
          <p>전체 상품</p>

          <strong>
            {{ products.length }}
          </strong>

          <small>본사 등록 상품</small>
        </div>

        <span class="summary-icon purple">
          ▣
        </span>
      </article>

      <article class="summary-card">
        <div>
          <p>고객 화면 노출</p>

          <strong>
            {{ displayedProductCount }}
          </strong>

          <small>현재 판매 화면에 표시</small>
        </div>

        <span class="summary-icon green">
          ✓
        </span>
      </article>

      <article class="summary-card">
        <div>
          <p>숨김 상품</p>

          <strong>
            {{ hiddenProductCount }}
          </strong>

          <small>고객 화면 미노출</small>
        </div>

        <span class="summary-icon gray">
          −
        </span>
      </article>

      <article class="summary-card">
        <div>
          <p>할인 적용 상품</p>

          <strong>
            {{ discountedProductCount }}
          </strong>

          <small>할인율 1% 이상</small>
        </div>

        <span class="summary-icon pink">
          %
        </span>
      </article>
    </div>

    <!-- 목록 패널 -->
    <section class="product-panel">
      <div class="panel-header">
        <div>
          <h2>본사 메뉴 목록</h2>

          <p>
            상품 정보와 가격, 할인율 및 고객 화면 노출 상태를 관리합니다.
          </p>
        </div>

        <div class="panel-actions">
          <input
            v-model="searchKeyword"
            type="search"
            placeholder="상품명 또는 ID 검색"
          />

          <select v-model="categoryFilter">
            <option value="ALL">
              전체 카테고리
            </option>

            <option
              v-for="category in categories"
              :key="category.categoryId"
              :value="category.categoryId"
            >
              {{ category.categoryName }}
            </option>
          </select>

          <select v-model="displayFilter">
            <option value="ALL">
              전체 노출 상태
            </option>

            <option value="DISPLAY">
              노출 중
            </option>

            <option value="HIDDEN">
              숨김
            </option>
          </select>

          <button
            type="button"
            class="create-button"
            @click="openCreateModal"
          >
            ＋ 상품 등록
          </button>
        </div>
      </div>

      <!-- 로딩 -->
      <div
        v-if="loading"
        class="loading-area"
      >
        <span class="spinner" />

        <p>
          상품 목록을 불러오는 중입니다.
        </p>
      </div>

      <!-- 상품 목록 -->
      <div v-else class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>상품</th>
              <th>카테고리</th>
              <th>기본 가격</th>
              <th>할인율</th>
              <th>최종 가격</th>
              <th>고객 노출</th>
              <th>관리</th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="product in filteredProducts"
              :key="product.productId"
            >
              <td>
                <div class="product-info">
                  <div class="product-image">
                    <img
                      v-if="product.imageUrl"
                      :src="product.imageUrl"
                      :alt="product.productName"
                    />

                    <span v-else>
                      BR
                    </span>
                  </div>

                  <div>
                    <strong>
                      {{ product.productName }}
                    </strong>

                    <small>
                      상품 ID:
                      {{ product.productId }}
                    </small>
                  </div>
                </div>
              </td>

              <td>
                {{ product.categoryName }}
              </td>

              <td class="price-cell">
                {{ formatPrice(product.basePrice) }}
              </td>

              <td>
                <span
                  v-if="product.discountRate > 0"
                  class="discount-badge"
                >
                  {{ product.discountRate }}%
                </span>

                <span v-else class="muted">
                  없음
                </span>
              </td>

              <td class="sale-price-cell">
                {{
                  formatPrice(
                    calculateSalePrice(product)
                  )
                }}
              </td>

              <td>
                <button
                  type="button"
                  class="display-toggle"
                  :class="{
                    active: product.isDisplay
                  }"
                  :disabled="
                    displayUpdatingId ===
                    product.productId
                  "
                  @click="toggleDisplay(product)"
                >
                  <span />

                  {{
                    displayUpdatingId ===
                      product.productId
                      ? '변경 중'
                      : product.isDisplay
                        ? '노출 중'
                        : '숨김'
                  }}
                </button>
              </td>

              <td>
                <div class="management-buttons">
                  <button
                    type="button"
                    class="detail-button"
                    @click="
                      openDetailModal(
                        product.productId
                      )
                    "
                  >
                    상세
                  </button>

                  <button
                    type="button"
                    class="edit-button"
                    @click="
                      openEditModal(
                        product.productId
                      )
                    "
                  >
                    수정
                  </button>
                </div>
              </td>
            </tr>

            <tr
              v-if="filteredProducts.length === 0"
            >
              <td
                colspan="7"
                class="empty-cell"
              >
                <strong>
                  조회된 상품이 없습니다.
                </strong>

                <p>
                  검색 조건을 변경하거나 상품을 등록해주세요.
                </p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="panel-footer">
        <span>
          전체 {{ products.length }}개 중
          {{ filteredProducts.length }}개 표시
        </span>

        <button
          type="button"
          @click="loadProducts"
        >
          새로고침
        </button>
      </div>
    </section>

    <!-- 등록·수정 모달 -->
    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="formModal.open"
          class="modal-backdrop"
          @click.self="closeFormModal"
        >
          <form
            class="product-form-modal"
            @submit.prevent="submitProduct"
          >
            <div class="modal-header">
              <div>
                <p>PRODUCT MANAGEMENT</p>

                <h2>
                  {{
                    formModal.mode === 'create'
                      ? '상품 등록'
                      : '상품 수정'
                  }}
                </h2>
              </div>

              <button
                type="button"
                :disabled="
                  saving ||
                  flavorSaving
                "
                aria-label="닫기"
                @click="closeFormModal"
              >
                ×
              </button>
            </div>

            <div class="modal-body">
              <div class="form-grid">
                <label>
                  <span>
                    카테고리
                    <strong>*</strong>
                  </span>

                  <select
                    v-model="form.categoryId"
                    :disabled="saving"
                  >
                    <option value="">
                      카테고리를 선택하세요
                    </option>

                    <option
                      v-for="category in categories"
                      :key="category.categoryId"
                      :value="category.categoryId"
                    >
                      {{ category.categoryName }}
                    </option>
                  </select>
                </label>

                <label>
                  <span>
                    상품명
                    <strong>*</strong>
                  </span>

                  <input
                    v-model="form.productName"
                    type="text"
                    maxlength="100"
                    placeholder="예: 싱글 레귤러"
                    :disabled="saving"
                  />
                </label>

                <label>
                  <span>
                    기본 가격
                    <strong>*</strong>
                  </span>

                  <input
                    v-model.number="form.basePrice"
                    type="number"
                    min="0"
                    step="100"
                    :disabled="saving"
                  />
                </label>

                <label>
                  <span>할인율</span>

                  <div class="input-suffix">
                    <input
                      v-model.number="form.discountRate"
                      type="number"
                      min="0"
                      max="100"
                      :disabled="saving"
                    />

                    <span>%</span>
                  </div>
                </label>
              </div>

              <section
                v-if="
                  formModal.mode === 'create' &&
                  isFlavorCategory
                "
                class="flavor-section"
              >
                <div class="flavor-section-header">
                  <div>
                    <strong>
                      아이스크림 맛 추가
                    </strong>

                    <p>
                      상품 등록과 별도로 icecream_flavors에 저장됩니다.
                    </p>
                  </div>
                </div>

                <div class="flavor-input-list">
                  <label>
                    <span>
                      맛 이름
                      <strong>*</strong>
                    </span>

                    <input
                      v-model="newFlavorName"
                      type="text"
                      maxlength="50"
                      placeholder="예: 바람과 함께 사라지다"
                      :disabled="
                        saving ||
                        flavorSaving
                      "
                    />
                  </label>

                  <label>
                    <span>
                      이미지 URL
                      <strong>*</strong>
                    </span>

                    <input
                      v-model="newFlavorImageUrl"
                      type="text"
                      maxlength="500"
                      placeholder="예: /images/flavors/twinberry_cheesecake.png"
                      :disabled="
                        saving ||
                        flavorSaving
                      "
                      @keydown.enter.prevent="
                        addNewFlavor
                      "
                    />
                  </label>
                </div>

                <div class="flavor-add-footer">
                  <p>
                    같은 맛 이름은 중복 등록되지 않습니다.
                  </p>

                  <button
                    type="button"
                    :disabled="
                      saving ||
                      flavorSaving
                    "
                    @click="addNewFlavor"
                  >
                    {{
                      flavorSaving
                        ? '확인 중...'
                        : '맛 추가'
                    }}
                  </button>
                </div>
              </section>

              <label class="full-field">
                <span>상품 설명</span>

                <textarea
                  v-model="form.description"
                  rows="4"
                  maxlength="1000"
                  placeholder="상품 설명을 입력하세요."
                  :disabled="saving"
                />
              </label>

              <label class="full-field">
                <span>
                  판매 지점 ID
                  <strong>*</strong>
                </span>

                <input
                  v-model="form.storeIdsText"
                  type="text"
                  placeholder="예: 1, 2, 3"
                  :disabled="saving"
                />

                <small>
                  판매할 지점 ID를 입력해주세요. 여러 지점은 쉼표로 구분합니다.
                </small>
              </label>

              <label class="display-field">
                <div>
                  <span>고객 화면 노출</span>

                  <small>
                    활성화하면 고객 키오스크에 상품이 표시됩니다.
                  </small>
                </div>

                <button
                  type="button"
                  class="form-toggle"
                  :class="{
                    active: form.isDisplay
                  }"
                  @click="
                    form.isDisplay =
                      !form.isDisplay
                  "
                >
                  <span />

                  {{
                    form.isDisplay
                      ? '노출'
                      : '숨김'
                  }}
                </button>
              </label>
            </div>

            <div class="modal-footer">
              <button
                type="button"
                class="cancel-button"
                :disabled="
                  saving ||
                  flavorSaving
                "
                @click="closeFormModal"
              >
                취소
              </button>

              <button
                type="submit"
                class="save-button"
                :disabled="
                  saving ||
                  flavorSaving
                "
              >
                {{
                  flavorSaving
                    ? '맛 처리 중...'
                    : saving
                      ? '저장 중...'
                      : formModal.mode === 'create'
                        ? '상품 등록'
                        : '상품 수정'
                }}
              </button>
            </div>
          </form>
        </div>
      </Transition>
    </Teleport>

    <!-- 상세 모달 -->
    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="detailModal.open"
          class="modal-backdrop"
          @click.self="closeDetailModal"
        >
          <section class="detail-modal">
            <button
              type="button"
              class="modal-close-button"
              @click="closeDetailModal"
            >
              ×
            </button>

            <div
              v-if="detailLoading"
              class="loading-area"
            >
              <span class="spinner" />

              <p>
                상품 상세 정보를 불러오는 중입니다.
              </p>
            </div>

            <template
              v-else-if="detailModal.product"
            >
              <p class="detail-label">
                PRODUCT DETAIL
              </p>

              <h2>
                {{
                  detailModal.product.productName
                }}
              </h2>

              <dl>
                <div>
                  <dt>상품 ID</dt>

                  <dd>
                    {{
                      detailModal.product.productId
                    }}
                  </dd>
                </div>

                <div>
                  <dt>카테고리</dt>

                  <dd>
                    {{
                      detailModal.product.categoryName
                    }}
                  </dd>
                </div>

                <div>
                  <dt>기본 가격</dt>

                  <dd>
                    {{
                      formatPrice(
                        detailModal.product.basePrice
                      )
                    }}
                  </dd>
                </div>

                <div>
                  <dt>할인율</dt>

                  <dd>
                    {{
                      detailModal.product.discountRate
                    }}%
                  </dd>
                </div>

                <div>
                  <dt>최종 가격</dt>

                  <dd>
                    {{
                      formatPrice(
                        calculateSalePrice(
                          detailModal.product
                        )
                      )
                    }}
                  </dd>
                </div>

                <div>
                  <dt>고객 화면</dt>

                  <dd>
                    {{
                      detailModal.product.isDisplay
                        ? '노출 중'
                        : '숨김'
                    }}
                  </dd>
                </div>

                <div>
                  <dt>상품 설명</dt>

                  <dd>
                    {{
                      detailModal.product.description ||
                      '등록된 설명이 없습니다.'
                    }}
                  </dd>
                </div>
              </dl>

              <div class="detail-options">
                <h3>
                  상품 옵션
                </h3>

                <div
                  v-if="
                    detailModal.product.options.length === 0
                  "
                  class="empty-option"
                >
                  등록된 상품 옵션이 없습니다.
                </div>

                <div
                  v-for="option in detailModal.product.options"
                  :key="
                    option.optionId ??
                    `${option.optionType}-${option.optionName}`
                  "
                  class="option-item"
                >
                  <strong>
                    {{ option.optionName }}
                  </strong>

                  <span>
                    {{
                      formatPrice(
                        option.extraPrice ?? 0
                      )
                    }}
                  </span>
                </div>
              </div>

              <button
                type="button"
                class="detail-edit-button"
                @click="
                  closeDetailModal();
                  openEditModal(
                    detailModal.product?.productId
                  )
                "
              >
                상품 정보 수정
              </button>
            </template>
          </section>
        </div>
      </Transition>
    </Teleport>
  </section>
</template>

<style scoped>
.product-page {
  display: grid;
  gap: 18px;
}

.summary-grid {
  display: grid;
  grid-template-columns:
    repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  display: flex;
  justify-content: space-between;
  min-height: 128px;
  padding: 18px;
  border: 1px solid #e3e6ed;
  border-radius: 15px;
  background: #ffffff;
  box-shadow:
    0 5px 18px rgba(44, 50, 70, 0.04);
}

.summary-card p {
  margin: 0;
  color: #798191;
  font-size: 11px;
  font-weight: 700;
}

.summary-card strong {
  display: block;
  margin-top: 13px;
  color: #262b38;
  font-size: 25px;
}

.summary-card small {
  display: block;
  margin-top: 8px;
  color: #969dab;
  font-size: 9px;
}

.summary-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 35px;
  height: 35px;
  border-radius: 10px;
  font-weight: 900;
}

.purple {
  color: #6654df;
  background: #edeaff;
}

.green {
  color: #159b68;
  background: #dff9ed;
}

.gray {
  color: #707887;
  background: #edf0f4;
}

.pink {
  color: #df4c87;
  background: #ffe4f0;
}

.product-panel {
  overflow: hidden;
  border: 1px solid #e2e6ed;
  border-radius: 15px;
  background: #ffffff;
}

.panel-header {
  display: flex;
  gap: 18px;
  align-items: center;
  justify-content: space-between;
  padding: 18px;
  border-bottom: 1px solid #e8ebf0;
}

.panel-header h2 {
  margin: 0;
  color: #2b303d;
  font-size: 15px;
}

.panel-header p {
  margin: 5px 0 0;
  color: #9299a8;
  font-size: 10px;
}

.panel-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.panel-actions input,
.panel-actions select {
  height: 36px;
  padding: 0 11px;
  border: 1px solid #dfe3ea;
  border-radius: 9px;
  outline: none;
  color: #454b59;
  font-size: 10px;
  background: #ffffff;
}

.create-button {
  height: 36px;
  padding: 0 13px;
  border: 0;
  border-radius: 9px;
  cursor: pointer;
  color: #ffffff;
  font-size: 10px;
  font-weight: 800;
  background: #725ee7;
}

.table-scroll {
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 1000px;
  border-collapse: collapse;
}

th,
td {
  padding: 12px 15px;
  text-align: left;
  font-size: 10px;
}

th {
  color: #7e8696;
  background: #fafbfc;
}

td {
  border-top: 1px solid #edf0f4;
  color: #515867;
}

.product-info {
  display: flex;
  gap: 10px;
  align-items: center;
}

.product-image {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  width: 42px;
  height: 42px;
  border-radius: 10px;
  color: #735ee7;
  font-weight: 900;
  background: #eeebff;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info strong,
.product-info small {
  display: block;
}

.product-info small {
  margin-top: 4px;
  color: #9aa1af;
  font-size: 9px;
}

.price-cell {
  font-weight: 700;
}

.sale-price-cell {
  color: #d84774;
  font-weight: 800;
}

.discount-badge {
  display: inline-flex;
  padding: 5px 8px;
  border-radius: 7px;
  color: #df4a72;
  font-size: 9px;
  font-weight: 800;
  background: #ffe5ed;
}

.muted {
  color: #a0a6b2;
}

.display-toggle,
.form-toggle {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  border: 0;
  border-radius: 20px;
  cursor: pointer;
  color: #767e8d;
  font-size: 9px;
  font-weight: 750;
  background: #edf0f4;
}

.display-toggle {
  min-width: 70px;
  height: 29px;
  padding: 0 9px;
}

.display-toggle span,
.form-toggle span {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #9ba2af;
}

.display-toggle.active,
.form-toggle.active {
  color: #168e61;
  background: #dff8ec;
}

.display-toggle.active span,
.form-toggle.active span {
  background: #24b579;
}

.management-buttons {
  display: flex;
  gap: 6px;
}

.management-buttons button {
  height: 29px;
  padding: 0 10px;
  border-radius: 7px;
  cursor: pointer;
  font-size: 9px;
  font-weight: 750;
}

.detail-button {
  border: 1px solid #dfe3e9;
  color: #5f6775;
  background: #ffffff;
}

.edit-button {
  border: 1px solid #dcd7fb;
  color: #6756dc;
  background: #f3f1ff;
}

.empty-cell {
  height: 170px;
  text-align: center;
}

.empty-cell strong {
  color: #5f6675;
  font-size: 12px;
}

.empty-cell p {
  margin: 8px 0 0;
  color: #9ba2b0;
  font-size: 10px;
}

.panel-footer {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid #e8ebf0;
  color: #8c94a3;
  font-size: 9px;
}

.panel-footer button {
  height: 29px;
  padding: 0 10px;
  border: 1px solid #dfe3e9;
  border-radius: 7px;
  cursor: pointer;
  color: #616978;
  font-size: 9px;
  background: #ffffff;
}

.loading-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  justify-content: center;
  min-height: 240px;
  color: #9097a6;
  font-size: 11px;
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #e7e4fa;
  border-top-color: #715ee6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.modal-backdrop {
  position: fixed;
  z-index: 3000;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(25, 29, 42, 0.52);
  backdrop-filter: blur(4px);
}

.product-form-modal,
.detail-modal {
  overflow-y: auto;
  width: 100%;
  max-height: calc(100vh - 48px);
  border: 1px solid #e3e6ed;
  border-radius: 18px;
  background: #ffffff;
  box-shadow:
    0 28px 70px rgba(23, 27, 43, 0.22);
}

.product-form-modal {
  max-width: 620px;
}

.detail-modal {
  position: relative;
  max-width: 520px;
  padding: 27px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  padding: 21px 22px 17px;
  border-bottom: 1px solid #ebedf1;
}

.modal-header p,
.detail-label {
  margin: 0 0 5px;
  color: #725fe5;
  font-size: 9px;
  font-weight: 900;
  letter-spacing: 1.2px;
}

.modal-header h2,
.detail-modal h2 {
  margin: 0;
  color: #292e3b;
  font-size: 20px;
}

.modal-header button,
.modal-close-button {
  border: 0;
  cursor: pointer;
  color: #858c9a;
  font-size: 25px;
  background: transparent;
}

.modal-close-button {
  position: absolute;
  top: 13px;
  right: 15px;
}

.modal-body {
  display: grid;
  gap: 18px;
  padding: 22px;
}

.form-grid {
  display: grid;
  grid-template-columns:
    repeat(2, minmax(0, 1fr));
  gap: 17px;
}

.modal-body label {
  display: grid;
  gap: 8px;
}

.modal-body label > span {
  color: #464c5a;
  font-size: 11px;
  font-weight: 750;
}

.modal-body label > span strong {
  color: #e34d64;
}

.modal-body input,
.modal-body select,
.modal-body textarea {
  width: 100%;
  border: 1px solid #dfe3ea;
  border-radius: 9px;
  outline: none;
  color: #3b414e;
  font-size: 11px;
  background: #ffffff;
}

.modal-body input,
.modal-body select {
  height: 43px;
  padding: 0 12px;
}

.modal-body textarea {
  padding: 11px 12px;
  resize: vertical;
}

.modal-body input:focus,
.modal-body select:focus,
.modal-body textarea:focus {
  border-color: #7461e7;
  box-shadow:
    0 0 0 3px rgba(116, 97, 231, 0.1);
}

.modal-body small {
  color: #969dab;
  font-size: 9px;
}

.input-suffix {
  position: relative;
}

.input-suffix input {
  padding-right: 40px;
}

.input-suffix span {
  position: absolute;
  top: 50%;
  right: 13px;
  color: #858c9b;
  font-size: 11px;
  transform: translateY(-50%);
}

.display-field {
  display: flex !important;
  align-items: center;
  justify-content: space-between;
  padding: 13px 14px;
  border: 1px solid #e2e5eb;
  border-radius: 10px;
  background: #f9fafc;
}

.display-field small {
  display: block;
  margin-top: 4px;
}

.form-toggle {
  min-width: 68px;
  height: 31px;
  justify-content: center;
  padding: 0 10px;
}

.modal-footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding: 15px 22px 20px;
}

.modal-footer button {
  height: 39px;
  padding: 0 16px;
  border-radius: 9px;
  cursor: pointer;
  font-size: 10px;
  font-weight: 800;
}

.cancel-button {
  border: 1px solid #dfe3e9;
  color: #646c7a;
  background: #ffffff;
}

.save-button {
  border: 1px solid #725ee7;
  color: #ffffff;
  background: #725ee7;
}

.detail-modal dl {
  display: grid;
  gap: 1px;
  margin: 21px 0 0;
  background: #edf0f4;
}

.detail-modal dl div {
  display: grid;
  grid-template-columns: 115px 1fr;
  padding: 12px;
  background: #ffffff;
}

.detail-modal dt {
  color: #888f9e;
  font-size: 10px;
}

.detail-modal dd {
  margin: 0;
  color: #414754;
  font-size: 11px;
}

.detail-options {
  margin-top: 21px;
}

.detail-options h3 {
  margin: 0 0 10px;
  font-size: 12px;
}

.empty-option {
  padding: 18px;
  border-radius: 9px;
  color: #9299a8;
  font-size: 10px;
  text-align: center;
  background: #f6f7f9;
}

.option-item {
  display: flex;
  justify-content: space-between;
  padding: 11px;
  border-bottom: 1px solid #eceef2;
  font-size: 10px;
}

.detail-edit-button {
  width: 100%;
  height: 42px;
  margin-top: 22px;
  border: 0;
  border-radius: 9px;
  cursor: pointer;
  color: #ffffff;
  font-size: 11px;
  font-weight: 800;
  background: #725ee7;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

@media (max-width: 1150px) {
  .summary-grid {
    grid-template-columns:
      repeat(2, minmax(0, 1fr));
  }

  .panel-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .panel-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}

@media (max-width: 600px) {
  .summary-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .panel-actions input,
  .panel-actions select {
    width: 100%;
  }
}

.flavor-section {
  display: grid;
  gap: 14px;
  padding: 15px;
  border: 1px solid #ddd8fb;
  border-radius: 11px;
  background: #f8f7ff;
}

.flavor-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flavor-section-header strong {
  color: #454b59;
  font-size: 11px;
}

.flavor-section-header p {
  margin: 4px 0 0;
  color: #9299a8;
  font-size: 9px;
}

.flavor-input-list {
  display: grid;
  grid-template-columns:
    repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.flavor-input-list label {
  display: grid;
  gap: 8px;
}

.flavor-input-list label > span {
  color: #464c5a;
  font-size: 10px;
  font-weight: 750;
}

.flavor-input-list label > span strong {
  color: #e34d64;
}

.flavor-add-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.flavor-add-footer p {
  margin: 0;
  color: #9299a8;
  font-size: 9px;
}

.flavor-add-footer button {
  min-width: 82px;
  height: 38px;
  padding: 0 14px;
  border: 0;
  border-radius: 9px;
  cursor: pointer;
  color: #ffffff;
  font-size: 9px;
  font-weight: 800;
  background: #725ee7;
}

.flavor-add-footer button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

@media (max-width: 600px) {
  .flavor-input-list {
    grid-template-columns: 1fr;
  }

  .flavor-add-footer {
    align-items: stretch;
    flex-direction: column;
  }

  .flavor-add-footer button {
    width: 100%;
  }
}
</style>