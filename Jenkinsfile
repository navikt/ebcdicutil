node {
    def project = "teampesys"
    def application = "ebcdicutil"

    /* metadata */
    def committer, pom, releaseVersion, nextVersion

    def mvnHome = tool "maven-3.3.9"
    def mvn = "${mvnHome}/bin/mvn"

    stage("checkout") {
        git url: "ssh://git@stash.devillo.no:7999/${project}/${application}.git"
    }

    stage("initialize") {
        pom = readMavenPom file: 'pom.xml'
        releaseVersion = pom.version.tokenize("-")[0]
        nextVersion = (releaseVersion.toInteger() + 1) + "-SNAPSHOT"

        /* gets the person who committed last as "Surname, First name (email@domain.tld) */
        committer = sh(script: 'git log -1 --pretty=format:"%an (%ae)"', returnStdout: true).trim()
    }

    stage("build") {
        sh "${mvn} clean install -Djava.io.tmpdir=/tmp/${application} -B -e"
    }

    stage("release") {
        sh "${mvn} versions:set -B -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false"
        sh "git commit -am 'Commit before creating tag ${application}-${releaseVersion}, by ${committer}'"
        sh "${mvn} clean deploy scm:tag -DskipTests -B -e"
    }

    stage("new dev version") {
        sh "${mvn} versions:set -B -DnewVersion=${nextVersion} -DgenerateBackupPoms=false"
        sh "git commit -am 'Updated version after release by ${committer}'"
        sh "git push origin master"
    }

}