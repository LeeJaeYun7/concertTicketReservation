

# '콘서트 좌석 5분간 선점 예약' Redis 분산 락 트러블 슈팅 보고서 

## 개요

이 보고서는 크게 4가지 파트로 구성됩니다.
  
**1) Redis 분산 락 AOP 도입 이유** <br> 
**2) Redis 분산 락 AOP 도입 과정** <br> 
**3) AOP 도입 시 발생한 문제점** <br>
**4) 문제 해결** <br>


<br> 


**1) Redis 분산 락 AOP 도입 이유** <br>
![image](https://github.com/user-attachments/assets/bba5abe8-9d67-4930-94fa-b2e30b8519d4)

- **'콘서트 좌석 5분간 선점 예약'** 기능에 **Redis 분산 락**을 도입할 때 **AOP**를 도입했습니다. <br>
   AOP(Aspect-Oriented Programming)를 도입한 이유는 **코드의 재사용성**을 높이기 위해서입니다. <br> 
   **Redis 분산 락**을 적용하는 과정에서 AOP를 사용하면, **공통적인 로직**을 한 곳에서 관리하고 **다른 기능**에 쉽게 적용할 수 있습니다. <br> 

-  이렇게 **AOP**를 사용함으로써 **코드의 중복**을 줄이고, <br>
   **새로운 기능**을 추가할 때에도 일관된 방식으로 **분산 락**을 처리할 수 있어 **유지보수성과 확장성**이 향상됩니다. <br>



<br> 
<br> 



**2) Redis 분산 락 AOP 도입 과정** <br>

(1) **DistributedLock 인터페이스 생성**
- Redis 분산 락의 key, timeUnit, waitTime, leaseTime을 지정한 인터페이스를 생성했습니다. <br>
  이 때, 타겟 클래스에는 인터페이스가 따로 존재하지 않으므로, **CGLIB 방식의 AOP가 적용**됩니다.<br>

[@Target, @Retention에 대하여](https://velog.io/@s2feeling/Target-Retention%EC%97%90-%EB%8C%80%ED%95%98%EC%97%AC) <br> 
[JDK Dynamic Proxy AOP vs CGLIB AOP 비교](https://velog.io/@s2feeling/JDK-Dynamic-Proxy-vs-CGLib-%EB%B9%84%EA%B5%90) <br> 

```
package com.example.concert.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락의 이름
     */
    String key();

    /**
     * 락의 시간 단위
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    long waitTime() default 1L;

    long leaseTime() default 30L;
}

```


<br> 


(2) **타겟 메소드에 @DistributedLock 어노테이션 지정**
- '콘서트 좌석 5분간 선점 예약'을 수행하는 메소드에 **커스텀 어노테이션인 @DistributedLock**을 적용해줍니다. <br> 
  이 때, **key로 메소드의 파라미터인 lockName이 지정**되었음을 확인할 수 있습니다. 

```
@DistributedLock(key = "#lockName", waitTime = 60, leaseTime = 300000, timeUnit = TimeUnit.MILLISECONDS)
public SeatInfo getSeatInfoWithDistributedLock(String lockName, long concertScheduleId, long number) {
        return seatInfoRepository.findSeatInfoWithDistributedLock(concertScheduleId, number)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
}
```

<br> 

(3) **타겟 메소드 호출 시 수행되는 AOP 클래스의 메소드**
- 타겟 메소드 호출 시, AOP 클래스의 메소드가 실행됩니다. <br>
  **ProceedingJoinPoint를 통해 실행되는 메소드의 정보**를 읽어옵니다. <br>

- 그 다음, @DistributedLock에 정의한 Key와 <br>
  CustomELParser를 통해 JoinPoint를 활용해 추출한 값을 조합하여 <br>
  **Redis 분산 락에 활용할 Key를 생성**합니다.

- 이후 해당 key로 RLock을 획득하고, **락 획득이 가능한지(tryLock) 확인**하는 작업을 거칩니다 <br>
  만약 락 획득이 가능하다면, 락을 획득한 후에, <br>
  aopForTransaction.proceed(joinPoint)를 통해 **트랜잭션을 생성**합니다. <br>


```
package com.example.concert.aop;

import com.example.concert.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @DistributedLock 선언 시 수행되는 Aop class
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(com.example.concert.lock.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);
        String key = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        RLock rLock = redissonClient.getLock(key);

        log.info("WaitTime: {}", distributedLock.waitTime());
        log.info("LeaseTime: {}", distributedLock.leaseTime());

        try {
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());  

            if (!available) {
                log.warn("Unable to acquire lock for service: {}, key: {}", method.getName(), key);
                return false;
            }

            log.info("Successfully acquired lock for service: {}, key: {}", method.getName(), key);
            return aopForTransaction.proceed(joinPoint);  
        } catch (InterruptedException e) {
            throw new InterruptedException();
        }
    }
}

```


<br> 


(4) **CustomSpringELParser 클래스 설정**

- CustomSpringELParser 클래스는 **Spring Expression Language (SpEL)** 을 활용한 파서 클래스로, <br>
  **메서드 파라미터의 값들을 동적으로 처리**하기 위해 사용됩니다. <br>
  주어진 parameterNames와 args 배열을 바탕으로, key로 전달된 SpEL 표현식을 평가하고 그 결과를 반환하는 기능을 합니다. <br> 


```
// CustomSpringELParser 클래스 

package com.example.concert.aop;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Spring Expression Language Parser
 */
public class CustomSpringELParser {
    private CustomSpringELParser() {
    }

    public static Object getDynamicValue(String[] parameterNames, Object[] args, String key) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return parser.parseExpression(key).getValue(context, Object.class);
    }
}

```


<br> 


**(5) AOPForTransaction 클래스**

- AOPForTranaction 클래스에서는 AOP 클래스에서 락 획득에 성공하면 <br>
  **새로운 트랜잭션을 생성**하는 역할을 합니다. <br>
   
```
@Component
public class AopForTransaction {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
```


<br> 


**3) AOP 도입 시 발생한 문제점** <br>

- 그런데 AOP 도입 시 문제점이 하나 발생했습니다. <br>
  그것은 바로.. 



