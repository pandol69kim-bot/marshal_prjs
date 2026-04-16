# ---- Stage 1: Build ----
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

# Vite 빌드 타임 환경변수 (번들에 정적 삽입)
ARG VITE_API_BASE_URL=""
ARG VITE_APP_NAME="API Service"
ARG VITE_OAUTH2_GOOGLE_URL="/oauth2/authorization/google"
ARG VITE_OAUTH2_KAKAO_URL="/oauth2/authorization/kakao"

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_APP_NAME=$VITE_APP_NAME
ENV VITE_OAUTH2_GOOGLE_URL=$VITE_OAUTH2_GOOGLE_URL
ENV VITE_OAUTH2_KAKAO_URL=$VITE_OAUTH2_KAKAO_URL

RUN npm run build

# ---- Stage 2: Serve ----
FROM nginx:1.25-alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
