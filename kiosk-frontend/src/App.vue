<script setup>
import {
  computed,
  onBeforeUnmount,
  ref,
  watch
} from 'vue'

import {
  RouterView,
  useRoute,
  useRouter
} from 'vue-router'

import { Client } from '@stomp/stompjs'

import TimeoutModal from '@/components/common/TimeoutModal.vue'

import {
  getUnconfirmedInventoryShortages,
  confirmInventoryShortages
} from '@/api/branch/statusApi'


const route = useRoute()
const router = useRouter()


/*
 * 키오스크 화면 여부
 */
const isKiosk = computed(() => {

  const path = route.path

  if (
    path.startsWith('/branch') ||
    path.startsWith('/head') ||
    path === '/'
  ) {
    return false
  }

  return true
})


/*
 * 기존 주문 알림 토스트
 */
const toastMessage = ref('')
const showToastBox = ref(false)


const showToast = (message) => {

  toastMessage.value =
    String(message ?? '')

  showToastBox.value = true
}


const closeToast = () => {

  showToastBox.value = false
  toastMessage.value = ''
}


/*
 * 재고 신청 승인·반려 알림
 */
const restockAlert = ref(null)


const showRestockAlertModal = computed(() => {

  return restockAlert.value !== null
})


const closeRestockAlert = () => {

  restockAlert.value = null
}

/*
 * 재고 부족 알림
 */
 const inventoryShortageAlert = ref(null)

const shortageConfirming = ref(false)


const showInventoryShortageModal = computed(() => {

  return inventoryShortageAlert.value !== null
})


const closeInventoryShortageAlert = () => {

  inventoryShortageAlert.value = null
}


/*
 * API 응답 본문 추출
 */
const extractResponseBody = (response) => {

  return (
    response?.data?.data ??
    response?.data ??
    null
  )
}


/*
 * 지점의 미확인 재고 부족 알림 조회
 */
const loadInventoryShortageAlert = async (
  storeId
) => {

  if (
    !Number.isInteger(Number(storeId)) ||
    Number(storeId) <= 0
  ) {
    return
  }

  try {

    const response =
      await getUnconfirmedInventoryShortages(
        storeId
      )

    const summary =
      extractResponseBody(
        response
      )

    const items =
      Array.isArray(summary?.items)
        ? summary.items
        : []


    if (
      !summary?.hasAlert ||
      items.length === 0
    ) {

      inventoryShortageAlert.value = null

      return
    }


    inventoryShortageAlert.value = {

      title:
        summary.title ??
        '부족한 재고가 있습니다.',

      message:
        summary.message ??
        '재고 관리 화면에서 부족 품목을 확인해주세요.',

      storeId:
        Number(storeId),

      itemCount:
        Number(
          summary.itemCount ??
          items.length
        ),

      items,

      routeName:
        summary.routeName ??
        'branch-inventory'
    }


    console.log(
      '재고 부족 알림 조회 완료',
      inventoryShortageAlert.value
    )

  } catch (error) {

    console.error(
      '재고 부족 알림 조회 실패',
      error
    )
  }
}


/*
 * 재고 부족 알림 확인 후
 * 재고 관리 화면 이동
 */
