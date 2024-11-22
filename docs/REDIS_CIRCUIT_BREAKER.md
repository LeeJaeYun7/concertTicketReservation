

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
      slidingWindowSize: 100
      minimumNumberOfCalls: 5
      permittedNumberOfCallsInHalfOpenState: 3
      automaticTransitionFromOpenToHalfOpenEnabled: true
      waitDurationInOpenState: 5s
      failureRateThreshold: 10
      eventConsumerBufferSize: 10
      recordExceptions:
        - org.springframework.web.client.HttpServerErrorException
        - java.util.concurrent.TimeoutException
        - java.io.IOException
      ignoreExceptions:
        - io.github.robwin.exception.BusinessException

```

- 이 중 가장 중요한 설정 정보는 **slidingWindowSize와 failureThreshold** 그리고 **automaticTransitionFromOpenToHalfOpenEnabled** 입니다. <br>

<br>   

(1) **slidingWindowSize: 100 & failureRateThreshold: 10**
- 이 설정은 최근 **100개의 호출**을 기준으로, 그 중 **10%가 실패**하면 서킷 브레이커가 **Open 상태**로 전환된다는 의미입니다. <br>
  
- **failureRateThreshold**를 **10%로 설정한 이유**는 Redis가 **캐시**, **분산 락** 등 **핵심 기능**을 담당하기 때문에 **빠른 실패 및 회복**이 중요하다고 판단했기 때문입니다. <br>
- 따라서, 시스템 장애가 발생했을 때 즉시 대응할 수 있도록 **낮은 임계값**을 설정했습니다. <br>
  
- 한편, **slidingWindowSize를 100으로 설정한 이유**는 **너무 작은 횟수**로 실패율을 계산할 경우, <br>
  서킷 브레이커가 자주 **Open 상태**로 전환될 수 있기 때문입니다. <br>
  이를 방지하고, **더 안정적인 동작**을 보장하기 위해 충분한 호출 데이터(100번)를 기반으로 실패율을 계산하도록 설정했습니다. <br>

<br> 
  
(2) **automaticTransitionFromOpenToHalfOpenEnabled: true** <br>
- automaticTransitionFromOpenToHalfOpenEnabled는 **서킷 브레이커의 중요한 설정** 중 하나로, 자동 복구 기능을 활성화합니다. <br>
  이 설정이 중요한 이유는 다음과 같습니다. <br> 


(1) **자동 상태 전환**
- **서킷 브레이커**가 **Open** 상태로 전환되면, 외부 서비스가 일시적으로 실패하거나 장애가 발생한 상태임을 나타냅니다. <br>
  Open 상태에서는 시스템이 더 이상 외부 요청을 처리하지 않거나 제한된 수의 요청만 받습니다. <br> 

- 자동 복구 기능이 활성화되면, **서킷 브레이커**는 **Open 상태**에서 **Half-Open 상태**로 자동으로 전환됩니다. <br>
  이 상태에서는 제한된 수의 요청을 다시 보내서, 서비스가 정상 상태로 복구될 수 있는지 테스트합니다. <br>
  이를 통해 **수동 개입 없이도** 시스템이 자동으로 회복을 시도할 수 있습니다.

(2) **빠른 회복 가능**
- 시스템 장애 발생 후, **자동으로 Half-Open 상태로 전환**되므로, 시스템이 정상화되었는지 검증하는 프로세스가 빠르게 진행됩니다.
  이로 인해 **서비스 복구 시간**을 단축시킬 수 있습니다.
  예를 들어, Redis와 같은 중요한 서비스에서는 **빠른 회복**이 필요합니다.
  자동 복구 기능을 통해 장애가 해결되었을 때 **서킷 브레이커**가 자주 자동으로 복구되므로 서비스의 가용성이 높아집니다.
