<!--
  [화면 흐름 안내] HeadCategory
  역할: 본사 관리에서 사용자가 보는 화면이다.
  진입: /head/categories -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/head/headCategoryApi, @/api/head/headDashboardApi -> 응답/상태 반영
  다음 이동: /head/events
-->
<script setup>
import {
  computed,
  onMounted,
  reactive,
  ref
} from 'vue'
import { useRouter } from 'vue-router'

import {
  createHeadCategory,
  deleteHeadCategory,
  extractCategoryErrorMessage,
  getHeadCategories,
  updateHeadCategory
} from '@/api/head/headCategoryApi'

import { getHeadDashboardSummary } from '@/api/head/headDashboardApi'

import P2ComingSoonModal from '@/components/head/P2ComingSoonModal.vue'

import AppMessageToast
  from '@/components/common/AppMessageToast.vue'

/*
 * 서버 데이터
 */
const categories = ref([])
const eventCount = ref(0)

const router = useRouter()

/*
 * 화면 상태
 */
const loading = ref(false)
const saving = ref(false)
const deletingId = ref(null)

const searchKeyword = ref('')

const message = ref('')
const messageType = ref('success')

/*
 * 등록·수정 모달
 */
const formModal = reactive({
  open: false,
  mode: 'create',
  categoryId: null,
  categoryName: '',
  displayOrder: 0
})

/*
 * P2 안내 모달
 */
const p2Modal = reactive({
  open: false,
  title: '',
  description: ''
})

/*
 * 검색 결과
 */
const filteredCategories = computed(() => {
  const keyword =
    searchKeyword.value
      .trim()
      .toLowerCase()

  if (!keyword) {
    return categories.value
  }

  return categories.value.filter((category) => {
    return (
      String(category.categoryId)
        .includes(keyword) ||
      category.categoryName
        ?.toLowerCase()
        .includes(keyword) ||
      String(category.displayOrder)
        .includes(keyword)
    )
  })
})

/*
 * 전체 카테고리 개수
 */
const totalCategoryCount = computed(() => {
  return categories.value.length
})

/*
 * 가장 빠른 노출 순서
 */
const firstDisplayOrder = computed(() => {
  if (categories.value.length === 0) {
    return '-'
  }

  return Math.min(
    ...categories.value.map(
      (category) =>
        Number(category.displayOrder ?? 0)
    )
  )
})

/*
 * 가장 느린 노출 순서
 */
const lastDisplayOrder = computed(() => {
  if (categories.value.length === 0) {
    return '-'
  }

  return Math.max(
    ...categories.value.map(
      (category) =>
        Number(category.displayOrder ?? 0)
    )
  )
})

/*
 * 카테고리 목록 조회
 */
const loadCategories = async () => {
  loading.value = true
  clearMessage()

  try {
    const result =
      await getHeadCategories()

    categories.value =
      Array.isArray(result)
        ? result
        : []

    try {
      const dashboard = await getHeadDashboardSummary('전주 대비')
      eventCount.value = dashboard.activeEvents || 0
    } catch (e) {
      console.error('이벤트 수 조회 실패', e)
    }

  } catch (error) {
    showMessage(
      extractCategoryErrorMessage(
        error,
        '카테고리 목록을 불러오지 못했습니다.'
      ),
      'error'
    )

  } finally {
    loading.value = false
  }
}

/*
 * 등록 모달 열기
 */
const openCreateModal = () => {
  formModal.open = true
  formModal.mode = 'create'
  formModal.categoryId = null
  formModal.categoryName = ''

  /*
   * 마지막 노출 순서 다음 번호를 기본값으로 사용
   */
  formModal.displayOrder =
    categories.value.length === 0
      ? 1
      : Number(lastDisplayOrder.value) + 1
}

/*
 * 수정 모달 열기
 */
const openEditModal = (category) => {
  formModal.open = true
  formModal.mode = 'edit'
  formModal.categoryId =
    category.categoryId

  formModal.categoryName =
    category.categoryName

  formModal.displayOrder =
    category.displayOrder ?? 0
}

/*
 * 등록·수정 모달 닫기
 */
const closeFormModal = () => {
  if (saving.value) {
    return
  }

  formModal.open = false
}

