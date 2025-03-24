#!/usr/bin/env bash

MVN_ARGS=(\
  -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
)

# args parsing inspired by https://gist.github.com/jehiah/855086
while [ "$1" != "" ]; do
  PARAM=`echo $1 | awk -F= '{print $1}'`
  VALUE=`echo $1 | awk -F= '{print $2}'`
  case $PARAM in
    -h | --help)
      echo "Perform Maven and p2 deployment for Xtext"
      echo ""
      echo "./full-deploy.sh"
      echo -e "\t-h --help"
      echo -e "\t--no-tests Skip test execution"
      echo -e "\t--local-repository=<PATH> Use local Maven repository"
      echo -e "\t--tp=$TARGET_PLATFORM (valid values: r2024-03,r2024-06,...,latest)"
      echo ""
      exit
      ;;
    --no-tests)
      MVN_ARGS+=(-DskipTests=true)
      ;;
    --local-repository)
      MVN_ARGS+=" -Dmaven.repo.local=$VALUE"
      ;;
    --tp)
      MVN_ARGS+=" -Dtarget-platform-classifier=xtext-$VALUE"
      ;;
    *)
      # append any additionally passed arg
      if [ -z "$VALUE" ]; then
        MVN_ARGS+=($PARAM)
      else
        MVN_ARGS+=($PARAM=$VALUE)
      fi
      ;;
  esac
  shift
done

MVN_ARGS+=(-PuseJenkinsSnapshots)

MVN_ARGS+=("-Dmaven.home=$(./mvnw --version | grep "Maven home:" | cut -c 13-)")

echo ./mvnw -B -f org.eclipse.xtext.full.releng ${MVN_ARGS[@]} $@

./mvnw -B \
  -f org.eclipse.xtext.full.releng \
  clean deploy \
  ${MVN_ARGS[@]} \
  $@
