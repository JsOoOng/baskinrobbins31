<!-- src/views/main/main.vue -->
<template>
  <div class="main-container">
    <h1 class="title">시스템 접속</h1>
    
    <div class="button-group">
      <!-- 키오스크 화면으로 이동 -->
      <button class="nav-btn kiosk-btn" @click="goTo('/kiosk')">
        키오스크
      </button>
      
      <!-- 지점 로그인으로 이동 (/branch 접속 시 라우터가 알아서 /branch/login으로 보냄) -->
      <button class="nav-btn branch-btn" @click="goTo('/branch')">
        지점
      </button>
      
      <!-- 본사 로그인으로 이동 -->
      <button class="nav-btn head-btn" @click="goTo('/head/login')">
        본점
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 버튼 클릭 시 해당 경로로 라우팅하는 함수
const goTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
/* 전체 화면을 꽉 채우고 중앙 정렬 */
.main-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #f5f6fa;
}

.title {
  margin-bottom: 3rem;
  font-size: 2rem;
  color: #2c3e50;
  font-weight: bold;
}

/* 버튼들을 가로로 나란히 배치하고 간격 띄우기 */
.button-group {
  display: flex;
  gap: 1.5rem;
}

/* 버튼 공통 스타일 */
.nav-btn {
  padding: 1.2rem 3rem;
  font-size: 1.2rem;
  font-weight: bold;
  color: white;
  background-color: #ff7c98; /* 배스킨라빈스 느낌의 핑크색 */
  border: none;
  border-radius: 12px;
  cursor: pointer;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

/* 마우스 올렸을 때 효과 */
.nav-btn:hover {
  background-color: #ff5277;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

/* 클릭했을 때 효과 */
.nav-btn:active {
  transform: translateY(1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>