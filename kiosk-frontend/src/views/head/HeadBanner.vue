<!--
  [화면 흐름 안내] HeadBanner
  역할: 본사 관리에서 사용자가 보는 화면이다.
  진입: /head/banners -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/head/headBannerApi -> 응답/상태 반영
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
  createHeadBanner,
  deleteHeadBanner,
  extractBannerData,
  extractBannerErrorMessage,
  getHeadBanners,
  updateHeadBanner,
  updateHeadBannerActive
} from '@/api/head/headBannerApi'

import AppMessageToast
  from '@/components/common/AppMessageToast.vue'
import HeadTablePagination from '@/components/head/HeadTablePagination.vue'

const banners = ref([])

const loading = ref(false)
const saving = ref(false)
const updatingId = ref(null)
const deletingId = ref(null)

const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const activeFilter = ref('ALL')

const message = ref('')
const messageType = ref('success')

const modal = reactive({
  open: false,
  mode: 'create',
  bannerId: null
})

const form = reactive({
  title: '',
  imageUrl: '',
  isActive: true,
  startDate: '',
  endDate: ''
})

const normalizeBanner = (banner = {}) => {
  return {
    bannerId:
      banner.bannerId ??
      banner.id ??
      null,

    title:
      banner.title ??
      '제목 없는 배너',

    imageUrl:
      banner.imageUrl ??
      banner.image_url ??
      '',

    isActive:
      Boolean(
        banner.isActive ??
        banner.active ??
        false
      ),
    startDate: banner.startDate ?? '',
    endDate: banner.endDate ?? ''
  }
}

const filteredBanners = computed(() => {
  const keyword =
    searchKeyword.value
      .trim()
      .toLowerCase()

  return banners.value.filter((banner) => {
    const matchesKeyword =
      !keyword ||
      banner.title
        .toLowerCase()
        .includes(keyword) ||
      banner.imageUrl
        .toLowerCase()
        .includes(keyword) ||
      String(banner.bannerId)
        .includes(keyword)

    const matchesActive =
      activeFilter.value === 'ALL' ||
      (
        activeFilter.value === 'ACTIVE' &&
        banner.isActive
      ) ||
      (
        activeFilter.value === 'INACTIVE' &&
        !banner.isActive
      )

    return (
      matchesKeyword &&
      matchesActive
    )
  })
})
/*
 * 검색·상태 필터가 끝난 결과에서 현재 페이지에 표시할 행만 잘라
 * 공통 HeadTablePagination의 currentPage/pageSize와 연결합니다.
 */
const paginatedBanners = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredBanners.value.slice(start, start + pageSize.value)
})

const activeCount = computed(() => {
  return banners.value.filter(
    (banner) => banner.isActive
  ).length
})

const inactiveCount = computed(() => {
  return banners.value.filter(
    (banner) => !banner.isActive
  ).length
})

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

const removeBrokenImage = (event) => {
  event.currentTarget.remove()
}

