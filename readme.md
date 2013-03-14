# Sphinx

An Arquillian extension for in container testing of real applications.

## Objectives

As most large projects have many entities and EJB's they take a long
time to deploy. Arquillian allows testing of these by using
`Shrinkwrap.createFromZipFile("/path/to/the-ear.ear")` but that means
each test will deploy your whole archive _once per test_ which in smaller
applications is a bit painful but almost unusably slow for large
applications.

Sphinx alleviates this problem by allowing you to configure archives to
be deployed before running tests and then auto-majiking dependencies
into your test archives. It also supports easy replacement of files
within your archive.

## Configuration

This is a slightly redacted version of the config I am using for an
application deployed in 2 parts. An ear (we are calling resources.ear)
containing all the entities and most of the EJB's and a seperate war
(web.war) which contains a legacy seam component that depends on the ear
but is seperate to stop a bunch of sean2<->seam3 classloading problems.

This config runs aginst AS7.2.0.Final and hibernate3 but should be
easily adaptable.

First off, in order to get the jar you will need to add this to either your project's `pom.xml` or `~/.m2/settings.xml`

>     <repositories>
>         <repository>
>             <id>machinecode-repository</id>
>             <name>MachineCode Repository</name>
>             <url>http://repository.machinecode.io/nexus/content/repositories/machinecode</url>
>             <releases>
>                 <enabled>true</enabled>
>                 <updatePolicy>never</updatePolicy>
>             </releases>
>             <snapshots>
>                 <enabled>true</enabled>
>                 <updatePolicy>never</updatePolicy>
>             </snapshots>
>         </repository>
>         <repository>
>             <id>machinecode-snapshots</id>
>             <name>MachineCode Snapshots</name>
>             <url>http://repository.machinecode.io/nexus/content/repositories/machinecode-snapshots</url>
>             <releases>
>                 <enabled>true</enabled>
>                 <updatePolicy>never</updatePolicy>
>             </releases>
>             <snapshots>
>                 <enabled>true</enabled>
>                 <updatePolicy>never</updatePolicy>
>             </snapshots>
>         </repository>
>     </repositories>

In your parent `pom.xml` add a dependency on sphinx.

>     <properties>
>         ...
>         <!-- Test dependencies -->
>         <version.io.machinecode.sphinx.sphinx-core>0.0.1-SNAPSHOT</version.io.machinecode.sphinx.sphinx-core>
>         <version.junit.junit>4.10</version.junit.junit>
>         <version.org.jboss.arquillian.arquillian-bom>1.0.3.Final</version.org.jboss.arquillian.arquillian-bom>
>         <version.org.jboss.as>7.2.0.Final</version.org.jboss.as>
>         <version.org.jboss.as.jboss-as-arquillian-container-managed>${version.org.jboss.as}</version.org.jboss.as.jboss-as-arquillian-container-managed>
>         <version.org.jboss.shrinkwrap.shrinkwrap-api>1.0.1</version.org.jboss.shrinkwrap.shrinkwrap-api>
>     </properties>
>
>     <dependencyManagement>
>         <dependencies>
>             ...
>             <!-- Test dependencies -->
>             <dependency>
>                 <groupId>io.machinecode.sphinx</groupId>
>                 <artifactId>sphinx-core</artifactId>
>                 <version>${version.io.machinecode.sphinx.sphinx-core}</version>
>             </dependency>
>             <dependency>
>                 <groupId>junit</groupId>
>                 <artifactId>junit</artifactId>
>                 <version>${version.junit.junit}</version>
>                 <scope>test</scope>
>             </dependency>
>             <dependency>
>                 <groupId>org.jboss.shrinkwrap</groupId>
>                 <artifactId>shrinkwrap-api</artifactId>
>                 <version>${version.org.jboss.shrinkwrap.shrinkwrap-api}</version>
>             </dependency>
>             <dependency>
>                 <groupId>org.jboss.arquillian</groupId>
>                 <artifactId>arquillian-bom</artifactId>
>                 <version>${version.org.jboss.arquillian.arquillian-bom}</version>
>                 <scope>import</scope>
>                 <type>pom</type>
>             </dependency>
>             <dependency>
>                 <groupId>org.jboss.as</groupId>
>                 <artifactId>jboss-as-arquillian-container-managed</artifactId>
>                 <version>${version.org.jboss.as.jboss-as-arquillian-container-managed}</version>
>             </dependency>
>         </dependencies>
>     </dependencyManagement>

In `pom.xml` for your integration test module add a dependency on
arquillian and sphinx

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
>         <dependency>
>             <groupId>org.jboss.weld</groupId>
>             <artifactId>weld-core</artifactId>
>             <scope>test</scope>
>         </dependency>
>     </dependencies>

Now configure your surefire plugin to run arquillian with sphinx.

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
>                         <dependency.directory>${project.build.directory}/dependencies</dependency.directory>
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
>                 <property name="javaVmArguments">-Djboss.inst=/usr/share/jboss-as -server -Xms2048m -Xmx2048m -XX:PermSize=256m -XX:MaxPermSize=1024m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+UseCompressedOops -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Djboss.bind.address.management=127.0.0.1 -Djboss.modules.system.pkgs=org.jboss.byteman -Djboss.server.log.dir=/var/log/jboss-as -Dorg.jboss.resolver.warning=true -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5050</property>
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

In my `standalone.xml` I have two datasouces configured. RealDS is the
postgres DB that I normally run my app against and TestDS is an H2 DB
that we can run tests against.

>     <datasources>
>         <datasource jndi-name="java:jboss/datasources/TestDS" pool-name="java:jboss/datasources/TestDS" enabled="true" use-java-context="true">
>             <connection-url>jdbc:h2:mem:test;DB_CLOSE_DELAY=-1</connection-url>
>             <driver>h2</driver>
>             <security>
>                 <user-name>sa</user-name>
>                 <password>sa</password>
>             </security>
>         </datasource>
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
>         <drivers>
>             <driver name="h2" module="com.h2database.h2">
>                 <xa-datasource-class>org.h2.jdbcx.JdbcDataSource</xa-datasource-class>
>             </driver>
>         </drivers>
>     </datasources>

In `sphinx.xml` we replaced this `persistence.xml` which the app
normally uses:

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
>                 <property name="hibernate.hbm2ddl.auto" value="validate"/>
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
>                 <property name="hibernate.hbm2ddl.auto" value="create-drop"/>
>                 ...
>             </properties>
>         </persistence-unit>
>     </persistence>

So now we should be deploying a single copy of our actual archives and
running our tests against an empty database.
