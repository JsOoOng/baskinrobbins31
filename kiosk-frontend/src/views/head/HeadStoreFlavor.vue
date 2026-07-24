<!--
  [화면 흐름 안내] HeadStoreFlavor
  역할: 본사 관리에서 사용자가 보는 화면이다.
  진입: /head/storeFlavor -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/headquarter/headStoreFlavorApi -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<template>

    <AppMessageToast
      :message="message"
      :type="messageType"
      @close="clearMessage"
    />
    
    
    <section class="inventory-page">
    
      <!-- Header -->
      <header class="page-header">
    
        <div>
    
          <p class="page-eyebrow">
            FLAVOR INVENTORY MANAGEMENT
          </p>
    
    
          <div class="inventory-title-row">
            <h1>전체 맛 재고 현황</h1>
            <!-- 상품 수량 재고와 아이스크림 통 재고를 서로 다른 화면·API로 분리합니다. -->
            <nav class="inventory-tabs">
              <RouterLink to="/head/inventory">상품</RouterLink>
              <RouterLink class="active" to="/head/storeFlavor">아이스크림 맛</RouterLink>
            </nav>
          </div>
    
    
          <p class="page-description">
            전체 지점의 아이스크림 맛 재고와 자동 보충 설정을 관리합니다.
          </p>
    
        </div>
    
    
        <button
          type="button"
          class="refresh-button"
          :disabled="loading"
          @click="loadInventories"
        >
    
          {{ loading ? '조회 중...' : '새로고침' }}
    
        </button>
    
      </header>
    
    
    
      <!-- Summary -->
      <div class="summary-grid">
    
    
        <article class="summary-card">
    
          <span class="summary-label">
            전체 맛 재고
          </span>
    
    
          <strong class="summary-value">
            {{ formatNumber(inventories.length) }}
          </strong>
    
    
          <span class="summary-unit">
            건
          </span>
    
        </article>
    
    
    
    
        <article class="summary-card">
    
          <span class="summary-label">
            품절 맛
          </span>
    
    
          <strong class="summary-value danger">
            {{ formatNumber(soldOutCount) }}
          </strong>
    
    
          <span class="summary-unit">
            건
          </span>
    
        </article>
    
    
    
    
    
        <article class="summary-card">
    
          <span class="summary-label">
            자동 보충 사용
          </span>
    
    
          <strong class="summary-value success">
            {{ formatNumber(enabledCount) }}
          </strong>
    
    
          <span class="summary-unit">
            건
          </span>
    
        </article>
    
    
    
    
    
        <article class="summary-card">
    
          <span class="summary-label">
            자동 보충 중지
          </span>
    
    
          <strong class="summary-value muted">
            {{ formatNumber(disabledCount) }}
          </strong>
    
    
          <span class="summary-unit">
            건
          </span>
    
        </article>
    
    
      </div>
    
    
    
    
    
      <!-- Filter -->
      <section class="filter-panel">
    
    
        <div class="filter-field search-field">
    
          <label>
            검색
          </label>
    
    
          <input
            v-model.trim="keyword"
            type="text"
            placeholder="지점명 또는 맛 이름 검색"
          />
    
        </div>
    
    
    
    
    
        <div class="filter-field">
    
          <label>
            지점
          </label>
    
    
          <select
            v-model="selectedStoreId"
          >
    
            <option value="">
              전체 지점
            </option>
    
    
            <option
              v-for="store in storeOptions"
              :key="store.storeId"
              :value="String(store.storeId)"
            >
    
              {{ store.storeName }}
    
            </option>
    
    
          </select>
    
        </div>
    
    
    
    
    
        <div class="filter-field">
    
          <label>
            자동 보충 방식
          </label>
    
    
          <select
            v-model="selectedRestockMode"
          >
    
            <option value="">
              전체 방식
            </option>
    
    
            <option value="THRESHOLD">
              임계 재고
            </option>
    
    
            <option value="DAILY">
              정기 보충
            </option>
    
    
            <option value="BOTH">
              임계 + 정기
            </option>
    
    
          </select>
    
        </div>
    
    
    
    
    
        <div class="filter-field">
    
          <label>
            상태
          </label>
    
    
          <select
            v-model="selectedStatus"
          >
    
            <option value="">
              전체 상태
            </option>
    
    
            <option value="SOLD_OUT">
              품절
            </option>
    
    
            <option value="NORMAL">
              판매 가능
            </option>
    
    
          </select>
    
    
        </div>
    
    
    
    
    
        <button
          type="button"
          class="reset-button"
          @click="resetFilters"
        >
    
          필터 초기화
    
        </button>
    
    
    
      </section>
    
    
    
    
    
      <!-- Table -->
      <section class="table-panel">
    
    
        <div class="table-header">
    
          <div>
    
            <h2>
              맛 재고 목록
            </h2>
    
    
            <p>
              검색 결과
              {{ formatNumber(filteredInventories.length) }}건
            </p>
    
          </div>
    
        </div>
    
    
    
    
    
        <div
          v-if="loading"
          class="empty-state"
        >
    
          맛 재고 정보를 불러오는 중입니다.
    
        </div>
    
    
    
    
    
        <div
          v-else-if="paginatedInventories.length === 0"
          class="empty-state"
        >
    
          조건에 맞는 맛 재고 정보가 없습니다.
    
        </div>
    
    
    
    
    
        <div
          v-else
          class="table-scroll"
        >
    
    
          <table class="inventory-table">
    
    
            <thead>
    
              <tr>
    
                <th>
                  번호
                </th>
    
    
                <th>
                  지점
                </th>
    
    
                <th>
                  맛
                </th>
    
    
                <th>
                  현재 통
                </th>
    
    
                <th>
                  상태
                </th>
    
    
                <th>
                  최소 재고
                </th>
    
    
                <th>
                  목표 재고
                </th>
    
    
                <th>
                  보충 방식
                </th>
    
    
                <th>
                  자동 보충
                </th>
    
    
                <th>
                  최근 변경
                </th>
    
    
                <th>
                  관리
                </th>
    
    
              </tr>
    
            </thead>
    
    
    
    
    
            <tbody>
    
    
              <tr
                v-for="flavor in paginatedInventories"
                :key="flavor.storeFlavorId"
              >
    
    
                <td>
                  {{ flavor.storeFlavorId }}
                </td>
    
    
    
                <td>
    
                  <strong class="store-name">
    
                    {{ flavor.storeName }}
    
                  </strong>
    
                </td>
    
    
    
    
                <td>
    
                  <div class="item-cell">
    
                    <strong>
                      {{ flavor.flavorName }}
                    </strong>
    
    
                    <span>
                      맛 번호 {{ flavor.flavorId }}
                    </span>
    
    
                  </div>
    
                </td>
    
    
    
    
    
                <td>
    
                  <strong
                    :class="{
                      'stock-danger':
                        flavor.container <= flavor.minStock
                    }"
                  >
    
                    {{ formatNumber(flavor.container) }}
    
                  </strong>
    
    
                </td>
    
    
    
    
    
                <td>
    
                  <span
                    class="status-badge"
                    :class="
                      flavor.isSoldOut
                      ? 'status-low'
                      : 'status-normal'
                    "
                  >
    
                    {{
                      flavor.isSoldOut
                      ? '품절'
                      : '판매 가능'
                    }}
    
                  </span>
    
    
                </td>
    
    
    
    
    
                <td>
                  {{ formatNumber(flavor.minStock) }}
                </td>
    
    
    
                <td>
                  {{ formatNumber(flavor.targetStock) }}
                </td>
    
    
    
    
    
                <td>
    
                  <span class="mode-label">
    
                    {{ getRestockModeLabel(flavor.restockMode) }}
    
                  </span>
    
    
                </td>
    
    
    
    
    
                <td>
    
    
                  <button
                    type="button"
                    class="enabled-toggle-button"
                    :class="{
                      active:
                        flavor.autoRestockEnabled
                    }"
                    :disabled="
                      togglingFlavorId ===
                        flavor.storeFlavorId
                    "
                    @click="toggleAutoRestock(flavor)"
                  >
    
                    {{
                      togglingFlavorId ===
                        flavor.storeFlavorId
                      ? '변경 중...'
                      : flavor.autoRestockEnabled
                      ? '사용'
                      : '중지'
                    }}
    
                  </button>
    
    
                </td>
    
    
    
    
    
                <td>
    
                  {{ formatDate(flavor.lastUpdated) }}
    
                </td>
    
    
    
    
    
                <td>
    
                  <div class="action-buttons">
                    <button
                      type="button"
                      class="stock-in-button"
                      @click="openStockInModal(flavor)"
                    >
                      통 입고
                    </button>
                    <button
                      type="button"
                      class="setting-button"
                      @click="openSettingModal(flavor)"
                    >
                      설정
                    </button>
                  </div>
    
    
                </td>
    
    
              </tr>
    
    
            </tbody>
    
    
          </table>
    
    
        </div>
    
    
    
    
    
        <!-- Pagination -->
    
        <nav
          v-if="totalPages > 1"
          class="pagination"
        >
    
          <button
            type="button"
            :disabled="currentPage === 1"
            @click="changePage(1)"
          >
    
            처음
    
          </button>
    
    
    
          <button
            type="button"
            :disabled="pageGroupStart === 1"
            @click="movePreviousPageGroup"
          >
    
            ‹
    
          </button>
    
    
    
          <button
            v-for="page in visiblePages"
            :key="page"
            type="button"
            class="page-number"
            :class="{
              active:
                currentPage === page
            }"
            @click="changePage(page)"
          >
    
            {{ page }}
    
          </button>
    
    
    
    
          <button
            type="button"
            :disabled="pageGroupEnd >= totalPages"
            @click="moveNextPageGroup"
          >
    
            ›
    
          </button>
    
    
    
          <button
            type="button"
            :disabled="currentPage === totalPages"
            @click="changePage(totalPages)"
          >
    
            마지막
    
          </button>
    
    
        </nav>
    
    
    
      </section>
    
    </section>
    
    
    
    
    
    
    <!-- Setting Modal -->
    
    <Teleport to="body">
    
    
    <div
      v-if="settingModalOpen"
      class="modal-backdrop"
      @click.self="closeSettingModal"
    >
    
    
    <section class="setting-modal">
    
    
    <header class="modal-header">
    
    
    <div>
    
    <p class="modal-eyebrow">
    AUTO RESTOCK
    </p>
    
    
    <h2>
    맛 자동 보충 설정
    </h2>
    
    
    </div>
    
    
    <button
     type="button"
     class="modal-close"
     @click="closeSettingModal"
    >
    
    ×
    
    
    </button>
    
    
    </header>
    
    
    
    
    
    <div
     v-if="selectedFlavor"
     class="selected-summary"
    >
    
    
    <strong>
    
    {{ selectedFlavor.storeName }}
    
    </strong>
    
    
    <span>
    
    {{ selectedFlavor.flavorName }}
    
    </span>
    
    
    <span>
    
    현재 통 :
    {{ formatNumber(selectedFlavor.container) }}
    
    </span>
    
    
    </div>
    
    
    
    
    
    <form
     class="setting-form"
     @submit.prevent="saveRestockSetting"
    >
    
    
    <label class="toggle-row">
    
    
    <div>
    
    <strong>
    자동 보충 사용
    </strong>
    
    
    <span>
    비활성화하면 자동 보충하지 않습니다.
    </span>
    
    
    </div>
    
    
    <input
     type="checkbox"
     v-model="settingForm.autoRestockEnabled"
    />
    
    
    </label>
    
    
    
    
    
    <div class="form-field">
    
    
    <label>
    자동 보충 방식
    </label>
    
    
    <select
    v-model="settingForm.restockMode"
    >
    
    <option value="THRESHOLD">
    최소 재고 이하 보충
    </option>
    
    
    <option value="DAILY">
    정기 보충
    </option>
    
    
    <option value="BOTH">
    임계 + 정기
    </option>
    
    
    </select>
    
    
    </div>
    
    
    
    
    
    <div class="number-grid">
    
    
    <div class="form-field">
    
    
    <label>
    최소 재고
    </label>
    
    
    <input
    type="number"
    v-model.number="settingForm.minStock"
    />
    
    
    </div>
    
    
    
    
    <div class="form-field">
    
    
    <label>
    목표 재고
    </label>
    
    
    <input
    type="number"
    v-model.number="settingForm.targetStock"
    />
    
    
    </div>
    
    
    </div>
    
    
    
    
    
    <footer class="modal-actions">
    
    
    <button
    type="button"
    class="cancel-button"
    @click="closeSettingModal"
    >
    
    취소
    
    </button>
    
    
    
    <button
    type="submit"
    class="save-button"
    :disabled="saving"
    >
    
    {{ saving ? '저장 중...' : '저장' }}
    
    
    </button>
    
    
    </footer>
    
    
    </form>
    
    
    </section>
    
    
    </div>
    
    
    </Teleport>

    <Teleport to="body">
      <div
        v-if="stockInModalOpen"
        class="modal-backdrop"
        @click.self="closeStockInModal"
      >
        <section class="setting-modal stock-in-modal">
          <header class="modal-header">
            <div>
              <p class="modal-eyebrow">FLAVOR STOCK IN</p>
              <h2>아이스크림 맛 통 입고</h2>
            </div>
            <button
              type="button"
              class="modal-close"
              @click="closeStockInModal"
            >
              ×
            </button>
          </header>

          <div v-if="selectedStockInFlavor" class="selected-summary">
            <strong>{{ selectedStockInFlavor.storeName }}</strong>
            <span>{{ selectedStockInFlavor.flavorName }}</span>
            <span>현재 재고: {{ formatNumber(selectedStockInFlavor.container) }}통</span>
          </div>

          <form class="setting-form" @submit.prevent="submitStockIn">
            <div class="form-field">
              <label for="stock-in-container">입고할 통 수</label>
              <input
                id="stock-in-container"
                v-model.number="stockInQuantity"
                type="number"
                min="1"
                step="1"
                required
              />
              <small class="field-help">
                일반 상품 개수가 아닌 아이스크림 컨테이너(통) 단위로 반영됩니다.
              </small>
            </div>

            <footer class="modal-actions">
              <button
                type="button"
                class="cancel-button"
                @click="closeStockInModal"
              >
                취소
              </button>
              <button
                type="submit"
                class="save-button"
                :disabled="stockingIn"
              >
                {{ stockingIn ? '입고 중...' : '입고 처리' }}
              </button>
            </footer>
          </form>
        </section>
      </div>
    </Teleport>
    
    
    </template>

