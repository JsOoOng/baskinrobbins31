<template>
    <div class="coupon-dashboard">
 
        <div class="top-actions">
            <button @click="openIssueAllModal" class="btn-secondary">🎁 쿠폰 발급</button>
            <button @click="openCreateModal" class="btn-primary">＋ 새 쿠폰 등록</button>    
        </div>
 
      <!-- 쿠폰 목록 테이블 -->
      <table class="coupon-table">
        
        <thead>
          <tr>
            <th>쿠폰 ID</th>
            <th>쿠폰 명</th>
            <th>할인 값</th>
            
            <!-- 💡 할인 유형 헤더 (토글 메뉴 적용) -->
            <th class="dropdown-header">
              <div class="header-content" @click.stop="toggleDiscountDropdown">
                <span>할인 유형</span>
                <span class="filter-arrow">▼</span>
              </div>
              <!-- 토글 메뉴 팝업 -->
              <div v-if="isDiscountDropdownOpen" class="filter-dropdown-menu">
                <div @click="selectDiscountFilter('ALL')" :class="{ selected: discountTypeFilter === 'ALL' }">전체 보기</div>
                <div @click="selectDiscountFilter('AMOUNT')" :class="{ selected: discountTypeFilter === 'AMOUNT' }">금액 (AMOUNT)</div>
                <div @click="selectDiscountFilter('PERCENT')" :class="{ selected: discountTypeFilter === 'PERCENT' }">비율 (PERCENT)</div>
              </div>
            </th>

            <th>유효 기간(일)</th>
            <th>등록일시</th>
            
            <!-- 💡 뿌리기 헤더 (토글 메뉴 적용) -->
            <th class="dropdown-header">
              <div class="header-content" @click.stop="toggleIssueDropdown">
                <span>쿠폰 발급</span>
                <span class="filter-arrow">▼</span>
              </div>
              <!-- 토글 메뉴 팝업 -->
              <div v-if="isIssueDropdownOpen" class="filter-dropdown-menu">
                <div @click="selectIssueFilter('ALL')" :class="{ selected: issueFilter === 'ALL' }">전체 보기</div>
                <div @click="selectIssueFilter('O')" :class="{ selected: issueFilter === 'O' }">O (발급 완료)</div>
                <div @click="selectIssueFilter('X')" :class="{ selected: issueFilter === 'X' }">X (미발급)</div>
              </div>
            </th>

            <th>관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filteredCoupons.length === 0">
            <td colspan="8" class="no-data">조건에 맞는 쿠폰이 없습니다.</td>
          </tr>
          <tr v-for="coupon in filteredCoupons" :key="coupon.couponId">
            <td>{{ coupon.couponId }}</td>
            <td>{{ coupon.couponName }}</td>
            <td>
              {{ (coupon.discountValue ?? 0).toLocaleString() }}
              {{ coupon.discountType === 'PERCENT' ? '%' : '원' }}
            </td>
            <td>
              <span :class="['badge', (coupon.discountType || '').toLowerCase()]">
                {{ coupon.discountType }}
              </span>
            </td>
            <td>{{ coupon.durationDays ?? 0 }}일</td>
            <td>{{ coupon.createdAt ? coupon.createdAt.substring(0, 10) : '' }}</td>
            <td>
                <span :class="coupon.isIssuedAll ? 'status-o' : 'status-x'">
                    {{ coupon.isIssuedAll ? 'O' : 'X' }}
                </span>
            </td>
            <td>
                <button 
                    type="button" 
                    @click="openEditModal(coupon)" 
                    :disabled="coupon.is_issued_all || coupon.isIssuedAll"
                    :class="{ 'btn-disabled': coupon.is_issued_all || coupon.isIssuedAll }"
                    class="btn-edit"
                    >
                    수정
                </button>
              <button @click="deleteCoupon(coupon.couponId)" class="btn-delete">삭제</button>
            </td>
          </tr>
        </tbody>
      </table>
 
      <!-- 등록 / 수정 모달 -->
      <div v-if="isModalOpen" class="modal-overlay">
        <div class="modal-content">
          <h3>{{ isEditMode ? '쿠폰 수정' : '새 쿠폰 등록' }}</h3>
          
          <form @submit.prevent="saveCoupon">
            <div class="form-group">
              <label>쿠폰 ID (코드)</label>
              <input 
                type="text" 
                v-model="form.couponId" 
                :disabled="isEditMode" 
                placeholder="예: SUMMER2026" 
                required 
              />
            </div>
 
            <div class="form-group">
              <label>쿠폰 이름</label>
              <input type="text" v-model="form.couponName" placeholder="예: 여름 맞이 할인 쿠폰" required />
            </div>
 
            <div class="form-group">
              <label>할인 값</label>
              <input type="number" v-model.number="form.discountValue" placeholder="예: 3000 또는 10" required />
            </div>
 
            <div class="form-group">
              <label>할인 유형</label>
              <select v-model="form.discountType" required>
                <option value="AMOUNT">금액 (AMOUNT)</option>
                <option value="PERCENT">비율 (PERCENT)</option>
              </select>
            </div>
 
            <div class="form-group">
              <label>유효 기간 (일)</label>
              <input type="number" v-model.number="form.durationDays" placeholder="예: 30" required />
            </div>
            
 
            <div class="modal-actions">
              <button type="submit" class="btn-submit">{{ isEditMode ? '수정 완료' : '등록' }}</button>
              <button type="button" @click="closeModal" class="btn-cancel">취소</button>
            </div>
          </form>
        </div>
      </div>

      <!-- 쿠폰 뿌리기(전체 발급) 선택 모달 -->
      <div v-if="isIssueModalOpen" class="modal-overlay">
        <div class="modal-content">
          <h3>🎁 전체 유저에게 쿠폰 뿌리기</h3>
          <p class="modal-desc">모든 유저에게 일괄 발급할 쿠폰을 선택하세요.</p>
          
          <div class="form-group">
            <label>발급할 쿠폰 선택</label>
            <select v-model="selectedIssueCouponId" class="issue-select">
              <option disabled value="">쿠폰을 선택해주세요</option>
              
              <template v-for="coupon in coupons" :key="coupon.couponId">
                <option 
                  v-if="!coupon.is_issued_all && !coupon.isIssuedAll" 
                  :value="coupon.couponId"
                >
                  [{{ coupon.couponId }}] {{ coupon.couponName }} 
                  ({{ (coupon.discountValue ?? 0).toLocaleString() }}{{ coupon.discountType === 'PERCENT' ? '%' : '원' }})
                </option>
              </template>
              
            </select>
          </div>

          <div class="modal-actions">
            <button type="button" @click="executeIssueAll" class="btn-submit">발급하기</button>
            <button type="button" @click="closeIssueModal" class="btn-cancel">취소</button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();

