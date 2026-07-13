import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import {
  extractHeadApiData,
  extractHeadApiErrorMessage,
  getHeadAdminMe,
  loginHeadAdmin,
  logoutHeadAdmin
} from '@/api/head/headAuthApi'

const TOKEN_KEY = 'token'
const HEAD_USER_KEY = 'headUser'
const AUTH_USER_TYPE_KEY = 'authUserType'

/*
 * localStorage의 사용자 정보를 안전하게 읽습니다.
 */
const readStoredHeadUser = () => {
  const storedUser = localStorage.getItem(
    HEAD_USER_KEY
  )

  if (!storedUser) {
    return null
  }

  try {
    return JSON.parse(storedUser)

  } catch {
    localStorage.removeItem(
      HEAD_USER_KEY
    )

    return null
  }
}

/*
 * 기존 HEAD_ADMIN 값을 ADMIN으로 통일합니다.
 */
const normalizeHeadRole = (role) => {
  if (role === 'HEAD_ADMIN') {
    return 'ADMIN'
  }

  return role
}

export const useHeadAuthStore = defineStore(
  'headAuth',

  () => {
    /*
     * 상태
     */
    const token = ref(
      localStorage.getItem(TOKEN_KEY)
    )

    const headUser = ref(
      readStoredHeadUser()
    )

    const loading = ref(false)
    const errorMessage = ref('')

    /*
     * 로그인 여부
     */
    const isAuthenticated = computed(() => {
      return Boolean(
        token.value &&
        headUser.value
      )
    })

    /*
     * 현재 권한
     */
    const role = computed(() => {
      return headUser.value?.role ?? null
    })

    /*
     * 일반 본사 관리자 여부
     */
    const isAdmin = computed(() => {
      return role.value === 'ADMIN'
    })

    /*
     * 최고 관리자 여부
     */
    const isSuperAdmin = computed(() => {
      return role.value === 'SUPER_ADMIN'
    })

    /*
     * 본사 관리자 권한 여부
     */
    const hasHeadAccess = computed(() => {
      return [
        'ADMIN',
        'SUPER_ADMIN'
      ].includes(role.value)
    })

    /*
     * 화면 표시용 관리자 이름
     */
    const displayName = computed(() => {
      return (
        headUser.value?.name ||
        headUser.value?.loginId ||
        '관리자'
      )
    })

    /*
     * localStorage와 Store에 인증 정보 저장
     */
    const saveAuthentication = (
      newToken,
      userData
    ) => {
      const normalizedUser = {
        ...userData,
        role: normalizeHeadRole(
          userData?.role
        )
      }

      token.value = newToken
      headUser.value = normalizedUser

      localStorage.setItem(
        TOKEN_KEY,
        newToken
      )

      localStorage.setItem(
        HEAD_USER_KEY,
        JSON.stringify(normalizedUser)
      )

      localStorage.setItem(
        AUTH_USER_TYPE_KEY,
        'HEAD'
      )
    }

    /*
     * 인증 정보 삭제
     */
    const clearAuthentication = () => {
      token.value = null
      headUser.value = null
      errorMessage.value = ''

      localStorage.removeItem(
        TOKEN_KEY
      )

      localStorage.removeItem(
        HEAD_USER_KEY
      )

      localStorage.removeItem(
        AUTH_USER_TYPE_KEY
      )
    }

    /*
     * 본사 관리자 로그인
     */
    const login = async (
      loginId,
      password
    ) => {
      loading.value = true
      errorMessage.value = ''

      try {
        const responseBody =
          await loginHeadAdmin({
            loginId: loginId.trim(),
            password
          })

        /*
         * HeadApiResponse가 success=false를
         * HTTP 200으로 반환하는 경우도 방어합니다.
         */
        if (responseBody?.success === false) {
          throw new Error(
            responseBody.message ||
            '본사 관리자 로그인에 실패했습니다.'
          )
        }

        const loginData =
          extractHeadApiData(responseBody)

        if (!loginData) {
          throw new Error(
            '로그인 응답 정보가 없습니다.'
          )
        }

        if (!loginData.token) {
          throw new Error(
            '로그인 토큰이 발급되지 않았습니다.'
          )
        }

        const normalizedRole =
          normalizeHeadRole(
            loginData.role
          )

        if (
          ![
            'ADMIN',
            'SUPER_ADMIN'
          ].includes(normalizedRole)
        ) {
          throw new Error(
            '본사 관리자 권한이 없습니다.'
          )
        }

        /*
         * 사용자 정보에는 토큰을 중복 저장하지 않습니다.
         */
        const {
          token: issuedToken,
          ...userData
        } = loginData

        saveAuthentication(
          issuedToken,
          {
            ...userData,
            role: normalizedRole
          }
        )

        return headUser.value

      } catch (error) {
        clearAuthentication()

        errorMessage.value =
          extractHeadApiErrorMessage(
            error,
            '본사 관리자 로그인에 실패했습니다.'
          )

        throw error

      } finally {
        loading.value = false
      }
    }

    /*
     * JWT를 이용해 현재 로그인한 관리자 조회
     */
    const fetchMe = async () => {
      if (!token.value) {
        clearAuthentication()
        return null
      }

      loading.value = true
      errorMessage.value = ''

      try {
        const responseBody =
          await getHeadAdminMe()

        if (responseBody?.success === false) {
          throw new Error(
            responseBody.message ||
            '관리자 정보를 불러오지 못했습니다.'
          )
        }

        const userData =
          extractHeadApiData(responseBody)

        if (!userData) {
          throw new Error(
            '관리자 정보가 없습니다.'
          )
        }

        const normalizedRole =
          normalizeHeadRole(
            userData.role
          )

        if (
          ![
            'ADMIN',
            'SUPER_ADMIN'
          ].includes(normalizedRole)
        ) {
          throw new Error(
            '본사 관리자 권한이 없습니다.'
          )
        }

        /*
         * /me 응답의 token은 null이므로
         * 기존 token.value를 그대로 유지합니다.
         */
        saveAuthentication(
          token.value,
          {
            ...userData,
            role: normalizedRole
          }
        )

        return headUser.value

      } catch (error) {
        clearAuthentication()

        errorMessage.value =
          extractHeadApiErrorMessage(
            error,
            '관리자 정보를 확인하지 못했습니다.'
          )

        throw error

      } finally {
        loading.value = false
      }
    }

    /*
     * 새로고침 후 로그인 상태 복구
     */
    const restoreSession = async () => {
      if (!token.value) {
        clearAuthentication()
        return false
      }

      try {
        await fetchMe()
        return true

      } catch {
        return false
      }
    }

    /*
     * 로그아웃
     */
    const logout = async () => {
      loading.value = true

      try {
        if (token.value) {
          await logoutHeadAdmin()
        }

      } catch {
        /*
         * JWT 로그아웃은 프론트 토큰 삭제가 핵심이므로
         * 서버 요청이 실패해도 인증 정보는 삭제합니다.
         */

      } finally {
        clearAuthentication()
        loading.value = false
      }
    }

    return {
      token,
      headUser,
      loading,
      errorMessage,

      isAuthenticated,
      role,
      isAdmin,
      isSuperAdmin,
      hasHeadAccess,
      displayName,

      login,
      fetchMe,
      restoreSession,
      logout,
      clearAuthentication
    }
  }
)