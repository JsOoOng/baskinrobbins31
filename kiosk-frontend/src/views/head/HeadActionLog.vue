<template>
  <div class="head-log-container">
    <div class="header-section">
      <h2>📝 관리자 작업 내역</h2>
      <div class="header-actions">
        <select v-model="typeFilter" class="status-filter">
          <option value="ALL">전체 분류</option>
          <option value="상품">상품</option>
          <option value="카테고리">카테고리</option>
          <option value="지점">지점</option>
          <option value="배너">배너</option>
          <option value="이벤트">이벤트</option>
          <option value="쿠폰">쿠폰</option>
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
          <tr v-else v-for="(log, index) in filteredLogs" :key="log.id">
            <td>{{ filteredLogs.length - index }}</td>
            <td>
              <span class="type-badge" :class="getTypeClass(log.type)">
                {{ log.type }}
              </span>
            </td>
            <td class="log-action">{{ log.action }}</td>
            <td>
              <span class="admin-badge">{{ log.administrator }}</span>
            </td>
            <td>{{ log.displayTime }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { getAdminLogs } from '@/api/head/headLogApi';

const logs = ref([]);
const isLoading = ref(false);
const error = ref(null);
const typeFilter = ref('ALL');

const filteredLogs = computed(() => {
  if (typeFilter.value === 'ALL') {
    return logs.value;
  }
  return logs.value.filter(log => log.type === typeFilter.value);
});

const fetchLogs = async () => {
  isLoading.value = true;
  error.value = null;
  try {
    const data = await getAdminLogs();
    logs.value = data || [];
  } catch (err) {
    console.error('작업 내역 조회 실패:', err);
    error.value = '작업 내역을 불러오는데 실패했습니다.';
  } finally {
    isLoading.value = false;
  }
};

const getTypeClass = (type) => {
  switch (type) {
    case '상품': return 'type-product';
    case '카테고리': return 'type-category';
    case '지점': return 'type-store';
    case '배너': return 'type-banner';
    case '이벤트': return 'type-event';
    case '쿠폰': return 'type-coupon';
    default: return 'type-default';
  }
};

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

.status-filter {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  background-color: white;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  margin-right: 10px;
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
.type-category { background-color: #f3e5f5; color: #7b1fa2; }
.type-store { background-color: #fff3e0; color: #f57c00; }
.type-banner { background-color: #e8f5e9; color: #388e3c; }
.type-event { background-color: #ffebee; color: #d32f2f; }
.type-coupon { background-color: #e0f7fa; color: #0097a7; }
.type-default { background-color: #f5f5f5; color: #616161; }

.admin-badge {
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.85em;
  color: #555;
}
</style>