<script setup>

import {
    computed,
    onMounted,
    reactive,
    ref,
    watch
} from 'vue'


import AppMessageToast
    from '@/components/common/AppMessageToast.vue'


import {
    getHeadStoreFlavorInventories,
    stockInHeadStoreFlavor,
    updateHeadStoreFlavorRestockSetting
} from '@/api/headquarter/headStoreFlavorApi'



const inventories = ref([])

const loading = ref(false)

const saving = ref(false)
const togglingFlavorId = ref(null)
const stockInModalOpen = ref(false)
const selectedStockInFlavor = ref(null)
const stockInQuantity = ref(1)
const stockingIn = ref(false)



const message = ref('')

const messageType = ref('success')



const keyword = ref('')

const selectedStoreId = ref('')

const selectedRestockMode = ref('')

const selectedStatus = ref('')



const currentPage = ref(1)

const pageSize = 10

const pageGroupSize = 5



const settingModalOpen = ref(false)

const selectedFlavor = ref(null)



const settingForm = reactive({

    minStock: 1,

    targetStock: 10,

    autoRestockEnabled: true,

    restockMode: 'THRESHOLD'

})





/*
 * Toast
 */
const showMessage = (
    text,
    type = 'success'
) => {

    message.value = text

    messageType.value = type

}



