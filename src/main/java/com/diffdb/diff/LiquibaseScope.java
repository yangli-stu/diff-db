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

    private static final ClassLoader PLUGIN_CLASS_LOADER = LiquibaseScope.class.getClassLoader();
    private static final StandardServiceLocator SERVICE_LOCATOR = new StandardServiceLocator();
    private static final ClassLoaderResourceAccessor RESOURCE_ACCESSOR = new ClassLoaderResourceAccessor(PLUGIN_CLASS_LOADER);

    private LiquibaseScope() {
    }

    public static <T> T run(ScopedCall<T> call) throws Exception {
        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(PLUGIN_CLASS_LOADER);
        try {
            Map<String, Object> scopeValues = new HashMap<>();
            scopeValues.put(Scope.Attr.logService.name(), new JavaLogService());
            scopeValues.put(Scope.Attr.resourceAccessor.name(), RESOURCE_ACCESSOR);
            scopeValues.put(Scope.Attr.serviceLocator.name(), SERVICE_LOCATOR);
            scopeValues.put(Scope.Attr.classLoader.name(), PLUGIN_CLASS_LOADER);
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
