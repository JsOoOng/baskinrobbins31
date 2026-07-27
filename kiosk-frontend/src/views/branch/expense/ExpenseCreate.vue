<!--
  [화면 흐름 안내] ExpenseCreate
  역할: 지점 운영에서 사용자가 보는 화면이다.
  진입: /branch/expense -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/axios -> 응답/상태 반영
  다음 이동: /branch/main
-->
<template>
  <div class="expense-container">

    <h2>지출 등록</h2>


    <div class="form-group">
      <label>금액</label>
      <input
        type="number"
        v-model="expense.amount"
        placeholder="금액 입력"
      />
    </div>


    <div class="form-group">
      <label>지출 날짜</label>
      <input
        type="date"
        v-model="expense.expenseDate"
      />
    </div>


    <div class="form-group">
      <label>지출 내용</label>
      <input
        type="text"
        v-model="expense.description"
        list="expense-history"
        placeholder="내용 입력"
      />

      <datalist id="expense-history">

        <option
          v-for="history in histories"
          :key="history.description"
          :value="history.description"
        ></option>

      </datalist>
    </div>


    <div class="form-group">
      <label>지출 분류</label>

      <select v-model="expense.expenseCategory">

        <option value="RENT">
          임대료
        </option>

        <option value="UTILITY">
          공과금
        </option>

        <option value="LABOR">
          인건비
        </option>

        <option value="SUPPLIES">
          비품
        </option>

        <option value="MAINTENANCE">
          유지보수
        </option>

        <option value="INGREDIENTS">
          재료비
        </option>

        <option value="INSURANCE">
          보험
        </option>

        <option value="ETC">
          기타
        </option>

      </select>

    </div>



    <div class="form-group">

      <label>
        결제 방법
      </label>


      <select v-model="expense.paymentMethod">

        <option value="CARD">
          카드
        </option>

        <option value="CASH">
          현금
        </option>

        <option value="TRANSFER">
          계좌이체
        </option>

      </select>

    </div>



    <button @click="saveExpense">
      등록
    </button>

    <button @click="goBack">
      돌아가기
    </button>


  </div>
</template>


<script setup>

import { reactive, ref, onMounted, watch } from "vue";
import axios from "@/api/axios";
import { useRouter } from 'vue-router'

// 뒤로가기onMounted
const goBack = () => {

router.push('/branch/main')

}

const branchUser = JSON.parse(
  localStorage.getItem("branchUser") || "{}"
);

const router = useRouter()

const expense = reactive({

storeId: branchUser.storeId,

employeeId: branchUser.employeeId,

staffId: null,

amount: null,

expenseDate: "",

description: "",

expenseCategory: "",

paymentMethod: "",

receiptUrl: null

});

const histories = ref([]);

const loadExpenseHistory = async () => {

try {

  const response = await axios.get(
    "/branch/expense/history",
    {
      params: {
        storeId: expense.storeId
      }
    }
  );

  histories.value = response.data;

} catch (error) {

  console.error(error);

}

};


onMounted(() => {
  loadExpenseHistory();
});

watch(
  () => expense.description,
  (value) => {

    const item = histories.value.find(
      history => history.description === value
    );

    if (item) {

      expense.expenseCategory = item.expenseCategory;
      expense.paymentMethod = item.paymentMethod;

    } else {

      expense.expenseCategory = "";
      expense.paymentMethod = "";

    }

  }
);


const saveExpense = async () => {

try {

  await axios.post(
    "/branch/expense",
    expense
  );

  // 자동완성 목록 새로고침
  await loadExpenseHistory();

  alert("지출 등록 완료");

  expense.amount = null;
  expense.description = "";
  expense.expenseCategory = "";
  expense.paymentMethod = "";
  expense.receiptUrl = null;

} catch (error) {

  console.error(error);

  alert("등록 실패");

}

}

</script>


<style scoped>

.expense-container {

  width:400px;
  margin:30px;

}


.form-group {

  margin-bottom:15px;

}


label {

  display:block;
  margin-bottom:5px;

}


input,
select {

  width:100%;
  padding:8px;

}


button {

  width:100%;
  padding:10px;

}

</style>