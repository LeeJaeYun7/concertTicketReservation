# API 부하 테스트 분석과 가상 장애 대응 방안에 관한 보고서


## 시간 여유가 부족해, 우선적으로 local에서 테스트를 진행하였습니다
## 추후에 Docker 혹은 배포를 통해 테스트를 진행하겠습니다 


## 1. 개별 API 테스트

- 각 API에 대해 개별적으로 부하 테스트를 실시했다. <br> 

<br> 


**(1) 잔액 조회 API 테스트**

```
import http from 'k6/http';
import { check, sleep } from 'k6';

// 부하 테스트 설정
export let options = {
  stages: [
      { duration: '60s', target: 1000 }, // 60초 동안 1000명의 가상 사용자가 요청을 보냄
  ],
  thresholds: {
      http_req_duration: ['p(99)<1000'], // 99%의 요청이 1000ms 이내에 처리되어야 함
      http_req_failed: ['rate<0.01'], // 실패율이 1% 미만이어야 함
  },
  summaryTrendStats: ['avg', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

export default function () {
  // 요청할 URL
  let url = 'http://localhost:8080/api/v1/charge';

  // ChargeRequest DTO에 맞는 JSON 데이터
  let payload = JSON.stringify({
    uuid: 'user-1234',     // UUID 예시값 (테스트용 고유값)
    amount: 1000           // 충전 금액 예시값
  });

  // HTTP POST 요청 보내기
  let params = {
    headers: {
      'Content-Type': 'application/json',  // JSON 형식으로 전송
    },
  };

  let response = http.post(url, payload, params);

  // 응답이 null이 아니고, 상태 코드가 200이어야 한다
  check(response, {
    'status is 200': (r) => r.status === 200,
  });

  // 요청 간 간격을 주기 위한 sleep
  sleep(1);
}
```

![image](https://github.com/user-attachments/assets/612abf8e-d737-4d05-a101-e23fe07c2530)


#### (1) 분석
##### (1-1) 성공률
- 모든 요청이 성공적으로 처리되었다 (100% 성공률).

##### (1-2) 처리량
= 초당 약 145개의 요청을 처리했다.
- 총 9490개의 요청이 처리되었다.

##### (1-3) 응답 시간
- 평균 응답 시간: 2.33s
- 90번째 백분위 응답 시간: 4.92s
- 95번째 백분위 응답 시간: 5.98s
- 에러율: 0%로, 모든 요청이 성공적으로 처리되었다.

##### (1-4) 동시 사용자
- 동시 사용자: 최대 1,000명의 가상 사용자(VU)가 동시에 테스트를 수행했다.

##### (1-5) 네트워크 지연
- 요청 차단 시간(avg): 4.03ms
- 연결 시간(avg): 2.82ms

#### (2) 테스트 분석 결론
- 모든 요청이 성공적으로 처리되었으며, 평균 응답 시간이 2.33s로 다소 느린 편이다.
- 95번째 백분위 응답 시간이 5.98s로 매우 높아, 일부 요청에서 지연이 발생할 수 있다.
- 시스템이 초당 145개의 요청을 처리할 수 있다. 초당 처리량의 개선이 필요하다.

