
# MSA 기반 서비스 분리 시, Saga 패턴을 활용한 분산 트랜잭션 적용 보고서 

## 개요

이 보고서는 크게 5가지 파트로 구성됩니다.
  
**1) 상황(Situation)** <br>
**2) 작업(Task)** <br>
**3) 행동(Action)** <br>
**4) 결과(Result)** <br>
**5) 참고 자료** <br> 


<br> 


**1) 상황(Situation)** <br>

<br> 

<img src="https://github.com/user-attachments/assets/2e7b71e7-cbee-4306-ba58-84d144972884" width="50%" />


- 기존 콘서트 예약 서비스는 **모놀리식 아키텍처**로 개발되어 있었습니다. <br>
  하지만, **서비스 확장성, 서비스별 개별 배포, 서비스별 장애 격리** 등을 고려해 <br>
  이를 **MSA**로 전환하는 것을 고려하게 되었습니다.

-
