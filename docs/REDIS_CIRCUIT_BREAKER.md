

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

- 각각의 설정에 대해 좀 더 자세히 살펴보겠습니다. <br>

(1) **registerHealthIndicator: true**
- 설명: 이 설정이 true로 설정되면, Resilience4j의 서킷 브레이커 상태가 Spring Boot Actuator의 헬스 체크에 등록됩니다. <br>
       즉, 서킷 브레이커가 정상적으로 동작하는지, 열린 상태인지, 반열림 상태인지 등을 모니터링할 수 있습니다. <br>
  
- 목적: 시스템의 상태를 실시간으로 모니터링하고, 문제가 있을 경우 즉시 알림을 받을 수 있게 도와줍니다.

(2) **slidingWindowSize: 10**
- 설명: 서킷 브레이커가 슬라이딩 윈도우를 사용하여 최근의 호출을 추적하는 데 사용하는 호출의 최대 수를 설정합니다. <br>
        이 예제에서는 10번의 요청을 기록하고, 그 요청들을 기반으로 서킷 브레이커의 동작을 결정합니다. <br>

- 목적: 서킷 브레이커의 상태를 결정할 때 얼마나 많은 요청을 고려할지를 설정합니다. <br>
        요청 수가 많을수록 더 신뢰할 수 있는 판단을 내릴 수 있습니다. <br> 

(3) **minimumNumberOfCalls: 5**
- 설명: 서킷 브레이커가 상태 변경을 결정하기 전에 최소한 몇 번의 요청이 있어야 하는지를 설정합니다. <br> 
        예를 들어, 5번 이상의 요청이 발생해야만 서킷 브레이커가 실패율을 기반으로 작동하기 시작합니다. <br>
  
- 목적: 시스템의 초기 요청들이 잘못된 판단을 유도하지 않도록 보호하는 역할을 합니다. 요청 수가 적을 때는 더 신중하게 처리됩니다. <br> 

(4) **permittedNumberOfCallsInHalfOpenState: 3**
- 설명: 서킷 브레이커가 반열림(Half-Open) 상태로 전환되었을 때, 그 상태에서 허용되는 최대 호출 수를 설정합니다. <br>
        반열림 상태는 서킷 브레이커가 "열림" 상태에서 "닫힘" 상태로 전환되는 중간 단계입니다. <br>
  
- 목적: 반열림 상태에서는 제한된 수의 호출만 허용하여, 시스템이 복구되었는지 확인할 수 있도록 합니다. <br>
        이 설정은 실험적인 요청 수를 제한하는 데 사용됩니다.

(5) **automaticTransitionFromOpenToHalfOpenEnabled: true**
- 설명: 이 설정이 true로 되어 있으면, 서킷 브레이커가 열림(Open) 상태에서 반열림(Half-Open) 상태로 자동 전환됩니다. <br>
       서킷 브레이커가 열림 상태에서 잠시 대기한 후 일부 요청을 테스트하여 시스템이 정상으로 돌아왔는지 확인합니다. <br>

- 목적: 시스템이 정상으로 돌아왔을 때 자동으로 복구할 수 있게 하여, 수동으로 상태를 변경할 필요가 없습니다. <br>

(6) **waitDurationInOpenState: 5s** 
- 설명: 서킷 브레이커가 열림(Open) 상태에 있을 때, 이 상태를 유지하는 최대 시간을 설정합니다. 이 예제에서는 5초로 설정되어 있습니다. <br>
- 목적: 서킷 브레이커가 열림 상태에서 대기하는 시간을 설정하여, 시스템에 과도한 요청을 보내지 않도록 합니다. 이 시간을 지나면 서킷 브레이커는 반열림 상태로 전환될 수 있습니다. <br>

(7) **failureRateThreshold: 50**
- 설명: 서킷 브레이커가 열림(Open) 상태로 전환되기 위한 실패율의 임계값을 설정합니다. 이 설정은 요청 중 실패한 비율이 50%를 넘으면 서킷 브레이커가 열림 상태로 전환된다는 의미입니다. <br>
- 목적: 실패율이 일정 비율을 초과하면 서킷 브레이커가 자동으로 열림 상태로 전환되어 시스템에 추가적인 부하를 걸지 않도록 보호합니다.
  
(8) **eventConsumerBufferSize: 10**
- 설명: 서킷 브레이커의 이벤트를 기록하는 버퍼의 크기를 설정합니다. 이벤트는 서킷 브레이커의 상태 변경(열림, 닫힘 등)에 대한 정보를 기록합니다. <br>
- 목적: 이벤트가 버퍼를 넘지 않도록 설정하여 과도한 이벤트 기록을 방지합니다. 버퍼 크기를 조정하여 메모리 사용량을 최적화할 수 있습니다. <br>
 
(9) **recordExceptions**
설명: 서킷 브레이커가 실패로 간주할 예외를 명시하는 설정입니다. 설정된 예외가 발생하면 서킷 브레이커가 이를 실패로 기록하고, 해당 호출을 실패한 것으로 간주합니다. <br>
예: org.springframework.web.client.HttpServerErrorException, java.util.concurrent.TimeoutException, java.io.IOException <br> 
목적: 서킷 브레이커가 실패를 판단할 때 어떤 예외를 기준으로 삼을지 결정하는 역할을 합니다. 이를 통해 특정 예외에 대해서만 서킷 브레이커가 작동하게 할 수 있습니다. <br>

(10) **ignoreExceptions**
- 설명: 서킷 브레이커가 무시할 예외를 설정하는 항목입니다. 이 예외가 발생해도 서킷 브레이커는 이를 실패로 간주하지 않습니다. <br>
예: io.github.robwin.exception.BusinessException <br> 

- 목적: 비즈니스 로직에서 발생할 수 있는 예외가 서킷 브레이커의 동작에 영향을 미치지 않도록 설정합니다. <br>
       예를 들어, 비즈니스 예외는 시스템의 상태를 나타내는 것이 아니므로, 이를 무시하고 시스템의 신뢰성을 보장할 수 있습니다.


