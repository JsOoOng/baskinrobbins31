#!/bin/bash

# ==========================================
# Baskin Robbins 31 - 백엔드 배포 스크립트 (Spring Boot 전용)
# ==========================================

echo "=========================================="
echo "🚀 백엔드 배포를 시작합니다..."
echo "=========================================="

# JWT 비밀키가 없으면 기존 서버를 종료하기 전에 배포를 중단합니다.
# Java 실행 인자로 붙이지 않고 환경변수로 상속하여
# EC2 프로세스 목록에 Secret 값이 노출되지 않게 합니다.
if [ -z "${JWT_SECRET:-}" ]; then
    echo "ERROR: JWT_SECRET environment variable is not set."
    exit 1
fi

# -1. 필수 패키지 점검 (JAVA)
echo "[Step -1] 필수 패키지(JAVA) 설치 점검"
if ! command -v java &> /dev/null
then
    sudo apt update
    sudo apt install openjdk-21-jdk -y
fi

# 0. 필수 패키지 점검 (lsof)
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

# 1. 구동 중인 백엔드(Spring Boot) 종료
echo "[Step 1] 실행 중인 구버전 백엔드 종료"

CURRENT_PID=$(pgrep -f "kiosk-.*\.jar")

if [ -z "$CURRENT_PID" ]; then
    echo " > 실행 중인 백엔드가 없습니다."
else
    echo " > 기존 백엔드 PID: $CURRENT_PID 종료"
    sudo kill -15 $CURRENT_PID

    sleep 10

    if ps -p $CURRENT_PID > /dev/null
    then
        echo " > 정상 종료 실패, 강제 종료"
        sudo kill -9 $CURRENT_PID
    fi

    echo " > 기존 백엔드 종료 완료"
fi


# 2. 새로운 백엔드(Spring Boot) 백그라운드 실행
echo "[Step 2] 최신 버전 백엔드 애플리케이션 실행"

# 가장 최근에 빌드된 JAR 파일 찾기
JAR_NAME=$(ls -tr /home/ubuntu/kiosk-deploy/kiosk/target/kiosk-*.jar | tail -n 1)
echo " > 실행 대상 파일: $JAR_NAME"

# 환경변수로 넘겨받은 RDS 정보를 사용하여 데이터베이스 연결
# GitHub Actions에서 export 형태로 주입받는 것을 상정
if [ -z "$RDS_ENDPOINT" ] || [ -z "$RDS_USERNAME" ] || [ -z "$RDS_PASSWORD" ]; then
    echo " ⚠️ 경고: RDS 관련 환경변수가 설정되지 않았습니다. application.properties의 기본 설정으로 실행될 수 있습니다."
else
    DB_URL="jdbc:mysql://${RDS_ENDPOINT}:3306/kiosk?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useUnicode=true&useSSL=false&allowPublicKeyRetrieval=true"
    DB_ARGS="-Dspring.datasource.url=${DB_URL} -Dspring.datasource.username=${RDS_USERNAME} -Dspring.datasource.password=${RDS_PASSWORD}"
fi

# 쉬운주석: 새 버전을 배포해도 업로드 사진이 사라지지 않도록
# 프로그램 밖의 고정 폴더에 저장하고 /images/flavors/{filename} 주소로 보여준다.
export FLAVOR_UPLOAD_DIR="/home/ubuntu/kiosk-deploy/data/flavors"

mkdir -p /home/ubuntu/kiosk-deploy/logs
mkdir -p /home/ubuntu/kiosk-deploy/data/flavors

nohup java -Xms128m -Xmx384m $DB_ARGS -jar $JAR_NAME > /dev/null 2>&1 &

echo "=========================================="
echo "🎉 백엔드 배포가 성공적으로 완료되었습니다!"
echo "=========================================="
