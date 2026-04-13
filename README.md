# MyMap (마이맵) 
#### 교통편이 망한 직장인을 위한 출퇴근 전용 지도 서비스
 - 2025-03-26 ~ 2025-05-22 (1차, 개발 시작 2025-04-08)
 - 2026-01-27 ~ (2차, 진행중)
 - 개인 프로젝트

## 프로젝트 개발 동기 및 핵심 제공 기능 
저는 역세권에 살지 않으면서 역세권이 아닌 곳으로 매일 출퇴근을 했던, 교통편이 망한 직장인이었습니다.<br>
출퇴근 길은 매일 이동하는 길이기에 길찾기 목적 보다는 대중교통 도착정보 확인을 위해 지도 서비스를 이용하게 됩니다. <br>
그런데 기존의 서비스가 알려주는 것 외에도 실제로 매일 다니다 보면 사용자가 로컬에서 체득하게 되는 정보들이 있습니다. <br>
이것을 활용해 사용자에 최적화된 대중교통 도착정보를 안내하여 전쟁같은 서울의 출퇴근길에서 보다 신속하고 간편한 이용을 위한 서비스를 개발하게 되었습니다. 

<strong>1. 이동 중에 경로가 바뀌는 경우, 기존 유사 서비스에서는 경로 재검색을 해야 했던 불편함 개선</strong>
   - 특정 경로와 상관 없이, 사용자가 정류장을 커스터마이징하여 경로를 생성할 수 있습니다.
   - 사용자가 설정한 정류장 위치를 기반으로 한번에 도착정보를 확인할 클러스터가 형성됩니다. 
   
<strong>2. 기존 유사 서비스에서는 정류장 단위로만 도착정보 확인이 가능했고, 모든 수단 중 내가 탈 수단을 찾아야 했던 불편함 개선</strong>
   - 정류장 단위가 아닌 클러스터 단위로 도착정보를 한번에 확인할 수 있습니다.
   - 예를 들면, 집이라는 클러스터에는 집에서 향할 수 있는 정류장a, 정류장b의 도착정보를 한번에 확인할 수 있게 됩니다. 
   - 버스의 경우, 클러스터 내에서 내가 탈 차의 도착정보만 확인할 수 있습니다.
   - 클러스터 내의 버스 노선들은 정류장 단위로 정렬되어 있습니다. 
  
| 경로 등록 화면 | 도착정보 조회 화면 |
|------|------|
| <img width="148" alt="Image" src="https://github.com/user-attachments/assets/ad3e1e76-4f80-472b-bd3a-6121454eba4c" /> | <img width="148" alt="Image" src="https://github.com/user-attachments/assets/3b901b0d-d9f9-4dfb-96d4-e79039d22b7d" /> |
<br>

- <strong>PostGIS 의 ST_DWithin 함수를 활용</strong>해 사용자가 클릭한 좌표의 정확한 <strong>정류장 후보군 리스트</strong>를 제시
- <strong>PostGIS 의 ST_ClusterDBSCAN 함수를 활용</strong>해 정류장들의 <strong>클러스터를 형성</strong>
- 버스의 경우 출발(d1), 출발 경유 환승(d2), 도착 경유 환승(d3), 도착(d4) 의 <strong>4way 방식으로 정류장 depth를 분류,</strong><br>
  연결된 depth를 <strong>경유하는 노선들을 필터링</strong>  
- 필터링 된 버스 도착정보는 경로 <strong>그래프를 통해 fanOut, fanIn 방면으로 정렬</strong>

## 기술 스택
<span><img src="https://img.shields.io/badge/Springboot-6DB33F?style=flat&logo=springboot&logoColor=white"/></span>
<span><img src="https://img.shields.io/badge/JWT-6DB33F?style=flat&logo=JWT&logoColor=white"/></span>
<span><img src="https://img.shields.io/badge/FastAPI-009688?style=flat&logo=FastAPI&logoColor=white"/></span>
<span><img src="https://img.shields.io/badge/WebSocket-009688?style=flat&logo=WebSocket&logoColor=white"/></span><br>
<span><img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=flat&logo=PostgreSQL&logoColor=white">
<span><img src="https://img.shields.io/badge/PostGIS-4169E1?style=flat&logo=PostGIS&logoColor=white"><br>
<span><img src="https://img.shields.io/badge/HTML5-E34F26?style=flat&logo=Html&logoColor=white">
<span><img src="https://img.shields.io/badge/CSS3-1572B6?style=flat&logo=CSS&logoColor=white">
<span><img src="https://img.shields.io/badge/Javascript-F7DF1E?logo=javascript&logoColor=white"/></span><br>
<span><img src="https://img.shields.io/badge/공공데이터포털 OpenAPI-gray"/></span>
<span><img src="https://img.shields.io/badge/서울열린데이터광장 OpenAPI-gray"/></span>
<span><img src="https://img.shields.io/badge/kakao map API-gray"/></span>



## 서버 흐름도
<img width="800" alt="Image" src="https://github.com/user-attachments/assets/85f08e04-e133-432f-9933-68e41e0fbe4d" />

<br>