const confirmAndMoveToInventory = async () => {

  if (
    !inventoryShortageAlert.value ||
    shortageConfirming.value
  ) {
    return
  }

  const branchUser =
    getBranchUser()

  if (
    !branchUser ||
    !branchUser.storeId
  ) {

    alert(
      '지점 로그인 정보를 확인할 수 없습니다.'
    )

    return
  }


  const shortageItems =
    inventoryShortageAlert.value.items ?? []


  if (
    shortageItems.length === 0
  ) {

    alert(
      '확인할 부족 재고가 없습니다.'
    )

    return
  }


  shortageConfirming.value = true


  try {

    /*
     * DETECTED 또는 SENT
     * → CONFIRMED
     */
    await confirmInventoryShortages(
      branchUser.storeId
    )


    /*
     * 여러 부족 품목이 있는 경우를 위해
     * 전체 목록을 임시 저장합니다.
     *
     * 화면에는 ID를 표시하지 않습니다.
     */
    sessionStorage.setItem(
      'branchInventoryShortageItems',
      JSON.stringify(
        shortageItems
      )
    )


    /*
     * 현재 재고 화면은 첫 번째 부족 품목의
     * 신청 모달을 자동으로 엽니다.
     */
    const firstItem =
      shortageItems[0]


    inventoryShortageAlert.value = null


    await router.push({

      name:
        'branch-inventory',

      query: {

        alertId:
          firstItem.alertId,

        storeInventoryId:
          firstItem.storeInventoryId,

        shortageQuantity:
          firstItem.shortageQuantity
      }
    })

  } catch (error) {

    console.error(
      '재고 부족 알림 확인 실패',
      error
    )

    alert(
      error?.response?.data?.error ??
      error?.response?.data?.message ??
      '재고 부족 알림 확인에 실패했습니다.'
    )

  } finally {

    shortageConfirming.value = false
  }
}

/*
 * 재고 신청 현황 이동
 */
const moveRestockHistory = async () => {

  closeRestockAlert()

  await router.push({
    name: 'branch-restock-history'
  })
}


/*
 * 재고 관리 이동
 */
const moveBranchInventory = async () => {

  closeRestockAlert()

  await router.push({
    name: 'branch-inventory'
  })
}


/*
 * localStorage 지점 사용자 조회
 */
const getBranchUser = () => {

  const storedUser =
    localStorage.getItem('branchUser')

  if (!storedUser) {
    return null
  }

  try {

    return JSON.parse(
      storedUser
    )

  } catch (error) {

    console.error(
      'branchUser JSON 파싱 실패',
      error
    )

    return null
  }
}


/*
 * JSON WebSocket 메시지 파싱
 */
const parseSocketMessage = (body) => {

  if (!body) {
    return null
  }

  try {

    return JSON.parse(
      body
    )

  } catch (error) {

    console.error(
      'WebSocket 메시지 JSON 파싱 실패',
      body,
      error
    )

    return null
  }
}


/*
 * WebSocket URL
 *
 * .env에 VITE_WS_URL이 있으면 우선 사용합니다.
 *
 * 없으면 현재 접속한 호스트의
 * 8889 포트로 연결합니다.
 *
 * 예:
 * ws://localhost:8889/ws
 * ws://192.168.0.10:8889/ws
 */
const getWebSocketUrl = () => {

  const configuredUrl =
    String(
      import.meta.env.VITE_WS_URL ?? ''
    ).trim()

  if (configuredUrl) {
    return configuredUrl
  }

  const protocol =
    window.location.protocol === 'https:'
      ? 'wss:'
      : 'ws:'

  return (
    `${protocol}//`
    + `${window.location.hostname}`
    + ':8889/ws'
  )
}


let client = null
let connectedStoreId = null

let headClient = null

/*
 * 본사 WebSocket 연결 해제
 */
const disconnectHeadSocket = () => {

  const currentClient = headClient

  headClient = null

  if (!currentClient) {
    return
  }

  currentClient
    .deactivate()
    .then(() => {
      console.log('본사 WebSocket 연결 해제')
    })
    .catch((error) => {
      console.error('본사 WebSocket 연결 해제 실패', error)
    })
}

/*
 * WebSocket 연결 해제
 */
const disconnectBranchSocket = () => {

  const currentClient = client

  client = null
  connectedStoreId = null

  if (!currentClient) {
    return
  }

  currentClient
    .deactivate()
    .then(() => {

      console.log(
        '지점 WebSocket 연결 해제'
      )

    })
    .catch((error) => {

      console.error(
        '지점 WebSocket 연결 해제 실패',
        error
      )

    })
}


/*
 * 승인·반려 메시지 처리
 */
