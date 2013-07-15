# Sphinx

An Arquillian extension for retrofitting in-container testing to
large applications or any application built using 'big ball of mud'
architecture.

## Objectives

I have a large project with many entities and EJB's which takes a long
time to deploy. Arquillian allows testing this sort of app by using
`Shrinkwrap.createFromZipFile("/path/to/the-ear.ear")` but that means
each test will deploy your whole archive _once per test_ which is not
practical for larger applications.

Sphinx alleviates this problem by allowing you to configure your
applications archives to be deployed independently of your test archives
before running tests and then auto-majiking dependencies
into your test archives. It also supports pasting sql (such as a schema)
into a database before running tests and replacing files from the
pre-built archive.

It will (probably) only work with AS7+ as I have not tested it on anything
else.

## Get the jar

You can built this repo with `mvn clean install`.

## Configuration

It is configured from an xml file, the location of which needs to be specified
in `arquillian.xml` as shown in the following snippet.

>     <extension qualifier="sphinx">
>         <property name="config-file">/path/to/sphinx.xml</property>
>     </extension>

The simplest version of sphinx.xml would look like:

>     <?xml version="1.0"?>
>     <sphinx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>             xmlns="http://machinecode.io/schema/sphinx:0.1"
>             xsi:schemaLocation="http://machinecode.io/schema/sphinx:0.1 http://machinecode.io/schema/sphinx_0_1.xsd">
>
>         <temp-dir>/tmp</temp-dir>
>
>     </sphinx>

`temp-dir` is used to put temporary files when processing archives to
replace files.

`sphinx.xml` supports system property replacement with the syntax ${property}.

## SQL

Just pastes code from an sql script into a database. An example snippet
from sphinx.xml could look like:

>     <database>
>         <id>a-database</id>
>         <pre-deployment>${schema.file}</pre-deployment>
>         <post-deployment>${cleanup.file}</post-deployment>
>         <run-in-container>false</run-in-container>
>         <jdbc-connection-string>jdbc:postgresql://localhost/test?user=user&amp;password=password</jdbc-connection-string>
>         <driver>
>             <driver-class>org.postgresql.Driver</driver-class>
>             <path-to-driver-jar>${postgresql.driver.path}</path-to-driver-jar>
>         </driver>
>     </database>

This will connect to the database and run schema.file before running the
tests and cleanup.file after they have finished. If the driver for your
database is already on the classpath used to run arquillian you will not
have to specify the driver jar.

It's not very robust and if you can, you are probably be better server
using bash instead. It can be useful in some situations however, if you
set `run-in-container` to true, the the script to be executed from inside
the container's JVM rather than surefires JVM which allows you to run
tests in an H2 DB where the lifecycle of the database is tied to the JVM
it is created in.

## Archives

The main feature of sphinx is that it allows you to process and deploy
archives before running tests in arquillian. A simple example is:

>     <archive>
>         <path-to-archive>${postgresql.driver.path}</path-to-archive>
>     </archive>

Which will deploy the jar pointed to by `postgresql.driver.path`.

A more complicated example is:

>     <archive>
>         <path-to-archive>${dependency.directory}/the.ear</path-to-archive>
>         <manifest-entry>deployment.the.ear</manifest-entry>
>         <manifest-entry>deployment.the.ear.first.jar</manifest-entry>
>         <manifest-entry>deployment.the.ear.second.jar</manifest-entry>
>         <replace-file>
>             <existing>lib/entities.jar/META-INF/persistence.xml</existing>
>             <replacement>${test.resources.directory}/persistence.xml</replacement>
>         </replace-file>
>     </archive>

This will:

1. Open `the.ear` and replace `persistence.xml` from `entities.jar`
2. Deploy the modified version of `the.ear`
3. Automatically add dependencies to the manifest of each of your test jar's for
   `the.ear` and the two jars inside the.ear.

## Example Configuration

This is a slightly redacted version of the relevent config I am using for an
application deployed in 2 parts. An ear (we are calling resources.ear)
and a seperate war (web.war).

In the projects parent `pom.xml` there is a dependency on sphinx:

>     <properties>
>         ...
>         <version.io.machinecode.sphinx.sphinx-core>0.0.1-SNAPSHOT</version.io.machinecode.sphinx.sphinx-core>
>         ...
>     </properties>
>
>     <dependencyManagement>
>         <dependencies>
>             ...
>             <dependency>
>                 <groupId>io.machinecode.sphinx</groupId>
>                 <artifactId>sphinx-core</artifactId>
>                 <version>${version.io.machinecode.sphinx.sphinx-core}</version>
>             </dependency>
>             ...
>         </dependencies>
>     </dependencyManagement>

In `pom.xml` for the integration test module there are a dependencies on
arquillian and sphinx:

