# MSA 기반 서비스 분리 시, Transactional Outbox Pattern 적용 보고서 

## 개요

이 보고서는 크게 6가지 파트로 구성됩니다.
  
**1) 문제 정의 - 분산 시스템에서 데이터 일관성 확보의 어려움** <br>
**2) 기존 예약 기능** <br>
**3) 2PC 방식 vs Saga 패턴** <br>
**4) MSA로 분리된 예약&결제 기능** <br>
**5) MSA 도입을 통해 개선된 점** <br> 
**6) 참고 자료** <br> 



### 1) 문제 정의 - 분산 시스템에서 데이터 일관성 확보의 어려움

- MSA 기반 서비스 분리 시, 특정 서버에서 발생한 이벤트를 다른 서버로 발송해야 합니다. <br> 
  이러한 경우, 데이터 일관성을 확보하는 것이 어렵습니다. <br> 
  이 문제를 예시를 통해 좀 더 자세하게 살펴보겠습니다. <br> 


#### (1) 발행되어야 하는 메시지가 발행되지 않는 경우
![image](https://github.com/user-attachments/assets/00e85476-f3cf-4dec-82d4-35cd8f17dbda)
![image](https://github.com/user-attachments/assets/51114dfa-b937-4663-850c-3c848aa97f75)
![image](https://github.com/user-attachments/assets/4549fa3e-332c-4c23-9fbc-fb80a2da9a9d)

- 위의 아키텍처는 상품을 등록하는 Market 서버 및 DB와 <br>
  상품을 검색하는 Search 서버와 DB가 분리된 것을 나타냅니다. <br> 
  이러한 경우 새로운 상품이 Market DB에 등록되면, 이 사실이 Search 서버로 전달되어,
  Search DB에도 등록되어야 합니다. <br>

  

#### (2) 발행되지 않아야 하는 메시지가 발행되는 경우
 








### 6) 참고 자료
- 분산 시스템에서 메시지 안전하게 다루기(https://blog.gangnamunni.com/post/transactional-outbox/)
- 

