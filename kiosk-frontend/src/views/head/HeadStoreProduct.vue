<script setup>
import {
  computed,
  onMounted,
  reactive,
  ref,
  watch
} from 'vue'

import {
  extractStoreData,
  getHeadStores
} from '@/api/head/headStoreApi'

import {
  extractProductData,
  getHeadProducts
} from '@/api/head/headProductApi'

import {
  createHeadStoreProduct,
  deleteHeadStoreProduct,
  extractStoreProductData,
  extractStoreProductErrorMessage,
  getHeadStoreProducts,
  updateHeadStoreProduct
} from '@/api/head/headStoreProductApi'

const stores = ref([])
const headquartersProducts = ref([])
const storeProducts = ref([])

const selectedStoreId = ref('')
const searchKeyword = ref('')
const soldOutFilter = ref('ALL')

const loading = ref(false)
const saving = ref(false)
const deletingId = ref(null)
const updatingId = ref(null)

const message = ref('')
const messageType = ref('success')

const modal = reactive({
  open: false,
  mode: 'create',
  storeProductId: null
})

const form = reactive({
  productId: '',
  storeProductPrice: 0,
  isSoldOut: false
})

const normalizeStore = (store = {}) => ({
  storeId:
    store.storeId ??
    store.id,

  storeName:
    store.storeName ??
    store.name ??
    '지점명 없음'
})

const normalizeProduct = (product = {}) => ({
  productId:
    product.productId ??
    product.id,

  productName:
    product.productName ??
    product.name ??
    '상품명 없음',

  basePrice:
    Number(
      product.basePrice ??
      product.price ??
      0
    )
})

const normalizeStoreProduct = (
  product = {}
) => ({
  storeProductId:
    product.storeProductId ??
    product.id,

  storeId:
    product.storeId,

  productId:
    product.productId,

  productName:
    product.productName ??
    '상품명 없음',

  basePrice:
    Number(product.basePrice ?? 0),

  storeProductPrice:
    Number(
      product.storeProductPrice ??
      product.basePrice ??
      0
    ),

  isSoldOut:
    Boolean(product.isSoldOut)
})

const selectedStore = computed(() => {
  return stores.value.find(
    (store) =>
      String(store.storeId) ===
      String(selectedStoreId.value)
  ) ?? null
})

/*
 * 아직 해당 지점에 등록하지 않은
 * 본사 상품만 추가 목록에 표시
 */
const availableProducts = computed(() => {
  const registeredIds =
    new Set(
      storeProducts.value.map(
        (product) =>
          String(product.productId)
      )
    )

  return headquartersProducts.value.filter(
    (product) =>
      !registeredIds.has(
        String(product.productId)
      )
  )
})

const filteredStoreProducts = computed(() => {
  const keyword =
    searchKeyword.value
      .trim()
      .toLowerCase()

  return storeProducts.value.filter(
    (product) => {
      const matchesKeyword =
        !keyword ||
        product.productName
          .toLowerCase()
          .includes(keyword) ||
        String(product.productId)
          .includes(keyword)

      const matchesSoldOut =
        soldOutFilter.value === 'ALL' ||
        (
          soldOutFilter.value === 'SOLD_OUT' &&
          product.isSoldOut
        ) ||
        (
          soldOutFilter.value === 'SELLING' &&
          !product.isSoldOut
        )

      return (
        matchesKeyword &&
        matchesSoldOut
      )
    }
  )
})

const soldOutCount = computed(() => {
  return storeProducts.value.filter(
    (product) => product.isSoldOut
  ).length
})

const sellingCount = computed(() => {
  return storeProducts.value.filter(
    (product) => !product.isSoldOut
  ).length
})

