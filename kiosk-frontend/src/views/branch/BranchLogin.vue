<!--
  지점 관리자 로그인 화면이다.
  인증 성공 시 토큰과 지점 정보를 저장하고 공통 지점 대시보드로 이동한다.
-->
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import VueTurnstile from 'vue-turnstile'

const router = useRouter()

const loginId = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const errorMessage = ref('')
const turnstileToken = ref('')
const turnstileSiteKey = import.meta.env.VITE_TURNSTILE_SITE_KEY

const loginIdInput = ref(null)
const passwordInput = ref(null)

// 필수값 검사 후 서버 인증을 요청하고, 오류는 브라우저 alert 대신 화면 안에 표시한다.
const login = async () => {
  errorMessage.value = ''

  if (!loginId.value.trim()) {
    errorMessage.value = '아이디를 입력해 주세요.'
    loginIdInput.value?.focus()
    return
  }

  if (!password.value) {
    errorMessage.value = '비밀번호를 입력해 주세요.'
    passwordInput.value?.focus()
    return
  }

  if (!turnstileToken.value) {
    errorMessage.value = '자동입력 방지(Turnstile) 인증을 완료해 주세요.'
    return
  }

  loading.value = true

  try {
    const response = await api.post('/branch/login', {
      loginId: loginId.value.trim(),
      password: password.value,
      turnstileToken: turnstileToken.value
    })

    localStorage.setItem('token', response.data.token)
    // 공통 헤더·사이드바에서도 지점명과 관리자명을 사용할 수 있도록 함께 저장한다.
    localStorage.setItem('branchUser', JSON.stringify(response.data.user))
    await router.push('/branch/main')
  } catch (error) {
    console.error(error)
    errorMessage.value = error.response
      ? '아이디 또는 비밀번호가 일치하지 않습니다.'
      : '서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.'
    loginIdInput.value?.focus()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="branch-login-page">
    <section class="brand-panel" aria-label="지점 관리 시스템 소개">
      <div class="brand-content">
        <div class="brand-lockup">
          <span class="brand-mark">31</span>
          <div>
            <strong>BASKIN ROBBINS</strong>
            <span>BRANCH OPERATIONS</span>
          </div>
        </div>

        <p class="eyebrow">SMART STORE MANAGEMENT</p>
        <h1>매장 운영의 모든 순간을<br />한곳에서 관리하세요.</h1>
        <p class="brand-description">
          주문부터 재고와 매출까지, 매장에 필요한 업무를 빠르고 편리하게 처리할 수 있습니다.
        </p>

        <ul class="feature-list">
          <li><span>✓</span> 주문 및 재고 현황 실시간 관리</li>
          <li><span>✓</span> 직원과 근무 일정의 효율적인 운영</li>
          <li><span>✓</span> 매출·지출 내역을 한눈에 확인</li>
        </ul>
      </div>
      <p class="copyright">© Baskin Robbins 31. Branch Management System</p>
    </section>

    <section class="form-panel">
      <div class="login-card">
        <div class="mobile-brand" aria-hidden="true">
          <span class="brand-mark">31</span>
          <strong>BASKIN ROBBINS</strong>
        </div>

        <p class="form-eyebrow">BRANCH ADMIN</p>
        <h2>지점 관리자 로그인</h2>
        <p class="form-description">매장 운영을 시작하려면 관리자 계정으로 로그인해 주세요.</p>

        <form @submit.prevent="login" novalidate>
          <label for="branch-login-id">아이디</label>
          <div class="input-wrap">
            <span aria-hidden="true">●</span>
            <input
              id="branch-login-id"
              ref="loginIdInput"
              v-model="loginId"
              type="text"
              autocomplete="username"
              placeholder="아이디를 입력하세요"
              :disabled="loading"
            />
          </div>

          <label for="branch-password">비밀번호</label>
          <div class="input-wrap">
            <span aria-hidden="true">◆</span>
            <input
              id="branch-password"
              ref="passwordInput"
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="비밀번호를 입력하세요"
              :disabled="loading"
            />
            <button
              class="password-toggle"
              type="button"
              :aria-label="showPassword ? '비밀번호 숨기기' : '비밀번호 보기'"
              @click="showPassword = !showPassword"
            >
              {{ showPassword ? '숨김' : '보기' }}
            </button>
          </div>

          <div style="display: flex; justify-content: center; margin-top: 10px;">
            <VueTurnstile :site-key="turnstileSiteKey" v-model="turnstileToken" />
          </div>

          <p v-if="errorMessage" class="error-message" role="alert">{{ errorMessage }}</p>

          <button class="login-button" type="submit" :disabled="loading || !turnstileToken">
            <span v-if="loading" class="spinner" aria-hidden="true"></span>
            {{ loading ? '로그인 중...' : '로그인' }}
          </button>
        </form>

        <p class="support">계정 관련 문의는 본사 관리자에게 연락해 주세요.</p>
      </div>
    </section>
  </main>
</template>

<style scoped>
.branch-login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(420px, 1.08fr) minmax(480px, 0.92fr);
  background: #fff;
  color: #241936;
}

.brand-panel {
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: clamp(42px, 6vw, 84px);
  color: #fff;
  background:
    radial-gradient(circle at 82% 18%, rgba(255, 255, 255, 0.17), transparent 23%),
    radial-gradient(circle at 10% 88%, rgba(255, 100, 177, 0.24), transparent 28%),
    linear-gradient(145deg, #311359 0%, #57218a 52%, #7e2d91 100%);
}

.brand-panel::after {
  content: "";
  position: absolute;
  width: 440px;
  height: 440px;
  right: -220px;
  bottom: -190px;
  border: 78px solid rgba(255, 255, 255, 0.06);
  border-radius: 50%;
}

.brand-content,
.copyright {
  position: relative;
  z-index: 1;
}

.brand-lockup,
.mobile-brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-lockup > div {
  display: grid;
  gap: 3px;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 58px;
  height: 58px;
  border-radius: 18px;
  color: #fff;
  font-size: 25px;
  font-weight: 900;
  background: linear-gradient(145deg, #f25a9b, #e83788);
  box-shadow: 0 12px 28px rgba(21, 6, 45, 0.28);
}

.brand-lockup strong {
  font-size: 15px;
  letter-spacing: 0.12em;
}

.brand-lockup div span {
  color: rgba(255, 255, 255, 0.66);
  font-size: 11px;
  letter-spacing: 0.18em;
}

.eyebrow {
  margin: clamp(70px, 12vh, 130px) 0 18px;
  color: #f9a8cf;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.2em;
}

.brand-panel h1 {
  margin: 0;
  font-size: clamp(34px, 3.4vw, 55px);
  line-height: 1.24;
  letter-spacing: -0.05em;
}

.brand-description {
  max-width: 540px;
  margin: 24px 0 36px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 16px;
  line-height: 1.8;
}

.feature-list {
  display: grid;
  gap: 16px;
  margin: 0;
  padding: 0;
  list-style: none;
  color: rgba(255, 255, 255, 0.88);
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 12px;
}

.feature-list span {
  display: grid;
  place-items: center;
  width: 25px;
  height: 25px;
  border-radius: 50%;
  color: #f8b2d4;
  background: rgba(255, 255, 255, 0.11);
}

.copyright {
  margin: 40px 0 0;
  color: rgba(255, 255, 255, 0.42);
  font-size: 12px;
}

.form-panel {
  display: grid;
  place-items: center;
  padding: 48px clamp(32px, 7vw, 110px);
}

.login-card {
  width: min(100%, 440px);
}

.mobile-brand {
  display: none;
  margin-bottom: 48px;
  color: #40156b;
}

.mobile-brand .brand-mark {
  width: 48px;
  height: 48px;
  border-radius: 15px;
  font-size: 21px;
}

.form-eyebrow {
  margin: 0 0 12px;
  color: #eb3f8c;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.18em;
}

.login-card h2 {
  margin: 0;
  font-size: clamp(30px, 3vw, 40px);
  letter-spacing: -0.045em;
}

.form-description {
  margin: 14px 0 38px;
  color: #81788e;
  line-height: 1.6;
}

form {
  display: grid;
  gap: 10px;
}

form label {
  margin-top: 10px;
  color: #4a4057;
  font-size: 14px;
  font-weight: 800;
}

.input-wrap {
  min-height: 54px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  border: 1px solid #ddd7e5;
  border-radius: 13px;
  background: #fbfafd;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}

.input-wrap:focus-within {
  border-color: #9d4fc2;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(135, 60, 176, 0.1);
}

.input-wrap > span {
  color: #b29cbe;
  font-size: 9px;
}

.input-wrap input {
  min-width: 0;
  flex: 1;
  border: 0;
  outline: 0;
  color: #2d2438;
  font: inherit;
  background: transparent;
}

.input-wrap input::placeholder {
  color: #b9b1c0;
}

.password-toggle {
  flex: 0 0 auto;
  padding: 6px;
  border: 0;
  color: #786b83;
  font-size: 12px;
  font-weight: 700;
  background: transparent;
  cursor: pointer;
}

.error-message {
  margin: 4px 0 0;
  padding: 11px 13px;
  border-radius: 9px;
  color: #c52c4f;
  font-size: 13px;
  background: #fff0f4;
}

.login-button {
  min-height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  margin-top: 18px;
  border: 0;
  border-radius: 13px;
  color: #fff;
  font-size: 16px;
  font-weight: 800;
  background: linear-gradient(135deg, #5d2189, #8b359b);
  box-shadow: 0 13px 25px rgba(79, 28, 119, 0.22);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 16px 30px rgba(79, 28, 119, 0.29);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: wait;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.38);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.support {
  margin: 28px 0 0;
  color: #a198aa;
  font-size: 13px;
  text-align: center;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 900px) {
  .branch-login-page {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    display: none;
  }

  .form-panel {
    min-height: 100vh;
    padding: 42px 24px;
    background:
      radial-gradient(circle at 100% 0, rgba(230, 57, 135, 0.09), transparent 32%),
      #fff;
  }

  .mobile-brand {
    display: flex;
  }
}

@media (max-width: 480px) {
  .form-panel {
    align-items: start;
    padding-top: 30px;
  }

  .mobile-brand {
    margin-bottom: 38px;
  }

  .form-description {
    margin-bottom: 28px;
  }
}
</style>
