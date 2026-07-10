// src/api/axios.js
import axios from 'axios';

const instance = axios.create({
  // 백엔드 스프링 부트 서버 주소
  baseURL: 'http://localhost:8888', 
  
  // 🚨 매우 중요: 백엔드와 세션 쿠키(JSESSIONID)를 공유하기 위한 핵심 설정
  withCredentials: true, 
  
  headers: {
    'Content-Type': 'application/json',
  }
});

export default instance;