#!groovy
@Library('jenkins-pipeline-shared@feature/version') _

pipeline {
    environment {
        RELEASE_TYPE = "PATCH"
        BRANCH_DEV = "develop"
        BRANCH_TEST = "release"
        BRANCH_PROD = "master"
        DEPLOY_DEV = "dev"
        DEPLOY_TEST = "test"
        DEPLOY_PROD = "prod"
        GIT_TYPE = "Github"
        GIT_CREDS = "github-sbr-user"
    }
    options {
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '30'))
        timeout(time: 30, unit: 'MINUTES')
    }
    agent any
    stages {
        stage('Checkout') {
            agent any
            steps {
                deleteDir()
                checkout scm
                stash name: 'app'
                sh "$SBT version"
                script {
                    version = '1.0.' + env.BUILD_NUMBER
                    currentBuild.displayName = version
                    env.NODE_STAGE = "Checkout"
                }
            }
        }
        stage('Build'){
            agent any
            steps {
                colourText("info", "Building ${env.BUILD_ID} on ${env.JENKINS_URL} from branch ${env.BRANCH_NAME}")
                dir('gitlab') {
                    git(url: "$GITLAB_URL/StatBusReg/sbr-query-api.git", credentialsId: 'sbr-gitlab-id', branch: 'develop')
                }
                // Replace fake enterprise data with real data
                sh 'rm -rf conf/sample/201706/sbr-2500-ent-data.csv'
                sh 'rm -rf conf/sample/201708/sbr-2500-ent-data.csv'

                sh 'cp gitlab/dev/data/201706/sbr-2500-ent-data.csv conf/sample/201706/sbr-2500-ent-data.csv'
                sh 'cp gitlab/dev/data/201708/sbr-2500-ent-data.csv conf/sample/201708/sbr-2500-ent-data.csv'

                sh '$SBT clean compile "project api" universal:packageBin coverage test coverageReport'

                script {
                    env.NODE_STAGE = "Build"
                    if (BRANCH_NAME == "BRANCH_DEV") {
                        env.DEPLOY_NAME = "DEPLOY_DEV"
                        sh 'cp target/universal/sbr-query-api-*.zip dev-ons-sbr-query-api.zip'
                    }
                    else if  (BRANCH_NAME == "BRANCH_TEST") {
                        env.DEPLOY_NAME = "DEPLOY_TEST"
                        sh 'cp target/universal/sbr-query-api-*.zip test-ons-sbr-query-api.zip'
                    }
                    else if (BRANCH_NAME == "BRANCH_PROD") {
                        env.DEPLOY_NAME = "DEPLOY_PROD"
                        sh 'cp target/universal/sbr-query-api-*.zip prod-ons-sbr-query-api.zip'
                    }
                }
            }
        }
        stage('Static Analysis') {
            agent any
            steps {
                parallel (
                        "Unit" :  {
                            colourText("info","Running unit tests")
                            // sh "$SBT test"
                        },
                        "Style" : {
                            colourText("info","Running style tests")
                            sh '''
                                $SBT scalastyleGenerateConfig
                                $SBT scalastyle
                            '''
                        },
                        "Additional" : {
                            colourText("info","Running additional tests")
                            sh "$SBT scapegoat"
                        }
                )
            }
            post {
                always {
                    script {
                        env.NODE_STAGE = "Static Analysis"
                    }
                }
                success {
                    colourText("info","Generating reports for tests")
                    //   junit '**/target/test-reports/*.xml'

                    step([$class: 'CoberturaPublisher', coberturaReportFile: '**/target/scala-2.11/coverage-report/*.xml'])
                    step([$class: 'CheckStylePublisher', pattern: 'target/scalastyle-result.xml, target/scala-2.11/scapegoat-report/scapegoat-scalastyle.xml'])
                }
                failure {
                    colourText("warn","Failed to retrieve reports.")
                }
            }
        }
        stage ('Bundle') {
            agent any
            when {
                anyOf {
                    branch "develop"
                    branch "release"
                    branch "master"
                }
            }
            steps {
                script {
                    env.NODE_STAGE = "Bundle"
                }
                colourText("info", "Bundling....")
                //packageApp('dev')
                //packageApp('test')
                stash name: "zip"
            }
        }
        stage ('Releases') {
            agent any
            when {
                branch "master"
            }
            steps {
                script {
                    env.NODE_STAGE = "Releases"
                    currentTag = getLatestGitTag()
                    colourText("info", "Found latest tag: ${currentTag}")
                    newTag =  IncrementTag( currentTag, RELEASE_TYPE )
                    colourText("info", "Generated new tag: ${newTag}")
                    push(newTag, currentTag)
                }
            }
        }
        stage ('Package and Push Artifact') {
            agent any
            when {
                branch "master"
            }
            steps {
                sh '''
                    $SBT clean compile package
                    $SBT clean compile assembly
                '''
                colourText("success", 'Package.')
            }

        }
        stage('Deploy'){
            agent any
            when {
                anyOf {
                    branch "develop"
                    branch "release"
                    branch "master"
                }
            }
            steps {
                colourText("success", 'Deploy.')
                script {
                    env.NODE_STAGE = "Deploy"
                }
                milestone(1)
                lock('Deployment Initiated') {
                    colourText("info", 'deployment in progress')
                    deploy()
                    // unstash zip
                }
            }
        }


    }
    post {
        always {
            script {
                colourText("info", 'Post steps initiated')
                deleteDir()
            }
        }
        success {
            colourText("success", "All stages complete. Build was successful.")
            sendNotifications currentBuild.result, "\$SBR_EMAIL_LIST"
        }
        unstable {
            colourText("warn", "Something went wrong, build finished with result ${currentResult}. This may be caused by failed tests, code violation or in some cases unexpected interrupt.")
            sendNotifications currentBuild.result, "\$SBR_EMAIL_LIST", "${env.NODE_STAGE}"
        }
        failure {
            colourText("warn","Process failed at: ${env.NODE_STAGE}")
            sendNotifications currentBuild.result, "\$SBR_EMAIL_LIST", "${env.NODE_STAGE}"
        }
    }
}

def push (String newTag, String currentTag) {
    echo "Pushing tag ${newTag} to Gitlab"
    GitRelease( GIT_CREDS, newTag, currentTag, "${env.BUILD_ID}", "${env.BRANCH_NAME}", GIT_TYPE)
}

def deploy () {
    echo "Deploying Api app to ${env.DEPLOY_NAME}"
    withCredentials([string(credentialsId: "sbr-api-dev-secret-key", variable: 'APPLICATION_SECRET')]) {
        deployToCloudFoundry("cloud-foundry-sbr-${env.DEPLOY_NAME}-user", 'sbr', "${env.DEPLOY_NAME}", "${env.DEPLOY_NAME}-sbr-query-api", "${env.DEPLOY_NAME}-ons-sbr-query-api.zip", "gitlab/${env.DEPLOY_NAME}/manifest.yml")
    }
}
