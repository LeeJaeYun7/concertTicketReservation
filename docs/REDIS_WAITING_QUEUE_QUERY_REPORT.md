# 대기 번호 조회 및 좌석 선점 기능 개선: Polling -> WebSocket 


## 개요

이 보고서는 크게 6가지 파트로 구성됩니다.
  
**1) 콘서트 예약 서비스의 두 가지 기능** <br>
**2) 두 가지 기능의 문제 정의** <br>
**3) 문제 해결 - WebSocket 도입** <br>
**4) WebSocket 도입 과정** <br>
**5) WebSocket 도입 결과** <br>
**6) 참고 자료** <br>

<br> 


## 1) 콘서트 예약 서비스의 두 가지 기능
- **콘서트 예약 서비스**에는 <br>
 
  **(1) **대기 번호 조회** <br>** 
  **(2) **5분간 콘서트 좌석 선점** <br>**
  
  이라는 두 가지 주요 기능이 있습니다 <br>


### (1) 대기 번호 조회


<img src="https://github.com/user-attachments/assets/ae775b68-a15f-49a3-a76e-cec11c942bdf" width="50%" />



- **목적**: 대기열에서 대기중인 사용자가 자신의 대기 번호를 파악할 수 있도록 돕는 기능입니다. <br> 
- **설명** <br> 
  많은 사용자가 몰리는 경우, 일부 사용자는 대기하게 되며, 이때 **대기 번호**를 통해 예약 가능 시점을 예측할 수 있습니다. <br> 
  주기적인 대기 번호 조회는 사용자가 자신의 예약 가능 시점을 알 수 있게 해주어, <br>
  서비스의 투명성을 높이고, 예측 가능성을 제공합니다. <br> 

- **기술적 관리 방식: Polling API** <br>
  
  사용자가 **주기적으로 대기 번호를 조회**할 수 있도록 하기 위해 **Polling API** 방식을 사용합니다. <br>

  
<br> 


### (2) 5분간 좌석 선점

![image](https://github.com/user-attachments/assets/ba3661da-6da2-4b56-9b9d-1d29eef6e972)


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

### (1) 대기 번호 조회
- **문제점: 주기적인 Polling API 호출로 인한 서버 부하** <br>

- 사용자가 대기 번호를 조회하기 위해 **주기적으로 Polling API**를 호출해야 합니다. <br> 
- 예를 들어, **10만 명**의 사용자가 대기하고 있을 경우, 10초마다 1번씩 대기 번호를 조회한다면, **10초마다 10만 번의 Polling API 호출**이 발생합니다. <br>
- 이 대량의 API 호출은 **서버 CPU 자원에 큰 부하**를 주게 되며, 특히 트래픽이 몰리는 시점에서는 **서버 성능 저하**를 초래할 수 있습니다 <br>


<br>



### (2) 5분간 좌석 선점
- **문제점: 이탈한 사용자의 관리와 Redis 락 해제**
  
- 사용자가 **5분간 좌석 선점**을 시작한 후, 중간에 이탈하는 경우(예: 1분 후) 해당 좌석은 **Redis 분산 락**에 의해 잠겨 있는 상태입니다. <br>
  이 경우, **다른 사용자가 예약을 시도**하려면 먼저 락을 해제해야 하는데, 이탈한 사용자가 **언제 이탈했는지 확인**하는 문제가 발생합니다. <br>

- 사용자가 이탈했음을 추적하기 위해 **별도의 Polling API** 또는 기타 장치가 필요합니다. <br> 

- 예를 들어, 사용자가 좌석 선점을 시작한 후 **주기적으로 Polling API**를 호출하여, 사용자가 더 이상 호출하지 않으면 이탈한 것으로 간주할 수 있습니다. <br>
  하지만 이 방식은 서버가 **사용자 상태를 주기적으로 관리**해야 하므로, **관리의 복잡성과 불필요한 리소스 소모**를 초래할 수 있습니다. <br>


<br> 
<br>


## 3) 문제 해결 - WebSocket 도입  

- 위의 문제를 해결하기 위해서 **WebSocket 도입**을 선택하게 되었습니다. 

### 웹소켓(WebSocket) 도입 시 이점

#### (1) 양방향 통신 및 항구적인 연결
- 웹소켓 연결은 **클라이언트가 시작**하며, **한 번 맺어진 연결은 지속적**입니다.
  연결이 설정되면 **서버와 클라이언트 간의 양방향 통신**이 가능해져, 서버가 클라이언트에게 **비동기적으로 메시지를 전송**할 수 있습니다.
  초기에는 HTTP 연결로 시작하지만, **특정 핸드셰이크 절차**를 통해 웹소켓 연결로 업그레이드됩니다.

#### (2) 비동기적 메시지 전송
- 웹소켓은 연결이 열린 상태에서 클라이언트와 서버가 서로 메시지를 자유롭게 주고받을 수 있기 때문에 <br>
  **실시간**으로 데이터를 주고받는 데 유리합니다.

#### (3) CPU 사용률 감소

- **Polling 방식**에서는 주기적으로 서버에 요청을 보내야 하므로 CPU 리소스를 많이 소모하는 반면, <br> 
  웹소켓은 **지속적인 연결을 유지**하기 때문에 서버의 부담을 줄이고, CPU 사용률을 **낮출 수 있는 장점**이 있습니다.

 (참고 자료: https://velog.io/@sadik/%ED%8F%B4%EB%A7%81Polling-%EC%9B%B9%EC%86%8C%EC%BC%93WebSocket-%EC%84%B1%EB%8A%A5-%EB%B9%84%EA%B5%90) 



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


## 5) WebSocket 연결 결과 
- WebSocket 연결이 맺어진 상태에서, 사용자의 UUID가 발송이 되면 토큰이 응답합니다. <br>
  또한, 토큰을 발송하면, 사용자의 Redis 대기열 대기번호가 응답합니다. <br> 

![image](https://github.com/user-attachments/assets/94bc6971-1bfe-4135-819d-8d96ca3de6d0)


## 6) 참고 자료 
- 네이버페이 주문에 적용된 확장 가능한 대기열 개발기(https://d2.naver.com/helloworld/6480558)









