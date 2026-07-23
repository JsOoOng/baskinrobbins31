<!--
  [화면 흐름 안내] HeadSettings
  역할: 본사 관리에서 사용자가 보는 화면이다.
  진입: /head/settings -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> props·Pinia·상위 화면 상태 -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<template>
  <div class="head-settings-container">
    <div class="header-section">
      <h2>⚙️ 시스템 설정</h2>
      <p>본사 시스템 및 키오스크 공통 설정을 관리합니다.</p>
    </div>

    <div v-if="settingsStore.error" class="error-msg">
      {{ settingsStore.error }}
    </div>
    
    <div v-if="settingsStore.isSaved" class="success-msg">
      ✓ 설정이 성공적으로 저장되었습니다.
    </div>

    <div class="tabs">
      <button 
        class="tab-btn" 
        :class="{ active: activeTab === 'GENERAL' }" 
        @click="activeTab = 'GENERAL'"
      >
        일반 설정
      </button>
      <button 
        class="tab-btn" 
        :class="{ active: activeTab === 'ORDER' }" 
        @click="activeTab = 'ORDER'"
      >
        주문 / 결제 설정
      </button>
    </div>

    <div class="settings-content" v-if="!settingsStore.isLoading">
      <form @submit.prevent="saveSettings">
        
        <!-- 일반 설정 탭 -->
        <div v-show="activeTab === 'GENERAL'" class="tab-pane">
          <div class="setting-item">
            <div class="setting-info">
              <h3>음성 안내 사용</h3>
              <p>키오스크 터치 및 결제 단계에서 음성 안내를 송출합니다.</p>
            </div>
            <div class="setting-control">
              <label class="switch">
                <input type="checkbox" v-model="formData.useVoiceGuide">
                <span class="slider round"></span>
              </label>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <h3>영수증 기본 출력 방식</h3>
              <p>결제 완료 시 영수증 출력 기본값을 설정합니다.</p>
            </div>
            <div class="setting-control">
              <select v-model="formData.receiptPrintMode">
                <option value="ALWAYS">무조건 출력</option>
                <option value="NEVER">출력 안 함</option>
                <option value="SELECT">고객 선택 (버튼 노출)</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 주문 / 결제 설정 탭 -->
        <div v-show="activeTab === 'ORDER'" class="tab-pane">
          <div class="setting-item">
            <div class="setting-info">
              <h3>다회용기(텀블러) 할인 금액</h3>
              <p>개인 컵 사용을 선택한 고객에게 적용할 기본 할인 금액(원)입니다.</p>
            </div>
            <div class="setting-control">
              <div class="input-with-unit">
                <input type="number" v-model="formData.tumblerDiscountAmount" min="0" step="100" />
                <span>원</span>
              </div>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <h3>품절 임박 알림 기준</h3>
              <p>재고 수량이 이 값 이하로 내려가면 '품절 임박' 뱃지가 표시됩니다.</p>
            </div>
            <div class="setting-control">
              <div class="input-with-unit">
                <input type="number" v-model="formData.lowStockAlertCount" min="1" max="50" />
                <span>개 이하</span>
              </div>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <h3>간편결제 사용</h3>
              <p>카카오페이, 네이버페이, 페이코 등 간편결제 수단을 활성화합니다.</p>
            </div>
            <div class="setting-control">
              <label class="switch">
                <input type="checkbox" v-model="formData.useEasyPay">
                <span class="slider round"></span>
              </label>
            </div>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" class="btn-cancel" @click="resetForm">변경 취소</button>
          <button type="submit" class="btn-primary">설정 저장하기</button>
        </div>
      </form>
    </div>
    <div class="loading-overlay" v-else>
      설정을 불러오는 중입니다...
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useHeadSettingsStore } from '@/stores/head/headSettingsStore';

const settingsStore = useHeadSettingsStore();
const activeTab = ref('GENERAL');

const formData = ref({
  useVoiceGuide: true,
  receiptPrintMode: 'SELECT',
  tumblerDiscountAmount: 300,
  lowStockAlertCount: 5,
  useEasyPay: true
});

onMounted(async () => {
  await settingsStore.fetchSettings();
  resetForm();
});

const resetForm = () => {
  formData.value = { ...settingsStore.settings };
};

const saveSettings = async () => {
  await settingsStore.saveSettings(formData.value);
};
</script>

<style scoped>
.head-settings-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  max-width: 900px;
  margin: 0 auto;
}

.header-section h2 {
  margin-top: 0;
  color: #333;
}

.header-section p {
  color: #666;
  margin-bottom: 20px;
}

.error-msg {
  background-color: #ffebee;
  color: #c62828;
  padding: 10px;
  border-radius: 5px;
  margin-bottom: 15px;
}

.success-msg {
  background-color: #e8f5e9;
  color: #2e7d32;
  padding: 10px;
  border-radius: 5px;
  margin-bottom: 15px;
  font-weight: bold;
}

.tabs {
  display: flex;
  border-bottom: 2px solid #eee;
  margin-bottom: 25px;
}

.tab-btn {
  padding: 12px 25px;
  background: none;
  border: none;
  font-size: 16px;
  font-weight: bold;
  color: #888;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
  transition: all 0.3s;
}

.tab-btn:hover {
  color: #555;
}

.tab-btn.active {
  color: #2196F3;
  border-bottom-color: #2196F3;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-info h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #333;
}

.setting-info p {
  margin: 0;
  font-size: 13px;
  color: #777;
}

.setting-control {
  min-width: 150px;
  text-align: right;
}

.setting-control select,
.setting-control input[type="number"] {
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 5px;
  font-size: 15px;
  width: 160px;
}

.input-with-unit {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
}

.input-with-unit input {
  width: 100px;
  text-align: right;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ddd;
}

.btn-cancel {
  padding: 10px 20px;
  background-color: #f1f1f1;
  border: 1px solid #ddd;
  border-radius: 5px;
  cursor: pointer;
  font-weight: bold;
  color: #555;
}

.btn-primary {
  padding: 10px 20px;
  background-color: #2196F3;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-weight: bold;
  color: white;
}

.btn-primary:hover {
  background-color: #1976D2;
}

.btn-cancel:hover {
  background-color: #e0e0e0;
}

/* Switch css */
.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
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
  height: 18px; width: 18px;
  left: 3px; bottom: 3px;
  background-color: white;
  transition: .4s;
}
input:checked + .slider { background-color: #4CAF50; }
input:checked + .slider:before { transform: translateX(26px); }
.slider.round { border-radius: 24px; }
.slider.round:before { border-radius: 50%; }

.loading-overlay {
  text-align: center;
  padding: 50px;
  color: #888;
}
</style>
