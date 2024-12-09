# Kafka 통합 테스트 시, TestContainer 적용 보고서 

## 개요

이 보고서는 크게 5가지 파트로 구성됩니다.
  
**1) 상황(Situation)** <br>
**2) 작업(Task)** <br>
**3) 행동(Action)** <br> 
**4) 결과(Result)** <br>
**5) 참고 자료** <br>


**1) 상황(Situation)**
![image](https://github.com/user-attachments/assets/ec7f68d7-064a-47e6-9917-fbdacf61df03)

- 콘서트 예약 서비스 프로젝트에서, 예약 서버에서 분리된 결제 서버로 메시지를 발송하였는데 <br> 
  이 때 Apache Kafka를 활용해 메시지를 발송하였습니다. <br> 
  이 때, 작성한 애플리케이션 코드가 Kafka를 통해 잘 동작하는지 테스트가 필요했습니다. <br>

- 하지만, 실제 Prod 환경에 배포될 Kafka를 그대로 테스트에 활용하게 되면, <br> 
  실제 서비스에 사용될 Offset 등이 오염되게 되어 문제가 발생할 수 있습니다. <br>
  따라서 실제 Prod 환경과 격리된 환경에서 Kafka 통합 테스트를 진행해야 할 필요성이 있었습니다. <br>

