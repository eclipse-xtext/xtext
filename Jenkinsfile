pipeline {
  agent {
    kubernetes {
      inheritFrom 'ubuntu-2404-6gb'
    }
  }

  parameters {
    choice(name: 'TARGET_PLATFORM', choices: ['latest', 'r202403', 'r202406', 'r202409', 'r202412', 'r202503', 'r202506'], description: 'Which Target Platform should be used?')
    // see https://wiki.eclipse.org/Jenkins#JDK
    choice(name: 'JDK_VERSION', choices: [ '21' , '17'], description: 'Which JDK version should be used?')
  }

  triggers {
    parameterizedCron(env.BRANCH_NAME == 'cd_tycho50' ? '''
      H H(13-14) * * * %TARGET_PLATFORM=latest;JDK_VERSION=21
      ''' : '')
  }


  options {
    buildDiscarder(logRotator(numToKeepStr:'10'))
    disableConcurrentBuilds()
    timeout(time: 360, unit: 'MINUTES')
  }

  tools {
     // the Java version we use to run the build
     // we force the effective JDK version for compilation/testing through Maven toolchains
     jdk "temurin-jdk21-latest"
  }

  stages {
    stage('Initialize') {
      steps {
        checkout scm

        script {
          currentBuild.displayName = String.format("#%s(JDK%s,Eclipse%s)", BUILD_NUMBER, javaVersion(), eclipseVersion())
        }

        sh '''
            if [ -f "/sys/fs/cgroup/memory/memory.limit_in_bytes" ]; then
                echo "Available memory: $(cat /sys/fs/cgroup/memory/memory.limit_in_bytes | numfmt --to iec --format '%f')"
            fi

            sed_inplace() {
                if [[ "$OSTYPE" == "darwin"* ]]; then
                    sed -i '' "$@"
                else
                    sed -i "$@"
                fi
            }
        '''
      }
    }

    stage('Maven/Tycho Build & Test') {
      environment {
        MAVEN_OPTS = "-Xmx1500m"
        // set all Java versions needed by our toolchains.xml
        JAVA_HOME_17_X64 = tool(type:'jdk', name:'temurin-jdk17-latest')
        JAVA_HOME_21_X64 = tool(type:'jdk', name:'temurin-jdk21-latest')
      }
      steps {
        xvnc(useXauthority: true) {
          sh """
            ./full-build.sh --tp=${selectedTargetPlatform()} \
              ${javaVersion() == 17 ? '--toolchains releng/toolchains.xml -Pstrict-jdk-17' : ''} \
              ${javaVersion() == 21 ? '-Pstrict-jdk-21' : ''}
          """
        }
      }// END steps
    } // END stage

    stage('Trigger Snapshot Deployment') {
      when {
        allOf {
          expression {
            currentBuild.getBuildCauses().toString().contains('Push event to branch')
          }
          branch 'main'
        }
      }
      steps {
        build job: "xtext-monorepo-full-deploy-nightly", wait: false
      }
    }

  } // END stages

  post {
    always {
      junit testResults: '**/target/surefire-reports/*.xml'
    }
    success {
      archiveArtifacts artifacts: 'build/**, **/target/work/data/.metadata/.log, **/target/work/data/.metadata/bak*.log'
    }
    unsuccessful {
      archiveArtifacts artifacts: 'org.eclipse.xtend.ide.swtbot.tests/screenshots/**, **/target/work/data/.metadata/.log, **/target/work/data/.metadata/bak*.log, **/hs_err_pid*.log, **/target/surefire-reports/*'
    }
    cleanup {
      script {
        def curResult = currentBuild.currentResult
        def lastResult = 'NEW'
        if (currentBuild.previousBuild != null) {
          lastResult = currentBuild.previousBuild.result
        }

        if (curResult != 'SUCCESS' || lastResult != 'SUCCESS') {
          def color = ''
          switch (curResult) {
            case 'SUCCESS':
              color = '#00FF00'
              break
            case 'UNSTABLE':
              color = '#FFFF00'
              break
            case 'FAILURE':
              color = '#FF0000'
              break
            default: // e.g. ABORTED
              color = '#666666'
          }

          matrixSendMessage https: true,
            hostname: 'matrix.eclipse.org',
            accessTokenCredentialsId: "matrix-token",
            roomId: '!zbliqcdqsggOFDCUoF:matrix.eclipse.org',
            body: "${lastResult} => ${curResult} ${env.BUILD_URL} | ${env.JOB_NAME}#${env.BUILD_NUMBER}",
            formattedBody: "<div><font color='${color}'>${lastResult} => ${curResult}</font> | <a href='${env.BUILD_URL}' target='_blank'>${env.JOB_NAME}#${env.BUILD_NUMBER}</a></div>"
        }
      }
    }
  }
}

/** return the Java version as Integer (8, 11, ...) */
def javaVersion() {
  return Integer.parseInt(params.JDK_VERSION)
}

/** returns true when this build was triggered by an upstream build */
def isTriggeredByUpstream() {
  return !"[]".equals(currentBuild.getBuildCauses('org.jenkinsci.plugins.workflow.support.steps.build.BuildUpstreamCause').toString())
}

/**
 * Returns the Eclipse version dependent on the selected target platform.
 * Result: '4.XX'
 */
def eclipseVersion() {
  def targetPlatform = selectedTargetPlatform()
  if (targetPlatform == 'latest') {
    return "4.37"
  } else {
    def baseDate = java.time.LocalDate.parse("2018-06-01") // 4.8 Photon
    def df = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
    def targetDate = java.time.LocalDate.parse(targetPlatform.substring(1)+"01", df)
    long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(baseDate, targetDate);
    return "4."+ (8+(monthsBetween/3))
  }
}

/**
 * The target platform is primarily defined by the build parameter TARGET_PLATFORM.
 * But when the build is triggered by upstream with at least Java version 21, 'latest'
 * is returned.
 */
def selectedTargetPlatform() {
    def tp = params.TARGET_PLATFORM
    def isUpstream = isTriggeredByUpstream()
    def javaVersion = javaVersion()

    if (isTriggeredByUpstream() && javaVersion>=21) {
        println("Choosing 'latest' target since this build was triggered by upstream with Java ${javaVersion}")
        return 'latest'
    } else if (isTriggeredByUpstream() && javaVersion>=17) {
        println("Choosing 'r2024-03' target since this build was triggered by upstream with Java ${javaVersion}")
        return 'r2024-03'
    } else {
        return tp
    }
}
