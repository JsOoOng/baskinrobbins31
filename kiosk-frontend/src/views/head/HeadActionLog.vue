<!--
  [화면 흐름 안내] HeadActionLog
  역할: 본사 관리에서 사용자가 보는 화면이다.
  진입: /head/logs -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/head/headLogApi -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<template>
  <div class="head-log-container">
    <div class="header-section">
      <h2>📝 관리자 작업 내역</h2>
      <div class="header-actions">
        <select v-model="typeFilter" class="status-filter">
          <option value="ALL">전체 분류</option>
          <!-- 서버에 새 작업 분류가 추가되어도 별도 화면 수정 없이 필터에 표시한다. -->
          <option v-for="type in logTypes" :key="type" :value="type">
            {{ type }}
          </option>
        </select>
        <select v-model="actionTypeFilter" class="status-filter">
          <option value="ALL">전체 작업</option>
          <option v-for="type in actionTypes" :key="type" :value="type">
            {{ type }}
          </option>
        </select>
        <button class="btn-refresh" @click="fetchLogs">새로고침</button>
      </div>
    </div>

    <div v-if="error" class="error-msg">
      {{ error }}
    </div>

    <div class="table-container">
      <table class="log-table">
        <thead>
          <tr>
            <th>번호</th>
            <th>분류</th>
            <th>작업 내용</th>
            <th>작업자</th>
            <th>작업 일시</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="isLoading">
            <td colspan="5" class="text-center">로딩 중...</td>
          </tr>
          <tr v-else-if="filteredLogs.length === 0">
            <td colspan="5" class="text-center">조회된 작업 내역이 없습니다.</td>
          </tr>
          <tr v-else v-for="(log, index) in paginatedLogs" :key="log.id">
            <td>{{ filteredLogs.length - ((currentPage - 1) * pageSize + index) }}</td>
            <td>
              <span class="type-badge" :class="getTypeClass(log.type)">
                {{ log.type }}
              </span>
            </td>
            <td class="log-action">
              <span class="action-badge">{{ getActionType(log.action) }}</span>
              {{ log.action }}
            </td>
            <td>
              <span class="admin-badge">{{ log.administrator }}</span>
            </td>
            <td>{{ log.displayTime }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <HeadTablePagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total-items="filteredLogs.length"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { getAdminLogs } from '@/api/head/headLogApi';
import HeadTablePagination from '@/components/head/HeadTablePagination.vue';

const logs = ref([]);
const isLoading = ref(false);
const error = ref(null);
const typeFilter = ref('ALL');
const actionTypeFilter = ref('ALL');
const currentPage = ref(1);
const pageSize = ref(10);

// 받아온 로그에서 분류를 직접 만들기 때문에 최근 추가된 본사 기능도 자동 반영된다.
const logTypes = computed(() =>
  [...new Set(logs.value.map(log => log.type).filter(Boolean))]
    .sort((a, b) => a.localeCompare(b, 'ko'))
);

/*
 * 기존 로그 문구를 작업 성격으로 분류합니다.
 * 더 구체적인 유형을 먼저 검사해야 "배송 상태 변경"이 일반 상태 변경으로 섞이지 않습니다.
 */
const getActionType = (action = '') => {
  const rules = [
    ['반려', '반려'],
    ['승인', '승인'],
    ['배송', '배송'],
    ['입고', '입고'],
    ['알림', '알림 전송'],
    ['발급', '발급'],
    ['회수', '회수'],
    ['삭제', '삭제'],
    ['비활성화', '삭제'],
    ['상태 변경', '상태 변경'],
    ['노출', '상태 변경'],
    ['등록', '등록'],
    ['생성', '등록'],
    ['배정', '등록'],
    ['수정', '수정'],
    ['설정 변경', '수정'],
    ['권한 변경', '수정'],
    ['비밀번호 변경', '수정']
  ];
  return rules.find(([keyword]) => action.includes(keyword))?.[1] ?? '기타';
};

const actionTypes = computed(() =>
  [...new Set(logs.value.map(log => getActionType(log.action)))]
    .sort((a, b) => a.localeCompare(b, 'ko'))
);

const filteredLogs = computed(() => {
  return logs.value.filter(log =>
    (typeFilter.value === 'ALL' || log.type === typeFilter.value) &&
    (actionTypeFilter.value === 'ALL' || getActionType(log.action) === actionTypeFilter.value)
  );
});

const paginatedLogs = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredLogs.value.slice(start, start + pageSize.value);
});

