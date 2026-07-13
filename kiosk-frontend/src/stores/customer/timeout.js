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
