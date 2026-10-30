This page explains common tasks related to working with Xtext's source code.

## Report an Issue

Please create a GitHub issue: https://github.com/eclipse-xtext/xtext/issues.

### Milestones

We use a milestone for each Xtext release. Fixed issues should be assigned to the milestone of the upcoming release in which the fix will be included. For example, if an issue is fixed on the `maintenance` branch, the corresponding milestone is the next service release (increasing the patch segment of the version). The next major or minor release should be assigned if an issue is fixed on the `main` branch.

It is possible to assign issues to a milestone before they are fixed, which can be useful for planning. However, make sure someone will actually work on that issue when you do that!

The list of issues assigned to a milestone gives a nice overview of new features and fixed bugs after a release has been done, facilitating the creation of release notes and communicating to users when a fix will be available.

### Issue Labels

Issue labels can serve several purposes:

* Indicate the issue type: `bug`, `enhancement`, `new_feature`, `question`
* Communicate the current status of an issue: `confirmed`, `help_wanted`
* Communicate why an issue has not been fixed: `duplicate`, `invalid`, `wontfix`
* Categorize issues for better overview, e.g. to assign an issue to a specific part of the software. Committers may add categorization labels as required. However, in most cases the `[category]` prefixing in the issue title should be sufficient.

In general, we want to keep the number of labels as low as possible to avoid overwhelming contributors and ensure the labels are used. A label is not helpful if it is assigned only to a fraction of the issues it belongs to, and that's why all contributors need to use the existing labels consistently.

### Workflow

All committers should feel responsible to read incoming issues. When you read a new issue, please think about an appropriate reaction:

* If possible, assign a type and a status.
* If appropriate, close the issue and explain why it won't be fixed.
* If the report seems reasonable, a comment with some feedback to the reporter would be nice (e.g. describe which action is required next to confirm or solve the issue), especially if the reporter is not a committer.
* If you are not familiar enough with the topic of the issue, you might find someone who is and delegate the action.

