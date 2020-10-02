package com.ruppyrup.credentialmanager.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@Aspect
public class ControllerAspect {
    @AfterReturning(value = "execution(* com..SpringController.*(..))", returning = "returnValue")
    public void afterReturningHelloBean(JoinPoint joinPoint, Object returnValue) {
        log.info("Controller Aspect Starting");
        log.info("Join Point = " + joinPoint.getStaticPart().toLongString());
        log.info("Signature = " + joinPoint.getSignature());
        log.info("Args = " + Arrays.toString(joinPoint.getArgs()));
        log.info("Kind = " + joinPoint.getKind());
        log.info("Return Value = " + returnValue);
        log.info("Controller Aspect Stopping");
    }
}
