<!--
  [화면 흐름 안내] AppMessageToast
  역할: 공통 프론트엔드 화면에서 재사용되는 UI 컴포넌트다.
  진입: 상위 라우트 또는 부모 컴포넌트 -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> props·Pinia·상위 화면 상태 -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<script setup>
defineProps({
  message: {
    type: String,
    default: ''
  },

  type: {
    type: String,
    default: 'success',
    validator: (value) =>
      [
        'success',
        'error',
        'warning',
        'info'
      ].includes(value)
  }
})

const emit = defineEmits([
  'close'
])
</script>

<template>
  <!--
    body 바로 아래에 표시하므로
    모달과 페이지 레이아웃의 영향을 받지 않습니다.
  -->
  <Teleport to="body">
    <Transition name="app-message">
      <div
        v-if="message"
        class="app-message-layer"
      >
        <div
          class="app-message-toast"
          :class="[
            `app-message-toast--${type}`
          ]"
          role="alert"
          aria-live="assertive"
        >
          <span class="app-message-icon">
            {{
              type === 'error'
                ? '!'
                : type === 'warning'
                  ? '!'
                  : type === 'info'
                    ? 'i'
                    : '✓'
            }}
          </span>

          <p>
            {{ message }}
          </p>

          <button
            type="button"
            aria-label="메시지 닫기"
            @click="emit('close')"
          >
            ×
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/*
 * 전체 화면의 가장 앞쪽에 메시지를 배치합니다.
 *
 * 현재 모달 z-index가 3000이므로
 * 메시지는 10000으로 설정합니다.
 */
.app-message-layer {
  position: fixed;
  z-index: 10000;
  inset: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  padding: 24px;

  /*
   * 배경 영역은 클릭을 막지 않습니다.
   */
  pointer-events: none;
}

.app-message-toast {
  display: flex;
  gap: 11px;
  align-items: center;

  width: min(560px, 100%);
  min-height: 58px;
  padding: 15px 17px;

  border: 1px solid #bcebd6;
  border-radius: 13px;

  color: #168a5e;
  background: #edfbf5;

  box-shadow:
    0 20px 55px rgba(20, 25, 40, 0.24);

  /*
   * 실제 메시지 부분은 클릭할 수 있습니다.
   */
  pointer-events: auto;
}

.app-message-icon {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;

  width: 24px;
  height: 24px;

  border-radius: 50%;

  color: #ffffff;
  font-size: 12px;
  font-weight: 900;

  background: #25ad78;
}

.app-message-toast p {
  flex: 1;

  margin: 0;

  font-size: 12px;
  font-weight: 650;
  line-height: 1.6;

  word-break: keep-all;
  overflow-wrap: anywhere;
}

.app-message-toast button {
  flex: 0 0 auto;

  border: 0;
  padding: 2px 5px;

  cursor: pointer;

  color: inherit;
  font-size: 22px;
  line-height: 1;

  background: transparent;
}

/*
 * 오류
 */
.app-message-toast--error {
  border-color: #ffc5ce;
  color: #c9354d;
  background: #fff1f3;
}

.app-message-toast--error
.app-message-icon {
  background: #e84f66;
}

/*
 * 경고
 */
.app-message-toast--warning {
  border-color: #ffe0a6;
  color: #a96d10;
  background: #fff8e8;
}

.app-message-toast--warning
.app-message-icon {
  background: #e8a72e;
}

/*
 * 안내
 */
.app-message-toast--info {
  border-color: #c7dcff;
  color: #3269b7;
  background: #eff6ff;
}

.app-message-toast--info
.app-message-icon {
  background: #4d82ce;
}

.app-message-enter-active,
.app-message-leave-active {
  transition:
    opacity 0.18s ease;
}

.app-message-enter-from,
.app-message-leave-to {
  opacity: 0;
}

@media (max-width: 600px) {
  .app-message-layer {
    padding: 16px;
  }

  .app-message-toast {
    min-height: 54px;
    padding: 13px 14px;
  }
}
</style>