<script setup>
import {
  computed,
  onMounted,
  reactive,
  ref
} from 'vue'

import {
  createHeadStore,
  createHeadStoreEmployee,
  extractStoreData,
  extractStoreErrorMessage,
  getHeadStoreDetail,
  getHeadStores,
  updateHeadStore
} from '@/api/head/headStoreApi'

const stores = ref([])

const loading = ref(false)
const detailLoading = ref(false)
const saving = ref(false)
const employeeSaving = ref(false)

const searchKeyword = ref('')
const statusFilter = ref('ALL')

const message = ref('')
const messageType = ref('success')

/*
 * 지점 등록·수정 모달
 */
const storeModal = reactive({
  open: false,
  mode: 'create',
  storeId: null
})

/*
 * 지점 상세 모달
 */
const detailModal = reactive({
  open: false,
  store: null
})

/*
 * 지점 관리자 계정 생성 모달
 */
const employeeModal = reactive({
  open: false,
  storeId: null,
  storeName: ''
})

const storeForm = reactive({
  storeName: '',
  businessNumber: '',
  region: '',
  address: '',
  phone: '',
  businessHours: '',
  storeStatus: 'CLOSED'
})

const employeeForm = reactive({
  loginId: '',
  password: '',
  passwordConfirm: '',
  name: '',
  role: 'STORE_ADMIN'
})

/*
 * 백엔드 DTO 필드명이 조금 달라도
 * 화면에서는 동일한 구조로 사용합니다.
 */
const normalizeStore = (store = {}) => {
  return {
    storeId:
      store.storeId ??
      store.id ??
      null,

    storeName:
      store.storeName ??
      store.name ??
      '지점명 없음',

    businessNumber:
      store.businessNumber ??
      store.businessNo ??
      '',

    region:
      store.region ??
      '',

    address:
      store.address ??
      '',

    phone:
      store.phone ??
      store.phoneNumber ??
      '',

    businessHours:
      store.businessHours ??
      store.operatingHours ??
      '',

    storeStatus:
      String(
        store.storeStatus ??
        store.status ??
        'CLOSED'
      ).toUpperCase(),

    createdAt:
      store.createdAt ??
      store.createdDate ??
      '',

    kioskCount:
      Number(
        store.kioskCount ??
        store.kiosks?.length ??
        0
      ),

    raw: store
  }
}

const filteredStores = computed(() => {
  const keyword =
    searchKeyword.value
      .trim()
      .toLowerCase()

  return stores.value.filter((store) => {
    const matchesKeyword =
      !keyword ||
      store.storeName
        .toLowerCase()
        .includes(keyword) ||
      store.address
        .toLowerCase()
        .includes(keyword) ||
      store.region
        .toLowerCase()
        .includes(keyword) ||
      String(store.storeId)
        .includes(keyword)

    const matchesStatus =
      statusFilter.value === 'ALL' ||
      store.storeStatus ===
        statusFilter.value

    return (
      matchesKeyword &&
      matchesStatus
    )
  })
})

const openStoreCount = computed(() => {
  return stores.value.filter(
    (store) =>
      store.storeStatus === 'OPEN'
  ).length
})

const closedStoreCount = computed(() => {
  return stores.value.filter(
    (store) =>
      store.storeStatus === 'CLOSED'
  ).length
})

const dayOffStoreCount = computed(() => {
  return stores.value.filter(
    (store) =>
      store.storeStatus === 'DAY_OFF'
  ).length
})

const statusText = (status) => {
  switch (status) {
    case 'OPEN':
      return '영업 중'

    case 'DAY_OFF':
      return '휴무'

    case 'CLOSED':
      return '영업 종료'

    default:
      return status || '상태 없음'
  }
}

const statusClass = (status) => {
  switch (status) {
    case 'OPEN':
      return 'status-open'

    case 'DAY_OFF':
      return 'status-day-off'

    default:
      return 'status-closed'
  }
}

