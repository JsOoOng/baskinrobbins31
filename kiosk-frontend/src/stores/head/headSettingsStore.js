import { defineStore } from 'pinia';
import { getHeadSettings, updateHeadSettings } from '@/api/head/headSettingsApi';

export const useHeadSettingsStore = defineStore('headSettings', {
  state: () => ({
    settings: {
      useVoiceGuide: true,
      receiptPrintMode: 'SELECT',
      tumblerDiscountAmount: 300,
      lowStockAlertCount: 5,
      useEasyPay: true
    },
    isLoading: false,
    error: null,
    isSaved: false
  }),
  actions: {
    async fetchSettings() {
      this.isLoading = true;
      this.error = null;
      try {
        const data = await getHeadSettings();
        this.settings = data;
      } catch (err) {
        this.error = '설정을 불러오지 못했습니다.';
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    },
    async saveSettings(newSettings) {
      this.isLoading = true;
      this.error = null;
      this.isSaved = false;
      try {
        const data = await updateHeadSettings(newSettings);
        this.settings = data;
        this.isSaved = true;
        setTimeout(() => { this.isSaved = false; }, 3000);
      } catch (err) {
        this.error = '설정을 저장하지 못했습니다.';
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    }
  }
});
