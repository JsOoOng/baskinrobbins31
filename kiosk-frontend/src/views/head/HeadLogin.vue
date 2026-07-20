<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'

import { useHeadAuthStore } from '@/stores/head/headAuthStore'

import AppMessageToast
  from '@/components/common/AppMessageToast.vue'

const router = useRouter()
const headAuthStore = useHeadAuthStore()

const {
  loading,
  errorMessage,
  isAuthenticated
} = storeToRefs(headAuthStore)

/*
 * 로그인 입력값
 */
const form = reactive({
  loginId: '',
  password: ''
})

/*
 * 비밀번호 표시 여부
 */
const showPassword = ref(false)

/*
 * 프론트 입력 검증 메시지
 */
const validationMessage = ref('')
/*
 * 화면에 표시할 최종 오류 메시지
 *
 * 프론트 검증 오류를 우선 표시하고,
 * 없으면 서버 로그인 오류를 표시합니다.
 */
 const visibleErrorMessage = computed(() => {
  return (
    validationMessage.value ||
    errorMessage.value ||
    ''
  )
})

/*
 * 오류 메시지 닫기
 */
const clearLoginMessage = () => {
  validationMessage.value = ''
  errorMessage.value = ''
}

/*
 * 로그인 버튼 비활성화 여부
 */
const isSubmitDisabled = computed(() => {
  return (
    loading.value ||
    !form.loginId.trim() ||
    !form.password
  )
})

/*
 * 로그인 처리
 */
 const handleLogin = async () => {
  validationMessage.value = ''
  errorMessage.value = ''

  const loginId = form.loginId.trim()
  const password = form.password

  if (!loginId) {
    validationMessage.value =
      '로그인 ID를 입력해주세요.'

    return
  }

  if (!password) {
    validationMessage.value =
      '비밀번호를 입력해주세요.'

    return
  }

  try {
    await headAuthStore.login(
      loginId,
      password
    )

    await router.replace({
      name: 'head-dashboard'
    })

  } catch {
    /*
     * 실제 오류 메시지는 Store의
     * errorMessage에 저장됩니다.
     */
  }
}

/*
 * 비밀번호 입력창 표시 전환
 */
const togglePasswordVisibility = () => {
  showPassword.value =
    !showPassword.value
}

/*
 * 이미 본사 로그인 상태라면
 * 로그인 화면을 다시 보여주지 않습니다.
 */
onMounted(() => {
  if (isAuthenticated.value) {
    router.replace(
      '/head/dashboard'
    )
  }
})
</script>

