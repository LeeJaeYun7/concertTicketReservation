

## 1. 대기열 토큰 발급

### Description
- 대기열에 사용자를 추가하고 대기열 토큰을 반환합니다.

### Request
- URL: /v1/queue-tokens/users/{userId}
- Method: POST
- URL Params:
- userId: Long (사용자 ID)

### Response
```
{
  "tokenId": 1,
  "createdAt": "2024-07-04T10:00:00",
  "expiredAt": "2024-07-04T10:10:00"
}
```

### Error
```
{
  "code": 404,
  "message": "user not found"
}
```
