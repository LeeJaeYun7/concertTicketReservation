

# 'Redis 서킷 브레이커'  도입 보고서 

## 개요

이 보고서는 크게 4가지 파트로 구성됩니다.

<br> 
  
**1) 서킷 브레이커란?** <br>
**2) 서킷 브레이커의 3가지 상태** <br>
**3) 서킷 브레이커의 상태 변경** <br>
**4) Redis 서킷 브레이커 도입** <br> 


<br> 


### 1) 서킷 브레이커란? 

- 서킷 브레이커란, 직역하면 **회로 차단기**로 서비스간 장애 전파를 막는 역할을 하는 것입니다. <br>
  서킷 브레이커는 문제가 발생했을 때, **Open State**로 변경하여 요청을 차단함으로써 장애 전파를 막습니다. <br>

  이를 그림으로 나타내면 다음과 같습니다. <br> 
![image](https://github.com/user-attachments/assets/f0dd10af-ca83-4bce-8537-467f2dd43a67)


<br>


### 2) 서킷 브레이커의 3가지 상태 

- 서킷 브레이커에는 3가지 상태가 있는데, 바로 **Closed, Open, Half Open**입니다. <br>
  이를 표로 나타내면 다음과 같습니다. <br>

<br> 


| 상태         | Closed                                       | Open                                    | Half Open                                |
|--------------|----------------------------------------------|-----------------------------------------|------------------------------------------|
| 상황         | 정상                                         | 장애                                    | Open 상태가 되고 일정 요청 횟수/시간이 지난 상황. |
| 요청에 대한 처리 | 요청에 대한 처리 수행, 정해진 횟수/비율만큼 실패할 경우 Open 상태로 변경 | 외부 요청을 차단하고 에러를 뱉거나 지정한 callback 메소드를 호출 | 요청에 대한 처리를 수행하고 실패시 Open 상태로, 성공시 Close 상태로 변경 |


- 서킷 브레이커에서 **장애로 판단하는 기준**은 크게 2가지가 있습니다. <br>

(1) **Slow call** : 기준보다 오래 걸린 요청 <br> 
(2) **Failure call** : 실패 혹은 오류 응답을 받은 요청 <br> 
  

<br> 


### 3) 서킷 브레이커의 상태 변경  
- 서킷 브레이커의 상태 변경을 순서대로 나타내면 다음과 같습니다. <br>
![image](https://github.com/user-attachments/assets/f260ae6e-5944-4132-9066-616a51eb4973)


(1) **Closed** 상태에선 정상 요청 수행 <br> 

(2) **실패 임계치**(failureRateThreshold or slowCallRateThreshold) 도달시 Closed 에서 Open 으로 상태 변경 <br> 

(3) Open 상태에서 **일정 시간**(waitDurationInOpenState) 소요시 **Half Open** 으로 상태 변경 <br> 

(4) **Half Open** 상태에서의 요청 수행 <br> 

a. 지정한 횟수 (permittedNumberOfCallsInHalfOpenState 횟수만큼) 수행 후 **성공** 시 Half Open 상태에서 Closed 상태로 변경 <br>
b. 지정한 횟수 (permittedNumberOfCallsInHalfOpenState 횟수만큼) 수행 후 **실패** 시 Half Open 상태에서 Open 상태로 변경 <br> 

<br> 

### 4) Redis 서킷 브레이커 도입 <br> 


(1) **Gradle 의존성 추가** 
```
// build.gradle

// Resilience4j 의존성 추가
implementation 'io.github.resilience4j:resilience4j-spring-boot2:1.7.0'

// Spring Actuator 추가
implementation 'org.springframework.boot:spring-boot-starter-actuator'

```

- **Resilience4j**는 Netflix의 Hystrix에서 영감을 받아 개발된 가벼운 **Fault Tolerance** 라이브러리로, <br>
  시스템 장애나 성능 저하 상황에서 안정성을 유지할 수 있도록 돕습니다. <br>
  Resilience4j는 서킷 브레이커, 재시도, 타임아웃, 제한, 서지 처리 등 다양한 패턴을 지원하여, <br>
  MSA 환경에서의 신뢰성을 크게 향상시킬 수 있습니다.. <br> 

- **Spring Actuator**는 **Redis 서킷 브레이커의 상태**를 모니터링하고, <br>
  실제로 잘 동작하는지 확인하는 **헬스 체크** 기능을 제공합니다


<br> 


(2) **application.yml에 resilience4j 설정 추가** <br> 

```
// application.yml

... 

resilience4j.circuitbreaker:
  configs:
    default:
      registerHealthIndicator: true
      slidingWindowSize: 10
      minimumNumberOfCalls: 5
      permittedNumberOfCallsInHalfOpenState: 3
      automaticTransitionFromOpenToHalfOpenEnabled: true
      waitDurationInOpenState: 5s
      failureRateThreshold: 50
      eventConsumerBufferSize: 10
      recordExceptions:
        - org.springframework.web.client.HttpServerErrorException
        - java.util.concurrent.TimeoutException
        - java.io.IOException
      ignoreExceptions:
        - io.github.robwin.exception.BusinessException

```

- 이를 표로 간략히 정리하면 다음과 같습니다. <br>

| **설정 항목**                                              | **설명**                                                                                               |
|-----------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| **registerHealthIndicator**                               | 회로 차단기의 상태를 헬스 인디케이터로 등록하여 회로 차단기의 상태를 확인할 수 있도록 설정합니다.     |
| **slidingWindowSize**                                      | 실패율을 계산할 때 사용하는 최근 호출의 수를 설정합니다.                                               |
| **minimumNumberOfCalls**                                   | 실패율 계산을 위해 최소 호출 횟수를 설정합니다.                                                          |
| **permittedNumberOfCallsInHalfOpenState**                  | 반열림 상태에서 허용할 호출 횟수를 설정합니다. 이 값을 넘으면 다시 열림 상태로 전환됩니다.               |
| **automaticTransitionFromOpenToHalfOpenEnabled**           | 열림 상태에서 반열림 상태로 자동 전환을 활성화합니다.                                                   |
| **waitDurationInOpenState**                                | 열림 상태에서 반열림 상태로 전환되기까지 기다리는 시간을 설정합니다.                                     |
| **failureRateThreshold**                                   | 실패율이 이 비율을 초과하면 회로 차단기가 열림 상태로 전환됩니다.                                          |
| **eventConsumerBufferSize**                                | 이벤트 소비자를 위한 버퍼 크기를 설정합니다. 보통 이벤트 큐의 크기를 의미합니다.                        |
| **recordExceptions**                                       | 실패로 간주할 예외들을 설정합니다. 이 예외들이 발생하면 실패로 처리됩니다.                              |
| **ignoreExceptions**                                       | 실패로 처리하지 않을 예외들을 설정합니다. 이 예외들은 실패로 간주되지 않습니다.                        |