<template>
  <AppMessageToast
    :message="visibleErrorMessage"
    type="error"
    @close="clearLoginMessage"
  />

  <main class="head-login-page">
    <!-- 왼쪽 소개 영역 -->
    <section class="head-login-brand">
      <div class="brand-content">
        <div class="brand-symbol">
          <span class="brand-symbol-inner">
            31
          </span>
        </div>

        <p class="brand-label">
          Baskin Robbins
        </p>

        <h1 class="brand-title">
          베스킨라빈스<br />
          본사 관리자 시스템
        </h1>

        <p class="brand-description">
          지점, 상품, 프로모션과 매출 현황을
          하나의 시스템에서 관리하세요.
        </p>

        <div class="brand-feature-list">
          <div class="brand-feature">
            <span class="feature-icon">
              ✓
            </span>

            <span>
              전체 지점 통합 관리
            </span>
          </div>

          <div class="brand-feature">
            <span class="feature-icon">
              ✓
            </span>

            <span>
              상품 및 카테고리 관리
            </span>
          </div>

          <div class="brand-feature">
            <span class="feature-icon">
              ✓
            </span>

            <span>
              매출 통계 및 운영 분석
            </span>
          </div>
        </div>
      </div>

      <p class="brand-copyright">
        © 2026 Baskin Robbins 31
      </p>
    </section>

    <!-- 오른쪽 로그인 영역 -->
    <section class="head-login-form-section">
      <div class="login-card">
        <div class="login-header">
          <div class="mobile-brand-symbol">
            31
          </div>

          <p class="login-eyebrow">
            HEADQUARTER ADMIN
          </p>

          <h2 class="login-title">
            관리자 로그인
          </h2>

          <p class="login-description">
            본사 관리자 계정으로 로그인해주세요.
          </p>
        </div>

        <form
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <!-- 로그인 ID -->
          <div class="form-field">
            <label
              for="head-login-id"
              class="form-label"
            >
              로그인 ID
            </label>

            <div class="input-wrapper">
              <span class="input-icon">
                <svg
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path
                    d="M20 21a8 8 0 0 0-16 0"
                  />
                  <circle
                    cx="12"
                    cy="7"
                    r="4"
                  />
                </svg>
              </span>

              <input
                id="head-login-id"
                v-model="form.loginId"
                class="form-input"
                type="text"
                autocomplete="username"
                maxlength="50"
                placeholder="로그인 ID를 입력하세요"
                :disabled="loading"
              />
            </div>
          </div>

          <!-- 비밀번호 -->
          <div class="form-field">
            <label
              for="head-login-password"
              class="form-label"
            >
              비밀번호
            </label>

            <div class="input-wrapper">
              <span class="input-icon">
                <svg
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <rect
                    x="4"
                    y="10"
                    width="16"
                    height="10"
                    rx="2"
                  />
                  <path
                    d="M8 10V7a4 4 0 0 1 8 0v3"
                  />
                </svg>
              </span>

              <input
                id="head-login-password"
                v-model="form.password"
                class="form-input password-input"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                placeholder="비밀번호를 입력하세요"
                :disabled="loading"
              />

              <button
                type="button"
                class="password-toggle"
                :aria-label="
                  showPassword
                    ? '비밀번호 숨기기'
                    : '비밀번호 표시'
                "
                @click="togglePasswordVisibility"
              >
                <svg
                  v-if="!showPassword"
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path
                    d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z"
                  />
                  <circle
                    cx="12"
                    cy="12"
                    r="3"
                  />
                </svg>

                <svg
                  v-else
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path
                    d="m3 3 18 18"
                  />
                  <path
                    d="M10.6 6.2A10.8 10.8 0 0 1 12 6c6.5 0 10 6 10 6a16 16 0 0 1-2.1 2.8"
                  />
                  <path
                    d="M6.5 6.5C3.5 8.3 2 12 2 12s3.5 6 10 6c1.6 0 3-.4 4.2-1"
                  />
                </svg>
              </button>
            </div>
          </div>
          
          <!-- 로그인 버튼 -->
          <button
            class="login-button"
            type="submit"
            :disabled="isSubmitDisabled"
          >
            <span
              v-if="loading"
              class="loading-spinner"
              aria-hidden="true"
            />

            <span>
              {{
                loading
                  ? '로그인 중...'
                  : '로그인'
              }}
            </span>
          </button>
        </form>

        <div class="login-notice">
          <div class="notice-icon">
            i
          </div>

          <p>
            승인된 본사 관리자 계정만
            접근할 수 있습니다. 로그인 기록은
            보안 목적으로 저장될 수 있습니다.
          </p>
        </div>

        <p class="login-support">
          계정에 문제가 있다면
          최고 관리자에게 문의해주세요.
        </p>
      </div>
    </section>
  </main>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.head-login-page {
  display: grid;
  grid-template-columns:
    minmax(420px, 42%)
    minmax(500px, 58%);

  width: 100%;
  min-width: 100%;
  min-height: 100vh;
  min-height: 100dvh;

  margin: 0;
  padding: 0;

  background: #f4f7fb;
}

