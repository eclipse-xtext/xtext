To run the integration tests, the `maven.home` variable must defined, i.e., passed to the Maven build with `-Dmaven.home=...`.

For example, if the Maven executable is `/usr/local/bin/mvn`, you need to pass `-Dmaven.home=/usr/local` (i.e., without the `bin` subdirectory).

The Maven Invoker Plugin's goal `install` is attached to the `pre-integration-test` phase, in order to install the needed Maven artifacts of Xtext/Xtend into the `target/.m2test` folder, which will be then used by the integration test to build the test project generated from the archetype.

The integration test uses `src/test/resources/settings.xml` to disable remote snapshot repositories, so that it uses the current SNAPSHOT artifacts installed in `target/.m2test`.