const clearMessage = () => {

    message.value = ''

}





/*
 * Error
 */
const extractErrorMessage = (
    error,
    fallback
) => {

    return (
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        fallback
    )

}





/*
 * Load
 */
const loadInventories = async () => {


    loading.value = true

    clearMessage()



    try {


        const response =
            await getHeadStoreFlavorInventories()



        inventories.value =
            response.data ?? []



    } catch(error){


        inventories.value = []


        showMessage(
            extractErrorMessage(
                error,
                '맛 재고 정보를 불러오지 못했습니다.'
            ),
            'error'
        )


    } finally {


        loading.value = false

    }

}







/*
 * Store Filter
 */
const storeOptions = computed(()=>{


    const map = new Map()



    inventories.value.forEach(
        flavor => {


            if(!map.has(flavor.storeId)){


                map.set(
                    flavor.storeId,
                    {
                        storeId:
                            flavor.storeId,

                        storeName:
                            flavor.storeName
                    }
                )

            }


        }
    )



    return Array
        .from(map.values())
        .sort(
            (a,b)=>
                a.storeId-b.storeId
        )


})







/*
 * Status
 */
const getStatus = (flavor)=>{


    return flavor.isSoldOut
        ? 'SOLD_OUT'
        : 'NORMAL'


}



const soldOutCount = computed(()=>{


    return inventories.value.filter(
        flavor =>
            flavor.isSoldOut
    ).length


})





