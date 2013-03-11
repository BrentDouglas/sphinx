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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class ArchiveUtil {

    private static int NEXT = 0;

    private static final String JAR_SPLITTER = "(.*\\.jar)(.*)";
    private static final String WAR_SPLITTER = "(.*\\.war)(.*)";

    private static final Pattern JAR = Pattern.compile(JAR_SPLITTER);
    private static final Pattern WAR = Pattern.compile(WAR_SPLITTER);

    private Set<File> files = new HashSet<File>();

    public static ArchiveUtil replace(final Archive<?> archive, final String target, final File replacement) throws IOException {
        final ArchiveUtil util = new ArchiveUtil();
        if (archive instanceof EnterpriseArchive) {
            util.doReplace((EnterpriseArchive) archive, target, replacement);
        } else if (archive instanceof WebArchive) {
            util.doReplace((WebArchive) archive, target, replacement);
        } else if (archive instanceof JavaArchive) {
            util.doReplace((JavaArchive) archive, target, replacement);
        }
        return util;
    }

    public void cleanUp() {
        for (final File file : files) {
             file.delete();
        }
    }

    private void doReplace(final JavaArchive archive, final String target, final File replacement) {
        archive.delete(target);
        archive.add(new FileAsset(replacement), target);
    }

    private void doReplace(final WebArchive archive, final String target, final File replacement) {
        archive.delete(target);
        archive.add(new FileAsset(replacement), target);
    }

    private void doReplace(final EnterpriseArchive archive, final String target, final File replacement) throws IOException {
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

    private <T extends Archive<T>> void replaceSubDeployment(final EnterpriseArchive archive, final Class<T> clazz, final Matcher matcher, final File replacement) throws IOException {
            final String path = matcher.group(1);
            final Node node = archive.get(path);
            final T replacementArchive = getReplacementArchive(clazz, node);
            final String subSection = matcher.group(2);
            replace(replacementArchive, subSection, replacement);
            final ArchivePath archivePath = node.getPath();
            archive.delete(archivePath);
            archive.merge(replacementArchive, archivePath);
    }

    private <T extends Archive<T>> T getReplacementArchive(final Class<T> clazz, final Node node) throws IOException {
            final InputStream stream = node.getAsset().openStream();
            final File file = new File("/tmp/sphinx_" + NEXT++);
            files.add(file);
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
