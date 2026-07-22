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