/*
 * 왼쪽 브랜드 영역
 */
 .head-login-brand {
  position: relative;

  display: flex;
  align-items: center;
  justify-content: center;

  min-width: 0;
  min-height: 100vh;
  min-height: 100dvh;

  overflow: hidden;
  padding: 70px;

  color: #ffffff;

  background:
    radial-gradient(
      circle at 20% 15%,
      rgba(255, 255, 255, 0.2),
      transparent 30%
    ),
    radial-gradient(
      circle at 90% 90%,
      rgba(255, 255, 255, 0.13),
      transparent 35%
    ),
    linear-gradient(
      145deg,
      #ef3a8b 0%,
      #d93ca7 42%,
      #675de8 100%
    );
}

.head-login-brand::before,
.head-login-brand::after {
  position: absolute;
  display: block;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 50%;
  content: '';
}

.head-login-brand::before {
  top: -130px;
  left: -130px;
  width: 390px;
  height: 390px;
}

.head-login-brand::after {
  right: -170px;
  bottom: -170px;
  width: 470px;
  height: 470px;
}

.brand-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 470px;
}

.brand-symbol {
  display: flex;
  align-items: center;
  justify-content: center;

  width: 72px;
  height: 72px;
  margin-bottom: 24px;

  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 22px;

  background: rgba(255, 255, 255, 0.16);
  box-shadow: 0 18px 40px rgba(49, 22, 122, 0.18);

  backdrop-filter: blur(12px);
}

.brand-symbol-inner {
  font-size: 26px;
  font-weight: 900;
  letter-spacing: -2px;
}

.brand-label {
  margin: 0 0 10px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 2.5px;
  text-transform: uppercase;
  opacity: 0.78;
}

.brand-title {
  margin: 22px 0 28px;

  font-size: 42px;
  line-height: 1.32;
  font-weight: 800;
  letter-spacing: -1.6px;

  color: #ffffff;
  word-break: keep-all;
}

.brand-title span {
  display: block;
  white-space: nowrap;
}

@media (max-width: 1200px) {
  .brand-title {
    font-size: 38px;
  }
}

.brand-description {
  max-width: 420px;
  margin: 28px 0 0;

  font-size: 16px;
  line-height: 1.8;
  opacity: 0.84;
}

.brand-feature-list {
  display: grid;
  gap: 15px;
  margin-top: 42px;
}

.brand-feature {
  display: flex;
  gap: 12px;
  align-items: center;

  font-size: 14px;
  font-weight: 600;
}

.feature-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  width: 25px;
  height: 25px;

  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
}

.brand-copyright {
  position: absolute;
  bottom: 26px;
  left: 70px;
  z-index: 1;

  margin: 0;
  font-size: 12px;
  opacity: 0.62;
}

/*
 * 오른쪽 로그인 영역
 */
 .head-login-form-section {
  display: flex;
  align-items: center;
  justify-content: center;

  min-width: 0;
  min-height: 100vh;
  min-height: 100dvh;

  padding: 48px 60px;

  background: #f4f7fb;
}

.login-card {
  width: 100%;
  max-width: 460px;
}

.mobile-brand-symbol {
  display: none;
}

.login-header {
  margin-bottom: 34px;
}

.login-eyebrow {
  margin: 0 0 8px;

  color: #715eea;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 1.8px;
}

.login-title {
  margin: 0;

  color: #242938;
  font-size: 34px;
  font-weight: 800;
  letter-spacing: -1.5px;
}

.login-description {
  margin: 12px 0 0;

  color: #7a8294;
  font-size: 14px;
  line-height: 1.6;
}

.login-form {
  display: grid;
  gap: 21px;
}

.form-field {
  display: grid;
  gap: 9px;
}

.form-label {
  color: #343a4b;
  font-size: 13px;
  font-weight: 700;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  top: 50%;
  left: 16px;

  display: flex;
  width: 19px;
  height: 19px;

  color: #9ba3b5;
  transform: translateY(-50%);
}

