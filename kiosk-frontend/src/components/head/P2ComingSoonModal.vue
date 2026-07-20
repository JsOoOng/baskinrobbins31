<script setup>
import {
  onBeforeUnmount,
  onMounted
} from 'vue'

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },

  title: {
    type: String,
    default: '준비 중인 기능'
  },

  description: {
    type: String,
    default: ''
  }
})

const emit = defineEmits([
  'close'
])

const closeModal = () => {
  emit('close')
}

const handleKeydown = (event) => {
  if (
    props.open &&
    event.key === 'Escape'
  ) {
    closeModal()
  }
}

onMounted(() => {
  window.addEventListener(
    'keydown',
    handleKeydown
  )
})

onBeforeUnmount(() => {
  window.removeEventListener(
    'keydown',
    handleKeydown
  )
})
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div
        v-if="open"
        class="modal-backdrop"
        @click.self="closeModal"
      >
        <section
          class="modal-card"
          role="dialog"
          aria-modal="true"
          :aria-label="title"
        >
          <button
            type="button"
            class="modal-close"
            aria-label="닫기"
            @click="closeModal"
          >
            ×
          </button>

          <div class="modal-icon">
            P2
          </div>

          <p class="modal-label">
            COMING SOON
          </p>

          <h2 class="modal-title">
            {{ title }}
          </h2>

          <p class="modal-message">
            P2에서 구현될 예정입니다.
          </p>

          <p
            v-if="description"
            class="modal-description"
          >
            {{ description }}
          </p>

          <button
            type="button"
            class="modal-confirm"
            @click="closeModal"
          >
            확인
          </button>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  z-index: 3000;
  inset: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  padding: 24px;

  background: rgba(25, 29, 42, 0.52);
  backdrop-filter: blur(4px);
}

.modal-card {
  position: relative;

  width: 100%;
  max-width: 420px;
  padding: 42px 36px 34px;

  border: 1px solid #e7e9f0;
  border-radius: 22px;

  text-align: center;

  background: #ffffff;

  box-shadow:
    0 28px 70px
    rgba(23, 27, 43, 0.22);
}

.modal-close {
  position: absolute;
  top: 15px;
  right: 17px;

  width: 34px;
  height: 34px;

  border: 0;
  border-radius: 10px;
  cursor: pointer;

  color: #8b92a2;
  font-size: 25px;
  line-height: 1;

  background: transparent;
}

.modal-close:hover {
  background: #f2f3f7;
}

.modal-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  width: 68px;
  height: 68px;

  border-radius: 22px;

  color: #ffffff;
  font-size: 19px;
  font-weight: 900;

  background:
    linear-gradient(
      140deg,
      #ffad3f,
      #ef5b7c
    );

  box-shadow:
    0 15px 30px
    rgba(239, 91, 124, 0.22);
}

.modal-label {
  margin: 21px 0 7px;

  color: #ef5b7c;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 1.7px;
}

.modal-title {
  margin: 0;

  color: #272c39;
  font-size: 25px;
  font-weight: 800;
  letter-spacing: -0.8px;
}

.modal-message {
  margin: 17px 0 0;

  color: #4e5565;
  font-size: 15px;
  font-weight: 700;
}

.modal-description {
  margin: 11px 0 0;

  color: #888f9e;
  font-size: 13px;
  line-height: 1.65;
}

.modal-confirm {
  width: 100%;
  height: 48px;
  margin-top: 28px;

  border: 0;
  border-radius: 11px;
  cursor: pointer;

  color: #ffffff;
  font-weight: 800;

  background:
    linear-gradient(
      100deg,
      #ec3f91,
      #765fe8
    );
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-active .modal-card,
.modal-leave-active .modal-card {
  transition:
    transform 0.2s ease,
    opacity 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-card,
.modal-leave-to .modal-card {
  opacity: 0;
  transform: translateY(12px) scale(0.97);
}
</style>