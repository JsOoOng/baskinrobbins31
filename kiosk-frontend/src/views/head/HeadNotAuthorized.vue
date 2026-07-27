<!--
  [화면 흐름 안내] HeadNotAuthorized
  역할: 본사 관리에서 사용자가 보는 화면이다.
  진입: /head/not-authorized -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> props·Pinia·상위 화면 상태 -> 응답/상태 반영
  다음 이동: /head/dashboard, /head/login
-->
<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goDashboard = () => {
  router.push('/head/dashboard')
}

const goLogin = () => {
  router.push('/head/login')
}
</script>

<template>
  <main class="unauthorized-page">
    <section class="unauthorized-card">
      <div class="error-code">
        403
      </div>

      <h1>
        접근 권한이 없습니다
      </h1>

      <p>
        현재 로그인한 관리자에게는<br />
        해당 화면에 접근할 권한이 없습니다.
      </p>

      <div class="button-group">
        <button
          type="button"
          class="secondary-button"
          @click="goLogin"
        >
          로그인 화면
        </button>

        <button
          type="button"
          class="primary-button"
          @click="goDashboard"
        >
          대시보드
        </button>
      </div>
    </section>
  </main>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.unauthorized-page {
  display: flex;
  align-items: center;
  justify-content: center;

  min-height: 100vh;
  padding: 30px;

  background: #f4f6fa;
}

.unauthorized-card {
  width: 100%;
  max-width: 460px;
  padding: 50px 35px;

  border: 1px solid #e1e5ed;
  border-radius: 18px;

  text-align: center;
  background: #ffffff;
  box-shadow: 0 18px 40px rgba(45, 52, 72, 0.08);
}

.error-code {
  color: #ef5474;
  font-size: 52px;
  font-weight: 900;
}

h1 {
  margin: 12px 0;

  color: #252b38;
  font-size: 27px;
}

p {
  margin: 0;

  color: #778091;
  font-size: 14px;
  line-height: 1.7;
}

.button-group {
  display: flex;
  gap: 10px;
  justify-content: center;

  margin-top: 30px;
}

button {
  height: 44px;
  padding: 0 19px;

  border-radius: 9px;
  cursor: pointer;

  font-family: inherit;
  font-weight: 700;
}

.secondary-button {
  border: 1px solid #dce1e9;

  color: #5e6677;
  background: #ffffff;
}

.primary-button {
  border: 1px solid #7161e9;

  color: #ffffff;
  background: #7161e9;
}
</style>