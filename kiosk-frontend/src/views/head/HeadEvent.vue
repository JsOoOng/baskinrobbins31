<!--
  [화면 흐름 안내] HeadEvent
  역할: 본사 관리에서 사용자가 보는 화면이다.
  진입: /head/events -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> props·Pinia·상위 화면 상태 -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<template>
  <div class="head-event-container">
    <div class="header-section">
      <h2>🎉 이벤트 관리</h2>
      <div class="header-actions">
        <input v-model="searchKeyword" class="table-search" type="search" placeholder="이벤트명·대상 검색" />
        <select v-model="discountTypeFilter" class="status-filter">
          <option value="ALL">전체 혜택</option>
          <option value="AMOUNT">정액 할인 (원)</option>
          <option value="RATE">정률 할인 (%)</option>
        </select>
        <select v-model="statusFilter" class="status-filter">
          <option value="ALL">전체 상태</option>
          <option value="SCHEDULED">예정 (SCHEDULED)</option>
          <option value="ACTIVE">진행중 (ACTIVE)</option>
          <option value="ENDED">종료 (ENDED)</option>
          <option value="INACTIVE">비활성 (INACTIVE)</option>
        </select>
        <button class="btn-primary" @click="openModal()">+ 이벤트 등록</button>
      </div>
    </div>

    <div v-if="eventStore.error" class="error-msg">
      {{ eventStore.error }}
    </div>

    <div class="table-container">
      <table class="event-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>이벤트명</th>
            <th>상태</th>
            <th>적용 대상</th>
            <th>할인 혜택</th>
            <th>시작일</th>
            <th>종료일</th>
            <th>노출 여부</th>
            <th>관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="eventStore.isLoading">
            <td colspan="9" class="text-center">로딩 중...</td>
          </tr>
          <tr v-else-if="filteredEvents.length === 0">
            <td colspan="9" class="text-center">조회된 이벤트가 없습니다.</td>
          </tr>
          <tr v-else v-for="event in paginatedEvents" :key="event.eventId">
            <td>{{ event.eventId }}</td>
            <td class="event-name">{{ event.eventName }}</td>
            <td>
              <span class="status-badge" :class="event.eventStatus.toLowerCase()">
                {{ event.eventStatus }}
              </span>
            </td>
            <td>{{ event.targetType || '전체' }}</td>
            <td>{{ event.discountType === 'RATE' ? (event.discountValue ? event.discountValue + '%' : '-') : (event.discountValue ? event.discountValue.toLocaleString() + '원' : '-') }}</td>
            <td>{{ formatDate(event.startDate) }}</td>
            <td>{{ formatDate(event.endDate) }}</td>
            <td>
              <label class="switch">
                <input 
                  type="checkbox" 
                  :checked="event.isVisible" 
                  @change="toggleVisibility(event)"
                >
                <span class="slider round"></span>
              </label>
            </td>
            <td>
              <button class="btn-sm btn-edit" @click="openModal(event)">수정</button>
              <button class="btn-sm btn-edit" @click="extendEvent(event)">연장</button>
              <button class="btn-sm btn-delete" @click="deleteEvent(event.eventId)">삭제</button>
            </td>
          </tr>
        </tbody>
      </table>
      <HeadTablePagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total-items="filteredEvents.length"
      />
    </div>

    <!-- 이벤트 등록/수정 모달 -->
    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3>{{ isEditMode ? '이벤트 수정' : '새 이벤트 등록' }}</h3>
        <form @submit.prevent="submitEvent">
          <div class="form-group">
            <label>이벤트명</label>
            <input type="text" v-model="formData.eventName" required />
          </div>
          <div class="form-group">
            <label>설명</label>
            <textarea v-model="formData.description" rows="3"></textarea>
          </div>
          <div class="form-row">
            <div class="form-group half">
              <label>시작일</label>
              <input type="datetime-local" v-model="formData.startDate" required />
            </div>
            <div class="form-group half">
              <label>종료일</label>
              <input type="datetime-local" v-model="formData.endDate" required />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group half">
              <label>상태</label>
              <select v-model="formData.eventStatus">
                <option value="SCHEDULED">예정 (SCHEDULED)</option>
                <option value="ACTIVE">진행중 (ACTIVE)</option>
                <option value="ENDED">종료 (ENDED)</option>
                <option value="INACTIVE">비활성 (INACTIVE)</option>
              </select>
            </div>
            <div class="form-group half">
              <label>대상 종류</label>
              <input type="text" v-model="formData.targetType" placeholder="ex) ALL, NEW_USER" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group half">
              <label>할인 혜택 (방식 / 수치)</label>
              <div class="discount-input-group">
                <select v-model="formData.discountType" class="discount-type-select">
                  <option value="AMOUNT">원 (₩)</option>
                  <option value="RATE">퍼센트 (%)</option>
                </select>
                <input type="number" v-model="formData.discountValue" class="discount-value-input" />
              </div>
            </div>
            <div class="form-group half checkbox-group">
              <label>키오스크 노출</label>
              <input type="checkbox" v-model="formData.isVisible" />
            </div>
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
import { ref, onMounted, computed, watch } from 'vue';
import { useHeadEventStore } from '@/stores/head/headEventStore';
import HeadTablePagination from '@/components/head/HeadTablePagination.vue';

