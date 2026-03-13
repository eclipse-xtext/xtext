pipeline {
  agent {
    kubernetes {
      inheritFrom 'ubuntu-2404-6gb'
    }
  }

  parameters {
    choice(name: 'TARGET_PLATFORM', choices: ['r202512', 'r202603', 'latest'], description: 'Which Target Platform should be used?')
    // see https://wiki.eclipse.org/Jenkins#JDK
    choice(name: 'JDK_VERSION', choices: [ '21', '25' ], description: 'Which JDK version should be used?')
  }

  triggers {
    parameterizedCron(env.BRANCH_NAME == 'main' ? '''
      H H(0-1) * * * %TARGET_PLATFORM=r202512;JDK_VERSION=21
      H H(3-4) * * * %TARGET_PLATFORM=latest;JDK_VERSION=25
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
        // Set all Java version that can be discovered/selected by maven-toolchains-plugin
        JAVA_21_HOME = tool(type:'jdk', name:'temurin-jdk21-latest')
        JAVA_25_HOME = tool(type:'jdk', name:'temurin-jdk25-latest')
      }
      steps {
        xvnc(useXauthority: true) {
          sh """
            jdkEnvVar='JAVA_${javaVersion()}_HOME'
            if [ "${JAVA_HOME}" = "\${JAVA_${javaVersion()}_HOME}" ]; then
              # Workaround for https://github.com/apache/maven-toolchains-plugin/pull/148
              # clear the structured variable and require JAVA_HOME instead
              export JAVA_${javaVersion()}_HOME=''
              jdkEnvVar='JAVA_HOME'
            fi
            ./full-build.sh --tp=${params.TARGET_PLATFORM} \
              -Pstrict-jdk-${javaVersion()} -Dtoolchain.jdk.env=\${jdkEnvVar}
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
      archiveArtifacts artifacts: 'org.eclipse.xtend.ide.swtbot.tests/screenshots/**, **/target/work/data/.metadata/.log, **/target/work/data/.metadata/bak*.log, **/hs_err_pid*.log'
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

/**
 * Returns the Eclipse version dependent on the selected target platform.
 * Result: '4.XX'
 */
def eclipseVersion() {
  def targetPlatform = params.TARGET_PLATFORM
  if (targetPlatform == 'latest') {
    return "4.40"
  } else {
    def baseDate = java.time.LocalDate.parse("2018-06-01") // 4.8 Photon
    def df = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
    def targetDate = java.time.LocalDate.parse(targetPlatform.substring(1)+"01", df)
    long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(baseDate, targetDate);
    return "4."+ (8+(monthsBetween/3))
  }
}
