<script setup>
import {
  computed,
  onMounted,
  reactive,
  ref,
  watch
} from 'vue'

import AppMessageToast
  from '@/components/common/AppMessageToast.vue'

import {
  extractProductData,
  extractProductErrorMessage,
  getHeadProducts
  } from '@/api/head/headProductApi'

import {
  createHeadProductOption,
  deleteHeadProductOption,
  extractProductOptionData,
  extractProductOptionErrorMessage,
  getHeadProductOptions,
  updateHeadProductOption
} from '@/api/head/headProductOptionApi'

const products = ref([])
const options = ref([])

const selectedProductId = ref('')
const searchKeyword = ref('')

const loadingProducts = ref(false)
const loadingOptions = ref(false)
const saving = ref(false)
const deletingId = ref(null)

const message = ref('')
const messageType = ref('success')

const formModal = reactive({
  open: false,
  mode: 'create',
  optionId: null
})

const form = reactive({
  optionType: '',
  optionName: '',
  extraPrice: 0,
  maxFlavorCount: null
})

const selectedProduct = computed(() => {
  return products.value.find(
    (product) =>
      String(product.productId) ===
      String(selectedProductId.value)
  ) ?? null
})

const filteredOptions = computed(() => {
  const keyword =
    searchKeyword.value
      .trim()
      .toLowerCase()

  if (!keyword) {
    return options.value
  }

  return options.value.filter((option) => {
    return [
      option.optionType,
      option.optionName,
      option.extraPrice,
      option.maxFlavorCount
    ].some((value) =>
      String(value ?? '')
        .toLowerCase()
        .includes(keyword)
    )
  })
})

const normalizeProduct = (product = {}) => {
  return {
    productId:
      product.productId ??
      product.id,

    productName:
      product.productName ??
      product.name ??
      '상품명 없음'
  }
}

const normalizeOption = (option = {}) => {
  return {
    optionId:
      option.optionId ??
      option.id,

    optionType:
      option.optionType ??
      '',

    optionName:
      option.optionName ??
      '',

    extraPrice:
      Number(
        option.extraPrice ?? 0
      ),

    maxFlavorCount:
      option.maxFlavorCount ??
      null
  }
}

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
 * 상품 목록 조회
 */
const loadProducts = async () => {
  loadingProducts.value = true
  clearMessage()

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

    if (
      !selectedProductId.value &&
      products.value.length > 0
    ) {
      selectedProductId.value =
        products.value[0].productId
    }

  } catch (error) {
    showMessage(
      extractProductErrorMessage(
        error,
        '상품 목록을 불러오지 못했습니다.'
      ),
      'error'
    )

  } finally {
    loadingProducts.value = false
  }
}

/*
 * 선택한 상품의 옵션 조회
 */
const loadOptions = async () => {
  if (!selectedProductId.value) {
    options.value = []
    return
  }

  loadingOptions.value = true

  try {
    const responseBody =
      await getHeadProductOptions(
        selectedProductId.value
      )

    const responseData =
      extractProductOptionData(
        responseBody
      )

    const optionList =
      Array.isArray(responseData)
        ? responseData
        : []

    options.value =
      optionList.map(normalizeOption)

  } catch (error) {
    options.value = []

    showMessage(
      extractProductOptionErrorMessage(
        error,
        '상품 옵션을 불러오지 못했습니다.'
      ),
      'error'
    )

  } finally {
    loadingOptions.value = false
  }
}

const resetForm = () => {
  form.optionType = ''
  form.optionName = ''
  form.extraPrice = 0
  form.maxFlavorCount = null
}

const openCreateModal = () => {
  if (!selectedProductId.value) {
    showMessage(
      '옵션을 등록할 상품을 먼저 선택해주세요.',
      'error'
    )

    return
  }

  resetForm()

  formModal.mode = 'create'
  formModal.optionId = null
  formModal.open = true
}

