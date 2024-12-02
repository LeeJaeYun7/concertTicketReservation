# MSA 기반 서비스 분리 시, Transactional Outbox Pattern 적용 보고서 

## 개요

이 보고서는 크게 6가지 파트로 구성됩니다.
  
**1) 문제 정의 - 분산 시스템에서 데이터 일관성 확보의 어려움** <br>
**2) 문제 해결 - Transactional Outbox Pattern 도입** <br>
**3) 2PC 방식 vs Saga 패턴** <br>
**4) MSA로 분리된 예약&결제 기능** <br>
**5) MSA 도입을 통해 개선된 점** <br> 
**6) 참고 자료** <br> 


<br> 


### 1) 문제 정의 - 분산 시스템에서 데이터 일관성 확보의 어려움

- MSA 기반 서비스 분리 시, **특정 서버에서 발생한 이벤트를 다른 서버로 발송**해야 합니다. <br> 
  이러한 경우, **데이터 일관성을 확보하는 것이 어렵습니다**. <br> 
  이 문제를 예시를 통해 좀 더 자세하게 살펴보겠습니다. <br> 

<br> 

#### (1) 발행되어야 하는 메시지가 발행되지 않는 경우
![image](https://github.com/user-attachments/assets/00e85476-f3cf-4dec-82d4-35cd8f17dbda)
![image](https://github.com/user-attachments/assets/51114dfa-b937-4663-850c-3c848aa97f75)
![image](https://github.com/user-attachments/assets/4549fa3e-332c-4c23-9fbc-fb80a2da9a9d)

- 위의 아키텍처는 **상품을 등록하는 Market 서버 및 DB**와 <br>
  상품을 검색하는 Search 서버와 DB가 분리된 구조 나타냅니다. <br> 
  이러한 경우 **새로운 상품이 Market DB에 등록되면**, <br>
  이 사실이 **Search 서버로 전달되어, Search DB에도 등록**되어야 합니다. <br>

- 이를 위해 서비스 클래스의 registerProduct 메소드에서 **Market DB에 상품을 등록**한 후, <br>
  **컨트롤러 클래스에서 메시지를 발행**하는 방식으로 구현할 수 있습니다. <br>

- 하지만 이와 같은 방식으로 구현할 경우, **메시지를 발행하는 도중에 예외가 발생**하면, <br>
  **Market DB에는 상품이 추가**되었지만 **Search DB에는 등록되지 않는 상황**이 발생할 수 있습니다. <br>
  즉, **상품 정보가 동기화되지 않는 문제**가 발생합니다. <br> 
  

<br> 


#### (2) 발행되지 않아야 하는 메시지가 발행되는 경우

![image](https://github.com/user-attachments/assets/de5a074e-f2bb-4f5e-aa2d-87cc7a185d5c)
![image](https://github.com/user-attachments/assets/156fef63-5f5f-4d30-8a60-b037e3d64e1d)

- 이번에는 **서비스 클래스에서 메시지 발송을 같이 처리**하는 방식으로 구현해보았습니다., <br>
  하지만 이와 같은 방식으로 구현할 경우, <br>
  **데이터베이스 지연 등으로 인해 트랜잭션 커밋이 실패**하는 상황이 발생할 수 있습니다. <br>
  **상품은 데이터베이스에 저장되지 않는데, 메시지는 발행된다는 문제**가 있습니다.

- 이 경우, **Search 서버는 상품이 등록된 것으로 잘못 판단**하여, <br>
  **Search DB에 새로운 상품을 등록**하게 됩니다. <br> 
  즉, **등록되지 않아야 하는 상품 정보가 등록된다는 문제**가 발생합니다. <br>  


<br> 


### 2) 문제 해결 - Transctional Outbox Pattern 도입 

- 위와 같은 문제는 Transactional Outbox Pattern을 도입함으로써 해결할 수 있습니다. <br>

**(1) Transactional Outbox Pattern 이란?** 

![image](https://github.com/user-attachments/assets/97492b36-862c-47b7-a21d-7ab170a2222d)








### 6) 참고 자료
- 분산 시스템에서 메시지 안전하게 다루기(https://blog.gangnamunni.com/post/transactional-outbox/)
- Transactional Outbox 패턴으로 메시지 발행 보장하기(https://ridicorp.com/story/transactional-outbox-pattern-ridi/)
  

