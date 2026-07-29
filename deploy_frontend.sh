#!/bin/bash

# ==========================================
# Baskin Robbins 31 - 프론트엔드 배포 스크립트 (Nginx 전용)
# ==========================================

echo "=========================================="
echo "🚀 프론트엔드 배포를 시작합니다..."
echo "=========================================="

# 1. Nginx 정적 폴더 교체
echo "[Step 1] 프론트엔드 정적 파일(Vue/Vite) 교체"
# Nginx의 기본 웹 루트 디렉토리 사용 (Ubuntu 기준)
WEB_ROOT="/var/www/html/kiosk-frontend"

sudo rm -rf $WEB_ROOT
sudo mkdir -p $WEB_ROOT
# GitHub Actions에서 SCP로 /home/ubuntu/kiosk-deploy/kiosk-frontend/dist에 파일을 넘겨준다고 가정
sudo cp -r /home/ubuntu/kiosk-deploy/kiosk-frontend/dist/* $WEB_ROOT/
echo " > 프론트엔드 파일 교체 완료."

# 2. Nginx 설정 파일 교체 (repo에 포함된 파일 적용)
echo "[Step 2] Nginx 리버스 프록시 설정 갱신"
sudo cp /home/ubuntu/kiosk-deploy/nginx/default.conf /etc/nginx/sites-available/default
# sites-enabled에 심볼릭 링크가 없다면 생성 (보통 우분투 Nginx 기본 설치시 이미 있음)
if [ ! -L /etc/nginx/sites-enabled/default ]; then
    sudo ln -s /etc/nginx/sites-available/default /etc/nginx/sites-enabled/default
fi

# 3. Nginx 재시작
echo "[Step 3] Nginx 재시작"
sudo systemctl restart nginx
echo " > Nginx 설정 적용 및 재시작 완료."

echo "=========================================="
echo "🎉 프론트엔드 배포가 성공적으로 완료되었습니다!"
echo "=========================================="