/*
 * 카테고리 저장
 */
 const submitCategory = async () => {
  const categoryName =
    formModal.categoryName.trim()

  const displayOrder =
    Number(formModal.displayOrder)

  if (!categoryName) {
    showMessage(
      '카테고리명을 입력해주세요.',
      'error'
    )

    return
  }

  if (
    Number.isNaN(displayOrder) ||
    displayOrder < 0
  ) {
    showMessage(
      '노출 순서는 0 이상의 숫자로 입력해주세요.',
      'error'
    )

    return
  }

  saving.value = true
  clearMessage()

  try {
    const payload = {
      categoryName,
      displayOrder
    }

    let successMessage = ''

    if (formModal.mode === 'create') {
      await createHeadCategory(payload)

      successMessage =
        '카테고리가 등록되었습니다.'

    } else {
      await updateHeadCategory(
        formModal.categoryId,
        payload
      )

      successMessage =
        '카테고리가 수정되었습니다.'
    }

    formModal.open = false

    await loadCategories()

    showMessage(
      successMessage,
      'success'
    )

  } catch (error) {
    showMessage(
      extractCategoryErrorMessage(
        error,
        formModal.mode === 'create'
          ? '카테고리 등록에 실패했습니다.'
          : '카테고리 수정에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    saving.value = false
  }
}

/*
 * 카테고리 삭제
 */
const removeCategory = async (category) => {
  const confirmed = window.confirm(
    `"${category.categoryName}" 카테고리를 삭제하시겠습니까?\n\n해당 카테고리를 사용하는 상품이 있으면 삭제할 수 없습니다.`
  )

  if (!confirmed) {
    return
  }

  deletingId.value =
    category.categoryId

  clearMessage()

  try {
    await deleteHeadCategory(
      category.categoryId
    )

  await loadCategories()

    showMessage(
      '카테고리가 삭제되었습니다.',
      'success'
    )

  } catch (error) {
    showMessage(
      extractCategoryErrorMessage(
        error,
        '카테고리 삭제에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    deletingId.value = null
  }
}

/*
 * P2 기능 안내
 */
const openEventManagement = () => {
  router.push('/head/events')
}

const closeP2Modal = () => {
  p2Modal.open = false
}

/*
 * 안내 메시지
 */
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

onMounted(() => {
  loadCategories()
})
</script>

<template>
  <AppMessageToast
    :message="message"
    :type="messageType"
    @close="clearMessage"
  />

  <section class="category-page">
    <!-- 요약 카드 -->
    <div class="category-summary-grid">
      <article class="summary-card">
        <div>
          <p>전체 카테고리</p>

          <strong>
            {{ totalCategoryCount }}
          </strong>

          <small>
            현재 등록된 카테고리
          </small>
        </div>

        <span class="summary-icon purple">
          ◫
        </span>
      </article>

      <article class="summary-card">
        <div>
          <p>첫 노출 순서</p>

          <strong>
            {{ firstDisplayOrder }}
          </strong>

          <small>
            가장 먼저 노출되는 순서
          </small>
        </div>

        <span class="summary-icon green">
          ↑
        </span>
      </article>

      <article class="summary-card">
        <div>
          <p>마지막 노출 순서</p>

          <strong>
            {{ lastDisplayOrder }}
          </strong>

          <small>
            가장 뒤에 노출되는 순서
          </small>
        </div>

        <span class="summary-icon yellow">
          ↓
        </span>
      </article>

      <article class="summary-card">
        <div>
          <p>이벤트 카테고리</p>

          <strong>
            {{ eventCount }}
          </strong>

          <small>
            기간형 이벤트 관리
          </small>
        </div>

        <button
          type="button"
          class="summary-icon pink"
          @click="openEventManagement"
        >
          ★
        </button>
      </article>
    </div>

    <!-- 목록 패널 -->
    <section class="category-panel">
      <div class="panel-header">
        <div>
          <div class="title-line">
            <h2>
              카테고리 목록
            </h2>

            <span class="count-badge">
              {{ totalCategoryCount }}개
            </span>
          </div>

          <p>
            상품 카테고리명과 키오스크 노출 순서를 관리합니다.
          </p>
        </div>

        <div class="panel-actions">
          <div class="search-box">
            <span>⌕</span>

            <input
              v-model="searchKeyword"
              type="search"
              placeholder="카테고리 검색"
            />
          </div>

          <button
            type="button"
            class="event-button"
            @click="openEventManagement"
          >
            ★ 이벤트 관리
          </button>

          <button
            type="button"
            class="create-button"
            @click="openCreateModal"
          >
            ＋ 카테고리 등록
          </button>
        </div>
      </div>

      <!-- 로딩 -->
      <div
        v-if="loading"
        class="loading-area"
      >
        <span class="loading-spinner" />

        <p>
          카테고리를 불러오는 중입니다.
        </p>
      </div>

      <!-- 테이블 -->
      <div
        v-else
        class="table-scroll"
      >
        <table>
          <thead>
            <tr>
              <th>카테고리 ID</th>
              <th>카테고리명</th>
              <th>노출 순서</th>
              <th>관리</th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="category in filteredCategories"
              :key="category.categoryId"
            >
              <td class="id-cell">
                #{{ category.categoryId }}
              </td>

              <td>
                <div class="category-name">
                  <span class="category-icon">
                    ◫
                  </span>

                  <div>
                    <strong>
                      {{ category.categoryName }}
                    </strong>

                    <small>
                      상품 분류 카테고리
                    </small>
                  </div>
                </div>
              </td>

              <td>
                <span class="order-badge">
                  {{ category.displayOrder }}
                </span>
              </td>

              <td>
                <div class="management-buttons">
                  <button
                    type="button"
                    class="edit-button"
                    @click="openEditModal(category)"
                  >
                    수정
                  </button>

                  <button
                    type="button"
                    class="delete-button"
                    :disabled="
                      deletingId === category.categoryId
                    "
                    @click="removeCategory(category)"
                  >
                    {{
                      deletingId === category.categoryId
                        ? '삭제 중'
                        : '삭제'
                    }}
                  </button>
                </div>
              </td>
            </tr>

            <tr
              v-if="filteredCategories.length === 0"
            >
              <td
                colspan="4"
                class="empty-cell"
              >
                <strong>
                  조회된 카테고리가 없습니다.
                </strong>

                <p>
                  검색어를 확인하거나 새로운 카테고리를 등록해주세요.
                </p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="panel-footer">
        <span>
          전체 {{ totalCategoryCount }}개 중
          {{ filteredCategories.length }}개 표시
        </span>

        <button
          type="button"
          @click="loadCategories"
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
            class="category-modal"
            @submit.prevent="submitCategory"
          >
            <div class="modal-header">
              <div>
                <p>
                  CATEGORY MANAGEMENT
                </p>

                <h2>
                  {{
                    formModal.mode === 'create'
                      ? '카테고리 등록'
                      : '카테고리 수정'
                  }}
                </h2>
              </div>

              <button
                type="button"
                aria-label="닫기"
                :disabled="saving"
                @click="closeFormModal"
              >
                ×
              </button>
            </div>

            <div class="modal-body">
              <label>
                <span>
                  카테고리명
                  <strong>*</strong>
                </span>

                <input
                  v-model="formModal.categoryName"
                  type="text"
                  maxlength="50"
                  placeholder="예: 아이스크림"
                  :disabled="saving"
                  autofocus
                />

                <small>
                  최대 50자까지 입력할 수 있습니다.
                </small>
              </label>

              <label>
                <span>
                  노출 순서
                  <strong>*</strong>
                </span>

                <input
                  v-model.number="formModal.displayOrder"
                  type="number"
                  min="0"
                  :disabled="saving"
                />

                <small>
                  숫자가 작을수록 고객 화면에서 먼저 노출됩니다.
                </small>
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
                      ? '등록하기'
                      : '수정하기'
                }}
              </button>
            </div>
          </form>
        </div>
      </Transition>
    </Teleport>

    <P2ComingSoonModal
      :open="p2Modal.open"
      :title="p2Modal.title"
      :description="p2Modal.description"
      @close="closeP2Modal"
    />
  </section>
</template>

<style scoped>
.category-page {
  display: grid;
  gap: 18px;
}

.category-summary-grid {
  display: grid;
  grid-template-columns:
    repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;

  min-height: 132px;
  padding: 18px;

  border: 1px solid #e3e6ed;
  border-radius: 15px;

  background: #ffffff;

  box-shadow:
    0 5px 18px
    rgba(44, 50, 70, 0.04);
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
  letter-spacing: -1px;
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
  padding: 0;

  border: 0;
  border-radius: 10px;

  font-size: 15px;
  font-weight: 900;
}

button.summary-icon {
  cursor: pointer;
}

.purple {
  color: #6654df;
  background: #edeaff;
}

.green {
  color: #159b68;
  background: #dff9ed;
}

.yellow {
  color: #dc8c17;
  background: #fff0cd;
}

.pink {
  color: #e54d8a;
  background: #ffe4f0;
}

.category-panel {
  overflow: hidden;

  border: 1px solid #e2e6ed;
  border-radius: 15px;

  background: #ffffff;

  box-shadow:
    0 5px 18px
    rgba(43, 49, 68, 0.035);
}

.panel-header {
  display: flex;
  gap: 18px;
  align-items: center;
  justify-content: space-between;

  padding: 18px;

  border-bottom: 1px solid #e8ebf0;
}

.title-line {
  display: flex;
  gap: 8px;
  align-items: center;
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

.count-badge {
  padding: 4px 7px;

  border-radius: 7px;

  color: #6858dd;
  font-size: 9px;
  font-weight: 900;

  background: #eeebff;
}

.panel-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-box {
  display: flex;
  align-items: center;

  width: 210px;
  height: 36px;
  padding: 0 11px;

  border: 1px solid #dfe3ea;
  border-radius: 9px;

  color: #9aa1af;
}

.search-box input {
  width: 100%;
  margin-left: 7px;

  border: 0;
  outline: 0;

  color: #414754;
  font-size: 11px;
}

.event-button,
.create-button {
  height: 36px;
  padding: 0 12px;

  border-radius: 9px;
  cursor: pointer;

  font-size: 10px;
  font-weight: 750;
}

.event-button {
  border: 1px solid #eadcec;

  color: #d94e86;
  background: #fff5f9;
}

.event-button small {
  margin-left: 4px;

  padding: 2px 4px;

  border-radius: 4px;

  color: #dc7a22;
  font-size: 7px;

  background: #fff0dc;
}

.create-button {
  border: 1px solid #725ee7;

  color: #ffffff;
  background: #725ee7;
}

.loading-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  justify-content: center;

  min-height: 270px;

  color: #9097a6;
  font-size: 11px;
}

.loading-spinner {
  width: 28px;
  height: 28px;

  border: 3px solid #e7e4fa;
  border-top-color: #715ee6;
  border-radius: 50%;

  animation: spin 0.8s linear infinite;
}

.table-scroll {
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 700px;

  border-collapse: collapse;
}

th {
  padding: 12px 16px;

  color: #7e8696;
  font-size: 10px;
  text-align: left;

  background: #fafbfc;
}

td {
  padding: 13px 16px;

  border-top: 1px solid #edf0f4;

  color: #515867;
  font-size: 10px;
}

.id-cell {
  color: #343a48;
  font-weight: 800;
}

.category-name {
  display: flex;
  gap: 10px;
  align-items: center;
}

.category-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  width: 34px;
  height: 34px;

  border-radius: 10px;

  color: #6959dc;
  font-size: 14px;

  background: #eeebff;
}

.category-name strong {
  display: block;

  color: #363c49;
  font-size: 11px;
}

.category-name small {
  display: block;
  margin-top: 4px;

  color: #9ba2af;
  font-size: 9px;
}

.order-badge {
  display: inline-flex;
  min-width: 30px;
  justify-content: center;

  padding: 5px 8px;

  border-radius: 7px;

  color: #3979db;
  font-weight: 800;

  background: #e4eeff;
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

.management-buttons button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.edit-button {
  border: 1px solid #dfe3e9;

  color: #5f6775;
  background: #ffffff;
}

.delete-button {
  border: 1px solid #ffd5dc;

  color: #db485e;
  background: #fff2f4;
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
  align-items: center;
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

/*
 * 등록·수정 모달
 */
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

.category-modal {
  width: 100%;
  max-width: 460px;

  border: 1px solid #e3e6ed;
  border-radius: 18px;

  background: #ffffff;

  box-shadow:
    0 28px 70px
    rgba(23, 27, 43, 0.22);
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;

  padding: 21px 22px 17px;

  border-bottom: 1px solid #ebedf1;
}

.modal-header p {
  margin: 0 0 5px;

  color: #725fe5;
  font-size: 9px;
  font-weight: 900;
  letter-spacing: 1.2px;
}

.modal-header h2 {
  margin: 0;

  color: #292e3b;
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
  gap: 19px;

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

.modal-body label > span strong {
  color: #e34d64;
}

.modal-body input {
  width: 100%;
  height: 43px;
  padding: 0 12px;

  border: 1px solid #dfe3ea;
  border-radius: 9px;
  outline: none;

  color: #3b414e;
  font-size: 12px;
}

.modal-body input:focus {
  border-color: #7461e7;
  box-shadow:
    0 0 0 3px
    rgba(116, 97, 231, 0.1);
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

.modal-footer button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
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

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

@media (max-width: 1100px) {
  .category-summary-grid {
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

  .search-box {
    flex: 1;
  }
}

@media (max-width: 550px) {
  .category-summary-grid {
    grid-template-columns: 1fr;
  }

  .search-box {
    width: 100%;
    flex-basis: 100%;
  }
}
</style>