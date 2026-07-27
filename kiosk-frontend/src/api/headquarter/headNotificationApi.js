/**
 * [모듈 흐름 안내] headNotificationApi
 * 역할: 알림 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/notifications, /head/notifications/unread-count, /head/notifications/${notificationId}/read, /head/notifications/read-all)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '@/api/axios'

/*
 * 본사 전체 알림 목록 조회
 *
 * GET /head/notifications
 */
export const getHeadNotifications = () => {
  return api.get(
    '/head/notifications'
  )
}

/*
 * 읽지 않은 알림 개수 조회
 *
 * GET /head/notifications/unread-count
 */
export const getHeadNotificationUnreadCount = () => {
  return api.get(
    '/head/notifications/unread-count'
  )
}

/*
 * 개별 알림 읽음 처리
 *
 * PATCH
 * /head/notifications/{notificationId}/read
 */
export const readHeadNotification = (
  notificationId
) => {
  return api.patch(
    `/head/notifications/${notificationId}/read`
  )
}

/*
 * 전체 알림 읽음 처리
 *
 * PATCH /head/notifications/read-all
 */
export const readAllHeadNotifications = () => {
  return api.patch(
    '/head/notifications/read-all'
  )
}