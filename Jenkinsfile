node {
    def project = "navikt"
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
            commitHashShort = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
            commitUrl = "https://github.com/${project}/${project}/commit/${commitHash}"

            /* gets the person who committed last as "Surname, First name" */
            committer = sh(script: 'git log -1 --pretty=format:"%an"', returnStdout: true).trim()

            notifyGithub("${project}", "${application}", "${commitHash}", 'pending', "Build #${env.BUILD_NUMBER} has started")
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
            sh "git add '*pom.xml'"
            sh "git commit -m 'Commit before creating tag ${application}-${releaseVersion}'"
            sh "git tag -a '${application}-${releaseVersion}' -m '${application}-${releaseVersion}'"

            withEnv(['HTTPS_PROXY=http://webproxy-utvikler.nav.no:8088']) {
                withCredentials([string(credentialsId: 'navikt-jenkins-oauthtoken', variable: 'GITHUB_OAUTH_TOKEN')]) {
                    sh("git push --tags https://navikt-jenkins:${GITHUB_OAUTH_TOKEN}@github.com/navikt/${application}.git master")
                }
            }

            withEnv(['HTTPS_PROXY=http://webproxy-utvikler.nav.no:8088']) {
                sh "${mvn} clean deploy -DskipTests -B -e"
            }

            sh "${mvn} versions:set -B -DnewVersion=${nextVersion} -DgenerateBackupPoms=false"
            sh "git add '*pom.xml'"
            sh "git commit -m 'Updated version to ${nextVersion} after release'"

            withEnv(['HTTPS_PROXY=http://webproxy-utvikler.nav.no:8088']) {
                withCredentials([string(credentialsId: 'navikt-jenkins-oauthtoken', variable: 'GITHUB_OAUTH_TOKEN')]) {
                    sh("git push https://navikt-jenkins:${GITHUB_OAUTH_TOKEN}@github.com/navikt/${application}.git master")
                }
            }
        }

        notifyGithub("${project}", "${application}", "${commitHash}", 'success', "Build #${env.BUILD_NUMBER} has finished")
        slackSend([
            color: 'good',
            message: "Build <${env.BUILD_URL}|#${env.BUILD_NUMBER}> (<${commitUrl}|${commitHashShort}>) of ${project}/${application}@master by ${committer} passed"
        ])
    } catch (e) {
        notifyGithub("${project}", "${application}", "${commitHash}", 'failure', "Build #${env.BUILD_NUMBER} has failed")

        slackSend([
            color: 'danger',
            message: "Build <${env.BUILD_URL}|#${env.BUILD_NUMBER}> (<${commitUrl}|${commitHashShort}>) of ${project}/${application}@master by ${committer} failed"
        ])

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