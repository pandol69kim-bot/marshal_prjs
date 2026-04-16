# AWS EC2 배포 절차

> 현재 프로젝트 스택: Spring Boot 백엔드 + Vue.js 프론트엔드 + PostgreSQL + Redis + MinIO
> 모두 Docker Compose로 단일 EC2 인스턴스에 배포하는 절차입니다.

---

## 목차

1. [AWS 사전 준비](#1-aws-사전-준비)
2. [EC2 인스턴스 생성](#2-ec2-인스턴스-생성)
3. [보안 그룹 설정](#3-보안-그룹-설정)
4. [서버 초기 설정](#4-서버-초기-설정)
5. [Docker 설치](#5-docker-설치)
6. [프로젝트 배포](#6-프로젝트-배포)
7. [환경변수 설정](#7-환경변수-설정)
8. [docker-compose.yml 프로덕션 수정](#8-docker-composeyml-프로덕션-수정)
9. [서비스 기동](#9-서비스-기동)
10. [HTTPS 설정 (Let's Encrypt)](#10-https-설정-lets-encrypt)
11. [OAuth2 리다이렉트 URI 업데이트](#11-oauth2-리다이렉트-uri-업데이트)
12. [도메인 연결](#12-도메인-연결)
13. [운영 관리](#13-운영-관리)

---

## 1. AWS 사전 준비

### 1-1. AWS 계정 및 IAM

1. [AWS Console](https://console.aws.amazon.com) 로그인
2. IAM → 사용자 → 배포용 사용자 생성 (루트 계정 직접 사용 금지)
3. 권한: `AmazonEC2FullAccess`, `AmazonRoute53FullAccess` (도메인 사용 시)

### 1-2. 키 페어 생성

```
EC2 콘솔 → 네트워크 및 보안 → 키 페어 → 키 페어 생성
- 이름: my-app-key
- 유형: RSA
- 형식: .pem (Linux/Mac) 또는 .ppk (Windows PuTTY)
```

> ⚠️ 다운로드된 `.pem` 파일은 분실 시 재발급 불가. 안전한 곳에 보관.

---

## 2. EC2 인스턴스 생성

### 2-1. 권장 사양

| 항목 | 권장 | 최소 |
|------|------|------|
| 인스턴스 유형 | **t3.medium** (2vCPU, 4GB) | t3.small (2vCPU, 2GB) |
| OS | **Ubuntu 22.04 LTS** | Ubuntu 20.04 LTS |
| 스토리지 | **30GB** gp3 | 20GB |
| 리전 | ap-northeast-2 (서울) | - |

> Spring Boot + PostgreSQL + Redis + MinIO 동시 실행 시 t3.small은 메모리 부족 위험.
> 트래픽이 많으면 t3.large 이상 권장.

### 2-2. 인스턴스 생성 절차

```
1. EC2 → 인스턴스 → 인스턴스 시작

2. 이름 및 태그
   이름: my-app-server

3. AMI 선택
   Ubuntu Server 22.04 LTS (HVM), SSD Volume Type
   아키텍처: 64비트(x86)

4. 인스턴스 유형
   t3.medium

5. 키 페어
   위에서 생성한 my-app-key 선택

6. 네트워크 설정
   VPC: 기본 VPC
   퍼블릭 IP 자동 할당: 활성화
   보안 그룹: 새 보안 그룹 생성 (3번에서 설정)

7. 스토리지
   30 GiB gp3

8. 인스턴스 시작
```

### 2-3. Elastic IP 할당 (필수)

재시작 시 IP가 바뀌는 것을 방지합니다.

```
EC2 → 네트워크 및 보안 → 탄력적 IP
→ 탄력적 IP 주소 할당 → 할당
→ 할당된 IP 선택 → 탄력적 IP 주소 연결 → 인스턴스 선택
```

---

## 3. 보안 그룹 설정

EC2 인스턴스의 보안 그룹에서 아래 인바운드 규칙을 추가합니다.

| 유형 | 프로토콜 | 포트 | 소스 | 용도 |
|------|---------|------|------|------|
| SSH | TCP | 22 | 내 IP | 서버 접속 |
| HTTP | TCP | 80 | 0.0.0.0/0 | 웹 서비스 |
| HTTPS | TCP | 443 | 0.0.0.0/0 | 웹 서비스 (SSL) |

> ⚠️ PostgreSQL(5432), Redis(6379), MinIO(9000/9001)는 외부에 열지 않습니다.
> 모두 Docker 내부 네트워크로만 통신합니다.

```
EC2 → 보안 그룹 → 인바운드 규칙 편집 → 규칙 추가
```

---

## 4. 서버 초기 설정

### 4-1. SSH 접속

**Mac/Linux:**
```bash
chmod 400 ~/Downloads/my-app-key.pem
ssh -i ~/Downloads/my-app-key.pem ubuntu@<EC2-PUBLIC-IP>
```

**Windows (PowerShell):**
```powershell
ssh -i C:\Users\사용자\Downloads\my-app-key.pem ubuntu@<EC2-PUBLIC-IP>
```

### 4-2. 시스템 업데이트

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y git curl wget unzip
```

### 4-3. 스왑 메모리 설정 (t3.small 사용 시 필수)

```bash
# 2GB 스왑 생성
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# 재부팅 후에도 유지
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# 확인
free -h
```

---

## 5. Docker 설치

### 5-1. Docker Engine 설치

```bash
# 의존성 설치
sudo apt install -y ca-certificates curl gnupg lsb-release

# Docker 공식 GPG 키 추가
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
  sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# 저장소 추가
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Docker 설치
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# ubuntu 유저가 sudo 없이 docker 사용
sudo usermod -aG docker ubuntu

# 변경사항 적용 (재로그인 또는 아래 명령)
newgrp docker

# 설치 확인
docker --version
docker compose version
```

---

## 6. 프로젝트 배포

### 방법 A: Git 사용 (권장)

```bash
# 서버에서
cd /home/ubuntu
git clone https://github.com/<사용자명>/<레포명>.git app
cd app
```

### 방법 B: 로컬에서 SCP로 직접 전송

```bash
# 로컬 PC에서 실행
scp -i ~/Downloads/my-app-key.pem -r \
  D:/Marchall_CLAUDE/m_prj1/output/project \
  ubuntu@<EC2-PUBLIC-IP>:/home/ubuntu/app
```

### 방법 C: rsync (변경사항만 전송, 이후 배포 시 유용)

```bash
# 로컬 PC에서 실행
rsync -avz --progress \
  -e "ssh -i ~/Downloads/my-app-key.pem" \
  --exclude='.git' \
  --exclude='node_modules' \
  --exclude='target' \
  D:/Marchall_CLAUDE/m_prj1/output/project/ \
  ubuntu@<EC2-PUBLIC-IP>:/home/ubuntu/app/
```

---

## 7. 환경변수 설정

서버에서 `.env` 파일을 생성합니다.

```bash
cd /home/ubuntu/app
cp .env .env.backup   # 기존 파일 백업 (있는 경우)
nano .env
```

`.env` 파일 내용:

```bash
# ── 필수: 반드시 실제 값으로 교체 ──────────────────────

# JWT (32자 이상의 랜덤 문자열)
JWT_SECRET=your-super-secret-key-minimum-32-characters-long-change-this

# Google OAuth2
GOOGLE_CLIENT_ID=123456789-xxxx.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-xxxxxxxxxxxxxxxxxx

# Kakao OAuth2
KAKAO_CLIENT_ID=abc123def456
KAKAO_CLIENT_SECRET=kakao_secret_here

# ── 선택: 이메일/SMS 발송이 필요한 경우 ────────────────
SENDGRID_API_KEY=SG.xxxxxxxxxxxxxxxxxxxx
SENDGRID_FROM_EMAIL=noreply@yourdomain.com

NCP_SMS_ACCESS_KEY=
NCP_SMS_SECRET_KEY=
NCP_SMS_SERVICE_ID=
NCP_SMS_SENDER_PHONE=
```

> **JWT_SECRET 랜덤 생성 방법:**
> ```bash
> openssl rand -base64 48
> ```

---

## 8. docker-compose.yml 프로덕션 수정

`docker-compose.yml`에서 도메인과 보안 설정을 실제 값으로 변경합니다.

```bash
nano /home/ubuntu/app/docker-compose.yml
```

### 8-1. 도메인 관련 환경변수 수정

```yaml
backend:
  environment:
    # 실제 도메인으로 변경 (HTTPS 설정 후)
    ALLOWED_ORIGINS: https://yourdomain.com
    OAUTH2_REDIRECT_URI: https://yourdomain.com/oauth2/callback

    # HTTP만 사용하는 경우 (초기 배포 시)
    # ALLOWED_ORIGINS: http://<EC2-PUBLIC-IP>
    # OAUTH2_REDIRECT_URI: http://<EC2-PUBLIC-IP>/oauth2/callback
```

### 8-2. DB 비밀번호 강화 (권장)

```yaml
postgres:
  environment:
    POSTGRES_PASSWORD: StrongPassword123!   # 기본값 postgres에서 변경

backend:
  environment:
    DATABASE_PASSWORD: StrongPassword123!   # postgres와 동일하게
```

### 8-3. 내부 서비스 포트 외부 노출 제거 (보안 강화)

외부에서 직접 접근할 필요가 없는 포트는 제거합니다:

```yaml
postgres:
  # ports:           ← 이 섹션 전체 주석 처리
  #   - "5432:5432"

redis:
  # ports:
  #   - "6379:6379"

minio:
  ports:
    - "9000:9000"    # API (백엔드에서만 사용 → 제거 가능)
    - "9001:9001"    # 콘솔 (필요한 경우만 열기)
```

---

## 9. 서비스 기동

### 9-1. 최초 빌드 및 기동

```bash
cd /home/ubuntu/app

# 빌드 + 백그라운드 실행 (처음에는 10~15분 소요)
docker compose up --build -d

# 실시간 로그 확인
docker compose logs -f
```

### 9-2. 기동 상태 확인

```bash
# 컨테이너 상태
docker compose ps

# 예상 출력:
# NAME                 STATUS
# project-postgres-1   Up (healthy)
# project-redis-1      Up (healthy)
# project-backend-1    Up
# project-frontend-1   Up
# project-minio-1      Up
```

### 9-3. 서비스 접속 확인

```bash
# 헬스체크
curl http://localhost/health               # nginx 헬스체크
curl http://localhost:8080/actuator/health # Spring Boot 헬스체크

# 외부에서 브라우저로 접속
# http://<EC2-PUBLIC-IP>
```

### 9-4. 자동 재시작 설정

모든 서비스에 `restart: unless-stopped`가 설정되어 있으므로
서버 재부팅 시 Docker 서비스만 자동 시작되면 자동 재시작됩니다.

```bash
# Docker 자동 시작 활성화 (대부분 기본 활성화됨)
sudo systemctl enable docker
sudo systemctl status docker
```

---

## 10. HTTPS 설정 (Let's Encrypt)

> 도메인이 있어야 합니다. IP만으로는 Let's Encrypt 인증서 발급 불가.

### 10-1. Certbot 설치

```bash
sudo apt install -y certbot
```

### 10-2. 인증서 발급 (standalone 방식 — 포트 80 임시 해제)

```bash
# 프론트엔드 컨테이너 일시 중지 (포트 80 해제)
docker compose stop frontend

# 인증서 발급
sudo certbot certonly --standalone -d yourdomain.com -d www.yourdomain.com

# 발급된 인증서 위치
# /etc/letsencrypt/live/yourdomain.com/fullchain.pem
# /etc/letsencrypt/live/yourdomain.com/privkey.pem
```

### 10-3. nginx.conf 수정 (SSL 적용)

```bash
nano /home/ubuntu/app/frontend/nginx.conf
```

기존 `server { listen 80; ... }` 를 아래로 교체:

```nginx
# HTTP → HTTPS 리다이렉트
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    return 301 https://$host$request_uri;
}

# HTTPS 서버
server {
    listen 443 ssl;
    server_name yourdomain.com www.yourdomain.com;

    ssl_certificate     /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;

    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://backend:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 60s;
    }

    location /oauth2/authorization {
        proxy_pass http://backend:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /login/oauth2 {
        proxy_pass http://backend:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    location /health {
        return 200 "ok";
        add_header Content-Type text/plain;
    }
}
```

### 10-4. docker-compose.yml에 443 포트 추가 및 인증서 마운트

```yaml
frontend:
  ports:
    - "80:80"
    - "443:443"       # 추가
  volumes:
    - /etc/letsencrypt:/etc/letsencrypt:ro   # 인증서 마운트 (읽기전용)
```

### 10-5. 프론트엔드 재빌드 및 재시작

```bash
cd /home/ubuntu/app
docker compose up --build frontend -d
```

### 10-6. 인증서 자동 갱신 (90일마다 만료)

```bash
# 갱신 테스트
sudo certbot renew --dry-run

# crontab에 자동 갱신 등록 (매월 1일 새벽 3시)
sudo crontab -e
# 아래 줄 추가:
0 3 1 * * certbot renew --quiet && docker compose -f /home/ubuntu/app/docker-compose.yml restart frontend
```

---

## 11. OAuth2 리다이렉트 URI 업데이트

### 11-1. Google Cloud Console

```
1. https://console.cloud.google.com 접속
2. API 및 서비스 → 사용자 인증 정보
3. OAuth 2.0 클라이언트 ID 선택
4. 승인된 JavaScript 원본에 추가:
   https://yourdomain.com
5. 승인된 리디렉션 URI에 추가:
   https://yourdomain.com/login/oauth2/code/google
6. 저장
```

### 11-2. Kakao Developers

```
1. https://developers.kakao.com 접속
2. 내 애플리케이션 → 앱 선택
3. 카카오 로그인 → Redirect URI 추가:
   https://yourdomain.com/login/oauth2/code/kakao
4. 플랫폼 → Web → 사이트 도메인 추가:
   https://yourdomain.com
5. 저장
```

### 11-3. docker-compose.yml 환경변수 업데이트

```yaml
backend:
  environment:
    ALLOWED_ORIGINS: https://yourdomain.com
    OAUTH2_REDIRECT_URI: https://yourdomain.com/oauth2/callback
```

```bash
# 변경 후 백엔드 재시작 (재빌드 필요 없음 — 환경변수만 변경)
docker compose up -d --no-build backend
```

---

## 12. 도메인 연결

### 12-1. Route 53 사용 (AWS 도메인)

```
1. Route 53 → 호스팅 영역 → 생성
2. 도메인 이름 입력 → 생성
3. A 레코드 추가:
   이름: @ (루트 도메인)
   값: <EC2 Elastic IP>
4. CNAME 추가 (www 서브도메인):
   이름: www
   값: yourdomain.com
```

### 12-2. 외부 도메인 (가비아, 카페24 등)

```
도메인 관리 페이지에서 DNS 레코드 추가:

A 레코드:
  호스트: @
  값: <EC2 Elastic IP>

A 레코드:
  호스트: www
  값: <EC2 Elastic IP>
```

> DNS 전파에 최대 24~48시간 소요될 수 있습니다.

---

## 13. 운영 관리

### 13-1. 로그 확인

```bash
# 전체 서비스 로그 (실시간)
docker compose logs -f

# 특정 서비스 로그
docker compose logs -f backend
docker compose logs -f frontend

# 최근 100줄
docker compose logs --tail=100 backend

# 에러만 필터링
docker compose logs backend 2>&1 | grep -i error
```

### 13-2. 코드 업데이트 배포

```bash
cd /home/ubuntu/app

# Git 사용 시
git pull origin main

# 전체 재빌드 (코드 변경 시)
docker compose up --build -d

# 특정 서비스만 재빌드
docker compose up --build backend -d
docker compose up --build frontend -d

# 재빌드 없이 재시작 (환경변수 변경 시)
docker compose up -d --no-build
```

### 13-3. 서비스 관리

```bash
# 모든 서비스 중지
docker compose stop

# 모든 서비스 시작
docker compose start

# 특정 서비스 재시작
docker compose restart backend

# 완전 삭제 (볼륨 유지)
docker compose down

# 볼륨까지 삭제 (DB 데이터 삭제됨 — 주의!)
docker compose down -v
```

### 13-4. DB 백업

```bash
# PostgreSQL 백업
docker compose exec postgres pg_dump -U postgres appdb > \
  backup_$(date +%Y%m%d_%H%M%S).sql

# 복원
docker compose exec -T postgres psql -U postgres appdb < backup_20260416.sql
```

### 13-5. 디스크/메모리 모니터링

```bash
# 디스크 사용량
df -h

# 메모리 사용량
free -h

# Docker 리소스 사용량
docker stats

# 사용하지 않는 Docker 이미지/컨테이너 정리
docker system prune -f
```

### 13-6. 서버 재부팅 후 확인

```bash
# Docker 서비스 상태
sudo systemctl status docker

# 컨테이너 자동 시작 확인 (1~2분 후)
docker compose ps
```

---

## 배포 체크리스트

### 최초 배포 전

- [ ] EC2 인스턴스 생성 완료
- [ ] Elastic IP 연결 완료
- [ ] 보안 그룹 (22, 80, 443) 설정 완료
- [ ] Docker 설치 완료
- [ ] 프로젝트 파일 전송 완료
- [ ] `.env` 파일 작성 완료 (JWT_SECRET, OAuth2 키)
- [ ] `docker-compose.yml` 도메인/IP 수정 완료

### 서비스 기동 확인

- [ ] `docker compose ps` 모든 서비스 Up 상태
- [ ] `http://<IP>` 접속 → 로그인 페이지 노출
- [ ] 이메일 로그인 테스트
- [ ] 어드민 로그인 (`admin@example.com` / `Admin1234!`) 테스트

### HTTPS 설정 후

- [ ] `https://yourdomain.com` 접속 확인
- [ ] HTTP → HTTPS 리다이렉트 확인
- [ ] Google OAuth2 리다이렉트 URI 업데이트
- [ ] Kakao OAuth2 리다이렉트 URI 업데이트
- [ ] OAuth2 로그인 테스트

---

## 빠른 참조 명령어

```bash
# 배포
docker compose up --build -d

# 로그
docker compose logs -f backend

# 상태
docker compose ps

# 재시작
docker compose restart backend

# 백엔드만 재빌드
docker compose up --build backend -d

# 프론트만 재빌드
docker compose up --build frontend -d

# DB 백업
docker compose exec postgres pg_dump -U postgres appdb > backup.sql

# 정리
docker system prune -f
```
