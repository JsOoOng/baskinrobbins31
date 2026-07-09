깃 브런치 전략

**1. @@@ main 브런치는 팀장만 clone  @@@**

**2. main 브런치를 copy한 dev 브런치를 팀장이 생성**

**3. 브런치 이름은 아래와 통일**
     1. main : 완성된 배포 브런치
     2. dev  + n : n차 개발 브런치
     3. feature/사용자_기능 : 팀원별 기능 구현 - 예시) feature/headquarter_login, feature/kiosk_menu 등    
          @ 사용자 분류 @
          1. 키오스크 기능 : Kiosk
          2. 사용자 기능 : Customer
          3. 지점 기능 : Branch
          4. 본사 기능 : Headquarter
     4. fix/사용자_기능 : 버그 수정
     
**4. 개발 혹은 버그 수정 후 commit할 때 summary는 아래의 예시 방식으로 정의**
     1. feat: 키오스크 상품 목록 조회 기능 추가
     2. feat: 주문 생성 API 구현
     3. fix: 로그인 실패 횟수 증가 오류 수정
     4. security: SQL Injection 방어 처리 추가
     5. docs: 요구사항 명세서 작성
     6. deploy: Docker 배포 설정 추가
