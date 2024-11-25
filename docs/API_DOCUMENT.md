
# 콘서트 예약 서비스 API 문서 


## 1. 대기열 토큰 발급

### Description
- 콘서트 대기열에 사용자를 추가하고 대기열 토큰을 반환합니다.

### Request
- URL: api/v1/waitingQueue/token
- Method: GET
- URL Params<br>
(1) concertId: Long (콘서트 ID) <br>
(2) uuid: String(사용자 ID) <br> 

### Response
```
{
    "token": "1732522359737:f00338f1-3a0e-4d1b-94d8-3a8ba14bbe36"
}

```

### Exception 
```
{
    "status": "NOT_FOUND",
    "message": "해당 사용자를 찾을 수 없습니다."
}
```


<br> 


## 2. 대기열 대기 번호 조회 

### Description
- 콘서트 대기열에서 대기 번호를 조회합니다.

### Request
- URL: api/v1/waitingQueue/rank
- Method: GET
- URL Params<br>
(1) concertId: Long (콘서트 ID) <br>
(2) token: String(대기열 토큰) <br> 

### Response
```
{
    "waitingRank": 1,
    "status": "waiting" 
}

// 사용자가 대기열 -> 활성화열로 전환된 경우 
{
    "waitingRank": -1,
    "status": "active"
}

```

<br> 

## 3. 예약 가능한 콘서트 일정 조회 

### Description
- 콘서트 중 예약 가능한 콘서트 일정을 조회합니다.

### Request
- URL: api/v1/concertSchedule
- Method: GET
- URL Params<br>
(1) concertId: Long (콘서트 ID) <br>

### Response
```
{
    "availableDateTimes": [
        "2024-11-30T09:00:00",
        "2024-11-30T09:00:00"
    ]
}
```
### Exception 
```
{
    "status": "NOT_FOUND",
    "message": "해당 콘서트를 찾을 수 없습니다."
}
```

<br> 

## 4. 예약 가능한 콘서트 좌석 조회 

### Description
- 콘서트 일정 중 예약 가능한 콘서트 좌석을 조회합니다.

### Request
- URL: api/v1/concertSchedule
- Method: GET
- URL Params<br>
(1) concertId: Long (콘서트 ID) <br>

### Response
```
{
    "availableDateTimes": [
        "2024-11-30T09:00:00",
        "2024-11-30T09:00:00"
    ]
}
 
```