// 필터가 바뀌면 결과가 없는 이전 페이지에 머물지 않고 첫 페이지로 이동한다.
watch([typeFilter, actionTypeFilter], () => {
  currentPage.value = 1;
});

const fetchLogs = async () => {
  isLoading.value = true;
  error.value = null;
  try {
    const data = await getAdminLogs();
    logs.value = data || [];
    currentPage.value = 1;
  } catch (err) {
    console.error('작업 내역 조회 실패:', err);
    error.value = '작업 내역을 불러오는데 실패했습니다.';
  } finally {
    isLoading.value = false;
  }
};

const typeClasses = {
  '상품': 'type-product',
  '상품 옵션': 'type-product-option',
  '지점 상품': 'type-store-product',
  '상품 재고': 'type-product-stock',
  '카테고리': 'type-category',
  '지점': 'type-store',
  '지점 맛': 'type-store-flavor',
  '맛': 'type-flavor',
  '맛 재고': 'type-flavor-stock',
  '배너': 'type-banner',
  '이벤트': 'type-event',
  '쿠폰': 'type-coupon',
  '재고 신청': 'type-restock',
  '배송': 'type-delivery',
  '정책': 'type-policy',
  '환경 설정': 'type-settings',
  '관리자 계정': 'type-admin',
  '지점 계정': 'type-store-account',
  '고객 문의': 'type-inquiry',
  '재고 알림': 'type-alert'
};

const getTypeClass = (type) => typeClasses[type] ?? 'type-default';

onMounted(() => {
  fetchLogs();
});
</script>

<style scoped>
.head-log-container {
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-filter {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  background-color: white;
  cursor: pointer;
  font-size: 14px;
  color: #333;
}

.btn-refresh {
  background-color: #f0f0f0;
  color: #333;
  border: 1px solid #ddd;
  padding: 8px 16px;
  border-radius: 5px;
  cursor: pointer;
  font-weight: bold;
}

.btn-refresh:hover {
  background-color: #e0e0e0;
}

.error-msg {
  color: #d32f2f;
  background-color: #ffebee;
  padding: 10px;
  border-radius: 5px;
  margin-bottom: 15px;
}

.table-container {
  overflow-x: auto;
}

.log-table {
  width: 100%;
  border-collapse: collapse;
}

.log-table th, .log-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.log-table th {
  background-color: #f8f9fa;
  font-weight: bold;
  color: #555;
}

.log-action {
  font-weight: 500;
  color: #2c3e50;
}

.action-badge {
  display: inline-block;
  min-width: 58px;
  margin-right: 8px;
  padding: 3px 8px;
  border-radius: 6px;
  color: #5d4cc7;
  font-size: 0.78em;
  font-weight: 700;
  text-align: center;
  background: #f0edff;
}

.text-center {
  text-align: center !important;
}

.type-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.85em;
  font-weight: bold;
  display: inline-block;
  min-width: 60px;
  text-align: center;
}

.type-product { background-color: #e3f2fd; color: #1976d2; }
.type-product-option { background-color: #e8eaf6; color: #3949ab; }
.type-store-product { background-color: #e0f2f1; color: #00796b; }
.type-product-stock { background-color: #e1f5fe; color: #0277bd; }
.type-category { background-color: #f3e5f5; color: #7b1fa2; }
.type-store { background-color: #fff3e0; color: #f57c00; }
.type-store-flavor { background-color: #fce4ec; color: #c2185b; }
.type-flavor { background-color: #f3e5f5; color: #8e24aa; }
.type-flavor-stock { background-color: #ede7f6; color: #5e35b1; }
.type-banner { background-color: #e8f5e9; color: #388e3c; }
.type-event { background-color: #ffebee; color: #d32f2f; }
.type-coupon { background-color: #e0f7fa; color: #0097a7; }
.type-restock { background-color: #fff8e1; color: #f9a825; }
.type-delivery { background-color: #e8eaf6; color: #303f9f; }
.type-policy { background-color: #efebe9; color: #6d4c41; }
.type-settings { background-color: #eceff1; color: #455a64; }
.type-admin { background-color: #fbe9e7; color: #d84315; }
.type-store-account { background-color: #f1f8e9; color: #558b2f; }
.type-inquiry { background-color: #e0f2f1; color: #00897b; }
.type-alert { background-color: #fff3e0; color: #e65100; }
.type-default { background-color: #f5f5f5; color: #616161; }

.admin-badge {
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.85em;
  color: #555;
}

@media (max-width: 760px) {
  .header-section,
  .header-actions {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