>     <dependencies>
>         <dependency>
>             <groupId>io.machinecode.sphinx</groupId>
>             <artifactId>sphinx-core</artifactId>
>             <scope>test</scope>
>         </dependency>
>         <dependency>
>             <groupId>org.jboss.arquillian.core</groupId>
>             <artifactId>arquillian-core-api</artifactId>
>             <scope>test</scope>
>         </dependency>
>         <dependency>
>             <groupId>org.jboss.arquillian.junit</groupId>
>             <artifactId>arquillian-junit-container</artifactId>
>             <scope>test</scope>
>         </dependency>
>         <dependency>
>             <groupId>org.jboss.as</groupId>
>             <artifactId>jboss-as-arquillian-container-managed</artifactId>
>             <scope>test</scope>
>         </dependency>
>     </dependencies>

The integration test modules runs after the application modules have been built. For
convenience the deployments I want are copied into a temporary directory and surefire
is configured to run arquillian with sphinx.

>     <plugin>
>         <groupId>org.apache.maven.plugins</groupId>
>         <artifactId>maven-antrun-plugin</artifactId>
>         <executions>
>             <execution>
>                 <phase>test-compile</phase>
>                 <goals>
>                     <goal>run</goal>
>                 </goals>
>                 <configuration>
>                     <target name="copy" description="Make directory for test archives">
>                         <mkdir dir="${project.build.directory}/dependencies"/>
>                     </target>
>                 </configuration>
>             </execution>
>         </executions>
>     </plugin>
>     <plugin>
>         <groupId>org.apache.maven.plugins</groupId>
>         <artifactId>maven-dependency-plugin</artifactId>
>         <executions>
>             <execution>
>                 <id>copy</id>
>                 <phase>pre-integration-test</phase>
>                 <goals>
>                     <goal>copy</goal>
>                 </goals>
>             </execution>
>         </executions>
>         <configuration>
>             <artifactItems>
>                 <artifactItem>
>                     <groupId>io.machinecode.fake</groupId>
>                     <artifactId>resources</artifactId>
>                     <version>${project.version}</version>
>                     <type>ear</type>
>                     <overWrite>true</overWrite>
>                     <outputDirectory>${project.build.directory}/dependencies</outputDirectory>
>                     <destFileName>resources.ear</destFileName>
>                 </artifactItem>
>                 <artifactItem>
>                     <groupId>io.machinecode.fake</groupId>
>                     <artifactId>web</artifactId>
>                     <version>${project.version}</version>
>                     <type>war</type>
>                     <overWrite>true</overWrite>
>                     <outputDirectory>${project.build.directory}/dependencies</outputDirectory>
>                     <destFileName>web.war</destFileName>
>                 </artifactItem>
>             </artifactItems>
>         </configuration>
>     </plugin>
>     <plugin>
>         <groupId>org.apache.maven.plugins</groupId>
>         <artifactId>maven-surefire-plugin</artifactId>
>         <configuration>
>             <skip>true</skip>
>         </configuration>
>         <executions>
>             <execution>
>                 <id>default-test</id>
>                 <phase>none</phase>
>             </execution>
>             <execution>
>                 <id>surefire-it</id>
>                 <phase>integration-test</phase>
>                 <goals>
>                     <goal>test</goal>
>                 </goals>
>                 <configuration>
>                     <skip>false</skip>
>                     <systemPropertyVariables>
>                         <test.resources.directory>${project.basedir}/src/test/resources</test.resources.directory>
>                         <sphinx.configuration.file>${project.basedir}/src/test/resources/sphinx.xml</sphinx.configuration.file>
>                         <schema.file>${project.basedir}/src/test/resources/schema.sql</schema.file>
>                         <cleanup.file>${project.basedir}/src/test/resources/cleanup.sql</cleanup.file>
>                         <dependency.directory>${project.build.directory}/dependencies</dependency.directory>
>                         <postgresql.driver.path>${env.HOME}/.m2/repository/postgresql/postgresql/${version.postgresql.postgresql}/postgresql-${version.postgresql.postgresql}.jar</postgresql.driver.path>
>                     </systemPropertyVariables>
>                 </configuration>
>             </execution>
>         </executions>
>     </plugin>

`arquillian.xml` needs to tell sphinx where to get it's config file from.

>     <?xml version="1.0"?>
>     <arquillian
>             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>             xmlns="http://jboss.org/schema/arquillian"
>             xsi:schemaLocation="http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd">
>
>         <container qualifier="jboss" default="true">
>             <configuration>
>                 <property name="jbossHome">/usr/share/jboss-as</property>
>                 <property name="javaVmArguments">-Djboss.inst=/usr/share/jboss-as -server -Xms2048m -Xmx2048m -XX:PermSize=256m -XX:MaxPermSize=1024m -XX:SurvivorRatio=6 -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSClassUnloadingEnabled -XX:+ExplicitGCInvokesConcurrent -XX:+UseCompressedOops -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Djboss.bind.address.management=127.0.0.1 -Djboss.modules.system.pkgs=org.jboss.byteman -Djboss.server.log.dir=/var/log/jboss-as -Dorg.jboss.resolver.warning=true -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5050</property>
>                 <property name="serverConfig">standalone.xml</property>
>                 <property name="allowConnectingToRunningServer">true</property>
>                 <property name="managementAddress">127.0.0.1</property>
>                 <property name="managementPort">9999</property>
>                 <property name="username">username</property>
>                 <property name="password">password</property>
>             </configuration>
>         </container>
>
>         <extension qualifier="sphinx">
>             <property name="config-file">${sphinx.configuration.file}</property>
>         </extension>
>     </arquillian>

