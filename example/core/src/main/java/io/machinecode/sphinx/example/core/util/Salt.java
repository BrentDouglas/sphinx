package io.machinecode.sphinx.example.core.util;

import java.security.SecureRandom;
import java.util.Random;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public final class Salt {

    public static String get() {
        final Random random = new SecureRandom();
        random.setSeed(System.nanoTime());
        return Hashing.hash(random.toString());
    }
}
