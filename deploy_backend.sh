#!/bin/bash

# Baskin Robbins 31 - 백엔드 배포 및 systemd 자동 실행 설정
set -euo pipefail

APP_NAME="kiosk-backend"
DEPLOY_DIR="/home/ubuntu/kiosk-deploy"
TARGET_DIR="${DEPLOY_DIR}/kiosk/target"
CURRENT_JAR="${DEPLOY_DIR}/kiosk-app.jar"
ENV_DIR="/etc/kiosk"
ENV_FILE="${ENV_DIR}/kiosk.env"
SERVICE_FILE="/etc/systemd/system/${APP_NAME}.service"
JOURNAL_CONFIG_DIR="/etc/systemd/journald.conf.d"
JOURNAL_CONFIG_FILE="${JOURNAL_CONFIG_DIR}/kiosk-storage-limit.conf"
RDS_TRUSTSTORE="${ENV_DIR}/rds-truststore.jks"
RDS_TRUSTSTORE_PASSWORD="kiosk-rds-ca-0807"
RDS_CA_BUNDLE_URL="https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem"

echo "=========================================="
echo "백엔드 배포와 자동 실행 설정을 시작합니다."
echo "=========================================="

# 서버를 멈추기 전에 필수 Secret이 모두 전달됐는지 먼저 검사합니다.
for required_name in RDS_ENDPOINT RDS_USERNAME RDS_PASSWORD JWT_SECRET TOSS_SECRET_KEY TURNSTILE_SECRET_KEY; do
    if [ -z "${!required_name:-}" ]; then
        echo "ERROR: ${required_name} environment variable is not set."
        exit 1
    fi
done

if ! command -v java >/dev/null 2>&1; then
    echo "[Step 1] Java 21을 설치합니다."
    sudo apt-get update
    sudo apt-get install -y openjdk-21-jre-headless
fi

if ! command -v curl >/dev/null 2>&1; then
    echo "[Step 1-1] RDS 인증서 다운로드에 필요한 curl을 설치합니다."
    sudo apt-get update
    sudo apt-get install -y curl ca-certificates
fi

# VERIFY_IDENTITY가 AWS RDS 인증서를 검증할 수 있도록 공식 Root CA를 JKS에 등록합니다.
# 이 작업은 기존 서비스를 중지하기 전에 끝내므로 인증서 설치 실패 시 운영 서버는 계속 유지됩니다.
echo "[Step 1-2] AWS 공식 RDS CA truststore를 준비합니다."
CERT_WORK_DIR=$(mktemp -d /tmp/kiosk-rds-ca.XXXXXX)
case "$CERT_WORK_DIR" in
    /tmp/kiosk-rds-ca.*) ;;
    *)
        echo "ERROR: 임시 인증서 폴더 경로가 안전하지 않습니다."
        exit 1
        ;;
esac
TEMP_TRUSTSTORE=$(mktemp)
rm -f "$TEMP_TRUSTSTORE"
trap 'rm -rf "$CERT_WORK_DIR"; rm -f "${TEMP_TRUSTSTORE:-}"' EXIT

curl --fail --silent --show-error --location \
    "$RDS_CA_BUNDLE_URL" \
    --output "${CERT_WORK_DIR}/global-bundle.pem"

CERTIFICATE_COUNT=$(grep -c -- '-----BEGIN CERTIFICATE-----' "${CERT_WORK_DIR}/global-bundle.pem")
if [ "$CERTIFICATE_COUNT" -lt 1 ]; then
    echo "ERROR: AWS RDS CA 인증서 번들을 확인할 수 없습니다."
    exit 1
fi

awk -v output_dir="$CERT_WORK_DIR" '
    /-----BEGIN CERTIFICATE-----/ {
        certificate_number++
        certificate_file = sprintf("%s/rds-root-%02d.pem", output_dir, certificate_number)
    }
    certificate_file != "" { print > certificate_file }
    /-----END CERTIFICATE-----/ {
        close(certificate_file)
        certificate_file = ""
    }
' "${CERT_WORK_DIR}/global-bundle.pem"

certificate_index=0
for certificate_file in "${CERT_WORK_DIR}"/rds-root-*.pem; do
    certificate_index=$((certificate_index + 1))
    keytool -importcert \
        -alias "aws-rds-root-${certificate_index}" \
        -file "$certificate_file" \
        -keystore "$TEMP_TRUSTSTORE" \
        -storetype JKS \
        -storepass "$RDS_TRUSTSTORE_PASSWORD" \
        -noprompt >/dev/null
done

# Java(ubuntu)가 truststore 파일 경로를 통과할 수 있게 그룹 실행 권한만 부여합니다.
# kiosk.env 자체는 아래에서 600으로 유지되어 ubuntu가 Secret 내용을 읽을 수 없습니다.
sudo install -d -m 710 -o root -g ubuntu "$ENV_DIR"
sudo install -m 640 -o root -g ubuntu "$TEMP_TRUSTSTORE" "$RDS_TRUSTSTORE"

# SCP가 전송한 가장 최신 실행 JAR를 찾습니다.
JAR_NAME=$(find "$TARGET_DIR" -maxdepth 1 -type f -name 'kiosk-*.jar' ! -name '*.original' -printf '%T@ %p\n' \
    | sort -n | tail -n 1 | cut -d' ' -f2-)

if [ -z "$JAR_NAME" ] || [ ! -s "$JAR_NAME" ]; then
    echo "ERROR: 실행할 Spring Boot JAR를 찾을 수 없습니다."
    exit 1
fi

echo "[Step 2] 실행 대상 JAR: $JAR_NAME"
ln -sfn "$JAR_NAME" "$CURRENT_JAR"
mkdir -p "${DEPLOY_DIR}/data/flavors"

