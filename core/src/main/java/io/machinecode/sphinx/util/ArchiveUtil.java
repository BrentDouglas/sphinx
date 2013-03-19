package io.machinecode.sphinx.util;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class ArchiveUtil {

    private static final String JAR_SPLITTER = "(.*\\.jar)(.*)";
    private static final String WAR_SPLITTER = "(.*\\.war)(.*)";

    private static final Pattern JAR = Pattern.compile(JAR_SPLITTER);
    private static final Pattern WAR = Pattern.compile(WAR_SPLITTER);

    private static final Set<File> FILES = Collections.synchronizedSet(new HashSet<File>());

    private static volatile int NEXT = 0;

    private static volatile String tempDir;

    public static void replace(final Archive<?> archive, final String target, final File replacement) throws IOException {
        if (archive instanceof EnterpriseArchive) {
            doReplace((EnterpriseArchive) archive, target, replacement);
        } else if (archive instanceof WebArchive) {
            doReplace((WebArchive) archive, target, replacement);
        } else if (archive instanceof JavaArchive) {
            doReplace((JavaArchive) archive, target, replacement);
        }
    }

    public static void setTempDir(final String tempDir) {
        ArchiveUtil.tempDir = tempDir;
    }

    public static void cleanUp() {
        for (final File file : FILES) {
             file.delete();
        }
    }

    private static void doReplace(final JavaArchive archive, final String target, final File replacement) {
        archive.delete(target);
        archive.add(new FileAsset(replacement), target);
    }

    private static void doReplace(final WebArchive archive, final String target, final File replacement) {
        archive.delete(target);
        archive.add(new FileAsset(replacement), target);
    }

    private static void doReplace(final EnterpriseArchive archive, final String target, final File replacement) throws IOException {
        final Matcher jarMatcher = JAR.matcher(target);
        if (jarMatcher.matches()) {
            replaceSubDeployment(archive, JavaArchive.class, jarMatcher, replacement);
            return;
        }
        final Matcher warMatcher = WAR.matcher(target);
        if (warMatcher.matches()) {
            replaceSubDeployment(archive, WebArchive.class, warMatcher, replacement);
            return;
        }
        archive.delete(target);
        archive.add(new FileAsset(replacement), target);
    }

    public static <T extends Archive<T>> void replaceSubDeployment(final EnterpriseArchive archive, final Class<T> clazz, final Matcher matcher, final File replacement) throws IOException {
            final String path = matcher.group(1);
            final Node node = archive.get(path);
            final T replacementArchive = getReplacementArchive(clazz, node);
            final String subSection = matcher.group(2);
            replace(replacementArchive, subSection, replacement);
            final ArchivePath archivePath = node.getPath();
            archive.delete(archivePath);
            archive.merge(replacementArchive, archivePath);
    }

    public static <T extends Archive<T>> T getReplacementArchive(final Class<T> clazz, final Node node) throws IOException {
            final InputStream stream = node.getAsset().openStream();
            final File file = new File(tempDir + File.separator + "sphinx_" + NEXT++);
            FILES.add(file);
            final FileOutputStream outputStream = new FileOutputStream(file);
            final byte[] buffer = new byte[1024*1024];
            int read;
            while((read = stream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            return ShrinkWrap.createFromZipFile(clazz, file);
    }
}
