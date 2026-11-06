To run the integration tests, the `target/.m2test` folder must first be populated with Maven dependencies and with the IT-SNAPSHOT jar of this Maven plugin.

Use the launch `pre-integration-test.launch` to do that.
This should also be done each time we change something in this plugin and its dependencies.

To run the integration tests from Eclipse, first run the above launch configuration.
Then, use the JUnit launch `Run ITs from Eclipse.launch` that appropriately passes the required properties `testMavenRepo`, pointing to the right `target/.m2test` absolute path, and `testSettingsXML`, pointing to the IT settings that disable remote snapshots.

The launch configuration `Run ITs from Eclipse.launch` can be used to run only the integration tests from Maven, making sure the requirements are also up-to-date (i.e., by using the command line argument `-am`).

NOTE: IT projects using xcore will face the version ranges in xcore concerning xtext dependencies.
For example, xcore has

```xml
    <dependency>
      <groupId>org.eclipse.xtext</groupId>
      <artifactId>org.eclipse.xtext</artifactId>
      <version>[2.13.0,3.0.0)</version>
    </dependency>
```

Thus, in `target/.m2test` you will find several subdirectories for Xtext artifacts, since Maven will
inspect the POMs for ranges defined in Xcode.
For example,

```
tree org/eclipse/xtext/org.eclipse.xtext
org/eclipse/xtext/org.eclipse.xtext
├── 2.13.0
│   ├── org.eclipse.xtext-2.13.0.pom
│   ├── org.eclipse.xtext-2.13.0.pom.lastUpdated
│   ├── org.eclipse.xtext-2.13.0.pom.sha1
│   └── _remote.repositories
...
├── 2.30.0
│   ├── org.eclipse.xtext-2.30.0.pom
│   ├── org.eclipse.xtext-2.30.0.pom.lastUpdated
│   ├── org.eclipse.xtext-2.30.0.pom.sha1
│   └── _remote.repositories
├── 2.30.0.M1
│   ├── org.eclipse.xtext-2.30.0.M1.pom
│   ├── org.eclipse.xtext-2.30.0.M1.pom.lastUpdated
│   ├── org.eclipse.xtext-2.30.0.M1.pom.sha1
│   └── _remote.repositories
...
├── 2.31.0-SNAPSHOT
│   ├── maven-metadata-local.xml
│   ├── org.eclipse.xtext-2.31.0-SNAPSHOT.jar
│   ├── org.eclipse.xtext-2.31.0-SNAPSHOT.pom
│   └── _remote.repositories
```

However, the important thing is that the latest and current SNAPSHOT version is resolved and used as JAR
(in this example, org.eclipse.xtext-2.31.0-SNAPSHOT.jar),
which corresponds to the JAR packaged from the current sources.
In fact, both Sonatype and Central Portal snapshots are completely disabled in IT projects
(see `src/test/resources/settings.xml`).