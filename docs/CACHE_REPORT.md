
# '최근 3일간 판매량 Top30 콘서트' 캐시 도입 보고서 

## 개요

이 보고서는 크게 4가지 파트로 구성됩니다.
  
**1) 캐시 도입 이유** <br>
**2) 캐시 도입 과정** <br>
**3) 캐시 구현** <br>
**4) 캐시 도입을 통해 개선된 점** <br> 


### 1) 캐시 도입 이유

- 콘서트 티켓 서비스에서는 **특정 API들이 빈번하게 데이터베이스를 조회**하는 상황이 발생하고 있습니다. <br> 
  사용자가 증가함에 따라, 이러한 반복적인 DB 조회로 인한 **DB 부하가 증가**하고, 이로 인해 **서비스 성능 저하**의 위험이 존재합니다. <br> 

- 이를 해결하기 위해, 빈번하게 조회되는 API에 대해 **캐시를 도입**하여, **조회 지연(latency)을 최소화**하고, <br> 
  서비스 성능을 개선함으로써 사용자 경험을 향상시키고자 합니다.


### 2) 캐시 도입 과정
![image](https://github.com/user-attachments/assets/33991bc0-e4e4-4bc3-a52f-38661aa6bc61)

**(1) 캐시 도입 기능 선택 <br>** 
- 콘서트 티켓 서비스에는 **콘서트**라는 주요 도메인이 존재합니다. <br> 
 사용자가 콘서트 메인 페이지에 접근할 때, **'가장 인기 있는'** 콘서트들의 정보가 제공되어야 합니다. <br> 

- 콘서트 티켓 서비스에서 **콘서트 메인 페이지**는 사용자들이 **가장 많이 방문**하는 페이지 중 하나입니다. <br> 
  따라서 **'가장 인기 있는'** 콘서트 정보를 제공하는 부분에 캐시를 적용하는 것이 <br>
  **서비스 성능 향상**과 **사용자 경험 개선**에 크게 기여할 것이라고 판단했습니다. <br> 
  이에 따라 **'최근 3일 간 판매량 Top 30 콘서트'** 정보를 제공하는 방식으로 캐시 적용을 결정하였습니다. <br> 

<br> 

**(2) 캐시 선택시 고려한 점**
- 캐시 선택은 **로컬 캐시와 글로벌 캐시**를 두고 고민했습니다. <br>


| **구분**      | **장점**                                                      | **단점**                                                          |
|---------------|---------------------------------------------------------------|-------------------------------------------------------------------|
| **로컬 캐시** | 빠른 데이터 접근 속도를 제공 / 캐시 서버의 장애로부터 자유롭다     |  분산 환경에서 캐시 동기화가 복잡할 수 있음                          |
| **글로벌 캐시**| 분산 환경에서 Single Source of Truth를 제공                    |  캐시 서버의 장애로 인한 네트워크 장애가 발생할 수 있음               |



**(3) 캐시 선택 결론**
- 저는 이번 캐시 도입을 **글로벌 캐시인 Redis**로 결정했습니다. 그 이유는 다음과 같습니다. <br>
  
1. **분산 환경에서는 동기화 문제**가 발생할 수 있기 때문에, <br>
    일반적으로 **글로벌 캐시**가 우선적으로 고려됩니다.<br>
    **로컬 캐시**는 각 서버별로 독립적으로 작동하므로 보조적인 역할로 활용되는 경우가 많습니다. <br> 
    
2. **캐시 서버 장애가 발생할 경우, Circuit Breaker** 패턴을 활용하여 장애를 제어하고 <br>
   시스템 안정성을 유지할 수 있다는 점을 고려했습니다. <br> 

- 따라서, 우선 **글로벌 캐시**를 도입하고, **성능 최적화**가 필요한 경우 **로컬 캐시**를 추가로 고려하는 방식으로 접근했습니다. <br> 
  프로젝트에서 이미 **Redis**와 **Redisson** 라이브러리를 사용하고 있었기 때문에, <br> 
  기존 라이브러리를 그대로 활용하여 캐시 기능을 구현하였습니다. <br>

<br>   

**(3) 캐시 구현**

**(3-1) ConcertCache 인터페이스**

```
// ConcertCache.java
public interface ConcertCache {
    void saveTop30Concerts(List<Concert> concerts) throws JsonProcessingException;
    List<Concert> findTop30Concerts() throws JsonProcessingException;
}

```

**(3-2) RedisConcertCache 클래스**
```
// RedisConcertCache.java
package com.example.concert.redis;

import com.example.concert.concert.cache.ConcertCache;
import com.example.concert.concert.domain.Concert;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class RedisConcertCache implements ConcertCache {

    private final RedissonClient redisson;
    private static final String TOP30_CONCERTS = "top30concerts";
    private final ObjectMapper objectMapper;

    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "fallbackSaveConcerts")
    public void saveTop30Concerts(List<Concert> concerts) throws JsonProcessingException {
        RMap<String, String> top30concertMap = redisson.getMap(TOP30_CONCERTS);
        String top30concertListJson = objectMapper.writeValueAsString(concerts);

        top30concertMap.put("top30concerts", top30concertListJson);
        top30concertMap.expire(Duration.ofMinutes(5).plusSeconds(10));

        log.info("Top 30 concerts saved to Redis as JSON.");
    }

    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "fallbackSaveConcerts")
    public List<Concert> findTop30Concerts() throws JsonProcessingException {
        RMap<String, String> top30concertMap = redisson.getMap(TOP30_CONCERTS);

        String top30concertListJson = top30concertMap.get("top30concerts");

        if (top30concertListJson != null && !top30concertListJson.isEmpty()) {
            log.info("Retrieved top 30 concerts from Redis.");
            return objectMapper.readValue(top30concertListJson, objectMapper.getTypeFactory().constructCollectionType(List.class, Concert.class));
        } else {
            log.warn("No top 30 concerts found in Redis.");
            return null;
        }
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

**(3-3) ConcertScheduler 클래스**
```
// ConcertScheduler.java

package com.example.concert.concert.scheduler;

import com.example.concert.concert.cache.ConcertCache;
import com.example.concert.concert.domain.Concert;
import com.example.concert.reservation.repository.ReservationRepository;
import com.example.concert.utils.TimeProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConcertScheduler {

    private final ReservationRepository reservationRepository;
    private final TimeProvider timeProvider;
    private final ConcertCache concertCache;

    @Scheduled(fixedRate = 300000)
    public void updateTop30Concerts() throws JsonProcessingException {
        LocalDateTime now = timeProvider.now();
        LocalDateTime threeDaysAgo = now.minus(Duration.ofHours(72));

        List<Concert> top30Concerts = reservationRepository.findTop30Concerts(threeDaysAgo);
        concertCache.saveTop30Concerts(top30Concerts);
    }
}

```

**(3-4) Circuit Breaker 설정**
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

#### Spring Actuator 헬스체크를 통한 CircuitBreaker 동작 확인
![image](https://github.com/user-attachments/assets/2a1b7404-e6ea-4371-ba5f-d02007cfb1a9)




**(4) 캐시 도입 시 고려한 점**


**(4-1) 캐시 워밍(Cache warming)**
```
  @GetMapping("/concert/save/top30/3days")
    public ResponseEntity<Void> saveTop30ConcertsIntoRedis() throws JsonProcessingException {
        concertFacade.saveTop30ConcertsIntoRedis();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

```

- **캐시 워밍**은 서버가 실제로 동작하기 전에 **미리 캐시를 채워 놓는 작업**을 의미합니다. <br>
  이를 통해 서버가 처음 시작될 때 캐시 조회가 가능하도록 준비하며, 성능을 향상시킬 수 있습니다. <br>
  만약 캐시 워밍을 하지 않고, 첫 요청 시 캐시가 없음을 확인한 뒤 DB에서 데이터를 조회해 캐시를 업데이트하는 방식으로 구현하면, <br>
  여러 요청이 동시에 캐시를 채우려 하면서 **캐시 스탬피드(Cache Stampede)** 현상이 발생할 수 있습니다. <br> 

- **캐시 스탬피드**는 캐시가 없거나 만료된 상황에서 여러 애플리케이션 인스턴스가 동시에 캐시를 업데이트하려 할 때 발생하는 현상으로, <br>
  **중복된 쓰기 작업**이 이루어져 서버에 불필요한 부하가 발생하는 문제입니다. <br> 

  

**(4-2) 캐시 스케줄러 구현**
```
@Component
@Slf4j
@RequiredArgsConstructor
public class ConcertScheduler {

    private final ReservationRepository reservationRepository;
    private final TimeProvider timeProvider;
    private final ConcertCache concertCache;

    @Scheduled(fixedRate = 300000)
    public void updateTop30Concerts() throws JsonProcessingException {
        LocalDateTime now = timeProvider.now();
        LocalDateTime threeDaysAgo = now.minus(Duration.ofHours(72));

        List<Concert> top30Concerts = reservationRepository.findTop30Concerts(threeDaysAgo);
        concertCache.saveTop30Concerts(top30Concerts);
    }
}

```
- **'최근 3일간 판매량 Top30 콘서트'** 정보를 제공함에 있어서, <br> 
  판매량은 계속해서 업데이트되므로, 실시간성을 반영할 필요가 있다고 생각했습니다.<br>
  따라서 캐시 스케줄러를 구현해서 **'5분마다'** 캐시 정보가 최신화되도록 하였습니다. <br> 
  

**(4-3) 캐시 TTL(Time To Live) 설정**
```
 @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "fallbackSaveConcerts")
 public void saveTop30Concerts(List<Concert> concerts) throws JsonProcessingException {
        RMap<String, String> top30concertMap = redisson.getMap(TOP30_CONCERTS);
        String top30concertListJson = objectMapper.writeValueAsString(concerts);

        top30concertMap.put("top30concerts", top30concertListJson);
        top30concertMap.expire(Duration.ofMinutes(5).plusSeconds(10));

        log.info("Top 30 concerts saved to Redis as JSON.");
}

```
- Redis 캐시는 일반적으로 메모리 관리를 고려해, TTL을 설정합니다. <br> 
  그리고 저는 이번 캐시 도입에서 캐시 TTL을 **5분 10초**로 설정했습니다. <br> 

- 그 이유는 다음과 같습니다.

(1) **캐시 스케줄러의 주기가 5분**임을 고려할 때, 캐시 TTL을 5분 이하 혹은 5분으로 설정하면, <br>
    순간적으로 사용자 요청에 캐시 미스(cache miss)가 발생할 수 있고, <br>
    이는 DB 부하로 이어질 가능성이 있다고 판단했습니다. <br> 
    따라서 캐시 미스를 없애고, 서비스를 안정적으로 제공하기 위해서 <br>
    캐시 TTL을 캐시 스케줄러의 주기보다 약간 긴 5분 10초로 설정하였습니다. 



### 3) 캐시 도입을 통해 개선된 점

- **'최근 3일간 Top30 콘서트' 정보**를 불러오는 것에 대해 **k6로 2가지 테스트를 진행**했습니다 <br>
(1) 해당 정보를 RDB에서 검색 <br>
(2) 해당 정보를 Redis에서 검색 <br> 

- **k6 테스트 조건**은 다음과 같았습니다 <br> 
(1) **60초 동안 1000건의 요청**이 발생 <br>
(2) **P90, P95, P99 지연시간** 측정 <br>
(3) **P99에 대한 threshold는 1초**로 설정 <br>

**(1) 해당 정보를 RDB에서 검색**
- 로컬에서 K6으로 테스트 시 다음과 같은 결과를 얻었습니다.
![image](https://github.com/user-attachments/assets/0cf0c014-d2bc-45ce-962e-c1c5d00a649d)

- **평균 응답 시간은 882.64ms**이었으며, 90%의 요청이 1.6s, 95%의 요청이 1.85s, 99%의 요청이 2.45s 이내에 응답하였습니다.
- **P99 threshold를 초과**해서 에러가 발생하였습니다. 

**(2) 해당 정보를 Redis에서 검색**
- 로컬에서 k6으로 테스트 시 다음과 같은 결과를 얻었습니다. 
![image](https://github.com/user-attachments/assets/ef758733-07d3-4ec6-ad5c-b1a7d0a0cad6)

- **평균 응답 시간은 256.16ms**이었으며, 90%의 요청이 496.25ms, 95%의 요청이 582.64ms, 99%의 요청이 798.72ms 이내에 응답하였습니다.
- Redis 사용 시 **평균 응답 시간 기준 RDB 대비 71.03% 개선**되었습니다.
  ![image](https://github.com/user-attachments/assets/035a9443-6da6-47b3-b844-1dbe441dcc28)

