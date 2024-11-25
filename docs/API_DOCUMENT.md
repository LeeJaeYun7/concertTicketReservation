
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
