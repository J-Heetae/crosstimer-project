# Crosstimer 🔴🟢
서울시 교차로 기반 보행자 신호등 잔여 시간 안내 서비스
<br><br><br>

## 서비스 및 프로젝트 소개
Crosstimer는 서울교통빅데이터플랫폼의 신호 잔여정보 서비스 API를 활용하여, 횡단보도 앞에서 대기 중인 사용자가 남은 신호 시간을 실시간으로 확인할 수 있도록 돕는 서비스입니다.
지도 기반 UI와 함께 잔여 시간을 시각적으로 안내하여 보행자의 불편함을 개선하는게 목적입니다.

## 주요 기능
- 지도 기반 위치 탐색 : 사용자의 위치 또는 지도 이동에 따라 주변 교차로를 자동 탐색
- 신호등 잔여 시간 실시간 표시 : 선택된 교차로의 보행자 신호 잔여 시간을 초 단위로 안내
- API 호출 최적화 및 캐싱 처리 : Redis 캐시와 Redisson 분산 락을 통해 외부 API 호출 수 절감 및 속도 개선
- 신호 변경 5초 전 알림 기능 (예정) : 사용자에게 신호 변경 5초 전 알림 제공
<br><br><br>

## 아키텍처
<img width="800" height="765" alt="Image" src="https://github.com/user-attachments/assets/3a4cb479-1094-4cb6-af60-754ea78e22aa" />
<br><br><br>

## 이슈 정리
- [외부 API 호출 하루 1,000회 제한, 중복 호출을 막아 API 효율 최적화](https://www.notion.so/API-1-000-API-23146845a91c80eaa00ee41235e4891d?source=copy_link)
- [조회 성능 80% 개선, 페이징과 인덱스 도입](https://www.notion.so/80-23646845a91c8083a1e6f1329ff15521?source=copy_link)
- [무중단 배포 환경 만들기](https://www.notion.so/24746845a91c80dcbab3c38255654c7f?source=copy_link)
- [Nginx에 HTTPS 적용하기](https://www.notion.so/Nginx-https-24246845a91c80e6a042dbffa51d0182?source=copy_link)
<br><br><br>


## 커밋 컨벤션
협업을 위한 git 커밋 컨벤션을 다음 기준에 따라 진행하였습니다.

| 태그         | 설명                                                 |
| ---------- | -------------------------------------------------- |
| `feat`     | 새로운 기능 추가 (예: 신호 잔여 시간 표시 기능 구현)                 |
| `chore`    | 🔧 기타 변경사항 (예: 로그 제거, 설정값 정리 등)                    |
| `docs`     | 📝 문서 수정 (예: README, API 명세 추가/수정)                 |
| `refactor` | ♻️ 리팩토링 (동작 변경 없이 구조 개선)                           |
| `test`     | ✅ 테스트 코드 추가 또는 수정 (예: JUnit 테스트 작성)                |