Then create a `sphinx.xml` file in the location specified.

>     <?xml version="1.0"?>
>     <sphinx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>             xmlns="http://machinecode.io/schema/sphinx:0.1"
>             xsi:schemaLocation="http://machinecode.io/schema/sphinx:0.1 http://machinecode.io/schema/sphinx_0_1.xsd">
>
>         <temp-dir>/tmp</temp-dir>
>
>         <database>
>             <id>test-database</id>
>             <pre-deployment>${schema.file}</pre-deployment>
>             <post-deployment>${cleanup.file}</post-deployment>
>             <jdbc-connection-string>jdbc:postgresql://localhost/testdatabase?user=user&amp;password=password</jdbc-connection-string>
>             <driver>
>                 <driver-class>org.postgresql.Driver</driver-class>
>                 <path-to-driver-jar>${postgresql.driver.path}</path-to-driver-jar>
>             </driver>
>         </database>
>
>         <archive>
>             <path-to-archive>${dependency.directory}/resources.ear</path-to-archive>
>             <manifest-entry>deployment.resources.ear</manifest-entry>
>             <manifest-entry>deployment.resources.ear.interop.jar</manifest-entry>
>             <manifest-entry>deployment.resources.ear.core.jar</manifest-entry>
>             <replace-file>
>                 <existing>lib/data.jar/META-INF/persistence.xml</existing>
>                 <replacement>${test.resources.directory}/persistence.xml</replacement>
>             </replace-file>
>         </archive>
>
>         <archive>
>             <path-to-archive>${dependency.directory}/web.war</path-to-archive>
>             <manifest-entry>deployment.web.war</manifest-entry>
>         </archive>
>     </sphinx>

In `standalone.xml` there are two datasouces configured. RealDS is the
postgres DB that I manually test my app against and TestDS is an empty postgres DB
that my arquillian tests will use.

>     <datasources>
>         <xa-datasource jndi-name="java:jboss/datasources/TestDS" pool-name="java:jboss/datasources/TestDS" enabled="true">
>             <xa-datasource-property name="DatabaseName">
>                 test
>             </xa-datasource-property>
>             <xa-datasource-property name="ServerName">
>                 localhost
>             </xa-datasource-property>
>             <xa-datasource-property name="User">
>                 user
>             </xa-datasource-property>
>             <xa-datasource-property name="Password">
>                 password
>             </xa-datasource-property>
>             <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>
>             <driver>postgresql-8.4-702.jdbc4.jar</driver>
>         </xa-datasource>
>         <xa-datasource jndi-name="java:jboss/datasources/RealDS" pool-name="java:jboss/datasources/RealDS" enabled="true">
>             <xa-datasource-property name="DatabaseName">
>                 something
>             </xa-datasource-property>
>             <xa-datasource-property name="ServerName">
>                 localhost
>             </xa-datasource-property>
>             <xa-datasource-property name="User">
>                 someone
>             </xa-datasource-property>
>             <xa-datasource-property name="Password">
>                 somepassword
>             </xa-datasource-property>
>             <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>
>             <driver>postgresql-8.4-702.jdbc4.jar</driver>
>         </xa-datasource>
>     </datasources>

The replacement for persistence.xml specifed in `sphinx.xml` changes the datasource of the persistence unit
to the empty database:

>    <?xml version="1.0" encoding="UTF-8"?>
>     <persistence xmlns="http://java.sun.com/xml/ns/persistence"
>                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>                  xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd"
>                  version="1.0">
>
>         <persistence-unit name="ExamplePU">
>             <provider>org.hibernate.ejb.HibernatePersistence</provider>
>             <jta-data-source>java:jboss/datasources/RealDS</jta-data-source>
>             <properties>
>                 ...
>             </properties>
>         </persistence-unit>
>     </persistence>

With this replacement `persistence.xml`:

>     <?xml version="1.0" encoding="UTF-8"?>
>     <persistence xmlns="http://java.sun.com/xml/ns/persistence"
>                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>                  xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd"
>                  version="1.0">
>
>         <persistence-unit name="ExamplePU">
>             <provider>org.hibernate.ejb.HibernatePersistence</provider>
>             <jta-data-source>java:jboss/datasources/TestDS</jta-data-source>
>             <properties>
>                 ...
>             </properties>
>         </persistence-unit>
>     </persistence>

## Roadmap

Things that I would like to add to this are:

- CDI Injection
- An example project
- Integration with BrowserStack's API so automated browser tests can be run with the container in a known state.

## Remarks

If you have a better way to do anything that this projet does I would love to
hear about it. This was only made as I couldn't find another way to
conveniently run arquillian tests against a decent sized application that
wasn't designed to be able to be seperated into a bunch of test-sized pieces.