// 상태 정의
const coupons = ref([]);          
const isModalOpen = ref(false);   
const isEditMode = ref(false);    

const isIssueModalOpen = ref(false);
const selectedIssueCouponId = ref('');

// 🔍 필터 상태 관리 변수
const issueFilter = ref('ALL');        
const discountTypeFilter = ref('ALL'); 

// 🔍 토글 메뉴 오픈 여부 상태
const isIssueDropdownOpen = ref(false);
const isDiscountDropdownOpen = ref(false);

// 토글 열기/닫기 제어 함수들
const toggleIssueDropdown = () => {
  isIssueDropdownOpen.value = !isIssueDropdownOpen.value;
  isDiscountDropdownOpen.value = false; // 다른 메뉴는 닫기
};

const toggleDiscountDropdown = () => {
  isDiscountDropdownOpen.value = !isDiscountDropdownOpen.value;
  isIssueDropdownOpen.value = false; // 다른 메뉴는 닫기
};

// 필터 선택 시 값 적용 및 메뉴 닫기
const selectIssueFilter = (val) => {
  issueFilter.value = val;
  isIssueDropdownOpen.value = false;
};

const selectDiscountFilter = (val) => {
  discountTypeFilter.value = val;
  isDiscountDropdownOpen.value = false;
};

