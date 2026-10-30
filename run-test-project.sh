#!/usr/bin/env bash

usage() {
  echo "Run tests for a single Xtext test project"
  echo ""
  echo "Usage: ./run-test-project.sh <test-project-artifact-id> [Maven arguments...]"
  echo ""
  echo "Example: ./run-test-project.sh org.eclipse.xtext.tests -Dtest=MyTest#myTest"
}

if [ "$#" -eq 0 ] || [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
  usage
  if [ "$#" -eq 0 ]; then
    exit 1
  fi
  exit 0
fi

TEST_PROJECT=$1
shift

RUNNING_SPECIFIED_TESTS=false
for arg in "$@"; do
  case "$arg" in
    -Dtest|-Dtest=*)
      RUNNING_SPECIFIED_TESTS=true
      break
      ;;
  esac
done

if [ "$RUNNING_SPECIFIED_TESTS" = true ]; then
  set -- "$@" -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false
fi

./mvnw -f org.eclipse.xtext.full.releng \
  -pl ":$TEST_PROJECT" -am \
  -DskipXtend -Dtycho.localArtifacts=ignore \
  verify \
  "$@"
