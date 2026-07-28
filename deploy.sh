#!/bin/bash

echo "> 0. lsof 설치 확인"
if ! command -v lsof &> /dev/null
then
    sudo apt-get update && sudo apt-get install lsof -y
fi

echo "> 1. 프론트엔드 파일 배포 (Nginx 서빙 경로로 이동)"
# 기존 dist 폴더 삭제 및 새 dist 폴더 복사/이동
sudo rm -rf /home/ubuntu/kiosk-frontend/dist
sudo mkdir -p /home/ubuntu/kiosk-frontend
sudo cp -r /home/ubuntu/kiosk-deploy/kiosk-frontend/dist /home/ubuntu/kiosk-frontend/

echo "> 2. 현재 구동중인 백엔드 프로세스 (8889 포트) 확인"
CURRENT_PID=$(sudo lsof -t -i:8889)

if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 구동중인 애플리케이션이 없습니다."
else
    echo "> 실행 중인 백엔드 종료: kill -9 $CURRENT_PID"
    sudo kill -9 $CURRENT_PID
    sleep 5
fi

echo "> 3. 새 백엔드 애플리케이션 백그라운드 배포"
# 최신 빌드된 jar 파일 찾기
JAR_NAME=$(ls -tr /home/ubuntu/kiosk-deploy/kiosk/target/kiosk-*.jar | tail -n 1)

echo "> 실행할 JAR 파일: $JAR_NAME"

# D 옵션은 반드시 -jar 앞에 와야 합니다.
nohup java -Dspring.datasource.url="jdbc:mysql://127.0.0.1:3308/kiosk?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useUnicode=true&useSSL=false&allowPublicKeyRetrieval=true" \
    -jar $JAR_NAME > /home/ubuntu/kiosk-deploy/nohup.out 2>&1 &

echo "> 배포가 완료되었습니다!"
