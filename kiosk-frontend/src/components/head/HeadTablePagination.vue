<script setup>
import { computed } from 'vue'

const props = defineProps({
  totalItems: { type: Number, default: 0 },
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 }
})

const emit = defineEmits(['update:currentPage', 'update:pageSize'])

/*
 * 본사 목록 화면 공통 페이징 계산
 *
 * 부모 화면이 전달한 전체 건수·현재 페이지·페이지당 건수를 기준으로
 * 총 페이지를 계산하고, 페이지 번호는 1~5 / 6~10처럼 5개씩 노출합니다.
 * 버튼 클릭 결과는 v-model 이벤트로 부모 화면에 다시 전달됩니다.
 */
const totalPages = computed(() =>
  Math.max(1, Math.ceil(props.totalItems / props.pageSize))
)
const groupStart = computed(() =>
  Math.floor((props.currentPage - 1) / 5) * 5 + 1
)
const groupEnd = computed(() =>
  Math.min(groupStart.value + 4, totalPages.value)
)
const visiblePages = computed(() =>
  Array.from(
    { length: groupEnd.value - groupStart.value + 1 },
    (_, index) => groupStart.value + index
  )
)

/*
 * 페이지당 건수를 바꾸면 새 범위에서 빈 페이지가 보이지 않도록
 * 부모의 현재 페이지도 1로 초기화합니다.
 */
const changePageSize = (event) => {
  emit('update:pageSize', Number(event.target.value))
  emit('update:currentPage', 1)
}
</script>

<template>
  <div class="table-pagination">
    <label>
      페이지당
      <select :value="pageSize" @change="changePageSize">
        <option :value="10">10개</option>
        <option :value="20">20개</option>
        <option :value="30">30개</option>
      </select>
    </label>

    <nav aria-label="목록 페이지">
      <button :disabled="currentPage === 1" @click="$emit('update:currentPage', 1)">처음</button>
      <button :disabled="groupStart === 1" @click="$emit('update:currentPage', groupStart - 1)">&lt;</button>
      <button
        v-for="page in visiblePages"
        :key="page"
        :class="{ active: page === currentPage }"
        @click="$emit('update:currentPage', page)"
      >{{ page }}</button>
      <button :disabled="groupEnd === totalPages" @click="$emit('update:currentPage', groupEnd + 1)">&gt;</button>
      <button :disabled="currentPage === totalPages" @click="$emit('update:currentPage', totalPages)">마지막</button>
    </nav>

    <span>총 {{ totalItems.toLocaleString() }}건</span>
  </div>
</template>

<style scoped>
.table-pagination {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
  padding: 16px 20px;
  border: 1px solid #eceef3;
  border-radius: 10px;
  background: #fafbfc;
}
.table-pagination label { display: flex; align-items: center; gap: 10px; justify-self: start; }
.table-pagination select { padding: 7px 10px; border: 1px solid #d8dbe8; border-radius: 6px; background: #fff; }
.table-pagination nav { display: flex; justify-content: center; gap: 6px; }
.table-pagination button { min-width: 38px; padding: 8px 10px; border: 1px solid #d8dbe8; border-radius: 6px; background: #fff; cursor: pointer; }
.table-pagination button.active { border-color: #6d5de2; background: #6d5de2; color: #fff; }
.table-pagination button:disabled { cursor: not-allowed; opacity: .4; }
.table-pagination > span { text-align: right; color: #6b7280; justify-self: end; }
@media (max-width: 760px) {
  .table-pagination { grid-template-columns: 1fr; }
  .table-pagination > span { text-align: left; }
  .table-pagination nav { flex-wrap: wrap; justify-content: flex-start; }
}
</style>