const eventStore = useHeadEventStore();

const isModalOpen = ref(false);
const isEditMode = ref(false);
const currentEventId = ref(null);
const statusFilter = ref('ALL');
const discountTypeFilter = ref('ALL');
const searchKeyword = ref('');
const currentPage = ref(1);
const pageSize = ref(10);

const filteredEvents = computed(() => {
  return eventStore.events.filter(event => {
    const matchStatus = statusFilter.value === 'ALL' || event.eventStatus === statusFilter.value;
    const matchDiscountType = discountTypeFilter.value === 'ALL' || event.discountType === discountTypeFilter.value;
    const keyword = searchKeyword.value.trim().toLowerCase();
    const matchKeyword = !keyword || [
      event.eventId, event.eventName, event.targetType, event.eventStatus
    ].some(value => String(value ?? '').toLowerCase().includes(keyword));
    return matchStatus && matchDiscountType && matchKeyword;
  });
});
/*
 * 상태·할인 유형·검색 필터 결과를 현재 페이지 범위로 잘라
 * 이벤트 관리 테이블에 전달합니다.
 */
const paginatedEvents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredEvents.value.slice(start, start + pageSize.value);
});
watch([searchKeyword, statusFilter, discountTypeFilter, pageSize], () => {
  currentPage.value = 1;
});

const defaultForm = {
  eventName: '',
  description: '',
  startDate: '',
  endDate: '',
  eventStatus: 'SCHEDULED',
  targetType: 'ALL',
  discountType: 'AMOUNT',
  discountValue: 0,
  isVisible: false
};

const formData = ref({ ...defaultForm });

onMounted(() => {
  eventStore.fetchEvents();
});

const formatDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleString('ko-KR', { 
    year: 'numeric', month: '2-digit', day: '2-digit', 
    hour: '2-digit', minute: '2-digit' 
  });
};

const formatToInputDateTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  // datetime-local input requires YYYY-MM-DDThh:mm
  const pad = (n) => n.toString().padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const openModal = (event = null) => {
  if (event) {
    isEditMode.value = true;
    currentEventId.value = event.eventId;
    formData.value = {
      eventName: event.eventName,
      description: event.description,
      startDate: formatToInputDateTime(event.startDate),
      endDate: formatToInputDateTime(event.endDate),
      eventStatus: event.eventStatus,
      targetType: event.targetType,
      discountType: event.discountType || 'AMOUNT',
      discountValue: event.discountValue,
      isVisible: event.isVisible
    };
  } else {
    isEditMode.value = false;
    currentEventId.value = null;
    formData.value = { ...defaultForm };
  }
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

const submitEvent = async () => {
  try {
    const payload = {
      ...formData.value,
      // Input datetime-local produces YYYY-MM-DDThh:mm, which Jackson usually parses well into LocalDateTime
      startDate: formData.value.startDate || null,
      endDate: formData.value.endDate || null
    };

    if (isEditMode.value) {
      await eventStore.updateEvent(currentEventId.value, payload);
    } else {
      await eventStore.createEvent(payload);
    }
    closeModal();
  } catch (error) {
    alert('저장 중 오류가 발생했습니다.');
  }
};

const deleteEvent = async (eventId) => {
  if (confirm('정말로 이 이벤트를 삭제하시겠습니까?')) {
    await eventStore.deleteEvent(eventId);
  }
};

const toggleVisibility = async (event) => {
  try {
    await eventStore.updateVisibility(event.eventId, !event.isVisible);
  } catch (error) {
    alert('노출 여부 변경에 실패했습니다.');
  }
};

/*
 * 쉬운주석: 현재 종료일을 입력 예시로 보여주고, 더 늦은 날짜를 입력하면 연장 API를 호출한다.
 */
const extendEvent = async (event) => {
  const endDate = prompt(
    '새 종료일을 입력해 주세요. (예: 2026-08-31T23:59)',
    formatToInputDateTime(event.endDate)
  );
  if (!endDate) return;

  try {
    await eventStore.extendEvent(event.eventId, endDate);
  } catch (error) {
    alert(error.response?.data?.message || '이벤트 연장에 실패했습니다.');
  }
};
</script>

<style scoped>
.head-event-container {
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
  gap: 10px;
  align-items: center;
}

.status-filter {
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 5px;
  background-color: white;
  cursor: pointer;
  font-size: 14px;
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

.table-container {
  overflow-x: auto;
}

.event-table {
  width: 100%;
  border-collapse: collapse;
}

.event-table th, .event-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.event-table th {
  background-color: #f8f9fa;
  font-weight: bold;
  color: #555;
}

.event-name {
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

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.85em;
  font-weight: bold;
}

.status-badge.scheduled { background-color: #ff9800; color: white; }
.status-badge.active { background-color: #4CAF50; color: white; }
.status-badge.ended { background-color: #9e9e9e; color: white; }
.status-badge.inactive { background-color: #f44336; color: white; }

/* Switch for toggle */
.switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 20px;
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
.form-group input[type="number"],
.form-group input[type="datetime-local"],
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}

.discount-input-group {
  display: flex;
  gap: 10px;
}
.discount-type-select {
  flex: 1;
}
.discount-value-input {
  flex: 2;
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 10px;
}
.checkbox-group label {
  margin-bottom: 0;
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

/*
 * 쉬운주석: 쿠폰 관리 화면과 같은 여백·모서리·보라색 버튼·작은 표 글씨를 사용한다.
 * 기존 기능 CSS 뒤에 두어 화면 구조를 바꾸지 않고 디자인만 통일한다.
 */
.head-event-container {
  display: grid;
  gap: 18px;
  padding: 0;
  background: transparent;
  box-shadow: none;
}

.header-section {
  align-items: center;
  gap: 18px;
  margin: 0;
  padding: 18px;
  border: 1px solid #e2e6ed;
  border-radius: 15px;
  background: #fff;
}

.header-section h2 {
  color: #303544;
  font-size: 20px;
}

.header-actions {
  flex: 1;
  flex-wrap: nowrap;
  justify-content: flex-end;
  min-width: 0;
}

.status-filter,
.header-actions .table-search {
  min-height: 40px;
  border: 1px solid #d8dbe8;
  border-radius: 8px;
  color: #303544;
  background: #fff;
}

/*
 * 쉬운주석: 검색창과 필터, 등록 버튼이 한 줄에서 나란히 보이도록
 * 검색창만 남는 공간을 사용하고 버튼과 선택창은 줄어들지 않게 한다.
 */
.header-actions .table-search {
  flex: 1 1 260px;
  min-width: 180px;
}

.header-actions :is(.status-filter, .btn-primary) {
  flex: 0 0 auto;
}

.btn-primary {
  height: 40px;
  padding: 0 15px;
  border-radius: 9px;
  background: #725ee7;
  font-size: 11px;
}

.btn-primary:hover {
  background: #624ed8;
}

.table-container {
  overflow-x: auto;
  border: 1px solid #e2e6ed;
  border-radius: 15px;
  background: #fff;
}

.event-table {
  min-width: 1050px;
}

.event-table th,
.event-table td {
  padding: 13px 12px;
  border-bottom: 1px solid #edf0f4;
  font-size: 10px;
  text-align: center;
}

.event-table th {
  color: #7e8696;
  background: #fafbfc;
  font-weight: 800;
}

.event-table td {
  color: #515867;
}

.event-name {
  color: #303544;
}

.btn-sm {
  height: 29px;
  padding: 0 10px;
  border-radius: 7px;
  font-size: 9px;
  font-weight: 750;
}

.btn-edit {
  border: 1px solid #dcd7fb;
  color: #6756dc;
  background: #f3f1ff;
}

.btn-delete {
  border: 1px solid #ffd5dc;
  color: #db485e;
  background: #fff2f4;
}

.modal-content {
  border: 1px solid #e3e6ed;
  border-radius: 18px;
}

.btn-submit {
  background: #725ee7;
}

@media (max-width: 900px) {
  .header-section {
    align-items: stretch;
    flex-direction: column;
  }

  .header-actions {
    justify-content: flex-start;
    overflow-x: auto;
  }
}
</style>
