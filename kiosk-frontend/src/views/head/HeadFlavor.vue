<!--
  [화면 흐름 안내] HeadFlavor
  역할: 본사 관리에서 사용자가 보는 화면이다.
  진입: /head/flavors -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/head/headFlavorApi -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<script setup>
import { onMounted, reactive, ref, computed, watch } from 'vue'

import {
  createHeadFlavor,
  getHeadFlavors,
  updateHeadFlavor
} from '@/api/head/headFlavorApi'

import AppMessageToast from '@/components/common/AppMessageToast.vue'
import HeadTablePagination from '@/components/head/HeadTablePagination.vue'

const flavors = ref([])
const loading = ref(false)
const saving = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
/*
 * 맛 이름·영문명·ID를 하나의 검색어로 검사해
 * 등록된 맛 원본 목록에서 화면 표시 대상만 만듭니다.
 */
const filteredFlavors = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return flavors.value
  return flavors.value.filter((flavor) =>
    [flavor.flavorName, flavor.name, flavor.imageUrl, flavor.image]
      .some((value) => String(value ?? '').toLowerCase().includes(keyword))
  )
})
/*
 * 검색된 맛 목록 중 현재 페이지에 해당하는 행만 표에 전달합니다.
 */
const paginatedFlavors = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredFlavors.value.slice(start, start + pageSize.value)
})
watch([searchKeyword, pageSize], () => { currentPage.value = 1 })

const message = ref('')
const messageType = ref('success')

const formModal = reactive({
  open: false,
  mode: 'create', // 'create' | 'edit'
  flavorId: null
})

const form = reactive({
  flavorName: '',
  isActive: true,
  imageFile: null
})

const fileInputRef = ref(null)

const showMessage = (text, type = 'success') => {
  message.value = text
  messageType.value = type
}

const clearMessage = () => {
  message.value = ''
}

const loadFlavors = async () => {
  loading.value = true
  try {
    const response = await getHeadFlavors()
    const flavorList = response.data || response.result || response || []
    flavors.value = Array.isArray(flavorList) ? flavorList : []
  } catch (error) {
    showMessage('맛 목록을 불러오지 못했습니다.', 'error')
  } finally {
    loading.value = false
  }
}

const openCreateModal = () => {
  form.flavorName = ''
  form.isActive = true
  form.imageFile = null
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
  
  formModal.mode = 'create'
  formModal.flavorId = null
  formModal.open = true
}

const openEditModal = (flavor) => {
  form.flavorName = flavor.flavorName || flavor.name || ''
  form.isActive = flavor.isActive !== undefined ? flavor.isActive : (flavor.active !== undefined ? flavor.active : true)
  form.imageFile = null
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
  
  formModal.mode = 'edit'
  formModal.flavorId = flavor.flavorId || flavor.id
  formModal.open = true
}

const closeFormModal = () => {
  if (saving.value) return
  formModal.open = false
}

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (!file) {
    form.imageFile = null
    return
  }
  form.imageFile = file
}

const submitFlavor = async () => {
  if (!form.flavorName.trim()) {
    showMessage('맛 이름을 입력해주세요.', 'error')
    return
  }
  
  if (formModal.mode === 'create' && !form.imageFile) {
    showMessage('이미지 파일을 등록해주세요.', 'error')
    return
  }
  
  saving.value = true
  clearMessage()
  
  try {
    const formData = new FormData()
    formData.append('flavorName', form.flavorName.trim())
    formData.append('isActive', form.isActive)
    
    if (form.imageFile) {
      formData.append('imageFile', form.imageFile)
    }
    
    if (formModal.mode === 'create') {
      await createHeadFlavor(formData)
      showMessage('맛이 등록되었습니다.', 'success')
    } else {
      await updateHeadFlavor(formModal.flavorId, formData)
      showMessage('맛 정보가 수정되었습니다.', 'success')
    }
    
    formModal.open = false
    await loadFlavors()
  } catch (error) {
    showMessage('요청 처리에 실패했습니다.', 'error')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadFlavors()
})
</script>

