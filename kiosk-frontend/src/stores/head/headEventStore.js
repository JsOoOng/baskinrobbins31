/**
 * [모듈 흐름 안내] headEventStore
 * 역할: 이벤트 화면들이 공유하는 Pinia 상태와 동작을 관리한다.
 * 호출 흐름: Vue 화면 -> 이 store의 state/action -> API 또는 localStorage -> 반응형 화면 갱신
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import { defineStore } from 'pinia';
import { headEventApi } from '@/api/headquarter/headEventApi';

export const useHeadEventStore = defineStore('headEvent', {
  state: () => ({
    events: [],
    isLoading: false,
    error: null,
  }),

  actions: {
    async fetchEvents() {
      this.isLoading = true;
      this.error = null;
      try {
        const data = await headEventApi.getAllEvents();
        this.events = data;
      } catch (error) {
        this.error = '이벤트 목록을 불러오는데 실패했습니다.';
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    },

    async createEvent(eventData) {
      this.isLoading = true;
      this.error = null;
      try {
        const newEvent = await headEventApi.createEvent(eventData);
        this.events.push(newEvent);
        return newEvent;
      } catch (error) {
        this.error = '이벤트 생성에 실패했습니다.';
        console.error(error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async updateEvent(eventId, eventData) {
      this.isLoading = true;
      this.error = null;
      try {
        const updatedEvent = await headEventApi.updateEvent(eventId, eventData);
        const index = this.events.findIndex(e => e.eventId === eventId);
        if (index !== -1) {
          this.events[index] = updatedEvent;
        }
        return updatedEvent;
      } catch (error) {
        this.error = '이벤트 수정에 실패했습니다.';
        console.error(error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async deleteEvent(eventId) {
      this.isLoading = true;
      this.error = null;
      try {
        await headEventApi.deleteEvent(eventId);
        this.events = this.events.filter(e => e.eventId !== eventId);
      } catch (error) {
        this.error = '이벤트 삭제에 실패했습니다.';
        console.error(error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async updateVisibility(eventId, isVisible) {
      try {
        const updatedEvent = await headEventApi.updateVisibility(eventId, isVisible);
        const index = this.events.findIndex(e => e.eventId === eventId);
        if (index !== -1) {
          this.events[index] = updatedEvent;
        }
      } catch (error) {
        console.error('가시성 수정 실패:', error);
        throw error;
      }
    },

    // 쉬운주석: 연장된 이벤트 한 건만 목록에서 바꿔 전체 화면을 다시 불러오지 않는다.
    async extendEvent(eventId, endDate) {
      const updatedEvent = await headEventApi.extendEvent(eventId, endDate);
      const index = this.events.findIndex(event => event.eventId === eventId);
      if (index !== -1) this.events[index] = updatedEvent;
      return updatedEvent;
    }
  }
});
