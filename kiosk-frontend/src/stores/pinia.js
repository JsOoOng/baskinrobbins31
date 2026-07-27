/**
 * [모듈 흐름 안내] pinia
 * 역할: 모든 Vue 화면이 공유할 Pinia 인스턴스를 한 번 생성해 내보낸다.
 * 호출 흐름: main.js -> 이 Pinia 인스턴스 등록 -> 각 화면과 라우터 가드에서 store 사용
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import { createPinia } from 'pinia'

const pinia = createPinia()

export default pinia