const enabledCount = computed(()=>{


    return inventories.value.filter(
        flavor =>
            flavor.autoRestockEnabled
    ).length


})





const disabledCount = computed(()=>{


    return inventories.value.length
        -
        enabledCount.value


})







/*
 * Filter
 */
const filteredInventories = computed(()=>{


    const key =
        keyword.value
            .trim()
            .toLowerCase()



    return inventories.value.filter(
        flavor=>{


            const matchKeyword =
                !key ||
                flavor.storeName
                    ?.toLowerCase()
                    .includes(key)
                ||
                flavor.flavorName
                    ?.toLowerCase()
                    .includes(key)



            const matchStore =
                !selectedStoreId.value
                ||
                String(flavor.storeId)
                ===
                selectedStoreId.value



            const matchMode =
                !selectedRestockMode.value
                ||
                flavor.restockMode
                ===
                selectedRestockMode.value




            const matchStatus =
                !selectedStatus.value
                ||
                getStatus(flavor)
                ===
                selectedStatus.value



            return (
                matchKeyword &&
                matchStore &&
                matchMode &&
                matchStatus
            )

        }
    )


})







/*
 * Pagination
 */
const totalPages = computed(()=>{


    return Math.max(
        1,
        Math.ceil(
            filteredInventories.value.length
            /
            pageSize
        )
    )

})





