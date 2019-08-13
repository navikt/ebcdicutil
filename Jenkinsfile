@Library('peon-pipeline') _

pipeline {
    agent any

    environment {
        APP_NAME 	= "ebcdicutil"
        APP_TOKEN   = github.generateAppToken()
        MAVEN_HOME  = tool "maven-3.3.9"
    }

    stages {
        stage("checkout") {
            steps {
                script {
                    latestStage = env.STAGE_NAME
                    cleanWs()
                }
                script {
                    sh "git init"
                    sh "git pull https://x-access-token:${env.APP_TOKEN}@github.com/navikt/${env.APP_NAME}.git"
                    env.COMMIT_HASH_LONG = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
                    env.COMMIT_HASH_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    github.commitStatus("pending", "navikt/$env.APP_NAME", env.APP_TOKEN, env.COMMIT_HASH_LONG)
                }
            }
        }
        stage("build") {
            steps {
                script {
                    latestStage = env.STAGE_NAME
                    pom = readMavenPom file: 'pom.xml'
                    releaseVersion = pom.version.tokenize("-")[0]
                    nextVersion = (releaseVersion.toInteger() + 1) + "-SNAPSHOT"
                }
                script {
                    withEnv(["PATH+MAVEN=${MAVEN_HOME}/bin"]) {
                        sh "mvn clean verify -Djava.io.tmpdir=/tmp/${APP_NAME} -B -e"
                    }
                }
            }
        }
        stage("release") {
            steps {
                script {
                    latestStage = env.STAGE_NAME
                    withEnv(["PATH+MAVEN=${MAVEN_HOME}/bin"]) {
                        sh "mvn versions:set -B -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false"
                    }
                }
                script {
                    withEnv(["PATH+MAVEN=${MAVEN_HOME}/bin"]) {
                        sh "mvn clean deploy -DskipTests -B -e"
                        sh "mvn versions:set -B -DnewVersion=${nextVersion} -DgenerateBackupPoms=false"
                    }
                }
                script {
                    sh "git add '*pom.xml'"
                    sh "git commit -m 'Updated version to ${nextVersion} after release'"
                    sh "git push https://x-access-token:${env.APP_TOKEN}@github.com/navikt/${env.APP_NAME}.git master"
                }
            }
        }
    }

    post {
        success {
            script {
                github.commitStatus("success", "navikt/$env.APP_NAME", env.APP_TOKEN, env.COMMIT_HASH_LONG)
                slackSend([color  : 'good', message: "Successful $latestStage $env.APP_NAME:<https://github.com/navikt/$env.APP_NAME/commit/$COMMIT_HASH_LONG|`$COMMIT_HASH_SHORT`>"])
            }
        }
        failure {
            script {
                github.commitStatus("failure", "navikt/$env.APP_NAME", env.APP_TOKEN, env.COMMIT_HASH_LONG)
                slackSend([color  : 'danger', message: "Failed to $latestStage $env.APP_NAME:<https://github.com/navikt/$env.APP_NAME/commit/$COMMIT_HASH_LONG|`$COMMIT_HASH_SHORT`>"])
            }
        }
    }
}
