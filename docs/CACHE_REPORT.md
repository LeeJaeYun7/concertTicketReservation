
# '최근 3일간 판매량 Top30 콘서트' 캐시 도입 보고서 

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
  그리고 사용자가 콘서트 메인 페이지에 접근했을 때, **'가장 인기 있는'** 콘서트들의 정보가 제공되어야 합니다. <br>

- 콘서트 티켓 서비스에서 콘서트 메인 페이지는 **사용자가 가장 많이 접근**하는 페이지 중 하나입니다. <br>
  따라서 **'가장 인기 있는'** 콘서트 정보를 제공하는 부분에 캐시를 적용하는 것이, 사용자 경험 향상에 도움이 될 것이라고 판단했습니다. <br> 
-> 그리고 이를 **'최근 3일 간 판매량 Top30 콘서트' 제공**하는 것으로 결정하였습니다.   

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
- 저는 이번 캐시 도입을 **글로벌 캐시인 Redis**로 결정했습니다. <br>
  
  그 이유는 <br> 
  (1) 일반적으로 분산 환경에서는 동기화 이슈로 인해 글로벌 캐시가 우선적으로 고려되고, 로컬 캐시는 보조적으로 활용된다는 점 <br> 
  (2) 캐시 서버의 장애는 Circuit Breaker를 활용함으로써 제어할 수 있다는 점 <br>  
  입니다. <br>

- 따라서 우선적으로 글로벌 캐시를 도입하고, 이후에 성능 최적화를 위해 로컬 캐시를 추가로 도입할 수 있다고 판단했습니다. <br>  

- 프로젝트에서는 기존에 Redis의 Redisson 라이브러리를 활용하고 있었습니다. <br> 
  따라서 Redisson 라이브러리를 그대로 활용해, 캐싱 기능을 구현하였습니다.  


**(3) 캐시 구현**

```
// ConcertCache.java
public interface ConcertCache {
    void saveTop30Concerts(List<Concert> concerts) throws JsonProcessingException;
    List<Concert> findTop30Concerts() throws JsonProcessingException;
}

```

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
- 캐시 워밍이란, 서버가 구동하기 이전에 미리 **캐시를 초기화해놓는 작업**을 의미합니다. <br>
  만약 캐시 워밍을 하지 않고, 첫 요청 시 캐시가 없음을 확인하고, DB 조회를 통해 캐시를 업데이트 하는 식으로 구현하면 <br>
  **캐시 스탬피드(Cache stampede)** 현상이 발생할 수 있습니다.

- 캐시 스탬피드 현상이란 캐시가 없거나 만료된 시점에 여러 개의 애플리케이션이 <br>
  동시에 캐시를 쓰는 **중복 쓰기**와 같은 작업으로 인해 부하가 발생하는 것을 의미합니다.


  

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
    이는 DB 부하로 이어질 가능성이 있다고 판단했습니다.
    따라서 캐시 미스를 없애고, 서비스를 안정적으로 제공하기 위해서 <br>
    캐시 TTL을 캐시 스케줄러의 주기보다 약간 긴 5분 10초로 설정하였습니다. 



### 3) 캐시 도입을 통해 개선된 점

- '최근 3일간 Top30 콘서트' 정보를 불러오는 것에 대해 k6로 2가지 테스트를 진행했습니다 <br>
(1) 해당 정보를 RDB에서 검색 <br>
(2) 해당 정보를 Redis에서 검색 <br> 


**(1) 해당 정보를 RDB에서 검색**
- Postman으로 5차례 테스트 시 567ms, 160ms, 91ms, 109ms, 75ms가 소요되었습니다
- 평균적으로 200.4ms가 소요되었습니다. 
![image](https://github.com/user-attachments/assets/0e598607-6b7a-43c1-9d56-a43d9822082b)

**(2) 해당 정보를 Redis에서 검색**
- Redis에 콘서트 데이터 캐싱
- ![image](https://github.com/user-attachments/assets/d3dc6e76-497d-4f4b-acbd-323fef6efe9f)


- Postman으로 5차례 테스트 시 636ms, 128ms, 84ms, 100ms, 64ms 가 소요되었습니다
- 평균적으로 202.4ms가 소요되었습니다.
![image](https://github.com/user-attachments/assets/c4a4ebc2-196d-4e2c-abda-b2f7392101d5)

