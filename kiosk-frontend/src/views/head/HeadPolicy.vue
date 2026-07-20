<template>
  <div class="head-policy-container">
    <div class="header-section">
      <h2>📜 약관 및 방침 관리</h2>
      <button class="btn-primary" @click="openModal()">+ 새 버전 등록</button>
    </div>

    <!-- 탭 네비게이션 -->
    <div class="tab-nav">
      <button 
        :class="{ active: currentTab === 'TERMS_OF_SERVICE' }" 
        @click="currentTab = 'TERMS_OF_SERVICE'">이용약관</button>
      <button 
        :class="{ active: currentTab === 'PRIVACY_POLICY' }" 
        @click="currentTab = 'PRIVACY_POLICY'">개인정보 처리방침</button>
    </div>

    <div v-if="policyStore.error" class="error-msg">
      {{ policyStore.error }}
    </div>

    <div class="table-container">
      <table class="policy-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>타입</th>
            <th>버전명</th>
            <th>생성일</th>
            <th>적용 상태</th>
            <th>관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="policyStore.isLoading">
            <td colspan="6" class="text-center">로딩 중...</td>
          </tr>
          <tr v-else-if="filteredPolicies.length === 0">
            <td colspan="6" class="text-center">등록된 약관이 없습니다.</td>
          </tr>
          <tr v-else v-for="policy in filteredPolicies" :key="policy.policyId">
            <td>{{ policy.policyId }}</td>
            <td>
              <span class="type-badge" :class="policy.type.toLowerCase()">
                {{ policy.type === 'TERMS_OF_SERVICE' ? '이용약관' : '개인정보 방침' }}
              </span>
            </td>
            <td class="version-name">{{ policy.version }}</td>
            <td>{{ formatDate(policy.createdAt) }}</td>
            <td>
              <label class="switch">
                <input 
                  type="checkbox" 
                  :checked="policy.isActive" 
                  @change="toggleActive(policy)"
                >
                <span class="slider round"></span>
              </label>
              <span v-if="policy.isActive" class="active-text">활성(적용중)</span>
            </td>
            <td>
              <button class="btn-sm btn-edit" @click="openModal(policy)">수정</button>
              <button class="btn-sm btn-delete" @click="deletePolicy(policy.policyId)">삭제</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 약관 등록/수정 모달 -->
    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content modal-lg">
        <h3>{{ isEditMode ? '약관/방침 수정' : '새 약관/방침 등록' }}</h3>
        <form @submit.prevent="submitPolicy">
          <div class="form-row">
            <div class="form-group half">
              <label>타입</label>
              <select v-model="formData.type" required :disabled="isEditMode">
                <option value="TERMS_OF_SERVICE">이용약관</option>
                <option value="PRIVACY_POLICY">개인정보 처리방침</option>
              </select>
            </div>
            <div class="form-group half">
              <label>버전 (ex: v1.0, 2026-07-20)</label>
              <input type="text" v-model="formData.version" required />
            </div>
          </div>
          
          <div class="form-group">
            <label>내용</label>
            <textarea v-model="formData.content" rows="15" required placeholder="약관 내용을 입력하세요..."></textarea>
          </div>

          <div class="form-group checkbox-group">
            <input type="checkbox" id="isActiveCheck" v-model="formData.isActive" />
            <label for="isActiveCheck">저장 즉시 활성화(적용)하기 (기존 버전은 비활성화됨)</label>
          </div>
          
          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="closeModal">취소</button>
            <button type="submit" class="btn-submit">저장</button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useHeadPolicyStore } from '@/stores/head/headPolicyStore';

const policyStore = useHeadPolicyStore();

const currentTab = ref('TERMS_OF_SERVICE');
const isModalOpen = ref(false);
const isEditMode = ref(false);
const currentPolicyId = ref(null);

const defaultForm = {
  type: 'TERMS_OF_SERVICE',
  version: '',
  content: '',
  isActive: false
};

const formData = ref({ ...defaultForm });

onMounted(() => {
  policyStore.fetchPolicies();
});

const filteredPolicies = computed(() => {
  return policyStore.policies.filter(p => p.type === currentTab.value)
          .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
});

const formatDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR');
};

