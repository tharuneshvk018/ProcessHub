
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(false)
    }

    environment {
        DOCKER = 'C:\\Users\\tharu\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe'
        MAVEN_IMAGE = 'maven:3.9.9-eclipse-temurin-21-alpine'
        NODE_IMAGE = 'node:22-alpine'
    }

    stages {

        stage('1. Git Checkout') {
            steps {
                echo 'Verifying ProcessHub source from GitHub'
                bat 'git log -1 --oneline'
            }
        }

        stage('2. Backend Validation and Testing') {
            steps {
                echo 'Validating backend dependencies and running JUnit tests'
                bat '''
                "%DOCKER%" run --rm -v "%WORKSPACE%:/workspace" -w /workspace/backend %MAVEN_IMAGE% mvn -B clean verify
                '''
            }
            post {
                always {
                    junit testResults: 'backend/target/surefire-reports/*.xml',
                          allowEmptyResults: true
                }
            }
        }

        stage('3. Frontend Build') {
            steps {
                echo 'Installing dependencies, linting and building frontend'
                bat '''
                "%DOCKER%" run --rm -v "%WORKSPACE%:/workspace" -w /workspace/frontend %NODE_IMAGE% sh -c "npm ci && npm run lint && npm run build"
                '''
            }
        }

        stage('4. Docker Image Build') {
            steps {
                echo 'Building ProcessHub backend and frontend images'
                bat '''
                "%DOCKER%" build -t processhub-backend:ci -f backend/Dockerfile backend
                "%DOCKER%" build -t processhub-frontend:ci -f frontend/Dockerfile frontend
                '''
            }
        }

        stage('5. Docker Compose Validation') {
            steps {
                echo 'Validating Docker Compose configuration without deploying'
                bat '''
                "%DOCKER%" compose config --quiet
                '''
            }
        }

        stage('6. Pipeline Report') {
            steps {
                echo 'Collecting test and coverage reports'
                archiveArtifacts artifacts: 'backend/target/site/jacoco/**,frontend/dist/**',
                                 allowEmptyArchive: true
                echo 'ProcessHub CI validation completed'
            }
        }
    }

    post {
        success {
            echo 'SUCCESS: ProcessHub CI pipeline completed.'
        }
        failure {
            echo 'FAILED: Check the stage logs above to identify the issue.'
        }
        always {
            echo 'ProcessHub Jenkins execution finished.'
        }
    }
}