const handleRestockStatusMessage = (body) => {

  if (
    !route.path.startsWith('/branch')
  ) {
    return
  }

  const data =
    parseSocketMessage(body)

  if (!data) {
    return
  }

  if (
    data.status !== 'APPROVED' &&
    data.status !== 'REJECTED'
  ) {

    console.warn(
      '처리할 수 없는 재고 신청 상태',
      data
    )

    return
  }

  restockAlert.value = {

    notificationType:
      data.notificationType ?? '',

    status:
      data.status,

    title:
      data.title ??
      (
        data.status === 'APPROVED'
          ? '재고 신청이 승인되었습니다.'
          : '재고 신청이 반려되었습니다.'
      ),

    message:
      data.message ??
      '재고 신청 처리 결과를 확인해주세요.',

    itemName:
      data.itemName ?? '재고 품목',

    requestQuantity:
      data.requestQuantity ?? null,

    requestTargetType:
      data.requestTargetType ?? null,

    processedAt:
      data.processedAt ?? null,

    /*
     * 내부 이동 및 식별에만 사용합니다.
     * 화면에는 출력하지 않습니다.
     */
    requestId:
      data.requestId ?? null
  }

  console.log(
    '재고 신청 처리 알림 수신',
    data
  )
}


/*
 * 지점 WebSocket 연결
 */
const connectBranchSocket = () => {

  const isBranchPage =
    route.path.startsWith('/branch')

  const branchUser =
    getBranchUser()

  /*
   * 지점 화면이 아니거나
   * 로그인 사용자가 없으면 연결 해제
   */
  if (
    !isBranchPage ||
    !branchUser ||
    !branchUser.storeId
  ) {

    disconnectBranchSocket()

    return
  }

  const storeId =
    Number(branchUser.storeId)

  if (
    !Number.isInteger(storeId) ||
    storeId <= 0
  ) {

    console.error(
      '올바르지 않은 지점 번호',
      branchUser.storeId
    )

    disconnectBranchSocket()

    return
  }

  /*
   * 같은 지점으로 이미 연결돼 있으면
   * 중복 연결하지 않습니다.
   */
  if (
    client &&
    connectedStoreId === storeId &&
    client.active
  ) {
    return
  }

  /*
   * 이전 연결 정리
   */
  disconnectBranchSocket()

  connectedStoreId = storeId

  const nextClient =
    new Client({

      brokerURL:
        getWebSocketUrl(),

      reconnectDelay:
        5000,

      heartbeatIncoming:
        10000,

      heartbeatOutgoing:
        10000,

      debug: (message) => {

        /*
         * 개발 중 STOMP 상세 로그가 필요하면
         * 아래 주석을 해제합니다.
         */
        // console.log('[STOMP]', message)
      }
    })

  client = nextClient


  nextClient.onConnect = () => {

    /*
     * 연결 중 사용자가 로그아웃했거나
     * 다른 연결로 교체된 경우 무시합니다.
     */
    if (
      client !== nextClient
    ) {
      return
    }

    console.log(
      `지점 WebSocket 연결 성공: storeId=${storeId}`
    )

    /*
    * 지점이 오프라인이었던 동안 생성된
    * 재고 부족 알림도 조회합니다.
    */
    loadInventoryShortageAlert(
      storeId
    )

    /*
     * 기존 주문 관련 지점 알림
     */
    nextClient.subscribe(

      `/topic/store/${storeId}`,

      (message) => {

        if (
          !route.path.startsWith('/branch')
        ) {
          return
        }

        showToast(
          message.body
        )
      }
    )


    /*
     * 키오스크 직원 호출
     */
    nextClient.subscribe(

      `/topic/stores/${storeId}/calls`,

      (message) => {

        if (
          !route.path.startsWith('/branch')
        ) {
          return
        }

        const callData =
          parseSocketMessage(
            message.body
          )

        if (!callData) {
          return
        }

        const reasonText =

          callData.reason === 'ORDER_SCREEN'
            ? '메뉴 주문 화면'

            : callData.reason === 'PAYMENT_ERROR'
              ? '결제 오류'

              : callData.reason ?? '기타'


        alert(
          `🔔 키오스크 ${callData.kioskNo}번에서 `
          + `직원을 호출했습니다.\n`
          + `사유: ${reasonText}`
        )
      }
    )

    /*
 * 재고 부족 실시간 알림
 */
nextClient.subscribe(

`/topic/stores/${storeId}/inventory-shortages`,

(message) => {

  if (
    !route.path.startsWith('/branch')
  ) {
    return
  }


  const socketData =
    parseSocketMessage(
      message.body
    )


  console.log(
    '재고 부족 WebSocket 수신',
    socketData
  )


  /*
   * WebSocket 데이터 하나만 사용하는 대신
   * API로 전체 부족 품목을 다시 조회합니다.
   */
  loadInventoryShortageAlert(
    storeId
  )
}
)

    /*
     * 재고 신청 승인·반려 알림
     */
    nextClient.subscribe(

      `/topic/stores/${storeId}/restock-requests`,

      (message) => {

        handleRestockStatusMessage(
          message.body
        )
      }
    )
  }


  nextClient.onStompError = (frame) => {

    console.error(
      'STOMP 오류',
      frame.headers?.message,
      frame.body
    )
  }


  nextClient.onWebSocketError = (error) => {

    console.error(
      'WebSocket 연결 오류',
      error
    )
  }


  nextClient.onWebSocketClose = () => {

    console.warn(
      `지점 WebSocket 연결 종료: storeId=${storeId}`
    )
  }


  nextClient.activate()
}