const openModal = (policy = null) => {
  if (policy) {
    isEditMode.value = true;
    currentPolicyId.value = policy.policyId;
    formData.value = {
      type: policy.type,
      version: policy.version,
      content: policy.content,
      isActive: policy.isActive
    };
  } else {
    isEditMode.value = false;
    currentPolicyId.value = null;
    formData.value = { ...defaultForm, type: currentTab.value };
  }
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

const submitPolicy = async () => {
  try {
    if (isEditMode.value) {
      await policyStore.updatePolicy(currentPolicyId.value, formData.value);
    } else {
      await policyStore.createPolicy(formData.value);
    }
    closeModal();
  } catch (error) {
    alert('저장 중 오류가 발생했습니다.');
  }
};

const deletePolicy = async (policyId) => {
  if (confirm('정말로 이 약관을 삭제하시겠습니까?')) {
    await policyStore.deletePolicy(policyId);
  }
};

const toggleActive = async (policy) => {
  if (!policy.isActive) {
    const isConfirm = confirm('이 버전을 활성화하면, 기존에 활성화된 동일 타입의 약관은 비활성화됩니다. 계속하시겠습니까?');
    if (!isConfirm) {
      // Re-fetch to reset checkbox state in UI since it's optimistic
      policyStore.fetchPolicies();
      return;
    }
  }
  
  try {
    await policyStore.updateActiveStatus(policy.policyId, !policy.isActive);
  } catch (error) {
    alert('상태 변경에 실패했습니다.');
    policyStore.fetchPolicies();
  }
};
</script>

<style scoped>
.head-policy-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-section h2 {
  margin: 0;
  color: #333;
}

.btn-primary {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  font-weight: bold;
}

.btn-primary:hover {
  background-color: #45a049;
}

.tab-nav {
  display: flex;
  border-bottom: 2px solid #eee;
  margin-bottom: 20px;
}

.tab-nav button {
  padding: 10px 20px;
  border: none;
  background: none;
  font-size: 1.1em;
  font-weight: bold;
  color: #888;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
}

.tab-nav button.active {
  color: #333;
  border-bottom: 3px solid #4CAF50;
}

.tab-nav button:hover {
  color: #555;
}

.table-container {
  overflow-x: auto;
}

.policy-table {
  width: 100%;
  border-collapse: collapse;
}

.policy-table th, .policy-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.policy-table th {
  background-color: #f8f9fa;
  font-weight: bold;
  color: #555;
}

.version-name {
  font-weight: bold;
  color: #2c3e50;
}

.text-center {
  text-align: center !important;
}

.btn-sm {
  padding: 5px 10px;
  margin-right: 5px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}

.btn-edit {
  background-color: #2196F3;
  color: white;
}

.btn-delete {
  background-color: #f44336;
  color: white;
}

.type-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.85em;
  font-weight: bold;
}
.type-badge.terms_of_service { background-color: #00bcd4; color: white; }
.type-badge.privacy_policy { background-color: #9c27b0; color: white; }

/* Switch for toggle */
.switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 20px;
  vertical-align: middle;
}
.switch input { opacity: 0; width: 0; height: 0; }
.slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: #ccc;
  transition: .4s;
}
.slider:before {
  position: absolute;
  content: "";
  height: 16px; width: 16px;
  left: 2px; bottom: 2px;
  background-color: white;
  transition: .4s;
}
input:checked + .slider { background-color: #4CAF50; }
input:checked + .slider:before { transform: translateX(20px); }
.slider.round { border-radius: 20px; }
.slider.round:before { border-radius: 50%; }

.active-text {
  margin-left: 10px;
  color: #4CAF50;
  font-weight: bold;
  font-size: 0.9em;
  vertical-align: middle;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 10px;
  width: 500px;
  max-width: 90%;
}
.modal-content.modal-lg {
  width: 800px;
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.form-group {
  margin-bottom: 15px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.half {
  flex: 1;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  font-size: 0.9em;
}

.form-group input[type="text"],
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f9f9f9;
  padding: 10px;
  border-radius: 5px;
  border: 1px dashed #ddd;
}
.checkbox-group label {
  margin-bottom: 0;
  color: #d32f2f;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.btn-cancel {
  background-color: #ddd;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
}

.btn-submit {
  background-color: #2196F3;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
}
</style>
