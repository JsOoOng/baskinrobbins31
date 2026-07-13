<script setup>
import {
  computed,
  onMounted,
  reactive,
  ref
} from 'vue'

import {
  createHeadAdmin,
  extractSecurityData,
  extractSecurityErrorMessage,
  getHeadAdmins,
  resetHeadAdminPassword,
  updateHeadAdmin,
  updateHeadAdminStatus
} from '@/api/head/headSecurityApi'

const admins = ref([])

const loading = ref(false)
const saving = ref(false)
const statusUpdatingId = ref(null)
const passwordSaving = ref(false)

const searchKeyword = ref('')
const roleFilter = ref('ALL')
const statusFilter = ref('ALL')

const message = ref('')
const messageType = ref('success')

const adminModal = reactive({
  open: false,
  mode: 'create',
  adminId: null
})

const passwordModal = reactive({
  open: false,
  adminId: null,
  adminName: '',
  loginId: ''
})

const adminForm = reactive({
  loginId: '',
  password: '',
  passwordConfirm: '',
  name: '',
  department: '',
  role: 'HEAD_ADMIN',
  status: 'ACTIVE'
})

const passwordForm = reactive({
  newPassword: '',
  passwordConfirm: ''
})

const roleOptions = [
  {
    value: 'SUPER_ADMIN',
    label: '최고 관리자'
  },
  {
    value: 'HEAD_ADMIN',
    label: '본사 관리자'
  },
  {
    value: 'ADMIN',
    label: '일반 관리자'
  }
]

const normalizeAdmin = (admin = {}) => {
  return {
    adminId:
      admin.adminId ??
      admin.id ??
      null,

    loginId:
      admin.loginId ??
      '',

    name:
      admin.name ??
      '이름 없음',

    department:
      admin.department ??
      '',

    role:
      String(
        admin.role ??
        'ADMIN'
      ).toUpperCase(),

    status:
      String(
        admin.status ??
        'INACTIVE'
      ).toUpperCase(),

    createdAt:
      admin.createdAt ??
      ''
  }
}

const filteredAdmins = computed(() => {
  const keyword =
    searchKeyword.value
      .trim()
      .toLowerCase()

  return admins.value.filter((admin) => {
    const matchesKeyword =
      !keyword ||
      admin.loginId
        .toLowerCase()
        .includes(keyword) ||
      admin.name
        .toLowerCase()
        .includes(keyword) ||
      admin.department
        .toLowerCase()
        .includes(keyword) ||
      String(admin.adminId)
        .includes(keyword)

    const matchesRole =
      roleFilter.value === 'ALL' ||
      admin.role === roleFilter.value

    const matchesStatus =
      statusFilter.value === 'ALL' ||
      admin.status === statusFilter.value

    return (
      matchesKeyword &&
      matchesRole &&
      matchesStatus
    )
  })
})

const activeCount = computed(() => {
  return admins.value.filter(
    (admin) =>
      admin.status === 'ACTIVE'
  ).length
})

const inactiveCount = computed(() => {
  return admins.value.filter(
    (admin) =>
      admin.status === 'INACTIVE'
  ).length
})

const superAdminCount = computed(() => {
  return admins.value.filter(
    (admin) =>
      admin.role === 'SUPER_ADMIN'
  ).length
})

const roleText = (role) => {
  switch (role) {
    case 'SUPER_ADMIN':
      return '최고 관리자'

    case 'HEAD_ADMIN':
      return '본사 관리자'

    case 'ADMIN':
      return '일반 관리자'

    default:
      return role || '-'
  }
}

const roleClass = (role) => {
  switch (role) {
    case 'SUPER_ADMIN':
      return 'role-super'

    case 'HEAD_ADMIN':
      return 'role-head'

    default:
      return 'role-admin'
  }
}

const statusText = (status) => {
  return status === 'ACTIVE'
    ? '활성'
    : '비활성'
}