# systemd EnvironmentFile 문법에 맞게 역슬래시와 큰따옴표를 보호합니다.
escape_env_value() {
    local value="$1"
    value="${value//\\/\\\\}"
    value="${value//\"/\\\"}"
    printf '%s' "$value"
}

echo "[Step 3] 재부팅 후에도 사용할 비밀 환경파일을 갱신합니다."
TEMP_ENV=$(mktemp)
trap 'rm -rf "$CERT_WORK_DIR"; rm -f "$TEMP_TRUSTSTORE" "$TEMP_ENV"' EXIT
chmod 600 "$TEMP_ENV"
{
    printf 'RDS_ENDPOINT="%s"\n' "$(escape_env_value "$RDS_ENDPOINT")"
    printf 'RDS_USERNAME="%s"\n' "$(escape_env_value "$RDS_USERNAME")"
    printf 'RDS_PASSWORD="%s"\n' "$(escape_env_value "$RDS_PASSWORD")"
    printf 'JWT_SECRET="%s"\n' "$(escape_env_value "$JWT_SECRET")"
    printf 'TOSS_SECRET_KEY="%s"\n' "$(escape_env_value "$TOSS_SECRET_KEY")"
    printf 'TURNSTILE_SECRET_KEY="%s"\n' "$(escape_env_value "$TURNSTILE_SECRET_KEY")"
    printf 'FLAVOR_UPLOAD_DIR="%s"\n' "${DEPLOY_DIR}/data/flavors"
} > "$TEMP_ENV"

sudo install -d -m 710 -o root -g ubuntu "$ENV_DIR"
sudo install -m 600 -o root -g root "$TEMP_ENV" "$ENV_FILE"

echo "[Step 4] systemd 서비스 파일을 설치합니다."
TEMP_SERVICE=$(mktemp)
trap 'rm -rf "$CERT_WORK_DIR"; rm -f "$TEMP_TRUSTSTORE" "$TEMP_ENV" "$TEMP_SERVICE"' EXIT
cat > "$TEMP_SERVICE" <<EOF
[Unit]
Description=Baskin Robbins Kiosk Spring Boot Backend
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=ubuntu
Group=ubuntu
WorkingDirectory=${DEPLOY_DIR}
EnvironmentFile=${ENV_FILE}
ExecStart=/usr/bin/java -Xms128m -Xmx384m -Djavax.net.ssl.trustStore=${RDS_TRUSTSTORE} -Djavax.net.ssl.trustStorePassword=${RDS_TRUSTSTORE_PASSWORD} -jar ${CURRENT_JAR}
SuccessExitStatus=143
Restart=on-failure
RestartSec=5
StandardOutput=journal
StandardError=journal
SyslogIdentifier=${APP_NAME}

[Install]
WantedBy=multi-user.target
EOF
sudo install -m 644 -o root -g root "$TEMP_SERVICE" "$SERVICE_FILE"

echo "[Step 5] systemd 로그가 디스크를 가득 채우지 않도록 제한합니다."
TEMP_JOURNAL_CONFIG=$(mktemp)
trap 'rm -rf "$CERT_WORK_DIR"; rm -f "$TEMP_TRUSTSTORE" "$TEMP_ENV" "$TEMP_SERVICE" "$TEMP_JOURNAL_CONFIG"' EXIT
cat > "$TEMP_JOURNAL_CONFIG" <<'EOF'
[Journal]
SystemMaxUse=100M
RuntimeMaxUse=50M
EOF
sudo install -d -m 755 -o root -g root "$JOURNAL_CONFIG_DIR"
sudo install -m 644 -o root -g root "$TEMP_JOURNAL_CONFIG" "$JOURNAL_CONFIG_FILE"
sudo systemctl restart systemd-journald

echo "[Step 6] 기존 nohup 프로세스를 정리하고 systemd로 실행합니다."
sudo systemctl daemon-reload
sudo systemctl stop "$APP_NAME" 2>/dev/null || true

# systemd 전환 전 방식으로 실행된 Java 프로세스만 종료합니다.
LEGACY_PIDS=$(pgrep -f "${TARGET_DIR}/kiosk-.*\\.jar" || true)
if [ -n "$LEGACY_PIDS" ]; then
    kill -15 $LEGACY_PIDS || true
    sleep 10
    LEGACY_PIDS=$(pgrep -f "${TARGET_DIR}/kiosk-.*\\.jar" || true)
    if [ -n "$LEGACY_PIDS" ]; then
        kill -9 $LEGACY_PIDS || true
    fi
fi

sudo systemctl enable "$APP_NAME"
sudo systemctl restart "$APP_NAME"
# Spring 초기화가 끝나기 전에 성공으로 오판하지 않도록 최대 60초 동안 포트를 확인합니다.
for attempt in $(seq 1 30); do
    if ! sudo systemctl is-active --quiet "$APP_NAME"; then
        break
    fi
    if (echo > /dev/tcp/127.0.0.1/8889) >/dev/null 2>&1; then
        break
    fi
    sleep 2
done

if ! sudo systemctl is-active --quiet "$APP_NAME" \
        || ! (echo > /dev/tcp/127.0.0.1/8889) >/dev/null 2>&1; then
    echo "ERROR: 백엔드 서비스가 정상적으로 시작되지 않았습니다."
    sudo journalctl -u "$APP_NAME" -n 50 --no-pager
    exit 1
fi

echo "=========================================="
echo "백엔드가 systemd 서비스로 정상 실행되었습니다."
echo "상태 확인: sudo systemctl status ${APP_NAME}"
echo "=========================================="
