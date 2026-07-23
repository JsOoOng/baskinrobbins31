<template>
    <div
      ref="notificationRoot"
      class="notification-center"
    >
      <!-- 알림벨 -->
      <button
        type="button"
        class="notification-button"
        :class="{ active: panelOpen }"
        aria-label="알림 센터"
        @click="togglePanel"
      >
        <span class="bell-icon">
          ♢
        </span>
  
        <span
          v-if="unreadCount > 0"
          class="unread-badge"
        >
          {{ unreadCountLabel }}
        </span>
      </button>
  
      <!-- 알림 패널 -->
      <section
        v-if="panelOpen"
        class="notification-panel"
      >
        <header class="notification-header">
          <div>
            <h2>알림</h2>
  
            <p>
              읽지 않은 알림
              {{ unreadCount }}건
            </p>
          </div>
  
          <button
            type="button"
            class="read-all-button"
            :disabled="
              markingAll ||
              unreadCount === 0
            "
            @click="markAllAsRead"
          >
            {{
              markingAll
                ? '처리 중...'
                : '전체 읽음'
            }}
          </button>
        </header>
  
        <div
          v-if="loading"
          class="notification-state"
        >
          알림을 불러오는 중입니다.
        </div>
  
        <div
          v-else-if="notifications.length === 0"
          class="notification-state"
        >
          새로운 알림이 없습니다.
        </div>
  
        <div
          v-else
          class="notification-list"
        >
          <button
            v-for="notification in notifications"
            :key="notification.notificationId"
            type="button"
            class="notification-item"
            :class="{
              unread: !notification.isRead
            }"
            :disabled="
              processingNotificationId ===
              notification.notificationId
            "
            @click="
              handleNotificationClick(
                notification
              )
            "
          >
            <span
              class="notification-category-icon"
              :class="
                `category-${notification.category?.toLowerCase()}`
              "
            >
              {{
                getCategoryIcon(
                  notification.category
                )
              }}
            </span>
  
            <span class="notification-content">
              <span class="notification-title-row">
                <strong>
                  {{ notification.title }}
                </strong>
  
                <span
                  v-if="!notification.isRead"
                  class="unread-dot"
                />
              </span>
  
              <span class="notification-message">
                {{ notification.message }}
              </span>
  
              <span class="notification-meta">
                {{
                  getCategoryLabel(
                    notification.category
                  )
                }}
  
                <span>·</span>
  
                {{
                  formatNotificationDate(
                    notification.createdAt
                  )
                }}
              </span>
            </span>
          </button>
        </div>
  
        <footer class="notification-footer">
          <button
            type="button"
            class="refresh-notification-button"
            :disabled="refreshing"
            @click="refreshNotifications"
          >
            {{
              refreshing
                ? '갱신 중...'
                : '새로고침'
            }}
          </button>
        </footer>
      </section>
    </div>
  </template>
  
  <script setup>
  import {
    computed,
    onBeforeUnmount,
    onMounted,
    ref
  } from 'vue'
  
  import {
    useRoute,
    useRouter
  } from 'vue-router'
  
  import {
    getHeadNotifications,
    getHeadNotificationUnreadCount,
    readAllHeadNotifications,
    readHeadNotification
  } from '@/api/headquarter/headNotificationApi'
  
  const router = useRouter()
  const route = useRoute()
  
  const notificationRoot = ref(null)
  
  const notifications = ref([])
  const unreadCount = ref(0)
  
  const panelOpen = ref(false)
  const loading = ref(false)
  const refreshing = ref(false)
  const markingAll = ref(false)
  
  const processingNotificationId =
    ref(null)
  
  /*
   * 알림 자동 갱신 주기
   *
   * 10000 = 10초
   */
  const POLLING_INTERVAL = 10000
  
  let pollingTimer = null
  
  const unreadCountLabel = computed(() => {
    if (unreadCount.value > 99) {
      return '99+'
    }
  
    return String(
      unreadCount.value
    )
  })
  
  /*
   * API 응답에서 배열 추출
   *
   * 배열 직접 응답과
   * { data: [...] } 응답 모두 지원
   */
  const extractList = (response) => {
    const body = response?.data
  
    if (Array.isArray(body)) {
      return body
    }
  
    if (Array.isArray(body?.data)) {
      return body.data
    }
  
    return []
  }
  
  /*
   * API 응답에서 단일 데이터 추출
   */
  const extractItem = (response) => {
    return response?.data?.data ??
      response?.data
  }
  
  /*
   * 서버 응답값을 프론트 형식으로 정리
   */
  const normalizeNotification = (
    notification
  ) => {
    return {
      ...notification,
  
      isRead: Boolean(
        notification?.isRead ??
        notification?.read
      )
    }
  }
  
  /*
   * 읽지 않은 알림 개수 조회
   */
  const loadUnreadCount = async (
    silent = false
  ) => {
    try {
      const response =
        await getHeadNotificationUnreadCount()
  
      const body =
        extractItem(response)
  
      unreadCount.value =
        Number(
          body?.unreadCount ?? 0
        )
    } catch (error) {
      if (!silent) {
        console.error(
          '[알림 개수 조회 실패]',
          error
        )
      }
    }
  }
  
  /*
   * 전체 알림 목록 조회
   */
  const loadNotifications = async (
    silent = false
  ) => {
    if (!silent) {
      loading.value = true
    }
  
    try {
      const response =
        await getHeadNotifications()
  
      notifications.value =
        extractList(response)
          .map(
            normalizeNotification
          )
    } catch (error) {
      if (!silent) {
        notifications.value = []
  
        console.error(
          '[알림 목록 조회 실패]',
          error
        )
      }
    } finally {
      if (!silent) {
        loading.value = false
      }
    }
  }
  
  /*
   * 알림벨 클릭
   */
  const togglePanel = async () => {
    panelOpen.value =
      !panelOpen.value
  
    if (!panelOpen.value) {
      return
    }
  
    await Promise.all([
      loadNotifications(false),
      loadUnreadCount(true)
    ])
  }
  
  /*
   * 직접 새로고침
   */
  const refreshNotifications = async () => {
    if (refreshing.value) {
      return
    }
  
    refreshing.value = true
  
    try {
      await Promise.all([
        loadNotifications(true),
        loadUnreadCount(true)
      ])
    } finally {
      refreshing.value = false
    }
  }
  
  /*
   * 알림 클릭 시 사용할
   * 화면별 Query 이름
   */
   const getReferenceQuery = (
    notification
    ) => {
    const referenceKey =
        notification?.referenceKey

    if (
        referenceKey === null ||
        referenceKey === undefined
    ) {
        return {}
    }

    const normalizedReferenceKey =
        String(referenceKey).trim()

    if (!normalizedReferenceKey) {
        return {}
    }

    const queryKeyMap = {
        INVENTORY: 'inventoryId',
        COUPON: 'couponId',
        EVENT: 'eventId',
        BANNER: 'bannerId'
    }

    const queryKey =
        queryKeyMap[
        notification.category
        ]

    if (!queryKey) {
        return {
        referenceKey:
            normalizedReferenceKey
        }
    }

    return {
        [queryKey]:
        normalizedReferenceKey
    }
    }
  
  /*
   * 개별 알림 클릭
   *
   * 1. 읽음 처리
   * 2. 읽지 않은 개수 감소
   * 3. 관련 화면 이동
   */
  const handleNotificationClick = async (
    notification
  ) => {
    if (!notification) {
      return
    }
  
    const notificationId =
      notification.notificationId
  
    processingNotificationId.value =
      notificationId
  
    try {
      if (!notification.isRead) {
        await readHeadNotification(
          notificationId
        )
  
        notification.isRead = true
  
        unreadCount.value =
          Math.max(
            0,
            unreadCount.value - 1
          )
      }
  
      panelOpen.value = false
  
      if (!notification.routeName) {
        return
      }
  
      let targetRouteName = notification.routeName
      if (targetRouteName === 'head-restock') {
        targetRouteName = 'head-inventory-requests'
      }

      const targetQuery =
        getReferenceQuery(
          notification
        )
  
      if (
        route.name ===
        targetRouteName
      ) {
        if (
          Object.keys(targetQuery)
            .length > 0
        ) {
          router.replace({
            name: targetRouteName,
            query: targetQuery
          })
        }
  
        return
      }
  
      router.push({
        name: targetRouteName,
        query: targetQuery
      })
    } catch (error) {
      console.error(
        '[알림 읽음 또는 이동 실패]',
        error
      )
    } finally {
      processingNotificationId.value =
        null
    }
  }
  
  /*
   * 전체 읽음 처리
   */
  const markAllAsRead = async () => {
    if (
      markingAll.value ||
      unreadCount.value === 0
    ) {
      return
    }
  
    markingAll.value = true
  
    try {
      await readAllHeadNotifications()
  
      notifications.value =
        notifications.value.map(
          (notification) => ({
            ...notification,
            isRead: true
          })
        )
  
      unreadCount.value = 0
    } catch (error) {
      console.error(
        '[전체 알림 읽음 처리 실패]',
        error
      )
    } finally {
      markingAll.value = false
    }
  }
  
  /*
   * 알림 종류 표시
   */
  const getCategoryLabel = (
    category
  ) => {
    const labels = {
      INVENTORY: '재고',
      COUPON: '쿠폰',
      EVENT: '이벤트',
      BANNER: '배너',
      SYSTEM: '시스템'
    }
  
    return labels[category] ??
      '알림'
  }
  
  /*
   * 알림 종류 아이콘
   */
  const getCategoryIcon = (
    category
  ) => {
    const icons = {
      INVENTORY: '▦',
      COUPON: '%',
      EVENT: '★',
      BANNER: '▣',
      SYSTEM: '!'
    }
  
    return icons[category] ??
      '•'
  }
  
  /*
   * 알림 생성 시각 표시
   */
  const formatNotificationDate = (
    value
  ) => {
    if (!value) {
      return '-'
    }
  
    const date =
      new Date(value)
  
    if (
      Number.isNaN(
        date.getTime()
      )
    ) {
      return '-'
    }
  
    const now =
      new Date()
  
    const difference =
      now.getTime() -
      date.getTime()
  
    const minute =
      60 * 1000
  
    const hour =
      60 * minute
  
    const day =
      24 * hour
  
    if (
      difference >= 0 &&
      difference < minute
    ) {
      return '방금 전'
    }
  
    if (
      difference >= minute &&
      difference < hour
    ) {
      return `${
        Math.floor(
          difference / minute
        )
      }분 전`
    }
  
    if (
      difference >= hour &&
      difference < day
    ) {
      return `${
        Math.floor(
          difference / hour
        )
      }시간 전`
    }
  
    if (
      difference >= day &&
      difference < day * 7
    ) {
      return `${
        Math.floor(
          difference / day
        )
      }일 전`
    }
  
    return new Intl.DateTimeFormat(
      'ko-KR',
      {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      }
    ).format(date)
  }
  
  /*
   * 주기적 자동 갱신
   */
  const runPolling = async () => {
    if (
      document.visibilityState !==
      'visible'
    ) {
      return
    }
  
    await loadUnreadCount(true)
  
    /*
     * 알림 패널이 열려 있을 때만
     * 목록 전체를 다시 조회합니다.
     */
    if (panelOpen.value) {
      await loadNotifications(true)
    }
  }
  
  const startPolling = () => {
    stopPolling()
  
    pollingTimer =
      window.setInterval(
        runPolling,
        POLLING_INTERVAL
      )
  }
  
  const stopPolling = () => {
    if (pollingTimer === null) {
      return
    }
  
    window.clearInterval(
      pollingTimer
    )
  
    pollingTimer = null
  }
  
  /*
   * 다른 브라우저 탭에서 돌아오면
   * 즉시 알림을 갱신합니다.
   */
  const handleVisibilityChange = () => {
    if (
      document.visibilityState ===
      'visible'
    ) {
      runPolling()
    }
  }
  
  /*
   * 알림 패널 바깥 클릭 시 닫기
   */
  const handleDocumentClick = (
    event
  ) => {
    if (!panelOpen.value) {
      return
    }
  
    const root =
      notificationRoot.value
  
    if (
      root &&
      !root.contains(
        event.target
      )
    ) {
      panelOpen.value = false
    }
  }
  
  onMounted(async () => {
    await loadUnreadCount(false)
  
    startPolling()
  
    document.addEventListener(
      'visibilitychange',
      handleVisibilityChange
    )
  
    document.addEventListener(
      'mousedown',
      handleDocumentClick
    )
  })
  
  onBeforeUnmount(() => {
    stopPolling()
  
    document.removeEventListener(
      'visibilitychange',
      handleVisibilityChange
    )
  
    document.removeEventListener(
      'mousedown',
      handleDocumentClick
    )
  })
  </script>
  
  <style scoped>
  .notification-center {
    position: relative;
  }
  
  .notification-button {
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
  
    width: 42px;
    height: 42px;
  
    border: 1px solid #e0e3ea;
    border-radius: 12px;
  
    background: white;
    color: #626a7b;
  
    cursor: pointer;
    font-family: inherit;
  
    transition:
      background 0.15s ease,
      border-color 0.15s ease,
      transform 0.15s ease;
  }
  
  .notification-button:hover,
  .notification-button.active {
    border-color: #c7ccd6;
    background: #f6f7f9;
  }
  
  .notification-button:hover {
    transform: translateY(-1px);
  }
  
  .bell-icon {
    font-size: 21px;
    line-height: 1;
    transform: rotate(180deg);
  }
  
  .unread-badge {
    position: absolute;
    top: -6px;
    right: -7px;
  
    display: inline-flex;
    align-items: center;
    justify-content: center;
  
    min-width: 19px;
    height: 19px;
    padding: 0 5px;
  
    border: 2px solid white;
    border-radius: 999px;
  
    background: #e45151;
    color: white;
  
    font-size: 10px;
    font-weight: 800;
  }
  
  .notification-panel {
    position: absolute;
    z-index: 9000;
    top: calc(100% + 12px);
    right: 0;
  
    width: min(
      420px,
      calc(100vw - 32px)
    );
  
    overflow: hidden;
  
    border: 1px solid #e2e5ec;
    border-radius: 16px;
  
    background: white;
  
    box-shadow:
      0 20px 55px
      rgba(24, 29, 41, 0.18);
  }
  
  .notification-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  
    padding: 18px 20px;
  
    border-bottom: 1px solid #eceef2;
  }
  
  .notification-header h2 {
    margin: 0;
    color: #2f3543;
    font-size: 18px;
  }
  
  .notification-header p {
    margin: 5px 0 0;
    color: #858c9c;
    font-size: 12px;
  }
  
  .read-all-button {
    padding: 8px 11px;
  
    border: 0;
    border-radius: 8px;
  
    background: #eef0f4;
    color: #555e70;
  
    cursor: pointer;
    font-family: inherit;
    font-size: 12px;
    font-weight: 700;
  }
  
  .read-all-button:disabled {
    cursor: not-allowed;
    opacity: 0.45;
  }
  
  .notification-list {
    max-height: 450px;
    overflow-y: auto;
  }
  
  .notification-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
  
    width: 100%;
    padding: 15px 18px;
  
    border: 0;
    border-bottom: 1px solid #eff1f4;
  
    background: white;
    color: inherit;
  
    cursor: pointer;
    font-family: inherit;
    text-align: left;
  }
  
  .notification-item:hover {
    background: #f8f9fb;
  }
  
  .notification-item.unread {
    background: #f5f8ff;
  }
  
  .notification-item.unread:hover {
    background: #edf3ff;
  }
  
  .notification-item:disabled {
    cursor: wait;
    opacity: 0.65;
  }
  
  .notification-category-icon {
    display: inline-flex;
    flex: 0 0 auto;
    align-items: center;
    justify-content: center;
  
    width: 34px;
    height: 34px;
  
    border-radius: 10px;
  
    background: #eef0f4;
    color: #5d6678;
  
    font-size: 15px;
    font-weight: 900;
  }
  
  .category-inventory {
    background: #eaf4ff;
    color: #3972ad;
  }
  
  .category-coupon {
    background: #fff3df;
    color: #b8761a;
  }
  
  .category-event {
    background: #f4edff;
    color: #7750aa;
  }
  
  .category-banner {
    background: #eaf8f2;
    color: #268063;
  }
  
  .category-system {
    background: #fff0f0;
    color: #cc4a4a;
  }
  
  .notification-content {
    display: flex;
    min-width: 0;
    flex: 1;
    flex-direction: column;
    gap: 5px;
  }
  
  .notification-title-row {
    display: flex;
    align-items: center;
    gap: 7px;
  }
  
  .notification-title-row strong {
    overflow: hidden;
  
    color: #333a48;
  
    font-size: 13px;
  
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .unread-dot {
    flex: 0 0 auto;
  
    width: 7px;
    height: 7px;
  
    border-radius: 50%;
    background: #e45151;
  }
  
  .notification-message {
    display: -webkit-box;
    overflow: hidden;
  
    color: #697183;
  
    font-size: 12px;
    line-height: 1.55;
  
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }
  
  .notification-meta {
    display: flex;
    align-items: center;
    gap: 5px;
  
    color: #9aa0ad;
    font-size: 11px;
  }
  
  .notification-state {
    padding: 60px 20px;
    color: #8b92a1;
    font-size: 13px;
    text-align: center;
  }
  
  .notification-footer {
    display: flex;
    justify-content: center;
  
    padding: 11px;
  
    border-top: 1px solid #eceef2;
    background: #fafbfc;
  }
  
  .refresh-notification-button {
    border: 0;
    background: transparent;
    color: #646d80;
    cursor: pointer;
    font-family: inherit;
    font-size: 12px;
    font-weight: 700;
  }
  
  .refresh-notification-button:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }
  
  @media (max-width: 600px) {
    .notification-panel {
      position: fixed;
      top: 70px;
      right: 16px;
      left: 16px;
      width: auto;
    }
  
    .notification-list {
      max-height:
        calc(100vh - 220px);
    }
  }
  </style>