const openEditModal = (option) => {
  form.optionType =
    option.optionType

  form.optionName =
    option.optionName

  form.extraPrice =
    option.extraPrice

  form.maxFlavorCount =
    option.maxFlavorCount

  formModal.mode = 'edit'
  formModal.optionId =
    option.optionId

  formModal.open = true
}

const closeFormModal = () => {
  if (saving.value) {
    return
  }

  formModal.open = false
}

const validateForm = () => {
  if (!form.optionType.trim()) {
    showMessage(
      '옵션 유형을 입력해주세요.',
      'error'
    )

    return false
  }

  if (!form.optionName.trim()) {
    showMessage(
      '옵션명을 입력해주세요.',
      'error'
    )

    return false
  }

  if (
    Number.isNaN(
      Number(form.extraPrice)
    ) ||
    Number(form.extraPrice) < 0
  ) {
    showMessage(
      '추가 금액은 0 이상의 숫자로 입력해주세요.',
      'error'
    )

    return false
  }

  if (
    form.maxFlavorCount !== null &&
    form.maxFlavorCount !== '' &&
    (
      !Number.isInteger(
        Number(form.maxFlavorCount)
      ) ||
      Number(form.maxFlavorCount) < 0
    )
  ) {
    showMessage(
      '최대 맛 개수는 0 이상의 정수로 입력해주세요.',
      'error'
    )

    return false
  }

  return true
}

const createPayload = () => {
  return {
    optionType:
      form.optionType
        .trim()
        .toUpperCase(),

    optionName:
      form.optionName.trim(),

    extraPrice:
      Number(form.extraPrice),

    maxFlavorCount:
      form.maxFlavorCount === '' ||
      form.maxFlavorCount === null
        ? null
        : Number(form.maxFlavorCount)
  }
}

