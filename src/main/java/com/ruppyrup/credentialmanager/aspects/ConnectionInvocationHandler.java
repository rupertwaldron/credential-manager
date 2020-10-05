package com.ruppyrup.credentialmanager.aspects;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ConnectionInvocationHandler implements InvocationHandler {

    private final Connection connection;

    private static Set<String> LOGGABLE_METHODS = new HashSet<>(Arrays.asList(
            "commit", "rollback", "close"
    ));

    public ConnectionInvocationHandler(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (shouldLogInvocation(method))
            log.info("Connection Trace: " + method.toGenericString());

        return method.invoke(connection, args);
    }

    private boolean shouldLogInvocation(Method method) {
        return LOGGABLE_METHODS.contains(method.getName());
    }
}