const formatDate = (value) => {
  if (!value) {
    return '-'
  }

  const date =
    new Date(value)

  if (Number.isNaN(date.getTime())) {
    return String(value)
  }

  return date.toLocaleString(
    'ko-KR',
    {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }
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
 * 관리자 목록 조회
 */
const loadAdmins = async () => {
  loading.value = true
  clearMessage()

  try {
    const responseBody =
      await getHeadAdmins()

    const responseData =
      extractSecurityData(
        responseBody
      )

    admins.value =
      Array.isArray(responseData)
        ? responseData.map(
            normalizeAdmin
          )
        : []

  } catch (error) {
    showMessage(
      extractSecurityErrorMessage(
        error,
        '관리자 목록을 불러오지 못했습니다.'
      ),
      'error'
    )

  } finally {
    loading.value = false
  }
}

const resetAdminForm = () => {
  adminForm.loginId = ''
  adminForm.password = ''
  adminForm.passwordConfirm = ''
  adminForm.name = ''
  adminForm.department = ''
  adminForm.role = 'HEAD_ADMIN'
  adminForm.status = 'ACTIVE'
}

const openCreateModal = () => {
  resetAdminForm()

  adminModal.mode = 'create'
  adminModal.adminId = null
  adminModal.open = true
}

const openEditModal = (admin) => {
  adminForm.loginId =
    admin.loginId

  adminForm.password = ''
  adminForm.passwordConfirm = ''

  adminForm.name =
    admin.name

  adminForm.department =
    admin.department

  adminForm.role =
    admin.role

  adminForm.status =
    admin.status

  adminModal.mode = 'edit'
  adminModal.adminId =
    admin.adminId

  adminModal.open = true
}

const closeAdminModal = () => {
  if (saving.value) {
    return
  }

  adminModal.open = false
}

const validateAdminForm = () => {
  if (
    adminModal.mode === 'create' &&
    !adminForm.loginId.trim()
  ) {
    showMessage(
      '로그인 ID를 입력해주세요.',
      'error'
    )

    return false
  }

  if (!adminForm.name.trim()) {
    showMessage(
      '관리자 이름을 입력해주세요.',
      'error'
    )

    return false
  }

  if (!adminForm.role) {
    showMessage(
      '관리자 역할을 선택해주세요.',
      'error'
    )

    return false
  }

  if (adminModal.mode === 'create') {
    if (!adminForm.password) {
      showMessage(
        '초기 비밀번호를 입력해주세요.',
        'error'
      )

      return false
    }

    if (adminForm.password.length < 4) {
      showMessage(
        '비밀번호는 4자 이상이어야 합니다.',
        'error'
      )

      return false
    }

    if (
      adminForm.password !==
      adminForm.passwordConfirm
    ) {
      showMessage(
        '비밀번호 확인이 일치하지 않습니다.',
        'error'
      )

      return false
    }
  }

  return true
}

const submitAdmin = async () => {
  if (!validateAdminForm()) {
    return
  }

  saving.value = true
  clearMessage()

  try {
    if (adminModal.mode === 'create') {
      await createHeadAdmin({
        loginId:
          adminForm.loginId.trim(),

        password:
          adminForm.password,

        name:
          adminForm.name.trim(),

        department:
          adminForm.department.trim() ||
          null,

        role:
          adminForm.role,

        status:
          adminForm.status
      })

      showMessage(
        '본사 관리자 계정이 생성되었습니다.'
      )

    } else {
      await updateHeadAdmin(
        adminModal.adminId,
        {
          name:
            adminForm.name.trim(),

          department:
            adminForm.department.trim() ||
            null,

          role:
            adminForm.role
        }
      )

      showMessage(
        '관리자 정보가 수정되었습니다.'
      )
    }

    adminModal.open = false

    await loadAdmins()

  } catch (error) {
    showMessage(
      extractSecurityErrorMessage(
        error,
        adminModal.mode === 'create'
          ? '관리자 계정 생성에 실패했습니다.'
          : '관리자 정보 수정에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    saving.value = false
  }
}

/*
 * 관리자 활성·비활성 변경
 */
const toggleAdminStatus = async (
  admin
) => {
  const nextStatus =
    admin.status === 'ACTIVE'
      ? 'INACTIVE'
      : 'ACTIVE'

  if (nextStatus === 'INACTIVE') {
    const confirmed = window.confirm(
      `"${admin.name}" 관리자 계정을 비활성화하시겠습니까?\n비활성화된 계정은 로그인할 수 없습니다.`
    )

    if (!confirmed) {
      return
    }
  }

  statusUpdatingId.value =
    admin.adminId

  clearMessage()

  try {
    await updateHeadAdminStatus(
      admin.adminId,
      nextStatus
    )

    admin.status =
      nextStatus

    showMessage(
      nextStatus === 'ACTIVE'
        ? '관리자 계정이 활성화되었습니다.'
        : '관리자 계정이 비활성화되었습니다.'
    )

  } catch (error) {
    showMessage(
      extractSecurityErrorMessage(
        error,
        '관리자 상태 변경에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    statusUpdatingId.value = null
  }
}

/*
 * 비밀번호 초기화 모달
 */
const openPasswordModal = (admin) => {
  passwordForm.newPassword = ''
  passwordForm.passwordConfirm = ''

  passwordModal.adminId =
    admin.adminId

  passwordModal.adminName =
    admin.name

  passwordModal.loginId =
    admin.loginId

  passwordModal.open = true
}

const closePasswordModal = () => {
  if (passwordSaving.value) {
    return
  }

  passwordModal.open = false
}

const submitPasswordReset = async () => {
  if (!passwordForm.newPassword) {
    showMessage(
      '새 비밀번호를 입력해주세요.',
      'error'
    )

    return
  }

  if (
    passwordForm.newPassword.length < 4
  ) {
    showMessage(
      '비밀번호는 4자 이상이어야 합니다.',
      'error'
    )

    return
  }

  if (
    passwordForm.newPassword !==
    passwordForm.passwordConfirm
  ) {
    showMessage(
      '비밀번호 확인이 일치하지 않습니다.',
      'error'
    )

    return
  }

  passwordSaving.value = true
  clearMessage()

  try {
    await resetHeadAdminPassword(
      passwordModal.adminId,
      passwordForm.newPassword
    )

    passwordModal.open = false

    showMessage(
      `${passwordModal.adminName} 관리자의 비밀번호가 초기화되었습니다.`
    )

  } catch (error) {
    showMessage(
      extractSecurityErrorMessage(
        error,
        '비밀번호 초기화에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    passwordSaving.value = false
  }
}

onMounted(() => {
  loadAdmins()
})
</script>

<template>
  <section class="security-page">
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
          <p>전체 관리자</p>

          <strong>
            {{ admins.length }}
          </strong>

          <small>본사 관리자 계정</small>
        </div>

        <span class="summary-icon purple">
          ♙
        </span>
      </article>

      <article>
        <div>
          <p>활성 계정</p>

          <strong>
            {{ activeCount }}
          </strong>

          <small>현재 로그인 가능</small>
        </div>

        <span class="summary-icon green">
          ✓
        </span>
      </article>

      <article>
        <div>
          <p>비활성 계정</p>

          <strong>
            {{ inactiveCount }}
          </strong>

          <small>로그인 차단 상태</small>
        </div>

        <span class="summary-icon gray">
          −
        </span>
      </article>

      <article>
        <div>
          <p>최고 관리자</p>

          <strong>
            {{ superAdminCount }}
          </strong>

          <small>SUPER_ADMIN 권한</small>
        </div>

        <span class="summary-icon pink">
          ◆
        </span>
      </article>
    </div>

    <!-- 관리자 목록 -->
    <section class="admin-panel">
      <header class="panel-header">
        <div>
          <p class="section-label">
            SECURITY MANAGEMENT
          </p>

          <h2>본사 관리자 계정</h2>

          <p>
            관리자 역할과 계정 상태 및 비밀번호를 관리합니다.
          </p>
        </div>

        <div class="panel-actions">
          <input
            v-model="searchKeyword"
            type="search"
            placeholder="이름, ID, 부서 검색"
          />

          <select v-model="roleFilter">
            <option value="ALL">
              전체 역할
            </option>

            <option value="SUPER_ADMIN">
              최고 관리자
            </option>

            <option value="HEAD_ADMIN">
              본사 관리자
            </option>

            <option value="ADMIN">
              일반 관리자
            </option>
          </select>

          <select v-model="statusFilter">
            <option value="ALL">
              전체 상태
            </option>

            <option value="ACTIVE">
              활성
            </option>

            <option value="INACTIVE">
              비활성
            </option>
          </select>

          <button
            type="button"
            class="create-button"
            @click="openCreateModal"
          >
            ＋ 관리자 등록
          </button>
        </div>
      </header>

      <div
        v-if="loading"
        class="loading-area"
      >
        <span class="spinner" />

        <p>관리자 목록을 불러오는 중입니다.</p>
      </div>

      <div v-else class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>관리자</th>
              <th>로그인 ID</th>
              <th>부서</th>
              <th>역할</th>
              <th>상태</th>
              <th>등록일</th>
              <th>관리</th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="admin in filteredAdmins"
              :key="admin.adminId"
            >
              <td>
                <div class="admin-name">
                  <span>
                    {{ admin.name.charAt(0) }}
                  </span>

                  <div>
                    <strong>
                      {{ admin.name }}
                    </strong>

                    <small>
                      관리자 #{{ admin.adminId }}
                    </small>
                  </div>
                </div>
              </td>

              <td>
                {{ admin.loginId }}
              </td>

              <td>
                {{ admin.department || '-' }}
              </td>

              <td>
                <span
                  class="role-badge"
                  :class="
                    roleClass(admin.role)
                  "
                >
                  {{ roleText(admin.role) }}
                </span>
              </td>

              <td>
                <button
                  type="button"
                  class="status-button"
                  :class="{
                    active:
                      admin.status === 'ACTIVE'
                  }"
                  :disabled="
                    statusUpdatingId ===
                    admin.adminId
                  "
                  @click="
                    toggleAdminStatus(admin)
                  "
                >
                  {{
                    statusUpdatingId ===
                      admin.adminId
                      ? '변경 중'
                      : statusText(admin.status)
                  }}
                </button>
              </td>

              <td>
                {{ formatDate(admin.createdAt) }}
              </td>

              <td>
                <div class="management-buttons">
                  <button
                    type="button"
                    class="edit-button"
                    @click="openEditModal(admin)"
                  >
                    수정
                  </button>

                  <button
                    type="button"
                    class="password-button"
                    @click="
                      openPasswordModal(admin)
                    "
                  >
                    비밀번호 초기화
                  </button>
                </div>
              </td>
            </tr>

            <tr
              v-if="
                filteredAdmins.length === 0
              "
            >
              <td
                colspan="7"
                class="empty-cell"
              >
                조회된 관리자 계정이 없습니다.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer class="panel-footer">
        <span>
          전체 {{ admins.length }}명 중
          {{ filteredAdmins.length }}명 표시
        </span>

        <button
          type="button"
          @click="loadAdmins"
        >
          새로고침
        </button>
      </footer>
    </section>

    <!-- 관리자 생성·수정 모달 -->
    <Teleport to="body">
      <div
        v-if="adminModal.open"
        class="modal-backdrop"
        @click.self="closeAdminModal"
      >
        <form
          class="form-modal"
          @submit.prevent="submitAdmin"
        >
          <header class="modal-header">
            <div>
              <p>ADMIN ACCOUNT</p>

              <h2>
                {{
                  adminModal.mode === 'create'
                    ? '본사 관리자 등록'
                    : '본사 관리자 수정'
                }}
              </h2>
            </div>

            <button
              type="button"
              :disabled="saving"
              @click="closeAdminModal"
            >
              ×
            </button>
          </header>

          <div class="modal-body">
            <label>
              <span>로그인 ID *</span>

              <input
                v-model="adminForm.loginId"
                type="text"
                maxlength="50"
                autocomplete="off"
                placeholder="예: head_admin02"
                :disabled="
                  adminModal.mode === 'edit'
                "
              />
            </label>

            <label>
              <span>관리자 이름 *</span>

              <input
                v-model="adminForm.name"
                type="text"
                maxlength="50"
                placeholder="관리자 이름"
              />
            </label>

            <label>
              <span>부서</span>

              <input
                v-model="adminForm.department"
                type="text"
                maxlength="50"
                placeholder="예: 운영관리팀"
              />
            </label>

            <label>
              <span>관리자 역할 *</span>

              <select v-model="adminForm.role">
                <option
                  v-for="role in roleOptions"
                  :key="role.value"
                  :value="role.value"
                >
                  {{ role.label }}
                </option>
              </select>
            </label>

            <template
              v-if="
                adminModal.mode === 'create'
              "
            >
              <label>
                <span>초기 비밀번호 *</span>

                <input
                  v-model="adminForm.password"
                  type="password"
                  autocomplete="new-password"
                  placeholder="4자 이상"
                />
              </label>

              <label>
                <span>비밀번호 확인 *</span>

                <input
                  v-model="
                    adminForm.passwordConfirm
                  "
                  type="password"
                  autocomplete="new-password"
                  placeholder="비밀번호 재입력"
                />
              </label>

              <label>
                <span>초기 계정 상태</span>

                <select
                  v-model="adminForm.status"
                >
                  <option value="ACTIVE">
                    활성
                  </option>

                  <option value="INACTIVE">
                    비활성
                  </option>
                </select>
              </label>
            </template>

            <div class="security-note">
              <strong>권한 안내</strong>

              <p>
                최고 관리자는 보안 및 권한 화면에 접근할 수 있습니다.
                자신의 역할 변경과 마지막 최고 관리자 비활성화는 제한됩니다.
              </p>
            </div>
          </div>

          <footer class="modal-footer">
            <button
              type="button"
              class="cancel-button"
              :disabled="saving"
              @click="closeAdminModal"
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
                  : adminModal.mode === 'create'
                    ? '관리자 등록'
                    : '수정 저장'
              }}
            </button>
          </footer>
        </form>
      </div>
    </Teleport>

    <!-- 비밀번호 초기화 모달 -->
    <Teleport to="body">
      <div
        v-if="passwordModal.open"
        class="modal-backdrop"
        @click.self="closePasswordModal"
      >
        <form
          class="form-modal password-modal"
          @submit.prevent="submitPasswordReset"
        >
          <header class="modal-header">
            <div>
              <p>PASSWORD RESET</p>

              <h2>비밀번호 초기화</h2>

              <small>
                {{ passwordModal.adminName }}
                · {{ passwordModal.loginId }}
              </small>
            </div>

            <button
              type="button"
              :disabled="passwordSaving"
              @click="closePasswordModal"
            >
              ×
            </button>
          </header>

          <div class="modal-body">
            <label>
              <span>새 비밀번호 *</span>

              <input
                v-model="
                  passwordForm.newPassword
                "
                type="password"
                autocomplete="new-password"
                placeholder="4자 이상"
              />
            </label>

            <label>
              <span>비밀번호 확인 *</span>

              <input
                v-model="
                  passwordForm.passwordConfirm
                "
                type="password"
                autocomplete="new-password"
                placeholder="비밀번호 재입력"
              />
            </label>

            <div class="warning-note">
              초기화 후 해당 관리자는 새 비밀번호로 로그인해야 합니다.
            </div>
          </div>

          <footer class="modal-footer">
            <button
              type="button"
              class="cancel-button"
              :disabled="passwordSaving"
              @click="closePasswordModal"
            >
              취소
            </button>

            <button
              type="submit"
              class="password-save-button"
              :disabled="passwordSaving"
            >
              {{
                passwordSaving
                  ? '초기화 중...'
                  : '비밀번호 초기화'
              }}
            </button>
          </footer>
        </form>
      </div>
    </Teleport>
  </section>
</template>

<style scoped>
.security-page {
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

.page-message strong {
  display: grid;
  width: 22px;
  height: 22px;
  place-items: center;
  border-radius: 50%;
  color: white;
  background: currentColor;
}

.page-message p {
  flex: 1;
  margin: 0;
  font-size: 11px;
}

.page-message button {
  border: 0;
  cursor: pointer;
  color: inherit;
  font-size: 20px;
  background: transparent;
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
  margin: 12px 0 5px;
  font-size: 25px;
}

.summary-grid small {
  color: #969dab;
  font-size: 9px;
}

.summary-icon {
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

.pink {
  color: #d94f86;
  background: #ffe5f0;
}

.admin-panel {
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

.section-label {
  margin: 0;
  color: #725ee7;
  font-size: 8px;
  font-weight: 900;
}

.panel-header h2 {
  margin: 5px 0 0;
  font-size: 15px;
}

.panel-header p {
  margin: 5px 0 0;
  color: #9299a8;
  font-size: 9px;
}

.panel-actions {
  display: flex;
  gap: 8px;
}

.panel-actions input,
.panel-actions select {
  height: 36px;
  padding: 0 10px;
  border: 1px solid #dfe3ea;
  border-radius: 8px;
  font-size: 9px;
}

.create-button {
  height: 36px;
  padding: 0 14px;
  border: 0;
  border-radius: 8px;
  cursor: pointer;
  color: white;
  font-size: 9px;
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
  padding: 13px 14px;
  text-align: left;
  font-size: 9px;
}

th {
  color: #7e8696;
  background: #fafbfc;
}

td {
  border-top: 1px solid #edf0f4;
}

.admin-name {
  display: flex;
  gap: 9px;
  align-items: center;
}

.admin-name > span {
  display: grid;
  width: 37px;
  height: 37px;
  place-items: center;
  border-radius: 10px;
  color: #6756dc;
  font-weight: 900;
  background: #eeebff;
}

.admin-name strong,
.admin-name small {
  display: block;
}

.admin-name small {
  margin-top: 3px;
  color: #9aa1af;
  font-size: 8px;
}

.role-badge {
  display: inline-flex;
  padding: 5px 9px;
  border-radius: 20px;
  font-size: 8px;
  font-weight: 800;
}

.role-super {
  color: #d13d75;
  background: #ffe7f1;
}

.role-head {
  color: #6554dc;
  background: #edeaff;
}

.role-admin {
  color: #3978d4;
  background: #e4f0ff;
}

.status-button {
  min-width: 58px;
  height: 28px;
  border: 0;
  border-radius: 20px;
  cursor: pointer;
  color: #757d8b;
  font-size: 8px;
  font-weight: 800;
  background: #edf0f4;
}

.status-button.active {
  color: #168e61;
  background: #dff8ec;
}

.management-buttons {
  display: flex;
  gap: 6px;
}

.management-buttons button {
  height: 29px;
  padding: 0 9px;
  border-radius: 7px;
  cursor: pointer;
  font-size: 8px;
  font-weight: 750;
}

.edit-button {
  border: 1px solid #dcd7fb;
  color: #6756dc;
  background: #f3f1ff;
}

.password-button {
  border: 1px solid #ffd7e6;
  color: #d8457d;
  background: #fff0f6;
}

.empty-cell {
  height: 160px;
  text-align: center;
  color: #9097a6;
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
  min-height: 240px;
  place-items: center;
  color: #9097a6;
  font-size: 10px;
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
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(25, 29, 42, 0.52);
}

.form-modal {
  overflow-y: auto;
  width: 100%;
  max-width: 540px;
  max-height: calc(100vh - 48px);
  border-radius: 18px;
  background: white;
}

.password-modal {
  max-width: 460px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  padding: 21px 22px;
  border-bottom: 1px solid #ebedf1;
}

.modal-header p {
  margin: 0;
  color: #725ee7;
  font-size: 8px;
  font-weight: 900;
}

.modal-header h2 {
  margin: 5px 0 0;
}

.modal-header small {
  display: block;
  margin-top: 5px;
  color: #8c94a3;
  font-size: 9px;
}

.modal-header button {
  border: 0;
  cursor: pointer;
  font-size: 24px;
  background: transparent;
}

.modal-body {
  display: grid;
  gap: 16px;
  padding: 22px;
}

.modal-body label {
  display: grid;
  gap: 7px;
}

.modal-body label > span {
  font-size: 10px;
  font-weight: 750;
}

.modal-body input,
.modal-body select {
  width: 100%;
  height: 42px;
  padding: 0 11px;
  border: 1px solid #dfe3ea;
  border-radius: 9px;
}

.security-note,
.warning-note {
  padding: 13px;
  border-radius: 9px;
  font-size: 9px;
}

.security-note {
  border: 1px solid #ddd8fa;
  color: #625a8f;
  background: #f6f4ff;
}

.security-note p {
  margin: 5px 0 0;
}

.warning-note {
  border: 1px solid #ffd8e5;
  color: #be426d;
  background: #fff2f7;
}

.modal-footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding: 15px 22px 20px;
}

.modal-footer button {
  height: 39px;
  padding: 0 15px;
  border-radius: 9px;
  cursor: pointer;
  font-size: 9px;
  font-weight: 800;
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

.password-save-button {
  border: 0;
  color: white;
  background: #d94c81;
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
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .panel-actions input,
  .panel-actions select,
  .create-button {
    width: 100%;
  }
}
</style>