const paginatedInventories = computed(()=>{


    const start =
        (currentPage.value - 1)
        *
        pageSize



    return filteredInventories.value.slice(
        start,
        start + pageSize
    )

})





const pageGroupStart = computed(()=>{


    return (
        Math.floor(
            (currentPage.value-1)
            /
            pageGroupSize
        )
        *
        pageGroupSize
    )
    +1


})





const pageGroupEnd = computed(()=>{


    return Math.min(
        pageGroupStart.value
        +
        pageGroupSize
        -
        1,

        totalPages.value
    )


})





const visiblePages = computed(()=>{


    const pages=[]


    for(
        let i=pageGroupStart.value;
        i<=pageGroupEnd.value;
        i++
    ){

        pages.push(i)

    }


    return pages

})





const changePage=(page)=>{


    if(
        page<1 ||
        page>totalPages.value
    ){

        return

    }


    currentPage.value=page

}





const movePreviousPageGroup=()=>{


    changePage(
        pageGroupStart.value
        -
        pageGroupSize
    )


}





const moveNextPageGroup=()=>{


    changePage(
        pageGroupStart.value
        +
        pageGroupSize
    )


}





const resetFilters=()=>{


    keyword.value=''

    selectedStoreId.value=''

    selectedRestockMode.value=''

    selectedStatus.value=''

    currentPage.value=1


}







/*
 * Modal
 */
const openSettingModal=(flavor)=>{


    selectedFlavor.value =
        flavor



    settingForm.minStock =
        flavor.minStock



    settingForm.targetStock =
        flavor.targetStock



    settingForm.autoRestockEnabled =
        flavor.autoRestockEnabled



    settingForm.restockMode =
        flavor.restockMode



    settingModalOpen.value=true


}





const closeSettingModal=()=>{


    if(saving.value){

        return

    }


    settingModalOpen.value=false

    selectedFlavor.value=null


}

/*
 * PATCH 응답으로 받은 최신 맛 재고 한 건을 현재 표의 같은 ID 위치에 교체합니다.
 * 전체 GET 재조회 없이 자동 보충·입고 결과가 즉시 화면에 반영됩니다.
 */
const replaceInventory = (updatedFlavor) => {
    if (!updatedFlavor?.storeFlavorId) return

    const index = inventories.value.findIndex(
        flavor =>
            flavor.storeFlavorId ===
            updatedFlavor.storeFlavorId
    )

    if (index >= 0) {
        inventories.value.splice(index, 1, updatedFlavor)
    }
}

/*
 * 목록의 자동 보충 사용/중지 버튼 처리
 *
 * 현재 행의 설정을 유지한 채 autoRestockEnabled만 반전
 * → PATCH /head/flavor-inventory/{storeFlavorId}
 * → 응답 DTO로 해당 행 교체 순서로 동작합니다.
 * 설정값이 잘못된 상태에서 켜려 하면 설정 모달로 이동합니다.
 */
