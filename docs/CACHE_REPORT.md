
# 캐시 도입 보고서 

## 개요

이 보고서는 크게 3가지 파트로 구성됩니다.
  
**1) 캐시 도입 이유** <br>
**2) 캐시 도입 과정** <br>
**3) 캐시 도입을 통해 개선된 점** <br> 


### 1) 캐시 도입 이유

- 콘서트 티켓 서비스를 운영하면서, 특정 API들이 빈번하게 DB에 조회되는 상황이 발생합니다. <br> 
  사용자가 증가함에 따라, 이러한 API들의 반복적인 DB 조회로 인해 DB 부하가 늘어나고, <br>
  이로 인해 서비스 성능 저하의 위험이 존재합니다. <br> 

- 이를 해결하기 위해, 빈번히 조회되는 API들에 대해 캐시를 도입함으로써 <br> 
  조회 지연(latency)을 줄이고, 서비스 성능을 개선하여 사용자 경험을 향상시키고자 합니다. <br> 


### 2) 캐시 도입 과정
![image](https://github.com/user-attachments/assets/33991bc0-e4e4-4bc3-a52f-38661aa6bc61)

**(1) 캐시 도입 기능 선택 <br>** 
- 콘서트 티켓 서비스에는 콘서트라는 도메인이 존재합니다. <br>
  그리고 사용자가 콘서트 메인 페이지에 접근했을 때, 주요한 콘서트들의 정보가 제공되어야 합니다. <br>

- 콘서트 티켓 서비스에서 콘서트 메인 페이지는 사용자가 가장 많이 접근하는 페이지 중 하나입니다. <br>
  그리고 그 때마다 콘서트들의 정보가 제공되어야 합니다. <br> 
  따라서 이 부분에 캐시를 적용하는 것이, 사용자 경험 향상에 도움이 될 것이라고 판단했습니다. <br> 
   

**(2) 캐시 선택시 고려한 점**
- 캐시 선택은 로컬 캐시(ehcache)와 글로벌 캐시(redis)를 두고 고민했습니다. <br>

- 로컬 캐시의 장점은<br>
  (1) 빠른 데이터 접근 속도를 제공하고, <br>
  (2) 캐시 서버의 장애로부터 자유롭다는 점이고, <br>
  
  반면, 단점은 <br>
  (1) 분산 환경을 고려할 때, 캐시의 동기화가 복잡할 수 있다는 점입니다. <br>

- 글로벌 캐시의 장점은 <br>
  (1) 분산 환경에서 Single Source of Truth를 제공한다는 점이고, <br>

  반면, 단점은 <br>
  (2) 캐시 서버의 장애로 인한 네트워크 장애가 발생할 수 있다는 점입니다. <br>  

**(3) 캐시 선택 결론**
- 저는 이번 캐시 도입을 글로벌 캐시인 Redis로 결정했습니다. <br>
  
  그 이유는 <br> 
  (1) 일반적으로 분산 환경에서는 동기화 이슈로 인해 글로벌 캐시가 우선적으로 고려되고, 로컬 캐시는 보조적으로 활용된다는 점 <br> 
  (2) 캐시 서버의 장애는 Circuit Breaker를 활용함으로써 제어할 수 있다는 점 <br>  
  입니다. <br>

- 따라서 우선적으로 글로벌 캐시를 도입하고, 이후에 성능 최적화를 위해 로컬 캐시를 추가로 도입할 수 있다고 판단했습니다. <br>  

- 프로젝트에서는 기존에 Redis의 Redisson 라이브러리를 활용하고 있었습니다. <br> 
  따라서 Redisson 라이브러리를 그대로 활용해, 캐싱 기능을 구현하였습니다.  


**(3) 캐시 구현**

```
// ConcertDao.java

package com.example.concert.redis;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConcertDao {

    private final RedissonClient redisson;

    private static final String CONCERTS = "concerts";

   // 콘서트 정보를 Redis에 저장하는 기능
    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "fallbackSaveConcertSchedules")
    public void saveConcerts(String concerts) {
        redisson.getBucket(CONCERTS).set(concerts);
        log.info("Concerts saved into redis");
    }

    // 콘서트 정보를 Redis에서 불러오는 기능
    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "fallbackSaveConcertSchedules")
    public String getConcerts() {
        RBucket<String> bucket = redisson.getBucket(CONCERTS);
        String value = bucket.get();

        if (value != null) {
            log.info("Retrieved concerts: {}", value);
        } else {
            log.warn("No concerts found.");
        }

        return value;
    }

    // Fallback 메소드 정의 (서킷 브레이커가 열렸을 때 호출됨)
    public void fallbackSaveConcerts(String concerts, Throwable t) {
        log.error("Failed to save concerts to Redis. Circuit breaker is open.", t);
    }

    public String fallbackGetConcerts(Throwable t) {
        log.error("Failed to retrieve concerts from Redis. Circuit breaker is open.", t);
        return null;
    }
}
```

```
// application.yml
// Circuit Breaker 설정 추가

...
...

management.endpoints.web.exposure.include: '*'
management.endpoint.health.show-details: always
management.health.diskspace.enabled: false
management.health.circuitbreakers.enabled: true
management.health.ratelimiters.enabled: false

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
![image](https://github.com/user-attachments/assets/2a1b7404-e6ea-4371-ba5f-d02007cfb1a9)



### 3) 캐시 도입을 통해 개선된 점

- 콘서트 정보 3000개를 불러오는 것을 기준으로 3가지 테스트를 진행했습니다 <br>
(1) 3000개를 RDB에서 검색 <br>
(2) 3000개를 Index가 적용된 RDB에서 검색 <br>
(3) 3000개를 Redis에서 검색 <br> 