/*
 * 본사 WebSocket 연결
 */
const connectHeadSocket = () => {

  const isHeadPage = route.path.startsWith('/head')

  if (!isHeadPage) {
    disconnectHeadSocket()
    return
  }

  if (headClient && headClient.active) {
    return
  }

  disconnectHeadSocket()

  const nextClient = new Client({
    brokerURL: getWebSocketUrl(),
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000
  })

  headClient = nextClient

  nextClient.onConnect = () => {

    if (headClient !== nextClient) {
      return
    }

    console.log('본사 WebSocket 연결 성공')

    nextClient.subscribe(

      '/topic/head/inventory-shortages',

      (message) => {

        if (!route.path.startsWith('/head')) {
          return
        }

        const socketData = parseSocketMessage(message.body)

        console.log('본사 재고 부족 WebSocket 수신', socketData)

        showToast(
          `[재고 부족 알람] 지점의 재고 부족 알람이 수신되었습니다.`
        )
      }
    )
  }

  nextClient.onStompError = (frame) => {
    console.error('본사 STOMP 오류', frame.headers?.message, frame.body)
  }

  nextClient.onWebSocketError = (error) => {
    console.error('본사 WebSocket 연결 오류', error)
  }

  nextClient.onWebSocketClose = () => {
    console.warn('본사 WebSocket 연결 종료')
  }

  nextClient.activate()
}


/*
 * 경로 변경 감시
 *
 * 로그인 후 /branch/main으로 이동할 때
 * App.vue가 다시 마운트되지 않아도 연결됩니다.
 *
 * 로그아웃 후 /branch/login으로 이동하면
 * branchUser가 없으므로 연결을 해제합니다.
 */
watch(

  () => route.path,

  () => {

    connectBranchSocket()
    connectHeadSocket()
  },

  {
    immediate: true
  }
)


onBeforeUnmount(() => {

  disconnectBranchSocket()
  disconnectHeadSocket()
})
</script>


<template>

  <div
    class="app-container"
    :class="{
      'kiosk-wrapper': isKiosk
    }"
  >

    <main
      class="main-content"
      :class="{
        'kiosk-mode': isKiosk
      }"
    >

      <RouterView />

    </main>


    <TimeoutModal />

    <!-- 재고 부족 알림 모달 -->
<div
  v-if="showInventoryShortageModal"
  class="shortage-alert-background"
