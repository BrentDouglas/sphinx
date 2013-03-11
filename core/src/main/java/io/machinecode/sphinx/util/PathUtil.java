package io.machinecode.sphinx.util;

import org.jboss.arquillian.container.spi.ConfigurationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class PathUtil {

    private static final String PROPERTY_START = "\\$\\{";
    private static final String PROPERTY_END = "\\}";
    private static final String PROPERTY_MATCHER = ".*" + PROPERTY_START + "(.*)" + PROPERTY_END + ".*";

    private static final Pattern PATTERN = Pattern.compile(PROPERTY_MATCHER);

    public static String resolve(String path) {
        Matcher matcher;
        while ((matcher = PATTERN.matcher(path)).matches()) {
            final String property = matcher.group(1);
            final String resolved = System.getProperty(property);
            if (resolved == null) {
                throw new ConfigurationException("Cannot find system property " + property);
            }
            path = path.replaceFirst(PROPERTY_START + property + PROPERTY_END, resolved);
        }
        return path;
    }
}
