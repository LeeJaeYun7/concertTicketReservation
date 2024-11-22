

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

### 4) Redis 서킷 브레이커 도입 


```
// build.gradle

// Resilience4j 의존성 추가
implementation 'io.github.resilience4j:resilience4j-spring-boot2:1.7.0'

// Spring Actuator 추가
implementation 'org.springframework.boot:spring-boot-starter-actuator'

```



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



