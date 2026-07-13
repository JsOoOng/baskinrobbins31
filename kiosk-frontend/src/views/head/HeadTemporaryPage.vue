<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const pageTitle = computed(() => {
  return route.meta.title || '본사 관리'
})

const phase = computed(() => {
  return route.meta.phase || 'P1'
})

const isP2 = computed(() => {
  return phase.value === 'P2'
})

const goDashboard = () => {
  router.push('/head/dashboard')
}
</script>

<template>
  <main class="temporary-page">
    <section class="temporary-card">
      <div
        class="temporary-icon"
        :class="{ 'temporary-icon-p2': isP2 }"
      >
        {{ isP2 ? 'P2' : 'BR' }}
      </div>

      <p class="temporary-label">
        HEADQUARTER ADMIN
      </p>

      <h1>
        {{ pageTitle }}
      </h1>

      <p v-if="isP2" class="temporary-message">
        P2에서 구현될 예정입니다.
      </p>

      <p v-else class="temporary-message">
        해당 관리 화면을 순서대로 연결하고 있습니다.
      </p>

      <button
        type="button"
        @click="goDashboard"
      >
        대시보드로 이동
      </button>
    </section>
  </main>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.temporary-page {
  display: flex;
  align-items: center;
  justify-content: center;

  min-height: 100vh;
  padding: 30px;

  background: #f3f6fb;
}

.temporary-card {
  width: 100%;
  max-width: 480px;
  padding: 52px 38px;

  border: 1px solid #e0e5ed;
  border-radius: 20px;

  text-align: center;
  background: #ffffff;
  box-shadow: 0 18px 45px rgba(50, 57, 78, 0.08);
}

.temporary-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  width: 64px;
  height: 64px;

  border-radius: 20px;

  color: #ffffff;
  font-size: 17px;
  font-weight: 900;

  background: linear-gradient(
    140deg,
    #ef3f91,
    #735ee9
  );
}

.temporary-icon-p2 {
  background: linear-gradient(
    140deg,
    #ffad39,
    #ef5b74
  );
}

.temporary-label {
  margin: 24px 0 8px;

  color: #7a68e7;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 1.6px;
}

h1 {
  margin: 0;

  color: #252b39;
  font-size: 29px;
  letter-spacing: -1px;
}

.temporary-message {
  margin: 17px 0 30px;

  color: #747d8e;
  font-size: 14px;
  line-height: 1.7;
}

button {
  height: 46px;
  padding: 0 22px;

  border: 0;
  border-radius: 10px;
  cursor: pointer;

  color: #ffffff;
  font-family: inherit;
  font-weight: 700;

  background: #6e61e8;
}
</style>