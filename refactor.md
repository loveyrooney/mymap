# 리팩토링 가이드 (Refactoring Guide)

본 문서는 `mymap` 프로젝트의 코드 품질, 안정성, 유지보수성을 높이기 위한 개선 사항을 기술합니다.

## 1. Backend - Java (Spring Boot)

### 1.1 로깅 시스템 개선 (Logging)
*   **현재 상태**: `System.out.println`을 사용하여 로그를 출력하고 있음. 이는 성능 저하를 유발하고 로그 레벨 관리가 불가능함.
*   **개선 방안**: `Slf4j` 및 `Log4j2` (또는 `Logback`)를 적용.
*   **Action Item**:
    *   모든 `System.out.println` 제거.
    *   `@Log4j2` 어노테이션 활용하여 `log.info()`, `log.error()` 등으로 대체.

### 1.2 예외 처리 강화 (Exception Handling)
*   **현재 상태**: `try-catch` 블록에서 `e.printStackTrace()`만 호출하거나 단순 에러 메시지 출력.
*   **개선 방안**: `@RestControllerAdvice`를 활용한 **Global Exception Handler** 도입.
*   **Action Item**:
    *   `GlobalExceptionHandler` 클래스 생성.
    *   `BusinessException` 등 커스텀 예외 정의 및 표준 에러 응답 포맷(JSON) 설계.

### 1.3 하드코딩 제거 (Configuration Management)
*   **현재 상태**: 코드 내부에 API URL, 테스트용 상수 등이 하드코딩되어 있음.
*   **개선 방안**: `application.yml` (또는 `properties`)로 설정 값 분리.
*   **Action Item**:
    *   외부 서비스 URL, 타임아웃 설정 등을 설정 파일로 이동.

## 2. Backend - Python (FastAPI)

### 2.1 코드 구조 분리 (Separation of Concerns)
*   **현재 상태**: `apicalltest.py`에 테스트 코드와 실행 로직이 혼재되어 있음. `myWebSocket.py` 파일 하나에 소켓 로직과 데이터를 가져오는 로직이 포함되어 방대해질 가능성 있음.
*   **개선 방안**: 모듈화.
*   **Action Item**:
    *   `services/` 패키지를 만들고 `bus_service.py`, `subway_service.py` 등으로 외부 API 호출 로직 분리.
    *   데이터 모델(DTO)은 `Pydantic` 모델로 정의하여 타입 안정성 확보.

### 2.2 타입 힌트 및 데이터 검증 (Type Hinting)
*   **현재 상태**: `dict` 타입을 주로 사용하며 키 값에 대한 명시적인 정의가 부족함.
*   **개선 방안**: Python 3.9+ 타입 힌트 및 Pydantic 활용.
*   **Action Item**:
    *   API 응답 및 내부 데이터 구조에 Pydantic Model(`BaseModel`) 적용.

### 2.3 환경 변수 관리
*   **현재 상태**: `.env`를 로드하고 있으나 일부 민감 정보 관리가 필요해 보임.
*   **Action Item**:
    *   `python-dotenv` 활용 컨벤션 유지하되, `config.py` 등을 통해 환경변수 로드 로직 중앙화.

## 3. 공통 사항 (Common)

### 3.1 API 명세 관리
*   Spring Boot와 FastAPI 간, 그리고 Android 앱과의 통신을 위한 명확한 API 명세서(Swagger/OpenAPI) 최신화 및 공유 필요.

### 3.2 테스트 코드 (Testing)
*   핵심 비즈니스 로직(경로 계산, 필터링 등)에 대한 단위 테스트(JUnit/Pytest) 작성 필수.
