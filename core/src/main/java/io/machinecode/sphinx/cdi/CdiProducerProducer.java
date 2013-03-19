package io.machinecode.sphinx.cdi;

import io.machinecode.sphinx.cdi.deployment.Message;
import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassFile;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.classfilewriter.util.ByteArrayDataOutputStream;
import org.jboss.classfilewriter.util.DescriptorUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class CdiProducerProducer {

    private static final String NAME = "SphinxCdiProducer";
    private static volatile int id = 0;

    private static final String BINDING = "BINDING";


    public static void createProducer(final Archive<?> archive, final Message message) {
        final String className = NAME + id++;
        final ClassFile file = new ClassFile(className, CdiProducer.class.getCanonicalName());

        writeBinding(file, message.getBinding());

        for (final String produced : message.getClasses()) {
            writeProducer(file, produced);
        }

        final String fileName = "/" + className.replaceAll("\\.", "/") + ".class";
        final Asset asset = new Asset() {
            @Override
            public InputStream openStream() {
                final ByteArrayDataOutputStream stream = new ByteArrayDataOutputStream();
                try {
                    file.write(stream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return new ByteArrayInputStream(stream.getBytes());
            }
        };

        if (archive instanceof JavaArchive) {
            JavaArchive.class.cast(archive).add(asset, fileName)
                    .addClass(CdiProducer.class);
        } else if (archive instanceof WebArchive) {
            WebArchive.class.cast(archive).add(asset, fileName)
                    .addClass(CdiProducer.class);
        } else if (archive instanceof EnterpriseArchive) {
            //TODO Not sure this is really relevant
        }

        CdiArchiveProcessor.ensureBeansXml(archive);
    }

    private static void writeBinding(final ClassFile file, final String binding) {
        file.addField(AccessFlag.PRIVATE & AccessFlag.STATIC & AccessFlag.FINAL, BINDING, String.class, binding);

        final ClassMethod method = file.addMethod(AccessFlag.PROTECTED, "getBinding", DescriptorUtils.makeDescriptor(String.class));
        final CodeAttribute code = method.getCodeAttribute();
        code.getstatic(file.getName(), BINDING, String.class);
        code.returnInstruction();
    }

    private static void writeProducer(final ClassFile file, final String produced) {
        final String methodName = produced.replaceAll("\\.", "_").toLowerCase();
        final String fieldName = produced.replaceAll("\\.", "_").toUpperCase();

        file.addField(AccessFlag.PRIVATE & AccessFlag.STATIC & AccessFlag.FINAL, fieldName, String.class, produced);

        final ClassMethod method = file.addMethod(AccessFlag.PUBLIC, methodName, produced);
        method.getRuntimeVisibleAnnotationsAttribute().addAnnotation(ProducesLiteral.INSTANCE);

        final CodeAttribute code = method.getCodeAttribute();

        code.getstatic(file.getName(), fieldName, String.class);
        code.invokespecial(file.getName(), "get", produced, new String[]{String.class.getCanonicalName()});
    }
}
