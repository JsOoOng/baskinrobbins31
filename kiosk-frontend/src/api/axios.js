// src/api/axios.js
import axios from 'axios'; // 1. 라이브러리 로드

// 2. 변수 이름을 axios가 아니라 'instance'로 명확히 분리!
const instance = axios.create({
  // 백엔드 스프링 부트 서버 주소
  baseURL: 'http://localhost:8888', 
  
  // 🚨 매우 중요: 백엔드와 세션 쿠키(JSESSIONID)를 공유하기 위한 핵심 설정
  withCredentials: true, 
  
  headers: {
    'Content-Type': 'application/json',
  }
});

// 3. 만든 인스턴스를 기본으로 내보내기
export default instance;