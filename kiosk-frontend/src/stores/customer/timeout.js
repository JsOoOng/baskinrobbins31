/**
 * [모듈 흐름 안내] timeout
 * 역할: 사용자 대기시간 화면들이 공유하는 Pinia 상태와 동작을 관리한다.
 * 호출 흐름: Vue 화면 -> 이 store의 state/action -> API 또는 localStorage -> 반응형 화면 갱신
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import { defineStore } from 'pinia';

export const useTimeoutStore = defineStore('timeout', {
  state: () => ({
    timeLeft: 60, // 60초 대기 시간 표시용
    showModal: false,
    countdown: 10 // 10초 경고 시간 표시용
  }),
  actions: {
    setTimeLeft(time) {
      this.timeLeft = time;
    },
    setShowModal(show) {
      this.showModal = show;
    },
    setCountdown(time) {
      this.countdown = time;
    }
  }
});