<template>
  <AppMessageToast
    :message="message"
    :type="messageType"
    @close="clearMessage"
  />

  <div class="head-flavor-page">
    <header class="page-header">
      <div class="header-content">
        <h1>아이스크림 관리</h1>
        <p>아이스크림 맛을 등록하고 정보를 수정합니다.</p>
      </div>

      <div class="header-actions">
        <input v-model="searchKeyword" class="table-search" type="search" placeholder="맛 이름·이미지 경로 검색" />
        <button
          type="button"
          class="create-button"
          @click="openCreateModal"
        >
          <span>+</span> 맛 등록
        </button>
      </div>
    </header>

    <section class="flavor-content">
      <div v-if="loading" class="loading-state">
        <p>목록을 불러오는 중입니다...</p>
      </div>

      <div v-else-if="flavors.length === 0" class="empty-state">
        <p>등록된 아이스크림 맛이 없습니다.</p>
      </div>

      <div v-else class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th>이미지</th>
              <th>맛 이름</th>
              <th>노출 상태</th>
              <th>이미지 경로</th>
              <th>관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="flavor in paginatedFlavors" :key="flavor.flavorId || flavor.id">
              <td class="td-image">
                <img :src="flavor.imageUrl || flavor.image" :alt="flavor.flavorName || flavor.name" class="flavor-img" />
              </td>
              <td>
                <strong>{{ flavor.flavorName || flavor.name }}</strong>
              </td>
              <td>
                <span :class="['status-badge', (flavor.isActive !== undefined ? flavor.isActive : flavor.active) ? 'status-active' : 'status-inactive']">
                  {{ (flavor.isActive !== undefined ? flavor.isActive : flavor.active) ? '노출' : '숨김' }}
                </span>
              </td>
              <td class="td-url">
                <code>{{ flavor.imageUrl || flavor.image }}</code>
              </td>
              <td>
                <button
                  type="button"
                  class="edit-button"
                  @click="openEditModal(flavor)"
                >
                  수정
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <HeadTablePagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total-items="filteredFlavors.length"
        />
      </div>
    </section>

    <!-- 모달 -->
    <div
      v-if="formModal.open"
      class="modal-overlay"
      @click.self="closeFormModal"
    >
      <div class="modal-content">
        <header class="modal-header">
          <h2>{{ formModal.mode === 'create' ? '아이스크림 맛 등록' : '아이스크림 맛 수정' }}</h2>
          <button type="button" class="close-button" @click="closeFormModal">×</button>
        </header>

        <div class="modal-body">
          <label class="form-field">
            <span>맛 이름 <strong>*</strong></span>
            <input type="text" v-model="form.flavorName" placeholder="예: 엄마는 외계인" maxlength="50" :disabled="saving" />
          </label>

          <label class="form-field">
            <span>이미지 파일 {{ formModal.mode === 'create' ? '<strong>*</strong>' : '(변경 시에만 선택)' }}</span>
            <input type="file" accept="image/*" @change="handleFileChange" ref="fileInputRef" :disabled="saving" />
          </label>
          
          <label class="form-field toggle-field">
            <span>고객 화면 노출 상태</span>
            <label class="toggle-switch">
              <input type="checkbox" v-model="form.isActive" :disabled="saving" />
              <span class="toggle-slider"></span>
            </label>
            <span class="toggle-text">{{ form.isActive ? '노출' : '숨김' }}</span>
          </label>
        </div>

        <footer class="modal-footer">
          <button type="button" class="cancel-button" @click="closeFormModal" :disabled="saving">취소</button>
          <button type="button" class="submit-button" @click="submitFlavor" :disabled="saving">
            {{ saving ? '저장 중...' : '저장' }}
          </button>
        </footer>
      </div>
    </div>
  </div>
</template>

<style scoped>
.head-flavor-page {
  padding: 30px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}
.page-header h1 {
  font-size: 24px;
  font-weight: 800;
  color: #1a1f36;
  margin: 0 0 8px;
}
.page-header p {
  font-size: 14px;
  color: #656f85;
  margin: 0;
}
.header-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}
.header-actions .table-search {
  margin: 0;
}
.create-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 8px;
  background: #3a2b99;
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
}
.create-button:hover {
  background: #2b1f7a;
}
.table-container {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.03);
  overflow: hidden;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table th, .data-table td {
  padding: 16px 20px;
  text-align: left;
  border-bottom: 1px solid #f0f2f5;
  font-size: 14px;
}
.data-table th {
  background: #f8f9fc;
  color: #4a5568;
  font-weight: 700;
  white-space: nowrap;
}
.td-image {
  width: 80px;
}
.flavor-img {
  width: 60px;
  height: 60px;
  object-fit: contain;
  border-radius: 8px;
  background: #f8f9fc;
  border: 1px solid #e2e8f0;
}
.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
}
.status-active {
  background: #e6f6ed;
  color: #1f9d55;
}
.status-inactive {
  background: #fcedf0;
  color: #d12953;
}
.td-url code {
  background: #f1f3f5;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #495057;
}
.edit-button {
  padding: 6px 12px;
  border: 1px solid #cbd5e0;
  border-radius: 6px;
  background: #ffffff;
  color: #4a5568;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.edit-button:hover {
  background: #f8f9fc;
}
.empty-state, .loading-state {
  padding: 40px;
  text-align: center;
  color: #718096;
  background: #ffffff;
  border-radius: 12px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  background: #ffffff;
  border-radius: 12px;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}
.modal-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1a202c;
}
.close-button {
  background: transparent;
  border: none;
  font-size: 24px;
  color: #a0aec0;
  cursor: pointer;
}
.modal-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.form-field span {
  font-size: 13px;
  font-weight: 700;
  color: #4a5568;
}
.form-field strong {
  color: #e53e3e;
}
.form-field input[type="text"], .form-field input[type="file"] {
  padding: 10px 12px;
  border: 1px solid #cbd5e0;
  border-radius: 8px;
  font-size: 14px;
}
.toggle-field {
  flex-direction: row;
  align-items: center;
  gap: 12px;
}
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
}
.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}
.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: #cbd5e0;
  transition: .2s;
  border-radius: 24px;
}
.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .2s;
  border-radius: 50%;
}
input:checked + .toggle-slider {
  background-color: #38b2ac;
}
input:checked + .toggle-slider:before {
  transform: translateX(20px);
}
.toggle-text {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
}
.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background: #f8f9fc;
  border-radius: 0 0 12px 12px;
}
.cancel-button {
  padding: 10px 16px;
  background: #ffffff;
  border: 1px solid #cbd5e0;
  border-radius: 8px;
  color: #4a5568;
  font-weight: 600;
  cursor: pointer;
}
.submit-button {
  padding: 10px 20px;
  background: #3a2b99;
  border: none;
  border-radius: 8px;
  color: #ffffff;
  font-weight: 600;
  cursor: pointer;
}
.submit-button:disabled {
  background: #a0aec0;
  cursor: not-allowed;
}
</style>
