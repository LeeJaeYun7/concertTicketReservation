

# '1만 RPM 레디스 대기열' 도입 보고서 

## 개요

이 보고서는 크게 5가지 파트로 구성됩니다.
  
**1) 레디스 대기열이란?** <br>
**2) 레디스 대기열 도입 이유** <br>
**3) 레디스 대기열 도입 과정** <br>
**4) 레디스 대기열 구현** <br>
**5) 레디스 대기열 도입을 통해 개선된 점** <br>
**6) 참고 자료** <br> 

<br> 

### 1) 레디스 대기열이란? 

![image](https://github.com/user-attachments/assets/da19bff3-8c3a-4432-b7ec-36c04a249212)
- 레디스 대기열은 레디스의 **Sorted Set** 자료구조를 활용하여, 마치 '놀이공원의 대기줄'처럼 대기 기능을 구현하는 방식입니다. <br>
  웹 서비스에서는 **특정 이벤트**(예: 배민 치킨 이벤트)로 인해 순간적으로 대용량 트래픽이 몰릴 수 있는데, <br>
  이러한 상황에서 **레디스 대기열**을 사용하여 트래픽을 효과적으로 제어할 수 있습니다. <br>


![image](https://github.com/user-attachments/assets/882719c4-04bd-4e20-a0ad-0dfe3abb2bc6)
- 레디스의 **Sorted Set** 자료구조는 각 항목이 **스코어(Score)** 값에 따라 정렬되는 고유한 문자열의 집합입니다. <br>
  이를 활용하여, 대용량 트래픽을 **timestamp** 값을 기준으로 Sorted Set에 순차적으로 추가하고, 퇴장시킬 수 있습니다. <br>
  이렇게 하면 대용량 트래픽을 효과적으로 제어할 수 있을 뿐만 아니라, **순서**도 보장할 수 있습니다.



<br> 


 ### 2) 레디스 대기열 도입 이유 

- **콘서트 티켓 예약 서비스**는 사용자가 콘서트 티켓을 예약할 수 있는 기능을 제공합니다. <br>
  이 때, 인기 있는 콘서트의 경우, 한 번에 많은 사람들이 **동시에 예약을 시도**하게 되어 서버의 **트래픽 과부하**가 발생할 수 있습니다. <br>
  이를 해결하기 위해 순간적인 대량 트래픽을 효과적으로 제어하기 위해 **레디스 대기열을 도입**하였습니다. <br>


<br> 


 ### 3) 레디스 대기열 도입 과정 

<br> 

**(1) 대기열과 활성화열**

- 레디스 대기열은 **활성화열**과 짝을 이루며 작동합니다. <br> 
  대기열에 있는 대기 인원은 일정 주기마다 **활성화열로 이동**하게 되며, <br>
  **활성화열로 이동**한 인원은 그때부터 **티켓 예약**이 가능합니다. <br> 
  
- 이 때 중요한 점은 **어떤 주기**로 **몇 명**을 대기열에서 활성화열로 전환할 것인지 결정하는 것입니다. <br> 
  주기의 길이, 그리고 전환되는 인원에 따른 **장점과 단점**은 다음과 같습니다. <br> 


| **조건**                                 | **장점**                                 | **단점**                               |
|------------------------------------------|------------------------------------------|----------------------------------------|
| **주기가 길고, 전환되는 인원 수가 적음** | 트래픽을 더 효과적으로 제어할 수 있음    | 고객들의 대기 시간이 길어짐           |
| **주기가 짧고, 전환되는 인원 수가 많음** | 고객들의 대기 시간이 짧음               | 트래픽 과부하 발생 위험                |


<br> 


**(2) 목표 TPS 및 응답시간 산출**

- **1만명이 동시 접속**해서 예약하는 **인기 콘서트**를 가정해서 목표 TPS를 산출했습니다. <br>
  **목표 고객 대기 시간은 30초 이내**를 목표로 하였습니다.  

- 이 때, 1만번째로 진입한 고객이 **30초 이내에 활성화** 되어야 하므로, <br>
**초당 333명이 대기열->활성화열로 이동**하여야 합니다. <br>  
  그리고 **고객의 평균 예약 시간을 10초로 가정**하면, 최대 동시 활성화 고객은 333x10 = **3330명**이 되게 됩니다. <br> 
  
- 고객의 **평균 요청 횟수는 5회**로 가정하였습니다. <br> 
  이 때, 고객은 평균 2초에 한 번 요청을 보내게 되므로, <br>
  이에 대응하기 위한 3330/2 = **1665 TPS**의 서버를 구축하는 것을 목표로 하였습니다. 

- 그리고 [web.dev](https://web.dev/articles/ttfb?hl=ko#what-is-a-good-ttfb-score)를 참고해 <br>
  **응답 시간을 0.8s 이내**로 하는 것을 목표로 하였습니다. 



<br> 



 ### 4) 레디스 대기열 구현 


```
package com.example.concert.redis;

import com.example.concert.utils.RandomStringGenerator;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WaitingQueueDao {

    private final RedissonClient redisson;

    public WaitingQueueDao(RedissonClient redisson){
        this.redisson = redisson;
    }

    public void addToWaitingQueue(long concertId, String uuid){
        String timestamp = Long.toString(System.currentTimeMillis());

        String queueEntry = new StringBuilder(timestamp).append(":").append(uuid).toString();
        RSortedSet<String> waitingQueue = redisson.getSortedSet("waitingQueue:" + concertId);
        waitingQueue.add(queueEntry);
    }

    public long getWaitingRank(long concertId, String uuid){
        RSortedSet<String> waitingQueue = redisson.getSortedSet("waitingQueue:" + concertId);
        Collection<String> waitingQueueList = waitingQueue.readAll();

        long rank = 1L;

        // rank 계산
        for(String queueEntry: waitingQueueList){
            String[] splits = queueEntry.split(":");
            if(splits[1].equals(uuid)){
                break;
            }
            rank += 1;
        }

        // uuid가 대기열에 없음 -> -1을 반환
        if(rank > waitingQueueList.size()){
            return -1;
        }

        return rank;
    }

    public String getActiveQueueToken(long concertId, String uuid){
        RMapCache<String, String> activeQueue = redisson.getMapCache("activeQueue:" + concertId);  // RMapCache 사용

        return activeQueue.get(uuid);
    }

    public void deleteActiveQueueToken(long concertId, String uuid){
        RMapCache<String, String> activeQueue = redisson.getMapCache("activeQueue:" + concertId);  // RMapCache 사용
        activeQueue.remove(uuid);
    }

    // 각 콘서트 대기열 마다
    // 250개의 queueEntry를 10초마다 활성화열로 이동
    public void getAndRemoveTop250FromQueue(long concertId) {
        RSortedSet<String> waitingQueue = redisson.getSortedSet("waitingQueue:" + concertId);
        Collection<String> total = waitingQueue.readAll();

        if (total.isEmpty()) {
             return;
        }

        RMapCache<String, String> activeQueue = redisson.getMapCache("activeQueue:" + concertId);  // RMapCache 사용
        Collection<String> top250 = total.stream()
                                         .limit(250)
                                         .toList();

        top250.forEach(entry -> {
            String[] parts = entry.split(":");
            String uuid = parts[1];
            String token = RandomStringGenerator.generateRandomString(16);

            activeQueue.putIfAbsent(uuid, token, 300, TimeUnit.SECONDS);
        });

        top250.forEach(waitingQueue::remove);
    }
}
```




### 6) 참고 자료
- **네이버 메인 페이지의 트래픽 처리** (https://d2.naver.com/helloworld/6070967)
- **nGrinder를 활용한 부하테스트** (https://blog.naver.com/naverfinancial/223493300536)
- **타행 이체 기능 성능 개선기, 프로젝트 소개** (https://ujkim-game.tistory.com/90)
  




  