const submitOption = async () => {
  if (!validateForm()) {
    return
  }

  saving.value = true
  clearMessage()

  try {
    const payload =
      createPayload()

    if (formModal.mode === 'create') {
      await createHeadProductOption(
        selectedProductId.value,
        payload
      )

      showMessage(
        '상품 옵션이 등록되었습니다.',
        'success'
      )

    } else {
      await updateHeadProductOption(
        selectedProductId.value,
        formModal.optionId,
        payload
      )

      showMessage(
        '상품 옵션이 수정되었습니다.',
        'success'
      )
    }

    formModal.open = false

    await loadOptions()

  } catch (error) {
    showMessage(
      extractProductOptionErrorMessage(
        error,
        formModal.mode === 'create'
          ? '상품 옵션 등록에 실패했습니다.'
          : '상품 옵션 수정에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    saving.value = false
  }
}

const removeOption = async (option) => {
  const confirmed = window.confirm(
    `"${option.optionName}" 옵션을 삭제하시겠습니까?`
  )

  if (!confirmed) {
    return
  }

  deletingId.value =
    option.optionId

  clearMessage()

  try {
    await deleteHeadProductOption(
      selectedProductId.value,
      option.optionId
    )

    showMessage(
      '상품 옵션이 삭제되었습니다.',
      'success'
    )

    await loadOptions()

  } catch (error) {
    showMessage(
      extractProductOptionErrorMessage(
        error,
        '상품 옵션 삭제에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    deletingId.value = null
  }
}

watch(
  selectedProductId,
  () => {
    searchKeyword.value = ''
    loadOptions()
  }
)

onMounted(async () => {
  await loadProducts()
})
</script>

<template>
  <AppMessageToast
  :message="message"
  :type="messageType"
  @close="clearMessage"
  />
  <section class="option-page">
    <section class="product-selector-card">
      <div>
        <p class="section-label">
          PRODUCT OPTION
        </p>

        <h2>
          옵션을 관리할 상품 선택
        </h2>

        <p class="section-description">
          상품을 선택하면 해당 상품에 등록된
          옵션 목록을 조회합니다.
        </p>
      </div>

      <select
        v-model="selectedProductId"
        :disabled="loadingProducts"
      >
        <option value="">
          상품을 선택해주세요
        </option>

        <option
          v-for="product in products"
          :key="product.productId"
          :value="product.productId"
        >
          #{{ product.productId }}
          {{ product.productName }}
        </option>
      </select>
    </section>

    <section class="option-panel">
      <div class="panel-header">
        <div>
          <h2>
            {{
              selectedProduct
                ? `${selectedProduct.productName} 옵션`
                : '상품 옵션 목록'
            }}
          </h2>

          <p>
            옵션 유형, 옵션명, 추가 금액과
            최대 맛 선택 개수를 관리합니다.
          </p>
        </div>

        <div class="panel-actions">
          <input
            v-model="searchKeyword"
            type="search"
            placeholder="옵션 검색"
          />

          <button
            type="button"
            class="create-button"
            :disabled="!selectedProductId"
            @click="openCreateModal"
          >
            ＋ 옵션 등록
          </button>
        </div>
      </div>

      <div
        v-if="loadingOptions"
        class="loading-area"
      >
        <span class="spinner" />

        <p>
          상품 옵션을 불러오는 중입니다.
        </p>
      </div>

      <div
        v-else
        class="table-scroll"
      >
        <table>
          <thead>
            <tr>
              <th>옵션 ID</th>
              <th>옵션 유형</th>
              <th>옵션명</th>
              <th>추가 금액</th>
              <th>최대 맛 개수</th>
              <th>관리</th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="option in filteredOptions"
              :key="option.optionId"
            >
              <td class="id-cell">
                #{{ option.optionId }}
              </td>

              <td>
                <span class="type-badge">
                  {{ option.optionType }}
                </span>
              </td>

              <td>
                <strong>
                  {{ option.optionName }}
                </strong>
              </td>

              <td class="price-cell">
                {{ formatPrice(option.extraPrice) }}
              </td>

              <td>
                {{
                  option.maxFlavorCount ??
                  '해당 없음'
                }}
              </td>

              <td>
                <div class="management-buttons">
                  <button
                    type="button"
                    class="edit-button"
                    @click="openEditModal(option)"
                  >
                    수정
                  </button>

                  <button
                    type="button"
                    class="delete-button"
                    :disabled="
                      deletingId === option.optionId
                    "
                    @click="removeOption(option)"
                  >
                    {{
                      deletingId === option.optionId
                        ? '삭제 중'
                        : '삭제'
                    }}
                  </button>
                </div>
              </td>
            </tr>

            <tr
              v-if="
                !selectedProductId ||
                filteredOptions.length === 0
              "
            >
              <td
                colspan="6"
                class="empty-cell"
              >
                <strong>
                  {{
                    selectedProductId
                      ? '등록된 상품 옵션이 없습니다.'
                      : '상품을 먼저 선택해주세요.'
                  }}
                </strong>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="panel-footer">
        전체 {{ options.length }}개 옵션
      </div>
    </section>

    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="formModal.open"
          class="modal-backdrop"
          @click.self="closeFormModal"
        >
          <form
            class="option-modal"
            @submit.prevent="submitOption"
          >
            <div class="modal-header">
              <div>
                <p>PRODUCT OPTION</p>

                <h2>
                  {{
                    formModal.mode === 'create'
                      ? '상품 옵션 등록'
                      : '상품 옵션 수정'
                  }}
                </h2>
              </div>

              <button
                type="button"
                :disabled="saving"
                @click="closeFormModal"
              >
                ×
              </button>
            </div>

            <div class="modal-body">
              <label>
                <span>옵션 유형 *</span>

                <select
                  v-model="form.optionType"
                  :disabled="saving"
                >
                  <option value="">
                    옵션 유형을 선택하세요
                  </option>

                  <option value="CONTAINER">
                    CONTAINER
                  </option>

                  <option value="SIZE">
                    SIZE
                  </option>

                  <option value="TOPPING">
                    TOPPING
                  </option>

                  <option value="SPOON">
                    SPOON
                  </option>

                  <option value="ETC">
                    ETC
                  </option>
                </select>

                <small>
                  백엔드 OptionType enum 값과 동일하게 입력합니다.
                </small>
              </label>

              <label>
                <span>옵션명 *</span>

                <input
                  v-model="form.optionName"
                  type="text"
                  maxlength="100"
                  placeholder="예: 싱글 레귤러"
                  :disabled="saving"
                />
              </label>

              <label>
                <span>추가 금액 *</span>

                <input
                  v-model.number="form.extraPrice"
                  type="number"
                  min="0"
                  step="100"
                  :disabled="saving"
                />
              </label>

              <label>
                <span>최대 맛 개수</span>

                <input
                  v-model.number="form.maxFlavorCount"
                  type="number"
                  min="0"
                  placeholder="해당 없으면 비워두세요"
                  :disabled="saving"
                />
              </label>
            </div>

            <div class="modal-footer">
              <button
                type="button"
                class="cancel-button"
                :disabled="saving"
                @click="closeFormModal"
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
                    : formModal.mode === 'create'
                      ? '옵션 등록'
                      : '옵션 수정'
                }}
              </button>
            </div>
          </form>
        </div>
      </Transition>
    </Teleport>
  </section>
</template>

<style scoped>
.option-page {
  display: grid;
  gap: 18px;
}

.product-selector-card {
  display: flex;
  gap: 24px;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border: 1px solid #e2e6ed;
  border-radius: 15px;
  background: #ffffff;
}

.section-label {
  margin: 0 0 5px;
  color: #715ee6;
  font-size: 9px;
  font-weight: 900;
  letter-spacing: 1.3px;
}

.product-selector-card h2 {
  margin: 0;
  color: #2b303d;
  font-size: 17px;
}

.section-description {
  margin: 6px 0 0;
  color: #9299a8;
  font-size: 10px;
}

.product-selector-card select {
  width: 330px;
  height: 42px;
  padding: 0 12px;
  border: 1px solid #dfe3ea;
  border-radius: 9px;
  outline: none;
  background: #ffffff;
}

.option-panel {
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
}

.panel-actions input {
  height: 36px;
  padding: 0 11px;
  border: 1px solid #dfe3ea;
  border-radius: 9px;
  outline: none;
  font-size: 10px;
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

.create-button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.table-scroll {
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 760px;
  border-collapse: collapse;
}

th,
td {
  padding: 13px 16px;
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

.id-cell {
  font-weight: 800;
}

.type-badge {
  display: inline-flex;
  padding: 5px 8px;
  border-radius: 7px;
  color: #6654df;
  font-size: 9px;
  font-weight: 800;
  background: #edeaff;
}

.price-cell {
  font-weight: 750;
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

.empty-cell {
  height: 160px;
  text-align: center;
  color: #9097a6;
}

.panel-footer {
  padding: 12px 16px;
  border-top: 1px solid #e8ebf0;
  color: #8c94a3;
  font-size: 9px;
}

.loading-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  justify-content: center;
  min-height: 220px;
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

.option-modal {
  width: 100%;
  max-width: 480px;
  border: 1px solid #e3e6ed;
  border-radius: 18px;
  background: #ffffff;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  padding: 21px 22px 17px;
  border-bottom: 1px solid #ebedf1;
}

.modal-header p {
  margin: 0 0 5px;
  color: #725fe5;
  font-size: 9px;
  font-weight: 900;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
}

.modal-header button {
  border: 0;
  cursor: pointer;
  color: #858c9a;
  font-size: 25px;
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

.modal-body label > span {
  color: #464c5a;
  font-size: 11px;
  font-weight: 750;
}

.modal-body input {
  width: 100%;
  height: 43px;
  padding: 0 12px;
  border: 1px solid #dfe3ea;
  border-radius: 9px;
  outline: none;
}

.modal-body input:focus {
  border-color: #7461e7;
  box-shadow:
    0 0 0 3px rgba(116, 97, 231, 0.1);
}

.modal-body small {
  color: #969dab;
  font-size: 9px;
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

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 700px) {
  .product-selector-card,
  .panel-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .product-selector-card select,
  .panel-actions,
  .panel-actions input {
    width: 100%;
  }
}
</style>