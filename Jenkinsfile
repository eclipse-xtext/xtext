pipeline {
  agent any

  stages {
    stage ('Checkout') {
      steps {
        checkout scm
        dir ('deploy-xtext-git-repo') {
          git credentialsId: '29d79994-c415-4a38-9ab4-7463971ba682', url: 'ssh://genie.xtext@git.eclipse.org:29418/www.eclipse.org/Xtext', branch: 'master'
        }
        dir ('deploy-xtend-git-repo') {
          git credentialsId: '29d79994-c415-4a38-9ab4-7463971ba682', url: 'ssh://genie.xtext@git.eclipse.org:29418/www.eclipse.org/xtend', branch: 'master'
        }
      }
    }
    stage('Generate site') {
      steps {
        echo 'Building..'
        dir ('git-repo/xtext-website') {
          sh '''
            # generate things in _site
            ~/.rvm/gems/ruby-2.2.0/wrappers/jekyll build
          '''
        }
        dir ('git-repo/xtend-website') {
          sh '''
            # generate things in _site
            ~/.rvm/gems/ruby-2.2.0/wrappers/jekyll build
          '''
        }
      }
    }
    stage('Commit changes') {
      steps {
        dir ('deploy-xtext-git-repo') {
          sh '''
            cp -r $WORKSPACE/git-repo/xtext-website/_site/* .
            git diff
            git add --all :/ && git commit -m "Generated from commit: https://github.com/eclipse/xtext/commit/$GIT_COMMIT"
            git status
          '''
        }
        dir ('deploy-xtend-git-repo') {
          sh '''
            cp -r $WORKSPACE/git-repo/xtext-website/_site/* .
            git diff
            git add --all :/ && git commit -m "Generated from commit: https://github.com/eclipse/xtext/commit/$GIT_COMMIT"
            git status
          '''
        }
      }
    }
    stage('Deploy') {
      steps {
        echo 'Deploying....'
        dir ('deploy-xtext-git-repo') {
          sshagent(['29d79994-c415-4a38-9ab4-7463971ba682']) { // 
            sh '''
              git push origin master
            '''
          }
        }
        dir ('deploy-xtend-git-repo') {
          sshagent(['29d79994-c415-4a38-9ab4-7463971ba682']) { // 
            sh '''
              git push origin master
            '''
          }
        }
      }
    }
  }
}