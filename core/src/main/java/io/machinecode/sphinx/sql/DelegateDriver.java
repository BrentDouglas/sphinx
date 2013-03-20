package io.machinecode.sphinx.sql;

import io.machinecode.sphinx.config.DriverConfig;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * From http://www.kfu.com/~nsayer/Java/dyn-jdbc.html
 *
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class DelegateDriver implements Driver {

    private final Driver delegate;

    private DelegateDriver(final Driver delegate) {
        this.delegate = delegate;
    }

    public static Driver from(final DriverConfig config) throws Exception {
        final URL url = new URL("jar:file:" + config.getPathToDriverJar() + "!/");
        final URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { url });
        return (Driver) Class.forName(config.getDriverClass(), true, urlClassLoader).newInstance();
    }

    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        return delegate.connect(url, info);
    }

    @Override
    public boolean acceptsURL(final String url) throws SQLException {
        return delegate.acceptsURL(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) throws SQLException {
        return delegate.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return delegate.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return delegate.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return delegate.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }
}
