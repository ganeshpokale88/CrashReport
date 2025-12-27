pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    environment {
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
                echo sdk.dir=C:\\Users\\Ganesh Pokale\\AppData\\Local\\Android\\Sdk > local.properties
                '''
            }
        }


        stage('Build') {
            steps {
                bat 'gradlew clean assemble'
            }
        }

        stage('Unit Tests') {
            steps {
                bat 'gradlew test'
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
