# MyMap (마이맵) 
### 교통편이 망한 직장인을 위한 출퇴근 전용 지도 서비스
 - 2025-03-26 ~ 2025-05-22 (1차, 개발 시작 2025-04-08)
 - 개인 프로젝트

### 프로젝트 개발 동기 및 핵심 제공 기능 
저는 역세권에 살지 않으면서 역세권이 아닌 곳으로 매일 출퇴근을 했던, 교통편이 망한 직장인이었습니다.<br>
출퇴근 길은 매일 이동하는 길이기에 길찾기 목적 보다는 대중교통 도착정보 확인을 위해 지도 서비스를 이용하게 됩니다. <br>
그런데 기존의 서비스가 알려주는 것 외에도 실제로 매일 다니다 보면 사용자가 로컬에서 체득하게 되는 정보들이 있습니다. <br>
이것을 활용해 사용자에 최적화된 대중교통 도착정보를 안내하여 전쟁같은 서울의 출퇴근길에서 보다 신속하고 간편한 이용을 위한 서비스를 개발하게 되었습니다. 

1. 이동 중에 경로가 바뀌는 경우, 기존 유사 서비스에서는 경로 재검색을 해야 했던 불편함 개선
   - 특정 경로와 상관 없이, 사용자가 정류소를 커스터마이징하여 경로를 생성할 수 있습니다.
   - 사용자가 설정한 정류소 위치를 기반으로 한번에 도착정보를 확인할 클러스터가 형성됩니다. 
   
3. 기존 유사 서비스에서는 정류장 단위로만 도착정보 확인이 가능했고, 모든 수단 중 내가 탈 수단을 찾아야 했던 불편함 개선
   - 정류소 단위가 아닌 클러스터 단위로 도착정보를 한번에 확인할 수 있습니다.
   - 예를 들면, 집이라는 클러스터에는 집에서 향할 수 있는 정류장a, 정류장b의 도착정보를 한번에 확인할 수 있게 됩니다. 
   - 버스의 경우, 클러스터 내에서 내가 탈 차의 도착정보만 확인할 수 있습니다.
   - 클러스터 내의 버스 노선들은 정류장 단위로 정렬되어 있습니다. 
  
| 경로 등록 화면 | 도착정보 조회 화면 |
|------|------|
| <img width="148" alt="Image" src="https://github.com/user-attachments/assets/ad3e1e76-4f80-472b-bd3a-6121454eba4c" /> | <img width="148" alt="Image" src="https://github.com/user-attachments/assets/3b901b0d-d9f9-4dfb-96d4-e79039d22b7d" /> |

<br>

### 기술 스택
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



### 서버 흐름도
<img width="800" alt="Image" src="https://github.com/user-attachments/assets/85f08e04-e133-432f-9933-68e41e0fbe4d" />

<br>

### 설계 및 구현 과정에서 했던 고민들 
#### 1. 비즈니스 서버와 웹소켓 서버를 분리해서 만들어야겠다.
  - 출퇴근 시간에 이용자가 몰릴 가능성이 크므로 웹소켓 서버의 확장이 용이하도록 서버를 따로 만들 필요가 있다.
  - 향후 지하철 필터 추가 및 기타 기능 확장을 생각할 때 역할에 따라 서버를 운영하는 것이 좋아 보였다. 
#### 2. JWT 를 이용하여 클라이언트 인증/인가 처리를 해야겠다.
  - 웹보다 앱으로 사용할 가능성이 크므로 Restful API 서버로 만드는 것이 좋겠다.
  - 웹소켓 서버의 수평확장 시에도 독립적으로 인증/인가 처리를 할 수 있도록.
  - 리프레시 토큰은 사용한 직후 버리고 새로 생성하도록 만들어 탈취 위험을 줄여야 겠다.
#### 3. 클라이언트 메시지용 DTO를 따로 만들어야겠다. 
  - 클러스터와 버스필터 테이블은 각각이 생성되는 로직의 과정이 복잡해서 한번에 트랜잭션 처리하기에 어려움이 있을것 같고, 정류장 단위로의 조회 기능도 확보하기 위해 분리했다.
  - 그러나 클라이언트가 클러스터 단위로 도착정보를 call하기 위해선 그 두가지 모두의 정보가 필요하다.
  - 그래서 두가지 정보를 포함한 클라이언트 메시지용 DTO를 만들었고, 웹소켓 서버에서 최소한의 접근으로 API를 호출할 수 있도록 구성했다.
#### 4. 좌표와 관련한 쿼리는 펑션으로 만들어 사용해야겠다.
  - 좌표계 변경 등의 과정은 PostgreSQL 내부에서 동작하는 과정이 복잡하여 이것을 서버단에서 직접 쿼리로 쓰기에는 DB의 안정성이 깨질 것 같다.

<br>

### 구현 과정 중 겪은 문제와 해결
#### 1. 버스 필터 로직에서 같은 정류장에서의 환승이 아닌, 근처의 다른 정류장에서의 환승 케이스를 반영할 수 없는 문제가 생겼다.
 - 처음에는 경로 등록과 같이 출발-환승-도착의 3way 방식으로, 출발과 도착을 모두 경유하는 곳만 필터링 하는 방법으로 구현했다. 
 - 다른 정류장 환승 문제를 반영하기 위해 환승역을 출발역과 경유를 같이하는 depth 2, 도착역과 경유를 같이하는 depth 3로 나누어 4way 방식으로 구조를 변경했다.
 - 4way 방식에선 환승역 노선들을 정류장별로 정렬하는 데 LinkedHashSet 만으로는 도착과 출발을 모두 반영하기 어려웠다.
 - 그래프 구조를 통해 fanOut 대상 정류장 정렬(도착방향), fanIn 대상 정류장 정렬(출발방향)을 통해 도착방향의 선순위, 출발방향의 후순위 방법으로 노선을 정렬하였다.
   
#### 2. 경로 등록 시에 모든 정류장 좌표를 불러오는 것은 너무 부하가 크다. 그러나 사용자가 선택한 좌표는 DB와 오차가 있는 문제가 있다.
 - DB를 공공데이터 파일자료로 구성했는데, 정류장 정보가 2만 row에 육박하기 때문에 이 모든 경우의 마커를 한번에 만들고 그 중에 선택하게 하는 방식은 클라이언트 측 부하가 너무 크다.
 - kakao map에서 사용자가 눈으로 정류장을 확인하고 찍는 좌표와, 공공데이터의 좌표는 오차가 있었기 때문에 이 문제를 해결하기 위해 위치 기반으로 검색하는 함수를 적용했다.
 - 사용자가 kakao map 에서 찍은 좌표를 서버에 보내면, 그와 가까이에 있는 DB내의 좌표들을 리스트업 해주는 PostGIS 함수를 통해 사용자가 그 리스트 중 정확한 위치를 선택할 수 있도록 적용하였다. 