>

  <section class="shortage-alert-modal">

    <div class="shortage-alert-icon">
      !
    </div>


    <span class="shortage-alert-badge">
      재고 부족
    </span>


    <h2>
      {{ inventoryShortageAlert.title }}
    </h2>


    <p class="shortage-alert-message">
      {{ inventoryShortageAlert.message }}
    </p>


    <div class="shortage-alert-count">

      부족 품목

      <strong>
        {{ inventoryShortageAlert.itemCount }}개
      </strong>

    </div>


    <!-- 화면에는 ID를 출력하지 않음 -->
    <ul class="shortage-item-list">

      <li
        v-for="item in inventoryShortageAlert.items"
        :key="item.alertId"
      >

        <div class="shortage-item-name">

          <strong>
            {{ item.itemName }}
          </strong>

          <span>
            {{ item.shortageQuantity }}개 부족
          </span>

        </div>


        <div class="shortage-stock-info">

          <span>
            현재 {{ item.currentStock }}
          </span>

          <span>
            최소 {{ item.minStock }}
          </span>

        </div>

      </li>

    </ul>


    <div class="shortage-alert-buttons">

      <button
        type="button"
        class="shortage-confirm-button"
        :disabled="shortageConfirming"
        @click="confirmAndMoveToInventory"
      >

        {{
          shortageConfirming
            ? '처리 중...'
            : '확인 후 재고 관리'
        }}

      </button>


      <button
        type="button"
        class="shortage-later-button"
        :disabled="shortageConfirming"
        @click="closeInventoryShortageAlert"
      >
        나중에 확인
      </button>

    </div>

  </section>

</div>

    <!-- 기존 주문 알림 토스트 -->
    <div
      v-if="showToastBox"
      class="order-toast"
    >

      <div class="order-toast-message">
        🔔 {{ toastMessage }}
      </div>

      <button
        type="button"
        @click="closeToast"
      >
        확인
      </button>

    </div>


    <!-- 재고 신청 승인·반려 모달 -->
    <div
      v-if="showRestockAlertModal"
      class="restock-alert-background"
      @click.self="closeRestockAlert"
    >

      <section
        class="restock-alert-modal"
        :class="{
          approved:
            restockAlert.status === 'APPROVED',

          rejected:
            restockAlert.status === 'REJECTED'
        }"
      >

        <div class="restock-alert-icon">

          {{
            restockAlert.status === 'APPROVED'
              ? '✓'
              : '!'
          }}

        </div>


        <div
          class="restock-alert-status"
          :class="{
            approved:
              restockAlert.status === 'APPROVED',

            rejected:
              restockAlert.status === 'REJECTED'
          }"
        >

          {{
            restockAlert.status === 'APPROVED'
              ? '승인'
              : '반려'
          }}

        </div>


        <h2>
          {{ restockAlert.title }}
        </h2>


        <p class="restock-alert-message">
          {{ restockAlert.message }}
        </p>


        <div class="restock-alert-detail">

          <div>

            <span>품목</span>

            <strong>
              {{ restockAlert.itemName }}
            </strong>

          </div>


          <div
            v-if="
              restockAlert.requestQuantity !== null
            "
          >

            <span>신청 수량</span>

            <strong>
              {{ restockAlert.requestQuantity }}개
            </strong>

          </div>

        </div>


        <div class="restock-alert-buttons">

          <button
            type="button"
            class="inventory-button"
            @click="moveBranchInventory"
          >
            재고 관리
          </button>


          <button
            type="button"
            class="history-button"
            @click="moveRestockHistory"
          >
            신청 현황
          </button>


          <button
            type="button"
            class="close-button"
            @click="closeRestockAlert"
          >
            닫기
          </button>

        </div>

      </section>

    </div>

  </div>

</template>


<style>
html,
body,
#app {

  margin: 0;
  padding: 0;

  width: 100%;
  min-height: 100%;

  font-family:
    'Pretendard',
    sans-serif;

  background-color:
    #f4f4f4;
}


* {

  box-sizing: border-box;
}


.app-container {

  width: 100%;
  min-height: 100vh;

  display: flex;

  justify-content: center;
  align-items: flex-start;
}


.app-container.kiosk-wrapper {

  height: 100vh;

  align-items: center;

  background-color:
    #f0f0f0;
}


.main-content {

  width: 100%;
  max-width: 1400px;
  min-height: 100vh;

  padding: 20px;
}


