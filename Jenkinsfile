node {
    def application = "ebcdicutil"

    /* metadata */
    def commitHash, pom, releaseVersion, nextVersion

    def mvnHome = tool "maven-3.3.9"
    def mvn = "${mvnHome}/bin/mvn"

    try {
        // delete whole workspace before starting the build,
        // so that the 'git clone' command below doesn't fail due to
        // directory not being empty
        cleanWs()

        stage("checkout") {
            // we are cloning the repository manually, because the standard 'git' and 'checkout' steps
            // infer with the Git polling that Jenkins already does (when polling for changes to the
            // repo containing the Jenkinsfile).
            withEnv(['HTTPS_PROXY=http://webproxy-utvikler.nav.no:8088']) {
                        sh(script: "git clone https://github.com/navikt/ebcdicutil.git .")
            }

            commitHash = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()

            notifyGithub("navikt", "${application}", "${commitHash}", 'pending', "Build #${env.BUILD_NUMBER} has started")
        }

        stage("initialize") {
            pom = readMavenPom file: 'pom.xml'
            releaseVersion = pom.version.tokenize("-")[0]
            nextVersion = (releaseVersion.toInteger() + 1) + "-SNAPSHOT"
        }

        stage("build") {
            sh "${mvn} clean install -Djava.io.tmpdir=/tmp/${application} -B -e"
        }

        stage("release") {
            sh "${mvn} versions:set -B -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false"
            sh "git commit -am 'Commit before creating tag ${application}-${releaseVersion} [ci skip]'"

            withEnv(['HTTPS_PROXY=http://webproxy-utvikler.nav.no:8088']) {
                sh "${mvn} clean deploy scm:tag -DskipTests -B -e"
            }
        }

        stage("new dev version") {
            sh "${mvn} versions:set -B -DnewVersion=${nextVersion} -DgenerateBackupPoms=false"
            sh "git commit -am 'Updated version after release [ci skip]'"

            withEnv(['HTTPS_PROXY=http://webproxy-utvikler.nav.no:8088']) {
                withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'navikt-jenkins-github', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]) {
                    sh("git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/navikt/ebcdicutil.git master")
                }
            }
        }

        notifyGithub("navikt", "${application}", "${commitHash}", 'success', "Build #${env.BUILD_NUMBER} has finished")
    } catch (e) {
        notifyGithub("navikt", "${application}", "${commitHash}", 'failure', "Build #${env.BUILD_NUMBER} has failed")

        throw e
    }
}

def notifyGithub(owner, repo, sha, state, description) {
    def postBody = [
        state: "${state}",
        context: 'ci/jenkins',
        description: "${description}",
        target_url: "${env.BUILD_URL}"
    ]
    def postBodyString = groovy.json.JsonOutput.toJson(postBody)

    withEnv(['HTTPS_PROXY=http://webproxy-utvikler.nav.no:8088']) {
        withCredentials([string(credentialsId: 'navikt-jenkins-oauthtoken', variable: 'ACCESS_TOKEN')]) {
            sh "curl 'https://api.github.com/repos/${owner}/${repo}/statuses/${sha}?access_token=$ACCESS_TOKEN' \
                -H 'Content-Type: application/json' \
                -X POST \
                -d '${postBodyString}'"
        }
    }
}