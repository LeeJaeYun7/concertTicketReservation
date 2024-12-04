

# '콘서트 좌석 5분간 선점 예약' Redis 분산 락 트러블 슈팅 보고서 

## 개요

이 보고서는 크게 3가지 파트로 구성됩니다.
  
**1) Redis 분산 락 AOP 도입** <br> 
**2) AOP 도입 시 발생한 문제점** <br>
**3) 문제 해결** <br>


<br> 


**1) Redis 분산 락 AOP 도입** <br>

- **'콘서트 좌석 5분간 선점 예약'** 기능에 **Redis 분산 락**을 도입할 때 **AOP**를 도입했습니다. <br>
   AOP(Aspect-Oriented Programming)를 도입한 이유는 **코드의 재사용성**을 높이기 위해서입니다. <br> 
   **Redis 분산 락**을 적용하는 과정에서 AOP를 사용하면, **공통적인 로직**을 한 곳에서 관리하고 **다른 기능**에 쉽게 적용할 수 있습니다. <br> 

-  이렇게 **AOP**를 사용함으로써 **코드의 중복**을 줄이고, <br>
   **새로운 기능**을 추가할 때에도 일관된 방식으로 **분산 락**을 처리할 수 있어 **유지보수성과 확장성**이 향상됩니다. <br>


<br> 


(1) **DistributedLock 인터페이스 생성**
- Redis 분산 락의 key, timeUnit, waitTime, leaseTime을 지정한 인터페이스를 생성했습니다. <br>
  이 때, 타겟 클래스에는 인터페이스가 따로 존재하지 않으므로, **CGLIB 방식의 AOP가 적용**됩니다.<br>

[@Target, @Retention에 대하여](https://velog.io/@s2feeling/Target-Retention%EC%97%90-%EB%8C%80%ED%95%98%EC%97%AC) <br> 
[JDK Dynamic Proxy AOP vs CGLIB AOP 비교](https://velog.io/@s2feeling/JDK-Dynamic-Proxy-vs-CGLib-Proxy-%EB%B9%84%EA%B5%90) <br> 

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


(2) **타겟 메소드에 @DistributedLock 어노테이션 지정**
- '콘서트 좌석 5분간 선점 예약'을 수행하는 메소드에 커스텀 어노테이션인 @DistributedLock을 적용해줍니다. <br> 
  이 때, **key로 메소드의 파라미터인 lockName이 지정**되었음을 확인할 수 있습니다. 

```
@DistributedLock(key = "#lockName", waitTime = 60, leaseTime = 300000, timeUnit = TimeUnit.MILLISECONDS)
public SeatInfo getSeatInfoWithDistributedLock(String lockName, long concertScheduleId, long number) {
        return seatInfoRepository.findSeatInfoWithDistributedLock(concertScheduleId, number)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
}
```

<br> 

(3) **타겟 메소드 호출 시 수행되는 AOP 클래스**

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


(4) **CustomSpringELParser 클래스 설정**

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
  


```
