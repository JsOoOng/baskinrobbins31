#!/bin/bash

echo "> 1. 프론트엔드 파일 배포 (Nginx 서빙 경로로 이동)"
# 기존 dist 폴더를 지우고 새로 전송받은 dist 폴더로 덮어씁니다.
sudo rm -rf /home/ubuntu/kiosk-frontend/dist
sudo mkdir -p /home/ubuntu/kiosk-frontend
sudo cp -r /home/ubuntu/kiosk-deploy/kiosk-frontend/dist /home/ubuntu/kiosk-frontend/

echo "> 2. 현재 구동중인 백엔드 프로세스 (8889 포트) 확인"
CURRENT_PID=$(lsof -t -i:8889)

if [ -z "$CURRENT_PID" ]; then
    echo "> 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> 실행 중인 백엔드 종료: kill -9 $CURRENT_PID"
    kill -9 $CURRENT_PID
    sleep 5
fi

echo "> 3. 새 백엔드 애플리케이션 백그라운드 배포"
# 전송된 최신 jar 파일을 찾습니다.
JAR_NAME=$(ls -tr /home/ubuntu/kiosk-deploy/kiosk/target/kiosk-*.jar | tail -n 1)

echo "> 실행할 JAR 파일: $JAR_NAME"

# 로컬 application.properties를 수정하지 않고도, 배포 시점에만 데이터베이스 URL을 EC2 내부 Docker 포트(3308)로 덮어쓰며 실행합니다.
nohup java -jar \
    -Dspring.datasource.url="jdbc:mysql://172.16.15.83:3308/kiosk?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useUnicode=true&useSSL=false&allowPublicKeyRetrieval=true" \
    $JAR_NAME > /home/ubuntu/kiosk-deploy/nohup.out 2>&1 &

echo "> 배포가 완료되었습니다!"