.main-content.kiosk-mode {

  max-width: 600px;

  height: 100vh;
  min-height: auto;

  padding: 0;

  background-color: white;

  box-shadow:
    0 0 20px
    rgba(0, 0, 0, 0.2);

  position: relative;

  overflow-y: auto;
  overflow-x: hidden;
}


/*
 * 기존 주문 토스트
 */
.order-toast {

  position: fixed;

  right: 30px;
  bottom: 30px;

  width: min(
    380px,
    calc(100vw - 40px)
  );

  background: white;

  padding: 20px 30px;

  border-radius: 15px;

  box-shadow:
    0 5px 20px
    rgba(0, 0, 0, 0.2);

  font-size: 18px;

  z-index: 9999;
}


.order-toast-message {

  white-space: pre-line;
  word-break: keep-all;

  line-height: 1.5;
}


.order-toast button {

  margin-top: 15px;

  width: 100%;

  padding: 10px 0;

  border: none;
  border-radius: 10px;

  background-color:
    #4caf50;

  color: white;

  font-size: 16px;
  font-weight: bold;

  cursor: pointer;
}


.order-toast button:hover {

  background-color:
    #43a047;
}


.order-toast button:active {

  transform:
    scale(0.98);
}


/*
 * 재고 승인·반려 모달 배경
 */
.restock-alert-background {

  position: fixed;

  inset: 0;

  display: flex;

  justify-content: center;
  align-items: center;

  padding: 20px;

  background:
    rgba(16, 24, 40, 0.55);

  backdrop-filter:
    blur(3px);

  z-index: 20000;
}


/*
 * 재고 승인·반려 모달
 */
.restock-alert-modal {

  width: min(
    460px,
    100%
  );

  padding: 34px;

  background: white;

  border-radius: 22px;

  box-shadow:
    0 24px 70px
    rgba(15, 23, 42, 0.25);

  text-align: center;
}


.restock-alert-modal.approved {

  border-top:
    6px solid #198754;
}


.restock-alert-modal.rejected {

  border-top:
    6px solid #dc3545;
}


.restock-alert-icon {

  width: 64px;
  height: 64px;

  margin:
    0 auto 14px;

  display: flex;

  justify-content: center;
  align-items: center;

  border-radius: 50%;

  background:
    #f1f3f5;

  color:
    #212529;

  font-size: 34px;
  font-weight: 800;
}


.restock-alert-status {

  display: inline-block;

  margin-bottom: 12px;

  padding: 6px 14px;

  border-radius: 999px;

  font-size: 13px;
  font-weight: 800;
}


.restock-alert-status.approved {

  color:
    #146c43;

  background:
    #d1e7dd;
}


.restock-alert-status.rejected {

  color:
    #b02a37;

  background:
    #f8d7da;
}


.restock-alert-modal h2 {

  margin:
    0 0 14px;

  color:
    #212529;

  font-size: 24px;

  word-break: keep-all;
}


.restock-alert-message {

  margin:
    0 0 24px;

  color:
    #667085;

  line-height: 1.6;

  word-break: keep-all;
}


.restock-alert-detail {

  margin-bottom: 26px;

  padding: 18px;

  background:
    #f8f9fa;

  border-radius: 14px;

  text-align: left;
}


.restock-alert-detail > div {

  display: flex;

  justify-content: space-between;
  align-items: center;

  gap: 20px;

  padding:
    7px 0;
}


.restock-alert-detail span {

  color:
    #868e96;

  font-size: 14px;
}


.restock-alert-detail strong {

  color:
    #343a40;

  text-align: right;
}


.restock-alert-buttons {

  display: grid;

  grid-template-columns:
    1fr 1fr;

  gap: 10px;
}


.restock-alert-buttons button {

  min-height: 46px;

  padding:
    11px 14px;

  border: none;
  border-radius: 10px;

  font-size: 15px;
  font-weight: 700;

  cursor: pointer;
}


.inventory-button {

  color:
    #343a40;

  background:
    #e9ecef;
}


.history-button {

  color: white;

  background:
    #212529;
}


.close-button {

  grid-column:
    1 / -1;

  color:
    #495057;

  background:
    white;

  border:
    1px solid #dee2e6 !important;
}


