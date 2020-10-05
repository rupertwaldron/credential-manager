package com.ruppyrup.credentialmanager.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.sql.Connection;

@Slf4j
@Component
@Aspect
public class DataSourceAspect {
    @Around("target(javax.sql.DataSource)")
    public Object aroundDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Data Source Trace: " + joinPoint.getSignature());

        Object returnObject = joinPoint.proceed();
        if (returnObject instanceof Connection) {
            return createConnectionProxy((Connection) returnObject);
        } else
            return returnObject;
    }

    private Connection createConnectionProxy(Connection connection) {
        return (Connection) Proxy.newProxyInstance(
                com.ruppyrup.credentialmanager.aspects.DataSourceAspect.class.getClassLoader(),
                new Class[]{Connection.class},
                new ConnectionInvocationHandler(connection)
        );
    }
}