const toggleAutoRestock = async (flavor) => {
    if (!flavor || togglingFlavorId.value !== null) return

    const nextEnabled = !flavor.autoRestockEnabled
    const minStock = Number(flavor.minStock ?? 0)
    const targetStock = Number(flavor.targetStock ?? 0)

    if (
        nextEnabled &&
        (targetStock < 1 || targetStock <= minStock)
    ) {
        openSettingModal(flavor)
        settingForm.autoRestockEnabled = true
        showMessage(
            '자동 보충을 사용하려면 최소 통 수와 목표 통 수를 먼저 설정해주세요.',
            'info'
        )
        return
    }

    togglingFlavorId.value = flavor.storeFlavorId

    try {
        const response =
            await updateHeadStoreFlavorRestockSetting(
                flavor.storeFlavorId,
                {
                    minStock,
                    targetStock,
                    autoRestockEnabled: nextEnabled,
                    restockMode:
                        flavor.restockMode ?? 'THRESHOLD'
                }
            )

        replaceInventory(response.data)
        showMessage(
            nextEnabled
                ? '맛 자동 보충을 사용하도록 변경했습니다.'
                : '맛 자동 보충을 중지했습니다.'
        )
    } catch (error) {
        showMessage(
            extractErrorMessage(
                error,
                '맛 자동 보충 상태를 변경하지 못했습니다.'
            ),
            'error'
        )
    } finally {
        togglingFlavorId.value = null
    }
}

/*
 * 선택한 지점·맛 정보를 보관하고 통 단위 입고 모달을 엽니다.
 */
const openStockInModal = (flavor) => {
    selectedStockInFlavor.value = flavor
    stockInQuantity.value = 1
    stockInModalOpen.value = true
}

/*
 * 입고 요청 중에는 중복 조작을 막고, 닫을 때 선택 행과 입력 수량을 초기화합니다.
 */
const closeStockInModal = () => {
    if (stockingIn.value) return

    stockInModalOpen.value = false
    selectedStockInFlavor.value = null
    stockInQuantity.value = 1
}

/*
 * 맛 재고 통 입고 처리
 *
 * 입력값 정수 검증 → PATCH /head/flavor-inventory/{id}/stock-in
 * → 백엔드 StoreFlavor.increaseStock() → 응답 행 교체 → 모달 종료 순서입니다.
 */
const submitStockIn = async () => {
    const quantity = Number(stockInQuantity.value)

    if (
        !selectedStockInFlavor.value ||
        !Number.isInteger(quantity) ||
        quantity < 1
    ) {
        showMessage(
            '입고할 통 수는 1 이상의 정수여야 합니다.',
            'error'
        )
        return
    }

    stockingIn.value = true

    try {
        const response =
            await stockInHeadStoreFlavor(
                selectedStockInFlavor.value.storeFlavorId,
                quantity
            )

        replaceInventory(response.data)
        stockInModalOpen.value = false
        selectedStockInFlavor.value = null
        stockInQuantity.value = 1
        showMessage(
            `${quantity.toLocaleString('ko-KR')}통 입고 처리가 완료되었습니다.`
        )
    } catch (error) {
        showMessage(
            extractErrorMessage(
                error,
                '맛 재고 입고 처리에 실패했습니다.'
            ),
            'error'
        )
    } finally {
        stockingIn.value = false
    }
}







/*
 * Save
 */
const saveRestockSetting=async()=>{


    if(!selectedFlavor.value){

        return

    }



    saving.value=true



    try{


        const response =
            await updateHeadStoreFlavorRestockSetting(

            selectedFlavor.value.storeFlavorId,

            {

                minStock:
                    settingForm.minStock,


                targetStock:
                    settingForm.targetStock,


                autoRestockEnabled:
                    settingForm.autoRestockEnabled,


                restockMode:
                    settingForm.restockMode

            }

        )

        replaceInventory(response.data)


        showMessage(
            '맛 자동 보충 설정이 저장되었습니다.'
        )


        settingModalOpen.value=false


    }catch(error){


        showMessage(
            extractErrorMessage(
                error,
                '설정 저장 실패'
            ),
            'error'
        )


    }finally{


        saving.value=false


    }


}







/*
 * Utils
 */
const getRestockModeLabel=(mode)=>{


    const map={

        THRESHOLD:'임계 재고',

        DAILY:'정기 보충',

        BOTH:'임계 + 정기'

    }


    return map[mode] ?? '-'


}





const formatNumber=(value)=>{


    return Number(value ?? 0)
        .toLocaleString('ko-KR')


}





const formatDate=(value)=>{


    if(!value){

        return '-'

    }


    return new Intl.DateTimeFormat(
        'ko-KR',
        {

            year:'numeric',

            month:'2-digit',

            day:'2-digit',

            hour:'2-digit',

            minute:'2-digit'

        }
    )
    .format(
        new Date(value)
    )


}