const loadBanners = async () => {
  loading.value = true
  clearMessage()

  try {
    const responseBody =
      await getHeadBanners()

    const responseData =
      extractBannerData(responseBody)

    banners.value =
      Array.isArray(responseData)
        ? responseData.map(
            normalizeBanner
          )
        : []

  } catch (error) {
    showMessage(
      extractBannerErrorMessage(
        error,
        '배너 목록을 불러오지 못했습니다.'
      ),
      'error'
    )

  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.title = ''
  form.imageUrl = ''
  form.isActive = true
  form.startDate = ''
  form.endDate = ''
}

const openCreateModal = () => {
  resetForm()

  modal.mode = 'create'
  modal.bannerId = null
  modal.open = true
}

const openEditModal = (banner) => {
  form.title =
    banner.title === '제목 없는 배너'
      ? ''
      : banner.title

  form.imageUrl =
    banner.imageUrl

  form.isActive =
    banner.isActive
  form.startDate = banner.startDate?.slice(0, 16) ?? ''
  form.endDate = banner.endDate?.slice(0, 16) ?? ''

  modal.mode = 'edit'
  modal.bannerId =
    banner.bannerId

  modal.open = true
}

const closeModal = () => {
  if (saving.value) {
    return
  }

  modal.open = false
}

const validateForm = () => {
  if (!form.imageUrl.trim()) {
    showMessage(
      '배너 이미지 URL을 입력해주세요.',
      'error'
    )

    return false
  }

  if (form.title.length > 100) {
    showMessage(
      '배너 제목은 100자 이하여야 합니다.',
      'error'
    )

    return false
  }

  if (form.imageUrl.length > 255) {
    showMessage(
      '이미지 URL은 255자 이하여야 합니다.',
      'error'
    )

    return false
  }

  if (form.startDate && form.endDate && form.endDate <= form.startDate) {
    showMessage('종료일은 시작일보다 뒤여야 합니다.', 'error')
    return false
  }

  return true
}

const createPayload = () => {
  return {
    title:
      form.title.trim() || null,

    imageUrl:
      form.imageUrl.trim(),

    isActive:
      Boolean(form.isActive),
    startDate: form.startDate || null,
    endDate: form.endDate || null
  }
}

const submitBanner = async () => {
  if (!validateForm()) {
    return
  }

  saving.value = true
  clearMessage()

  try {
    const payload =
      createPayload()

    let successMessage = ''

    if (modal.mode === 'create') {
      await createHeadBanner(payload)

      successMessage =
        '배너가 등록되었습니다.'

    } else {
      await updateHeadBanner(
        modal.bannerId,
        payload
      )

      successMessage =
        '배너가 수정되었습니다.'
    }

    modal.open = false

    await loadBanners()

    /*
     * 목록 조회가 끝난 뒤 표시해야
     * clearMessage에 지워지지 않습니다.
     */
    showMessage(
      successMessage,
      'success'
    )

  } catch (error) {
    showMessage(
      extractBannerErrorMessage(
        error,
        modal.mode === 'create'
          ? '배너 등록에 실패했습니다.'
          : '배너 수정에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    saving.value = false
  }
}

const toggleActive = async (banner) => {
  const nextActive =
    !banner.isActive

  updatingId.value =
    banner.bannerId

  clearMessage()

  try {
    await updateHeadBannerActive(
      banner.bannerId,
      nextActive
    )

    banner.isActive =
      nextActive

    showMessage(
      nextActive
        ? '배너가 노출 상태로 변경되었습니다.'
        : '배너가 숨김 상태로 변경되었습니다.'
    )

  } catch (error) {
    showMessage(
      extractBannerErrorMessage(
        error,
        '배너 노출 상태 변경에 실패했습니다.'
      ),
      'error'
    )

  } finally {
    updatingId.value = null
  }
}

const removeBanner = async (banner) => {
  const confirmed = window.confirm(
    `"${banner.title}" 배너를 삭제하시겠습니까?\n삭제 후 복구할 수 없습니다.`
  )

  if (!confirmed) {
    return
  }

  deletingId.value =
    banner.bannerId

  clearMessage()

  try {
    await deleteHeadBanner(
      banner.bannerId
    )

    await loadBanners()

    showMessage(
      '배너가 삭제되었습니다.',
      'success'
    )

  } catch (error) {
    showMessage(
      extractBannerErrorMessage(
        error,
        '배너 삭제에 실패했습니다.'
      ),
      'error'
    )
  } finally {
    deletingId.value = null
  }
}

onMounted(() => {
  loadBanners()
})
</script>

<template>

  <AppMessageToast
    :message="message"
    :type="messageType"
    @close="clearMessage"
  />

  <section class="banner-page">
    <div class="summary-grid">
      <article>
        <div>
          <p>전체 배너</p>
          <strong>{{ banners.length }}</strong>
          <small>본사 등록 배너</small>
        </div>

        <span class="summary-icon purple">
          ▤
        </span>
      </article>

      <article>
        <div>
          <p>노출 중</p>
          <strong>{{ activeCount }}</strong>
          <small>키오스크 표시 배너</small>
        </div>

        <span class="summary-icon green">
          ✓
        </span>
      </article>

      <article>
        <div>
          <p>숨김</p>
          <strong>{{ inactiveCount }}</strong>
          <small>현재 미노출 배너</small>
        </div>

        <span class="summary-icon gray">
          −
        </span>
      </article>
    </div>

    <section class="banner-panel">
      <header class="panel-header">
        <div>
          <h2>배너 관리</h2>

          <p>
            고객 키오스크에 표시할 광고 이미지를 관리합니다.
          </p>
        </div>

        <div class="panel-actions">
          <input
            v-model="searchKeyword"
            type="search"
            placeholder="제목 또는 URL 검색"
          />

          <select v-model="activeFilter">
            <option value="ALL">
              전체 상태
            </option>

            <option value="ACTIVE">
              노출 중
            </option>

            <option value="INACTIVE">
              숨김
            </option>
          </select>

          <button
            type="button"
            class="create-button"
            @click="openCreateModal"
          >
            ＋ 배너 등록
          </button>
        </div>
      </header>

      <div
        v-if="loading"
        class="loading-area"
      >
        <span class="spinner" />

        <p>배너 목록을 불러오는 중입니다.</p>
      </div>

      <div
        v-else-if="filteredBanners.length > 0"
        class="banner-grid"
      >
        <article
          v-for="banner in paginatedBanners"
          :key="banner.bannerId"
          class="banner-card"
        >
          <div class="banner-image">
            <span class="image-fallback">
              BR BANNER
            </span>

            <img
              v-if="banner.imageUrl"
              :src="banner.imageUrl"
              :alt="banner.title"
              @error="removeBrokenImage"
            />

            <span
              class="status-badge"
              :class="{
                active: banner.isActive
              }"
            >
              {{
                banner.isActive
                  ? '노출 중'
                  : '숨김'
              }}
            </span>
          </div>

          <div class="banner-content">
            <div class="banner-title">
              <div>
                <strong>
                  {{ banner.title }}
                </strong>

                <small>
                  배너 ID #{{ banner.bannerId }}
                </small>
              </div>

              <button
                type="button"
                class="active-button"
                :class="{
                  active: banner.isActive
                }"
                :disabled="
                  updatingId === banner.bannerId
                "
                @click="toggleActive(banner)"
              >
                {{
                  updatingId === banner.bannerId
                    ? '변경 중'
                    : banner.isActive
                      ? '노출'
                      : '숨김'
                }}
              </button>
            </div>

            <p class="image-url">
              {{ banner.imageUrl }}
            </p>
            <p class="image-url">
              기간:
              {{ banner.startDate ? banner.startDate.slice(0, 16).replace('T', ' ') : '즉시' }}
              ~
              {{ banner.endDate ? banner.endDate.slice(0, 16).replace('T', ' ') : '계속' }}
            </p>

            <div class="card-actions">
              <button
                type="button"
                class="edit-button"
                @click="openEditModal(banner)"
              >
                수정
              </button>

              <button
                type="button"
                class="delete-button"
                :disabled="
                  deletingId === banner.bannerId
                "
                @click="removeBanner(banner)"
              >
                {{
                  deletingId === banner.bannerId
                    ? '삭제 중'
                    : '삭제'
                }}
              </button>
            </div>
          </div>
        </article>
      </div>

      <div
        v-else
        class="empty-area"
      >
        <strong>
          조회된 배너가 없습니다.
        </strong>

        <p>
          검색 조건을 변경하거나 새로운 배너를 등록해주세요.
        </p>
      </div>

      <HeadTablePagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total-items="filteredBanners.length"
      />

      <footer class="panel-footer">
        <span>
          전체 {{ banners.length }}개 중
          {{ filteredBanners.length }}개 표시
        </span>

        <button
          type="button"
          @click="loadBanners"
        >
          새로고침
        </button>
      </footer>
    </section>

    <Teleport to="body">
      <div
        v-if="modal.open"
        class="modal-backdrop"
        @click.self="closeModal"
      >
        <form
          class="banner-modal"
          @submit.prevent="submitBanner"
        >
          <header class="modal-header">
            <div>
              <p>BANNER MANAGEMENT</p>

              <h2>
                {{
                  modal.mode === 'create'
                    ? '배너 등록'
                    : '배너 수정'
                }}
              </h2>
            </div>

            <button
              type="button"
              :disabled="saving"
              @click="closeModal"
            >
              ×
            </button>
          </header>

          <div class="modal-body">
            <label>
              <span>관리용 제목</span>

              <input
                v-model="form.title"
                type="text"
                maxlength="100"
                placeholder="예: 여름 신메뉴 출시 배너"
                :disabled="saving"
              />
            </label>

            <label>
              <span>이미지 URL *</span>

              <input
                v-model="form.imageUrl"
                type="text"
                maxlength="255"
                placeholder="https://example.com/banner.jpg"
                :disabled="saving"
              />
            </label>

            <label>
              <span>노출 시작일</span>
              <input
                v-model="form.startDate"
                type="datetime-local"
                :disabled="saving"
              />
            </label>

            <label>
              <span>노출 종료일</span>
              <input
                v-model="form.endDate"
                type="datetime-local"
                :disabled="saving"
              />
            </label>

            <div
              v-if="form.imageUrl"
              class="image-preview"
            >
              <span>이미지 미리보기</span>

              <div>
                <strong>BR BANNER</strong>

                <img
                  :src="form.imageUrl"
                  alt="배너 미리보기"
                  @error="removeBrokenImage"
                />
              </div>
            </div>

            <label class="active-field">
              <div>
                <span>키오스크 화면 노출</span>

                <small>
                  활성화하면 고객 키오스크에 배너가 표시됩니다.
                </small>
              </div>

              <button
                type="button"
                class="form-toggle"
                :class="{
                  active: form.isActive
                }"
                @click="
                  form.isActive =
                    !form.isActive
                "
              >
                {{
                  form.isActive
                    ? '노출'
                    : '숨김'
                }}
              </button>
            </label>
          </div>

          <footer class="modal-footer">
            <button
              type="button"
              class="cancel-button"
              :disabled="saving"
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
                    ? '배너 등록'
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
.banner-page {
  display: grid;
  gap: 18px;
}

