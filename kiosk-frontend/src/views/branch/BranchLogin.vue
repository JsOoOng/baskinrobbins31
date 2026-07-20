<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()

const loginId = ref('')
const password = ref('')

const loginIdInput = ref(null)
const passwordInput = ref(null)

const login = async () => {

  // 아이디 미입력
  if (!loginId.value.trim()) {
    alert('아이디를 입력해주세요.')
    loginIdInput.value.focus()
    return
  }

  // 비밀번호 미입력
  if (!password.value.trim()) {
    alert('비밀번호를 입력해주세요.')
    passwordInput.value.focus()
    return
  }

  try {

    const response = await api.post('/branch/login', {
      loginId: loginId.value,
      password: password.value
    })

    // JWT 저장
    localStorage.setItem(
      'token',
      response.data.token
    )

    // 로그인 정보 저장
    localStorage.setItem(
      'branchUser',
      JSON.stringify(response.data.user)
    )

    alert('로그인 성공')

    router.push('/branch/main')

  } catch (error) {

    console.error(error)

    if (error.response) {

      alert('아이디 또는 비밀번호가 일치하지 않습니다.')

      // 아이디 입력창으로 이동
      loginIdInput.value.focus()
      loginIdInput.value.select()

    } else {

      alert('서버와 연결할 수 없습니다.')

      // 아이디 입력창으로 이동
      loginIdInput.value.focus()

    }

  }

}
</script>

<template>
  <div class="login-container">

    <h2>지점 관리자 로그인</h2>

    <div class="form-group">
      <label>아이디</label>

      <input
        ref="loginIdInput"
        type="text"
        v-model="loginId"
        placeholder="아이디를 입력하세요"
      />
    </div>

    <div class="form-group">
      <label>비밀번호</label>

      <input
        ref="passwordInput"
        type="password"
        v-model="password"
        placeholder="비밀번호를 입력하세요"
        @keyup.enter="login"
      />
    </div>

    <button @click="login">
      로그인
    </button>

  </div>
</template>

<style scoped>

.login-container {

  width: 350px;

  margin: 100px auto;

  padding: 30px;

  border: 1px solid #ddd;

  border-radius: 10px;

}

h2 {

  text-align: center;

  margin-bottom: 30px;

}

.form-group {

  margin-bottom: 20px;

}

label {

  display: block;

  margin-bottom: 5px;

  font-weight: bold;

}

input {

  width: 100%;

  padding: 10px;

  box-sizing: border-box;

}

button {

  width: 100%;

  padding: 12px;

  border: none;

  background-color: #1976d2;

  color: white;

  font-size: 16px;

  cursor: pointer;

  border-radius: 5px;

}

button:hover {

  background-color: #1565c0;

}

</style>