watch(
    [
        keyword,
        selectedStoreId,
        selectedRestockMode,
        selectedStatus
    ],

    ()=>{

        currentPage.value=1

    }

)





watch(
    totalPages,

    value=>{

        if(currentPage.value>value){

            currentPage.value=value

        }

    }

)





onMounted(()=>{


    loadInventories()


})



</script>

<style scoped>

.inventory-page {

    display:flex;

    flex-direction:column;

    gap:24px;

}



.page-header {

    display:flex;

    align-items:flex-start;

    justify-content:space-between;

    gap:24px;

}



.page-eyebrow,

.modal-eyebrow {

    margin:0 0 8px;

    color:#7b8497;

    font-size:12px;

    font-weight:800;

    letter-spacing:.12em;

}



.page-header h1 {

    margin:0;

    color:#252a38;

    font-size:30px;

}
.inventory-title-row { display:flex; align-items:center; gap:18px; flex-wrap:wrap; }
.inventory-tabs { display:flex; gap:6px; }
.inventory-tabs a { padding:8px 14px; border:1px solid #dfe3ea; border-radius:8px; color:#697083; background:#fff; text-decoration:none; font-size:13px; font-weight:700; }
.inventory-tabs a.active { border-color:#6d5de2; background:#6d5de2; color:#fff; }



.page-description {

    margin:10px 0 0;

    color:#71798b;

    font-size:14px;

}



.refresh-button,
.reset-button,
.setting-button,
.cancel-button,
.save-button {

    border:0;

    border-radius:10px;

    cursor:pointer;

    font-weight:700;

}



.refresh-button {

    padding:11px 17px;

    background:#30384b;

    color:white;

}



.refresh-button:disabled,
.save-button:disabled {

    cursor:not-allowed;

    opacity:.6;

}



/* Summary */

.summary-grid {

    display:grid;

    grid-template-columns:
        repeat(4,minmax(0,1fr));

    gap:16px;

}



.summary-card {

    padding:22px;

    border:1px solid #e4e7ee;

    border-radius:16px;

    background:white;

    box-shadow:
        0 8px 24px rgba(30,35,48,.04);

}



.summary-label {

    display:block;

    margin-bottom:14px;

    color:#747c8e;

    font-size:13px;

    font-weight:700;

}



.summary-value {

    color:#292e3b;

    font-size:30px;

}



.summary-value.danger {

    color:#d64e4e;

}



.summary-value.success {

    color:#16855f;

}



.summary-value.muted {

    color:#7b8497;

}



.summary-unit {

    margin-left:5px;

    color:#858c9b;

    font-size:13px;

}



/* Filter */

.filter-panel {

    display:grid;

    grid-template-columns:

        minmax(220px,1.5fr)

        repeat(3,minmax(150px,1fr))

        auto;

    gap:14px;

    padding:20px;

    border:1px solid #e4e7ee;

    border-radius:16px;

    background:white;

    align-items:end;

}



.filter-field,
.form-field {

    display:flex;

    flex-direction:column;

    gap:8px;

}



.filter-field label,
.form-field label {

    color:#565e70;

    font-size:13px;

    font-weight:700;

}



.filter-field input,
.filter-field select,
.form-field input,
.form-field select {

    min-height:42px;

    padding:0 12px;

    border:1px solid #d9dde7;

    border-radius:9px;

    background:white;

    outline:none;

}



.filter-field input:focus,
.filter-field select:focus,
.form-field input:focus,
.form-field select:focus {

    border-color:#6c768d;

    box-shadow:
        0 0 0 3px rgba(70,82,111,.1);

}



.reset-button {

    height:42px;

    padding:0 16px;

    background:#eef0f5;

    color:#535b6e;

}



/* Table */

.table-panel {

    overflow:hidden;

    border:1px solid #e4e7ee;

    border-radius:16px;

    background:white;

}



.table-header {

    padding:20px 22px;

    border-bottom:1px solid #ebedf2;

}



.table-header h2 {

    margin:0;

    color:#303542;

    font-size:18px;

}



.table-header p {

    margin:6px 0 0;

    color:#858c9b;

    font-size:13px;

}



.table-scroll {

    overflow-x:auto;

}



.inventory-table {

    width:100%;

    min-width:1200px;

    border-collapse:collapse;

}



.inventory-table th,
.inventory-table td {

    padding:15px 13px;

    border-bottom:1px solid #eef0f4;

    text-align:center;

    white-space:nowrap;

}



.inventory-table th {

    background:#f8f9fb;

    color:#626a7c;

    font-size:12px;

    font-weight:800;

}



.inventory-table td {

    color:#454c5d;

    font-size:13px;

}



.inventory-table tbody tr:hover {

    background:#fafbfc;

}



.store-name {

    color:#303746;

}



.item-cell {

    display:flex;

    flex-direction:column;

    align-items:flex-start;

    gap:4px;

}



.item-cell span {

    color:#9299a7;

    font-size:11px;

}



.stock-danger {

    color:#d64e4e;

}



/* Badge */

.status-badge,
.enabled-badge,
.enabled-toggle-button {

    display:inline-flex;

    justify-content:center;

    align-items:center;

    min-width:70px;

    padding:6px 10px;

    border-radius:999px;

    font-size:11px;

    font-weight:800;

}

.enabled-toggle-button {
    border:0;
    cursor:pointer;
    color:#8b93a3;
    background:#eceef3;
}

.enabled-toggle-button.active {
    color:#159b68;
    background:#dff9ed;
}

.enabled-toggle-button:disabled {
    cursor:wait;
    opacity:.65;
}



.status-low {

    background:#fff0f0;

    color:#cf4242;

}



.status-normal {

    background:#eef6ff;

    color:#356ca8;

}



.enabled-badge {

    background:#f0f1f4;

    color:#747b89;

}



.enabled-badge.active {

    background:#ecf8f3;

    color:#187655;

}



.mode-label {

    font-weight:700;

    color:#596174;

}



.setting-button {

    padding:7px 12px;

    background:#30384b;

    color:white;

    font-size:12px;

}

.action-buttons {
    display:flex;
    justify-content:center;
    gap:7px;
}

.stock-in-button {
    padding:7px 12px;
    border:0;
    border-radius:8px;
    cursor:pointer;
    color:#fff;
    background:#6d5de2;
    font-size:12px;
    font-weight:700;
}

.field-help {
    color:#858c9b;
    font-size:11px;
    line-height:1.5;
}



/* Empty */

.empty-state {

    padding:70px 20px;

    text-align:center;

    color:#858c9b;

}



/* Pagination */

.pagination {

    display:flex;

    justify-content:center;

    gap:6px;

    padding:20px;

}



.pagination button {

    min-width:36px;

    height:36px;

    border:1px solid #dfe2e9;

    border-radius:8px;

    background:white;

    cursor:pointer;

}



.pagination button.active {

    background:#30384b;

    color:white;

}



/* Modal */

.modal-backdrop {

    position:fixed;

    inset:0;

    z-index:9998;

    display:flex;

    justify-content:center;

    align-items:center;

    padding:24px;

    background:rgba(21,25,35,.55);

}



.setting-modal {

    width:min(520px,100%);

    background:white;

    border-radius:18px;

    overflow:hidden;

}



.modal-header {

    display:flex;

    justify-content:space-between;

    padding:24px;

    border-bottom:1px solid #e9ebf0;

}



.modal-header h2 {

    margin:0;

    color:#2d3341;

}



.modal-close {

    border:0;

    background:none;

    font-size:27px;

    cursor:pointer;

}



.selected-summary {

    display:flex;

    flex-direction:column;

    gap:5px;

    margin:20px 24px 0;

    padding:16px;

    background:#f6f7fa;

    border-radius:12px;

}



.selected-summary span {

    color:#747c8d;

    font-size:13px;

}



.setting-form {

    display:flex;

    flex-direction:column;

    gap:20px;

    padding:22px 24px 24px;

}



.toggle-row {

    display:flex;

    justify-content:space-between;

    align-items:center;

}



.number-grid {

    display:grid;

    grid-template-columns:

        repeat(2,minmax(0,1fr));

    gap:14px;

}



.modal-actions {

    display:flex;

    justify-content:flex-end;

    gap:10px;

}



.cancel-button,
.save-button {

    padding:11px 18px;

}



.cancel-button {

    background:#eceef3;

    color:#555d6f;

}



.save-button {

    background:#30384b;

    color:white;

}



@media(max-width:1100px){


.summary-grid {

    grid-template-columns:
        repeat(2,minmax(0,1fr));

}


.filter-panel {

    grid-template-columns:
        repeat(2,minmax(0,1fr));

}


}



@media(max-width:700px){


.page-header {

    flex-direction:column;

}


.summary-grid,
.filter-panel,
.number-grid {

    grid-template-columns:1fr;

}


}



</style>
