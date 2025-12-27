pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    environment {
        ANDROID_HOME = "C:\\Android\\Sdk"
        GRADLE_USER_HOME = "C:\\gradle-cache"
        GRADLE_OPTS = "-Dorg.gradle.daemon=false"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Setup Android SDK') {
            steps {
                bat '''
                echo sdk.dir=C:\\Android\\Sdk > local.properties
                '''
            }
        }

        stage('Build') {
            steps {
                bat 'gradlew clean assembleDebug --stacktrace'
            }
        }

        stage('Unit Tests') {
            steps {
                bat 'gradlew testDebugUnitTest'
            }
        }
    }

    post {
        success {
            echo '✅ Build & Tests Passed'
        }
        failure {
            echo '❌ Build Failed'
        }
    }
}
