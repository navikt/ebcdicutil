node {
    def application = "ebcdicutil"

    /* metadata */
    def pom, releaseVersion, nextVersion

    def mvnHome = tool "maven-3.3.9"
    def mvn = "${mvnHome}/bin/mvn"

    stage("checkout") {
        git url: "https://github.com/navikt/ebcdicutil.git"
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

}