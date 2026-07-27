<!--
  [화면 흐름 안내] HeadTemporaryPage
  역할: 본사 관리 화면에서 재사용되는 UI 컴포넌트다.
  진입: 상위 라우트 또는 부모 컴포넌트 -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> props·Pinia·상위 화면 상태 -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<script setup>
import { computed } from 'vue'
import {
  useRoute,
  useRouter
} from 'vue-router'

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

/*
 * 현재 페이지가 대시보드일 때는
 * 대시보드 이동 버튼을 표시하지 않습니다.
 */
const showDashboardButton = computed(() => {
  return route.name !== 'head-dashboard'
})

const goDashboard = async () => {
  if (route.name === 'head-dashboard') {
    return
  }

  await router.push({
    name: 'head-dashboard'
  })
}
</script>

<template>
  <section class="temporary-page">
    <div class="temporary-card">
      <div
        class="temporary-icon"
        :class="{
          'temporary-icon-p2': isP2
        }"
      >
        {{ phase }}
      </div>

      <p class="temporary-label">
        HEADQUARTER ADMIN
      </p>

      <h2>
        {{ pageTitle }}
      </h2>

      <p
        v-if="isP2"
        class="temporary-message"
      >
        P2에서 구현될 예정입니다.
      </p>

      <p
        v-else
        class="temporary-message"
      >
        해당 관리 화면을 순서대로 연결하고 있습니다.
      </p>
    </div>
  </section>
</template>

<style scoped>
.temporary-page {
  display: flex;
  align-items: center;
  justify-content: center;

  min-height:
    calc(100vh - 145px);
}

.temporary-card {
  width: 100%;
  max-width: 500px;
  padding: 52px 38px;

  border: 1px solid #e2e5ec;
  border-radius: 20px;

  text-align: center;

  background: #ffffff;

  box-shadow:
    0 18px 45px
    rgba(50, 57, 78, 0.07);
}

.temporary-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  width: 65px;
  height: 65px;

  border-radius: 20px;

  color: #ffffff;
  font-size: 16px;
  font-weight: 900;

  background:
    linear-gradient(
      140deg,
      #ef3f91,
      #735ee9
    );
}

.temporary-icon-p2 {
  background:
    linear-gradient(
      140deg,
      #ffae3d,
      #ef5c78
    );
}

.temporary-label {
  margin: 23px 0 8px;

  color: #7462e5;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 1.6px;
}

h2 {
  margin: 0;

  color: #292e3b;
  font-size: 28px;
  letter-spacing: -1px;
}

.temporary-message {
  margin: 17px 0 0;

  color: #7d8595;
  font-size: 14px;
  line-height: 1.7;
}
</style>