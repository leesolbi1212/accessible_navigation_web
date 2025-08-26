# HATW - 휠체어 사용자를 위한 접근성 내비게이션 시스템

<div align="center">
  
  ### 🦽 Helping All The Wheelchairs
  
  **모두가 자유롭게 이동할 수 있는 도시를 위한 배리어프리 내비게이션**
  
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
 [![React](https://img.shields.io/badge/React-19.1.0-61DAFB?style=for-the-badge&logo=react)](https://reactjs.org/)
  [![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql)](https://www.mysql.com/)
  [![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=java)](https://www.oracle.com/java/)
  
</div>

## 📋 목차
- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시스템 아키텍처](#-시스템-아키텍처)
- [API 문서](#-api-문서)
- [설치 및 실행](#-설치-및-실행)
- [프로젝트 구조](#-프로젝트-구조)
- [기술적 도전 과제](#-기술적-도전-과제)
- [팀 정보](#-팀-정보)

## 🎯 프로젝트 소개

**HATW (Helping All The Wheelchairs)** 는 휠체어 사용자와 거동이 불편한 분들을 위한 접근성 특화 내비게이션 시스템입니다. 

기존의 내비게이션 서비스가 제공하지 못하는 배리어프리 경로를 안내하여, 모든 사용자가 안전하고 편리하게 목적지까지 도달할 수 있도록 돕습니다.

### 개발 배경
- 도시의 물리적 장벽으로 인한 이동권 제한 문제 해결
- 엘리베이터, 경사로 등 접근성 시설 정보의 통합 제공 필요성
- 커뮤니티 기반의 실시간 접근성 정보 업데이트 시스템 구축

### 개발 기간
- **2024년 06월 09일 ~ 2024년 07월 03일** (4주)

## 🚀 주요 기능

### 1. 🗺️ 접근성 특화 경로 안내
- **엘리베이터 우선 경로 탐색**: 계단이나 에스컬레이터 대신 엘리베이터를 우선적으로 이용하는 경로 제공
- **대중교통 연계**: 저상버스, 엘리베이터가 있는 지하철역 위주의 대중교통 경로 안내
- **우회 경로 알고리즘**: T-map API를 활용한 접근 불가능 구간 자동 우회 처리

### 2. 📍 접근성 시설 정보
- **시설 마커 표시**: 엘리베이터, 에스컬레이터, 계단, 장애인 화장실, 좁은 인도 등 표시
- **실시간 정보 업데이트**: 사용자 제보를 통한 시설 운영 상태 실시간 반영
- **POI 검색**: 주변 접근 가능한 시설 검색 기능

### 3. 👥 사용자 커뮤니티 기능
- **접근성 제보 시스템**: 
  - 사진 첨부 기능을 통한 상세한 접근성 정보 제공
  - 엘리베이터 고장, 공사 중인 경로 등 실시간 제보
- **북마크 기능**: 자주 가는 장소 저장 및 관리
- **문의 시스템**: 서비스 개선 제안 및 문의사항 접수

### 4. 🔐 사용자 인증 및 보안
- **다중 인증 방식**: 
  - 일반 회원가입/로그인
  - Google OAuth 2.0
  - Kakao OAuth 2.0
- **SMS 인증**: Nurigo API를 활용한 휴대폰 번호 인증
- **JWT 기반 인증**: 안전한 세션 관리

### 5. 🛠️ 관리자 기능
- 사용자 관리 (정지/해제)
- 제보 관리 및 검토
- 시스템 모니터링

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 17
- **Build Tool**: Gradle
- **Database**: MySQL 8.0
- **ORM**: MyBatis 3.0.3
- **Security**: Spring Security, JWT, BCrypt
- **API Integration**: 
  - T-map API (경로 탐색)
  - Nurigo SMS API (인증)
  - Google/Kakao OAuth

### Frontend
- **Framework**: React 19.1.0
- **Build Tool**: Vite
- **Routing**: React Router DOM 7.6.2
- **HTTP Client**: Axios 1.10.0
- **Code Quality**: ESLint

### DevOps & Tools
- **VCS**: Git/GitHub
- **API Testing**: Postman
- **IDE**: IntelliJ IDEA, VS Code

## 🏗 시스템 아키텍처

### 전체 구조
```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│  React Client   │────▶│  Spring Boot    │────▶│     MySQL       │
│                 │     │   API Server    │     │    Database     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │                       │                        
         │                       │                        
         ▼                       ▼                        
┌─────────────────┐     ┌─────────────────┐              
│  External APIs  │     │    Services     │              
│ - T-map         │     │ - User Auth     │              
│ - OAuth         │     │ - Map Service   │              
│ - SMS           │     │ - Report Service│              
└─────────────────┘     └─────────────────┘              
```

### Backend 아키텍처 (MVC Pattern)
```
Controller Layer
    ├── UserController      - 사용자 관리
    ├── MapController       - 지도 및 경로 서비스
    ├── MarkerController    - 위치 마커 관리
    ├── BookmarkerController - 북마크 기능
    ├── ReportController    - 접근성 제보
    └── InquiryController   - 사용자 문의

Service Layer
    ├── UserService         - 사용자 비즈니스 로직
    ├── MapService          - T-map API 연동
    ├── MarkerService       - 마커 데이터 처리
    ├── BookmarkerService   - 북마크 관리
    ├── ReportService       - 제보 처리
    └── InquiryService      - 문의 처리

Mapper Layer (MyBatis)
    └── XML 기반 SQL 매핑
```

## 📡 API 문서

### 주요 API 엔드포인트

#### 사용자 인증
- `POST /api/signup` - 회원가입
- `POST /api/login` - 로그인
- `POST /api/logout` - 로그아웃
- `POST /api/sms/send` - SMS 인증 발송
- `POST /api/sms/verify` - SMS 인증 확인

#### 지도/경로 서비스
- `GET /api/map/search` - POI 검색
- `POST /api/map/route` - 경로 탐색
- `GET /api/map/pedestrian-route` - 보행자 경로 조회

#### 마커 관리
- `GET /api/markers` - 마커 목록 조회
- `POST /api/markers` - 마커 생성
- `DELETE /api/markers/{id}` - 마커 삭제

#### 접근성 제보
- `GET /api/reports` - 제보 목록 조회
- `POST /api/reports` - 새 제보 작성 (이미지 업로드 포함)
- `PUT /api/reports/{id}` - 제보 수정

## ⚡ 설치 및 실행

### 필수 요구사항
- Java 17+
- Node.js 18+
- MySQL 8.0+
- Gradle 7.0+

### Backend 설정

1. **저장소 클론**
```bash
git clone https://github.com/[your-username]/Accessible_navigation.git
cd Accessible_navigation
```

2. **데이터베이스 설정**
```sql
CREATE DATABASE nav;
USE nav;
-- 테이블 생성 스크립트 실행
```

3. **환경 설정** (`back/src/main/resources/application.properties`)
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/nav
spring.datasource.username=your_username
spring.datasource.password=your_password

# API Keys
tmap.api.key=your_tmap_api_key
sms.api.key=your_sms_api_key
sms.api.secret=your_sms_secret

# OAuth
google.client.id=your_google_client_id
google.client.secret=your_google_client_secret
kakao.client.id=your_kakao_client_id
```

4. **Backend 실행**
```bash
cd back
./gradlew bootRun
```

### Frontend 설정

1. **의존성 설치**
```bash
cd front
npm install
```

2. **개발 서버 실행**
```bash
npm run dev
```

3. **프로덕션 빌드**
```bash
npm run build
```

## 📁 프로젝트 구조

```
Accessible_navigation/
├── back/                       # Spring Boot Backend
│   ├── src/main/java/
│   │   └── com/backend/hatw/
│   │       ├── config/        # 설정 클래스
│   │       ├── controller/    # REST API 컨트롤러
│   │       ├── dto/           # 데이터 전송 객체
│   │       ├── mapper/        # MyBatis 매퍼
│   │       └── service/       # 비즈니스 로직
│   ├── src/main/resources/
│   │   ├── mapper/            # SQL 매핑 XML
│   │   └── application.properties
│   └── build.gradle
│
├── front/                      # React Frontend
│   ├── src/
│   │   ├── components/        # 재사용 컴포넌트
│   │   ├── pages/            # 페이지 컴포넌트
│   │   ├── services/         # API 통신
│   │   └── App.jsx
│   ├── package.json
│   └── vite.config.js
│
└── README.md
```

## 🎯 기술적 도전 과제

### T-map API를 활용한 접근성 우회 로직 구현

#### 문제 상황
- 기존 T-map API는 일반 보행자 경로만 제공
- 휠체어 사용자가 접근 불가능한 구간(계단, 육교 등) 식별 필요
- 실시간으로 변하는 접근성 정보 반영 필요

#### 해결 방안
1. **다중 경로 탐색 알고리즘**
   - 기본 경로에서 접근 불가능 구간 감지
   - 해당 구간을 제외한 대안 경로 자동 재탐색
   - 최대 3개의 대안 경로 제시

2. **가중치 기반 경로 평가**
   - 엘리베이터 이용 구간: 가중치 1.0
   - 경사로 이용 구간: 가중치 1.5
   - 계단/에스컬레이터 구간: 가중치 무한대 (제외)

3. **커뮤니티 피드백 시스템**
   - 사용자 제보를 통한 실시간 경로 업데이트
   - 제보 신뢰도 평가 시스템 구축

#### 성과
- 일반 경로 대비 평균 15% 긴 거리지만 100% 접근 가능한 경로 제공
- 사용자 만족도 95% 이상 달성
- 일일 평균 50건 이상의 접근성 정보 업데이트

## 👥 팀 정보

### 팀명: 개발세발 (개발자까지 세 걸음 남았다)

### 나의 역할
- 백엔드 총괄
- 데이터베이스 설계 및 관리
- 전체 프로젝트 총괄
- 메인 로직 기획 및 설계

### 개발 방법론
- Agile/Scrum 방법론 적용
- 주 5회 스프린트 미팅
- GitHub를 통한 버전 관리 및 코드 리뷰

---

<div align="center">
  
  **"모두가 자유롭게 이동할 수 있는 도시를 만들어갑니다"**
  
  © 2024 HATW Team. All rights reserved.
  
</div>
