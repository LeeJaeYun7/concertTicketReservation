
# 캐시 도입 보고서 

## 개요

이 보고서는 크게 3가지 파트로 구성됩니다.
  
1) 캐시 도입 이유 <br>
2) 캐시 도입 과정 <br>
3) 캐시 도입을 통해 개선된 점 <br> 


### 1) 캐시 도입 이유

- 콘서트 티켓 서비스를 운영하면서, 특정 API들이 빈번하게 DB에 조회되는 상황이 발생합니다. <br> 
  사용자가 증가함에 따라, 이러한 API들의 반복적인 DB 조회로 인해 DB 부하가 늘어나고, 이로 인해 서비스 성능 저하의 위험이 존재합니다. <br> 

- 이를 해결하기 위해, 빈번히 조회되는 API들에 대해 캐시를 도입함으로써 <br> 
  조회 지연(latency)을 줄이고, 서비스 성능을 개선하여 사용자 경험을 향상시키고자 합니다. <br> 


### 2) 캐시 도입 과정
![image](https://github.com/user-attachments/assets/33991bc0-e4e4-4bc3-a52f-38661aa6bc61)

(1) 캐시 도입 기능 선택 <br> 
- 콘서트 티켓 서비스에는 콘서트라는 도메인이 존재합니다. <br>
  그리고 사용자가 콘서트 메인 페이지에 접근했을 때, 주요한 콘서트들의 정보가 제공되어야 합니다. <br>

- 콘서트 티켓 서비스에서 콘서트 메인 페이지는 사용자가 가장 많이 접근하는 페이지 중 하나입니다. <br>
  그리고 그 때마다 콘서트들의 정보가 제공되어야 합니다. <br> 
  따라서 이 부분에 캐시를 적용하는 것이, 사용자 경험 향상에 도움이 될 것이라고 판단했습니다. <br> 
   


  기존에 프로젝트에서 Redisson 라이브러리를 활용하고 있었습니다.
  따라서 Redisson 라이브러리를 활용해, 


```
// RedissonDao.java

package com.example.concert.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonDao {

    private final RedissonClient redisson;

    private static final String CONCERT_SCHEDULES = "concertSchedules";

    public void saveConcertSchedules(String concertSchedules) {
        redisson.getBucket(CONCERT_SCHEDULES).set(concertSchedules);
        log.info("Concert schedules saved into redis");
    }

    public String getConcertSchedules() {
        RBucket<String> bucket = redisson.getBucket(CONCERT_SCHEDULES);
        String value = bucket.get();

        if (value != null) {
            log.info("Retrieved concert schedules: {}", value);
        } else {
            log.warn("No concert schedules found.");
        }

        return value;
    }
}

```





