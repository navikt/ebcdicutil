#!/usr/bin/env groovy
@Library('peon-pipeline') _

node {
    def project = "navikt"
    def application = "ebcdicutil"

    /* metadata */
    def commitHash, pom, releaseVersion, nextVersion

    def mvnHome = tool "maven-3.3.9"

    try {
        // delete whole workspace before starting the build,
        // so that the 'git clone' command below doesn't fail due to
        // directory not being empty
        cleanWs()

        stage("checkout") {
            // we are cloning the repository manually, because the standard 'git' and 'checkout' steps
            // infer with the Git polling that Jenkins already does (when polling for changes to the
            // repo containing the Jenkinsfile).
            sh "git clone https://github.com/navikt/ebcdicutil.git ."

            commitHash = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
            commitHashShort = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
            commitUrl = "https://github.com/${project}/${project}/commit/${commitHash}"

            /* gets the person who committed last as "Surname, First name" */
            committer = sh(script: 'git log -1 --pretty=format:"%an"', returnStdout: true).trim()

            github.commitStatus("navikt-ci-oauthtoken", "navikt/ebcdicutil", 'continuous-integration/jenkins', commitHash, 'pending', "Build #${env.BUILD_NUMBER} has started")
        }

        stage("initialize") {
            pom = readMavenPom file: 'pom.xml'
            releaseVersion = pom.version.tokenize("-")[0]
            nextVersion = (releaseVersion.toInteger() + 1) + "-SNAPSHOT"
        }

        stage("build") {
            withEnv(["PATH+MAVEN=${mvnHome}/bin"]) {
                sh "mvn clean verify -Djava.io.tmpdir=/tmp/${application} -B -e"
            }
        }

        stage("release") {
            withEnv(["PATH+MAVEN=${mvnHome}/bin"]) {
                sh "mvn versions:set -B -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false"
            }
            sh "git add '*pom.xml'"
            sh "git commit -m 'Commit before creating tag ${application}-${releaseVersion}'"
            sh "git tag -a '${application}-${releaseVersion}' -m '${application}-${releaseVersion}'"

            withCredentials([string(credentialsId: 'navikt-ci-oauthtoken', variable: 'GITHUB_OAUTH_TOKEN')]) {
                sh "git push --tags https://navikt-ci:${GITHUB_OAUTH_TOKEN}@github.com/navikt/${application}.git master"
            }

            withEnv(["PATH+MAVEN=${mvnHome}/bin"]) {
                sh "mvn clean deploy -DskipTests -B -e"
                sh "mvn versions:set -B -DnewVersion=${nextVersion} -DgenerateBackupPoms=false"
            }

            sh "git add '*pom.xml'"
            sh "git commit -m 'Updated version to ${nextVersion} after release'"

            withCredentials([string(credentialsId: 'navikt-ci-oauthtoken', variable: 'GITHUB_OAUTH_TOKEN')]) {
                sh "git push https://navikt-ci:${GITHUB_OAUTH_TOKEN}@github.com/navikt/${application}.git master"
            }
        }

        github.commitStatus("navikt-ci-oauthtoken", "navikt/ebcdicutil", 'continuous-integration/jenkins', commitHash, 'success', "Build #${env.BUILD_NUMBER} has finished")
        slackSend([
            color: 'good',
            message: "Build <${env.BUILD_URL}|#${env.BUILD_NUMBER}> (<${commitUrl}|${commitHashShort}>) of ${project}/${application}@master by ${committer} passed"
        ])
    } catch (e) {
        github.commitStatus("navikt-ci-oauthtoken", "navikt/ebcdicutil", 'continuous-integration/jenkins', commitHash, 'failure', "Build #${env.BUILD_NUMBER} has failed")
        slackSend([
            color: 'danger',
            message: "Build <${env.BUILD_URL}|#${env.BUILD_NUMBER}> (<${commitUrl}|${commitHashShort}>) of ${project}/${application}@master by ${committer} failed"
        ])

        throw e
    }
}
