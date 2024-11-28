# API 부하 테스트 분석과 가상 장애 대응 방안에 관한 보고서



## 1. 개별 API 테스트

- 각 API에 대해 개별적으로 부하 테스트를 실시했다.

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