## Set up your Eclipse Workspace
Before starting the setup process, make sure that you have both an [Eclipse](https://accounts.eclipse.org/user/register) and a [GitHub](https://github.com/join) account.

The easiest way to set up a Xtext development environment is to use the following automated Oomph setup configuration link:

[![Create Eclipse Development Environment for Xtext](https://download.eclipse.org/oomph/www/setups/svg/Xtext.svg)](https://www.eclipse.org/setups/installer/?url=https://raw.githubusercontent.com/eclipse-xtext/xtext/main/releng/org.eclipse.xtext.contributor/XtextConfiguration.setup&show=true "Click to open Eclipse-Installer Auto Launch or drag onto your running installer's title area")

Alternatively you can do those steps manually as follows:

1. Download and start the [Eclipse Installer](https://www.eclipse.org/downloads/packages/installer).
2. On the initial page, open the hamburger menu and click on *Advanced Mode...*.
3. On the *Product* page, select *Eclipse IDE for Eclipse Committers*.
4. On the *Projects* page, select the *Xtext* node.
5. Choose your preferred installation settings on the *Variables* page. Tick the *Show all variables* checkbox to be able to choose your preferred authentication method in the *Github Repository* drop-down menu. If you don't have an SSH key registered with GitHub then make sure that you select `HTTPS (read-only, anonymous)` or `HTTPS (read-write)`.
6. Finish the wizard, drink a cup of coffee, and watch how your Xtext development environment is assembled.

## Contribute via a Fork
You need a [GitHub](https://github.com/join) and an [Eclipse](https://accounts.eclipse.org/user/register) account for which you signed the [Eclipse Contributor Agreement](https://accounts.eclipse.org/user/login?destination=user/eca).

 1. Make sure there is a GitHub issue for what you want to work on.
 2. Announce in the comments section that you want to work on the issue. Also, describe the solution you want to implement. To improve the chance that your contribution is accepted, you'll want to wait for the feedback of the committers.
 3. Implement your change. Take care to follow the [quality guidelines](QUALITY_GUIDELINES.md) to improve the chance that your contribution is accepted by a committer.
 4. [Sign off](https://git-scm.com/docs/git-commit#git-commit--s) your commits using the same email address you are using for your Eclipse account.
 5. Run the Maven/Gradle build locally to see if everything compiles and all tests pass.
 6. Push your changes to your forked repository. It is recommended to create a separate branch, this will make it easier to include the feedback from committers and update your changes.
 7. Create a [pull request](https://help.github.com/articles/using-pull-requests/).

## Contribute as a Committer
You're a committer if you have write-access to the Xtext git-repositories.

 1. Make sure there is a GitHub issue for what you want to work on
 2. Assign the issue to yourself.
 3. Create a local git branch.
 4. Implement your change. Take care to follow the [quality guidelines](QUALITY_GUIDELINES.md).
 5. [Sign off](https://git-scm.com/docs/git-commit#git-commit--s) your commits using the same email address you are using for your Eclipse account.
 6. Push the branch into the repository.
 7. [Jenkins](https://ci.eclipse.org/xtext) will automatically create a job for your branch and build it. A few GitHub Actions workflows are also started.
 9. If, and only if, all tests have passed, create a pull request and ask somebody who is familiar with the code you modified to review it. Note that pull requests from this repository will not be built: only external pull requests are built, so please make sure your branch is up-to-date with the main branch.
 10. If the reviewer approves, merge.

## Run Tests in a Single Test Project

From the repository root, select a test project's artifact ID in the full reactor and run Maven directly:

```sh
./mvnw -f org.eclipse.xtext.full.releng -pl :org.eclipse.xtext.tests -am \
  -DskipXtend -Dtycho.localArtifacts=ignore \
  verify
```

Replace `org.eclipse.xtext.tests` with the desired project. As an alternative, use `run-test-project.sh`, which runs the same command:

```sh
./run-test-project.sh org.eclipse.xtext.tests
```

In Eclipse, each test project also has a Maven launch configuration for running this command. For example, use `org.eclipse.xtext.tests/mvn verify org.eclipse.xtext.tests.launch` for the `org.eclipse.xtext.tests` project.

Generate the MWE2 and Xtend sources first (for example, as part of the workspace setup); the skip options reuse those generated sources, so do not run `clean` with either command. Maven also builds prerequisite projects, and may run tests in prerequisite test projects. Eclipse UI tests require a graphical display.

To run a single test class or method, pass the Maven `-Dtest` option after the project artifact ID:

```sh
./run-test-project.sh org.eclipse.xtext.tests -Dtest=MyTest
./run-test-project.sh org.eclipse.xtext.tests -Dtest=MyTest#myTest
```

When invoking Maven with `-Dtest` in the multi-project reactor, also pass `-DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false`. This allows prerequisite projects without a matching test to succeed; `run-test-project.sh` adds these options automatically when it detects `-Dtest`.

## Create a Release Locally
 1. Run the Maven build locally by calling the shell script [`full-build.sh`](https://github.com/eclipse-xtext/xtext/blob/main/full-build.sh). You may want to pass `--no-tests` to the script to skip all tests (keep in mind that with tests the build might take more than 1 hour).
 2. Find the artifacts at `build/maven-repository` or `build/p2-repository` in a folder relative to the repository root.

## Consume the Latest Artifacts
There are two ways/sources: 

 * All Maven artifacts are published every 24 hours to the [public Maven Central snapshot repository](https://central.sonatype.com/repository/maven-snapshots/) and can be consumed from there.
 * The [Jenkins](https://ci.eclipse.org/xtext) archives the created repositories. You can find the repository of your choice at: `https://ci.eclipse.org/xtext/job/xtext/job/<git-branch>/(lastSuccessfulBuild|<build-number>)/artifact/build/(maven|p2)-repository/`

## Eclipse Contributor Agreement

Before your contribution can be accepted by the project team, contributors must
electronically sign the Eclipse Contributor Agreement (ECA).

* http://www.eclipse.org/legal/ECA.php

Commits that are provided by non-committers must have a Signed-off-by field in
the footer indicating that the author is aware of the terms by which the
contribution has been provided to the project. The non-committer must
additionally have an Eclipse Foundation account and must have a signed Eclipse
Contributor Agreement (ECA) on file.

For more information, please see the Eclipse Committer Handbook:
https://www.eclipse.org/projects/handbook/#resources-commit
