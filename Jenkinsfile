pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    environment {
        ANDROID_SDK_ROOT = "C:/Android/Sdk"
        ANDROID_HOME = ""
        GRADLE_USER_HOME = "C:/gradle-cache"
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
                del local.properties 2>nul
                echo sdk.dir=C:/Android/Sdk>local.properties
                type local.properties
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
