package io.machinecode.sphinx.sql;

import java.sql.Driver;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface DriverProducer {

    Driver produce() throws Exception;
}
