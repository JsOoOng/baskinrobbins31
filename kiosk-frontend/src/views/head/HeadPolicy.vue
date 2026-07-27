<!--
  [화면 흐름 안내] HeadPolicy
  역할: 본사 관리에서 사용자가 보는 화면이다.
  진입: /head/policies -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> props·Pinia·상위 화면 상태 -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
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
            <div class="content-label-row">
              <label>내용</label>
              <button type="button" class="btn-example" @click="applyExample">
                {{ formData.type === 'TERMS_OF_SERVICE' ? '이용약관 예시 불러오기' : '개인정보 방침 예시 불러오기' }}
              </button>
            </div>
            <textarea v-model="formData.content" rows="15" required placeholder="약관 내용을 입력하세요..."></textarea>
            <p class="example-note">예시는 운영 전 담당 부서의 검토가 필요한 초안입니다.</p>
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

const policyExamples = {
  TERMS_OF_SERVICE: `제1조 (목적)
본 약관은 회사가 제공하는 키오스크 주문, 결제 및 멤버십 서비스의 이용 조건과 절차를 정함을 목적으로 합니다.

제2조 (서비스 이용)
이용자는 화면에 표시된 상품, 수량, 옵션 및 결제금액을 확인한 후 주문을 확정해야 합니다. 매장 재고 상황에 따라 일부 상품의 판매가 제한될 수 있습니다.

제3조 (결제 및 주문 취소)
결제 완료 전에는 주문 내용을 변경하거나 취소할 수 있습니다. 결제 완료 후의 취소·환불은 해당 매장의 운영 기준과 관련 법령에 따라 처리됩니다.

제4조 (서비스 제한)
시스템 점검, 통신 장애, 천재지변 등 불가피한 사유가 있는 경우 서비스의 전부 또는 일부가 일시 중단될 수 있습니다.

제5조 (책임과 의무)
회사는 안정적인 서비스 제공을 위해 노력하며, 이용자는 타인의 결제수단이나 멤버십 정보를 부정하게 사용해서는 안 됩니다.

부칙
본 약관은 2026년 8월 1일부터 시행합니다.`,
  PRIVACY_POLICY: `1. 개인정보의 처리 목적
회사는 주문 처리, 결제 확인, 멤버십 포인트 적립·사용, 고객 문의 대응을 위해 필요한 범위에서 개인정보를 처리합니다.

2. 처리하는 개인정보 항목
필수 항목: 휴대전화번호, 주문·결제 내역, 멤버십 식별정보
자동 수집 항목: 서비스 이용 기록, 접속 일시, 기기 및 오류 기록

3. 개인정보의 보유 및 이용 기간
관련 법령에 별도 보존 의무가 있는 경우를 제외하고, 처리 목적 달성 후 지체 없이 파기합니다. 결제 및 계약 관련 기록은 관계 법령이 정한 기간 동안 보관할 수 있습니다.

4. 개인정보의 제3자 제공 및 처리위탁
회사는 결제 처리와 서비스 운영에 필요한 경우에 한해 결제대행사 등 수탁자에게 업무를 위탁하며, 법적 근거 또는 동의 없이 제3자에게 제공하지 않습니다.

5. 정보주체의 권리
이용자는 개인정보 열람, 정정, 삭제 및 처리정지를 요청할 수 있습니다. 요청은 고객센터 또는 매장 문의 채널을 통해 접수할 수 있습니다.

6. 안전성 확보 조치
회사는 접근 권한 관리, 전송구간 암호화, 접속기록 보관 등 개인정보 보호를 위한 기술적·관리적 조치를 시행합니다.

7. 개인정보 보호책임자
개인정보 관련 문의는 본사 고객지원 담당 부서로 접수할 수 있습니다.

본 방침은 2026년 8월 1일부터 시행합니다.`
};

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

const applyExample = () => {
  if (formData.value.content.trim() &&
      !confirm('작성 중인 내용을 예시로 바꾸시겠습니까?')) return;

  formData.value.content = policyExamples[formData.value.type];
  if (!formData.value.version) {
    formData.value.version = 'v1.0';
  }
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

.content-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 7px;
}

.btn-example {
  padding: 6px 10px;
  border: 1px solid #cfc8f5;
  border-radius: 6px;
  cursor: pointer;
  color: #5d4cc7;
  font-weight: 700;
  background: #f5f2ff;
}

.example-note {
  margin: 6px 0 0;
  color: #888;
  font-size: 12px;
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