## 서버 설계와 데이터 구조에 대한 고민
### 비즈니스 요구의 최우선순위를 기준으로 둔 서버 구조와 기술 선택
  - 대중교통 도착정보는 <strong>실시간성을 지키는 것을 최우선</strong>으로 두고 클라이언트와 <strong>WebSocket</strong> 으로 통신
  - 출퇴근 시간에 이용자가 몰릴 가능성이 크므로 <strong>WebSocket 서버의 확장이 용이하도록 전용서버를 따로 설계</strong>
  - 버스 노선 필터링 과정의 <strong>복잡한 계산 및 전반적인 비즈니스 API 관리</strong>를 위해 <strong>Spring boot</strong>를 선택
  - WebSocket에서 다양한 도착정보 <strong>API 게이트웨이 및 데이터 파이프라인</strong> 역할을 위해 <strong>fastAPI</strong>를 선택
  - 향후 <strong>앱으로 사용할 가능성 및 서버 스케일 아웃 시의 유연함 대비</strong>를 위해 <strong>JWT 토큰 기반 인증</strong> 방법을 선택 
  - <strong>좌표 기반의 다양한 내장 함수 기능</strong>을 비즈니스 요구에 적용하기 위해 <strong>PostgreSQL, PostGIS</strong>를 선택 
### DB 구조와 클라이언트가 WebSocket에 요청할 ClusterMsgDTO를 별도로 설계
  #### 1. 복잡한 Insert 및 update 로직에 단계를 두기 위해 테이블을 구분하여 설계
   - 클러스터 : 정류장 배열을 교통수단별로 모아둔 테이블
   - 버스필터 : 사용자 필터 노선 배열을 정류장 기준으로 모아둔 테이블
  #### 2. 실시간성을 지키기 위해서 클라이언트가 모든 정보를 요청 시에 WebSocket에 넘겨주도록 설계
   - WebSocket에서 DB에 요청하는 방법은 응답속도 병목 예상
   - 트레이드 오프 : 클라이언트가 요청을 하려면 두 테이블 정보 모두를 보내야 하는 메시지 구조 복잡성
  #### 3. WebSocket 서버에서 요청 처리 시간을 최소한으로 할 수 있도록 요청 메시지 구조를 설계
   - 두 테이블 내용을 그대로 DTO로 만들면 버스의 경우 WebSocket 서버에서 중첩 반복문 순회에서 병목 예상
   - 클러스터의 정류장을 key로, 버스필터의 노선들을 value로 하는 Map 타입으로 ClusterMsgDTO를 설계
   - WebSocket 서버는 Map 의 keyset 에 대한 반복문을 한 번만 순회하여 API 호출

<br>

## 구현 과정 중 트러블 슈팅
### 버스 필터 로직에서 같은 정류장에서의 환승이 아닌, 다른 정류장 환승케이스 반영 불가의 문제 발생
 #### 기존 : 경로 등록 시와 같은 출발-환승-도착의 3way 방식
  - 출발과 도착을 모두 경유하는 곳만 LinkedHashSet으로 필터링
 #### 접근 : 다른 정류장 환승 문제 반영을 위해 환승역을 두 단계로 나누어 4way 방식으로 구조 변경 
  - 출발역과 경유를 같이하는 depth 2
  - 도착역과 경유를 같이하는 depth 3
 #### 트레이드 오프 : 4way 방식에서는 정류장 정렬 시 LinkedHashSet 의 한계 
 #### 해결 : 그래프 구조를 통해 depth 2와 depth 3을 각각 탐색하여 정렬  
  - 각 depth에서 fanOut 대상 정류장 먼저 정렬 (도착방향)
  - 그 후 fanIn 대상 정류장 정렬 (출발방향)

<a href="https://loveyrooney.tistory.com/60">버스 필터 로직 문제 해결 과정의 더 자세한 내용은 여기를 참조🔗</a>
   
### 데이터 이기종으로 인한 ‘중복 그룹’ 문제 및 필터된 노선에 반대방향이 포함되는 ‘페어 정류장’ 문제 발생
 #### 문제 1. 서울 / 경기 모두에 속하는 정류장의 api call 기준을 정하기 어려운 문제를 ‘중복 그룹’ 이라고 정의 
  - 서울의 API 요청 파라미터인 ars_id 는 지역 간 중복이 가능 
 #### 문제 2. 노선Id 로만 필터를 해서 운행 방향이 고려되지 않고 모두 잡혀버리는 문제를 ‘페어 정류장’ 이라고 정의
  - 운행 방향을 단순 상행/하행으로 구분 불가능한 엣지 케이스 존재 
 #### 접근 : 문제는 두 가지이지만, 테이블 구조를 변경하면 한 방법으로 해결 가능
  - 지역별 원천에서 정류장 temp, 순번 temp 테이블 2개를 만들고 지역 간 중복처리 후 운영 DB에 통합
  - 정류장 테이블과 순번 테이블을 만들고 통합하는 배치 스크립트 작성
  - truncate - insert 방식으로 최신화 유지
 #### 해결 1. 중복 그룹의 ars_id는 서울 기준으로 통합, API call 조건은 크로스 트라이 방식으로 엣지케이스 커버
  - 중복 그룹의 ars_id는 요청에 필요한 서울 기준으로 통합 
  - API call은 station_id에 따라 구분된 지역으로 첫 트라이 후 결과가 없는 경우에만 반대지역에 리트라이 
 #### 해결 2. depth간 순번을 계산하여 절대값이 큰 페어 정류장을 2차 필터링 
  - 순번 테이블에는 지역별 순번 temp 전부를 반영하되, 중복그룹의 ars_id는 서울 기준으로 통합 
  - 노선Id로 1차 필터링 한 결과에 페어 정류장을 걸러내기 위해 노선과 정류장으로 순번 검색 
  - 다음 depth의 정류장 순번과 현재 정류장 순번 차의 절대값이 15 이상인 경우를 페어정류장으로 판단하여 삭제

