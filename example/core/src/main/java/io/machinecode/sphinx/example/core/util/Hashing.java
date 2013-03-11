package io.machinecode.sphinx.example.core.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hashing {

    public static String hash(final String text) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            final byte[] sha1hash = md.digest();
            return convertToHex(sha1hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String convertToHex(final byte[] data) {
        final StringBuilder builder = new StringBuilder();
        for (final byte that : data) {
            int halfByte = (that >>> 4) & 0x0F;
            int twoHalfs = 0;
            do {
                if ((0 <= halfByte) && (halfByte <= 9)) {
                    builder.append((char) ('0' + halfByte));
                } else {
                    builder.append((char) ('a' + (halfByte - 10)));
                }
                halfByte = that & 0x0F;
            } while (twoHalfs++ < 1);
        }
        return builder.toString();
    }
}
