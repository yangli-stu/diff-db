package com.diffdb.diff;

import liquibase.Scope;
import liquibase.logging.core.JavaLogService;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.servicelocator.StandardServiceLocator;

import java.util.HashMap;
import java.util.Map;

/**
 * Runs Liquibase work inside a properly initialized {@link Scope}.
 *
 * <p>In an IntelliJ plugin classloader, Liquibase's SPI-based service discovery
 * (log service, snapshot generators, etc.) often fails because
 * {@code ClassLoader.getResources()} doesn't traverse plugin jars correctly.
 * We explicitly supply {@link JavaLogService}, {@link ClassLoaderResourceAccessor},
 * {@link StandardServiceLocator}, and the plugin's {@link ClassLoader} so that
 * {@link java.util.ServiceLoader} can discover Liquibase's built-in services
 * (e.g., {@code SnapshotGenerator} implementations) from the plugin's
 * {@code lib/} directory.
 */
public final class LiquibaseScope {

    private LiquibaseScope() {
    }

    public static <T> T run(ScopedCall<T> call) throws Exception {
        ClassLoader pluginClassLoader = LiquibaseScope.class.getClassLoader();
        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(pluginClassLoader);
        try {
            Map<String, Object> scopeValues = new HashMap<>();
            scopeValues.put(Scope.Attr.logService.name(), new JavaLogService());
            scopeValues.put(Scope.Attr.resourceAccessor.name(), new ClassLoaderResourceAccessor());
            scopeValues.put(Scope.Attr.serviceLocator.name(), new StandardServiceLocator());
            scopeValues.put(Scope.Attr.classLoader.name(), pluginClassLoader);
            return Scope.child(scopeValues, call::call);
        } finally {
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }

    @FunctionalInterface
    public interface ScopedCall<T> {
        T call() throws Exception;
    }
}
