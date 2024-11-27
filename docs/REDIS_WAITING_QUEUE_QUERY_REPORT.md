# 대기 번호 조회 및 좌석 선점 기능 개선: Polling -> WebSocket 개선


## 개요

이 보고서는 크게 4가지 파트로 구성됩니다.
  
**1) 콘서트 예약 서비스의 두 가지 기능** <br>
**2) 두 가지 기능의 문제 정의** <br>
**3) 문제 해결 - WebSocket 도입** <br>
**4) WebSocket 도입 과정** <br>

<br> 


## 1) 콘서트 예약 서비스의 두 가지 기능
- **콘서트 예약 서비스**에는 <br>
 
  **(1) **대기번호 조회** <br>** 
  **(2) **5분간 콘서트 좌석 선점** <br>** 

  하는 두 가지 주요 기능이 있습니다 <br>


<br> 

### (1) 대기 번호 조회


![image](https://github.com/user-attachments/assets/ae775b68-a15f-49a3-a76e-cec11c942bdf)



- **목적**: 대기열을 통해 사용자가 자신의 예약 가능 시간을 파악할 수 있도록 돕는 기능입니다. <br> 
- **설명** <br> 
  많은 사용자가 몰리는 경우, 일부 사용자는 대기하게 되며, 이때 **대기 번호**를 통해 예약 가능 시점을 예측할 수 있습니다. <br> 
  주기적인 대기 번호 조회는 사용자가 자신의 예약 가능 시점을 알 수 있게 해주어, <br>
  서비스의 투명성을 높이고, 예측 가능성을 제공합니다. <br> 

- **기술적 관리 방식: Polling API** <br>
  
  사용자가 **주기적으로 대기 번호를 조회**할 수 있도록 하기 위해 **Polling API** 방식을 사용합니다. <br>

  
<br> 


**(2) 5분간 좌석 선점**
- **목적**: 사용자가 예약 시 좌석을 선점해 다른 사용자의 중복 예약을 방지하고, 결제 과정에서의 문제를 예방하는 기능입니다. <br> 
- **설명** <br>
  
  **대기열에서 활성화된 사용자**는 **5분 동안 좌석을 선점**할 수 있습니다. <br> 
  **5분간 좌석 선점** 기능은 여러 사용자가 동시에 같은 좌석을 예약하려고 할 때 발생할 수 있는 **결제 과정 중의 충돌**을 방지하기 위해 도입되었습니다. <br>
  이 기능을 통해, 결제 단계에서 **한 명의 사용자만 결제**를 진행할 수 있게 하여 **사용자 경험을 개선**합니다. <br> 

- **기술적 관리 방식: Redis 분산 락** <br>
  
  **Redis 분산 락**을 사용하여 좌석 선점 기능을 관리합니다. <br>
  이 방식은 **여러 사용자가 동시에 같은 좌석**을 예약하려는 경쟁 상황에서 **경쟁 상태를 안전하게 처리**할 수 있게 도와줍니다.


<br>


## 2) 두 기능의 문제 정의 

- 그런데 두 기능은 각각 다음과 같은 문제점을 갖고 있습니다.

<br>

**(1) 대기 번호 조회**
- **문제점: 주기적인 Polling API 호출로 인한 서버 부하** <br>

- 사용자가 대기 번호를 조회하기 위해 **주기적으로 Polling API**를 호출해야 합니다. <br> 
- 예를 들어, **10만 명**의 사용자가 대기하고 있을 경우, 10초마다 1번씩 대기 번호를 조회한다면, **10초마다 10만 번의 Polling API 호출**이 발생합니다. <br>
- 이 대량의 API 호출은 **서버 CPU 자원에 심각한 부하**를 주게 되며, 특히 트래픽이 몰리는 시점에서는 **서버 성능 저하**를 초래할 수 있습니다 <br>

**(2) 5분간 좌석 선점**
- **문제점: 이탈한 사용자의 관리와 Redis 락 해제**
- 
- 사용자가 **5분간 좌석 선점**을 시작한 후, 중간에 이탈하는 경우(예: 1분 후) 해당 좌석은 **Redis 분산 락**에 의해 잠겨 있는 상태입니다. <br>
- 이 경우, **다른 사용자가 예약을 시도**하려면 먼저 락을 해제해야 하는데, 이탈한 사용자가 **언제 이탈했는지 확인**하는 문제가 발생합니다. <br>
- 사용자가 이탈했음을 추적하기 위해 **별도의 Polling API** 또는 기타 장치가 필요합니다. <br> 
- 예를 들어, 사용자가 좌석 선점을 시작한 후 **주기적으로 Polling API**를 호출하여, 사용자가 더 이상 호출하지 않으면 이탈한 것으로 간주할 수 있습니다. <br>
- 하지만 이 방식은 서버가 **사용자 상태를 주기적으로 관리**해야 하므로, **관리의 복잡성과 불필요한 리소스 소모**를 초래할 수 있습니다. <br>


<br> 



## 3) 문제 해결 - 웹소켓 도입  

- 위의 문제를 해결하기 위해서 **웹소켓 도입**을 선택하게 되었습니다. 

<br>

(1) **웹소켓 도입 시 이점**





<br> 



## 4) WebSocket 도입 과정   

- WebSocket 도입 과정은 다음과 같습니다.


**(1) WebSocketConfig 클래스 설정**
```
// WebSocketConfig.java

package com.example.concertTicket_websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// Spring 설정 클래스임을 나타냅니다 
@Configuration
// Message Broker로 웹소켓을 활성화합니다. 
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /topic이라는 prefix로 시작하는 메모리 기반 Message Broker를 활성화합니다. 
        config.enableSimpleBroker("/topic");
        // @MessageMapping 어노테이션이 붙은 메소드에 /app이라는 prefix를 추가합니다. 
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Websocket 연결을 위한 end-point를 등록합니다. 
        registry.addEndpoint("/gs-guide-websocket");
    }
}
```

**(2) WaitingQueueController 클래스 설정**
```
// WaitingQueueController.java

package com.example.concertTicket_websocket.waitingQueue.controller;

import com.example.concertTicket_websocket.waitingQueue.dto.request.TokenRequest;
import com.example.concertTicket_websocket.waitingQueue.dto.request.WaitingRankRequest;
import com.example.concertTicket_websocket.waitingQueue.dto.response.TokenResponse;
import com.example.concertTicket_websocket.waitingQueue.dto.response.WaitingRankResponse;
import com.example.concertTicket_websocket.waitingQueue.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WaitingQueueController {

    private final WaitingQueueService waitingQueueService;

    @MessageMapping("/waitingQueue/token")
    @SendTo("/topic/token")
    public TokenResponse retrieveToken(TokenRequest tokenRequest) throws Exception {
        long concertId = tokenRequest.getConcertId();
        String uuid = tokenRequest.getUuid();

        String token = waitingQueueService.addToWaitingQueue(concertId, uuid);
        return TokenResponse.of(token);
    }

    @MessageMapping("/waitingQueue/rank")
    @SendTo("/topic/rank")
    public WaitingRankResponse retrieveWaitingRank(WaitingRankRequest waitingRankRequest) {
        long concertId = waitingRankRequest.getConcertId();
        String token = waitingRankRequest.getToken();
        String uuid = token.split(":")[1];

        return waitingQueueService.retrieveWaitingRank(concertId, uuid);
    }
}

```










