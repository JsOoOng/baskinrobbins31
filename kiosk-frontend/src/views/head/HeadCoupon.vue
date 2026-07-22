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
  display: grid;
  gap: 18px;
}


/* 상단 버튼 */
.top-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;

  padding: 18px;

  border: 1px solid #e2e6ed;
  border-radius: 15px;

  background: #ffffff;
}


.btn-primary,
.btn-secondary {

  height: 36px;

  padding: 0 15px;

  border-radius: 9px;

  cursor: pointer;

  font-size: 10px;

  font-weight: 800;

}


/* 새 쿠폰 */
.btn-primary {

  border: none;

  color: white;

  background: #725ee7;

}


/* 전체 발급 */
.btn-secondary {

  border: 1px solid #dcd7fb;

  color: #6756dc;

  background: #f3f1ff;

}



/* =====================
   테이블
===================== */


.coupon-table {

  width:100%;

  min-width:900px;

  border-collapse:collapse;

  background:white;

  border:1px solid #e2e6ed;

}


.coupon-table th,
.coupon-table td {

  padding:13px 12px;

  text-align:center;

  font-size:10px;

  word-break:break-word;

}



.coupon-table th {

  color:#7e8696;

  background:#fafbfc;

  font-weight:800;

}


.coupon-table td {

  color:#515867;

  border-top:1px solid #edf0f4;

}



/* =====================
   필터 드롭다운
===================== */


.dropdown-header {

  position:relative;

  cursor:pointer;

}


.header-content {

  display:flex;

  align-items:center;

  justify-content:center;

  gap:5px;

}


.filter-arrow {

  font-size:9px;

  color:#725ee7;

}



.filter-dropdown-menu {

  position:absolute;

  top:35px;

  left:50%;

  transform:translateX(-50%);

  z-index:1000;


  width:140px;

  padding:5px 0;


  border:1px solid #e2e6ed;

  border-radius:10px;


  background:white;

  box-shadow:
    0 8px 20px rgba(0,0,0,0.08);

}



.filter-dropdown-menu div {

  padding:10px;

  cursor:pointer;

  font-size:10px;

}



.filter-dropdown-menu div:hover {

  background:#f4f1ff;

}



.filter-dropdown-menu .selected {

  color:#725ee7;

  font-weight:800;

  background:#edeaff;

}



/* =====================
   할인 타입
===================== */


.badge {

  display:inline-flex;

  padding:5px 8px;

  border-radius:7px;

  font-size:9px;

  font-weight:800;

}


.badge.amount {

  color:#6756dc;

  background:#edeaff;

}


.badge.percent {

  color:#1c8b62;

  background:#e7f8ef;

}



/* =====================
   발급 상태
===================== */


.status-o,
.status-x {

  display:inline-flex;

  justify-content:center;

  width:22px;

  padding:4px 7px;

  border-radius:7px;

  font-size:9px;

  font-weight:900;

}



.status-o {

  color:#16804c;

  background:#e5f7ed;

}



.status-x {

  color:#db485e;

  background:#fff0f3;

}



/* =====================
   관리 버튼
===================== */


.btn-edit,
.btn-delete {

  height:29px;

  padding:0 10px;

  border-radius:7px;

  cursor:pointer;

  font-size:9px;

  font-weight:750;

}



.btn-edit {

  margin-right:5px;

  border:1px solid #dcd7fb;

  color:#6756dc;

  background:#f3f1ff;

}



.btn-edit:disabled {

  opacity:0.45;

  cursor:not-allowed;

}



.btn-delete {

  border:1px solid #ffd5dc;

  color:#db485e;

  background:#fff2f4;

}



/* 데이터 없음 */

.no-data {

  height:160px;

  color:#9097a6;

}



/* =====================
   모달
===================== */


.modal-overlay {

  position:fixed;

  z-index:3000;

  inset:0;


  display:flex;

  justify-content:center;

  align-items:center;


  background:
    rgba(25,29,42,0.52);


  backdrop-filter:blur(4px);

}



.modal-content {

  width:100%;

  max-width:480px;


  padding:22px;


  border:1px solid #e3e6ed;

  border-radius:18px;


  background:white;

}



.modal-content h3 {

  margin:0 0 20px;

  font-size:20px;

}



.modal-desc {

  margin:-10px 0 20px;

  color:#9299a8;

  font-size:11px;

}



/* 입력 */

.form-group {

  display:grid;

  gap:8px;

  margin-bottom:15px;

}



.form-group label {

  color:#464c5a;

  font-size:11px;

  font-weight:750;

}



.form-group input,
.form-group select,
.issue-select {

  width:100%;

  height:43px;


  padding:0 12px;


  border:1px solid #dfe3ea;

  border-radius:9px;


  outline:none;

}



.form-group input:focus,
.form-group select:focus {

  border-color:#7461e7;


  box-shadow:

    0 0 0 3px rgba(116,97,231,0.1);

}



/* 모달 버튼 */

.modal-actions {

  display:flex;

  justify-content:flex-end;

  gap:8px;

  margin-top:20px;

}



.btn-submit,
.btn-cancel {

  height:39px;

  padding:0 16px;


  border-radius:9px;

  cursor:pointer;


  font-size:10px;

  font-weight:800;

}



.btn-submit {

  border:1px solid #725ee7;

  color:white;

  background:#725ee7;

}



.btn-cancel {

  border:1px solid #dfe3e9;

  color:#646c7a;

  background:white;

}



/* 반응형 */

@media(max-width:700px){

  .top-actions{

    flex-direction:column;

  }


  .coupon-table{

    min-width:700px;

  }

}

</style>