// 화면 아무 곳이나 누르면 토글 메뉴 닫히게 처리
const closeDropdowns = (e) => {
  if (!e.target.closest('.dropdown-header')) {
    isIssueDropdownOpen.value = false;
    isDiscountDropdownOpen.value = false;
  }
};

onMounted(() => {
  fetchCoupons();
  window.addEventListener('click', closeDropdowns);
});

onUnmounted(() => {
  window.removeEventListener('click', closeDropdowns);
});

// 🔍 필터링된 목록 계산
const filteredCoupons = computed(() => {
  return coupons.value.filter(coupon => {
    // 1. 뿌리기 상태 필터 검사
    const isIssued = Boolean(coupon.is_issued_all ?? coupon.isIssuedAll);
    if (issueFilter.value === 'O' && !isIssued) return false;
    if (issueFilter.value === 'X' && isIssued) return false;

    // 2. 할인 유형 필터 검사
    if (discountTypeFilter.value !== 'ALL' && coupon.discountType !== discountTypeFilter.value) {
      return false;
    }

    return true;
  });
});

// 폼 데이터 초기값
const form = ref({
  couponId: '',
  couponName: '',
  discountValue: 0,
  discountType: 'AMOUNT',
  durationDays: 30
});

const fetchCoupons = async () => {
  try {
    const res = await axios.get('http://localhost:8889/head/coupon');
    coupons.value = res.data;
  } catch (error) {
    console.error('쿠폰 목록 조회 실패:', error);
  }
};

const openIssueAllModal = () => {
  selectedIssueCouponId.value = '';
  isIssueModalOpen.value = true;
};

const openCreateModal = () => {
  isEditMode.value = false; 
  form.value = {            
    couponId: '',
    couponName: '',
    discountValue: 0,
    discountType: 'AMOUNT',
    durationDays: 30
  };
  isModalOpen.value = true;
};

