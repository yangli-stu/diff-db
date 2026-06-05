package com.diffdb.connection;

import com.diffdb.model.DatabaseType;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads JDBC drivers. MySQL and PostgreSQL drivers are bundled with the plugin.
 * A user-provided jar overrides the bundled driver when given.
 *
 * <p>Drivers loaded from an external jar cannot be registered with the global
 * {@link java.sql.DriverManager} (cross-classloader restriction), so callers use
 * {@link #connect} which invokes the {@link Driver} instance directly.
 */
public final class DriverProvider {

    private static final Map<String, Driver> CACHE = new ConcurrentHashMap<>();

    private DriverProvider() {
    }

    /**
     * Opens a connection using the appropriate driver.
     *
     * @param type          database type (selects the driver class)
     * @param driverJarPath optional path to a driver jar; empty -> classpath
     * @param url           JDBC URL to connect to
     * @param user          username
     * @param password      password (may be null)
     */
    public static java.sql.Connection connect(DatabaseType type, String driverJarPath,
                                              String url, String user, String password)
            throws Exception {
        Driver driver = resolve(type, driverJarPath);
        Properties info = new Properties();
        if (user != null) info.put("user", user);
        if (password != null) info.put("password", password);
        java.sql.Connection conn = driver.connect(url, info);
        if (conn == null) {
            throw new IllegalStateException(
                    "Driver " + type.getDriverClassName() + " did not accept URL: " + url);
        }
        return conn;
    }

    private static Driver resolve(DatabaseType type, String driverJarPath) throws Exception {
        String cacheKey = type.getDriverClassName() + "@"
                + (driverJarPath == null ? "" : driverJarPath);
        Driver cached = CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        ClassLoader loader;
        if (driverJarPath != null && !driverJarPath.isBlank()) {
            File jar = new File(driverJarPath);
            if (!jar.isFile()) {
                throw new IllegalArgumentException("Driver jar not found: " + driverJarPath);
            }
            loader = new URLClassLoader(new URL[]{jar.toURI().toURL()},
                    DriverProvider.class.getClassLoader());
        } else {
            loader = DriverProvider.class.getClassLoader();
        }

        try {
            Class<?> clazz = Class.forName(type.getDriverClassName(), true, loader);
            Driver driver = (Driver) clazz.getDeclaredConstructor().newInstance();
            CACHE.put(cacheKey, driver);
            return driver;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    type.getDisplayName() + " JDBC driver not found ("
                            + type.getDriverClassName() + "). "
                            + "Specify a driver jar in the connection dialog, or reinstall the plugin.",
                    e);
        }
    }
}