const formatPrice = (price) => {
  return `${Number(price ?? 0)
    .toLocaleString('ko-KR')}원`
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
 * 지점 및 본사 상품 조회
 */
const loadInitialData = async () => {
  try {
    const [
      storeResponse,
      productResponse
    ] = await Promise.all([
      getHeadStores(),
      getHeadProducts()
    ])

    const storeData =
      extractStoreData(storeResponse)

    const productData =
      extractProductData(productResponse)

    stores.value =
      Array.isArray(storeData)
        ? storeData.map(normalizeStore)
        : []

    headquartersProducts.value =
      Array.isArray(productData)
        ? productData.map(normalizeProduct)
        : []

    if (
      !selectedStoreId.value &&
      stores.value.length > 0
    ) {
      selectedStoreId.value =
        stores.value[0].storeId
    }

  } catch (error) {
    showMessage(
      extractStoreProductErrorMessage(
        error,
        '기본 정보를 불러오지 못했습니다.'
      ),
      'error'
    )
  }
}

/*
 * 선택한 지점의 판매 상품 조회
 */
const loadStoreProducts = async () => {
  if (!selectedStoreId.value) {
    storeProducts.value = []
    return
  }

  loading.value = true
  clearMessage()

  try {
    const responseBody =
      await getHeadStoreProducts(
        selectedStoreId.value
      )

    const responseData =
      extractStoreProductData(
        responseBody
      )

    storeProducts.value =
      Array.isArray(responseData)
        ? responseData.map(
            normalizeStoreProduct
          )
        : []

  } catch (error) {
    storeProducts.value = []

    showMessage(
      extractStoreProductErrorMessage(
        error,
        '지점 판매 메뉴를 불러오지 못했습니다.'
      ),
      'error'
    )

  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.productId = ''
  form.storeProductPrice = 0
  form.isSoldOut = false
}

const openCreateModal = () => {
  if (!selectedStoreId.value) {
    showMessage(
      '지점을 먼저 선택해주세요.',
      'error'
    )

    return
  }

  resetForm()

  modal.mode = 'create'
  modal.storeProductId = null
  modal.open = true
}

const openEditModal = (product) => {
  form.productId =
    product.productId

  form.storeProductPrice =
    product.storeProductPrice

  form.isSoldOut =
    product.isSoldOut

  modal.mode = 'edit'
  modal.storeProductId =
    product.storeProductId

  modal.open = true
}

const closeModal = () => {
  if (saving.value) {
    return
  }

  modal.open = false
}

/*
 * 등록 상품 선택 시
 * 본사 가격을 자동 입력
 */
const handleProductChange = () => {
  const product =
    headquartersProducts.value.find(
      (item) =>
        String(item.productId) ===
        String(form.productId)
    )

  if (product) {
    form.storeProductPrice =
      product.basePrice
  }
}

const validateForm = () => {
  if (
    modal.mode === 'create' &&
    !form.productId
  ) {
    showMessage(
      '추가할 상품을 선택해주세요.',
      'error'
    )

    return false
  }

  if (
    Number.isNaN(
      Number(form.storeProductPrice)
    ) ||
    Number(form.storeProductPrice) < 0
  ) {
    showMessage(
      '지점 판매 가격은 0원 이상이어야 합니다.',
      'error'
    )

    return false
  }

  return true
}

/*
 * 등록 또는 수정
 */
const submitStoreProduct = async () => {
  if (!validateForm()) {
    return
  }

  saving.value = true
  clearMessage()

  try {
    if (modal.mode === 'create') {
      await createHeadStoreProduct(
        selectedStoreId.value,
        {
          productId:
            Number(form.productId),

          storeProductPrice:
            Number(
              form.storeProductPrice
            ),

          isSoldOut:
            Boolean(form.isSoldOut)
        }
      )

      showMessage(
        '지점 판매 메뉴가 등록되었습니다.'
      )

    } else {
      await updateHeadStoreProduct(
        selectedStoreId.value,
        modal.storeProductId,
        {
          storeProductPrice:
            Number(
              form.storeProductPrice
            ),

          isSoldOut:
            Boolean(form.isSoldOut)
        }
      )

      showMessage(
        '지점 판매 메뉴가 수정되었습니다.'
      )
    }

    modal.open = false

    await loadStoreProducts()

  } catch (error) {
    showMessage(
      extractStoreProductErrorMessage(
        error,
        modal.mode === 'create'
          ? '지점 판매 메뉴 등록에 실패했습니다.'
          : '지점 판매 메뉴 수정에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    saving.value = false
  }
}

/*
 * 빠른 품절 상태 변경
 */
const toggleSoldOut = async (product) => {
  updatingId.value =
    product.storeProductId

  clearMessage()

  try {
    await updateHeadStoreProduct(
      selectedStoreId.value,
      product.storeProductId,
      {
        storeProductPrice:
          product.storeProductPrice,

        isSoldOut:
          !product.isSoldOut
      }
    )

    product.isSoldOut =
      !product.isSoldOut

    showMessage(
      product.isSoldOut
        ? '상품이 품절 처리되었습니다.'
        : '상품 판매가 재개되었습니다.'
    )

  } catch (error) {
    showMessage(
      extractStoreProductErrorMessage(
        error,
        '품절 상태 변경에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    updatingId.value = null
  }
}

/*
 * 지점 판매 메뉴 삭제
 */
const removeStoreProduct = async (
  product
) => {
  const confirmed = window.confirm(
    `"${product.productName}" 상품을 해당 지점 판매 메뉴에서 삭제하시겠습니까?`
  )

  if (!confirmed) {
    return
  }

  deletingId.value =
    product.storeProductId

  clearMessage()

  try {
    await deleteHeadStoreProduct(
      selectedStoreId.value,
      product.storeProductId
    )

    showMessage(
      '지점 판매 메뉴에서 삭제되었습니다.'
    )

    await loadStoreProducts()

  } catch (error) {
    showMessage(
      extractStoreProductErrorMessage(
        error,
        '지점 판매 메뉴 삭제에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    deletingId.value = null
  }
}

watch(
  selectedStoreId,
  () => {
    searchKeyword.value = ''
    soldOutFilter.value = 'ALL'
    loadStoreProducts()
  }
)

onMounted(async () => {
  await loadInitialData()

  if (selectedStoreId.value) {
    await loadStoreProducts()
  }
})
</script>

<template>
  <section class="store-product-page">
    <div
      v-if="message"
      class="page-message"
      :class="{ error: messageType === 'error' }"
    >
      <strong>
        {{ messageType === 'error' ? '!' : '✓' }}
      </strong>

      <p>{{ message }}</p>

      <button
        type="button"
        @click="clearMessage"
      >
        ×
      </button>
    </div>

    <section class="store-selector">
      <div>
        <p class="section-label">
          STORE PRODUCT
        </p>

        <h2>지점 판매 메뉴 관리</h2>

        <p>
          지점을 선택하여 판매 상품과 품절 상태 및 지점 가격을 관리합니다.
        </p>
      </div>

      <select v-model="selectedStoreId">
        <option value="">
          지점을 선택해주세요
        </option>

        <option
          v-for="store in stores"
          :key="store.storeId"
          :value="store.storeId"
        >
          #{{ store.storeId }}
          {{ store.storeName }}
        </option>
      </select>
    </section>

    <div class="summary-grid">
      <article>
        <p>등록 메뉴</p>
        <strong>{{ storeProducts.length }}</strong>
        <small>
          {{ selectedStore?.storeName || '지점 미선택' }}
        </small>
      </article>

      <article>
        <p>판매 중</p>
        <strong>{{ sellingCount }}</strong>
        <small>현재 주문 가능</small>
      </article>

      <article>
        <p>품절</p>
        <strong>{{ soldOutCount }}</strong>
        <small>현재 주문 불가</small>
      </article>

      <article>
        <p>추가 가능 상품</p>
        <strong>{{ availableProducts.length }}</strong>
        <small>본사 등록 상품</small>
      </article>
    </div>

    <section class="product-panel">
      <header class="panel-header">
        <div>
          <h2>
            {{
              selectedStore
                ? `${selectedStore.storeName} 판매 메뉴`
                : '지점 판매 메뉴'
            }}
          </h2>

          <p>
            지점 가격과 품절 여부를 관리합니다.
          </p>
        </div>

        <div class="panel-actions">
          <input
            v-model="searchKeyword"
            type="search"
            placeholder="상품명 또는 ID 검색"
          />

          <select v-model="soldOutFilter">
            <option value="ALL">
              전체 상태
            </option>

            <option value="SELLING">
              판매 중
            </option>

            <option value="SOLD_OUT">
              품절
            </option>
          </select>

          <button
            type="button"
            class="create-button"
            :disabled="!selectedStoreId"
            @click="openCreateModal"
          >
            ＋ 판매 메뉴 추가
          </button>
        </div>
      </header>

      <div
        v-if="loading"
        class="loading-area"
      >
        판매 메뉴를 불러오는 중입니다.
      </div>

      <div v-else class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>상품</th>
              <th>본사 가격</th>
              <th>지점 가격</th>
              <th>가격 차이</th>
              <th>판매 상태</th>
              <th>관리</th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="product in filteredStoreProducts"
              :key="product.storeProductId"
            >
              <td>
                <strong>
                  {{ product.productName }}
                </strong>

                <small>
                  상품 #{{ product.productId }}
                  · 지점 상품 #{{ product.storeProductId }}
                </small>
              </td>

              <td>
                {{ formatPrice(product.basePrice) }}
              </td>

              <td class="store-price">
                {{
                  formatPrice(
                    product.storeProductPrice
                  )
                }}
              </td>

              <td>
                {{
                  formatPrice(
                    product.storeProductPrice -
                    product.basePrice
                  )
                }}
              </td>

              <td>
                <button
                  type="button"
                  class="sold-out-toggle"
                  :class="{
                    soldOut: product.isSoldOut
                  }"
                  :disabled="
                    updatingId ===
                    product.storeProductId
                  "
                  @click="toggleSoldOut(product)"
                >
                  {{
                    updatingId ===
                      product.storeProductId
                      ? '변경 중'
                      : product.isSoldOut
                        ? '품절'
                        : '판매 중'
                  }}
                </button>
              </td>

              <td>
                <div class="management-buttons">
                  <button
                    type="button"
                    class="edit-button"
                    @click="openEditModal(product)"
                  >
                    수정
                  </button>

                  <button
                    type="button"
                    class="delete-button"
                    :disabled="
                      deletingId ===
                      product.storeProductId
                    "
                    @click="
                      removeStoreProduct(product)
                    "
                  >
                    {{
                      deletingId ===
                        product.storeProductId
                        ? '삭제 중'
                        : '삭제'
                    }}
                  </button>
                </div>
              </td>
            </tr>

            <tr
              v-if="
                filteredStoreProducts.length === 0
              "
            >
              <td
                colspan="6"
                class="empty-cell"
              >
                {{
                  selectedStoreId
                    ? '등록된 판매 메뉴가 없습니다.'
                    : '지점을 먼저 선택해주세요.'
                }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <Teleport to="body">
      <div
        v-if="modal.open"
        class="modal-backdrop"
        @click.self="closeModal"
      >
        <form
          class="form-modal"
          @submit.prevent="submitStoreProduct"
        >
          <header>
            <div>
              <p>STORE PRODUCT</p>

              <h2>
                {{
                  modal.mode === 'create'
                    ? '판매 메뉴 추가'
                    : '판매 메뉴 수정'
                }}
              </h2>
            </div>

            <button
              type="button"
              @click="closeModal"
            >
              ×
            </button>
          </header>

          <div class="modal-body">
            <label v-if="modal.mode === 'create'">
              <span>본사 상품 *</span>

              <select
                v-model="form.productId"
                @change="handleProductChange"
              >
                <option value="">
                  상품을 선택해주세요
                </option>

                <option
                  v-for="product in availableProducts"
                  :key="product.productId"
                  :value="product.productId"
                >
                  {{ product.productName }}
                  · {{ formatPrice(product.basePrice) }}
                </option>
              </select>
            </label>

            <label>
              <span>지점 판매 가격 *</span>

              <input
                v-model.number="
                  form.storeProductPrice
                "
                type="number"
                min="0"
                step="100"
              />
            </label>

            <label class="check-field">
              <input
                v-model="form.isSoldOut"
                type="checkbox"
              />

              <span>등록 즉시 품절 처리</span>
            </label>
          </div>

          <footer>
            <button
              type="button"
              class="cancel-button"
              @click="closeModal"
            >
              취소
            </button>

            <button
              type="submit"
              class="save-button"
              :disabled="saving"
            >
              {{
                saving
                  ? '저장 중...'
                  : modal.mode === 'create'
                    ? '판매 메뉴 추가'
                    : '수정 저장'
              }}
            </button>
          </footer>
        </form>
      </div>
    </Teleport>
  </section>
</template>

<style scoped>
.store-product-page {
  display: grid;
  gap: 18px;
}

.page-message {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 13px 15px;
  border: 1px solid #bcebd6;
  border-radius: 11px;
  color: #168a5e;
  background: #edfbf5;
}

.page-message.error {
  border-color: #ffd0d7;
  color: #d64359;
  background: #fff2f4;
}

.page-message p {
  flex: 1;
  margin: 0;
}

.page-message button {
  border: 0;
  cursor: pointer;
  background: transparent;
}

.store-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border: 1px solid #e2e6ed;
  border-radius: 15px;
  background: white;
}

.section-label {
  margin: 0;
  color: #725ee7;
  font-size: 9px;
  font-weight: 900;
}

.store-selector h2 {
  margin: 4px 0;
}

.store-selector p {
  color: #9299a8;
  font-size: 10px;
}

.store-selector select {
  width: 330px;
  height: 42px;
  padding: 0 12px;
  border: 1px solid #dfe3ea;
  border-radius: 9px;
}

.summary-grid {
  display: grid;
  grid-template-columns:
    repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-grid article {
  min-height: 115px;
  padding: 18px;
  border: 1px solid #e3e6ed;
  border-radius: 15px;
  background: white;
}

.summary-grid p {
  margin: 0;
  color: #798191;
  font-size: 11px;
}

.summary-grid strong {
  display: block;
  margin: 12px 0 5px;
  font-size: 25px;
}

.summary-grid small {
  color: #969dab;
}

.product-panel {
  overflow: hidden;
  border: 1px solid #e2e6ed;
  border-radius: 15px;
  background: white;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px;
  border-bottom: 1px solid #e8ebf0;
}

.panel-header h2 {
  margin: 0;
  font-size: 15px;
}

.panel-header p {
  color: #9299a8;
  font-size: 10px;
}

.panel-actions {
  display: flex;
  gap: 8px;
}

.panel-actions input,
.panel-actions select {
  height: 36px;
  padding: 0 11px;
  border: 1px solid #dfe3ea;
  border-radius: 9px;
}

.create-button {
  height: 36px;
  padding: 0 14px;
  border: 0;
  border-radius: 9px;
  cursor: pointer;
  color: white;
  background: #725ee7;
}

.table-scroll {
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 900px;
  border-collapse: collapse;
}

th,
td {
  padding: 13px 15px;
  text-align: left;
  font-size: 10px;
}

th {
  color: #7e8696;
  background: #fafbfc;
}

td {
  border-top: 1px solid #edf0f4;
}

td strong,
td small {
  display: block;
}

td small {
  margin-top: 4px;
  color: #9aa1af;
}

.store-price {
  color: #d84774;
  font-weight: 800;
}

.sold-out-toggle {
  min-width: 66px;
  height: 29px;
  border: 0;
  border-radius: 20px;
  cursor: pointer;
  color: #168e61;
  background: #dff8ec;
}

.sold-out-toggle.soldOut {
  color: #d64359;
  background: #fff0f2;
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
}

.edit-button {
  border: 1px solid #dcd7fb;
  color: #6756dc;
  background: #f3f1ff;
}

.delete-button {
  border: 1px solid #ffd5dc;
  color: #db485e;
  background: #fff2f4;
}

.empty-cell,
.loading-area {
  height: 170px;
  text-align: center;
  color: #9097a6;
}

.modal-backdrop {
  position: fixed;
  z-index: 3000;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(25, 29, 42, 0.52);
}

.form-modal {
  width: 100%;
  max-width: 480px;
  border-radius: 18px;
  background: white;
}

.form-modal header {
  display: flex;
  justify-content: space-between;
  padding: 21px 22px;
  border-bottom: 1px solid #ebedf1;
}

.form-modal header p {
  margin: 0;
  color: #725ee7;
  font-size: 9px;
  font-weight: 900;
}

.form-modal header h2 {
  margin: 4px 0 0;
}

.form-modal header button {
  border: 0;
  cursor: pointer;
  font-size: 24px;
  background: transparent;
}

.modal-body {
  display: grid;
  gap: 17px;
  padding: 22px;
}

.modal-body label {
  display: grid;
  gap: 8px;
}

.modal-body input,
.modal-body select {
  width: 100%;
  height: 43px;
  padding: 0 12px;
  border: 1px solid #dfe3ea;
  border-radius: 9px;
}

.check-field {
  display: flex !important;
  align-items: center;
}

.check-field input {
  width: 17px;
  height: 17px;
}

.form-modal footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding: 15px 22px 20px;
}

.form-modal footer button {
  height: 39px;
  padding: 0 16px;
  border-radius: 9px;
  cursor: pointer;
}

.cancel-button {
  border: 1px solid #dfe3e9;
  background: white;
}

.save-button {
  border: 0;
  color: white;
  background: #725ee7;
}

@media (max-width: 900px) {
  .summary-grid {
    grid-template-columns:
      repeat(2, minmax(0, 1fr));
  }

  .store-selector,
  .panel-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 14px;
  }

  .store-selector select,
  .panel-actions {
    width: 100%;
  }
}
</style>