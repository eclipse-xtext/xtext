To run the integration tests, the `target/.m2test` folder must first be populated with Maven dependencies and with the IT-SNAPSHOT jar of this Maven plugin.

Use the launch `pre-integration-test.launch` to do that.
This should also be done each time we change something in this plugin and its dependencies.

To run the integration tests from Eclipse, first run the above launch configuration.
Then, use the JUnit launch `Run ITs from Eclipse.launch` that appropriately passes the required properties `testMavenRepo`, pointing to the right `target/.m2test` absolute path, and `testSettingsXML`, pointing to the IT settings that disable remote snapshots.

The launch configuration `Run ITs from Eclipse.launch` can be used to run only the integration tests from Maven, making sure the requirements are also up-to-date (i.e., by using the command line argument `-am`).