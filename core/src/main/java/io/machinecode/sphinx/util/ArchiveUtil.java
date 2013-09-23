package io.machinecode.sphinx.util;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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

    private static volatile String tempDir;

    private static Set<File> files = new HashSet<File>();

    public static void  replace(final Archive<?> archive, final String target, File replacement, final boolean substituteProperties) throws IOException {
        if (substituteProperties) {
            replacement = replaceFileProperties(replacement);
        }
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
        for (final File file : files) {
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

    private static <T extends Archive<T>> void replaceSubDeployment(final EnterpriseArchive archive, final Class<T> clazz, final Matcher matcher, final File replacement) throws IOException {
            final String path = matcher.group(1);
            final Node node = archive.get(path);
            final T replacementArchive = getReplacementArchive(clazz, node);
            final String subSection = matcher.group(2);
            replace(replacementArchive, subSection, replacement, false);
            final ArchivePath archivePath = node.getPath();
            archive.delete(archivePath);
            archive.merge(replacementArchive, archivePath);
    }

    private static <T extends Archive<T>> T getReplacementArchive(final Class<T> clazz, final Node node) throws IOException {
            final InputStream stream = node.getAsset().openStream();
            final File file = new File(tempDir + File.separatorChar + "sphinx_" + NEXT++);
            files.add(file);
            final FileOutputStream outputStream = new FileOutputStream(file);
            final byte[] buffer = new byte[1024*1024];
            int read;
            while((read = stream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            return ShrinkWrap.createFromZipFile(clazz, file);
    }

    private static File replaceFileProperties(final File inFile) throws IOException {
        final FileReader stream = new FileReader(inFile); //TODO Allow different encoding?
        final CharArrayWriter bytes = new CharArrayWriter();
        final char[] buffer = new char[1024*1024];
        int read;
        while((read = stream.read(buffer)) > 0) {
            bytes.write(buffer, 0, read);
        }

        final String fileAsString = PropertyUtil.resolve(new String(bytes.toCharArray()));

        final File outFile = new File(tempDir + File.separatorChar + "sphinx_" + NEXT++);
        files.add(outFile);
        final FileWriter writer = new FileWriter(outFile);
        writer.write(fileAsString);
        writer.flush();
        writer.close();
        return outFile;
    }
}
