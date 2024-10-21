

# 동시성 제어 보고서 

## 1. 콘서트 대기열 시스템에서 동시성 문제가 발생할 수 있는 로직 

### 1) 잔액 충전

**(1) 발생 원인**<br>
- 한 명의 사용자가 잔액을 충전할 때, 같은 요청을 여러 번 호출 할 수 있음<br> 
-> 이러한 경우, 1번의 요청만 승인되도록 해야 하며, 이를 멱등성(idempotent) 처리라고 합니다. 

**(2) 목표 결과**<br>
- 한 명의 사용자가 여러 번 충전 요청을 보내더라도, 잔액은 한 번만 증가해야 한다
- 충전 금액은 사용자 계정에 정확하게 반영되어야 한다.

### 2) 좌석 예약 요청

**(1) 발생 원인**<br>
- 동시에 여러 명의 사용자가 하나의 좌석에 대해 예약 요청을 할 수 있음  

**(2) 목표 결과**<br>
- 특정 좌석에 대해 한 명의 사용자에게만 예약 요청이 성공해야 한다
- 동시에 요청한 나머지 사용자들은 예약 요청이 실패해야 한다   


## 2. 동시성 제어 과정


### 1) 잔액 충전

**코드**
```
public ChargeResponse chargeBalance(UUID uuid, long amount) throws Exception {
        validateMember(uuid);

        Member member = memberService.getMemberByUuidWithLock(uuid);
        long balance = member.getBalance();
        long updatedBalance = balance + amount;
        member.updateBalance(updatedBalance);

        chargeService.createCharge(uuid, amount);

        return ChargeResponse.of(updatedBalance);
}
```
```
public Member getMemberByUuidWithLock(UUID uuid) throws Exception {
        return memberRepository.findByUuidWithLock(uuid).orElseThrow(Exception::new);
}
```
@Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT m from Member m WHERE m.uuid = :uuid")
    Optional<Member> findByUuidWithLock(@Param("uuid") UUID uuid);
```

```

### 2) 좌석 예약 요청 


