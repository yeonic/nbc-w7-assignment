# SPRING ADVANCED

## LV 5. 문제 정의 및 해결
### 공통 예외처리의 모호성
- 문제 인식 및 정의
  - GlobalExceptionHandler에서 예외를 처리할 때, 내부적으로 에러 정보를 알 수 있는 로그가 남지 않음.
  - ResponseStatus도 제대로 지정되어있지 않아, client의 혼란이 야기될 수 있음.

- 해결 방안
	- ErrorResponse가 나가기 전에 다음과 같이 exception 정보를 담아 로깅
  - 각 예외에 상응하는 ResponseStatus 지정
 
```java
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidRequestException(
          InvalidRequestException ex) {

    // 어느 handler에서 처리되었는지와 함께 exception 정보를 포함해 로깅
    log.info("handleInvalidRequestExceptionException", ex);

    HttpStatus status = HttpStatus.BAD_REQUEST;
    return getErrorResponse(status, ex.getMessage());
  }
```
	
- 해결 완료
  - 운영 주체의 입장에서는 예외 정보가 로그에 남기 때문에, 문제 발생시 파악할 여지가 생김.
  - 좀 더 명확한 Status Code를 통한 Client와의 원활한 소통 도모.

<br/>

### Aspect를 통해 Request Body를 로깅할 때, InputStream 문제
- 문제 인식 및 정의
  - Spring AOP를 활용해 Request Body를 로깅하고자 할 때, ServletRequestAttribute로 가져오려고 시도함.
  - InputStream은 한 번만 조회가 가능하도록 설계되어 있어, AOP에서 소비할 시, Controller에서 활용하지 못할 수 있는 문제 발생.
 
- 해결 방안
  - Request Body를 조회하고 캐싱하여 이런 문제를 해결할 수 있지만, AOP를 위해 Controller 구현을 변경하는 것은 본말이 전도된 결정으로 판단.
  - Aspect의 PointCut의 pattern을 설정하여 controller의 @RequestBody로 매핑된 argument를 가져오도록 구현함.
```java
  @Around("adminAnnotation() &&"
          + "!execution(void *(*)) &&"
          + "args(.., body)")
  public void withRequestBody(ProceedingJoinPoint joinPoint, Object body) throws Throwable {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    doLog(joinPoint, request, body);
  }

  @Around("adminAnnotation() && execution(void *(*))")
  public void withoutRequestBody(ProceedingJoinPoint joinPoint) throws Throwable {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    doLog(joinPoint, request, new HashMap<String, Object>());
  }
```

- 해결 완료
  - argument의 종류에 따라 Object body가 매치될 수도, 아닐 수도 있음.
  - 기존의 controller 코드를 변경하지 않은 것은 좋으나, argument의 종류만큼 method가 생겨야 하는 문제가 있음. 이 점에는 더 고민이 필요함.
 
<br/><br/>

## LV 6. 테스트 커버리지
![스크린샷 2025-02-27 10 21 13](https://github.com/user-attachments/assets/38967eb6-f200-466a-9508-da285fadc047)
