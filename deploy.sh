#!/bin/bash

# ==========================================
# Baskin Robbins 31 - 배포 스크립트
# ==========================================

echo "=========================================="
echo "🚀 배포를 시작합니다..."
echo "=========================================="

# ------------------------------------------
# 0. 필수 패키지 점검 (lsof)
# ------------------------------------------
echo "[Step 0] 필수 패키지(lsof) 설치 점검"
if ! command -v lsof &> /dev/null
then
    echo " > lsof가 존재하지 않아 설치를 진행합니다."
    sudo apt-get update > /dev/null 2>&1
    sudo apt-get install lsof -y > /dev/null 2>&1
    echo " > lsof 설치 완료."
else
    echo " > lsof가 이미 설치되어 있습니다."
fi

# ------------------------------------------
# 1. 프론트엔드 배포 (Nginx 서빙 폴더 교체)
# ------------------------------------------
echo "[Step 1] 프론트엔드 정적 파일(Vue/Vite) 배포"
sudo rm -rf /home/ubuntu/kiosk-frontend/dist
sudo mkdir -p /home/ubuntu/kiosk-frontend
sudo cp -r /home/ubuntu/kiosk-deploy/kiosk-frontend/dist /home/ubuntu/kiosk-frontend/
echo " > 프론트엔드 파일 교체 완료."

# ------------------------------------------
# 2. 구동 중인 백엔드(Spring Boot) 종료
# ------------------------------------------
echo "[Step 2] 실행 중인 구버전 백엔드 종료"
CURRENT_PID=$(sudo lsof -t -i:8889)

if [ -z "$CURRENT_PID" ]; then
    echo " > 구동 중인 백엔드 애플리케이션이 없습니다."
else
    echo " > 구동 중인 백엔드(PID: $CURRENT_PID) 종료 중..."
    sudo kill -9 $CURRENT_PID
    sleep 5
    echo " > 기존 백엔드 종료 완료."
fi

# ------------------------------------------
# 3. 새로운 백엔드(Spring Boot) 백그라운드 실행
# ------------------------------------------
echo "[Step 3] 최신 버전 백엔드 애플리케이션 실행"

# 가장 최근에 빌드된 JAR 파일 찾기
JAR_NAME=$(ls -tr /home/ubuntu/kiosk-deploy/kiosk/target/kiosk-*.jar | tail -n 1)
echo " > 실행 대상 파일: $JAR_NAME"

# 로컬 application.properties 대신 EC2 내부 Docker 포트로 강제 연결 (옵션 위치 주의: -jar 앞)
DB_URL="jdbc:mysql://127.0.0.1:3308/kiosk?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useUnicode=true&useSSL=false&allowPublicKeyRetrieval=true"

nohup java -Dspring.datasource.url="$DB_URL" -jar $JAR_NAME > /home/ubuntu/kiosk-deploy/nohup.out 2>&1 &

echo "=========================================="
echo "🎉 배포가 성공적으로 완료되었습니다!"
echo "=========================================="
