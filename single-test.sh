#!/usr/bin/env bash

usage() {
  echo "Run tests for a single Xtext test project"
  echo ""
  echo "Usage: ./single-test.sh <test-project-artifact-id> [Maven arguments...]"
  echo ""
  echo "Example: ./single-test.sh org.eclipse.xtext.tests -Dtest=MyTest#myTest"
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

./mvnw -f org.eclipse.xtext.full.releng \
  -pl ":$TEST_PROJECT" -am \
  -DskipXtend -Dtycho.localArtifacts=ignore \
  verify \
  "$@"