.summary-grid {
  display: grid;
  grid-template-columns:
    repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.summary-grid article {
  display: flex;
  justify-content: space-between;
  min-height: 125px;
  padding: 18px;
  border: 1px solid #e3e6ed;
  border-radius: 15px;
  background: #ffffff;
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

.banner-panel {
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
  color: #ffffff;
  font-size: 10px;
  font-weight: 800;
  background: #725ee7;
}

.banner-grid {
  display: grid;
  grid-template-columns:
    repeat(3, minmax(0, 1fr));
  gap: 16px;
  padding: 18px;
}

.banner-card {
  overflow: hidden;
  border: 1px solid #e3e6ed;
  border-radius: 13px;
  background: #ffffff;
}

.banner-image {
  position: relative;
  display: grid;
  overflow: hidden;
  height: 145px;
  place-items: center;
  background:
    linear-gradient(
      135deg,
      #f3d9e7,
      #ddd8fb
    );
}

.banner-image img {
  position: absolute;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-fallback {
  color: #775fca;
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 1px;
}

.status-badge {
  position: absolute;
  z-index: 2;
  top: 10px;
  right: 10px;
  padding: 5px 9px;
  border-radius: 20px;
  color: #707887;
  font-size: 9px;
  font-weight: 800;
  background: #edf0f4;
}

.status-badge.active {
  color: #168e61;
  background: #dff8ec;
}

.banner-content {
  padding: 14px;
}

.banner-title {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  justify-content: space-between;
}

.banner-title strong,
.banner-title small {
  display: block;
}

.banner-title strong {
  font-size: 12px;
}

.banner-title small {
  margin-top: 4px;
  color: #9aa1af;
  font-size: 9px;
}

.active-button {
  min-width: 52px;
  height: 27px;
  border: 0;
  border-radius: 20px;
  cursor: pointer;
  color: #737b89;
  font-size: 9px;
  font-weight: 800;
  background: #edf0f4;
}

.active-button.active {
  color: #168e61;
  background: #dff8ec;
}

.image-url {
  overflow: hidden;
  margin: 13px 0;
  color: #8d94a2;
  font-size: 9px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-actions {
  display: flex;
  gap: 7px;
}

.card-actions button {
  flex: 1;
  height: 31px;
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

.empty-area,
.loading-area {
  display: grid;
  min-height: 260px;
  place-items: center;
  text-align: center;
  color: #9097a6;
}

.empty-area p {
  margin-top: 8px;
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
  background: #ffffff;
}

.modal-backdrop {
  position: fixed;
  z-index: 3000;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(25, 29, 42, 0.52);
  backdrop-filter: blur(4px);
}

.banner-modal {
  width: 100%;
  max-width: 560px;
  border-radius: 18px;
  background: #ffffff;
  box-shadow:
    0 28px 70px rgba(23, 27, 43, 0.22);
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
  font-size: 9px;
  font-weight: 900;
}

.modal-header h2 {
  margin: 4px 0 0;
}

.modal-header button {
  border: 0;
  cursor: pointer;
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

.image-preview > span {
  display: block;
  margin-bottom: 8px;
  font-size: 11px;
  font-weight: 750;
}

.image-preview > div {
  position: relative;
  display: grid;
  overflow: hidden;
  height: 150px;
  place-items: center;
  border-radius: 10px;
  color: #775fca;
  background:
    linear-gradient(
      135deg,
      #f3d9e7,
      #ddd8fb
    );
}

.image-preview img {
  position: absolute;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.active-field {
  display: flex !important;
  align-items: center;
  justify-content: space-between;
  padding: 14px;
  border: 1px solid #e1e4ea;
  border-radius: 10px;
  background: #f9fafc;
}

.active-field small {
  display: block;
  margin-top: 4px;
  color: #969dab;
  font-size: 9px;
}

.form-toggle {
  min-width: 62px;
  height: 31px;
  border: 0;
  border-radius: 20px;
  cursor: pointer;
  color: #737b89;
  font-size: 9px;
  font-weight: 800;
  background: #edf0f4;
}

.form-toggle.active {
  color: #168e61;
  background: #dff8ec;
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
  background: #ffffff;
}

.save-button {
  border: 0;
  color: #ffffff;
  background: #725ee7;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1050px) {
  .banner-grid,
  .summary-grid {
    grid-template-columns:
      repeat(2, minmax(0, 1fr));
  }

  .panel-header {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 650px) {
  .banner-grid,
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .panel-actions {
    width: 100%;
    flex-direction: column;
  }

  .panel-actions input,
  .panel-actions select,
  .create-button {
    width: 100%;
  }
}
</style>