const openEditModal = (coupon) => {
  isEditMode.value = true;
  form.value = { ...coupon };
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

// 쿠폰 저장 ( 등록, 수정 )
const saveCoupon = async () => {
  if (form.value.discountType === 'PERCENT' && form.value.discountValue > 100) {
    alert('비율(PERCENT) 할인 값은 100을 초과할 수 없습니다.');
    return;
  }

  try {
    if (isEditMode.value) {
      await axios.put('http://localhost:8889/head/coupon', form.value);
      alert('쿠폰이 수정되었습니다.');
    } else {
      await axios.post('http://localhost:8889/head/coupon', form.value);
      alert('새 쿠폰이 등록되었습니다.');
    }
    
    closeModal();
    fetchCoupons();
  } catch (error) {
    console.error('쿠폰 저장 실패:', error);
    alert('작업 중 오류가 발생했습니다. (중복된 ID인지 확인해주세요)');
  }
};

const deleteCoupon = async (couponId) => {
  if (!confirm(`정말 쿠폰 [${couponId}]을 삭제하시겠습니까?`)) return;

  try {
    await axios.delete(`http://localhost:8889/head/coupon/${couponId}`);
    alert('쿠폰이 삭제되었습니다.');
    fetchCoupons();
  } catch (error) {
    console.error('쿠폰 삭제 실패:', error);
    alert('쿠폰 삭제 중 오류가 발생했습니다.');
  }
};

const closeIssueModal = () => {
  isIssueModalOpen.value = false;
};

const executeIssueAll = async () => {
  if (!selectedIssueCouponId.value) {
    alert('발급할 쿠폰을 선택해주세요.');
    return;
  }

  if (!confirm(`선택한 쿠폰 [${selectedIssueCouponId.value}]을(를) 모든 유저에게 일괄 발급하시겠습니까?`)) return;

  try {
    await axios.post(`http://localhost:8889/head/coupon/issue-all/${selectedIssueCouponId.value}`);
    alert('쿠폰이 모든 유저에게 성공적으로 뿌려졌습니다!');
    closeIssueModal();
    await fetchCoupons();
  } catch (error) {
    console.error('쿠폰 일괄 발급 실패:', error);
    alert('쿠폰 일괄 발급 중 오류가 발생했습니다.');
  }
};
</script>
  
  <style scoped>
  .coupon-dashboard {
    padding: 30px;
    max-width: 1000px;
    margin: 0 auto;
  }
  .top-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-bottom: 20px;
  }
  .btn-primary {
    background-color: #4f46e5;
    color: white;
    border: none;
    padding: 10px 16px;
    border-radius: 6px;
    cursor: pointer;
    font-weight: bold;
  }
  .btn-secondary {
    padding: 10px 16px;
    background-color: #3182ce;
    color: white;
    border: none;
    border-radius: 4px;
    font-weight: bold;
    cursor: pointer;
  }
  .coupon-table {
    width: 100%;
    table-layout: fixed; 
    border-collapse: collapse;
  }
  .coupon-table th, 
  .coupon-table td {
    word-break: break-all;    
    overflow-wrap: break-word; 
    white-space: normal;       
    padding: 12px 8px;
    text-align: center;
    vertical-align: middle;
  }
  .coupon-table th {
    background-color: #f8fafc;
    font-weight: 600;
    color: #334155;
  }

  /* 🔍 드롭다운 헤더 필터 스타일 */
  .dropdown-header {
    position: relative;
    cursor: pointer;
    user-select: none;
  }
  .dropdown-header:hover {
    background-color: #f1f5f9;
  }
  .header-content {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
  }
  .filter-arrow {
    font-size: 10px;
    color: #64748b;
  }
  
  /* 팝업 드롭다운 메뉴 스타일 */
  .filter-dropdown-menu {
    position: absolute;
    top: 100%;
    left: 50%;
    transform: translateX(-50%);
    background: white;
    border: 1px solid #cbd5e1;
    border-radius: 6px;
    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
    z-index: 100;
    width: 130px;
    padding: 4px 0;
    text-align: left;
  }
  .filter-dropdown-menu div {
    padding: 8px 12px;
    font-size: 13px;
    color: #334155;
    cursor: pointer;
  }
  .filter-dropdown-menu div:hover {
    background-color: #f8fafc;
    color: #4f46e5;
  }
  .filter-dropdown-menu div.selected {
    font-weight: bold;
    color: #4f46e5;
    background-color: #e0e7ff;
  }

  .no-data {
    color: #94a3b8;
    padding: 30px !important;
  }
  .badge {
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: bold;
  }
  .badge.amount { background-color: #e0f2fe; color: #0369a1; }
  .badge.percent { background-color: #fef3c7; color: #b45309; }
  
  .btn-edit {
    background-color: #e2e8f0;
    border: none;
    padding: 6px 12px;
    border-radius: 4px;
    cursor: pointer;
    margin-right: 6px;
  }
  .btn-edit:disabled {
    background-color: #f1f5f9;
    color: #cbd5e1;
    cursor: not-allowed;
  }
  .btn-delete {
    background-color: #fee2e2;
    color: #991b1b;
    border: none;
    padding: 6px 12px;
    border-radius: 4px;
    cursor: pointer;
  }
  
  /* 모달 스타일 */
  .modal-overlay {
    position: fixed;
    top: 0; left: 0; width: 100%; height: 100%;
    background: rgba(0,0,0,0.5);
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .modal-content {
    background: white;
    padding: 30px;
    border-radius: 12px;
    width: 400px;
    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  }
  .form-group {
    margin-bottom: 15px;
    display: flex;
    flex-direction: column;
  }
  .form-group label {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 5px;
    color: #475569;
  }
  .form-group input, .form-group select {
    padding: 10px;
    border: 1px solid #cbd5e1;
    border-radius: 6px;
    font-size: 14px;
  }
  .modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 20px;
  }
  .btn-submit {
    background-color: #10b981;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 6px;
    cursor: pointer;
  }
  .btn-cancel {
    background-color: #cbd5e1;
    border: none;
    padding: 8px 16px;
    border-radius: 6px;
    cursor: pointer;
  }
  </style>