.restock-alert-buttons button:hover {

  filter:
    brightness(0.95);
}


.restock-alert-buttons button:active {

  transform:
    scale(0.98);
}

/*
 * 재고 부족 알림 배경
 */
 .shortage-alert-background {

position: fixed;

inset: 0;

display: flex;

justify-content: center;
align-items: center;

padding: 20px;

background:
  rgba(16, 24, 40, 0.58);

backdrop-filter:
  blur(4px);

z-index: 21000;
}


/*
* 재고 부족 알림 모달
*/
.shortage-alert-modal {

width: min(
  500px,
  100%
);

max-height:
  calc(100vh - 40px);

overflow-y: auto;

padding: 34px;

background: white;

border-top:
  6px solid #dc3545;

border-radius: 22px;

box-shadow:
  0 24px 70px
  rgba(15, 23, 42, 0.28);

text-align: center;
}


.shortage-alert-icon {

width: 64px;
height: 64px;

margin:
  0 auto 14px;

display: flex;

justify-content: center;
align-items: center;

border-radius: 50%;

color:
  #b02a37;

background:
  #f8d7da;

font-size: 34px;
font-weight: 900;
}


.shortage-alert-badge {

display: inline-block;

margin-bottom: 12px;

padding:
  6px 14px;

border-radius: 999px;

color:
  #b02a37;

background:
  #f8d7da;

font-size: 13px;
font-weight: 800;
}


.shortage-alert-modal h2 {

margin:
  0 0 12px;

color:
  #212529;

font-size: 24px;

word-break: keep-all;
}


.shortage-alert-message {

margin:
  0 0 20px;

color:
  #667085;

line-height: 1.6;

word-break: keep-all;
}


.shortage-alert-count {

margin-bottom: 14px;

color:
  #495057;

font-size: 15px;
}


.shortage-alert-count strong {

margin-left: 5px;

color:
  #dc3545;
}


.shortage-item-list {

margin:
  0 0 24px;

padding: 0;

list-style: none;

text-align: left;
}


.shortage-item-list li {

margin-bottom: 10px;

padding: 16px;

border:
  1px solid #f1c2c7;

border-radius: 12px;

background:
  #fff8f8;
}


.shortage-item-list li:last-child {

margin-bottom: 0;
}


.shortage-item-name {

display: flex;

justify-content: space-between;
align-items: center;

gap: 16px;

margin-bottom: 8px;
}


.shortage-item-name strong {

color:
  #343a40;
}


.shortage-item-name span {

color:
  #dc3545;

font-size: 13px;
font-weight: 800;
}


.shortage-stock-info {

display: flex;

gap: 14px;

color:
  #868e96;

font-size: 13px;
}


.shortage-alert-buttons {

display: grid;

grid-template-columns:
  2fr 1fr;

gap: 10px;
}


.shortage-alert-buttons button {

min-height: 48px;

padding:
  11px 14px;

border: none;
border-radius: 10px;

font-size: 15px;
font-weight: 700;

cursor: pointer;
}


.shortage-alert-buttons button:disabled {

opacity: 0.6;

cursor: default;
}


.shortage-confirm-button {

color: white;

background:
  #dc3545;
}


.shortage-later-button {

color:
  #495057;

background:
  #e9ecef;
}


.shortage-alert-buttons button:not(:disabled):hover {

filter:
  brightness(0.95);
}


.shortage-alert-buttons button:not(:disabled):active {

transform:
  scale(0.98);
}

@media (
  max-width: 600px
) {

  .main-content {

    padding: 12px;
  }


  .order-toast {

    right: 20px;
    bottom: 20px;
  }

  .shortage-alert-modal {

  padding:
    26px 20px;

  border-radius:
    18px;
  }


  .shortage-alert-buttons {

  grid-template-columns:
    1fr;
  }

  .restock-alert-modal {

    padding: 26px 20px;

    border-radius: 18px;
  }


  .restock-alert-buttons {

    grid-template-columns:
      1fr;
  }


  .close-button {

    grid-column:
      auto;
  }
}
</style>