.input-icon svg,
.password-toggle svg {
  width: 100%;
  height: 100%;

  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.form-input {
  width: 100%;
  height: 54px;

  padding: 0 17px 0 48px;

  border: 1px solid #dfe4ec;
  border-radius: 12px;
  outline: none;

  color: #292e3d;
  font-family: inherit;
  font-size: 14px;

  background: #ffffff;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    background 0.2s ease;
}

.password-input {
  padding-right: 50px;
}

.form-input::placeholder {
  color: #adb4c2;
}

.form-input:focus {
  border-color: #7766ee;
  box-shadow: 0 0 0 4px rgba(119, 102, 238, 0.1);
}

.form-input:disabled {
  cursor: not-allowed;
  background: #f1f3f7;
}

.password-toggle {
  position: absolute;
  top: 50%;
  right: 16px;

  display: flex;
  width: 20px;
  height: 20px;
  padding: 0;

  border: 0;
  cursor: pointer;

  color: #969eae;
  background: transparent;

  transform: translateY(-50%);
}

.login-button {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: center;

  width: 100%;
  height: 55px;
  margin-top: 3px;

  border: 0;
  border-radius: 12px;
  cursor: pointer;

  color: #ffffff;
  font-family: inherit;
  font-size: 15px;
  font-weight: 800;

  background:
    linear-gradient(
      100deg,
      #ec3f91,
      #795fe9
    );

  box-shadow:
    0 13px 28px
    rgba(115, 82, 218, 0.22);

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    opacity 0.2s ease;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-1px);

  box-shadow:
    0 16px 32px
    rgba(115, 82, 218, 0.28);
}

.login-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
  box-shadow: none;
}

.loading-spinner {
  width: 17px;
  height: 17px;

  border: 2px solid
    rgba(255, 255, 255, 0.38);

  border-top-color: #ffffff;
  border-radius: 50%;

  animation: spin 0.8s linear infinite;
}

.login-notice {
  display: flex;
  gap: 11px;
  align-items: flex-start;

  margin-top: 28px;
  padding: 14px 15px;

  border: 1px solid #e2e6ee;
  border-radius: 11px;

  color: #7b8394;
  font-size: 12px;
  line-height: 1.65;

  background: #f8f9fc;
}

.login-notice p {
  margin: 0;
}

.notice-icon {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;

  width: 19px;
  height: 19px;

  border-radius: 50%;

  color: #7766ea;
  font-size: 11px;
  font-weight: 900;

  background: #ebe8ff;
}

.login-support {
  margin: 21px 0 0;

  color: #9aa1af;
  font-size: 12px;
  text-align: center;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/*
 * 태블릿
 */
@media (max-width: 1000px) {
  .head-login-page {
    grid-template-columns:
      minmax(300px, 38%)
      minmax(440px, 62%);
  }

  .head-login-brand {
    padding: 50px 40px;
  }

  .brand-title {
    font-size: 39px;
  }

  .brand-copyright {
    left: 40px;
  }

  .head-login-form-section {
    padding: 45px;
  }
}

/*
 * 모바일
 */
@media (max-width: 760px) {
  .head-login-page {
    display: block;
    min-height: 100vh;

    background:
      linear-gradient(
        145deg,
        #f7f5ff,
        #fff5fa
      );
  }

  .head-login-brand {
    display: none;
  }

  .head-login-form-section {
    min-height: 100vh;
    padding: 30px 22px;
  }

  .login-card {
    max-width: 430px;
  }

  .mobile-brand-symbol {
    display: inline-flex;
    align-items: center;
    justify-content: center;

    width: 52px;
    height: 52px;
    margin-bottom: 24px;

    border-radius: 16px;

    color: #ffffff;
    font-size: 19px;
    font-weight: 900;

    background:
      linear-gradient(
        140deg,
        #ef3e91,
        #735ee9
      );

    box-shadow:
      0 12px 25px
      rgba(115, 82, 218, 0.22);
  }

  .login-title {
    font-size: 29px;
  }
}
</style>
