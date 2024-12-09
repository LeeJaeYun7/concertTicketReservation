# Kafka 통합 테스트 시, TestContainer 적용 보고서 

## 개요

이 보고서는 크게 5가지 파트로 구성됩니다.
  
**1) 상황(Situation)** <br>
**2) 작업(Task)** <br>
**3) 행동(Action)** <br> 
**4) 결과(Result)** <br>
**5) 참고 자료** <br>


<br> 

**1) 상황(Situation)** <br> 
![image](https://github.com/user-attachments/assets/ec7f68d7-064a-47e6-9917-fbdacf61df03)

- 콘서트 예약 서비스 프로젝트에서, 결제 서버로 메시지를 발송하는 과정에서<br> 
  **Apache Kafka**를 사용하여 메시지를 전달하고 있었습니다. <br> 
  이 때, 작성한 애플리케이션 코드가 **Kafka를 통해 정상적으로 동작하는지 테스트**해야 했습니다. <br>

- 하지만, 실제 Prod 환경에 배포될 Kafka를 그대로 테스트에 활용하게 되면, <br> 
  실제 서비스에 사용될 **Offset 오염**, **메시지 Cleaning**  등의 문제가 발생할 수 있습니다. <br>
  따라서 **실제 Prod 환경과 격리된 환경**에서 Kafka 통합 테스트를 진행해야 했습니다. <br>


<br> 

**2) 작업(Task)** <br>

![image](https://github.com/user-attachments/assets/98077e89-2c1d-42b3-80ec-3ff38924f5d5)

- 위의 문제를 해결하기 위해 **'Test Container' 도입**을 선택했습니다. <br> 
  Test Container 도입을 **선택한 이유**는 다음과 같습니다.

  **(1) 격리된 테스트 환경 제공**
  - Test Container는 테스트가 실행될 때마다, 독립적인 환경에서 진행됩니다. <br>
    따라서 다른 환경과 격리되어 원하는 테스트를 수행할 수 있습니다. <br>

  **(2) 데이터 Cleaning 불필요 및 멱등성 보장**
  - 테스트가 끝나면 Test Container는 자동으로 종료되고 제거됩니다. <br>
    이로 인해 별도의 데이터 Cleaning 작업이 필요하지 않으며, <br>
    각 테스트는 독립적으로 실행되어 다른 테스트에 영향을 주지 않기 때문에 <br>
    멱등성을 보장할 수 있습니다. <br> 

