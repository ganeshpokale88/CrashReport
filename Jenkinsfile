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
