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

### 