const formatDate = (dateValue) => {
  if (!dateValue) {
    return '-'
  }

  const date =
    new Date(dateValue)

  if (Number.isNaN(date.getTime())) {
    return String(dateValue)
  }

  return date.toLocaleDateString(
    'ko-KR'
  )
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
 * 지점 목록 조회
 */
const loadStores = async () => {
  loading.value = true
  clearMessage()

  try {
    const responseBody =
      await getHeadStores()

    const responseData =
      extractStoreData(responseBody)

    let storeList = []

    if (Array.isArray(responseData)) {
      storeList = responseData
    } else if (
      Array.isArray(responseData?.content)
    ) {
      storeList = responseData.content
    }

    stores.value =
      storeList.map(normalizeStore)

  } catch (error) {
    showMessage(
      extractStoreErrorMessage(
        error,
        '지점 목록을 불러오지 못했습니다.'
      ),
      'error'
    )

  } finally {
    loading.value = false
  }
}

const resetStoreForm = () => {
  storeForm.storeName = ''
  storeForm.businessNumber = ''
  storeForm.region = ''
  storeForm.address = ''
  storeForm.phone = ''
  storeForm.businessHours = ''
  storeForm.storeStatus = 'CLOSED'
}

const openCreateStoreModal = () => {
  resetStoreForm()

  storeModal.mode = 'create'
  storeModal.storeId = null
  storeModal.open = true
}

/*
 * 상세 조회 후 수정 폼에 입력
 */
const openEditStoreModal = async (
  storeId
) => {
  detailLoading.value = true
  clearMessage()

  try {
    const responseBody =
      await getHeadStoreDetail(storeId)

    const store =
      normalizeStore(
        extractStoreData(responseBody)
      )

    storeForm.storeName =
      store.storeName

    storeForm.businessNumber =
      store.businessNumber

    storeForm.region =
      store.region

    storeForm.address =
      store.address

    storeForm.phone =
      store.phone

    storeForm.businessHours =
      store.businessHours

    storeForm.storeStatus =
      store.storeStatus

    storeModal.mode = 'edit'
    storeModal.storeId = storeId
    storeModal.open = true

  } catch (error) {
    showMessage(
      extractStoreErrorMessage(
        error,
        '지점 정보를 불러오지 못했습니다.'
      ),
      'error'
    )

  } finally {
    detailLoading.value = false
  }
}

const closeStoreModal = () => {
  if (saving.value) {
    return
  }

  storeModal.open = false
}

/*
 * 지점 등록·수정 요청 객체
 */
const createStorePayload = () => {
  return {
    storeName:
      storeForm.storeName.trim(),

    businessNumber:
      storeForm.businessNumber.trim() ||
      null,

    region:
      storeForm.region.trim(),

    address:
      storeForm.address.trim(),

    phone:
      storeForm.phone.trim() ||
      null,

    businessHours:
      storeForm.businessHours.trim() ||
      null,

    storeStatus:
      storeForm.storeStatus
  }
}

const validateStoreForm = () => {
  if (!storeForm.storeName.trim()) {
    showMessage(
      '지점명을 입력해주세요.',
      'error'
    )

    return false
  }

  if (!storeForm.region.trim()) {
    showMessage(
      '지역을 입력해주세요.',
      'error'
    )

    return false
  }

  if (!storeForm.address.trim()) {
    showMessage(
      '지점 주소를 입력해주세요.',
      'error'
    )

    return false
  }

  return true
}

/*
 * 지점 저장
 */
const submitStore = async () => {
  if (!validateStoreForm()) {
    return
  }

  saving.value = true
  clearMessage()

  try {
    const payload =
      createStorePayload()

    if (storeModal.mode === 'create') {
      await createHeadStore(payload)

      showMessage(
        '지점이 등록되었습니다.',
        'success'
      )

    } else {
      await updateHeadStore(
        storeModal.storeId,
        payload
      )

      showMessage(
        '지점 정보가 수정되었습니다.',
        'success'
      )
    }

    storeModal.open = false

    await loadStores()

  } catch (error) {
    showMessage(
      extractStoreErrorMessage(
        error,
        storeModal.mode === 'create'
          ? '지점 등록에 실패했습니다.'
          : '지점 수정에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    saving.value = false
  }
}

/*
 * 지점 상세 조회
 */
const openDetailModal = async (
  storeId
) => {
  detailModal.open = true
  detailModal.store = null
  detailLoading.value = true
  clearMessage()

  try {
    const responseBody =
      await getHeadStoreDetail(storeId)

    detailModal.store =
      normalizeStore(
        extractStoreData(responseBody)
      )

  } catch (error) {
    detailModal.open = false

    showMessage(
      extractStoreErrorMessage(
        error,
        '지점 상세 정보를 불러오지 못했습니다.'
      ),
      'error'
    )

  } finally {
    detailLoading.value = false
  }
}

const closeDetailModal = () => {
  detailModal.open = false
  detailModal.store = null
}

const editDetailStore = () => {
  const storeId =
    detailModal.store?.storeId

  if (!storeId) {
    return
  }

  closeDetailModal()
  openEditStoreModal(storeId)
}

/*
 * 관리자 계정 생성 모달
 */
const resetEmployeeForm = () => {
  employeeForm.loginId = ''
  employeeForm.password = ''
  employeeForm.passwordConfirm = ''
  employeeForm.name = ''
  employeeForm.role = 'STORE_ADMIN'
}

const openEmployeeModal = (store) => {
  resetEmployeeForm()

  employeeModal.storeId =
    store.storeId

  employeeModal.storeName =
    store.storeName

  employeeModal.open = true
}

const closeEmployeeModal = () => {
  if (employeeSaving.value) {
    return
  }

  employeeModal.open = false
}

const validateEmployeeForm = () => {
  if (!employeeForm.loginId.trim()) {
    showMessage(
      '지점 관리자 로그인 ID를 입력해주세요.',
      'error'
    )

    return false
  }

  if (!employeeForm.name.trim()) {
    showMessage(
      '관리자 이름을 입력해주세요.',
      'error'
    )

    return false
  }

  if (!employeeForm.password) {
    showMessage(
      '초기 비밀번호를 입력해주세요.',
      'error'
    )

    return false
  }

  if (
    employeeForm.password !==
    employeeForm.passwordConfirm
  ) {
    showMessage(
      '비밀번호 확인이 일치하지 않습니다.',
      'error'
    )

    return false
  }

  return true
}

/*
 * 지점 관리자 계정 생성
 */
const submitEmployee = async () => {
  if (!validateEmployeeForm()) {
    return
  }

  employeeSaving.value = true
  clearMessage()

  try {
    await createHeadStoreEmployee(
      employeeModal.storeId,
      {
        loginId:
          employeeForm.loginId.trim(),

        password:
          employeeForm.password,

        name:
          employeeForm.name.trim(),

        role:
          'STORE_ADMIN'
      }
    )

    employeeModal.open = false

    showMessage(
      `${employeeModal.storeName} 관리자 계정이 생성되었습니다.`,
      'success'
    )

  } catch (error) {
    showMessage(
      extractStoreErrorMessage(
        error,
        '지점 관리자 계정 생성에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    employeeSaving.value = false
  }
}

onMounted(() => {
  loadStores()
})
</script>

<template>
  <section class="store-page">
    <Transition name="message">
      <div
        v-if="message"
        class="page-message"
        :class="{
          error: messageType === 'error'
        }"
      >
        <strong>
          {{
            messageType === 'error'
              ? '!'
              : '✓'
          }}
        </strong>

        <p>{{ message }}</p>

        <button
          type="button"
          @click="clearMessage"
        >
          ×
        </button>
      </div>
    </Transition>

    <!-- 요약 카드 -->
    <div class="summary-grid">
      <article>
        <div>
          <p>전체 지점</p>
          <strong>{{ stores.length }}</strong>
          <small>본사 등록 지점</small>
        </div>

        <span class="icon purple">
          ▦
        </span>
      </article>

      <article>
        <div>
          <p>영업 중</p>
          <strong>{{ openStoreCount }}</strong>
          <small>현재 운영 지점</small>
        </div>

        <span class="icon green">
          ✓
        </span>
      </article>

      <article>
        <div>
          <p>영업 종료</p>
          <strong>{{ closedStoreCount }}</strong>
          <small>현재 영업 종료</small>
        </div>

        <span class="icon gray">
          −
        </span>
      </article>

      <article>
        <div>
          <p>휴무</p>
          <strong>{{ dayOffStoreCount }}</strong>
          <small>오늘 휴무 지점</small>
        </div>

        <span class="icon orange">
          ◷
        </span>
      </article>
    </div>

    <!-- 지점 목록 -->
    <section class="store-panel">
      <header class="panel-header">
        <div>
          <h2>전체 지점 관리</h2>

          <p>
            지점 정보와 영업 상태 및 관리자 계정을 관리합니다.
          </p>
        </div>

        <div class="panel-actions">
          <input
            v-model="searchKeyword"
            type="search"
            placeholder="지점명, 주소, 지역 검색"
          />

          <select v-model="statusFilter">
            <option value="ALL">
              전체 상태
            </option>

            <option value="OPEN">
              영업 중
            </option>

            <option value="CLOSED">
              영업 종료
            </option>

            <option value="DAY_OFF">
              휴무
            </option>
          </select>

          <button
            type="button"
            class="create-button"
            @click="openCreateStoreModal"
          >
            ＋ 지점 등록
          </button>
        </div>
      </header>

      <div
        v-if="loading"
        class="loading-area"
      >
        <span class="spinner" />

        <p>지점 목록을 불러오는 중입니다.</p>
      </div>

      <div v-else class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>지점</th>
              <th>지역</th>
              <th>주소</th>
              <th>전화번호</th>
              <th>영업시간</th>
              <th>상태</th>
              <th>관리</th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="store in filteredStores"
              :key="store.storeId"
            >
              <td>
                <div class="store-name">
                  <span>
                    {{ store.storeName.charAt(0) }}
                  </span>

                  <div>
                    <strong>
                      {{ store.storeName }}
                    </strong>

                    <small>
                      지점 ID #{{ store.storeId }}
                    </small>
                  </div>
                </div>
              </td>

              <td>
                {{ store.region || '-' }}
              </td>

              <td class="address-cell">
                {{ store.address || '-' }}
              </td>

              <td>
                {{ store.phone || '-' }}
              </td>

              <td>
                {{ store.businessHours || '-' }}
              </td>

              <td>
                <span
                  class="status-badge"
                  :class="
                    statusClass(
                      store.storeStatus
                    )
                  "
                >
                  {{
                    statusText(
                      store.storeStatus
                    )
                  }}
                </span>
              </td>

              <td>
                <div class="management-buttons">
                  <button
                    type="button"
                    class="detail-button"
                    @click="
                      openDetailModal(
                        store.storeId
                      )
                    "
                  >
                    상세
                  </button>

                  <button
                    type="button"
                    class="edit-button"
                    @click="
                      openEditStoreModal(
                        store.storeId
                      )
                    "
                  >
                    수정
                  </button>

                  <button
                    type="button"
                    class="employee-button"
                    @click="
                      openEmployeeModal(store)
                    "
                  >
                    계정 발급
                  </button>
                </div>
              </td>
            </tr>

            <tr
              v-if="
                filteredStores.length === 0
              "
            >
              <td
                colspan="7"
                class="empty-cell"
              >
                <strong>
                  조회된 지점이 없습니다.
                </strong>

                <p>
                  검색 조건을 변경하거나 지점을 등록해주세요.
                </p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer class="panel-footer">
        <span>
          전체 {{ stores.length }}개 중
          {{ filteredStores.length }}개 표시
        </span>

        <button
          type="button"
          @click="loadStores"
        >
          새로고침
        </button>
      </footer>
    </section>

    <!-- 지점 등록·수정 모달 -->
    <Teleport to="body">
      <div
        v-if="storeModal.open"
        class="modal-backdrop"
        @click.self="closeStoreModal"
      >
        <form
          class="form-modal"
          @submit.prevent="submitStore"
        >
          <header class="modal-header">
            <div>
              <p>STORE MANAGEMENT</p>

              <h2>
                {{
                  storeModal.mode === 'create'
                    ? '지점 등록'
                    : '지점 정보 수정'
                }}
              </h2>
            </div>

            <button
              type="button"
              :disabled="saving"
              @click="closeStoreModal"
            >
              ×
            </button>
          </header>

          <div class="modal-body">
            <div class="form-grid">
              <label>
                <span>지점명 *</span>

                <input
                  v-model="storeForm.storeName"
                  type="text"
                  maxlength="100"
                  placeholder="예: 강남역점"
                />
              </label>

              <label>
                <span>지역 *</span>

                <input
                  v-model="storeForm.region"
                  type="text"
                  maxlength="50"
                  placeholder="예: 서울"
                />
              </label>

              <label>
                <span>사업자 등록번호</span>

                <input
                  v-model="
                    storeForm.businessNumber
                  "
                  type="text"
                  maxlength="20"
                  placeholder="123-45-67890"
                />
              </label>

              <label>
                <span>전화번호</span>

                <input
                  v-model="storeForm.phone"
                  type="text"
                  maxlength="20"
                  placeholder="02-1234-5678"
                />
              </label>

              <label class="full-field">
                <span>주소 *</span>

                <input
                  v-model="storeForm.address"
                  type="text"
                  maxlength="255"
                  placeholder="지점 주소를 입력하세요."
                />
              </label>

              <label>
                <span>영업시간</span>

                <input
                  v-model="
                    storeForm.businessHours
                  "
                  type="text"
                  maxlength="100"
                  placeholder="10:00~22:00"
                />
              </label>

              <label>
                <span>영업 상태</span>

                <select
                  v-model="
                    storeForm.storeStatus
                  "
                >
                  <option value="OPEN">
                    영업 중
                  </option>

                  <option value="CLOSED">
                    영업 종료
                  </option>

                  <option value="DAY_OFF">
                    휴무
                  </option>
                </select>
              </label>
            </div>
          </div>

          <footer class="modal-footer">
            <button
              type="button"
              class="cancel-button"
              :disabled="saving"
              @click="closeStoreModal"
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
                  : storeModal.mode === 'create'
                    ? '지점 등록'
                    : '수정 저장'
              }}
            </button>
          </footer>
        </form>
      </div>
    </Teleport>

    <!-- 관리자 계정 생성 모달 -->
    <Teleport to="body">
      <div
        v-if="employeeModal.open"
        class="modal-backdrop"
        @click.self="closeEmployeeModal"
      >
        <form
          class="form-modal employee-modal"
          @submit.prevent="submitEmployee"
        >
          <header class="modal-header">
            <div>
              <p>STORE ADMIN ACCOUNT</p>

              <h2>지점 관리자 계정 발급</h2>

              <small>
                {{ employeeModal.storeName }}
              </small>
            </div>

            <button
              type="button"
              :disabled="employeeSaving"
              @click="closeEmployeeModal"
            >
              ×
            </button>
          </header>

          <div class="modal-body">
            <label>
              <span>로그인 ID *</span>

              <input
                v-model="employeeForm.loginId"
                type="text"
                maxlength="50"
                autocomplete="off"
                placeholder="예: gangnam_admin"
              />
            </label>

            <label>
              <span>관리자 이름 *</span>

              <input
                v-model="employeeForm.name"
                type="text"
                maxlength="50"
                placeholder="담당자 이름"
              />
            </label>

            <label>
              <span>초기 비밀번호 *</span>

              <input
                v-model="employeeForm.password"
                type="password"
                autocomplete="new-password"
                placeholder="초기 비밀번호"
              />
            </label>

            <label>
              <span>비밀번호 확인 *</span>

              <input
                v-model="
                  employeeForm.passwordConfirm
                "
                type="password"
                autocomplete="new-password"
                placeholder="비밀번호 재입력"
              />
            </label>

            <div class="role-box">
              <span>발급 권한</span>

              <strong>STORE_ADMIN</strong>

              <small>
                해당 지점의 주문 및 판매 메뉴를 관리하는 계정입니다.
              </small>
            </div>
          </div>

          <footer class="modal-footer">
            <button
              type="button"
              class="cancel-button"
              :disabled="employeeSaving"
              @click="closeEmployeeModal"
            >
              취소
            </button>

            <button
              type="submit"
              class="save-button"
              :disabled="employeeSaving"
            >
              {{
                employeeSaving
                  ? '발급 중...'
                  : '관리자 계정 발급'
              }}
            </button>
          </footer>
        </form>
      </div>
    </Teleport>

    <!-- 상세 모달 -->
    <Teleport to="body">
      <div
        v-if="detailModal.open"
        class="modal-backdrop"
        @click.self="closeDetailModal"
      >
        <section class="detail-modal">
          <button
            type="button"
            class="modal-close"
            @click="closeDetailModal"
          >
            ×
          </button>

          <div
            v-if="detailLoading"
            class="loading-area"
          >
            <span class="spinner" />
          </div>

          <template
            v-else-if="detailModal.store"
          >
            <p class="detail-label">
              STORE DETAIL
            </p>

            <h2>
              {{ detailModal.store.storeName }}
            </h2>

            <span
              class="status-badge detail-status"
              :class="
                statusClass(
                  detailModal.store.storeStatus
                )
              "
            >
              {{
                statusText(
                  detailModal.store.storeStatus
                )
              }}
            </span>

            <dl>
              <div>
                <dt>지점 ID</dt>
                <dd>
                  #{{ detailModal.store.storeId }}
                </dd>
              </div>

              <div>
                <dt>사업자 번호</dt>
                <dd>
                  {{
                    detailModal.store.businessNumber ||
                    '-'
                  }}
                </dd>
              </div>

              <div>
                <dt>지역</dt>
                <dd>
                  {{ detailModal.store.region || '-' }}
                </dd>
              </div>

              <div>
                <dt>주소</dt>
                <dd>
                  {{ detailModal.store.address || '-' }}
                </dd>
              </div>

              <div>
                <dt>전화번호</dt>
                <dd>
                  {{ detailModal.store.phone || '-' }}
                </dd>
              </div>

              <div>
                <dt>영업시간</dt>
                <dd>
                  {{
                    detailModal.store.businessHours ||
                    '-'
                  }}
                </dd>
              </div>

              <div>
                <dt>키오스크 수</dt>
                <dd>
                  {{
                    detailModal.store.kioskCount
                  }}대
                </dd>
              </div>

              <div>
                <dt>등록일</dt>
                <dd>
                  {{
                    formatDate(
                      detailModal.store.createdAt
                    )
                  }}
                </dd>
              </div>
            </dl>

            <button
              type="button"
              class="detail-edit-button"
              @click="editDetailStore"
            >
              지점 정보 수정
            </button>
          </template>
        </section>
      </div>
    </Teleport>
  </section>
</template>

<style scoped>
.store-page {
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

.page-message strong {
  display: grid;
  width: 22px;
  height: 22px;
  place-items: center;
  border-radius: 50%;
  color: white;
  background: #25ad78;
}

.page-message p {
  flex: 1;
  margin: 0;
  font-size: 12px;
}

.page-message button {
  border: 0;
  cursor: pointer;
  color: inherit;
  font-size: 20px;
  background: transparent;
}

.page-message.error {
  border-color: #ffd0d7;
  color: #d64359;
  background: #fff2f4;
}

.page-message.error strong {
  background: #eb566b;
}

.summary-grid {
  display: grid;
  grid-template-columns:
    repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-grid article {
  display: flex;
  justify-content: space-between;
  min-height: 125px;
  padding: 18px;
  border: 1px solid #e3e6ed;
  border-radius: 15px;
  background: white;
}

.summary-grid p {
  margin: 0;
  color: #798191;
  font-size: 11px;
  font-weight: 700;
}

.summary-grid strong {
  display: block;
  margin-top: 12px;
  font-size: 25px;
}

.summary-grid small {
  color: #969dab;
  font-size: 9px;
}

.icon {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
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

.orange {
  color: #d98525;
  background: #fff0dc;
}

.store-panel {
  overflow: hidden;
  border: 1px solid #e2e6ed;
  border-radius: 15px;
  background: white;
}

.panel-header {
  display: flex;
  gap: 20px;
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

.panel-actions input,
.panel-actions select {
  height: 36px;
  padding: 0 11px;
  border: 1px solid #dfe3ea;
  border-radius: 9px;
  outline: none;
  font-size: 10px;
}

.create-button {
  height: 36px;
  padding: 0 14px;
  border: 0;
  border-radius: 9px;
  cursor: pointer;
  color: white;
  font-size: 10px;
  font-weight: 800;
  background: #725ee7;
}

.table-scroll {
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 1050px;
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
  color: #515867;
}

.store-name {
  display: flex;
  gap: 10px;
  align-items: center;
}

.store-name > span {
  display: grid;
  width: 39px;
  height: 39px;
  place-items: center;
  border-radius: 10px;
  color: #6756dc;
  font-weight: 900;
  background: #eeebff;
}

.store-name strong,
.store-name small {
  display: block;
}

.store-name small {
  color: #9aa1af;
  font-size: 9px;
}

.address-cell {
  max-width: 250px;
}

.status-badge {
  display: inline-flex;
  padding: 5px 9px;
  border-radius: 20px;
  font-size: 9px;
  font-weight: 800;
}

.status-open {
  color: #168e61;
  background: #dff8ec;
}

.status-closed {
  color: #737b89;
  background: #edf0f4;
}

.status-day-off {
  color: #d07819;
  background: #fff0dc;
}

.management-buttons {
  display: flex;
  gap: 5px;
}

.management-buttons button {
  height: 29px;
  padding: 0 9px;
  border-radius: 7px;
  cursor: pointer;
  font-size: 9px;
  font-weight: 750;
}

.detail-button {
  border: 1px solid #dfe3e9;
  color: #5f6775;
  background: white;
}

.edit-button {
  border: 1px solid #dcd7fb;
  color: #6756dc;
  background: #f3f1ff;
}

.employee-button {
  border: 1px solid #ffd6e6;
  color: #d8457d;
  background: #fff0f6;
}

.empty-cell {
  height: 170px;
  text-align: center;
}

.empty-cell p {
  color: #9ba2b0;
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
  border: 1px solid #dfe3e9;
  border-radius: 7px;
  cursor: pointer;
  background: white;
}

.loading-area {
  display: grid;
  min-height: 220px;
  place-items: center;
  color: #9097a6;
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

.form-modal,
.detail-modal {
  overflow-y: auto;
  width: 100%;
  max-height: calc(100vh - 48px);
  border-radius: 18px;
  background: white;
  box-shadow:
    0 28px 70px rgba(23, 27, 43, 0.22);
}

.form-modal {
  max-width: 620px;
}

.employee-modal {
  max-width: 480px;
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
  font-size: 20px;
}

.modal-header button,
.modal-close {
  border: 0;
  cursor: pointer;
  color: #858c9a;
  font-size: 25px;
  background: transparent;
}

.modal-close {
  position: absolute;
  top: 13px;
  right: 15px;
}

.modal-body {
  display: grid;
  gap: 17px;
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

.full-field {
  grid-column: 1 / -1;
}

.modal-body input,
.modal-body select {
  width: 100%;
  height: 43px;
  padding: 0 12px;
  border: 1px solid #dfe3ea;
  border-radius: 9px;
  outline: none;
}

.role-box {
  padding: 14px;
  border: 1px solid #e1def8;
  border-radius: 10px;
  background: #f6f4ff;
}

.role-box span,
.role-box strong,
.role-box small {
  display: block;
}

.role-box strong {
  margin: 6px 0;
  color: #6756dc;
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
  background: white;
}

.save-button {
  border: 1px solid #725ee7;
  color: white;
  background: #725ee7;
}

.detail-status {
  margin-top: 12px;
}

.detail-modal dl {
  display: grid;
  gap: 1px;
  margin-top: 20px;
  background: #edf0f4;
}

.detail-modal dl div {
  display: grid;
  grid-template-columns: 120px 1fr;
  padding: 12px;
  background: white;
}

.detail-modal dt {
  color: #888f9e;
  font-size: 10px;
}

.detail-modal dd {
  margin: 0;
  font-size: 11px;
}

.detail-edit-button {
  width: 100%;
  height: 42px;
  margin-top: 22px;
  border: 0;
  border-radius: 9px;
  cursor: pointer;
  color: white;
  font-weight: 800;
  background: #725ee7;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1100px) {
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

@media (max-width: 650px) {
  .summary-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .full-field {
    grid-column: auto;
  }

  .panel-actions input,
  .panel-actions select,
  .create-button {
    width: 100%;
  }
}
</style>